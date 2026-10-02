package com.goianinho.medicina.ui.login

import androidx.compose.runtime.Composable
import com.goianinho.medicina.ui.comum.TelaEmConstrucao

// PROVISÓRIA — Módulo 1. Substitua o corpo pela tela real descrita no doc.md desta pasta.
// Mantenha os parâmetros: é por eles que a navegação liga esta tela às outras.
@Composable
fun LoginScreen(
    onEntrouComoPaciente: () -> Unit,
    onEntrouComoMedico: () -> Unit,
    onCriarConta: () -> Unit
) {
    TelaEmConstrucao(
        titulo = "Login",
        modulo = "Módulo 1",
        acoes = listOf("Entrar como paciente" to onEntrouComoPaciente, "Entrar como médico" to onEntrouComoMedico, "Criar conta" to onCriarConta)
    )
}
