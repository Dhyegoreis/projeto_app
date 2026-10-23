package com.goianinho.medicina.data.repository

import com.goianinho.medicina.data.model.StatusDocumento

// A "IA" simulada do Goianinho (RN-03 e RN-04): lista fixa "gatilho -> resposta".
// Os textos são fixos; o Contexto só escolhe a variante e preenche nomes e números,
// para a resposta não contradizer os dados reais do app.
// No futuro, a IA real entra só aqui dentro (recebendo o mesmo Contexto).
object RespostasSimuladas {

    data class Resposta(val texto: String, val fonte: String = "")

    // Dados simples do momento do envio (sem tipos do Firebase).
    // Os campos de fonte (ultimoExame*, documentoPendente, proximoPacienteDocumento) só
    // podem trazer documento já revisado para o paciente (RN-05); o médico pode citar pendente.
    data class Contexto(
        // Paciente
        val nomeMedico: String = "",
        val ultimoExameNome: String = "",
        val ultimoExameStatus: String = "",   // StatusDocumento.*; vazio = nenhum exame
        val proximaConsulta: String = "",     // texto já formatado, ex.: "Qui, 24/10 às 09:30"
        // Médico
        val pendentes: Int = 0,
        val documentoPendente: String = "",
        val proximoPacienteNome: String = "",
        val proximoPacienteHora: String = "",
        val proximoPacienteDocumento: String = ""
    )

    // Contador simulado de pacientes sem resposta (spec §10)
    const val PACIENTES_SEM_RESPOSTA = 1

    // Cada gatilho tem uma lista de variações; vale a primeira lista que combinar.
    private val gatilhosPaciente: List<Pair<List<String>, (Contexto) -> Resposta>> = listOf(
        listOf("exame") to ::respostaExamePaciente,
        listOf("consulta") to { c ->
            if (c.proximaConsulta.isNotBlank()) {
                Resposta(
                    "Sua próxima consulta é ${c.proximaConsulta}. " +
                        "Para marcar outra, escolha um horário livre na aba Agenda."
                )
            } else {
                Resposta(
                    "Os horários livres ${medicoOuGenerico(c.nomeMedico, "do")} estão na aba Agenda: " +
                        "escolha o dia e toque no horário."
                )
            }
        },
        listOf("horário", "horario") to { _ ->
            Resposta("Os horários livres estão na aba Agenda: escolha o dia e toque no horário.")
        }
    )

    private val respostaPadraoPaciente = Resposta(
        "Entendi. Vou organizar isso e te aviso. Qualquer decisão clínica fica com o seu médico."
    )

    private val gatilhosMedico: List<Pair<List<String>, (Contexto) -> Resposta>> = listOf(
        listOf("próximo paciente", "proximo paciente") to ::respostaProximoPaciente,
        listOf("pendências", "pendencias") to ::respostaPendencias
    )

    private val respostaPadraoMedico = Resposta(
        "Entendi. Posso mostrar o próximo paciente ou as pendências do dia."
    )

    private fun respostaExamePaciente(c: Contexto): Resposta {
        val nome = c.ultimoExameNome
        return when {
            nome.isBlank() || c.ultimoExameStatus.isBlank() -> Resposta(
                "Para enviar um exame, use a aba Documentos. Eu organizo os pontos principais " +
                    "e o seu médico revisa — só ele confirma o resultado."
            )
            c.ultimoExameStatus == StatusDocumento.CONFIRMADO || c.ultimoExameStatus == StatusDocumento.CORRIGIDO -> Resposta(
                "${medicoOuGenerico(c.nomeMedico, "O")} já revisou o resumo de $nome. " +
                    "Veja na aba Documentos — a avaliação é dele. Para enviar outro, use a aba Documentos.",
                fonte = "$nome · pág. 1, linhas 8–14"
            )
            else -> Resposta(
                "Seu exame $nome já está com ${medicoOuGenerico(c.nomeMedico, "o")} para revisão. " +
                    "Para enviar outro, use a aba Documentos. Só ele confirma o resultado."
            )
        }
    }

    private fun respostaProximoPaciente(c: Contexto): Resposta {
        if (c.proximoPacienteHora.isBlank() && c.proximoPacienteNome.isBlank()) {
            return Resposta("Você não tem mais consultas hoje.")
        }
        val paciente = c.proximoPacienteNome.ifBlank { "um paciente" }
        val inicio = "Seu próximo paciente é $paciente às ${c.proximoPacienteHora}. "
        return if (c.proximoPacienteDocumento.isNotBlank()) {
            Resposta(
                inicio + "Há um resumo aguardando sua confirmação — a avaliação é sua.",
                fonte = "${c.proximoPacienteDocumento} · pág. 1, linha 12"
            )
        } else {
            Resposta(inicio + "Sem exames novos para revisar.")
        }
    }

    private fun respostaPendencias(c: Contexto): Resposta {
        val fonte = if (c.documentoPendente.isNotBlank()) {
            "${c.documentoPendente} · pág. 1, linhas 8–14"
        } else ""
        return when (c.pendentes) {
            0 -> Resposta("Nada pendente: nenhum resumo aguardando sua confirmação.")
            1 -> Resposta(
                "Você tem 1 resumo de exame aguardando sua confirmação na aba Revisões.",
                fonte = fonte
            )
            else -> Resposta(
                "Você tem ${c.pendentes} resumos de exame aguardando sua confirmação na aba Revisões.",
                fonte = fonte
            )
        }
    }

    // "o Dr. X" / "do Dr. X" / "O Dr. X"; sem nome: "o seu médico" / "do seu médico" / "O seu médico"
    private fun medicoOuGenerico(nome: String, artigo: String): String {
        val medico = if (nome.isBlank()) "seu médico" else {
            if (nome.startsWith("Dr", ignoreCase = true)) nome else "Dr. $nome"
        }
        return "$artigo $medico"
    }

    fun gerarResposta(
        mensagem: String,
        perfil: String = "paciente",
        contexto: Contexto = Contexto()
    ): Resposta {
        val medico = perfil == "medico"
        val gatilhos = if (medico) gatilhosMedico else gatilhosPaciente
        val padrao = if (medico) respostaPadraoMedico else respostaPadraoPaciente
        return gatilhos.firstOrNull { (palavras, _) ->
            palavras.any { mensagem.contains(it, ignoreCase = true) }
        }?.second?.invoke(contexto) ?: padrao
    }
}
