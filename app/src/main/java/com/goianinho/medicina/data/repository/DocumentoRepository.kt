package com.goianinho.medicina.data.repository

import com.goianinho.medicina.data.COL_DOCUMENTOS
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.model.PontoResumo
import com.goianinho.medicina.data.model.StatusDocumento
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class DocumentoRepository(private val db: FirebaseFirestore) {

    private val col = db.collection(COL_DOCUMENTOS)

    // Resumo simulado: sempre 3 pontos com fontes fictícias
    private val resumoSimulado = listOf(
        PontoResumo("Ponto relevante 1 extraído do exame", "pág. 1, linha 9"),
        PontoResumo("Ponto relevante 2 extraído do exame", "pág. 1, linha 12"),
        PontoResumo("Ponto relevante 3 extraído do exame", "pág. 2, linha 3")
    )

    // Lista do paciente, atualizando em tempo real
    fun ouvirDoPaciente(pacienteId: String, limite: Long = 20): Flow<List<Documento>> =
        callbackFlow {
            val registro = col
                .whereEqualTo("pacienteId", pacienteId)
                .orderBy("enviadoEm", Query.Direction.DESCENDING)
                .limit(limite)
                .addSnapshotListener { snapshot, erro ->
                    if (erro != null) {
                        close(erro)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObjects(Documento::class.java).orEmpty())
                }
            awaitClose { registro.remove() }
        }

    // Fila do médico: só documentos dos pacientes dele (RN-07)
    fun ouvirPendentes(medicoId: String): Flow<List<Documento>> = callbackFlow {
        val registro = col
            .whereEqualTo("medicoId", medicoId)
            .whereEqualTo("status", StatusDocumento.AGUARDANDO_MEDICO)
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    // Se faltar índice composto, o link aparece no Logcat
                    close(erro)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Documento::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    suspend fun registrar(doc: Documento): Result<Unit> = runCatching {
        withTimeout(10_000) {
            col.add(
                doc.copy(
                    resumo = resumoSimulado,
                    status = StatusDocumento.AGUARDANDO_MEDICO
                )
            ).await()
        }
        Unit
    }

    suspend fun confirmar(id: String, medicoId: String, observacao: String): Result<Unit> =
        decidir(id, StatusDocumento.CONFIRMADO, medicoId, observacao)

    suspend fun corrigir(id: String, medicoId: String, observacao: String): Result<Unit> {
        if (observacao.isBlank()) {
            return Result.failure(IllegalArgumentException("Observação obrigatória")) // RN-13
        }
        return decidir(id, StatusDocumento.CORRIGIDO, medicoId, observacao)
    }

    private suspend fun decidir(
        id: String,
        status: String,
        medicoId: String,
        observacao: String
    ): Result<Unit> = runCatching {
        withTimeout(10_000) {
            col.document(id).update(
                mapOf(
                    "status" to status,
                    "observacao" to observacao,
                    "confirmadoPor" to medicoId,
                    "confirmadoEm" to FieldValue.serverTimestamp(),
                    "apoioIA" to true // RN-06
                )
            ).await()
        }
        Unit
    }
}