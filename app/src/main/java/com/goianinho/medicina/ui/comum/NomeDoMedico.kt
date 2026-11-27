package com.goianinho.medicina.ui.comum

import java.util.Calendar

// Evita "Dr. Dr. Fulano": só acrescenta o título se o nome ainda não tiver
fun nomeDoMedico(nome: String): String {
    val n = nome.trim()
    if (n.isEmpty()) return "Doutor(a)"
    val tem = n.startsWith("Dr.", true) || n.startsWith("Dra.", true) ||
            n.startsWith("Dr ", true) || n.startsWith("Dra ", true)
    return if (tem) n else "Dr. $n"
}

fun saudacaoDoMomento(hora: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String =
    when (hora) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }