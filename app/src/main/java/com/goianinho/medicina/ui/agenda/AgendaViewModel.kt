package com.goianinho.medicina.ui.agenda

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.goianinho.medicina.GoianinhoApp
import com.goianinho.medicina.data.MEDICO_TESTE_ID
import com.goianinho.medicina.data.model.Consulta
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.model.Horario
import com.goianinho.medicina.data.repository.AgendaRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.goianinho.medicina.ui.comum.consultaDoMomento
import com.goianinho.medicina.ui.comum.mensagemDeErro
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------- Modelos de UI ----------

data class DiaAgenda(val data: String, val diaSemana: String, val diaMes: String)

// A-01: o horário carrega a própria data/instante — a reserva nunca usa o estado "atual" da tela
data class HorarioUi(val id: String, val hora: String, val data: String, val instante: Date)

data class ProximaConsultaUi(val data: String, val hora: String)

data class ConsultaProximaUi(
    val hora: String,
    val pacienteNome: String,
    val resumoPendente: Boolean,
    val dicaEngajamento: String
)

data class ConsultaAtualUi(
    val hora: String,
    val pacienteNome: String,
    val resumoPendente: Boolean,
    val briefing: String
)

// ---------- Estado da Agenda do paciente ----------

sealed interface AgendaPacienteUiState {
    data object Carregando : AgendaPacienteUiState
    data class Erro(val mensagem: String) : AgendaPacienteUiState
    data class Sucesso(
        val medicoNome: String,
        val proximaConsulta: ProximaConsultaUi?,
        val dias: List<DiaAgenda>,
        val diaSelecionado: String,
        val horariosCarregando: Boolean,
        val horariosErro: String?,
        val horarios: List<HorarioUi>,
        val horarioSelecionadoId: String?,
        val reservando: Boolean,
        val aviso: String?,
        val erroReserva: String?
    ) : AgendaPacienteUiState {
        val horarioSelecionado: HorarioUi?
            get() = horarios.firstOrNull { it.id == horarioSelecionadoId }
        val diaSelecionadoRotulo: String
            get() = dias.firstOrNull { it.data == diaSelecionado }
                ?.let { "${it.diaSemana}, ${it.diaMes}" }.orEmpty()
    }
}

// ---------- Estado da Agenda do dia (médico) ----------

sealed interface AgendaMedicoUiState {
    data object Carregando : AgendaMedicoUiState
    data class Erro(val mensagem: String) : AgendaMedicoUiState
    data class Sucesso(
        val diaRotulo: String,
        val consultaAtual: ConsultaAtualUi?,
        val proximas: List<ConsultaProximaUi>
    ) : AgendaMedicoUiState {
        val totalConsultas: Int get() = proximas.size + if (consultaAtual != null) 1 else 0
    }
}

class AgendaViewModel(
    private val repo: AgendaRepository,
    private val documentoRepo: DocumentoRepository
) : ViewModel() {

    private val diaSelecionadoManual = MutableStateFlow<String?>(null)
    private val horarioSelecionadoId = MutableStateFlow<String?>(null)
    private val estadoReserva = MutableStateFlow(EstadoReserva())
    private val retryHorarios = MutableStateFlow(0)
    private val retryMedico = MutableStateFlow(0)

    private data class EstadoReserva(
        val reservando: Boolean = false,
        val aviso: String? = null,
        val erro: String? = null
    )

    // A-11: recalcula sempre que o dia virar — não trava no "hoje" de quando o app abriu
    private val chaveDoDiaFlow: Flow<String> = tickerDeUmMinuto()
        .map { FORMATO_DATA.format(Date()) }
        .distinctUntilChanged()

    private val diasFlow: Flow<List<DiaAgenda>> = chaveDoDiaFlow.map { proximosDias(QTD_DIAS_SELETOR) }

    private val diaSelecionadoFlow: Flow<String> =
        combine(diasFlow, diaSelecionadoManual) { dias, manual ->
            manual?.takeIf { d -> dias.any { it.data == d } } ?: dias.first().data
        }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val horariosFlow: Flow<ResultadoHorarios> =
        combine(diaSelecionadoFlow, retryHorarios) { dia, _ -> dia }
            .flatMapLatest { dia ->
                repo.ouvirHorariosLivres(MEDICO_TESTE_ID, dia)
                    .map<List<Horario>, ResultadoHorarios> { lista ->
                        ResultadoHorarios.Sucesso(
                            lista.mapNotNull(::horarioUiValido).filter { horarioAindaValeAPena(it, dia) }
                        )
                    }
                    .onStart { emit(ResultadoHorarios.Carregando) }
                    .catch { e ->
                        Log.e("Goianinho", "ouvirHorariosLivres falhou", e)
                        emit(ResultadoHorarios.Erro(mensagemDeErro(e)))
                    }
            }

    private val proximaConsultaFlow: Flow<ProximaConsultaUi?> =
        repo.ouvirProximaConsulta(PACIENTE_ID)
            .map { consulta ->
                consulta?.dataHora?.toDate()?.let { ProximaConsultaUi(formatarDiaSemanaEData(it), formatarHora(it)) }
            }
            .catch { e ->
                Log.e("Goianinho", "ouvirProximaConsulta falhou", e)
                emit(null)
            }

    private data class EstadoBasePaciente(
        val proximaConsulta: ProximaConsultaUi?,
        val dias: List<DiaAgenda>,
        val diaSelecionado: String,
        val horariosResultado: ResultadoHorarios
    )

    private val estadoBasePacienteFlow: Flow<EstadoBasePaciente> = combine(
        proximaConsultaFlow, diasFlow, diaSelecionadoFlow, horariosFlow
    ) { proxima, dias, diaSel, horariosResultado ->
        EstadoBasePaciente(proxima, dias, diaSel, horariosResultado)
    }

    val uiStatePaciente: StateFlow<AgendaPacienteUiState> = combine(
        estadoBasePacienteFlow, horarioSelecionadoId, estadoReserva
    ) { base, horarioSelId, reserva ->
        AgendaPacienteUiState.Sucesso(
            medicoNome = "[NOME DO MÉDICO]",
            proximaConsulta = base.proximaConsulta,
            dias = base.dias,
            diaSelecionado = base.diaSelecionado,
            horariosCarregando = base.horariosResultado is ResultadoHorarios.Carregando,
            horariosErro = (base.horariosResultado as? ResultadoHorarios.Erro)?.mensagem,
            horarios = (base.horariosResultado as? ResultadoHorarios.Sucesso)?.horarios.orEmpty(),
            horarioSelecionadoId = horarioSelId,
            reservando = reserva.reservando,
            aviso = reserva.aviso,
            erroReserva = reserva.erro
        ) as AgendaPacienteUiState
    }
        .catch { e -> emit(AgendaPacienteUiState.Erro(mensagemDeErro(e))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgendaPacienteUiState.Carregando)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val consultasHojeFlow: Flow<List<Consulta>> =
        combine(chaveDoDiaFlow, retryMedico) { dia, _ -> dia }
            .flatMapLatest { diaKey ->
                val (inicio, fim) = intervaloDoDia(diaKey)
                repo.ouvirConsultasDoDia(MEDICO_TESTE_ID, inicio, fim)
                    .map { lista -> lista.filter { it.status != "cancelada" } }
            }

    private val pendentesFlow: Flow<Set<String>> =
        ouvirPendentesComoFlow(documentoRepo, MEDICO_TESTE_ID)
            .map { lista -> lista.map { it.pacienteId }.toSet() }
            .catch { e ->
                Log.e("Goianinho", "ouvirPendentes falhou", e)
                emit(emptySet())
            }

    val uiStateMedico: StateFlow<AgendaMedicoUiState> = combine(
        chaveDoDiaFlow, consultasHojeFlow, pendentesFlow, tickerDeUmMinuto()
    ) { diaKey, consultas, comPendencia, _ ->
        val ordenadas = consultas.sortedBy { it.dataHora?.seconds ?: Long.MAX_VALUE }
        val atual = consultaDoMomento(consultas)
        val proximas = ordenadas.filter { it !== atual }.map { consulta ->
            ConsultaProximaUi(
                hora = consulta.dataHora?.toDate()?.let(::formatarHora).orEmpty(),
                pacienteNome = "[PACIENTE]",
                resumoPendente = consulta.pacienteId in comPendencia,
                dicaEngajamento = "Dica de engajamento (texto fixo nesta fase)"
            )
        }
        AgendaMedicoUiState.Sucesso(
            diaRotulo = rotuloDoDia(diaKey),
            consultaAtual = atual?.let {
                ConsultaAtualUi(
                    hora = it.dataHora?.toDate()?.let(::formatarHora).orEmpty(),
                    pacienteNome = "[PACIENTE]",
                    resumoPendente = it.pacienteId in comPendencia,
                    briefing = "Motivo relatado: [MOTIVO DA CONSULTA]. Aguardando confirmação do resumo."
                )
            },
            proximas = proximas
        ) as AgendaMedicoUiState
    }
        .catch { e -> emit(AgendaMedicoUiState.Erro(mensagemDeErro(e))) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgendaMedicoUiState.Carregando)

    fun selecionarDia(data: String) {
        diaSelecionadoManual.value = data
        horarioSelecionadoId.value = null
    }

    fun selecionarHorario(horario: HorarioUi) {
        if (estadoReserva.value.reservando) return
        horarioSelecionadoId.value = horario.id
    }

    fun tentarDeNovoHorarios() { retryHorarios.update { it + 1 } }

    fun recarregarMedico() { retryMedico.update { it + 1 } }

    fun limparAviso() { estadoReserva.update { it.copy(aviso = null) } }

    fun limparErroReserva() { estadoReserva.update { it.copy(erro = null) } }

    // RN-08 e RNF-03: clique duplo não cria duas consultas
    fun confirmarReserva() {
        if (estadoReserva.value.reservando) return
        val horario = (uiStatePaciente.value as? AgendaPacienteUiState.Sucesso)?.horarioSelecionado ?: return

        viewModelScope.launch {
            estadoReserva.update { it.copy(reservando = true, erro = null, aviso = null) }
            repo.reservar(
                horario = Horario(id = horario.id, medicoId = MEDICO_TESTE_ID, data = horario.data, hora = horario.hora),
                pacienteId = PACIENTE_ID,
                dataHora = Timestamp(horario.instante)
            ).onSuccess {
                horarioSelecionadoId.value = null
                estadoReserva.update {
                    it.copy(reservando = false, aviso = "Consulta marcada para ${rotuloCurto(horario.data)} às ${horario.hora}!")
                }
            }.onFailure { e ->
                Log.e("Goianinho", "reservar falhou", e)
                estadoReserva.update { it.copy(reservando = false, erro = mensagemDeErro(e)) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GoianinhoApp
                AgendaViewModel(app.container.agendaRepository, app.container.documentoRepository)
            }
        }
    }
}

// ---------- Privado ao arquivo ----------

// TODO (Fase 3): trocar pelo uid do paciente logado de verdade quando o Módulo 1 (Login)
// migrar e expuser o usuário atual. Precisa bater com o cadastrado no Firestore.
private const val PACIENTE_ID = "paciente-teste-1"
private const val QTD_DIAS_SELETOR = 14

private sealed interface ResultadoHorarios {
    data object Carregando : ResultadoHorarios
    data class Erro(val mensagem: String) : ResultadoHorarios
    data class Sucesso(val horarios: List<HorarioUi>) : ResultadoHorarios
}

private val NOMES_DIA_SEMANA = mapOf(
    Calendar.SUNDAY to "Dom", Calendar.MONDAY to "Seg", Calendar.TUESDAY to "Ter",
    Calendar.WEDNESDAY to "Qua", Calendar.THURSDAY to "Qui", Calendar.FRIDAY to "Sex",
    Calendar.SATURDAY to "Sáb"
)

private val NOMES_DIA_SEMANA_COMPLETO = mapOf(
    Calendar.SUNDAY to "Domingo", Calendar.MONDAY to "Segunda", Calendar.TUESDAY to "Terça",
    Calendar.WEDNESDAY to "Quarta", Calendar.THURSDAY to "Quinta", Calendar.FRIDAY to "Sexta",
    Calendar.SATURDAY to "Sábado"
)

private val FORMATO_DATA = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val FORMATO_DIA_MES_BARRA = SimpleDateFormat("dd/MM", Locale.getDefault())
private val FORMATO_DIA_MES = SimpleDateFormat("dd", Locale.getDefault())
private val FORMATO_HORA = SimpleDateFormat("HH:mm", Locale.getDefault())
private val FORMATO_DATA_HORA = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

private fun tickerDeUmMinuto(): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(60_000)
    }
}

private fun proximosDias(quantidade: Int): List<DiaAgenda> {
    val calendario = Calendar.getInstance()
    return (0 until quantidade).map { offset ->
        calendario.time = Date()
        calendario.add(Calendar.DAY_OF_YEAR, offset)
        val data = calendario.time
        DiaAgenda(
            data = FORMATO_DATA.format(data),
            diaSemana = NOMES_DIA_SEMANA[calendario.get(Calendar.DAY_OF_WEEK)].orEmpty(),
            diaMes = FORMATO_DIA_MES.format(data)
        )
    }
}

private fun rotuloDoDia(diaKey: String): String =
    runCatching { FORMATO_DATA.parse(diaKey) }.getOrNull()?.let { data ->
        val cal = Calendar.getInstance().apply { time = data }
        val diaSemana = NOMES_DIA_SEMANA_COMPLETO[cal.get(Calendar.DAY_OF_WEEK)].orEmpty()
        "$diaSemana, ${FORMATO_DIA_MES_BARRA.format(data)}"
    } ?: diaKey

private fun rotuloCurto(diaKey: String): String =
    runCatching { FORMATO_DATA.parse(diaKey) }.getOrNull()?.let(::formatarDiaSemanaEData) ?: diaKey

private fun formatarDiaSemanaEData(data: Date): String {
    val calendario = Calendar.getInstance().apply { time = data }
    val diaSemana = NOMES_DIA_SEMANA[calendario.get(Calendar.DAY_OF_WEEK)].orEmpty()
    return "$diaSemana, ${FORMATO_DIA_MES_BARRA.format(data)}"
}

private fun formatarHora(data: Date): String = FORMATO_HORA.format(data)

private fun intervaloDoDia(diaKey: String): Pair<Timestamp, Timestamp> {
    val base = runCatching { FORMATO_DATA.parse(diaKey) }.getOrNull() ?: Date()
    val inicio = Calendar.getInstance().apply {
        time = base
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val fim = (inicio.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    return Timestamp(inicio.time) to Timestamp(fim.time)
}

// A-02: hora cadastrada à mão com formato inválido não derruba o app — só é ignorada
private fun horarioUiValido(h: Horario): HorarioUi? {
    val instante = runCatching { FORMATO_DATA_HORA.parse("${h.data} ${h.hora}") }.getOrNull()
    if (instante == null) {
        Log.e("Goianinho", "Horario com data/hora inválida ignorado: ${h.data} ${h.hora}")
        return null
    }
    return HorarioUi(id = h.id, hora = h.hora, data = h.data, instante = instante)
}

// RN-14: horário de hoje que já passou não aparece na grade
private fun horarioAindaValeAPena(h: HorarioUi, diaConsultado: String): Boolean {
    if (diaConsultado != FORMATO_DATA.format(Date())) return true
    return h.instante.after(Date())
}

// A-09: nunca duplica "Dr." nem deixa "Dr. " pendurado sem nome
fun nomeDoMedicoExibicao(nome: String): String {
    val limpo = nome.trim()
    if (limpo.isEmpty()) return "[NOME DO MÉDICO]"
    return if (limpo.startsWith("Dr.", ignoreCase = true)) limpo else "Dr. $limpo"
}

// A-10: plural correto
fun rotuloDeConsultas(quantidade: Int): String =
    if (quantidade == 1) "1 consulta" else "$quantidade consultas"

// Adaptador (decisão combinada com você): embrulha a função antiga em callback do
// Módulo 3 num Flow, sem mexer no arquivo dele. Quando o Módulo 3 migrar
// ouvirPendentes para Flow nativo, troque esta chamada por documentoRepo.ouvirPendentes(medicoId) direto.
private fun ouvirPendentesComoFlow(repo: DocumentoRepository, medicoId: String): Flow<List<Documento>> =
    callbackFlow {
        val registro = repo.ouvirPendentes(
            medicoId = medicoId,
            onErro = { mensagem -> close(RuntimeException(mensagem)) },
            onMudou = { lista -> trySend(lista) }
        )
        awaitClose { registro.remove() }
    }