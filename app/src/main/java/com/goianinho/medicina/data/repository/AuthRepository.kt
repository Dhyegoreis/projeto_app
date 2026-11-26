package com.goianinho.medicina.data.repository

import com.goianinho.medicina.data.COL_USUARIOS
import com.goianinho.medicina.data.MEDICO_TESTE_ID
import com.goianinho.medicina.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

// auth e db chegam do AppContainer (sem getInstance aqui)
class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    val usuarioLogadoUid: String? get() = auth.currentUser?.uid

    // Roda o bloco com limite de 10 s e devolve Result.
    // Timeout vira falha (mensagemDeErro mostra "Sem conexão");
    // cancelamento de verdade é repassado.
    private suspend fun <T> comTimeout(bloco: suspend () -> T): Result<T> =
        try {
            Result.success(withTimeout(10_000) { bloco() })
        } catch (e: TimeoutCancellationException) {
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun lerUsuario(uid: String): Usuario =
        db.collection(COL_USUARIOS).document(uid).get().await()
            .toObject(Usuario::class.java)
            ?: throw IllegalStateException("Perfil não encontrado")

    // RF-01 e RN-01. L-03: se o perfil falhar depois do signIn, derruba a sessão
    suspend fun entrar(email: String, senha: String): Result<Usuario> = comTimeout {
        val uid = auth.signInWithEmailAndPassword(email, senha).await().user?.uid
            ?: error("Usuário sem uid")
        try {
            lerUsuario(uid)
        } catch (e: Throwable) {
            auth.signOut()
            throw e
        }
    }

    suspend fun buscarUsuario(uid: String): Result<Usuario> = comTimeout { lerUsuario(uid) }

    // RF-04 e RN-12: paciente nasce vinculado ao médico de teste
    suspend fun criarConta(
        nome: String,
        email: String,
        senha: String,
        perfil: String
    ): Result<Usuario> = comTimeout {
        val user = auth.createUserWithEmailAndPassword(email, senha).await().user!!
        val usuario = Usuario(
            uid = user.uid,
            nome = nome,
            email = email,
            perfil = perfil,
            medicoId = if (perfil == "paciente") MEDICO_TESTE_ID else ""
        )
        try {
            db.collection(COL_USUARIOS).document(user.uid).set(usuario).await()
        } catch (e: Throwable) {
            // Rollback: não deixa conta "pela metade" no Auth.
            // NonCancellable garante que a limpeza rode mesmo se o timeout cancelar.
            withContext(NonCancellable) { runCatching { user.delete().await() } }
            throw e
        }
        usuario
    }

    // RF-05
    suspend fun recuperarSenha(email: String): Result<Unit> = comTimeout {
        auth.sendPasswordResetEmail(email).await()
        Unit
    }

    fun sair() = auth.signOut()
}