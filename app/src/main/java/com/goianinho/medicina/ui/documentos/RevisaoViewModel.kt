package com.goianinho.medicina.ui.documentos

import androidx.lifecycle.ViewModel
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RevisaoUiState(
    val documentos: List<Documento> = emptyList(),
    val carregando: Boolean = true,
    val mensagem: String? = null,
    val erro: String? = null
)

class RevisaoViewModel(
    private val repository: DocumentoRepository = DocumentoRepository(),
    private val auth: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevisaoUiState())
    val uiState: StateFlow<RevisaoUiState> = _uiState.asStateFlow()

    private var listener: ListenerRegistration? = null

    // O médico é o próprio usuário logado
    private val medicoId: String? = auth.usuarioLogadoUid

    init {
        if (medicoId == null) {
            _uiState.value = _uiState.value.copy(
                carregando = false,
                erro = "Sessão expirada. Entre novamente."
            )
        } else {
            carregar(medicoId)
        }
    }

    private fun carregar(medicoId: String) {
        listener?.remove()
        listener = repository.ouvirPendentes(medicoId, onErro = ::mostrarErro) { documentos ->
            _uiState.value = _uiState.value.copy(
                documentos = documentos,
                carregando = false
            )
        }
    }

    fun confirmar(documento: Documento, observacao: String) {
        val medico = medicoId ?: return
        repository.confirmar(documento.id, medico, observacao, onErro = ::mostrarErro) { sucesso ->
            if (sucesso) {
                _uiState.value = _uiState.value.copy(mensagem = "Resumo confirmado.", erro = null)
            }
        }
    }

    fun corrigir(documento: Documento, observacao: String) {
        val medico = medicoId ?: return
        repository.corrigir(documento.id, medico, observacao, onErro = ::mostrarErro) { sucesso ->
            if (sucesso) {
                _uiState.value = _uiState.value.copy(mensagem = "Correção registrada.", erro = null)
            }
        }
    }

    private fun mostrarErro(mensagem: String) {
        _uiState.value = _uiState.value.copy(carregando = false, erro = mensagem)
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}
