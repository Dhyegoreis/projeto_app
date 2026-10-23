package com.goianinho.medicina.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.model.Usuario
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.VinculoException
import com.goianinho.medicina.data.repository.traduzirErroFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Provisório (demo): diálogo "Assumir paciente" do Início do médico.
// Fica separado do HomeViewModel porque o Início só lê (RN-10).
data class AssumirPacienteUiState(
    val carregando: Boolean = false,
    val pacientes: List<Usuario> = emptyList(),
    val assumindoUid: String? = null,
    val erro: String? = null,
    val mensagem: String? = null,
    val medicoUid: String = ""
)

class AssumirPacienteViewModel(
    private val auth: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssumirPacienteUiState(medicoUid = auth.usuarioLogadoUid.orEmpty()))
    val uiState: StateFlow<AssumirPacienteUiState> = _uiState.asStateFlow()

    private var jobCarregar: Job? = null
    private var jobAssumir: Job? = null

    // Chamado quando o diálogo abre; lista vazia é um estado válido
    fun carregar() {
        jobCarregar?.cancel()
        _uiState.update { it.copy(carregando = true, erro = null) }
        jobCarregar = viewModelScope.launch {
            try {
                val lista = auth.listarPacientes()
                _uiState.update { it.copy(carregando = false, pacientes = lista) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Goianinho", "Início: falha ao listar os pacientes", e)
                _uiState.update { it.copy(carregando = false, erro = traduzirErroFirestore(e)) }
            }
        }
    }

    fun assumir(paciente: Usuario) {
        if (_uiState.value.assumindoUid != null) return
        val medicoId = auth.usuarioLogadoUid
        if (medicoId.isNullOrBlank()) {
            _uiState.update { it.copy(erro = "Sessão expirada. Entre novamente.", mensagem = null) }
            return
        }
        _uiState.update { it.copy(assumindoUid = paciente.uid, erro = null, mensagem = null) }
        jobAssumir = viewModelScope.launch {
            try {
                auth.assumirPaciente(paciente.uid, medicoId)
                val nome = paciente.nome.ifEmpty { paciente.email }
                _uiState.update {
                    it.copy(assumindoUid = null, mensagem = "Agora você acompanha $nome.")
                }
                carregar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: VinculoException) {
                _uiState.update { it.copy(assumindoUid = null, erro = e.message) }
            } catch (e: Exception) {
                Log.e("Goianinho", "Início: falha ao assumir o paciente", e)
                _uiState.update { it.copy(assumindoUid = null, erro = traduzirErroFirestore(e)) }
            }
        }
    }

    // Zera o estado ao fechar o diálogo
    fun limpar() {
        jobCarregar?.cancel()
        jobAssumir?.cancel()
        _uiState.value = AssumirPacienteUiState(medicoUid = auth.usuarioLogadoUid.orEmpty())
    }
}
