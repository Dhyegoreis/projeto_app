package com.goianinho.medicina.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// Coleção "mensagens". Dono: Módulo 2.
data class Mensagem(
    @DocumentId val id: String = "",
    val pacienteId: String = "",
    val medicoId: String = "",
    val autor: String = "usuario",     // "usuario" ou "goianinho"
    val texto: String = "",
    val fonte: String = "",            // vazio = sem card de fonte
    @ServerTimestamp val criadaEm: Timestamp? = null
)
