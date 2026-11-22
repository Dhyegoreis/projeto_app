package com.goianinho.medicina.ui.comum

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import kotlinx.coroutines.TimeoutCancellationException

const val AVISO_GOOGLE = "Entrar com Google chega em breve. Use e-mail e senha."
const val AVISO_VOZ = "O comando de voz chega em breve. Por enquanto, digite sua mensagem."
const val AVISO_VIDEOCHAMADA = "A videochamada ainda não está disponível nesta versão."
const val ERRO_FOTO_GRANDE = "A foto é grande demais. Tente de novo com menos zoom ou mais longe."

private const val MSG_SEM_REDE = "Sem conexão. Verifique sua internet e tente de novo."
private const val MSG_GENERICA = "Algo deu errado. Tente de novo."

// Lançada pela transação de agendamento quando o horário já foi reservado (RN-08).
class HorarioOcupadoException : Exception("Horário já reservado")

// Procura na cadeia de causas (até 10 níveis), pois transações embrulham o erro.
fun mensagemDeErro(erro: Throwable): String {
    var atual: Throwable? = erro
    var profundidade = 0
    while (atual != null && profundidade < 10) {
        when (atual) {
            is HorarioOcupadoException -> return "Esse horário acabou de ser reservado. Escolha outro."
            // WeakPassword é subclasse de InvalidCredentials: precisa vir antes.
            is FirebaseAuthWeakPasswordException -> return "A senha precisa ter pelo menos 6 caracteres."
            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException -> return "E-mail ou senha incorretos."
            is FirebaseAuthUserCollisionException -> return "Este e-mail já tem uma conta. Tente entrar."
            is FirebaseNetworkException,
            is TimeoutCancellationException -> return MSG_SEM_REDE
            is FirebaseFirestoreException -> when (atual.code) {
                Code.UNAVAILABLE, Code.DEADLINE_EXCEEDED -> return MSG_SEM_REDE
                Code.PERMISSION_DENIED -> return "Você não tem acesso a esta informação."
                Code.FAILED_PRECONDITION -> {
                    // Índice faltando: a mensagem técnica traz o link para criá-lo no Logcat.
                    Log.e("Goianinho", "Índice do Firestore faltando: ${atual.message}")
                    return MSG_GENERICA
                }
                // Código não mapeado: segue a cadeia, a causa pode ser reconhecida.
                else -> Unit
            }
        }
        atual = atual.cause
        profundidade++
    }
    return MSG_GENERICA
}
