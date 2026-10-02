package com.goianinho.medicina.ui.comum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Tela provisória usada pelo esqueleto. Cada integrante SUBSTITUI a sua tela
 * provisória pela tela real (ver o doc.md do módulo). Não altere este arquivo.
 */
@Composable
fun TelaEmConstrucao(
    titulo: String,
    modulo: String,
    acoes: List<Pair<String, () -> Unit>> = emptyList()
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            "$modulo · em construção",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        acoes.forEachIndexed { i, (rotulo, acao) ->
            if (i == 0) Button(onClick = acao, modifier = Modifier.fillMaxWidth()) { Text(rotulo) }
            else OutlinedButton(onClick = acao, modifier = Modifier.fillMaxWidth()) { Text(rotulo) }
        }
    }
}
