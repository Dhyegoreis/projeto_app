package com.goianinho.medicina.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.goianinho.medicina.GoianinhoApp
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.ui.comum.mensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CadastroUiState(
    val perfil: String = "paciente",
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val erroNome: String? = null,
    val erroEmail: String? = null,
    val erroSenha: String? = null,
    val erroGeral: String? = null,
    val carregando: Boolean = false,
    val contaCriada: Boolean = false
)

class CadastroViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CadastroUiState())
    val uiState: StateFlow<CadastroUiState> = _uiState.asStateFlow()

    fun onPerfilChange(v: String) = _uiState.update { it.copy(perfil = v) }
    fun onNomeChange(v: String) =
        _uiState.update { it.copy(nome = v, erroNome = null, erroGeral = null) }
    fun onEmailChange(v: String) =
        _uiState.update { it.copy(email = v, erroEmail = null, erroGeral = null) }
    fun onSenhaChange(v: String) =
        _uiState.update { it.copy(senha = v, erroSenha = null, erroGeral = null) }

    // RF-04, L-08, L-10, L-11
    fun criarConta() {
        val s = _uiState.value
        if (s.carregando) return
        val erroNome = if (s.nome.isBlank()) "Digite seu nome." else null
        val erroEmail = if (!Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches())
            "Digite um e-mail válido." else null
        val erroSenha = if (s.senha.length < 6)
            "A senha precisa ter pelo menos 6 caracteres." else null
        if (erroNome != null || erroEmail != null || erroSenha != null) {
            _uiState.update {
                it.copy(erroNome = erroNome, erroEmail = erroEmail, erroSenha = erroSenha)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erroGeral = null) }
            repo.criarConta(s.nome.trim(), s.email.trim(), s.senha, s.perfil).fold(
                onSuccess = {
                    ContaRecemCriada.email = s.email.trim()   // L-10
                    repo.sair()
                    _uiState.update { it.copy(carregando = false, contaCriada = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(carregando = false, erroGeral = mensagemDeErro(e)) }
                }
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GoianinhoApp
                CadastroViewModel(app.container.authRepository)
            }
        }
    }
}