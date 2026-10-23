package com.goianinho.medicina.ui.login

import com.goianinho.medicina.data.repository.traduzirErroAuth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.repository.AuthRepository
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
    val carregando: Boolean = false,
    val erro: String? = null,
    val contaCriada: Boolean = false
)

class CadastroViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadastroUiState())
    val uiState: StateFlow<CadastroUiState> = _uiState.asStateFlow()

    fun onPerfilChange(v: String) = _uiState.update { it.copy(perfil = v) }
    fun onNomeChange(v: String) = _uiState.update { it.copy(nome = v, erro = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, erro = null) }
    fun onSenhaChange(v: String) = _uiState.update { it.copy(senha = v, erro = null) }

    // RF-04
    fun criarConta() {
        val s = _uiState.value
        val erro = when {
            s.nome.isBlank() -> "Digite seu nome."
            s.email.isBlank() -> "Digite seu e-mail."
            s.senha.length < 6 -> "A senha precisa ter pelo menos 6 caracteres."   // RN-02
            else -> null
        }
        if (erro != null) {
            _uiState.update { it.copy(erro = erro) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            try {
                repo.criarConta(s.nome.trim(), s.email.trim(), s.senha, s.perfil)
                // O Firebase deixa o usuário logado ao criar a conta. Saímos para ele entrar
                // pela tela de login, como o AppNavigation espera (cadastro volta ao login).
                repo.sair()
                _uiState.update { it.copy(carregando = false, contaCriada = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(carregando = false, erro = traduzirErroAuth(e)) }
            }
        }
    }
}