package com.goianinho.medicina.ui.home

import androidx.compose.runtime.Composable
import com.goianinho.medicina.ui.comum.TelaEmConstrucao

// PROVISÓRIA — Módulo 1. Substitua o corpo pela tela real descrita no doc.md desta pasta.
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun HomePacienteScreen(
    onAbrirChat: () -> Unit,
    onAbrirAgenda: () -> Unit,
    onAbrirDocumentos: () -> Unit
) {
    TelaEmConstrucao(
        titulo = "Início do paciente",
        modulo = "Módulo 1",
        acoes = listOf("Falar com o Goianinho" to onAbrirChat, "Marcar consulta" to onAbrirAgenda, "Enviar exame" to onAbrirDocumentos)
    )
}
