package com.goianinho.medicina.ui.documentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentosUiState(
    val documentos: List<Documento> = emptyList(),
    val carregando: Boolean = true,
    val mensagem: String? = null,
    val bloqueio: String? = null,   // permanente: sessão, perfil, sem médico, erro de listener
    val erro: String? = null        // passageiro: falha de uma ação do usuário
)

class DocumentosViewModel(
    private val repository: DocumentoRepository = DocumentoRepository(),
    private val auth: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentosUiState())
    val uiState: StateFlow<DocumentosUiState> = _uiState.asStateFlow()

    private var observacao: ListenerRegistration? = null

    // Preenchidos a partir do usuário logado
    private var pacienteId: String = ""
    private var medicoId: String = ""

    init {
        val uid = auth.usuarioLogadoUid
        if (uid == null) {
            _uiState.value = _uiState.value.copy(
                carregando = false,
                bloqueio = "Sessão expirada. Entre novamente."
            )
        } else {
            viewModelScope.launch {
                try {
                    val usuario = auth.buscarUsuario(uid)
                    pacienteId = uid
                    medicoId = usuario.medicoId
                    if (medicoId.isBlank()) {
                        _uiState.value = _uiState.value.copy(
                            bloqueio = "Você ainda não tem um médico vinculado. Peça para a clínica vincular sua conta."
                        )
                    }
                    carregar(pacienteId)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        carregando = false,
                        bloqueio = "Não foi possível carregar seu perfil."
                    )
                }
            }
        }
    }

    private fun carregar(pacienteId: String) {
        observacao?.remove()
        observacao = repository.ouvirDoPaciente(pacienteId, onErro = ::mostrarBloqueio) { documentos ->
            _uiState.value = _uiState.value.copy(
                documentos = documentos,
                carregando = false
            )
        }
    }

    fun registrarDocumento(
        nome: String,
        tipo: String,
        base64: String = ""
    ) {
        if (pacienteId.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                erro = "Não foi possível identificar seu usuário."
            )
            return
        }
        if (medicoId.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                erro = "Você ainda não tem um médico vinculado. Peça para a clínica vincular sua conta."
            )
            return
        }
        repository.registrar(
            pacienteId, medicoId, nome, tipo, base64,
            onErro = ::mostrarErro
        ) { sucesso ->
            // Em caso de falha, o motivo já chegou por onErro
            if (sucesso) {
                _uiState.value = _uiState.value.copy(
                    mensagem = "Documento registrado!",
                    erro = null
                )
            }
        }
    }

    // Erro de listener: a lista fica vazia enquanto o problema existir, então o aviso é fixo
    private fun mostrarBloqueio(mensagem: String) {
        _uiState.value = _uiState.value.copy(carregando = false, bloqueio = mensagem)
    }

    // Falha de uma ação do usuário (registrar documento): aviso passageiro
    private fun mostrarErro(mensagem: String) {
        _uiState.value = _uiState.value.copy(carregando = false, erro = mensagem)
    }

    // A foto passou do limite de tamanho do documento
    fun avisarFotoGrande() {
        mostrarErro("A foto ficou grande demais. Tente de novo com mais luz ou mais perto.")
    }

    fun limparErro() {
        _uiState.value = _uiState.value.copy(erro = null)
    }

    fun limparMensagem() {
        _uiState.value = _uiState.value.copy(mensagem = null)
    }

    override fun onCleared() {
        observacao?.remove()
        super.onCleared()
    }
}
