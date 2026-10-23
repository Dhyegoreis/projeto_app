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

    // Provisório (demo): lista os pacientes cadastrados para o médico assumir
    suspend fun listarPacientes(): List<Usuario> {
        val docs = db.collection("usuarios").whereEqualTo("perfil", "paciente").get().await()
        return docs.documents
            .mapNotNull { doc -> doc.toObject(Usuario::class.java)?.copy(uid = doc.id) }
            .sortedBy { it.nome.lowercase() }
    }

    // Provisório (demo): o médico assume um paciente sem médico (preenche usuarios/{uid}.medicoId)
    suspend fun assumirPaciente(pacienteUid: String, medicoId: String) {
        val ref = db.collection("usuarios").document(pacienteUid)
        val paciente = ref.get().await().toObject(Usuario::class.java)
        if (paciente == null || paciente.perfil != "paciente") {
            throw VinculoException("Paciente não encontrado.")
        }
        if (paciente.medicoId == medicoId) throw VinculoException("Esse paciente já é seu.")
        if (paciente.medicoId.isNotBlank()) {
            throw VinculoException("Esse paciente já tem outro médico.")
        }
        ref.update("medicoId", medicoId).await()
    }

    // RF-05
    suspend fun recuperarSenha(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun sair() = auth.signOut()
}

// Erro de regra do vínculo médico-paciente; a mensagem já pode ser mostrada na tela
class VinculoException(mensagem: String) : Exception(mensagem)
