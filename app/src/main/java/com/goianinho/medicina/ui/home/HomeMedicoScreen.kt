package com.goianinho.medicina.ui.home

import androidx.compose.runtime.Composable
import com.goianinho.medicina.ui.comum.TelaEmConstrucao

// PROVISÓRIA — Módulo 1. Substitua o corpo pela tela real descrita no doc.md desta pasta.
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun HomeMedicoScreen(
    onAbrirChat: () -> Unit,
    onAbrirRevisoes: () -> Unit,
    onAbrirAgenda: () -> Unit
) {
    TelaEmConstrucao(
        titulo = "Início do médico",
        modulo = "Módulo 1",
        acoes = listOf("Pergunte ao Goianinho" to onAbrirChat, "Revisões pendentes" to onAbrirRevisoes, "Agenda do dia" to onAbrirAgenda)
    )
}
