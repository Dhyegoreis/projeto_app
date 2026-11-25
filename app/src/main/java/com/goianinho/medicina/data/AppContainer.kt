package com.goianinho.medicina.data

import com.goianinho.medicina.data.repository.AgendaRepository
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.goianinho.medicina.data.repository.MensagemRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Container de dependências, criado uma vez em GoianinhoApp.
// Decisão B: a base só ACRESCENTA; os módulos continuam funcionando como hoje.
// Quando o módulo X migrar, ele:
//   1. muda o construtor do próprio repositório para receber `db` (o Módulo 1 já recebe);
//   2. troca SÓ A SUA LINHA abaixo (ex.: AuthRepository(auth, db), ChatRepository(db));
//   3. passa a usar viewModel(factory = XViewModel.Factory).
// Essa linha é a exceção autorizada pelo líder à regra dos arquivos compartilhados.
class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val authRepository = AuthRepository(auth, db)
    val chatRepository = MensagemRepository()
    val documentoRepository = DocumentoRepository()
    val agendaRepository = AgendaRepository(db)
}
