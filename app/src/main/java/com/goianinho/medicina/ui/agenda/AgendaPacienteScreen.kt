package com.goianinho.medicina.ui.agenda

import androidx.compose.runtime.Composable
import com.goianinho.medicina.ui.comum.TelaEmConstrucao

// PROVISÓRIA — Módulo 4. Substitua o corpo pela tela real descrita no doc.md desta pasta.
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun AgendaPacienteScreen(
    onEntrarNaChamada: () -> Unit
) {
    TelaEmConstrucao(
        titulo = "Agenda",
        modulo = "Módulo 4",
        acoes = listOf("Entrar na chamada" to onEntrarNaChamada)
    )
}
