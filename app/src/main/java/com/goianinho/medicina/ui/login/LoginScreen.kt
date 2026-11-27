package com.goianinho.medicina.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goianinho.medicina.ui.comum.AVISO_GOOGLE
import com.goianinho.medicina.ui.comum.TelaErro
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onEntrouComoPaciente: () -> Unit,
    onEntrouComoMedico: () -> Unit,
    onCriarConta: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()   // L-12
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // L-05: aviso de perfil divergente
    LaunchedEffect(state.avisoPerfil) {
        state.avisoPerfil?.let {
            snackbar.showSnackbar(it)
            viewModel.avisoMostrado()
        }
    }
    // RF-03: navega pelo perfil salvo
    LaunchedEffect(state.perfilLogado) {
        when (state.perfilLogado) {
            "medico" -> onEntrouComoMedico()
            "paciente" -> onEntrouComoPaciente()
        }
    }

    if (state.verificandoSessao) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // L-04: erro ao reabrir o app (sem internet etc.)
    state.erroSessao?.let { mensagem ->
        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TelaErro(mensagem = mensagem, onTentarDeNovo = viewModel::tentarDeNovo)
            TextButton(onClick = viewModel::continuarSemSessao) {
                Text("Entrar com outra conta")
            }
        }
        return
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)                 // L-09
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }

                Text(
                    "Seu cuidado, organizado por quem conversa com você.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Entre para falar com seu assistente e seu médico.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Entrar como",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SeletorPerfil(
                    state.perfilSelecionado,
                    viewModel::onPerfilChange,
                    enabled = !state.carregando
                )

                OutlinedTextField(
                    value = state.email, onValueChange = viewModel::onEmailChange,
                    label = { Text("E-mail") }, singleLine = true,
                    isError = state.erroEmail != null,
                    supportingText = { state.erroEmail?.let { Text(it) } },
                    enabled = !state.carregando,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.senha, onValueChange = viewModel::onSenhaChange,
                    label = { Text("Senha") }, singleLine = true,
                    isError = state.erroSenha != null,
                    supportingText = { state.erroSenha?.let { Text(it) } },
                    enabled = !state.carregando,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = viewModel::esqueciSenha,
                    enabled = !state.carregando,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Esqueci minha senha", fontWeight = FontWeight.Bold) }

                state.erroGeral?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                state.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

                Button(
                    onClick = viewModel::entrar,
                    enabled = !state.carregando,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    if (state.carregando) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Entrar", fontWeight = FontWeight.Bold)
                    }
                }

                // L-06 / RNF-01
                OutlinedButton(
                    onClick = { scope.launch { snackbar.showSnackbar(AVISO_GOOGLE) } },
                    enabled = !state.carregando,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = RoundedCornerShape(20.dp)
                ) { Text("Continuar com Google", fontWeight = FontWeight.Bold) }

                TextButton(
                    onClick = onCriarConta,
                    enabled = !state.carregando,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("Ainda não tem conta? Criar conta") }
            }
        }
    }
}