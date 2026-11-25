package com.goianinho.medicina.ui.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goianinho.medicina.ui.comum.AVISO_VIDEOCHAMADA
import com.goianinho.medicina.ui.theme.AzulNoite
import com.goianinho.medicina.ui.theme.CinzaTexto
import com.goianinho.medicina.ui.theme.VerdePrincipal

// Módulo 4. Tela simulada da videochamada (RF-30, RNF-01).
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun ChamadaEmBreveScreen(
    onVoltar: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("\uD83C\uDFA5", fontSize = 48.sp)
        Text(
            "Chamada em breve",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AzulNoite,
            textAlign = TextAlign.Center
        )
        Text(
            AVISO_VIDEOCHAMADA,
            style = MaterialTheme.typography.bodyMedium,
            color = CinzaTexto,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onVoltar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
        ) {
            Text("Voltar")
        }
    }
}