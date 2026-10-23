package com.goianinho.medicina.ui.home

import com.goianinho.medicina.ui.comum.AvisoErro
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goianinho.medicina.R
import com.goianinho.medicina.ui.theme.AlertaFundo
import com.goianinho.medicina.ui.theme.AlertaTexto
import com.goianinho.medicina.ui.theme.AzulNoite
import com.goianinho.medicina.ui.theme.Branco
import com.goianinho.medicina.ui.theme.CinzaBorda
import com.goianinho.medicina.ui.theme.CinzaTexto
import com.goianinho.medicina.ui.theme.FundoApp
import com.goianinho.medicina.ui.theme.VerdePrincipal
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeMedicoScreen(
    onAbrirChat: () -> Unit,
    onAbrirRevisoes: () -> Unit,
    onAbrirAgenda: () -> Unit,
    onSair: () -> Unit,
    onIniciarChamada: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val hora = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

    Column(
        Modifier
            .fillMaxSize()
            .background(FundoApp)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(state.dataHoje, style = MaterialTheme.typography.bodyMedium, color = CinzaTexto)
                Text(
                    "Bom dia, Dr. ${state.nome}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulNoite
                )
            }
            TextButton(onClick = {
                viewModel.sair()
                onSair()
            }) { Text("Sair", color = VerdePrincipal) }
        }

        state.erro?.let { AvisoErro(it) }

        // Contadores
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador(state.consultasHoje.size, "consultas hoje", Branco, AzulNoite, Modifier.weight(1f))
            Contador(state.pendentes.size, "para revisar", AlertaFundo, AlertaTexto, Modifier.weight(1f))
            Contador(state.semResposta, "sem resposta", Branco, AzulNoite, Modifier.weight(1f))
        }

        // Próxima consulta
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AzulNoite),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val proxima = state.proximaDoDia
                val data = proxima?.dataHora?.toDate()
                if (proxima == null || data == null) {
                    Text("Nenhuma consulta marcada", color = Branco, fontWeight = FontWeight.Bold)
                } else {
                    val paciente = state.nomesPacientes[proxima.pacienteId].orEmpty().ifEmpty { "Paciente" }
                    Text(
                        "PRÓXIMA · ${hora.format(data)}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Branco
                    )
                    Text(
                        "$paciente · teleconsulta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Branco
                    )
                    Text(
                        "Briefing pronto. A avaliação clínica é sua.",
                        color = Branco
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onAbrirRevisoes,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Branco),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Branco)
                        ) { Text("Ver briefing") }
                        Button(
                            onClick = onIniciarChamada,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Branco,
                                contentColor = AzulNoite
                            )
                        ) { Text("Iniciar") }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "PRECISA DE VOCÊ",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = CinzaTexto
            )
            TextButton(onClick = onAbrirRevisoes) {
                Text("Ver todas", color = VerdePrincipal, fontWeight = FontWeight.Bold)
            }
        }
        if (state.pendentes.isEmpty()) {
            Text("Nada pendente", color = CinzaTexto)
        } else {
            state.pendentes.take(3).forEach { doc ->
                val paciente = state.nomesPacientes[doc.pacienteId].orEmpty().ifEmpty { "Paciente" }
                ItemPendente("Revisar ${doc.nome} · $paciente", "Aguardando sua confirmação", AlertaTexto, onAbrirRevisoes)
            }
        }

        // Campo de pergunta ao Goianinho
        Surface(
            onClick = onAbrirChat,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Branco,
            border = BorderStroke(1.dp, CinzaBorda)
        ) {
            Row(
                Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pergunte ao Goianinho…", modifier = Modifier.weight(1f), color = CinzaTexto)
                Box(
                    Modifier.size(36.dp).background(AzulNoite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_chat),
                        contentDescription = "Abrir o chat",
                        tint = Branco
                    )
                }
            }
        }
    }
}

@Composable
private fun Contador(
    valor: Int,
    rotulo: String,
    fundo: Color,
    texto: Color,
    modifier: Modifier
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = fundo),
        border = BorderStroke(1.dp, CinzaBorda),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = texto
            )
            Text(rotulo, style = MaterialTheme.typography.bodySmall, color = texto)
        }
    }
}

@Composable
private fun ItemPendente(titulo: String, apoio: String, ponto: Color, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Branco),
        border = BorderStroke(1.dp, CinzaBorda),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).background(ponto, CircleShape))
            Column {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulNoite
                )
                Text(apoio, color = CinzaTexto)
            }
        }
    }
}
