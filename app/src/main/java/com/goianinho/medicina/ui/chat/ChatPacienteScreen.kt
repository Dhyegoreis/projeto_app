package com.goianinho.medicina.ui.chat

import com.goianinho.medicina.ui.comum.AvisoErro
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goianinho.medicina.data.model.Mensagem
import com.goianinho.medicina.ui.theme.AzulNoite
import com.goianinho.medicina.ui.theme.Branco
import com.goianinho.medicina.ui.theme.CinzaTexto
import com.goianinho.medicina.ui.theme.FundoApp
import com.goianinho.medicina.ui.theme.VerdePrincipal

@Composable
fun ChatPacienteScreen() {
    val viewModel: ChatViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    // Com o histórico vazio, uma bolha local de boas-vindas (não é gravada)
    val mensagens = if (state.mensagens.isEmpty() && state.bloqueio == null &&
        state.nomeUsuario.isNotBlank()) {
        listOf(
            Mensagem(
                id = "boas-vindas",
                autor = "goianinho",
                texto = "Olá, ${state.nomeUsuario}. Sou o Goianinho, ${if (state.nomeMedico.isNotEmpty()) "assistente do Dr. ${state.nomeMedico}" else "seu assistente"}. " +
                    "Posso marcar consultas, organizar seus exames e levar tudo pronto para ele."
            )
        )
    } else {
        state.mensagens
    }

    Column(Modifier.fillMaxSize().background(FundoApp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Branco)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Goianinho",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AzulNoite
            )
            Text(
                if (state.nomeMedico.isNotEmpty()) "Assistente do Dr. ${state.nomeMedico}" else "Goianinho · seu assistente",
                style = MaterialTheme.typography.bodyMedium,
                color = CinzaTexto
            )
        }
        state.bloqueio?.let { AvisoErro(it, Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
        state.avisoContadores?.let { AvisoErro(it, Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
        state.erro?.let {
            AvisoErro(
                it,
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onSumir = { viewModel.limparErro() }
            )
        }
        ChatConteudo(
            mensagens = mensagens,
            chips = listOf("Marcar consulta", "Enviar outro exame", "Tirar dúvida"),
            onChip = { viewModel.enviar(it) },
            onEnviar = { viewModel.enviar(it) },
            onFonteClick = null,
            placeholder = "Fale com o Goianinho…",
            corEnviar = VerdePrincipal,
            enabled = state.perfil.isNotBlank() && state.bloqueio == null
        )
    }
}
