package com.goianinho.medicina.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.model.Consulta
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.model.Mensagem
import com.goianinho.medicina.data.repository.AgendaRepository
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.goianinho.medicina.data.repository.MensagemRepository
import com.goianinho.medicina.data.repository.RespostasSimuladas
import com.goianinho.medicina.ui.comum.consultaDoMomento
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

// Estado das duas telas do Chat (paciente e médico)
data class ChatUiState(
    val perfil: String = "",            // "paciente" | "medico"
    val nomeUsuario: String = "",
    val nomeMedico: String = "",
    val mensagens: List<Mensagem> = emptyList(),
    val consultasHoje: Int = 0,
    val paraRevisar: Int = 0,
    val semResposta: Int = RespostasSimuladas.PACIENTES_SEM_RESPOSTA,
    val bloqueio: String? = null,       // permanente e desabilita o envio: sessão, perfil, sem médico, erro das mensagens
    val avisoContadores: String? = null, // permanente, sem desabilitar o envio: erro dos contadores e dos dados da resposta (revisões, consultas, último exame)
    val erro: String? = null            // passageiro: falha de uma ação do usuário
)

class ChatViewModel(
    private val auth: AuthRepository = AuthRepository(),
    private val repo: MensagemRepository = MensagemRepository(),
    private val documentoRepo: DocumentoRepository = DocumentoRepository(),
    private val agendaRepo: AgendaRepository = AgendaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var uid = ""
    private var medicoIdDoPaciente = ""

    private var listenerMensagens: ListenerRegistration? = null
    private var listenerPendentes: ListenerRegistration? = null
    private var listenerConsultas: ListenerRegistration? = null
    private var listenerUltimoExame: ListenerRegistration? = null
    private var listenerProximaConsulta: ListenerRegistration? = null

    // Dados para o contexto da resposta simulada (RespostasSimuladas.Contexto)
    private var ultimoExame: Documento? = null
    private var proximaConsulta: Consulta? = null
    private var pendentes: List<Documento> = emptyList()
    private var consultasHoje: List<Consulta> = emptyList()
    private val nomesPacientes = mutableMapOf<String, String>()
    private val nomesEmBusca = mutableSetOf<String>()

    init {
        val logado = auth.usuarioLogadoUid
        if (logado.isNullOrBlank()) {
            _uiState.update { it.copy(bloqueio = "Sessão expirada. Entre novamente.") }
        } else {
            uid = logado
            viewModelScope.launch {
                try {
                    val usuario = auth.buscarUsuario(logado)
                    _uiState.update {
                        it.copy(perfil = usuario.perfil, nomeUsuario = usuario.nome, bloqueio = null)
                    }
                    when (usuario.perfil) {
                        "medico" -> iniciarComoMedico(logado)
                        "paciente" -> iniciarComoPaciente(logado, usuario.medicoId)
                        else -> _uiState.update { it.copy(bloqueio = "Perfil de usuário inválido.") }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("Goianinho", "Chat: falha ao buscar o perfil", e)
                    _uiState.update { it.copy(bloqueio = "Não foi possível carregar seu perfil.") }
                }
            }
        }
    }

    private fun iniciarComoPaciente(uid: String, medicoId: String) {
        medicoIdDoPaciente = medicoId
        if (medicoId.isBlank()) {
            _uiState.update { it.copy(bloqueio = "Você ainda não tem um médico vinculado. Peça para a clínica vincular sua conta.") }
            return
        }
        listenerMensagens?.remove()
        listenerMensagens = repo.ouvirDoPaciente(uid, onErro = ::mostrarBloqueio) { lista ->
            _uiState.update { it.copy(mensagens = lista) }
        }
        listenerUltimoExame?.remove()
        listenerUltimoExame = documentoRepo.ouvirDoPaciente(uid, 1, onErro = ::mostrarAvisoContadores) { lista ->
            ultimoExame = lista.firstOrNull()
        }
        listenerProximaConsulta?.remove()
        listenerProximaConsulta = agendaRepo.ouvirProximaConsulta(uid, onErro = ::mostrarAvisoContadores) { consulta ->
            proximaConsulta = consulta
        }
        viewModelScope.launch {
            try {
                val nome = auth.buscarUsuario(medicoId).nome
                _uiState.update { it.copy(nomeMedico = nome) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // O chat continua funcionando; a tela usa um nome genérico
                Log.e("Goianinho", "Chat: falha ao buscar o nome do médico", e)
            }
        }
    }

    private fun iniciarComoMedico(uid: String) {
        listenerMensagens?.remove()
        listenerMensagens = repo.ouvirDoMedico(uid, onErro = ::mostrarBloqueio) { lista ->
            _uiState.update { it.copy(mensagens = lista) }
        }
        listenerPendentes?.remove()
        listenerPendentes = documentoRepo.ouvirPendentes(uid, onErro = ::mostrarAvisoContadores) { lista ->
            pendentes = lista
            _uiState.update { it.copy(paraRevisar = lista.size) }
        }
        val inicio = inicioDoDia(0)
        val fim = inicioDoDia(1)
        listenerConsultas?.remove()
        listenerConsultas = agendaRepo.ouvirConsultasDoDia(uid, inicio, fim, onErro = ::mostrarAvisoContadores) { lista ->
            consultasHoje = lista
            _uiState.update { it.copy(consultasHoje = lista.count { c -> c.status != "cancelada" }) }
            buscarNomesDosPacientes(lista)
        }
    }

    // Guarda o nome dos pacientes das consultas do dia para a resposta "próximo paciente" (não bloqueia nada)
    private fun buscarNomesDosPacientes(lista: List<Consulta>) {
        lista.filter { it.status != "cancelada" }
            .map { it.pacienteId }
            .filter { it.isNotBlank() && !nomesPacientes.containsKey(it) && nomesEmBusca.add(it) }
            .distinct()
            .forEach { pacienteId ->
                viewModelScope.launch {
                    try {
                        nomesPacientes[pacienteId] = auth.buscarUsuario(pacienteId).nome
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // A resposta usa "um paciente" quando o nome não está disponível
                        Log.e("Goianinho", "Chat: falha ao buscar o nome do paciente", e)
                    } finally {
                        nomesEmBusca.remove(pacienteId)
                    }
                }
            }
    }

    // Monta os dados que a resposta simulada usa, no momento do envio
    private fun montarContexto(perfil: String): RespostasSimuladas.Contexto {
        if (perfil == "medico") {
            val proxima = consultaDoMomento(consultasHoje)
            val pacienteId = proxima?.pacienteId.orEmpty()
            val hora = proxima?.dataHora?.toDate()?.let { SimpleDateFormat("HH:mm", PT_BR).format(it) }.orEmpty()
            return RespostasSimuladas.Contexto(
                pendentes = pendentes.size,
                documentoPendente = pendentes.firstOrNull()?.nome.orEmpty(),
                proximoPacienteNome = nomesPacientes[pacienteId].orEmpty(),
                proximoPacienteHora = hora,
                proximoPacienteDocumento = pendentes.firstOrNull { it.pacienteId == pacienteId }?.nome.orEmpty()
            )
        }
        return RespostasSimuladas.Contexto(
            nomeMedico = _uiState.value.nomeMedico,
            ultimoExameNome = ultimoExame?.nome.orEmpty(),
            ultimoExameStatus = ultimoExame?.status.orEmpty(),
            proximaConsulta = proximaConsulta?.dataHora?.toDate()?.let(::formatarConsulta).orEmpty()
        )
    }

    // "Qui, 24/10 às 09:30"
    private fun formatarConsulta(data: Date): String {
        val dia = SimpleDateFormat("EEE", PT_BR).format(data).trimEnd('.')
            .replaceFirstChar { it.uppercase() }
        return "$dia, ${SimpleDateFormat("dd/MM", PT_BR).format(data)} às ${SimpleDateFormat("HH:mm", PT_BR).format(data)}"
    }

    // Erro de listener: a lista fica vazia enquanto o problema existir, então o aviso é fixo
    private fun mostrarBloqueio(mensagem: String) {
        _uiState.update { it.copy(bloqueio = mensagem) }
    }

    // Erro de listener dos contadores: aviso fixo, mas o chat continua liberado
    private fun mostrarAvisoContadores(mensagem: String) {
        _uiState.update { it.copy(avisoContadores = mensagem) }
    }

    // Falha de uma ação do usuário (enviar mensagem): aviso passageiro
    private fun mostrarErro(mensagem: String) {
        _uiState.update { it.copy(erro = mensagem) }
    }

    private fun inicioDoDia(diasAFrente: Int): Timestamp {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, diasAFrente)
        }
        return Timestamp(c.time)
    }

    fun enviar(texto: String) {
        if (texto.isBlank()) return
        val estado = _uiState.value
        if (uid.isBlank()) return
        val mensagem = if (estado.perfil == "medico") {
            Mensagem(pacienteId = "", medicoId = uid, autor = "usuario", texto = texto.trim())
        } else {
            if (estado.perfil != "paciente") return
            if (medicoIdDoPaciente.isBlank()) {
                _uiState.update { it.copy(erro = "Você ainda não tem um médico vinculado. Peça para a clínica vincular sua conta.") }
                return
            }
            Mensagem(
                pacienteId = uid,
                medicoId = medicoIdDoPaciente,
                autor = "usuario",
                texto = texto.trim()
            )
        }
        repo.enviar(mensagem, estado.perfil, montarContexto(estado.perfil), onErro = ::mostrarErro)
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    override fun onCleared() {
        listenerMensagens?.remove()
        listenerPendentes?.remove()
        listenerConsultas?.remove()
        listenerUltimoExame?.remove()
        listenerProximaConsulta?.remove()
        super.onCleared()
    }
}

private val PT_BR = Locale("pt", "BR")
