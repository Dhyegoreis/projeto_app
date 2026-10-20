package com.goianinho.medicina.data.repository

import com.goianinho.medicina.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // UID de quem está logado (null = ninguém). O Firebase guarda a sessão no aparelho: RF-07
    val usuarioLogadoUid: String? get() = auth.currentUser?.uid

    // RF-01
    suspend fun entrar(email: String, senha: String): Usuario {
        val resultado = auth.signInWithEmailAndPassword(email, senha).await()
        return buscarUsuario(resultado.user!!.uid)
    }

    // RF-04 e RN-01: cria no Auth e salva o perfil no Firestore
    suspend fun criarConta(nome: String, email: String, senha: String, perfil: String): Usuario {
        val resultado = auth.createUserWithEmailAndPassword(email, senha).await()
        val usuario = Usuario(
            uid = resultado.user!!.uid,
            nome = nome,
            email = email,
            perfil = perfil
        )
        db.collection("usuarios").document(usuario.uid).set(usuario).await()
        return usuario
    }

    suspend fun buscarUsuario(uid: String): Usuario {
        val doc = db.collection("usuarios").document(uid).get().await()
        return doc.toObject(Usuario::class.java)
            ?: throw IllegalStateException("Perfil não encontrado")
    }

    // RF-05
    suspend fun recuperarSenha(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun sair() = auth.signOut()
}