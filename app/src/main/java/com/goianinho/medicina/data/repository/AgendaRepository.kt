package com.goianinho.medicina.data.repository

import com.goianinho.medicina.data.COL_CONSULTAS
import com.goianinho.medicina.data.COL_HORARIOS
import com.goianinho.medicina.data.model.Consulta
import com.goianinho.medicina.data.model.Horario
import com.goianinho.medicina.ui.comum.HorarioOcupadoException
import com.goianinho.medicina.ui.comum.consultaDoMomento
import com.goianinho.medicina.ui.comum.mensagemDeErro
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Único arquivo do Módulo 4 que fala com o Firebase.
 * Coleções "horarios" e "consultas" — ver doc.md desta pasta e seção 11 da documentação.
 *
 * `db` tem valor padrão só pra não quebrar quem ainda instancia esta classe sem
 * argumento (Módulo 1 e Módulo 2, antes de migrarem) — o AppContainer já passa o `db`
 * de verdade.
 */
class AgendaRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // RF-24: horários livres do médico, num dia específico
    fun ouvirHorariosLivres(medicoId: String, data: String): Flow<List<Horario>> = callbackFlow {
        val registro = db.collection(COL_HORARIOS)
            .whereEqualTo("medicoId", medicoId)
            .whereEqualTo("data", data)
            .whereEqualTo("livre", true)
            .addSnapshotListener { snap, erro ->
                if (erro != null) { close(erro); return@addSnapshotListener }
                trySend(snap?.toObjects(Horario::class.java).orEmpty().sortedBy { it.hora })
            }
        awaitClose { registro.remove() }
    }

    // RF-25 e RN-08: reserva com transação — conflito lança HorarioOcupadoException
    suspend fun reservar(horario: Horario, pacienteId: String, dataHora: Timestamp): Result<Unit> =
        runCatching {
            withTimeout(10_000) {
                val refHorario = db.collection(COL_HORARIOS).document(horario.id)
                val refConsulta = db.collection(COL_CONSULTAS).document()
                db.runTransaction { tx ->
                    if (tx.get(refHorario).getBoolean("livre") != true) throw HorarioOcupadoException()
                    tx.update(refHorario, "livre", false)
                    tx.set(
                        refConsulta,
                        Consulta(pacienteId = pacienteId, medicoId = horario.medicoId, dataHora = dataHora)
                    )
                }.await()
            }
        }

    // RF-27: consultas do médico num intervalo (ex.: o dia de hoje). Status "cancelada"
    // é filtrado por quem consome (ViewModel), não aqui.
    fun ouvirConsultasDoDia(medicoId: String, inicio: Timestamp, fim: Timestamp): Flow<List<Consulta>> =
        callbackFlow {
            val registro = db.collection(COL_CONSULTAS)
                .whereEqualTo("medicoId", medicoId)
                .whereGreaterThanOrEqualTo("dataHora", inicio)
                .whereLessThan("dataHora", fim)
                .orderBy("dataHora")
                .addSnapshotListener { snap, erro ->
                    if (erro != null) { close(erro); return@addSnapshotListener }
                    trySend(snap?.toObjects(Consulta::class.java).orEmpty())
                }
            awaitClose { registro.remove() }
        }

    // RF-26: próxima consulta do paciente — também usada pelo Início (Módulo 1).
    // Usa a mesma regra de "consulta do momento" do Início e do Chat (ui/comum),
    // recalculada a cada minuto — não trava no "agora" de quando o listener nasceu.
    fun ouvirProximaConsulta(pacienteId: String): Flow<Consulta?> {
        val consultas = callbackFlow {
            val registro = db.collection(COL_CONSULTAS)
                .whereEqualTo("pacienteId", pacienteId)
                .orderBy("dataHora")
                .addSnapshotListener { snap, erro ->
                    if (erro != null) { close(erro); return@addSnapshotListener }
                    trySend(snap?.toObjects(Consulta::class.java).orEmpty())
                }
            awaitClose { registro.remove() }
        }
        return combine(consultas, tickerDeUmMinuto()) { lista, _ -> consultaDoMomento(lista) }
    }

    // ---------- Compatibilidade (Módulo 1 e Módulo 2 ainda não migraram pra Flow) ----------
    // Não mexa nestas duas: elas existem só pra não quebrar ChatViewModel.kt e
    // HomeViewModel.kt. Quando esses módulos migrarem, combine com o líder pra remover.

    fun ouvirConsultasDoDia(
        medicoId: String,
        inicio: Timestamp,
        fim: Timestamp,
        onErro: (String) -> Unit = {},
        onMudou: (List<Consulta>) -> Unit
    ): ListenerRegistration {
        val escopo = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        escopo.launch {
            ouvirConsultasDoDia(medicoId, inicio, fim)
                .catch { e -> onErro(mensagemDeErro(e)) }
                .collect { lista -> onMudou(lista) }
        }
        return ListenerRegistration { escopo.cancel() }
    }

    fun ouvirProximaConsulta(
        pacienteId: String,
        onErro: (String) -> Unit = {},
        onMudou: (Consulta?) -> Unit
    ): ListenerRegistration {
        val escopo = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        escopo.launch {
            ouvirProximaConsulta(pacienteId)
                .catch { e -> onErro(mensagemDeErro(e)) }
                .collect { consulta -> onMudou(consulta) }
        }
        return ListenerRegistration { escopo.cancel() }
    }
}

private fun tickerDeUmMinuto(): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(60_000)
    }
}
