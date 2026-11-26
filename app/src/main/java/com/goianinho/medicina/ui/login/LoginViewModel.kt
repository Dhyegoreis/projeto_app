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

data class LoginUiState(
    val perfilSelecionado: String = "paciente",
    val email: String = "",
    val senha: String = "",
    val erroEmail: String? = null,
    val erroSenha: String? = null,
    val erroGeral: String? = null,
    val erroSessao: String? = null,      // L-04
    val mensagem: String? = null,
    val avisoPerfil: String? = null,     // L-05
    val carregando: Boolean = false,
    val verificandoSessao: Boolean = true,
    val perfilLogado: String? = null
)

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // L-10: veio do cadastro? preenche o e-mail e avisa
        ContaRecemCriada.email?.let { e ->
            _uiState.update {
                it.copy(email = e, mensagem = "Conta criada! Entre com seu e-mail e senha.")
            }
            ContaRecemCriada.email = null
        }
        verificarSessao()
    }

    // RF-07 + L-04
    private fun verificarSessao() {
        val uid = repo.usuarioLogadoUid
        if (uid == null) {
            _uiState.update { it.copy(verificandoSessao = false) }
            return
        }
        _uiState.update { it.copy(verificandoSessao = true, erroSessao = null) }
        viewModelScope.launch {
            repo.buscarUsuario(uid).fold(
                onSuccess = { u ->
                    _uiState.update { it.copy(verificandoSessao = false, perfilLogado = u.perfil) }
                },
                onFailure = { e ->
                    if (e is IllegalStateException) {
                        // perfil não existe: derruba a sessão
                        repo.sair()
                        _uiState.update { it.copy(verificandoSessao = false) }
                    } else {
                        _uiState.update {
                            it.copy(verificandoSessao = false, erroSessao = mensagemDeErro(e))
                        }
                    }
                }
            )
        }
    }

    fun tentarDeNovo() = verificarSessao()

    fun continuarSemSessao() {
        repo.sair()
        _uiState.update { it.copy(erroSessao = null) }
    }

    fun onPerfilChange(v: String) = _uiState.update { it.copy(perfilSelecionado = v) }
    fun onEmailChange(v: String) =
        _uiState.update { it.copy(email = v, erroEmail = null, erroGeral = null) }
    fun onSenhaChange(v: String) =
        _uiState.update { it.copy(senha = v, erroSenha = null, erroGeral = null) }
    fun avisoMostrado() = _uiState.update { it.copy(avisoPerfil = null) }

    // RF-01, RN-01, L-05, L-08
    fun entrar() {
        val s = _uiState.value
        if (s.carregando) return
        val erroEmail = if (!Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches())
            "Digite um e-mail válido." else null
        val erroSenha = if (s.senha.length < 6)
            "A senha precisa ter pelo menos 6 caracteres." else null
        if (erroEmail != null || erroSenha != null) {
            _uiState.update { it.copy(erroEmail = erroEmail, erroSenha = erroSenha) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erroGeral = null, mensagem = null) }
            repo.entrar(s.email.trim(), s.senha).fold(
                onSuccess = { u ->
                    val aviso = if (u.perfil != s.perfilSelecionado)
                        "Sua conta é de ${if (u.perfil == "medico") "médico" else "paciente"}."
                    else null
                    _uiState.update {
                        it.copy(carregando = false, perfilLogado = u.perfil, avisoPerfil = aviso)
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(carregando = false, erroGeral = mensagemDeErro(e)) }
                }
            )
        }
    }

    // RF-05 + L-07: mesma mensagem sempre
    fun esqueciSenha() {
        val s = _uiState.value
        if (s.carregando) return
        if (!Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches()) {
            _uiState.update { it.copy(erroEmail = "Digite seu e-mail para receber o link.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erroGeral = null) }
            repo.recuperarSenha(s.email.trim())
            _uiState.update {
                it.copy(
                    carregando = false,
                    mensagem = "Se o e-mail estiver cadastrado, enviamos um link."
                )
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GoianinhoApp
                LoginViewModel(app.container.authRepository)
            }
        }
    }
}