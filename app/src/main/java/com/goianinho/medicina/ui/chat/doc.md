# Módulo 2 — Chat com o Goianinho

> **Versão v1.6 — Fase 2: backend no Firebase.** A Etapa 1 (telas visuais) está concluída. Este doc diz **o que já existe** e **o que você precisa fazer agora** (seções 2 e 3) e **os erros do código atual que você precisa corrigir** (seção "Erros encontrados"). Leia também os *padrões obrigatórios da fase 2*, iguais para todo o time.

| | |
|---|---|
| **Responsável** | [INTEGRANTE 2] |
| **Branch** | `feature/chat-ia` |
| **Pasta** | `ui/chat` |
| **Telas do protótipo** | 2 · Chat com o Goianinho (paciente) e 2 · Goianinho do médico · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 7 e **Seção 11 (Fase 2 — Backend)** da Documentação v1.6 (`docs/Documentacao-Goianinho-Medicina-v1.6.pdf`) |
| **Etapa atual** | Etapa 2 — Integração com Firebase (⏳ em andamento) |

> **Duas visões, um dono:** você entrega o **chat do paciente** e o **Goianinho do médico**. O módulo só está pronto quando as duas passam nos critérios de aceite.

---

## Arquitetura obrigatória (vale para todo o projeto)

O projeto segue o **MVVM recomendado pelo Google** ([Guide to app architecture](https://developer.android.com/topic/architecture)). Na v1.6 a arquitetura **continua a mesma**; o que muda é *como* as camadas se ligam:

```
Tela (Screen)  ──evento──▶  ViewModel  ──▶  Repository  ──▶  Firebase
     ▲                          │
     └──── UiState (StateFlow) ─┘
```

| Camada | Arquivo | Pode | Não pode |
|---|---|---|---|
| **Tela** | `XxxScreen.kt` | Desenhar, ler o `UiState` e avisar cliques | Chamar o Firebase, navegar sozinha ou escrever texto de erro solto |
| **ViewModel** | `XxxViewModel.kt` | Guardar o estado (`UiState` + `StateFlow`), validar e decidir cada ação | Ter código de interface ou criar `FirebaseFirestore`/`FirebaseAuth` |
| **Repository** | `data/repository/` | Ser **o único** que fala com o Firebase; listas saem como `Flow`, ações são `suspend` e devolvem `Result` | Saber que tela existe ou mostrar mensagem |

**Três regras que derivam disso:**
- **Fluxo em mão única:** o estado desce (ViewModel → Tela) e os eventos sobem (Tela → ViewModel).
- **Fonte única da verdade:** o dado oficial está no Firestore, e o acesso a ele passa pelo Repository dono da coleção.
- **Telas independentes:** a tela recebe funções (`onAbrirChat`, `onVoltar`...) e quem liga as telas é o `navigation/`.

**Teste rápido antes de abrir o PR:**
```bash
grep -rn "Firebase\.\|getInstance()" app/src/main/java/com/goianinho/medicina/ui
```
Não pode aparecer nada. Na v1.6 nem o Repository chama `getInstance()`: quem cria o Firebase é o `AppContainer`.

Para estudar: [Camada de UI](https://developer.android.com/topic/architecture/ui-layer) · [Camada de dados](https://developer.android.com/topic/architecture/data-layer) · [Codelab: ViewModel and State in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state)

---

## Regras do repositório (obrigatórias)

**Você trabalha somente na branch `feature/chat-ia`.** Ela é a única branch deste módulo, e é a única em que você pode fazer commit e push.

**A `main` não pode ser alterada por você de maneira alguma.** Isso inclui:
- ❌ fazer commit ou `git push` direto na `main`;
- ❌ fazer merge de qualquer branch na `main`, nem pelo botão do GitHub;
- ❌ usar `git push --force` em qualquer branch;
- ❌ trabalhar, fazer commit ou push na branch de outro integrante;
- ❌ alterar arquivos fora das suas pastas (`ui/chat` e o seu Repository em `data/repository/`) sem combinar com o líder;
- ❌ mexer no Gradle (`libs.versions.toml`, `build.gradle.kts`): **na v1.6 o líder adiciona Coil e coroutines na `main`**, e você só faz `git pull origin main`.

**O que você pode fazer:**
- ✅ trazer o que já entrou na `main` **para a sua branch** (isso não altera a `main`);
- ✅ abrir um **Pull Request** da sua branch para a `main` quando terminar uma etapa;
- ✅ ajustar o PR quando o líder pedir mudanças.

**Quem altera a `main`:** só o **líder do projeto**. Ele revisa o seu Pull Request, aprova e faz o merge ("Squash and merge"). Se precisar mudar algo compartilhado (`theme/`, `navigation/`, `data/model/`, `data/AppContainer.kt`, `ui/comum/`, Gradle), avise no grupo e o líder decide como entra.

### Comandos do dia a dia

```bash
# 1x no início: baixar o projeto e entrar na SUA branch
git clone <url-do-repositorio>
cd <pasta-do-projeto>
git checkout feature/chat-ia

# Sempre antes de começar: confirmar que está na branch certa
git branch --show-current          # tem que mostrar: feature/chat-ia

# Trazer as novidades da main para a sua branch (faça isso ANTES de começar a v1.6)
git pull origin main

# Salvar e enviar o seu trabalho (sempre para a SUA branch)
git add .
git commit -m "feat(modulo): o que você fez"      # bug de Issue: "fix(modulo): ... Corrige #12"
git push origin feature/chat-ia
```

Terminou uma etapa? No GitHub, abra **Pull request → base: `main` ← compare: `feature/chat-ia`** e avise o líder. **Não clique em "Merge".**

> Se o `git branch --show-current` mostrar `main`, **pare** e rode `git checkout feature/chat-ia` antes de qualquer commit.

---

## Fase 2 (v1.6) — padrões obrigatórios para todos os módulos

A Etapa 1 (telas visuais) está concluída. Na **Etapa 2** cada tela deixa de ser só visual: lê e grava dados reais, trata erros, segue as regras de negócio e funciona em qualquer tamanho de tela. O backend continua o **Firebase (Auth + Firestore), plano gratuito**. Documentação completa: **seção 11 da Documentação v1.6** (`docs/Documentacao-Goianinho-Medicina-v1.6.pdf`).

### Decisões técnicas (não discutir de novo no PR)

| Assunto | Decisão |
|---|---|
| Backend | Firebase Auth + Cloud Firestore (mantido) |
| Banco local (Room) | **Não usar** — o Firestore já faz cache offline |
| Injeção de dependência | **Manual**, com `AppContainer` (sem Hilt) |
| Flavors mock/prod | **Não usar** — dados de teste ficam no Firestore |
| Estado da tela | `UiState` com **Carregando / Sucesso / Erro** |
| Imagens | **Coil** (`AsyncImage`) |
| Retrofit | Só quando a IA for real |

### O que o líder entrega na `main` antes de você começar ✅ entregue

```
com/goianinho/medicina/
 ├── GoianinhoApp.kt           (Application: cria o AppContainer uma vez só)
 ├── data/AppContainer.kt      (cria o Firebase e os 4 repositórios)
 ├── data/Constantes.kt        (MEDICO_TESTE_ID e nomes das coleções)
 ├── ui/comum/Mensagens.kt     (mensagemDeErro(erro), HorarioOcupadoException, os avisos AVISO_* e o erro ERRO_FOTO_GRANDE)
 └── ui/comum/EstadosDeTela.kt (TelaCarregando, TelaErro e EstadoVazio)
```

Mais: Coil e coroutines no Gradle e campo `donoId` em `Mensagem`. **Pendente (passo manual do líder):** médico de teste e horários de exemplo no Firestore. **Faça `git pull origin main` antes de começar.**

**Estado da base (decisão B):** o líder entregou a base, mas **cada módulo migra o próprio código**. Hoje o `AppContainer` ainda cria `chatRepository = MensagemRepository()`, `documentoRepository = DocumentoRepository()` e `agendaRepository = AgendaRepository()` sem argumentos, e os módulos ainda usam `getInstance()` e `traduzirErroAuth`/`traduzirErroFirestore` (que continuam em `data/repository/`, intactos). Roteiro de migração do **seu** módulo, nesta ordem:

1. O construtor do seu repositório passa a receber `db` (e `auth`, no caso do `AuthRepository`) e deixa de chamar `getInstance()`.
2. Troque **a sua própria linha** no `AppContainer` para passar `db` (só a sua; as dos outros módulos ficam como estão, para não gerar conflito).
3. Crie a `Factory` da sua ViewModel (Padrão 1) e use `viewModel(factory = ...)` na tela.
4. Troque `traduzirErroAuth`/`traduzirErroFirestore` por `mensagemDeErro` nas suas ViewModels.

> **Módulo 2 (Chat):** o campo `donoId: String = ""` **já existe** em `data/model/Mensagem.kt` (documentos antigos sem ele continuam sendo lidos). **Gravá-lo em toda mensagem é tarefa do módulo**, junto com a consulta `ouvirConversa(donoId)`.

> **Médico de teste:** `MEDICO_TESTE_ID` em `data/Constantes.kt` pode estar com o texto provisório `COLOQUE_AQUI_O_UID_DO_MEDICO_TESTE` até o líder criar o médico de teste. Se for o caso, o vínculo do paciente novo (RN-12) não funciona ainda; avise o líder.
>
> **Regras do Firestore:** `firestore.rules` já foi reescrito para a v1.6, mas **não está publicado**. O líder só publica depois que os 4 módulos migrarem e que os passos 5 e 6 do spec 7 (ajustar `medicoId` dos pacientes e apagar `mensagens`) estiverem feitos.

### Padrão 1 — ViewModel recebe o Repository pelo `AppContainer`

```kotlin
// data/AppContainer.kt (líder)
// Forma final, DEPOIS da migração dos 4 módulos. Hoje os repositórios de chat, documentos e agenda ainda são criados sem argumentos (o de chat ainda se chama MensagemRepository).
class AppContainer {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    val authRepository = AuthRepository(auth, db)
    val chatRepository = ChatRepository(db)
    val documentoRepository = DocumentoRepository(db)
    val agendaRepository = AgendaRepository(db)
}

// Na SUA ViewModel
class XxxViewModel(private val repo: XxxRepository) : ViewModel() {
    // ...
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GoianinhoApp
                XxxViewModel(app.container.xxxRepository)
            }
        }
    }
}

// Na SUA tela
val vm: XxxViewModel = viewModel(factory = XxxViewModel.Factory)
```

O Repository passa a **receber** `db` (e `auth`) no construtor — não cria mais `FirebaseFirestore.getInstance()` lá dentro.

### Padrão 2 — `UiState` com Carregando / Sucesso / Erro

`TelaCarregando`, `TelaErro` e `EstadoVazio` já existem em `ui/comum/EstadosDeTela.kt`; não crie versões próprias.

```kotlin
sealed interface XxxUiState {
    data object Carregando : XxxUiState
    data class Sucesso(val itens: List<Item>) : XxxUiState   // lista vazia = estado vazio (RNF-05)
    data class Erro(val mensagem: String) : XxxUiState
}

// ViewModel: o Flow do Repository vira estado
val uiState: StateFlow<XxxUiState> = repo.ouvirAlgo(uid)
    .map<List<Item>, XxxUiState> { XxxUiState.Sucesso(it) }
    .catch { emit(XxxUiState.Erro(mensagemDeErro(it))) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), XxxUiState.Carregando)

// Tela
when (val e = estado) {
    is XxxUiState.Carregando -> TelaCarregando()
    is XxxUiState.Sucesso    -> if (e.itens.isEmpty()) EstadoVazio("…") else Lista(e.itens)
    is XxxUiState.Erro       -> TelaErro(e.mensagem, onTentarDeNovo = vm::recarregar)
}
```

Para formulários (Login, Criar conta, campo do chat, observação da Revisão) use uma `data class` de estado com `carregando: Boolean` e `erro: String?` — o importante é que **as três situações existam** na tela.

### Padrão 3 — Repository: listas em `Flow`, ações `suspend` + `Result`

```kotlin
// Lista em tempo real
fun ouvirAlgo(uid: String): Flow<List<Item>> = callbackFlow {
    val registro = db.collection("colecao")
        .whereEqualTo("pacienteId", uid)
        .addSnapshotListener { snap, erro ->
            if (erro != null) { close(erro); return@addSnapshotListener }
            trySend(snap?.toObjects(Item::class.java).orEmpty())
        }
    awaitClose { registro.remove() }      // para de ouvir quando a tela fecha
}

// Ação: nunca lança erro para a tela
suspend fun gravar(item: Item): Result<Unit> = runCatching {
    withTimeout(10_000) {                  // sem internet o .await() esperaria para sempre
        db.collection("colecao").add(item).await()
    }
}

// ViewModel
viewModelScope.launch {
    _estado.update { it.copy(carregando = true, erro = null) }
    repo.gravar(item)
        .onSuccess { _estado.update { it.copy(carregando = false) } }
        .onFailure { e ->
            Log.e("Goianinho", "gravar falhou", e)                       // RNF-07 (sem dado de saúde)
            _estado.update { it.copy(carregando = false, erro = mensagemDeErro(e)) }
        }
}
```

> Os callbacks `onErro`/`onMudou` e o `ListenerRegistration` da Etapa 1 **saem**. As funções `traduzirErroAuth` e `traduzirErroFirestore` são substituídas por `mensagemDeErro(erro)` em `ui/comum/Mensagens.kt`.

### Requisitos de qualidade (RNF — valem para todos)

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-01 | **Aviso de funcionalidade:** botão de algo que ainda não existe (Google, voz, videochamada) mostra um `Snackbar` dizendo que chega em breve. Nenhum botão fica sem resposta. | Must |
| RNF-02 | **Erros tratados:** toda chamada ao Firebase dentro de `runCatching`; o erro vira mensagem em português (`mensagemDeErro`) e o app nunca fecha, nem sem internet. | Must |
| RNF-03 | **Carregamento:** indicador na tela e botão da ação **desabilitado** enquanto espera o Firebase (evita clique duplo). | Must |
| RNF-04 | **Responsivo:** funciona em 360 dp e em celular grande, em pé e deitado, com fonte grande do sistema. | Must |
| RNF-05 | **Estado vazio:** lista sem dados mostra um texto explicando. | Should |
| RNF-06 | **Validação antes de gravar:** e-mail válido, senha 6+, texto não vazio — erro **embaixo do campo**, antes de chamar o Firebase. | Must |
| RNF-07 | **Bugs registrados:** `Log.e("Goianinho", ...)` em erro inesperado; bug de teste vira Issue no GitHub. | Should |
| RNF-08 | **Dados protegidos:** regras do Firestore por usuário; nunca logar senha nem dado de saúde. | Must |

**Mensagens padrão** (vêm de `mensagemDeErro(erro)`; ninguém escreve texto de erro solto):

| Situação | Mensagem |
|---|---|
| E-mail ou senha errados | E-mail ou senha incorretos. |
| E-mail já cadastrado | Este e-mail já tem uma conta. Tente entrar. |
| Senha fraca | A senha precisa ter pelo menos 6 caracteres. |
| Sem internet / tempo esgotado | Sem conexão. Verifique sua internet e tente de novo. |
| Sem permissão (`PERMISSION_DENIED`) | Você não tem acesso a esta informação. |
| Horário já reservado (RN-08) | Esse horário acabou de ser reservado. Escolha outro. |
| Foto grande demais | A foto é grande demais. Tente de novo com menos zoom ou mais longe. |
| Qualquer outro erro | Algo deu errado. Tente de novo. |
| Aviso: login com Google | Entrar com Google chega em breve. Use e-mail e senha. |
| Aviso: comando de voz | O comando de voz chega em breve. Por enquanto, digite sua mensagem. |
| Aviso: videochamada | A videochamada ainda não está disponível nesta versão. |

### Como deixar a tela responsiva (RNF-04)

- Conteúdo que pode passar da tela fica em `LazyColumn` ou em `Column` com `verticalScroll(rememberScrollState())`.
- Em tela larga (deitado, tablet): conteúdo centralizado com `Modifier.widthIn(max = 600.dp)`.
- Telas com campo de texto usam `Modifier.imePadding()` para o teclado não cobrir o campo nem o botão.
- Nada de altura fixa em card com texto: use padding e deixe o conteúdo definir a altura.
- Botões e ícones clicáveis com área mínima de **48 dp**.
- Girar o celular não perde nada: estado na ViewModel, campos digitados em `rememberSaveable`.
- Teste mínimo: celular pequeno, celular grande, deitado e com **fonte grande** nas configurações do Android.

### Bugs comuns que já sabemos evitar

| Sintoma | Causa | Como evitar |
|---|---|---|
| Lista não carrega; Logcat mostra `FAILED_PRECONDITION` | Filtro + ordenação pedem índice composto | Abrir o link do Logcat, criar o índice e avisar o líder |
| Itens duplicados | Listener nunca removido | `callbackFlow` com `awaitClose { registro.remove() }` |
| Duas contas / duas consultas criadas | Clique duplo | Botão desabilitado enquanto carrega (RNF-03) |
| Voltar leva de novo ao Login | Pilha não foi limpa | Usar o `entrarNoPerfil` da navegação (já faz `popUpTo`) |
| App trava "salvando" sem internet | `.await()` espera o servidor | `withTimeout(10_000)` + mensagem de sem conexão |
| Mensagem nova sem hora / fora de ordem | `@ServerTimestamp` ainda nulo no 1º aviso | Data como `Timestamp?` e tratar o nulo na tela |
| App perde dados ao girar | Estado só na tela | Estado na ViewModel e `rememberSaveable` |

**Como reportar um bug:** Issue no GitHub com a etiqueta `bug`: título curto, passos para reproduzir, esperado, o que aconteceu, print ou trecho do Logcat e a branch. Quem corrige commita `fix(modulo): ...` citando a Issue (`Corrige #12`).

### Checklist de pronto (todo PR da fase 2)

- [ ] A tela usa `UiState` com Carregando, Sucesso e Erro
- [ ] Nenhuma chamada ao Firebase fora do Repository (teste rápido do `grep` acima)
- [ ] Testado em **modo avião**: aparece mensagem de erro e o app não fecha
- [ ] Botões sem função mostram o aviso "em breve"
- [ ] Botões de ação desabilitados enquanto carrega
- [ ] Testado em celular pequeno, grande, deitado e com fonte grande
- [ ] Regras de negócio do módulo conferidas nos **dois perfis** (paciente e médico)
- [ ] Nenhum log com senha ou dado de saúde
- [ ] Visual continua igual ao protótipo

---

## 1. O que estas telas fazem

É o coração do projeto: a conversa em linguagem natural com o Goianinho.

**Paciente:** cabeçalho com avatar do Goianinho e "Assistente do Dr. X"; mensagens em bolhas; card de **fonte** dentro das respostas que citam documento; chips de ação rápida; campo de texto; botões enviar e voz.

**Médico:** cabeçalho escuro; faixa com os contadores do dia; fonte **clicável** que abre a Revisão (Módulo 3); chips "Próximo paciente" e "Pendências do dia".

**Na v1.6 o objetivo é: o chat tem que funcionar** — gravar, responder, mostrar "digitando…", sobreviver a falta de internet e nunca perder o que o usuário escreveu.

## 2. Onde estamos (fim da Etapa 1)

| Item | Situação |
|---|---|
| `ChatPacienteScreen`, `ChatMedicoScreen`, `ChatConteudo`, `BolhaMensagem`, `ChatViewModel` | ✅ Telas prontas, gravando e lendo no Firestore |
| `RespostasSimuladas` com `Contexto` (nome do médico, último exame, pendentes, próximo paciente) | ✅ Respostas usam dados reais; RN-03 e RN-05 respeitadas |
| `MensagemRepository` | ⚠️ Cria `FirebaseFirestore.getInstance()`, usa callbacks; **passa a se chamar `ChatRepository(db)`** |
| Conversa do médico | ⚠️ Hoje filtra `medicoId` + `pacienteId == ""` (gambiarra). Na v1.6 usa o campo **`donoId`** |
| Resposta do Goianinho | ⚠️ Se a gravação da resposta falhar, só vai para o Logcat; o usuário não fica sabendo |
| "Goianinho está digitando…" (RF-38) | ❌ Não existe |
| Falha ao enviar | ❌ O texto já saiu do campo e se perde |
| Gatilhos | ⚠️ Faltam "dor" e "oi" (cumprimento) |
| Botão de voz | ❌ Sem resposta (viola RNF-01) |
| Responsividade | ❌ Sem `imePadding`; bolha com `widthIn(max = 300.dp)` fixo |

## 3. O que fazer na v1.6 (sua lista de tarefas)

1. **Renomear e injetar:** `MensagemRepository` → **`ChatRepository(db)`** (é o nome usado no `AppContainer`). `ChatViewModel` com `Factory`.
2. **Campo `donoId` (mudança no modelo, feita pelo líder):** toda mensagem grava `donoId` = uid de quem conversa (paciente **ou** médico). Mensagem do Goianinho repete o `donoId` da conversa.
3. **Histórico (RF-08):** `ouvirConversa(donoId): Flow<List<Mensagem>>` — `whereEqualTo("donoId", ...)` + `orderBy("criadaEm")`. Substitui `ouvirDoPaciente` e `ouvirDoMedico`. Pede índice composto (abrir o link do Logcat e avisar o líder).
4. **Enviar (RF-09, RF-38):** `suspend enviar(...)`: grava a mensagem do usuário → mostra **"Goianinho está digitando…"** por ~1 s → grava a resposta com `autor = "goianinho"`. Tudo com `Result`.
5. **Falha (RNF-02):** se não gravar, o **texto volta para o campo** e aparece `mensagemDeErro(e)`. Se falhar só a resposta, mostrar aviso — nada se perde.
6. **Respostas (RN-03, RN-04):** acrescentar gatilhos **"dor"** (orienta procurar o médico/urgência, sem diagnosticar) e **"oi"/"olá"** (apresenta-se como assistente do Dr. X — P-04). Toda resposta sobre exame termina lembrando que o médico confirma.
7. **Fonte (RF-10):** respostas sobre exame citam o **documento mais recente** do paciente (nome, página, linha) — já feito via `Contexto`; manter.
8. **Visão do médico:** respostas simuladas usam os números reais do dia (consultas, revisões pendentes) — já feito; manter e conferir com o Início.
9. **Voz (RNF-01):** o botão de microfone mostra o Snackbar *"O comando de voz chega em breve. Por enquanto, digite sua mensagem."* (o RF-14 continua Could).
10. **Carregamento (RNF-03):** botão Enviar e chips desabilitados enquanto grava; campo vazio não envia (RNF-06).
11. **Hora nula:** `criadaEm` vem nulo no primeiro aviso do listener (`@ServerTimestamp`). A bolha mostra "agora" e a ordem não quebra.
12. **Responsivo (RNF-04):** `imePadding()` na coluna do chat; bolha com largura máxima **proporcional** (ex.: 80% da largura) em vez de 300 dp fixo; campo cresce até 4 linhas.

## Erros encontrados no código atual (corrigir nesta versão)

> Análise do líder em 22/11/2026, na branch `feature/chat-ia`. Os números de linha valem para essa versão; se o arquivo já mudou, procure pelo trecho citado. **Cada erro corrigido = 1 commit** `fix(chat): ... (C-XX)`. Gravidade: 🔴 alta (perde dado, fecha o app ou quebra regra de negócio) · 🟠 média (comportamento errado que o usuário percebe) · 🟡 baixa (acabamento, responsividade, código).

| ID | Grav. | Onde | O que acontece | Como corrigir |
|---|---|---|---|---|
| C-01 | 🔴 | [`ChatConteudo.kt:115-116`](ChatConteudo.kt#L115) · [`ChatViewModel.kt:243`](ChatViewModel.kt#L243) | O campo é **limpo antes** de saber se a mensagem foi gravada. Se a gravação falhar, o que o paciente escreveu **se perde** e só aparece um aviso. | Enviar pela ViewModel com `Result`; na falha, devolver o texto ao campo (`textoDevolvido`, ver 6.2). |
| C-02 | 🔴 | [`MensagemRepository.kt:69`](../../data/repository/MensagemRepository.kt#L69) | Se a **resposta do Goianinho** não for gravada, o erro só vai para o Logcat: o usuário fica **sem resposta e sem aviso**. | Gravar a resposta com `Result` e mostrar `mensagemDeErro` se falhar. |
| C-03 | 🔴 | [`MensagemRepository.kt:33-40`](../../data/repository/MensagemRepository.kt#L33) | A conversa do médico usa a gambiarra `pacienteId == ""`, e nenhuma mensagem grava `donoId`. Quando as regras da v1.6 forem publicadas (ler/criar só com `donoId == uid`), **o chat para de funcionar para os dois perfis**. | Gravar `donoId` em toda mensagem e trocar as duas consultas por `ouvirConversa(donoId)`. |
| C-04 | 🟠 | [`ChatConteudo.kt:83`](ChatConteudo.kt#L83) | Os chips não ficam bloqueados durante o envio: tocar 3 vezes seguidas em "Pendências do dia" gera **3 perguntas e 3 respostas**. | Flag `enviando` no estado; chips e botão Enviar com `enabled = !enviando` (**RNF-03**). |
| C-05 | 🟠 | [`ChatViewModel.kt:170`](ChatViewModel.kt#L170) (`montarContexto`) | Se o médico perguntar logo ao abrir, as listas (consultas, pendentes, nomes) ainda **não chegaram** e o Goianinho responde errado: "Você não tem mais consultas hoje" ou "Nada pendente". | Só liberar o envio depois que os fluxos de contexto emitirem pela primeira vez (estado `Carregando`). |
| C-06 | 🟠 | [`ChatMedicoScreen.kt:49`](ChatMedicoScreen.kt#L49) e [`:69`](ChatMedicoScreen.kt#L69) · [`ChatPacienteScreen.kt:37`](ChatPacienteScreen.kt#L37) e [`:59`](ChatPacienteScreen.kt#L59) | Enquanto carrega, o cabeçalho mostra **"Dr. "** sem nome e os contadores mostram **0**. Se o nome já tem "Dr.", vira **"Dr. Dr. Fulano"**. | Mostrar os contadores só com os dados carregados e usar o helper `nomeDoMedico` (combinar com o Módulo 1, ver H-04). |
| C-07 | 🟡 | [`ChatConteudo.kt:63`](ChatConteudo.kt#L63) | Com `enableEdgeToEdge()` e sem `imePadding()`, **o teclado cobre o campo de mensagem** em vários aparelhos. | `Modifier.imePadding()` na coluna do chat (**RNF-04**). |
| C-08 | 🟡 | [`BolhaMensagem.kt:45`](BolhaMensagem.kt#L45) | A bolha tem largura máxima fixa de 300 dp: em celular deitado ou tablet ocupa pouco espaço, e em 360 dp quase não sobra margem. | Largura proporcional (≈ 80% da tela) com `BoxWithConstraints`. |
| C-09 | 🟡 | [`ChatMedicoScreen.kt:74`](ChatMedicoScreen.kt#L74) | O chat do médico vazio fica **em branco** (o do paciente tem boas-vindas) e não há botão de voz nas duas telas (o protótipo tem). | Boas-vindas local para o médico (**RNF-05**); botão de microfone com o aviso "em breve" (**RNF-01**). |
| C-10 | 🟡 | [`ChatViewModel.kt:199`](ChatViewModel.kt#L199) | Um erro no listener de mensagens **desabilita o chat de vez**, sem opção de tentar de novo. | Estado `Erro` com "Tentar de novo". |
| C-11 | 🟡 | [`ChatViewModel.kt:137`](ChatViewModel.kt#L137) | O intervalo "hoje" do médico é calculado uma vez: depois da meia-noite, "Próximo paciente" e os contadores usam o dia anterior. | Recalcular o intervalo ao voltar à tela (mesma correção do H-05). |


---

## 4. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-08 | Exibir o histórico de mensagens, salvo no Firestore | Must |
| RF-09 | Enviar mensagem e mostrar uma resposta do Goianinho | Must |
| RF-10 | Respostas que citam documento mostram o card de fonte | Must |
| RF-11 | Chips enviam a frase como mensagem | Must |
| RF-12 | Médico: três contadores do dia no topo | Should |
| RF-13 | Médico: tocar na fonte abre a Revisão | Should |
| RF-14 | Botão de voz converte fala em texto | Could (nesta fase: aviso RNF-01) |
| **RF-38 (novo)** | Mostrar "Goianinho está digitando…" por cerca de 1 segundo antes da resposta | Should |

- **RN-03:** o Goianinho **nunca** afirma diagnóstico. Toda resposta sobre exame lembra que o médico confirma.
- **RN-04:** respostas vêm de uma lista fixa "frase-gatilho → resposta" (revisada pelo time). Sem gatilho, vai a resposta padrão.

## 5. Dados no Firestore

```kotlin
// data/model/Mensagem.kt (compartilhado — o líder acrescenta donoId)
data class Mensagem(
    @DocumentId val id: String = "",
    val donoId: String = "",           // NOVO (v1.6): uid de quem conversa (paciente ou médico)
    val pacienteId: String = "",
    val medicoId: String = "",
    val autor: String = "usuario",     // "usuario" ou "goianinho"
    val texto: String = "",
    val fonte: String = "",            // vazio = sem card de fonte
    @ServerTimestamp val criadaEm: Timestamp? = null
)
```

**Por que `donoId`:** sem ele, o médico, ao buscar por `medicoId`, receberia as conversas de **todos** os pacientes. As regras do Firestore da v1.6 só deixam ler/criar mensagem com `donoId == uid` do usuário logado.

---

## 6. Como implementar (v1.6)

### 6.1. `ChatRepository` — `Flow` + `Result`

```kotlin
class ChatRepository(private val db: FirebaseFirestore) {
    private val col = db.collection(COL_MENSAGENS)

    fun ouvirConversa(donoId: String): Flow<List<Mensagem>> = callbackFlow {
        val registro = col.whereEqualTo("donoId", donoId)
            .orderBy("criadaEm")
            .addSnapshotListener { snap, erro ->
                if (erro != null) { close(erro); return@addSnapshotListener }
                trySend(snap?.toObjects(Mensagem::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    suspend fun gravar(mensagem: Mensagem): Result<Unit> = runCatching {
        withTimeout(10_000) { col.add(mensagem.copy(id = "", criadaEm = null)).await() }
    }
}
```

`gerarResposta(...)` continua em `RespostasSimuladas` — **é o único lugar que muda quando a IA for real**.

### 6.2. ViewModel — enviar, "digitando…" e falha sem perder o texto

```kotlin
data class ChatUiState(
    val mensagens: List<Mensagem> = emptyList(),
    val carregando: Boolean = true,        // primeira leitura do histórico
    val enviando: Boolean = false,         // RNF-03
    val digitando: Boolean = false,        // RF-38
    val textoDevolvido: String? = null,    // falha: devolve o texto para o campo
    val erro: String? = null,
    val aviso: String? = null              // Snackbar (voz)
)

fun enviar(texto: String) {
    val limpo = texto.trim()
    if (limpo.isEmpty() || _uiState.value.enviando) return          // RNF-06 / clique duplo
    viewModelScope.launch {
        _uiState.update { it.copy(enviando = true, erro = null) }
        val minha = Mensagem(donoId = uid, pacienteId = pacienteId, medicoId = medicoId, texto = limpo)
        repo.gravar(minha)
            .onFailure { e ->
                Log.e("Goianinho", "enviar falhou", e)                // sem logar o texto (RNF-08)
                _uiState.update { it.copy(enviando = false, textoDevolvido = limpo, erro = mensagemDeErro(e)) }
                return@launch
            }
        _uiState.update { it.copy(digitando = true) }
        delay(1_000)                                                  // RF-38
        val r = RespostasSimuladas.gerarResposta(limpo, perfil, contextoAtual())
        repo.gravar(minha.copy(autor = "goianinho", texto = r.texto, fonte = r.fonte))
            .onFailure { e -> _uiState.update { it.copy(erro = mensagemDeErro(e)) } }
        _uiState.update { it.copy(enviando = false, digitando = false) }
    }
}

fun onVozClick() = _uiState.update { it.copy(aviso = AVISO_VOZ) }     // RNF-01
```

Na tela, quando `textoDevolvido != null`, coloque-o de volta no campo (`texto = it`) e chame `vm.textoDevolvidoUsado()`.

### 6.3. `ChatConteudo` — "digitando…", teclado e largura

```kotlin
Column(Modifier.fillMaxSize().imePadding()) {                          // teclado não cobre o campo
    LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
        items(state.mensagens, key = { it.id }) { msg -> BolhaMensagem(msg, onFonteClick) }
        if (state.digitando) item(key = "digitando") {
            Text("Goianinho está digitando…", style = MaterialTheme.typography.bodySmall)
        }
    }
    // chips: enabled = !state.enviando
    // campo: OutlinedTextField(maxLines = 4, ...) + botão Enviar (enabled = !state.enviando) + botão voz
}

// BolhaMensagem: largura proporcional em vez de 300.dp fixo
BoxWithConstraints(Modifier.fillMaxWidth()) {
    Surface(modifier = Modifier.widthIn(max = maxWidth * 0.8f).align(...)) { /* ... */ }
}

// Hora da bolha: criadaEm pode ser nulo no primeiro aviso
val hora = msg.criadaEm?.let { formatarHora(it) } ?: "agora"
```

- `LaunchedEffect(state.mensagens.size, state.digitando)` rola até o fim quando chega mensagem ou aparece o "digitando…".
- Com `items(key = { it.id })` não há mais mensagens duplicadas — **desde que** o listener seja removido no `awaitClose`.

---

## 7. O que fica simulado

- Toda a inteligência do Goianinho (`RespostasSimuladas`). A fonte citada usa nome real do documento com página/linha fictícias. Não use valores clínicos reais.
- O "digitando…" é um `delay(1_000)`.
- Voz: só o aviso "em breve".

## 8. Critérios de aceite

- [ ] **Todos os erros da seção "Erros encontrados" corrigidos (C-01 a C-11)**, cada um no seu commit `fix(...)`
- [ ] Mensagem enviada aparece na hora e continua lá ao reabrir o app
- [ ] "Goianinho está digitando…" aparece ~1 s antes de cada resposta
- [ ] Cada chip gera a resposta esperada; "oi" e "dor" têm resposta própria
- [ ] Card de fonte aparece só nas respostas que citam documento
- [ ] No médico, tocar na fonte abre a Revisão
- [ ] Médico **não** vê conversas dos pacientes, e paciente não vê a do médico (`donoId`)
- [ ] Modo avião: o texto volta para o campo, aparece "Sem conexão…" e o app não fecha
- [ ] Clique duplo em Enviar não duplica a mensagem
- [ ] Botão de voz mostra o aviso "em breve"
- [ ] Teclado não cobre o campo; funciona deitado e com fonte grande
- [ ] Visual das duas telas igual ao protótipo

## 9. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| Estado em Compose | [Codelab: State in Jetpack Compose](https://developer.android.com/codelabs/jetpack-compose-state) | `remember`, `rememberSaveable`, state hoisting |
| UiState Carregando/Sucesso/Erro | [Codelab: Get data from the internet (Mars Photos)](https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet) | Padrão de estado da v1.6 |
| Injeção manual + Factory | [Codelab: Add repository and Manual DI](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository) | `ChatRepository` vindo do `AppContainer` |
| Flow e callbackFlow | [Guia: Kotlin flows on Android](https://developer.android.com/kotlin/flow) | `ouvirConversa` com `awaitClose` |
| Coroutines | [Guia: Kotlin coroutines on Android](https://developer.android.com/kotlin/coroutines) | `viewModelScope`, `delay`, `withTimeout` |
| Listas | [Guia: Lists and grids](https://developer.android.com/develop/ui/compose/lists) | `LazyColumn`, `key`, scroll |
| Teclado e insets | [Guia: Window insets in Compose](https://developer.android.com/develop/ui/compose/layouts/insets) | `imePadding()` |
| Snackbar | [Guia: Snackbar](https://developer.android.com/develop/ui/compose/components/snackbar) | Aviso do botão de voz |
| Campos de texto | [Guia: Text fields](https://developer.android.com/develop/ui/compose/text/user-input) | Campo de mensagem |
| Tempo real | [Firebase: Get realtime updates](https://firebase.google.com/docs/firestore/query-data/listen) | Histórico que atualiza sozinho |
| Gravar dados | [Firebase: Add data](https://firebase.google.com/docs/firestore/manage-data/add-data) | `add()` e `@ServerTimestamp` |
| Índices | [Firebase: Index types](https://firebase.google.com/docs/firestore/query-data/index-overview) | Índice `donoId` + `criadaEm` |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
