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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    var mostrarAssumir by rememberSaveable { mutableStateOf(false) }

    if (mostrarAssumir) {
        val assumirVm: AssumirPacienteViewModel = viewModel()
        val assumir by assumirVm.uiState.collectAsState()
        LaunchedEffect(mostrarAssumir) { if (mostrarAssumir) assumirVm.carregar() }
        val fechar = {
            assumirVm.limpar()
            mostrarAssumir = false
        }
        AlertDialog(
            onDismissRequest = fechar,
            title = { Text("Assumir paciente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Toque em um paciente para passar a acompanhá-lo.")
                    if (assumir.carregando) {
                        CircularProgressIndicator(color = VerdePrincipal)
                    } else if (assumir.pacientes.isEmpty()) {
                        Text("Nenhum paciente cadastrado.", color = CinzaTexto)
                    } else {
                        LazyColumn(
                            Modifier.heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(assumir.pacientes, key = { it.uid }) { paciente ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            paciente.nome.ifEmpty { paciente.email },
                                            fontWeight = FontWeight.Bold,
                                            color = AzulNoite
                                        )
                                        Text(
                                            paciente.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CinzaTexto
                                        )
                                    }
                                    when {
                                        paciente.medicoId.isBlank() -> TextButton(
                                            onClick = { assumirVm.assumir(paciente) },
                                            enabled = assumir.assumindoUid == null
                                        ) {
                                            if (assumir.assumindoUid == paciente.uid) {
                                                CircularProgressIndicator(
                                                    Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = VerdePrincipal
                                                )
                                            } else {
                                                Text("Assumir", color = VerdePrincipal)
                                            }
                                        }
                                        paciente.medicoId == assumir.medicoUid -> Text(
                                            "Seu paciente",
                                            color = VerdePrincipal,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        else -> Text(
                                            "Com outro médico",
                                            color = CinzaTexto,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                    assumir.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    assumir.mensagem?.let { Text(it, color = VerdePrincipal) }
                }
            },
            confirmButton = { TextButton(onClick = fechar) { Text("Fechar") } }
        )
    }

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

        state.bloqueio?.let { AvisoErro(it) }

        // Contadores
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador(state.consultasHoje.size, "consultas hoje", Branco, AzulNoite, Modifier.weight(1f))
            Contador(state.pendentes.size, "para revisar", AlertaFundo, AlertaTexto, Modifier.weight(1f))
            Contador(state.semResposta, "sem resposta", Branco, AzulNoite, Modifier.weight(1f))
        }

        // Provisório (demo): o médico assume um paciente (a escrita fica em outro ViewModel, o Início só lê)
        OutlinedButton(
            onClick = { mostrarAssumir = true },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, CinzaBorda),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulNoite)
        ) { Text("+ Assumir paciente (provisório)") }

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
                    Text(
                        if (state.consultasHoje.isNotEmpty()) "Nenhuma consulta restante hoje." else "Nenhuma consulta marcada",
                        color = Branco,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    val paciente = state.nomesPacientes[proxima.pacienteId].orEmpty().ifEmpty { "Paciente" }
                    Text(
                        "${state.rotuloProximaDoDia} · ${hora.format(data)}",
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
