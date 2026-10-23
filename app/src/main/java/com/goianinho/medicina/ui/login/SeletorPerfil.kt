package com.goianinho.medicina.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun SeletorPerfil(selecionado: String, onSelecionar: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0EAE7), RoundedCornerShape(20.dp))
            .padding(6.dp)
    ) {
        listOf("paciente" to "Paciente", "medico" to "Médico").forEach { (valor, rotulo) ->
            val ativo = valor == selecionado
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)   // P-05: área de toque mínima
                    .background(if (ativo) Color.White else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onSelecionar(valor) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rotulo,
                    fontWeight = FontWeight.Bold,
                    color = if (ativo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}