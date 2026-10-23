package com.goianinho.medicina.ui.agenda

import com.goianinho.medicina.ui.comum.AvisoErro
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.goianinho.medicina.ui.theme.AzulNoite
import com.goianinho.medicina.ui.theme.Branco
import com.goianinho.medicina.ui.theme.CinzaBorda
import com.goianinho.medicina.ui.theme.CinzaTexto
import com.goianinho.medicina.ui.theme.VerdeClaro
import com.goianinho.medicina.ui.theme.VerdePrincipal

// Módulo 4. Tela real: ver doc.md desta pasta. Protótipo: "4 · Agenda (paciente)".
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun AgendaPacienteScreen(
    onEntrarNaChamada: () -> Unit,
    viewModel: AgendaViewModel = viewModel()
) {
    val uiState by viewModel.uiStatePaciente.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Agenda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AzulNoite
        )

        uiState.mensagemErro?.let { erro ->
            AvisoErro(erro)
        }

        CardProximaConsulta(
            medicoNome = uiState.medicoNome,
            temProximaConsulta = uiState.temProximaConsulta,
            tipoEData = "${uiState.proximaConsultaTipo} · ${uiState.proximaConsultaData} · ${uiState.proximaConsultaHora}",
            onEntrarNaChamada = onEntrarNaChamada
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Marcar nova consulta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AzulNoite
            )
            SeletorDeDias(
                dias = uiState.dias,
                diaSelecionado = uiState.diaSelecionado,
                onDiaClick = viewModel::selecionarDia
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Horários disponíveis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AzulNoite
            )
            if (uiState.horarios.isEmpty()) {
                Text(
                    "Nenhum horário livre nesse dia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinzaTexto
                )
            } else {
                GradeDeHorarios(
                    horarios = uiState.horarios,
                    horarioSelecionadoId = uiState.horarioSelecionadoId,
                    onHorarioClick = viewModel::selecionarHorario
                )
            }
        }

        val horario = uiState.horarioSelecionado
        Button(
            onClick = viewModel::confirmarReserva,
            enabled = horario != null && !uiState.reservando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulNoite, contentColor = Branco)
        ) {
            Text(
                when {
                    uiState.reservando -> "Reservando..."
                    horario != null -> "Confirmar ${uiState.diaSelecionadoRotulo} às ${horario.hora}"
                    else -> "Escolha um horário"
                },
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "Prefere falar? Diga ao Goianinho \u201cmarca pra mim na sexta à tarde\u201d na aba Assistente.",
            style = MaterialTheme.typography.bodyMedium,
            color = CinzaTexto
        )
    }
}
@Composable
private fun CardProximaConsulta(
    medicoNome: String,
    temProximaConsulta: Boolean,
    tipoEData: String,
    onEntrarNaChamada: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VerdePrincipal)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "PRÓXIMA CONSULTA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Branco.copy(alpha = 0.85f)
            )
            if (!temProximaConsulta) {
                Text(
                    "Nenhuma consulta marcada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Branco
                )
            } else {
                Text(
                    "Dr. $medicoNome",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Branco
                )
                Text(tipoEData, style = MaterialTheme.typography.bodyMedium, color = Branco.copy(alpha = 0.9f))

                Button(
                    onClick = onEntrarNaChamada,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Branco, contentColor = VerdePrincipal)
                ) {
                    Text("\uD83C\uDFA5  Entrar na chamada", fontWeight = FontWeight.Bold)
                }

                Text(
                    "O Goianinho te lembra 1 hora antes pelo WhatsApp.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Branco.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun SeletorDeDias(
    dias: List<DiaAgenda>,
    diaSelecionado: String,
    onDiaClick: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        dias.forEach { dia ->
            val selecionado = dia.data == diaSelecionado
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .background(
                        color = if (selecionado) VerdePrincipal else Branco,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (selecionado) VerdePrincipal else CinzaBorda,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onDiaClick(dia.data) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    dia.diaSemana,
                    color = if (selecionado) Branco else AzulNoite,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    dia.diaMes,
                    color = if (selecionado) Branco else AzulNoite,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun GradeDeHorarios(
    horarios: List<HorarioUi>,
    horarioSelecionadoId: String?,
    onHorarioClick: (HorarioUi) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(112.dp)
    ) {
        items(horarios, key = { it.id }) { horario ->
            val selecionado = horario.id == horarioSelecionadoId
            Column(
                modifier = Modifier
                    .height(52.dp)
                    .background(
                        color = if (selecionado) VerdeClaro else Branco,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (selecionado) VerdePrincipal else CinzaBorda,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = horario.disponivel) { onHorarioClick(horario) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    horario.hora,
                    color = if (horario.disponivel) AzulNoite else CinzaTexto,
                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}