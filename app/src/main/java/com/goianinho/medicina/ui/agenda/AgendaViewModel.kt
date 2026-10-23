package com.goianinho.medicina.ui.agenda

import androidx.lifecycle.ViewModel
import com.goianinho.medicina.data.model.Horario
import com.goianinho.medicina.data.repository.AgendaRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val medicoNome: String = "[NOME DO MÉDICO]",
    val proximaConsultaTipo: String = "Teleconsulta",
    val proximaConsultaData: String = "Qui, 24/09",
    val proximaConsultaHora: String = "09:30",
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

// Status do resumo da Íris para a consulta, espelhando o Módulo 3
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
    val pacienteAtualNome: String = "[PACIENTE A]",
    val horaAtual: String = "09:30",
    val briefing: String = "Motivo relatado: [MOTIVO DA CONSULTA]. Aguardando confirmação do resumo.",
    val proximas: List<ConsultaProximaUi> = emptyList()
)

class AgendaViewModel : ViewModel() {

    private val repository = AgendaRepository()

    // TODO (Etapa 3): trocar pelos ids do usuário logado de verdade
    // assim que o Módulo 1 (Login) entrar na main. Precisam bater com os
    // valores cadastrados manualmente no Firestore (RN-09).
    private val medicoId = "medico-teste-1"
    private val pacienteId = "paciente-teste-1"

    private val _uiStatePaciente = MutableStateFlow(AgendaPacienteUiState())
    val uiStatePaciente: StateFlow<AgendaPacienteUiState> = _uiStatePaciente.asStateFlow()

    private val _uiStateMedico = MutableStateFlow(AgendaMedicoUiState())
    val uiStateMedico: StateFlow<AgendaMedicoUiState> = _uiStateMedico.asStateFlow()

    private var listenerHorarios: ListenerRegistration? = null
    private var listenerProximaConsulta: ListenerRegistration? = null
    private var listenerConsultasDoDia: ListenerRegistration? = null

    init {
        val dias = proximosDias(4)
        _uiStatePaciente.update { it.copy(dias = dias, diaSelecionado = dias.first().data) }
        ouvirHorariosDoDia(dias.first().data)
        ouvirProximaConsulta()

        _uiStateMedico.update { it.copy(diaRotulo = rotuloDeHoje()) }
        ouvirConsultasDeHoje()
    }

    fun selecionarDia(data: String) {
        _uiStatePaciente.update { it.copy(diaSelecionado = data, horarioSelecionadoId = null) }
        ouvirHorariosDoDia(data)
    }

    fun selecionarHorario(horario: HorarioUi) {
        if (!horario.disponivel) return
        _uiStatePaciente.update { it.copy(horarioSelecionadoId = horario.id) }
    }

    fun confirmarReserva() {
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
            if (dataHora != null) {
                _uiStatePaciente.update {
                    it.copy(
                        proximaConsultaData = formatarDiaSemanaEData(dataHora.toDate()),
                        proximaConsultaHora = formatarHora(dataHora.toDate())
                    )
                }
            }
        }
    }

    private fun ouvirConsultasDeHoje() {
        val (inicio, fim) = intervaloDeHoje()
        listenerConsultasDoDia?.remove()
        listenerConsultasDoDia = repository.ouvirConsultasDoDia(medicoId, inicio, fim) { consultas ->
            val ordenadas = consultas.sortedBy { it.dataHora?.seconds ?: Long.MAX_VALUE }
            val atual = ordenadas.firstOrNull()
            val proximas = ordenadas.drop(1).map { consulta ->
                ConsultaProximaUi(
                    hora = consulta.dataHora?.toDate()?.let(::formatarHora).orEmpty(),
                    // TODO (Etapa 3): nome real quando o Módulo 1 expuser o Usuario do paciente
                    pacienteNome = "[PACIENTE]",
                    // TODO (Etapa 3): cruzar com DocumentoRepository.ouvirPendentes do Módulo 3
                    status = StatusResumoConsulta.SEM_EXAME,
                    dicaEngajamento = "Dica de engajamento (texto fixo nesta fase)"
                )
            }
            _uiStateMedico.update { estadoAtual ->
                if (atual != null) {
                    estadoAtual.copy(
                        totalConsultas = ordenadas.size,
                        pacienteAtualNome = "[PACIENTE]",
                        horaAtual = atual.dataHora?.toDate()?.let(::formatarHora).orEmpty(),
                        proximas = proximas
                    )
                } else {
                    estadoAtual.copy(totalConsultas = ordenadas.size, proximas = proximas)
                }
            }
        }
    }
    override fun onCleared() {
        listenerHorarios?.remove()
        listenerProximaConsulta?.remove()
        listenerConsultasDoDia?.remove()
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