package com.goianinho.medicina.ui.comum

import com.goianinho.medicina.data.model.Consulta
import java.util.Date

// Duração de uma teleconsulta, em minutos
const val DURACAO_CONSULTA_MIN = 30

private const val MILIS_POR_MINUTO = 60_000L

/**
 * Consulta "atual/próxima" do dia: a primeira, em ordem de dataHora, que não foi cancelada
 * e ainda não terminou (dataHora + 30 min >= agora). Regra única da Agenda, do Início e do
 * Chat do médico. Devolve null se não restar nenhuma.
 */
fun consultaDoMomento(consultas: List<Consulta>, agora: Date = Date()): Consulta? =
    consultas
        .filter { it.status != "cancelada" && it.dataHora != null }
        .sortedBy { it.dataHora?.seconds ?: Long.MAX_VALUE }
        .firstOrNull { c ->
            val inicio = c.dataHora?.toDate()?.time ?: return@firstOrNull false
            inicio + DURACAO_CONSULTA_MIN * MILIS_POR_MINUTO >= agora.time
        }

// "AGORA" se a consulta já começou; "PRÓXIMA" se ainda não
fun rotuloDaConsulta(consulta: Consulta, agora: Date = Date()): String {
    val inicio = consulta.dataHora?.toDate()?.time ?: return "PRÓXIMA"
    return if (inicio <= agora.time) "AGORA" else "PRÓXIMA"
}
