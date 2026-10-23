package com.goianinho.medicina.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.goianinho.medicina.R
import com.goianinho.medicina.data.model.Mensagem
import com.goianinho.medicina.ui.theme.Branco
import com.goianinho.medicina.ui.theme.CinzaBorda
import com.goianinho.medicina.ui.theme.VerdeEscuro
import com.goianinho.medicina.ui.theme.VerdePrincipal

// Lista + chips + campo de texto, usados pelas duas telas do Chat
@Composable
fun ChatConteudo(
    mensagens: List<Mensagem>,
    chips: List<String>,
    onChip: (String) -> Unit,
    onEnviar: (String) -> Unit,
    onFonteClick: ((String) -> Unit)?,
    placeholder: String,
    corEnviar: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var texto by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Rola para a última mensagem sempre que a lista muda
    LaunchedEffect(mensagens) {
        if (mensagens.isNotEmpty()) listState.animateScrollToItem(mensagens.lastIndex)
    }

    Column(modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(mensagens, key = { i, m -> m.id.ifEmpty { "local-$i" } }) { _, msg ->
                BolhaMensagem(msg, onFonteClick)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips) { chip ->
                SuggestionChip(
                    onClick = { onChip(chip) },
                    enabled = enabled,
                    label = { Text(chip, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(20.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Branco,
                        labelColor = VerdeEscuro
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = CinzaBorda
                    )
                )
            }
        }

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text(placeholder) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(
                onClick = {
                    if (texto.isNotBlank()) {
                        onEnviar(texto)
                        texto = ""
                    }
                },
                enabled = enabled && texto.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = corEnviar)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = "Enviar mensagem"
                )
            }
        }
    }
}
