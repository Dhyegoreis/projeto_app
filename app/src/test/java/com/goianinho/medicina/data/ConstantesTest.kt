package com.goianinho.medicina.data

import com.goianinho.medicina.data.model.Mensagem
import org.junit.Assert.assertEquals
import org.junit.Test

class ConstantesTest {
    @Test
    fun colecoes() {
        assertEquals("usuarios", COL_USUARIOS)
        assertEquals("mensagens", COL_MENSAGENS)
        assertEquals("documentos", COL_DOCUMENTOS)
        assertEquals("horarios", COL_HORARIOS)
        assertEquals("consultas", COL_CONSULTAS)
    }

    @Test
    fun mensagemSemDonoId_temPadraoVazio() {
        assertEquals("", Mensagem().donoId)
    }
}
