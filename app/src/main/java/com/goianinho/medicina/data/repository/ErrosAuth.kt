package com.goianinho.medicina.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

// Transforma erros técnicos do Firebase em frases simples (P-05).
// A ordem importa: WeakPassword é "filha" de InvalidCredentials, então vem antes.
fun traduzirErroAuth(e: Exception): String = when (e) {
    is FirebaseAuthWeakPasswordException -> "Senha muito fraca. Use pelo menos 6 caracteres."
    is FirebaseAuthInvalidCredentialsException,
    is FirebaseAuthInvalidUserException -> "E-mail ou senha incorretos."
    is FirebaseAuthUserCollisionException -> "Já existe uma conta com esse e-mail."
    is FirebaseNetworkException -> "Sem internet. Tente de novo."
    else -> "Algo deu errado. Tente de novo."
}