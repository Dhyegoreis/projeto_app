package com.goianinho.medicina.ui.agenda

import androidx.compose.runtime.Composable
import com.goianinho.medicina.ui.comum.TelaEmConstrucao

// PROVISÓRIA — Módulo 4. Substitua o corpo pela tela real descrita no doc.md desta pasta.
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun ChamadaEmBreveScreen(
    onVoltar: () -> Unit
) {
    TelaEmConstrucao(
        titulo = "Chamada em breve",
        modulo = "Módulo 4",
        acoes = listOf("Voltar" to onVoltar)
    )
}
