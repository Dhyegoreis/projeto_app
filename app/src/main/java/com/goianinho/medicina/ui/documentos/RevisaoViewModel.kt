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
import com.goianinho.medicina.ui.comum.mensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RevisaoUiState(
    val documentos: List<Documento> = emptyList(),
    val nomesPacientes: Map<String, String> = emptyMap(),
    val observacoes: Map<String, String> = emptyMap(),  // D-05: texto de cada documento, sobrevive ao girar
    val carregando: Boolean = true,
    val salvando: Boolean = false,                       // D-02: true enquanto grava
    val mensagem: String? = null,                        // aviso de sucesso (vira Snackbar)
    val bloqueio: String? = null,                        // permanente: sessão, perfil, erro de lista
    val erro: String? = null                             // passageiro: falha ao confirmar/corrigir
)

class RevisaoViewModel(
    private val repository: DocumentoRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevisaoUiState())
    val uiState: StateFlow<RevisaoUiState> = _uiState.asStateFlow()

    // Cache dos nomes dos pacientes, para o médico saber de quem é cada exame
    private val nomesPacientes = mutableMapOf<String, String>()
    private val nomesEmBusca = mutableSetOf<String>()

    // O médico é o próprio usuário logado
    private val medicoId: String? = auth.usuarioLogadoUid

    init {
        val uid = medicoId
        if (uid == null) {
            _uiState.update {
                it.copy(carregando = false, bloqueio = "Sessão expirada. Entre novamente.")
            }
        } else {
            viewModelScope.launch {
                runCatching { auth.buscarUsuario(uid) }
                    .onSuccess { usuario ->
                        // D-11: só médico acessa a Revisão
                        if (usuario.perfil == "medico") {
                            carregar(uid)
                        } else {
                            _uiState.update {
                                it.copy(
                                    carregando = false,
                                    bloqueio = "Esta tela é só para médicos."
                                )
                            }
                        }
                    }
                    .onFailure { e ->
                        Log.e("Goianinho", "Revisão: buscarUsuario falhou", e)
                        _uiState.update {
                            it.copy(carregando = false, bloqueio = "Não foi possível carregar seu perfil.")
                        }
                    }
            }
        }
    }

    private fun carregar(medicoId: String) {
        viewModelScope.launch {
            repository.ouvirPendentes(medicoId)
                .catch { e ->
                    Log.e("Goianinho", "ouvirPendentes falhou", e)
                    _uiState.update { it.copy(carregando = false, bloqueio = mensagemDeErro(e)) }
                }
                .collect { documentos ->
                    _uiState.update { it.copy(documentos = documentos, carregando = false) }
                    documentos.forEach { buscarNomePaciente(it.pacienteId) }
                }
        }
    }

    private fun buscarNomePaciente(id: String) {
        if (id.isBlank() || nomesPacientes.containsKey(id) || !nomesEmBusca.add(id)) return
        viewModelScope.launch {
            runCatching { auth.buscarUsuario(id).nome }
                .onSuccess { nome ->
                    nomesPacientes[id] = nome
                    _uiState.update { it.copy(nomesPacientes = nomesPacientes.toMap()) }
                }
                .onFailure { e ->
                    Log.e("Goianinho", "Revisão: falha ao buscar o nome do paciente", e)
                }
            nomesEmBusca.remove(id)
        }
    }

    // D-05: a tela avisa a cada letra digitada; o texto fica guardado aqui
    fun atualizarObservacao(documentoId: String, texto: String) {
        _uiState.update { it.copy(observacoes = it.observacoes + (documentoId to texto)) }
    }

    fun confirmar(documento: Documento, observacao: String) {
        val medico = medicoId ?: return
        if (_uiState.value.salvando) return                 // D-02: ignora o segundo toque
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null) }
            repository.confirmar(documento.id, medico, observacao)
                .onSuccess { concluir(documento.id, "Resumo confirmado.") }
                .onFailure { e -> falhar(e, "confirmar") }
        }
    }

    fun corrigir(documento: Documento, observacao: String) {
        val medico = medicoId ?: return
        if (_uiState.value.salvando) return
        // D-01 / RN-13: corrigir exige observação
        if (observacao.isBlank()) {
            _uiState.update { it.copy(erro = "Escreva uma observação para corrigir o resumo.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null) }
            repository.corrigir(documento.id, medico, observacao)
                .onSuccess { concluir(documento.id, "Correção registrada.") }
                .onFailure { e -> falhar(e, "corrigir") }
        }
    }

    private fun concluir(documentoId: String, texto: String) {
        _uiState.update {
            it.copy(
                salvando = false,
                mensagem = texto,
                observacoes = it.observacoes - documentoId
            )
        }
    }

    private fun falhar(e: Throwable, acao: String) {
        Log.e("Goianinho", "Revisão: $acao falhou", e)
        _uiState.update { it.copy(salvando = false, erro = mensagemDeErro(e)) }
    }

    fun limparErro() {
        _uiState.update { it.copy(erro = null) }
    }

    // D-09: a tela chama depois de mostrar o Snackbar
    fun limparMensagem() {
        _uiState.update { it.copy(mensagem = null) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GoianinhoApp
                RevisaoViewModel(
                    app.container.documentoRepository,
                    app.container.authRepository
                )
            }
        }
    }
}