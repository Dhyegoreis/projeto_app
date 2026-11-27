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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CadastroScreen(
    onContaCriada: () -> Unit,
    onVoltar: () -> Unit,
    viewModel: CadastroViewModel = viewModel(factory = CadastroViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()   // L-12

    LaunchedEffect(state.contaCriada) {
        if (state.contaCriada) onContaCriada()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)                          // L-09
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Criar conta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                "Eu sou",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SeletorPerfil(state.perfil, viewModel::onPerfilChange, enabled = !state.carregando)

            OutlinedTextField(
                value = state.nome, onValueChange = viewModel::onNomeChange,
                label = { Text("Nome") }, singleLine = true,
                isError = state.erroNome != null,
                supportingText = { state.erroNome?.let { Text(it) } },
                enabled = !state.carregando,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
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
                label = { Text("Senha (mínimo 6 caracteres)") }, singleLine = true,
                isError = state.erroSenha != null,
                supportingText = { state.erroSenha?.let { Text(it) } },
                enabled = !state.carregando,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )

            state.erroGeral?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = viewModel::criarConta,
                enabled = !state.carregando,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                if (state.carregando) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Criar conta", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onVoltar,
                enabled = !state.carregando,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Voltar") }
        }
    }
}