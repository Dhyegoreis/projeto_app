package com.goianinho.medicina.ui.documentos

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.model.StatusDocumento
import com.goianinho.medicina.ui.comum.AvisoErro

@Composable
fun DocumentosScreen() {
    val viewModel: DocumentosViewModel = viewModel(factory = DocumentosViewModel.Factory)
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // D-09: mensagem de sucesso aparece num Snackbar e é limpa depois
    LaunchedEffect(state.mensagem) {
        state.mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    val escolherArquivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val nome = nomeDoArquivo(context, uri)
            val tipo = if (
                context.contentResolver.getType(uri) == "application/pdf"
            ) "pdf" else "imagem"

            viewModel.registrarDocumento(nome = nome, tipo = tipo)
        }
    }

    val tirarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // D-04: reduz para 1280 px e recusa acima de 700 KB
            val base64 = fotoParaBase64(bitmap)
            if (base64 == null) {
                viewModel.avisarFotoGrande()
            } else {
                viewModel.registrarDocumento(
                    nome = "Foto do documento",
                    tipo = "imagem",
                    base64 = base64
                )
            }
        }
    }

    val podeAgir = state.bloqueio == null && !state.salvando   // D-02

    // D-10: um Box e um único LazyColumn (sem Scaffold interno)
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Meus documentos",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    state.bloqueio?.let { bloqueio ->
                        Spacer(Modifier.height(8.dp))
                        AvisoErro(bloqueio)
                    }

                    state.erro?.let { erro ->
                        Spacer(Modifier.height(8.dp))
                        AvisoErro(erro, onSumir = { viewModel.limparErro() })
                    }

                    Spacer(Modifier.height(8.dp))

                    Text("Selecione um exame para registrar e acompanhar a revisão médica.")

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            escolherArquivo.launch(arrayOf("application/pdf", "image/*"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = podeAgir
                    ) {
                        Text("Enviar arquivo PDF ou imagem")
                    }

                    OutlinedButton(
                        onClick = {
                            // D-03: aparelho sem app de câmera não fecha o app
                            try {
                                tirarFoto.launch(null)
                            } catch (e: ActivityNotFoundException) {
                                viewModel.avisar("Não encontramos a câmera neste aparelho.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = podeAgir
                    ) {
                        Text("Tirar foto")
                    }

                    if (state.salvando) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator()
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Documentos anteriores",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            if (state.carregando) {
                item { CircularProgressIndicator() }
            } else if (state.documentos.isEmpty()) {
                item { Text("Você ainda não enviou documentos") }
            } else {
                items(items = state.documentos, key = { it.id }) { documento ->
                    DocumentoCard(documento, state.nomeMedico)
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
private fun DocumentoCard(documento: Documento, nomeMedico: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = documento.nome,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Tipo: ${documento.tipo.uppercase()}")
            Text("Status: ${rotuloDoStatus(documento.status)}")

            Spacer(Modifier.height(12.dp))

            Text(
                "Resumo do Goianinho",
                style = MaterialTheme.typography.titleSmall
            )

            // RN-05: o resumo só vale para o paciente depois da confirmação do médico
            val liberado = documento.status == StatusDocumento.CONFIRMADO ||
                documento.status == StatusDocumento.CORRIGIDO

            if (!liberado) {
                Text(
                    when (documento.status) {
                        StatusDocumento.ARQUIVADO -> "Este documento foi arquivado."
                        // D-07: texto da v1.6 com o nome do médico
                        else -> if (nomeMedico.isBlank()) {
                            "Aguardando revisão do seu médico"
                        } else {
                            "Aguardando revisão do Dr. $nomeMedico"
                        }
                    }
                )
            } else {
                val temObservacao = documento.observacao.isNotBlank()
                if (documento.status == StatusDocumento.CORRIGIDO) {
                    Text(
                        if (temObservacao) "O médico corrigiu este resumo. Leia a observação."
                        else "O médico corrigiu este resumo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (documento.resumo.isEmpty()) {
                    Text("Resumo ainda não disponível.")
                } else {
                    documento.resumo.forEach { ponto ->
                        Spacer(Modifier.height(8.dp))
                        Text("• ${ponto.texto}")
                        Text(
                            "Fonte: ${ponto.fonte}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (temObservacao) {
                    Spacer(Modifier.height(8.dp))
                    Text("Observação do médico: ${documento.observacao}")
                }
            }
        }
    }
}

private fun nomeDoArquivo(context: Context, uri: Uri): String {
    return context.contentResolver.query(
        uri, null, null, null, null
    )?.use { cursor ->
        val coluna = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && coluna >= 0) {
            cursor.getString(coluna)
        } else {
            null
        }
    } ?: "documento"
}

private fun rotuloDoStatus(status: String): String = when (status) {
    StatusDocumento.AGUARDANDO_MEDICO -> "Aguardando médico"
    StatusDocumento.CONFIRMADO -> "Confirmado pelo médico"
    StatusDocumento.CORRIGIDO -> "Corrigido pelo médico"
    StatusDocumento.ARQUIVADO -> "Arquivado"
    else -> status.replace('_', ' ')
}