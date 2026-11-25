package com.goianinho.medicina.ui.agenda

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goianinho.medicina.ui.comum.TelaCarregando
import com.goianinho.medicina.ui.comum.TelaErro
import com.goianinho.medicina.ui.theme.AlertaFundo
import com.goianinho.medicina.ui.theme.AlertaTexto
import com.goianinho.medicina.ui.theme.AzulNoite
import com.goianinho.medicina.ui.theme.Branco
import com.goianinho.medicina.ui.theme.CinzaTexto
import com.goianinho.medicina.ui.theme.VerdeClaro
import com.goianinho.medicina.ui.theme.VerdeEscuro
import com.goianinho.medicina.ui.theme.VerdePrincipal

// Módulo 4. Tela real: ver doc.md desta pasta. Protótipo: "4 · Agenda do dia (médico)".
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun AgendaMedicoScreen(
    onVerResumo: () -> Unit,
    onIniciarChamada: () -> Unit,
    viewModel: AgendaViewModel = viewModel(factory = AgendaViewModel.Factory)
) {
    val uiState by viewModel.uiStateMedico.collectAsState()

    when (val estado = uiState) {
        AgendaMedicoUiState.Carregando -> TelaCarregando()
        is AgendaMedicoUiState.Erro -> TelaErro(estado.mensagem, onTentarDeNovo = viewModel::recarregarMedico)
        is AgendaMedicoUiState.Sucesso -> AgendaMedicoConteudo(
            estado = estado,
            onVerResumo = onVerResumo,
            onIniciarChamada = onIniciarChamada
        )
    }
}

@Composable
private fun AgendaMedicoConteudo(
    estado: AgendaMedicoUiState.Sucesso,
    onVerResumo: () -> Unit,
    onIniciarChamada: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Agenda de hoje",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulNoite
                )
                Text(
                    "${estado.diaRotulo} · ${rotuloDeConsultas(estado.totalConsultas)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinzaTexto
                )
            }

            CardPacienteAtual(
                horaAtual = estado.consultaAtual?.hora ?: "09:30",
                pacienteNome = estado.consultaAtual?.pacienteNome ?: "[PACIENTE A]",
                briefing = estado.consultaAtual?.briefing
                    ?: "Motivo relatado: [MOTIVO DA CONSULTA]. Aguardando confirmação do resumo.",
                onVerResumo = onVerResumo,
                onIniciarChamada = onIniciarChamada
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "PRÓXIMAS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CinzaTexto
                )
                if (estado.proximas.isEmpty()) {
                    Text(
                        "Nenhuma outra consulta hoje.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinzaTexto
                    )
                } else {
                    estado.proximas.forEach { consulta ->
                        CardConsultaProxima(consulta)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardPacienteAtual(
    horaAtual: String,
    pacienteNome: String,
    briefing: String,
    onVerResumo: () -> Unit,
    onIniciarChamada: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AzulNoite)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "AGORA · $horaAtual",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Branco.copy(alpha = 0.75f)
            )
            Text(
                pacienteNome,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Branco
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VerdeEscuro, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Briefing do Goianinho",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Branco
                )
                Text(
                    briefing,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Branco.copy(alpha = 0.9f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onVerResumo,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Branco.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Branco)
                ) {
                    Text("Ver resumo", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onIniciarChamada,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Branco, contentColor = AzulNoite)
                ) {
                    Text("\uD83C\uDFA5  Iniciar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CardConsultaProxima(consulta: ConsultaProximaUi) {
    val (corFundo, corTexto, rotulo) = if (consulta.resumoPendente) {
        Triple(AlertaFundo, AlertaTexto, "Resumo aguardando você")
    } else {
        Triple(VerdeClaro, VerdePrincipal, "Confirmado")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Branco)
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                consulta.hora,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AzulNoite
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    consulta.pacienteNome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AzulNoite
                )
                Text(
                    rotulo,
                    modifier = Modifier
                        .background(corFundo, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = corTexto
                )
                Text(
                    consulta.dicaEngajamento,
                    style = MaterialTheme.typography.bodySmall,
                    color = CinzaTexto
                )
            }
        }
    }
}