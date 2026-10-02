package com.goianinho.medicina.data.model

import com.google.firebase.firestore.DocumentId

// Coleção "horarios". Dono: Módulo 4.
data class Horario(
    @DocumentId val id: String = "",
    val medicoId: String = "",
    val data: String = "",       // "2026-09-24"
    val hora: String = "",       // "09:30"
    val livre: Boolean = true
)
