package com.goianinho.medicina.ui.documentos

import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.ui.comum.AvisoErro
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RevisaoScreen() {
    val viewModel: RevisaoViewModel = viewModel(factory = RevisaoViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // D-09: "Resumo confirmado." aparece num Snackbar e some sozinho
    LaunchedEffect(state.mensagem) {
        state.mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    // D-10: um Box e um único LazyColumn (sem Scaffold interno)
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Revisão de resumos",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    state.bloqueio?.let {
                        Spacer(Modifier.height(8.dp))
                        AvisoErro(it)
                    }

                    state.erro?.let {
                        Spacer(Modifier.height(8.dp))
                        AvisoErro(it, onSumir = { viewModel.limparErro() })
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Confira os resumos pendentes antes de confirmar.")
                }
            }

            when {
                state.carregando -> item { CircularProgressIndicator() }

                state.documentos.isEmpty() -> item {
                    Text("Nenhum resumo aguardando revisão")
                }

                else -> items(items = state.documentos, key = { it.id }) { documento ->
                    val observacao = state.observacoes[documento.id].orEmpty()
                    RevisaoCard(
                        documento = documento,
                        pacienteNome = state.nomesPacientes[documento.pacienteId].orEmpty(),
                        observacao = observacao,
                        salvando = state.salvando,
                        onObservacaoChange = { viewModel.atualizarObservacao(documento.id, it) },
                        onConfirmar = { viewModel.confirmar(documento, observacao) },
                        onCorrigir = { viewModel.corrigir(documento, observacao) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun RevisaoCard(
    documento: Documento,
    pacienteNome: String,
    observacao: String,
    salvando: Boolean,
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

            Text("Paciente: ${pacienteNome.ifEmpty { "…" }}")
            documento.enviadoEm?.toDate()?.let { data ->
                Text(
                    "Enviado em ${SimpleDateFormat("dd/MM 'às' HH:mm", Locale("pt", "BR")).format(data)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text("Status: aguardando médico")

            // D-06 / RF-23: o médico vê a foto (ou os dados, se não houver foto)
            FotoDoDocumento(documento)

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
                label = { Text("Observação (obrigatória para corrigir)") },   // D-01
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = onConfirmar,
                enabled = !salvando,                                          // D-02
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar resumo")
            }

            OutlinedButton(
                onClick = onCorrigir,
                enabled = !salvando && observacao.isNotBlank(),               // D-01 / RN-13
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Corrigir resumo")
            }

            if (salvando) {
                CircularProgressIndicator()
            }

            Text(
                "A decisão será registrada no Firestore.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun FotoDoDocumento(documento: Documento) {
    if (documento.imagemBase64.isBlank()) {
        // Sem foto: mostra os dados do documento
        Text("${documento.nome} · ${documento.tipo.uppercase()}")
        return
    }
    val bytes = remember(documento.id) {
        Base64.decode(documento.imagemBase64, Base64.DEFAULT)
    }
    val cinza = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = bytes,                                  // Coil aceita ByteArray
        contentDescription = "Foto do documento ${documento.nome}",
        placeholder = cinza,                            // carregando
        error = cinza,                                  // falhou
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 480.dp)
    )
}