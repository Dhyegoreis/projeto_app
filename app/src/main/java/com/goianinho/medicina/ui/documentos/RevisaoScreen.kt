
package com.goianinho.medicina.ui.documentos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goianinho.medicina.data.model.Documento

private const val MEDICO_DEMO_REVISAO = "medico_demo"

@Composable
fun RevisaoScreen() {
    val viewModel: RevisaoViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val observacoes = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(Unit) {
        viewModel.carregar(MEDICO_DEMO_REVISAO)
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Revisão de resumos",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))
            Text("Confira os resumos pendentes antes de confirmar.")

            state.mensagem?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.carregando -> CircularProgressIndicator()

                state.documentos.isEmpty() -> {
                    Text("Não há documentos aguardando revisão.")
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.documentos,
                            key = { it.id }
                        ) { documento ->
                            RevisaoCard(
                                documento = documento,
                                observacao = observacoes[documento.id].orEmpty(),
                                onObservacaoChange = {
                                    observacoes[documento.id] = it
                                },
                                onConfirmar = {
                                    viewModel.confirmar(
                                        documento,
                                        MEDICO_DEMO_REVISAO,
                                        observacoes[documento.id].orEmpty()
                                    )
                                },
                                onCorrigir = {
                                    viewModel.corrigir(
                                        documento,
                                        MEDICO_DEMO_REVISAO,
                                        observacoes[documento.id].orEmpty()
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RevisaoCard(
    documento: Documento,
    observacao: String,
    onObservacaoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCorrigir: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                documento.nome,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Status: aguardando médico")

            Text(
                "Resumo do Goianinho",
                style = MaterialTheme.typography.titleSmall
            )

            documento.resumo.forEach { ponto ->
                Text("• ${ponto.texto}")
                Text(
                    "Fonte: ${ponto.fonte}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = observacao,
                onValueChange = onObservacaoChange,
                label = { Text("Observação do médico (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = onConfirmar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar resumo")
            }

            OutlinedButton(
                onClick = onCorrigir,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Corrigir resumo")
            }

            Text(
                "A decisão será registrada no Firestore.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
