package com.goianinho.medicina.data.model

// Coleção "usuarios" — id do documento = uid do Firebase Auth. Dono: Módulo 1.
data class Usuario(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val perfil: String = "paciente",   // "paciente" ou "medico"
    val medicoId: String = "",         // só para paciente: o médico vinculado
    val canalPreferido: String = ""    // "whatsapp", "app", "ligacao"
)
