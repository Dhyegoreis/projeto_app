package com.goianinho.medicina.data.repository

// A "IA" simulada do Goianinho (RN-03 e RN-04): lista fixa "gatilho -> resposta".
// No futuro, a IA real entra só aqui dentro.
object RespostasSimuladas {

    data class Resposta(val texto: String, val fonte: String = "")

    // Contador simulado de pacientes sem resposta (spec §10)
    const val PACIENTES_SEM_RESPOSTA = 1

    // Cada gatilho tem uma lista de variações; vale a primeira lista que combinar.
    private val gatilhosPaciente: List<Pair<List<String>, Resposta>> = listOf(
        listOf("exame") to Resposta(
            "Recebi seu exame e organizei os pontos principais num resumo. " +
                "Enviei para o seu médico revisar — só ele confirma o resultado.",
            fonte = "Hemograma_set2026.pdf · pág. 1, linhas 8–14"
        ),
        listOf("consulta") to Resposta(
            "O médico tem horários na quinta às 09:30 ou 14:00. Quer que eu reserve?"
        ),
        listOf("horário", "horario") to Resposta(
            "Posso ver os horários livres na aba Agenda ou reservar por aqui."
        )
    )

    private val respostaPadraoPaciente = Resposta(
        "Entendi. Vou organizar isso e te aviso. Qualquer decisão clínica fica com o seu médico."
    )

    private val gatilhosMedico: List<Pair<List<String>, Resposta>> = listOf(
        listOf("próximo paciente", "proximo paciente") to Resposta(
            "Seu próximo paciente está na Agenda do dia. Deixei o briefing pronto — a avaliação é sua.",
            fonte = "Hemograma_set2026.pdf · pág. 1, linha 12"
        ),
        listOf("pendências", "pendencias") to Resposta(
            "Você tem resumos de exame aguardando sua confirmação na aba Revisões.",
            fonte = "Hemograma_set2026.pdf · pág. 1, linhas 8–14"
        )
    )

    private val respostaPadraoMedico = Resposta(
        "Entendi. Posso mostrar o próximo paciente ou as pendências do dia."
    )

    fun gerarResposta(mensagem: String, perfil: String = "paciente"): Resposta {
        val medico = perfil == "medico"
        val gatilhos = if (medico) gatilhosMedico else gatilhosPaciente
        val padrao = if (medico) respostaPadraoMedico else respostaPadraoPaciente
        return gatilhos.firstOrNull { (palavras, _) ->
            palavras.any { mensagem.contains(it, ignoreCase = true) }
        }?.second ?: padrao
    }
}
