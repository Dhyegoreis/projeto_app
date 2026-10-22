
package com.goianinho.medicina.ui.documentos

import androidx.lifecycle.ViewModel
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RevisaoUiState(
    val documentos: List<Documento> = emptyList(),
    val carregando: Boolean = true,
    val mensagem: String? = null
)

class RevisaoViewModel(
    private val repository: DocumentoRepository = DocumentoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevisaoUiState())
    val uiState: StateFlow<RevisaoUiState> = _uiState.asStateFlow()

    private var listener: ListenerRegistration? = null

    fun carregar(medicoId: String) {
        listener?.remove()
        listener = repository.ouvirPendentes(medicoId) { documentos ->
            _uiState.value = _uiState.value.copy(
                documentos = documentos,
                carregando = false
            )
        }
    }

    fun confirmar(documento: Documento, medicoId: String, observacao: String) {
        repository.confirmar(documento.id, medicoId, observacao) { sucesso ->
            _uiState.value = _uiState.value.copy(
                mensagem = if (sucesso) "Resumo confirmado."
                else "Erro ao confirmar. Tente novamente."
            )
        }
    }

    fun corrigir(documento: Documento, medicoId: String, observacao: String) {
        repository.corrigir(documento.id, medicoId, observacao) { sucesso ->
            _uiState.value = _uiState.value.copy(
                mensagem = if (sucesso) "Correção registrada."
                else "Erro ao corrigir. Tente novamente."
            )
        }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}
