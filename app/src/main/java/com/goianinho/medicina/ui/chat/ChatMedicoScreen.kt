package com.goianinho.medicina.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goianinho.medicina.ui.theme.AlertaTexto
import com.goianinho.medicina.ui.theme.AzulNoite
import com.goianinho.medicina.ui.theme.Branco
import com.goianinho.medicina.ui.theme.CinzaTexto
import com.goianinho.medicina.ui.theme.FundoApp
import com.goianinho.medicina.ui.theme.VerdeClaro
import androidx.compose.foundation.layout.RowScope

@Composable
fun ChatMedicoScreen(
    onAbrirRevisao: () -> Unit
) {
    val viewModel: ChatViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().background(FundoApp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(AzulNoite)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Goianinho",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Branco
            )
            Text(
                "Seu assistente · Dr. ${state.nomeUsuario}",
                style = MaterialTheme.typography.bodyMedium,
                color = VerdeClaro
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(Branco)
                .padding(vertical = 8.dp)
        ) {
            Contador(state.consultasHoje, "consultas hoje", AzulNoite)
            Contador(state.paraRevisar, "para revisar", AlertaTexto)
            Contador(state.semResposta, "sem resposta", AzulNoite)
        }
        state.erro?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        ChatConteudo(
            mensagens = state.mensagens,
            chips = listOf("Próximo paciente", "Pendências do dia", "Abrir revisões"),
            onChip = { chip ->
                if (chip == "Abrir revisões") onAbrirRevisao() else viewModel.enviar(chip)
            },
            onEnviar = { viewModel.enviar(it) },
            onFonteClick = { onAbrirRevisao() },
            placeholder = "Peça algo ao Goianinho…",
            corEnviar = AzulNoite,
            enabled = state.perfil.isNotBlank()
        )
    }
}

@Composable
private fun RowScope.Contador(valor: Int, rotulo: String, cor: androidx.compose.ui.graphics.Color) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            valor.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = cor
        )
        Text(rotulo, style = MaterialTheme.typography.bodySmall, color = CinzaTexto)
    }
}
