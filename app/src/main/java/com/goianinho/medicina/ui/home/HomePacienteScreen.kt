package com.goianinho.medicina.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goianinho.medicina.R
import com.goianinho.medicina.data.model.Consulta
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.model.StatusDocumento
import com.goianinho.medicina.ui.comum.AVISO_VOZ
import com.goianinho.medicina.ui.comum.AvisoErro
import com.goianinho.medicina.ui.comum.nomeDoMedico
import com.goianinho.medicina.ui.theme.AlertaFundo
import com.goianinho.medicina.ui.theme.AlertaTexto
import com.goianinho.medicina.ui.theme.AzulNoite
import com.goianinho.medicina.ui.theme.Branco
import com.goianinho.medicina.ui.theme.CinzaBorda
import com.goianinho.medicina.ui.theme.CinzaTexto
import com.goianinho.medicina.ui.theme.FundoApp
import com.goianinho.medicina.ui.theme.VerdeClaro
import com.goianinho.medicina.ui.theme.VerdeEscuro
import com.goianinho.medicina.ui.theme.VerdePrincipal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomePacienteScreen(
    onAbrirChat: () -> Unit,
    onAbrirAgenda: () -> Unit,
    onAbrirDocumentos: () -> Unit,
    onSair: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val medico = state.nomeMedico
    val textoAssistente = if (medico.isNotEmpty()) "Goianinho · assistente de ${nomeDoMedico(medico)}"   // H-04
    else "Goianinho · seu assistente"

    Box(Modifier.fillMaxSize().background(FundoApp)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(state.dataHoje, style = MaterialTheme.typography.bodyMedium, color = CinzaTexto)
                    Text(
                        "Olá, ${state.nome}",
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

            // Card do Goianinho
            Card(
                onClick = onAbrirChat,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VerdePrincipal),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Em que posso ajudar hoje?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Branco
                    )
                    Text(
                        textoAssistente,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Branco
                    )
                    Surface(
                        onClick = onAbrirChat,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Branco
                    ) {
                        Row(
                            Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Escreva para o Goianinho…",
                                modifier = Modifier.weight(1f),
                                color = CinzaTexto
                            )
                            // RNF-01: botão de voz mostra o aviso "em breve"
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .background(VerdePrincipal, CircleShape)
                                    .clickable {
                                        scope.launch { snackbar.showSnackbar(AVISO_VOZ) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_chat),
                                    contentDescription = "Falar com o Goianinho",
                                    tint = Branco
                                )
                            }
                        }
                    }
                    // H-06: FlowRow quebra linha com fonte grande
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChipAcao("Marcar consulta", onAbrirAgenda)
                        ChipAcao("Enviar exame", onAbrirDocumentos)
                        ChipAcao("Tirar dúvida", onAbrirChat)
                    }
                }
            }

            if (!state.temMedico) {
                Text("Você ainda não tem um médico vinculado. Peça para a clínica vincular sua conta.", color = CinzaTexto)
            }
            CardProximaConsulta(state.proximaConsulta, medico, onAbrirAgenda)

            Text(
                "ACOMPANHAMENTO",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = CinzaTexto
            )
            if (state.documentos.isEmpty()) {
                Text("Nenhum documento enviado ainda", color = CinzaTexto)
            } else {
                state.documentos.forEach { doc ->
                    ItemDocumento(doc, onAbrirDocumentos)
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

@Composable
private fun ChipAcao(texto: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.heightIn(min = 48.dp),   // área de toque mínima
        shape = RoundedCornerShape(20.dp),
        color = VerdePrincipal,
        border = BorderStroke(1.dp, Branco)
    ) {
        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            Text(
                texto,
                color = Branco,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CardProximaConsulta(consulta: Consulta?, medico: String, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Branco),
        border = BorderStroke(1.dp, CinzaBorda),
        shape = RoundedCornerShape(16.dp)
    ) {
        val data = consulta?.dataHora?.toDate()
        if (consulta == null || data == null) {
            Text(
                "Nenhuma consulta marcada",
                modifier = Modifier.padding(16.dp),
                color = CinzaTexto
            )
        } else {
            val locale = Locale("pt", "BR")
            val diaSemana = SimpleDateFormat("EEE", locale).format(data)
                .trimEnd('.').replaceFirstChar { it.titlecase(locale) }
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier.background(VerdeClaro, RoundedCornerShape(12.dp)).padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(diaSemana, color = VerdeEscuro, fontWeight = FontWeight.Bold)
                    Text(
                        SimpleDateFormat("dd", locale).format(data),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VerdeEscuro
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        "PRÓXIMA CONSULTA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinzaTexto
                    )
                    Text(
                        if (medico.isNotEmpty()) nomeDoMedico(medico) else "Seu médico",   // H-04
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AzulNoite
                    )
                    Text(
                        "Teleconsulta · ${SimpleDateFormat("HH:mm", locale).format(data)}",
                        color = CinzaTexto
                    )
                }
                Text("Ver", color = VerdePrincipal, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ItemDocumento(doc: Documento, onClick: () -> Unit) {
    val pendente = doc.status == StatusDocumento.AGUARDANDO_MEDICO
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
            Box(
                Modifier
                    .size(40.dp)
                    .background(if (pendente) AlertaFundo else VerdeClaro, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(if (pendente) R.drawable.ic_documento else R.drawable.ic_revisao),
                    contentDescription = null,
                    tint = if (pendente) AlertaTexto else VerdeEscuro
                )
            }
            Column {
                Text(
                    doc.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzulNoite
                )
                Text(rotuloDoStatus(doc.status), color = CinzaTexto)
            }
        }
    }
}

private fun rotuloDoStatus(status: String): String = when (status) {
    StatusDocumento.AGUARDANDO_MEDICO -> "Aguardando revisão do médico"
    StatusDocumento.CONFIRMADO -> "Confirmado pelo médico"
    StatusDocumento.CORRIGIDO -> "Corrigido pelo médico"
    StatusDocumento.ARQUIVADO -> "Arquivado"
    else -> status
}