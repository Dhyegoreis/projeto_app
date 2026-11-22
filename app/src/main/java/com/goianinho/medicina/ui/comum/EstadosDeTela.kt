package com.goianinho.medicina.ui.comum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goianinho.medicina.ui.theme.GoianinhoMedicinaTheme

/** Indicador de carregamento centralizado. */
@Composable
fun TelaCarregando(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

/** Mensagem de erro com botão "Tentar de novo". */
@Composable
fun TelaErro(
    mensagem: String,
    onTentarDeNovo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            mensagem,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Button(onClick = onTentarDeNovo, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("Tentar de novo")
        }
    }
}

/** Texto explicativo centralizado para listas sem itens. */
@Composable
fun EstadoVazio(texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TelaCarregandoPreview() {
    GoianinhoMedicinaTheme { TelaCarregando() }
}

@Preview(showBackground = true)
@Composable
private fun TelaErroPreview() {
    GoianinhoMedicinaTheme {
        TelaErro(mensagem = "Não foi possível carregar. Verifique a conexão.", onTentarDeNovo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EstadoVazioPreview() {
    GoianinhoMedicinaTheme { EstadoVazio(texto = "Nenhum item por aqui ainda.") }
}
