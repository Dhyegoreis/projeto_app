package com.goianinho.medicina.ui.agenda

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.model.Consulta
import com.goianinho.medicina.data.model.Horario
import com.goianinho.medicina.data.repository.AgendaRepository
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Um dia do seletor de dias da Agenda do paciente
data class DiaAgenda(
    val data: String,       // "2026-10-20" — identifica o dia (RF-24)
    val diaSemana: String,  // "Ter"
    val diaMes: String      // "20"
)

// Um horário da grade (RF-24 e RF-25)
data class HorarioUi(
    val id: String,
    val hora: String,
    val disponivel: Boolean = true
)

// Estado da tela Agenda do paciente
data class AgendaPacienteUiState(
    val medicoNome: String = "",
    val temProximaConsulta: Boolean = false,
    val proximaConsultaTipo: String = "",
    val proximaConsultaData: String = "",
    val proximaConsultaHora: String = "",
    val dias: List<DiaAgenda> = emptyList(),
    val diaSelecionado: String = "",
    val horarios: List<HorarioUi> = emptyList(),
    val horarioSelecionadoId: String? = null,
    val reservando: Boolean = false,
    val mensagemErro: String? = null
) {
    val horarioSelecionado: HorarioUi?
        get() = horarios.firstOrNull { it.id == horarioSelecionadoId }

    val diaSelecionadoRotulo: String
        get() = dias.firstOrNull { it.data == diaSelecionado }
            ?.let { "${it.diaSemana}, ${it.diaMes}" } ?: ""
}

// Status do resumo do Goianinho para a consulta, espelhando o Módulo 3
enum class StatusResumoConsulta(val rotulo: String) {
    SEM_EXAME("Sem exames novos"),
    AGUARDANDO_MEDICO("Resumo aguardando você"),
    CONFIRMADO("Confirmado")
}

// Uma consulta na lista "Próximas" da Agenda do dia (médico)
data class ConsultaProximaUi(
    val hora: String,
    val pacienteNome: String,
    val status: StatusResumoConsulta,
    val dicaEngajamento: String
)

// Estado da tela Agenda do dia (médico)
data class AgendaMedicoUiState(
    val diaRotulo: String = "",
    val totalConsultas: Int = 0,
    val temConsultaAtual: Boolean = false,
    val pacienteAtualNome: String = "",
    val horaAtual: String = "",
    val briefing: String = "",
    val mensagemErro: String? = null,
    val proximas: List<ConsultaProximaUi> = emptyList()
)

class AgendaViewModel(
    private val auth: AuthRepository = AuthRepository()
) : ViewModel() {

    private val repository = AgendaRepository()
    private val documentoRepository = DocumentoRepository()

    // Preenchidos a partir do usuário logado (RN-09)
    private var medicoId = ""
    private var pacienteId = ""

    private val _uiStatePaciente = MutableStateFlow(AgendaPacienteUiState())
    val uiStatePaciente: StateFlow<AgendaPacienteUiState> = _uiStatePaciente.asStateFlow()

    private val _uiStateMedico = MutableStateFlow(AgendaMedicoUiState())
    val uiStateMedico: StateFlow<AgendaMedicoUiState> = _uiStateMedico.asStateFlow()

    private var listenerHorarios: ListenerRegistration? = null
    private var listenerProximaConsulta: ListenerRegistration? = null
    private var listenerConsultasDoDia: ListenerRegistration? = null
    private var listenerPendentes: ListenerRegistration? = null

    // Dados do médico: últimas consultas do dia, pacientes com resumo pendente e cache de nomes
    private var consultasDoDia: List<Consulta> = emptyList()
    private var pacientesComPendencia: Set<String> = emptySet()
    private val nomesPacientes = mutableMapOf<String, String>()
    private val nomesEmBusca = mutableSetOf<String>()

    init {
        val dias = proximosDias(4)
        _uiStatePaciente.update { it.copy(dias = dias, diaSelecionado = dias.first().data) }
        _uiStateMedico.update { it.copy(diaRotulo = rotuloDeHoje()) }

        val uid = auth.usuarioLogadoUid
        if (uid == null) {
            definirErro("Sessão expirada. Entre novamente.")
        } else {
            viewModelScope.launch {
                try {
                    val usuario = auth.buscarUsuario(uid)
                    if (usuario.perfil == "medico") {
                        iniciarComoMedico(uid)
                    } else {
                        iniciarComoPaciente(uid, usuario.medicoId)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("Goianinho", "Agenda: falha ao buscar o perfil", e)
                    definirErro("Não foi possível carregar seu perfil.")
                }
            }
        }
    }

    private fun definirErro(mensagem: String) {
        _uiStatePaciente.update { it.copy(mensagemErro = mensagem) }
        _uiStateMedico.update { it.copy(mensagemErro = mensagem) }
    }

    private fun iniciarComoPaciente(uid: String, vinculado: String) {
        pacienteId = uid
        medicoId = vinculado
        if (vinculado.isEmpty()) {
            _uiStatePaciente.update {
                it.copy(mensagemErro = "Você ainda não tem um médico vinculado.")
            }
            return
        }
        _uiStatePaciente.update { it.copy(mensagemErro = null) }
        ouvirHorariosDoDia(_uiStatePaciente.value.diaSelecionado)
        ouvirProximaConsulta()
        buscarNomeDoMedico(vinculado)
    }

    private fun iniciarComoMedico(uid: String) {
        medicoId = uid
        _uiStateMedico.update { it.copy(mensagemErro = null) }
        ouvirPendentes()
        ouvirConsultasDeHoje()
    }

    private fun buscarNomeDoMedico(id: String) {
        viewModelScope.launch {
            try {
                val nome = auth.buscarUsuario(id).nome
                _uiStatePaciente.update { it.copy(medicoNome = nome) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Goianinho", "Agenda: falha ao buscar o nome do médico", e)
            }
        }
    }

    fun selecionarDia(data: String) {
        _uiStatePaciente.update { it.copy(diaSelecionado = data, horarioSelecionadoId = null) }
        if (medicoId.isNotEmpty()) ouvirHorariosDoDia(data)
    }

    fun selecionarHorario(horario: HorarioUi) {
        if (!horario.disponivel) return
        _uiStatePaciente.update { it.copy(horarioSelecionadoId = horario.id) }
    }

    fun confirmarReserva() {
        if (pacienteId.isEmpty() || medicoId.isEmpty()) {
            _uiStatePaciente.update {
                it.copy(mensagemErro = "Você ainda não tem um médico vinculado.")
            }
            return
        }
        val estado = _uiStatePaciente.value
        val horarioSelecionado = estado.horarioSelecionado ?: return
        val dataHora = horarioParaTimestamp(estado.diaSelecionado, horarioSelecionado.hora) ?: return

        _uiStatePaciente.update { it.copy(reservando = true, mensagemErro = null) }

        repository.reservar(
            horario = Horario(
                id = horarioSelecionado.id,
                medicoId = medicoId,
                data = estado.diaSelecionado,
                hora = horarioSelecionado.hora
            ),
            pacienteId = pacienteId,
            dataHora = dataHora,
            onSucesso = {
                _uiStatePaciente.update { it.copy(reservando = false, horarioSelecionadoId = null) }
            },
            onErro = { mensagem ->
                _uiStatePaciente.update { it.copy(reservando = false, mensagemErro = mensagem) }
            }
        )
    }

    private fun ouvirHorariosDoDia(data: String) {
        listenerHorarios?.remove()
        listenerHorarios = repository.ouvirHorariosLivres(medicoId, data) { horarios ->
            _uiStatePaciente.update {
                it.copy(horarios = horarios.map { h -> HorarioUi(id = h.id, hora = h.hora) })
            }
        }
    }

    private fun ouvirProximaConsulta() {
        listenerProximaConsulta?.remove()
        listenerProximaConsulta = repository.ouvirProximaConsulta(pacienteId) { consulta ->
            val dataHora = consulta?.dataHora
            _uiStatePaciente.update {
                if (consulta != null && dataHora != null) {
                    it.copy(
                        temProximaConsulta = true,
                        proximaConsultaTipo = consulta.tipo.replaceFirstChar { c -> c.uppercase() },
                        proximaConsultaData = formatarDiaSemanaEData(dataHora.toDate()),
                        proximaConsultaHora = formatarHora(dataHora.toDate())
                    )
                } else {
                    it.copy(
                        temProximaConsulta = false,
                        proximaConsultaTipo = "",
                        proximaConsultaData = "",
                        proximaConsultaHora = ""
                    )
                }
            }
        }
    }

    private fun ouvirPendentes() {
        listenerPendentes?.remove()
        listenerPendentes = documentoRepository.ouvirPendentes(medicoId) { documentos ->
            pacientesComPendencia = documentos.map { it.pacienteId }.toSet()
            atualizarAgendaDoMedico()
        }
    }

    private fun ouvirConsultasDeHoje() {
        val (inicio, fim) = intervaloDeHoje()
        listenerConsultasDoDia?.remove()
        listenerConsultasDoDia = repository.ouvirConsultasDoDia(medicoId, inicio, fim) { consultas ->
            consultasDoDia = consultas.sortedBy { it.dataHora?.seconds ?: Long.MAX_VALUE }
            consultasDoDia.map { it.pacienteId }.distinct().forEach(::buscarNomeDoPaciente)
            atualizarAgendaDoMedico()
        }
    }

    // Busca o nome uma única vez por paciente (cache + controle de buscas em andamento)
    private fun buscarNomeDoPaciente(id: String) {
        if (id.isEmpty() || nomesPacientes.containsKey(id) || !nomesEmBusca.add(id)) return
        viewModelScope.launch {
            try {
                nomesPacientes[id] = auth.buscarUsuario(id).nome
                atualizarAgendaDoMedico()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Goianinho", "Agenda: falha ao buscar o nome do paciente", e)
            } finally {
                nomesEmBusca.remove(id)
            }
        }
    }

    private fun atualizarAgendaDoMedico() {
        val ordenadas = consultasDoDia
        val atual = ordenadas.firstOrNull()
        val proximas = ordenadas.drop(1).map { consulta ->
            val pendente = consulta.pacienteId in pacientesComPendencia
            ConsultaProximaUi(
                hora = consulta.dataHora?.toDate()?.let(::formatarHora).orEmpty(),
                pacienteNome = nomesPacientes[consulta.pacienteId].orEmpty(),
                status = if (pendente) StatusResumoConsulta.AGUARDANDO_MEDICO
                else StatusResumoConsulta.SEM_EXAME,
                dicaEngajamento = "Dica de engajamento (texto fixo nesta fase)"
            )
        }
        _uiStateMedico.update { estadoAtual ->
            if (atual != null) {
                val pendente = atual.pacienteId in pacientesComPendencia
                estadoAtual.copy(
                    totalConsultas = ordenadas.size,
                    temConsultaAtual = true,
                    pacienteAtualNome = nomesPacientes[atual.pacienteId].orEmpty(),
                    horaAtual = atual.dataHora?.toDate()?.let(::formatarHora).orEmpty(),
                    briefing = if (pendente) "Resumo de exames aguardando sua confirmação."
                    else "Sem exames novos para revisar.",
                    proximas = proximas
                )
            } else {
                estadoAtual.copy(
                    totalConsultas = 0,
                    temConsultaAtual = false,
                    pacienteAtualNome = "",
                    horaAtual = "",
                    briefing = "",
                    proximas = emptyList()
                )
            }
        }
    }

    override fun onCleared() {
        listenerHorarios?.remove()
        listenerProximaConsulta?.remove()
        listenerConsultasDoDia?.remove()
        listenerPendentes?.remove()
        super.onCleared()
    }
}

// --- Funções de data/hora (privadas ao arquivo) ---

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

private fun rotuloDeHoje(): String {
    val calendario = Calendar.getInstance()
    val diaSemana = NOMES_DIA_SEMANA_COMPLETO[calendario.get(Calendar.DAY_OF_WEEK)].orEmpty()
    return "$diaSemana, ${FORMATO_DIA_MES_BARRA.format(calendario.time)}"
}

private fun formatarDiaSemanaEData(data: Date): String {
    val calendario = Calendar.getInstance().apply { time = data }
    val diaSemana = NOMES_DIA_SEMANA[calendario.get(Calendar.DAY_OF_WEEK)].orEmpty()
    return "$diaSemana, ${FORMATO_DIA_MES_BARRA.format(data)}"
}

private fun formatarHora(data: Date): String = FORMATO_HORA.format(data)

private fun intervaloDeHoje(): Pair<Timestamp, Timestamp> {
    val inicio = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val fim = (inicio.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    return Timestamp(inicio.time) to Timestamp(fim.time)
}

private fun horarioParaTimestamp(data: String, hora: String): Timestamp? {
    val date = FORMATO_DATA_HORA.parse("$data $hora") ?: return null
    return Timestamp(date)
}