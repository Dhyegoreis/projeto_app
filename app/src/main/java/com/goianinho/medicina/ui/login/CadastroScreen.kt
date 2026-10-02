package com.goianinho.medicina.ui.login

import androidx.compose.runtime.Composable
import com.goianinho.medicina.ui.comum.TelaEmConstrucao

// PROVISÓRIA — Módulo 1. Substitua o corpo pela tela real descrita no doc.md desta pasta.
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun CadastroScreen(
    onContaCriada: () -> Unit,
    onVoltar: () -> Unit
) {
    TelaEmConstrucao(
        titulo = "Criar conta",
        modulo = "Módulo 1",
        acoes = listOf("Conta criada (simular)" to onContaCriada, "Voltar" to onVoltar)
    )
}
