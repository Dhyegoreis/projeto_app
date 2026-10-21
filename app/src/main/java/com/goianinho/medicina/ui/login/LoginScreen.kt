package com.goianinho.medicina.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onEntrouComoPaciente: () -> Unit,
    onEntrouComoMedico: () -> Unit,
    onCriarConta: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // RF-03: quando o perfil salvo aparece, navega para o fluxo certo
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) { Text("✦", color = Color.White, style = MaterialTheme.typography.headlineMedium) }

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

        Text("Entrar como", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SeletorPerfil(state.perfilSelecionado, viewModel::onPerfilChange)

        OutlinedTextField(
            value = state.email, onValueChange = viewModel::onEmailChange,
            label = { Text("E-mail") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.senha, onValueChange = viewModel::onSenhaChange,
            label = { Text("Senha") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(onClick = viewModel::esqueciSenha, modifier = Modifier.align(Alignment.End)) {
            Text("Esqueci minha senha", fontWeight = FontWeight.Bold)
        }

        state.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.mensagem?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        Button(
            onClick = viewModel::entrar,
            enabled = !state.carregando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            if (state.carregando) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Entrar", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = { /* RF-06 (Could): Google, fazer por último */ },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = RoundedCornerShape(20.dp)
        ) { Text("Continuar com Google", fontWeight = FontWeight.Bold) }

        TextButton(onClick = onCriarConta, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Ainda não tem conta? Criar conta")
        }
    }
}