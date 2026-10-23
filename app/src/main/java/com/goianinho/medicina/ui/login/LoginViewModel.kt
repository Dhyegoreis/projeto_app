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

data class LoginUiState(
    val perfilSelecionado: String = "paciente",
    val email: String = "",
    val senha: String = "",
    val carregando: Boolean = false,
    val verificandoSessao: Boolean = true,
    val erro: String? = null,
    val mensagem: String? = null,
    val perfilLogado: String? = null   // quando preenchido, a tela navega
)

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init { verificarSessao() }

    // RF-07: se já há usuário logado ao abrir o app, pula o login
    private fun verificarSessao() {
        val uid = repo.usuarioLogadoUid
        if (uid == null) {
            _uiState.update { it.copy(verificandoSessao = false) }
            return
        }
        viewModelScope.launch {
            try {
                val usuario = repo.buscarUsuario(uid)
                _uiState.update { it.copy(verificandoSessao = false, perfilLogado = usuario.perfil) }
            } catch (e: Exception) {
                _uiState.update { it.copy(verificandoSessao = false) }
            }
        }
    }

    fun onPerfilChange(v: String) = _uiState.update { it.copy(perfilSelecionado = v) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, erro = null) }
    fun onSenhaChange(v: String) = _uiState.update { it.copy(senha = v, erro = null) }

    // RF-01
    fun entrar() {
        val s = _uiState.value
        val erro = when {
            s.email.isBlank() -> "Digite seu e-mail."
            s.senha.length < 6 -> "A senha precisa ter pelo menos 6 caracteres."   // RN-02
            else -> null
        }
        if (erro != null) {
            _uiState.update { it.copy(erro = erro) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null, mensagem = null) }
            try {
                val usuario = repo.entrar(s.email.trim(), s.senha)
                _uiState.update { it.copy(carregando = false, perfilLogado = usuario.perfil) }
            } catch (e: Exception) {
                _uiState.update { it.copy(carregando = false, erro = traduzirErroAuth(e)) }
            }
        }
    }

    // RF-05
    fun esqueciSenha() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty()) {
            _uiState.update { it.copy(erro = "Digite seu e-mail acima para recuperar a senha.") }
            return
        }
        viewModelScope.launch {
            try {
                repo.recuperarSenha(email)
                _uiState.update { it.copy(mensagem = "Enviamos um link para o seu e-mail.", erro = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(erro = traduzirErroAuth(e)) }
            }
        }
    }
}