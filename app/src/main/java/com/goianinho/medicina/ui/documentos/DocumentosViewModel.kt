package com.goianinho.medicina.ui.documentos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.goianinho.medicina.GoianinhoApp
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.goianinho.medicina.ui.comum.ERRO_FOTO_GRANDE
import com.goianinho.medicina.ui.comum.mensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentosUiState(
    val documentos: List<Documento> = emptyList(),
    val carregando: Boolean = true,
    val salvando: Boolean = false,      // D-02: true enquanto grava (desabilita os botões)
    val nomeMedico: String = "",        // D-07: "Aguardando revisão do Dr. X"
    val mensagem: String? = null,       // aviso passageiro de sucesso (vira Snackbar)
    val bloqueio: String? = null,       // permanente: sessão, perfil, sem médico, erro de lista
    val erro: String? = null            // passageiro: falha de uma ação do usuário
)

class DocumentosViewModel(
    private val repository: DocumentoRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentosUiState())
    val uiState: StateFlow<DocumentosUiState> = _uiState.asStateFlow()

    private var pacienteId: String = ""
    private var medicoId: String = ""

    init {
        val uid = auth.usuarioLogadoUid
        if (uid == null) {
            _uiState.update {
                it.copy(carregando = false, bloqueio = "Sessão expirada. Entre novamente.")
            }
        } else {
            viewModelScope.launch {
                runCatching { auth.buscarUsuario(uid) }
                    .onSuccess { usuario ->
                        pacienteId = uid
                        medicoId = usuario.medicoId
                        if (medicoId.isBlank()) {
                            _uiState.update {
                                it.copy(
                                    carregando = false,
                                    bloqueio = "Você ainda não tem um médico vinculado. Peça para a clínica vincular sua conta."
                                )
                            }
                        } else {
                            carregarNomeDoMedico()
                        }
                        carregar()
                    }
                    .onFailure { e ->
                        Log.e("Goianinho", "buscarUsuario falhou", e)
                        _uiState.update {
                            it.copy(carregando = false, bloqueio = "Não foi possível carregar seu perfil.")
                        }
                    }
            }
        }
    }

    private fun carregarNomeDoMedico() {
        viewModelScope.launch {
            runCatching { auth.buscarUsuario(medicoId) }
                .onSuccess { medico -> _uiState.update { it.copy(nomeMedico = medico.nome) } }
        }
    }

    private fun carregar() {
        viewModelScope.launch {
            repository.ouvirDoPaciente(pacienteId)
                .catch { e ->
                    Log.e("Goianinho", "ouvirDoPaciente falhou", e)
                    _uiState.update { it.copy(carregando = false, bloqueio = mensagemDeErro(e)) }
                }
                .collect { documentos ->
                    _uiState.update { it.copy(documentos = documentos, carregando = false) }
                }
        }
    }

    fun registrarDocumento(nome: String, tipo: String, base64: String = "") {
        if (_uiState.value.salvando) return               // D-02: ignora o segundo toque
        if (pacienteId.isEmpty()) {
            _uiState.update { it.copy(erro = "Não foi possível identificar seu usuário.") }
            return
        }
        if (medicoId.isEmpty()) {
            _uiState.update {
                it.copy(erro = "Você ainda não tem um médico vinculado. Peça para a clínica vincular sua conta.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null) }
            repository.registrar(
                Documento(
                    pacienteId = pacienteId,
                    medicoId = medicoId,
                    nome = nome,
                    tipo = tipo,
                    imagemBase64 = base64
                )
            )
                .onSuccess {
                    _uiState.update { it.copy(salvando = false, mensagem = "Documento registrado!") }
                }
                .onFailure { e ->
                    Log.e("Goianinho", "registrar falhou", e)
                    _uiState.update { it.copy(salvando = false, erro = mensagemDeErro(e)) }
                }
        }
    }

    // A foto passou do limite de tamanho
    fun avisarFotoGrande() {
        _uiState.update { it.copy(erro = ERRO_FOTO_GRANDE) }
    }

    // Aviso genérico (ex.: câmera não encontrada)
    fun avisar(texto: String) {
        _uiState.update { it.copy(erro = texto) }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    fun limparMensagem() {
        _uiState.update { it.copy(mensagem = null) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GoianinhoApp
                DocumentosViewModel(
                    app.container.documentoRepository,
                    app.container.authRepository
                )
            }
        }
    }
}