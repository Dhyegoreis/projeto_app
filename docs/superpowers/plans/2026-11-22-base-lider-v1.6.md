# Base do líder da Fase 2 (v1.6): plano de implementação

> **Para agentes executores:** sub-skill obrigatória: superpowers:subagent-driven-development. Os passos usam checkbox (`- [ ]`). Modelos: implementação no **sonnet** e revisão no **opus**.

- **Objetivo:** entregar na `main` a base compartilhada da Fase 2. São Gradle, `GoianinhoApp`, `AppContainer`, `Constantes`, `Mensagens`, `EstadosDeTela`, `donoId`, regras escritas mas não publicadas e a documentação corrigida. Depois, atualizar as branches `feature/*`.
- **Arquitetura:** a base só **acrescenta** arquivos novos. O `AppContainer` instancia os repositórios atuais sem alterá-los (decisão B), e cada módulo migra depois, na própria branch.
- **Stack:** Kotlin, Jetpack Compose (Material 3), Firebase Auth e Firestore (BoM 34.19.0), Coil 2.7.0, kotlinx-coroutines-play-services 1.10.2 e JUnit 4.
- **Spec:** `docs/superpowers/specs/2026-11-22-base-lider-v1.6-design.md`. Leia o spec junto com este plano: ele contém a tabela de mensagens (6.5) e as regras (6.7) com os valores exatos.

Pacote base: `app/src/main/java/com/goianinho/medicina/` (abaixo chamado de `BASE/`). Testes ficam em `app/src/test/java/com/goianinho/medicina/`.

## Restrições globais

- **Não alterar nenhum `.kt`** em `BASE/ui/login`, `BASE/ui/home`, `BASE/ui/chat`, `BASE/ui/documentos`, `BASE/ui/agenda` nem em `BASE/data/repository/`. Os únicos arquivos dessas pastas que podem mudar são os 5 `doc.md`.
- `ErrosAuth.kt` e `ErrosFirestore.kt` **ficam intactos**.
- Os nomes no código são iguais aos dos `doc.md`:
  - constantes de nível de arquivo `MEDICO_TESTE_ID`, `COL_USUARIOS`, `COL_MENSAGENS`, `COL_DOCUMENTOS`, `COL_HORARIOS`, `COL_CONSULTAS`, `AVISO_GOOGLE`, `AVISO_VOZ`, `AVISO_VIDEOCHAMADA` e `ERRO_FOTO_GRANDE`;
  - a função `mensagemDeErro(erro: Throwable): String`;
  - a classe `HorarioOcupadoException`;
  - as propriedades `authRepository`, `chatRepository`, `documentoRepository` e `agendaRepository`.
- Textos de erro e de aviso **literalmente** iguais aos da tabela 6.5 do spec.
- Não mudar as versões de Kotlin ou AGP. Se o build quebrar por incompatibilidade, parar e reportar.
- Logs com `Log.e("Goianinho", …)` nunca levam senha nem dado de saúde.
- Depois do merge, o app continua funcionando exatamente como hoje.
- Commits no padrão `feat(base): …`, `docs: …` e `chore: …`, terminando com `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- Verificação de build: `./gradlew assembleDebug`. Testes: `./gradlew testDebugUnitTest`.

## Pontos de atenção na revisão

1. **Erro embrulhado em transação:** um `HorarioOcupadoException` ou `FirebaseFirestoreException` dentro de `cause` precisa devolver a mensagem certa, não a genérica. Teste na Tarefa 2.
2. **`FirebaseAuthWeakPasswordException` é subclasse de `InvalidCredentials`:** a ordem do `when` importa. Teste na Tarefa 2.
3. **Documentos antigos de `mensagens` sem `donoId`:** a desserialização não pode quebrar (o padrão é `""`). Teste na Tarefa 3.
4. **Índice faltando (`FAILED_PRECONDITION`):** devolve a mensagem genérica e loga, sem mostrar texto técnico ao usuário. Teste na Tarefa 2.
5. **App abrindo com a `Application` nova registrada:** se o `GoianinhoApp` não estiver no Manifest, o cast `as GoianinhoApp` das `Factory` quebra. A Tarefa 3 confere o Manifest e a abertura manual do app.

---

### Tarefa 1: Gradle (Coil e coroutines-play-services)

**Arquivos:**
- Modificar: `gradle/libs.versions.toml`
- Modificar: `app/build.gradle.kts`

**Interfaces:**
- Produz: `libs.coil.compose` disponível; `kotlinx-coroutines-play-services` 1.10.2 (`.await()`).

- [ ] **Passo 1:** em `[versions]`, `coroutinesPlayServices = "1.10.2"` e `coil = "2.7.0"`. Em `[libraries]`, `coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }`.
- [ ] **Passo 2:** em `app/build.gradle.kts`, `implementation(libs.coil.compose)` junto das outras dependências. Em `android { testOptions { unitTests.isReturnDefaultValues = true } }`, para que `Log.e` não quebre os testes JVM da Tarefa 2.
- [ ] **Passo 3:** rodar `./gradlew buildEnvironment | grep -i kotlin` e anotar a versão real do compilador Kotlin no relatório.
- [ ] **Passo 4:** rodar `./gradlew assembleDebug`. Esperado: `BUILD SUCCESSFUL`. Se falhar por incompatibilidade de versão, parar e reportar sem mudar nada além do que está nesta tarefa.
- [ ] **Passo 5:** commit `chore(base): Coil 2.7.0 e coroutines-play-services 1.10.2`.

### Tarefa 2: `ui/comum/Mensagens.kt` (mensagemDeErro, avisos, HorarioOcupadoException)

**Arquivos:**
- Criar: `BASE/ui/comum/Mensagens.kt`
- Testar: `app/src/test/java/com/goianinho/medicina/ui/comum/MensagensTest.kt`

**Interfaces:**
- Consome: a configuração de teste da Tarefa 1.
- Produz:
  - `fun mensagemDeErro(erro: Throwable): String`;
  - `class HorarioOcupadoException : Exception("Horário já reservado")`;
  - `const val AVISO_GOOGLE`, `AVISO_VOZ`, `AVISO_VIDEOCHAMADA` e `ERRO_FOTO_GRANDE`, com os textos do spec 6.5.

- [ ] **Passo 1: testes que falham.** Um `@Test` por linha, cada um com `assertEquals(textoEsperado, mensagemDeErro(x))`:
  - `senhaFraca_vemAntesDeCredenciais`: `FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "x", "x")` → "A senha precisa ter pelo menos 6 caracteres."
  - `credenciaisInvalidas`: `FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "x")` → "E-mail ou senha incorretos."
  - `usuarioInvalido`: `FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "x")` → "E-mail ou senha incorretos."
  - `emailJaExiste`: `FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "x")` → "Este e-mail já tem uma conta. Tente entrar."
  - `semRede`: `FirebaseNetworkException("x")` → "Sem conexão. Verifique sua internet e tente de novo."
  - `timeout`: a exceção capturada de `runBlocking { withTimeout(1) { delay(1_000) } }` → "Sem conexão. Verifique sua internet e tente de novo."
  - `firestoreIndisponivel`: `FirebaseFirestoreException("x", Code.UNAVAILABLE)` → texto de sem conexão. Fazer o mesmo para `DEADLINE_EXCEEDED`.
  - `semPermissao`: `FirebaseFirestoreException("x", Code.PERMISSION_DENIED)` → "Você não tem acesso a esta informação."
  - `horarioOcupado`: `HorarioOcupadoException()` → "Esse horário acabou de ser reservado. Escolha outro."
  - `horarioOcupadoEmbrulhado`: `RuntimeException("tx", HorarioOcupadoException())` → mensagem de horário.
  - `permissaoEmbrulhada`: `RuntimeException("a", RuntimeException("b", FirebaseFirestoreException("x", Code.PERMISSION_DENIED)))` → mensagem de acesso.
  - `indiceFaltando_genérica`: `FirebaseFirestoreException("x", Code.FAILED_PRECONDITION)` → "Algo deu errado. Tente de novo."
  - `outro`: `IllegalStateException()` → "Algo deu errado. Tente de novo."
  - `avisos`: os 4 `const` são iguais aos textos do spec 6.5.

  Se alguma exceção do Firebase não puder ser instanciada na JVM (construtor inacessível ou erro de classe Android), remover só aquele teste e registrar no relatório qual ficou de fora.
- [ ] **Passo 2:** rodar `./gradlew testDebugUnitTest --tests '*MensagensTest*'`. Esperado: FAIL (não compila, `mensagemDeErro` não existe).
- [ ] **Passo 3: implementação.**
  - Percorrer `erro` e `cause` até 10 níveis.
  - Na primeira exceção reconhecida, decidir por esta ordem: `HorarioOcupadoException`, `FirebaseAuthWeakPasswordException`, `InvalidCredentials`/`InvalidUser`, `UserCollision`, `FirebaseNetworkException`/`TimeoutCancellationException`, `FirebaseFirestoreException` por código.
  - Para `FAILED_PRECONDITION`, chamar `Log.e("Goianinho", "Índice do Firestore faltando: ${e.message}")`.
  - Qualquer outra coisa devolve a mensagem genérica.
- [ ] **Passo 4:** rodar o mesmo comando. Esperado: PASS.
- [ ] **Passo 5:** commit `feat(base): mensagemDeErro e avisos padrão (Mensagens.kt)`.

### Tarefa 3: `Constantes`, `donoId`, `AppContainer`, `GoianinhoApp` e Manifest

**Arquivos:**
- Criar: `BASE/data/Constantes.kt`, `BASE/data/AppContainer.kt` e `BASE/GoianinhoApp.kt`
- Modificar: `BASE/data/model/Mensagem.kt` e `app/src/main/AndroidManifest.xml` (tag `<application>`)
- Testar: `app/src/test/java/com/goianinho/medicina/data/ConstantesTest.kt`

**Interfaces:**
- Produz:
  - `MEDICO_TESTE_ID` e `COL_*` (de nível de arquivo, pacote `com.goianinho.medicina.data`);
  - `Mensagem.donoId: String = ""`;
  - `class AppContainer` com `authRepository: AuthRepository`, `chatRepository: MensagemRepository`, `documentoRepository: DocumentoRepository` e `agendaRepository: AgendaRepository`;
  - `class GoianinhoApp : Application` com `lateinit var container: AppContainer`.

- [ ] **Passo 1: testes que falham** em `ConstantesTest`:
  - `colecoes`: `assertEquals("usuarios", COL_USUARIOS)` e o mesmo para mensagens, documentos, horarios e consultas;
  - `mensagemSemDonoId_temPadraoVazio`: `assertEquals("", Mensagem().donoId)`.
- [ ] **Passo 2:** rodar `./gradlew testDebugUnitTest --tests '*ConstantesTest*'`. Esperado: FAIL (não compila).
- [ ] **Passo 3:** criar `Constantes.kt` exatamente como no spec 6.4. O valor de `MEDICO_TESTE_ID` é `"COLOQUE_AQUI_O_UID_DO_MEDICO_TESTE"`, com o comentário `TODO(líder)`.
- [ ] **Passo 4:** em `Mensagem.kt`, acrescentar `val donoId: String = ""` logo depois de `id`, com o comentário `// uid de quem conversa (paciente ou médico). Novo na v1.6.`
- [ ] **Passo 5:** criar `AppContainer.kt` conforme o spec 6.3, com `private val auth` e `private val db`. Usar `AuthRepository(auth, db)` e os outros 3 sem argumentos. No topo, um comentário explicando a decisão B e que cada módulo troca **só a sua linha** ao migrar.
- [ ] **Passo 6:** criar `GoianinhoApp.kt` (spec 6.2) e acrescentar `android:name=".GoianinhoApp"` em `<application>`.
- [ ] **Passo 7:** rodar `./gradlew testDebugUnitTest assembleDebug`. Esperado: PASS e `BUILD SUCCESSFUL`.
- [ ] **Passo 8:** commit `feat(base): AppContainer, GoianinhoApp, Constantes e donoId em Mensagem`.

### Tarefa 4: `ui/comum/EstadosDeTela.kt`

**Arquivos:**
- Criar: `BASE/ui/comum/EstadosDeTela.kt`

**Interfaces:**
- Produz: `@Composable fun TelaCarregando(modifier: Modifier = Modifier)`, `@Composable fun TelaErro(mensagem: String, onTentarDeNovo: () -> Unit, modifier: Modifier = Modifier)` e `@Composable fun EstadoVazio(texto: String, modifier: Modifier = Modifier)`.

- [ ] **Passo 1:** implementar seguindo o spec 6.5.1.
  - Conteúdo centralizado em `fillMaxSize()`, com padding de 24 dp e cores de `MaterialTheme`/`ui/theme`.
  - `TelaErro` tem o texto e um `Button("Tentar de novo")` com `Modifier.heightIn(min = 48.dp)`.
  - Nada de altura fixa.
  - Um `@Preview` para cada componente.
- [ ] **Passo 2:** rodar `./gradlew assembleDebug`. Esperado: `BUILD SUCCESSFUL`.
- [ ] **Passo 3:** commit `feat(base): TelaCarregando, TelaErro e EstadoVazio`.

### Tarefa 5: `firestore.rules` v1.6 (não publicar)

**Arquivos:**
- Modificar: `firestore.rules`

- [ ] **Passo 1:** reescrever conforme o spec 6.7.
  - Cabeçalho com o aviso "NÃO PUBLICAR…" literal.
  - Manter as funções auxiliares atuais.
  - Implementar cada bloco: usuarios (read restrito, create com RN-12 via `get()`, update sem "Assumir paciente"), mensagens (`donoId`, create por perfil, sem update/delete), documentos (+ RN-13), horarios e consultas (sem update).
  - Comentar as decisões "update proibido de propósito".
- [ ] **Passo 2:** se `firebase` CLI estiver instalado, rodar `firebase emulators:exec --only firestore "true"` para checar a sintaxe. Se não estiver, registrar "não validado por emulador".
- [ ] **Passo 3:** no relatório, montar uma tabela com cada operação que os `doc.md` mandam os repositórios fazer **depois da migração** (ler perfil, criar conta, ouvir conversa por `donoId`, enviar mensagem, criar documento, confirmar, corrigir, ouvir horários, reservar em transação, ouvir consultas) e a regra que a permite.
- [ ] **Passo 4:** commit `feat(base): regras do Firestore v1.6 (escritas, não publicadas)`.

### Tarefa 6: documentação do repositório (README e 5 `doc.md`)

**Arquivos:**
- Modificar: `README.md`, `BASE/ui/login/doc.md`, `BASE/ui/home/doc.md`, `BASE/ui/chat/doc.md`, `BASE/ui/documentos/doc.md` e `BASE/ui/agenda/doc.md`

**Interfaces:**
- Consome: os nomes produzidos nas Tarefas 2, 3 e 4.

- [ ] **Passo 1:** aplicar o spec 6.8 em todos os arquivos.
  - Na seção "O que o líder entrega na `main`": ✅ entregue e `EstadosDeTela.kt` na árvore.
  - Acrescentar o roteiro de migração do módulo:
    1. construtor do repositório recebe `db`;
    2. trocar a própria linha no `AppContainer`;
    3. criar a `Factory`;
    4. trocar `traduzirErro*` por `mensagemDeErro`.
  - Avisar que o `MEDICO_TESTE_ID` pode estar provisório.
  - No `ui/login/doc.md`, dizer que a linha `AuthRepository(auth, db)` já está pronta.
  - No `ui/chat/doc.md`, dizer que `donoId` já existe no modelo e que gravá-lo é tarefa do módulo.
  - No `ui/agenda/doc.md`, dizer que `HorarioOcupadoException` já está em `ui/comum/Mensagens.kt` e remover a instrução de criá-la. Atualizar o bloco de código correspondente.
  - No `ui/documentos/doc.md`, citar `ERRO_FOTO_GRANDE`.
  - No Padrão 2 de cada `doc.md`, citar que `TelaCarregando`, `TelaErro` e `EstadoVazio` vêm de `ui/comum/EstadosDeTela.kt`.
- [ ] **Passo 2:** no `README.md`, deixar a estrutura de pastas igual à real.
  - Na seção "Fase atual — v1.6": passo 1 (Líder) entregue, com uma nota sobre a decisão B.
  - Nota sobre as regras escritas e não publicadas, e sobre os passos manuais do spec 7.
- [ ] **Passo 3:** verificar com `grep -rn "COLECAO_\|HorarioJaReservado\|FotoGrandeDemais" README.md BASE/ui/*/doc.md`, que deve vir vazio. Conferir, para cada nome da base citado nos documentos, que `grep` o encontra em `BASE/` (fora de `doc.md`).
- [ ] **Passo 4:** rodar `git diff --stat main -- BASE/ui BASE/data/repository`. Só podem aparecer `doc.md` e os arquivos novos de `ui/comum/`.
- [ ] **Passo 5:** commit `docs: base do líder entregue e roteiro de migração por módulo`.

### Tarefa 7: documentação no Claude Docs (fonte do PDF, mantém v1.6)

Executada pelo controlador (sessão principal), porque precisa do conector do Claude Docs e da skill `anthropic-skills:docs`.

**Fonte:** https://claude.ai/artifact/AvNfDgcfVoRwGY94n2YeMm

- [ ] **Passo 1:** carregar a skill de docs e ler o documento.
- [ ] **Passo 2:** aplicar as 6 correções do spec 6.9:
  1. RF-37 (Must) e RF-38 (Should) nas tabelas;
  2. regras alinhadas com o `firestore.rules` da Tarefa 5;
  3. decisão B na ordem de trabalho;
  4. `HorarioOcupadoException`, `ERRO_FOTO_GRANDE` e `EstadosDeTela`;
  5. "Atualizado em 22/11/2026" na capa;
  6. revisão na linha 1.6 do histórico.
- [ ] **Passo 3:** exportar o PDF pelo conector, se a exportação estiver disponível, e substituir `docs/Documentacao-Goianinho-Medicina-v1.6.pdf` com o mesmo nome. Se não estiver, pedir ao líder que exporte.
- [ ] **Passo 4:** commit `docs: PDF v1.6 revisado (RF-37/38, regras, migração por módulo)`.

### Tarefa 8: verificação final, merge na `main` e atualização das branches

Executada pelo controlador depois da revisão final do branch inteiro (opus). O líder autorizou subir para a `main` e atualizar as branches.

- [ ] **Passo 1:** rodar `./gradlew testDebugUnitTest assembleDebug`. Esperado: tudo verde.
- [ ] **Passo 2:** rodar `git diff --stat main` e conferir as restrições globais (nenhum `.kt` de módulo ou de `data/repository/`).
- [ ] **Passo 3:** fazer `git checkout main && git pull origin main && git merge --no-ff lider/atualizacao-v1.6 && git push origin main`.
- [ ] **Passo 4:** para cada `feature/login`, `feature/chat-ia`, `feature/documentos` e `feature/agenda`, fazer `git checkout <b> && git pull origin <b> && git merge main && git push origin <b>`. Se houver conflito, parar e reportar ao líder; não resolver por conta própria em código de módulo.
- [ ] **Passo 5:** fazer `git push origin lider/atualizacao-v1.6` (histórico) e voltar para a `main`.
- [ ] **Passo 6:** relatório ao líder:
  - o que entrou;
  - a versão real do Kotlin;
  - os testes que ficaram de fora, se houver;
  - se as regras foram validadas;
  - a lista dos passos manuais do spec 7, que ainda estão pendentes.
