package com.goianinho.medicina.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Coleção "consultas". Dono: Módulo 4. Lida também pelos Módulos 1 e 2.
data class Consulta(
    @DocumentId val id: String = "",
    val pacienteId: String = "",
    val medicoId: String = "",
    val dataHora: Timestamp? = null,
    val tipo: String = "teleconsulta",
    val status: String = "agendada",    // agendada | realizada | cancelada
    val motivo: String = ""
)
