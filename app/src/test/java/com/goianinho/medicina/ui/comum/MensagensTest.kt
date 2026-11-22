package com.goianinho.medicina.ui.comum

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test

class MensagensTest {
    private val semRede = "Sem conexão. Verifique sua internet e tente de novo."
    private val generica = "Algo deu errado. Tente de novo."
    private val horario = "Esse horário acabou de ser reservado. Escolha outro."
    private val acesso = "Você não tem acesso a esta informação."

    @Test fun senhaFraca_vemAntesDeCredenciais() = assertEquals(
        "A senha precisa ter pelo menos 6 caracteres.",
        mensagemDeErro(FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "x", "x"))
    )

    @Test fun credenciaisInvalidas() = assertEquals(
        "E-mail ou senha incorretos.",
        mensagemDeErro(FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "x"))
    )

    @Test fun usuarioInvalido() = assertEquals(
        "E-mail ou senha incorretos.",
        mensagemDeErro(FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "x"))
    )

    @Test fun emailJaExiste() = assertEquals(
        "Este e-mail já tem uma conta. Tente entrar.",
        mensagemDeErro(FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "x"))
    )

    @Test fun semRede() = assertEquals(semRede, mensagemDeErro(FirebaseNetworkException("x")))

    @Test fun timeout() {
        val e = try {
            runBlocking { withTimeout(1) { delay(1_000) } }
            error("não deveria chegar aqui")
        } catch (t: Throwable) { t }
        assertEquals(semRede, mensagemDeErro(e))
    }

    @Test fun firestoreIndisponivel() = assertEquals(
        semRede, mensagemDeErro(FirebaseFirestoreException("x", Code.UNAVAILABLE))
    )

    @Test fun firestorePrazoEsgotado() = assertEquals(
        semRede, mensagemDeErro(FirebaseFirestoreException("x", Code.DEADLINE_EXCEEDED))
    )

    @Test fun semPermissao() = assertEquals(
        acesso, mensagemDeErro(FirebaseFirestoreException("x", Code.PERMISSION_DENIED))
    )

    @Test fun horarioOcupado() = assertEquals(horario, mensagemDeErro(HorarioOcupadoException()))

    @Test fun horarioOcupadoEmbrulhado() = assertEquals(
        horario, mensagemDeErro(RuntimeException("tx", HorarioOcupadoException()))
    )

    @Test fun permissaoEmbrulhada() = assertEquals(
        acesso,
        mensagemDeErro(
            RuntimeException(
                "a",
                RuntimeException("b", FirebaseFirestoreException("x", Code.PERMISSION_DENIED))
            )
        )
    )

    @Test fun indiceFaltando_generica() = assertEquals(
        generica, mensagemDeErro(FirebaseFirestoreException("x", Code.FAILED_PRECONDITION))
    )

    @Test fun outro() = assertEquals(generica, mensagemDeErro(IllegalStateException()))

    @Test fun avisos() {
        assertEquals("Entrar com Google chega em breve. Use e-mail e senha.", AVISO_GOOGLE)
        assertEquals("O comando de voz chega em breve. Por enquanto, digite sua mensagem.", AVISO_VOZ)
        assertEquals("A videochamada ainda não está disponível nesta versão.", AVISO_VIDEOCHAMADA)
        assertEquals(
            "A foto é grande demais. Tente de novo com menos zoom ou mais longe.",
            ERRO_FOTO_GRANDE
        )
    }
}
