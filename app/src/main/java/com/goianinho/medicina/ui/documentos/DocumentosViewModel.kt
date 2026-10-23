package com.goianinho.medicina.ui.documentos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.repository.DocumentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentosUiState(
    val documentos: List<Documento> = emptyList(),
    val carregando: Boolean = true,
    val mensagem: String? = null
)

class DocumentosViewModel(
    private val repository: DocumentoRepository = DocumentoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentosUiState())
    val uiState: StateFlow<DocumentosUiState> = _uiState.asStateFlow()

    private var observacao: com.google.firebase.firestore.ListenerRegistration? = null

    fun carregar(pacienteId: String) {
        observacao?.remove()
        observacao = repository.ouvirDoPaciente(pacienteId) { documentos ->
            _uiState.value = _uiState.value.copy(
                documentos = documentos,
                carregando = false
            )
        }
    }

    fun registrarDocumento(
        pacienteId: String,
        medicoId: String,
        nome: String,
        tipo: String,
        base64: String = ""
    ) {
        repository.registrar(pacienteId, medicoId, nome, tipo, base64) { sucesso ->
            _uiState.value = _uiState.value.copy(
                mensagem = if (sucesso) "Documento registrado!"
                else "Não foi possível registrar o documento."
            )
        }
    }

    fun limparMensagem() {
        _uiState.value = _uiState.value.copy(mensagem = null)
    }

    override fun onCleared() {
        observacao?.remove()
        super.onCleared()
    }
}
