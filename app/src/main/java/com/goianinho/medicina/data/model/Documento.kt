package com.goianinho.medicina.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// Coleção "documentos". Dono: Módulo 3. Lido também pelos Módulos 1 e 4.
data class PontoResumo(
    val texto: String = "",
    val fonte: String = ""
)

data class Documento(
    @DocumentId val id: String = "",
    val pacienteId: String = "",
    val medicoId: String = "",
    val nome: String = "",
    val tipo: String = "",                     // "pdf" ou "imagem"
    @ServerTimestamp val enviadoEm: Timestamp? = null,
    val imagemBase64: String = "",             // opcional, < 1 MB
    val resumo: List<PontoResumo> = emptyList(),
    val status: String = StatusDocumento.AGUARDANDO_MEDICO,
    val observacao: String = "",
    val confirmadoPor: String = "",
    val confirmadoEm: Timestamp? = null,
    val apoioIA: Boolean = false
)

// Use SEMPRE estas constantes: o Início e a Agenda filtram por elas.
object StatusDocumento {
    const val AGUARDANDO_MEDICO = "aguardando_medico"
    const val CONFIRMADO = "confirmado"
    const val CORRIGIDO = "corrigido"
    const val ARQUIVADO = "arquivado"
}
