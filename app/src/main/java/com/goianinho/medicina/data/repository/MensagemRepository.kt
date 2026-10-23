package com.goianinho.medicina.data.repository

import android.util.Log
import com.goianinho.medicina.data.model.Mensagem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MensagemRepository {

    private val db = FirebaseFirestore.getInstance()
    private val col = db.collection("mensagens")

    fun ouvirDoPaciente(
        pacienteId: String,
        onErro: (String) -> Unit = {},
        onMudou: (List<Mensagem>) -> Unit
    ): ListenerRegistration {
        return col
            .whereEqualTo("pacienteId", pacienteId)
            .orderBy("criadaEm")
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    // Ex.: índice composto ainda não criado; o link aparece no Logcat
                    Log.e("Goianinho", "mensagens", erro)
                    onErro(traduzirErroFirestore(erro))
                } else {
                    onMudou(snapshot?.toObjects(Mensagem::class.java) ?: emptyList())
                }
            }
    }

    // Conversa do médico com o Goianinho: mensagens sem paciente (pacienteId vazio)
    fun ouvirDoMedico(
        medicoId: String,
        onErro: (String) -> Unit = {},
        onMudou: (List<Mensagem>) -> Unit
    ): ListenerRegistration {
        return col
            .whereEqualTo("medicoId", medicoId)
            .whereEqualTo("pacienteId", "")
            .orderBy("criadaEm")
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    // Ex.: índice composto ainda não criado; o link aparece no Logcat
                    Log.e("Goianinho", "mensagens", erro)
                    onErro(traduzirErroFirestore(erro))
                } else {
                    onMudou(snapshot?.toObjects(Mensagem::class.java) ?: emptyList())
                }
            }
    }

    fun enviar(
        mensagem: Mensagem,
        perfil: String,
        contexto: RespostasSimuladas.Contexto = RespostasSimuladas.Contexto(),
        onErro: (String) -> Unit = {}
    ) {
        if (mensagem.texto.isBlank()) return
        // criadaEm nulo: o @ServerTimestamp do modelo faz o servidor preencher a hora
        val usuario = mensagem.copy(id = "", criadaEm = null)
        col.add(usuario)
            .addOnSuccessListener {
                if (mensagem.autor == "usuario") {
                    // Gravada só depois da mensagem do usuário, então a hora do servidor é posterior
                    val r = RespostasSimuladas.gerarResposta(mensagem.texto, perfil, contexto)
                    col.add(
                        usuario.copy(autor = "goianinho", texto = r.texto, fonte = r.fonte)
                    ).addOnFailureListener { Log.e("Goianinho", "mensagens", it) }
                }
            }
            .addOnFailureListener {
                Log.e("Goianinho", "mensagens", it)
                onErro(traduzirErroFirestore(it))
            }
    }
}
