package com.goianinho.medicina.ui.chat

import androidx.compose.runtime.Composable
import com.goianinho.medicina.ui.comum.TelaEmConstrucao

// PROVISÓRIA — Módulo 2. Substitua o corpo pela tela real descrita no doc.md desta pasta.
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun ChatMedicoScreen(
    onAbrirRevisao: () -> Unit
) {
    TelaEmConstrucao(
        titulo = "Goianinho do médico",
        modulo = "Módulo 2",
        acoes = listOf("Abrir revisão (fonte)" to onAbrirRevisao)
    )
}
