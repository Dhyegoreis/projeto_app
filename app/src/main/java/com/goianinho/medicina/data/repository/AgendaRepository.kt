package com.goianinho.medicina.data.repository

import com.goianinho.medicina.data.model.Consulta
import com.goianinho.medicina.data.model.Horario
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

/**
 * Único arquivo do Módulo 4 que fala com o Firebase.
 * Coleções "horarios" e "consultas" — ver doc.md desta pasta e seção 10 da documentação.
 */
class AgendaRepository {

    private val db = Firebase.firestore

    // RF-24: horários livres do médico, num dia específico
    fun ouvirHorariosLivres(
        medicoId: String,
        data: String,
        onMudou: (List<Horario>) -> Unit
    ): ListenerRegistration =
        db.collection("horarios")
            .whereEqualTo("medicoId", medicoId)
            .whereEqualTo("data", data)
            .whereEqualTo("livre", true)
            .addSnapshotListener { snap, erro ->
                if (erro != null) return@addSnapshotListener
                onMudou(snap?.toObjects(Horario::class.java).orEmpty().sortedBy { it.hora })
            }

    // RF-25 e RN-08: reserva com transação — só um paciente consegue o horário
    fun reservar(
        horario: Horario,
        pacienteId: String,
        dataHora: Timestamp,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) {
        val refHorario = db.collection("horarios").document(horario.id)
        val refConsulta = db.collection("consultas").document()

        db.runTransaction { tx ->
            val atual = tx.get(refHorario)
            if (atual.getBoolean("livre") != true) {
                throw FirebaseFirestoreException(
                    "Horário já reservado",
                    FirebaseFirestoreException.Code.ABORTED
                )
            }
            tx.update(refHorario, "livre", false)
            tx.set(
                refConsulta,
                Consulta(
                    pacienteId = pacienteId,
                    medicoId = horario.medicoId,
                    dataHora = dataHora
                )
            )
        }
            .addOnSuccessListener { onSucesso() }
            .addOnFailureListener { onErro("Esse horário acabou de ser reservado. Escolha outro.") }
    }

    // RF-27 e RF-28: consultas do dia do médico, em ordem de horário
    fun ouvirConsultasDoDia(
        medicoId: String,
        inicio: Timestamp,
        fim: Timestamp,
        onMudou: (List<Consulta>) -> Unit
    ): ListenerRegistration =
        db.collection("consultas")
            .whereEqualTo("medicoId", medicoId)
            .whereGreaterThanOrEqualTo("dataHora", inicio)
            .whereLessThan("dataHora", fim)
            .orderBy("dataHora")
            .addSnapshotListener { snap, erro ->
                if (erro != null) return@addSnapshotListener
                onMudou(snap?.toObjects(Consulta::class.java).orEmpty())
            }

    // RF-26: próxima consulta do paciente — também usada pelo Início (Módulo 1)
    fun ouvirProximaConsulta(
        pacienteId: String,
        onMudou: (Consulta?) -> Unit
    ): ListenerRegistration =
        db.collection("consultas")
            .whereEqualTo("pacienteId", pacienteId)
            .whereGreaterThanOrEqualTo("dataHora", Timestamp.now())
            .orderBy("dataHora")
            .limit(1)
            .addSnapshotListener { snap, erro ->
                if (erro != null) return@addSnapshotListener
                onMudou(snap?.toObjects(Consulta::class.java)?.firstOrNull())
            }
}