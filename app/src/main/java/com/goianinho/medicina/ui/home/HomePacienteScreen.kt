package com.goianinho.medicina.ui.home

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomePacienteScreen(
    onAbrirChat: () -> Unit,
    onAbrirAgenda: () -> Unit,
    onAbrirDocumentos: () -> Unit,
    onSair: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val medico = state.nomeMedico
    val textoAssistente = if (medico.isNotEmpty()) "Goianinho · assistente do Dr. $medico"
    else "Goianinho · seu assistente"

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

        state.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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
                            "Fale ou escreva para o Goianinho…",
                            modifier = Modifier.weight(1f),
                            color = CinzaTexto
                        )
                        Box(
                            Modifier.size(36.dp).background(VerdePrincipal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_mic),
                                contentDescription = "Falar com o Goianinho",
                                tint = Branco
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipAcao("Marcar consulta", onAbrirAgenda)
                    ChipAcao("Enviar exame", onAbrirDocumentos)
                }
                ChipAcao("Tirar dúvida", onAbrirChat)
            }
        }

        if (!state.temMedico) {
            Text("Você ainda não tem um médico vinculado.", color = CinzaTexto)
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
}

@Composable
private fun ChipAcao(texto: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = VerdePrincipal,
        border = BorderStroke(1.dp, Branco)
    ) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Branco,
            fontWeight = FontWeight.Bold
        )
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
                        if (medico.isNotEmpty()) "Dr. $medico" else "Seu médico",
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
    StatusDocumento.AGUARDANDO_MEDICO -> "Resumo pronto · aguardando o médico"
    StatusDocumento.CONFIRMADO -> "Confirmado pelo médico"
    StatusDocumento.CORRIGIDO -> "Corrigido pelo médico"
    StatusDocumento.ARQUIVADO -> "Arquivado"
    else -> status
}
