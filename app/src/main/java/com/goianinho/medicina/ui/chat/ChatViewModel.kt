package com.goianinho.medicina.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.model.Mensagem
import com.goianinho.medicina.data.repository.AgendaRepository
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.goianinho.medicina.data.repository.MensagemRepository
import com.goianinho.medicina.data.repository.RespostasSimuladas
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

// Estado das duas telas do Chat (paciente e médico)
data class ChatUiState(
    val perfil: String = "",            // "paciente" | "medico"
    val nomeUsuario: String = "",
    val nomeMedico: String = "",
    val mensagens: List<Mensagem> = emptyList(),
    val consultasHoje: Int = 0,
    val paraRevisar: Int = 0,
    val semResposta: Int = RespostasSimuladas.PACIENTES_SEM_RESPOSTA,
    val erro: String? = null
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

    init {
        val logado = auth.usuarioLogadoUid
        if (logado.isNullOrBlank()) {
            _uiState.update { it.copy(erro = "Sessão expirada. Entre novamente.") }
        } else {
            uid = logado
            viewModelScope.launch {
                try {
                    val usuario = auth.buscarUsuario(logado)
                    _uiState.update {
                        it.copy(perfil = usuario.perfil, nomeUsuario = usuario.nome, erro = null)
                    }
                    when (usuario.perfil) {
                        "medico" -> iniciarComoMedico(logado)
                        "paciente" -> iniciarComoPaciente(logado, usuario.medicoId)
                        else -> _uiState.update { it.copy(erro = "Perfil de usuário inválido.") }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("Goianinho", "Chat: falha ao buscar o perfil", e)
                    _uiState.update { it.copy(erro = "Não foi possível carregar seu perfil.") }
                }
            }
        }
    }

    private fun iniciarComoPaciente(uid: String, medicoId: String) {
        medicoIdDoPaciente = medicoId
        if (medicoId.isBlank()) {
            _uiState.update { it.copy(erro = "Você ainda não tem um médico vinculado. Peça para a clínica vincular sua conta.") }
            return
        }
        listenerMensagens?.remove()
        listenerMensagens = repo.ouvirDoPaciente(uid, onErro = ::mostrarErro) { lista ->
            _uiState.update { it.copy(mensagens = lista) }
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
        listenerMensagens = repo.ouvirDoMedico(uid, onErro = ::mostrarErro) { lista ->
            _uiState.update { it.copy(mensagens = lista) }
        }
        listenerPendentes?.remove()
        listenerPendentes = documentoRepo.ouvirPendentes(uid, onErro = ::mostrarErro) { lista ->
            _uiState.update { it.copy(paraRevisar = lista.size) }
        }
        val inicio = inicioDoDia(0)
        val fim = inicioDoDia(1)
        listenerConsultas?.remove()
        listenerConsultas = agendaRepo.ouvirConsultasDoDia(uid, inicio, fim, onErro = ::mostrarErro) { lista ->
            _uiState.update { it.copy(consultasHoje = lista.size) }
        }
    }

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
        repo.enviar(mensagem, estado.perfil, onErro = ::mostrarErro)
    }

    override fun onCleared() {
        listenerMensagens?.remove()
        listenerPendentes?.remove()
        listenerConsultas?.remove()
        super.onCleared()
    }
}
