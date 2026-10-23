package com.goianinho.medicina.ui.comum

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val TEMPO_AVISO_MS = 5_000L

/**
 * Aviso de erro visível, de largura total, para o topo das telas.
 * Explica o motivo da falha (texto vindo do ViewModel). Sem ícone: o projeto não tem
 * ícone de alerta em res/drawable.
 *
 * Sem [onSumir], o aviso fica fixo na tela (bloqueio: problema que continua valendo, como
 * sessão expirada, médico não vinculado ou erro de leitura de uma lista). Com [onSumir], some
 * sozinho após 5 s (erro de ação do usuário): ele é chamado e o ViewModel deve limpar o erro
 * para a tela parar de exibir o aviso. Um novo texto reinicia a contagem.
 */
@Composable
fun AvisoErro(texto: String, modifier: Modifier = Modifier, onSumir: () -> Unit = {}) {
    val aoSumir by rememberUpdatedState(onSumir)
    LaunchedEffect(texto) {
        delay(TEMPO_AVISO_MS)
        aoSumir()
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}
