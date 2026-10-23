package com.goianinho.medicina.ui.documentos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RevisaoUiState(
    val documentos: List<Documento> = emptyList(),
    val nomesPacientes: Map<String, String> = emptyMap(),
    val carregando: Boolean = true,
    val mensagem: String? = null,
    val bloqueio: String? = null,   // permanente: sessão, erro de listener
    val erro: String? = null        // passageiro: falha ao confirmar/corrigir
)

class RevisaoViewModel(
    private val repository: DocumentoRepository = DocumentoRepository(),
    private val auth: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevisaoUiState())
    val uiState: StateFlow<RevisaoUiState> = _uiState.asStateFlow()

    private var listener: ListenerRegistration? = null

    // Cache dos nomes dos pacientes, para o médico saber de quem é cada exame
    private val nomesPacientes = mutableMapOf<String, String>()
    private val nomesEmBusca = mutableSetOf<String>()

    // O médico é o próprio usuário logado
    private val medicoId: String? = auth.usuarioLogadoUid

    init {
        if (medicoId == null) {
            _uiState.value = _uiState.value.copy(
                carregando = false,
                bloqueio = "Sessão expirada. Entre novamente."
            )
        } else {
            carregar(medicoId)
        }
    }

    private fun carregar(medicoId: String) {
        listener?.remove()
        listener = repository.ouvirPendentes(medicoId, onErro = ::mostrarBloqueio) { documentos ->
            _uiState.value = _uiState.value.copy(
                documentos = documentos,
                carregando = false
            )
            documentos.forEach { buscarNomePaciente(it.pacienteId) }
        }
    }

    private fun buscarNomePaciente(id: String) {
        if (id.isBlank() || nomesPacientes.containsKey(id) || !nomesEmBusca.add(id)) return
        viewModelScope.launch {
            try {
                nomesPacientes[id] = auth.buscarUsuario(id).nome
                _uiState.value = _uiState.value.copy(nomesPacientes = nomesPacientes.toMap())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Goianinho", "Revisão: falha ao buscar o nome do paciente", e)
            } finally {
                nomesEmBusca.remove(id)
            }
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

    // Erro de listener: a lista fica vazia enquanto o problema existir, então o aviso é fixo
    private fun mostrarBloqueio(mensagem: String) {
        _uiState.value = _uiState.value.copy(carregando = false, bloqueio = mensagem)
    }

    // Falha de uma ação do usuário (confirmar/corrigir): aviso passageiro
    private fun mostrarErro(mensagem: String) {
        _uiState.value = _uiState.value.copy(carregando = false, erro = mensagem)
    }

    fun limparErro() {
        _uiState.value = _uiState.value.copy(erro = null)
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}
