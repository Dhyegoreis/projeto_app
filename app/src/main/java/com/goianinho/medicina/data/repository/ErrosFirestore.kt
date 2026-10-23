package com.goianinho.medicina.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code

// Transforma erros do Firestore em frases que explicam o motivo, com o código HTTP equivalente.
// O erro pode vir embrulhado (ex.: transação), então procura o primeiro FirebaseFirestoreException na cadeia.
fun traduzirErroFirestore(e: Throwable?): String {
    var atual: Throwable? = e
    var profundidade = 0
    while (atual != null && atual !is FirebaseFirestoreException && profundidade < 10) {
        atual = atual.cause
        profundidade++
    }
    val codigo = (atual as? FirebaseFirestoreException)?.code
    return when (codigo) {
        Code.FAILED_PRECONDITION ->
            "Falta configurar o banco de dados (índice do Firestore). Avise o administrador do app. (erro 400)"
        Code.INVALID_ARGUMENT -> "Os dados enviados são inválidos. (erro 400)"
        Code.UNAUTHENTICATED -> "Sua sessão expirou. Saia e entre novamente. (erro 401)"
        Code.PERMISSION_DENIED -> "Você não tem permissão para acessar esses dados. (erro 403)"
        Code.NOT_FOUND -> "O dado procurado não foi encontrado. (erro 404)"
        Code.ABORTED,
        Code.ALREADY_EXISTS ->
            "Conflito ao salvar: outra pessoa alterou esse dado. Tente de novo. (erro 409)"
        Code.RESOURCE_EXHAUSTED -> "Limite de uso do banco atingido. Tente mais tarde. (erro 429)"
        Code.UNAVAILABLE,
        Code.DEADLINE_EXCEEDED -> "Sem conexão com o servidor. Verifique sua internet. (erro 503)"
        else -> "Algo deu errado. Tente de novo."
    }
}
