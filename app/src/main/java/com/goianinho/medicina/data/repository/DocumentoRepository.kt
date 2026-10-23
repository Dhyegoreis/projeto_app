
package com.goianinho.medicina.data.repository

import android.util.Log
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.model.PontoResumo
import com.goianinho.medicina.data.model.StatusDocumento
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class DocumentoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("documentos")

    fun registrar(
        pacienteId: String,
        medicoId: String,
        nome: String,
        tipo: String,
        base64: String = "",
        onResultado: (Boolean) -> Unit = {}
    ) {
        val resumoFixo = listOf(
            PontoResumo(
                "Ponto relevante 1 extraído do exame",
                "pág. 1, linha 9"
            ),
            PontoResumo(
                "Ponto relevante 2 extraído do exame",
                "pág. 1, linha 12"
            ),
            PontoResumo(
                "Ponto relevante 3 extraído do exame",
                "pág. 2, linha 3"
            )
        )

        val documento = Documento(
            pacienteId = pacienteId,
            medicoId = medicoId,
            nome = nome,
            tipo = tipo,
            imagemBase64 = base64,
            resumo = resumoFixo,
            status = StatusDocumento.AGUARDANDO_MEDICO
        )

        col.add(documento)
            .addOnSuccessListener { onResultado(true) }
            .addOnFailureListener { onResultado(false) }
    }

    fun ouvirPendentes(
        medicoId: String,
        onMudou: (List<Documento>) -> Unit
    ): ListenerRegistration {
        return col
            .whereEqualTo("medicoId", medicoId)
            .whereEqualTo("status", StatusDocumento.AGUARDANDO_MEDICO)
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    // Ex.: índice composto ainda não criado; o link aparece no Logcat
                    Log.e("Goianinho", "ouvirPendentes falhou", erro)
                    onMudou(emptyList())
                } else {
                    onMudou(
                        snapshot?.toObjects(Documento::class.java)
                            ?: emptyList()
                    )
                }
            }
    }

    fun ouvirDoPaciente(
        pacienteId: String,
        limite: Long = 20,
        onMudou: (List<Documento>) -> Unit
    ): ListenerRegistration {
        return col
            .whereEqualTo("pacienteId", pacienteId)
            .orderBy("enviadoEm", Query.Direction.DESCENDING)
            .limit(limite)
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    // Ex.: índice composto ainda não criado; o link aparece no Logcat
                    Log.e("Goianinho", "ouvirDoPaciente falhou", erro)
                    onMudou(emptyList())
                } else {
                    onMudou(
                        snapshot?.toObjects(Documento::class.java)
                            ?: emptyList()
                    )
                }
            }
    }

    fun confirmar(
        docId: String,
        medicoId: String,
        observacao: String,
        onResultado: (Boolean) -> Unit = {}
    ) {
        atualizarStatus(
            docId,
            StatusDocumento.CONFIRMADO,
            medicoId,
            observacao,
            onResultado
        )
    }

    fun corrigir(
        docId: String,
        medicoId: String,
        observacao: String,
        onResultado: (Boolean) -> Unit = {}
    ) {
        atualizarStatus(
            docId,
            StatusDocumento.CORRIGIDO,
            medicoId,
            observacao,
            onResultado
        )
    }

    private fun atualizarStatus(
        docId: String,
        status: String,
        medicoId: String,
        observacao: String,
        onResultado: (Boolean) -> Unit
    ) {
        col.document(docId).update(
            mapOf(
                "status" to status,
                "observacao" to observacao,
                "confirmadoPor" to medicoId,
                "confirmadoEm" to FieldValue.serverTimestamp(),
                "apoioIA" to true
            )
        )
            .addOnSuccessListener { onResultado(true) }
            .addOnFailureListener { onResultado(false) }
    }
}
