# Módulo 3 — Documentos e Revisão

> **Versão v1.6 — Fase 2: backend no Firebase.** A Etapa 1 (telas visuais) está concluída. Este doc diz **o que já existe** e **o que você precisa fazer agora** (seções 2 e 3) e **os erros do código atual que você precisa corrigir** (seção "Erros encontrados"). Leia também os *padrões obrigatórios da fase 2*, iguais para todo o time.

| | |
|---|---|
| **Responsável** | [INTEGRANTE 3] |
| **Branch** | `feature/documentos` |
| **Pasta** | `ui/documentos` |
| **Telas do protótipo** | 3 · Documentos (paciente) e 3 · Revisão de resumos (médico) · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 8 e **Seção 11 (Fase 2 — Backend)** da Documentação v1.6 (`docs/Documentacao-Goianinho-Medicina-v1.6.pdf`) |
| **Etapa atual** | Etapa 2 — Integração com Firebase (⏳ em andamento) |

> **Duas visões, um dono:** você entrega **Documentos** (paciente) e **Revisão de resumos** (médico). O módulo só está pronto quando as duas passam nos critérios de aceite.

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

**Você trabalha somente na branch `feature/documentos`.** Ela é a única branch deste módulo, e é a única em que você pode fazer commit e push.

**A `main` não pode ser alterada por você de maneira alguma.** Isso inclui:
- ❌ fazer commit ou `git push` direto na `main`;
- ❌ fazer merge de qualquer branch na `main`, nem pelo botão do GitHub;
- ❌ usar `git push --force` em qualquer branch;
- ❌ trabalhar, fazer commit ou push na branch de outro integrante;
- ❌ alterar arquivos fora das suas pastas (`ui/documentos` e o seu Repository em `data/repository/`) sem combinar com o líder;
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
git checkout feature/documentos

# Sempre antes de começar: confirmar que está na branch certa
git branch --show-current          # tem que mostrar: feature/documentos

# Trazer as novidades da main para a sua branch (faça isso ANTES de começar a v1.6)
git pull origin main

# Salvar e enviar o seu trabalho (sempre para a SUA branch)
git add .
git commit -m "feat(modulo): o que você fez"      # bug de Issue: "fix(modulo): ... Corrige #12"
git push origin feature/documentos
```

Terminou uma etapa? No GitHub, abra **Pull request → base: `main` ← compare: `feature/documentos`** e avise o líder. **Não clique em "Merge".**

> Se o `git branch --show-current` mostrar `main`, **pare** e rode `git checkout feature/documentos` antes de qualquer commit.

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

> **Módulo 3 (Documentos):** o texto da foto grande demais já está pronto em `ERRO_FOTO_GRANDE` (`ui/comum/Mensagens.kt`); use a constante, não copie a frase.

> **Médico de teste:** `MEDICO_TESTE_ID` em `data/Constantes.kt` pode estar com o texto provisório `COLOQUE_AQUI_O_UID_DO_MEDICO_TESTE` até o líder criar o médico de teste. Se for o caso, o vínculo do paciente novo (RN-12) não funciona ainda; avise o líder.
>
> **Regras do Firestore:** `firestore.rules` já foi reescrito para a v1.6, mas **não está publicado**. O líder só publica depois que os 4 módulos migrarem e que os passos 5 e 6 do spec 7 (ajustar `medicoId` dos pacientes e apagar `mensagens`) estiverem feitos.

### Padrão 1 — ViewModel recebe o Repository pelo `AppContainer`

```kotlin
// data/AppContainer.kt (líder)
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

O paciente envia um exame, o Goianinho gera um resumo com a fonte de cada ponto, e o médico **confirma ou corrige**.

**Paciente:** botões "Enviar arquivo" e "Tirar foto"; card do documento mais recente com o "Resumo do Goianinho" (cada ponto com página e linha); etiqueta de status; lista de documentos anteriores.

**Médico:** fila de pacientes com resumo pendente; card do documento com os pontos e o link "Ver no documento"; campo de observação; botões Corrigir e Confirmar; aviso de registro no prontuário.

> **Decisão v1.1 (mantida) — sem Firebase Storage.** O app **não envia o arquivo**. Grava no Firestore só os dados do documento. Foto pequena vai como texto Base64, abaixo de 1 MB.

## 2. Onde estamos (fim da Etapa 1)

| Item | Situação |
|---|---|
| `DocumentosScreen`, `RevisaoScreen`, `DocumentosViewModel`, `RevisaoViewModel` | ✅ Telas prontas, gravando e lendo no Firestore |
| RN-05 no paciente (resumo escondido até a confirmação) | ✅ Já esconde — trocar o texto para "Aguardando revisão do Dr. X" |
| Confirmar / Corrigir gravam `confirmadoPor`, `confirmadoEm`, `apoioIA` | ✅ |
| `DocumentoRepository` | ⚠️ Cria `getInstance()`, usa callbacks `onErro`/`onResultado` — migrar para `Flow` + `Result` |
| Foto | ⚠️ Comprime em JPEG 70% e aceita até 900 KB, **sem reduzir o tamanho**. A v1.6 pede máx. 1280 px e limite de **700 KB** |
| "Ver no documento" (RF-23) | ❌ Não mostra a foto — usar **Coil** (`AsyncImage`) |
| Corrigir sem observação (RN-13) | ❌ Botão Corrigir fica habilitado com o campo vazio |
| Carregamento nos botões (RNF-03) | ⚠️ Conferir Confirmar/Corrigir/Enviar arquivo desabilitados enquanto grava |
| Responsividade | ⚠️ Conferir 360 dp, deitado e fonte grande |

## 3. O que fazer na v1.6 (sua lista de tarefas)

1. **Repository com injeção:** `DocumentoRepository(db)`; listas como `Flow` (`ouvirDoPaciente`, `ouvirPendentes`), ações `suspend ... : Result<Unit>` (`registrar`, `confirmar`, `corrigir`) com `withTimeout(10_000)`.
2. **ViewModels com `Factory`** e `UiState` Carregando / Sucesso / Erro nas duas telas.
3. **Enviar arquivo (RF-15):** seletor do Android → grava `nome`, `tipo`, `enviadoEm`, resumo simulado e `status = aguardando_medico`, com `pacienteId` = uid e `medicoId` do paciente (RN-12 garante que existe).
4. **Tirar foto (RF-16):** reduzir para **no máximo 1280 px** no maior lado, JPEG 70%. Se passar de **700 KB**, recusa com *"A foto é grande demais. Tente de novo com menos zoom ou mais longe."*; se couber, salva em Base64.
5. **Ver no documento (RF-23):** `AsyncImage` do Coil com os bytes do Base64, com imagem de **carregamento** e de **erro**. Sem foto: mostrar os dados do documento (nome, tipo, data).
6. **Paciente (RN-05):** enquanto `status == aguardando_medico`, mostrar **"Aguardando revisão do Dr. X"** (nome do médico vinculado) no lugar do resumo.
7. **Médico (RN-06, RN-07, RN-13):** fila em tempo real só dos seus pacientes; Confirmar grava os 3 campos de registro; **Corrigir exige observação** — botão desabilitado com o campo vazio.
8. **Carregamento (RNF-03):** Enviar arquivo, Tirar foto, Confirmar e Corrigir desabilitados e com indicador enquanto gravam.
9. **Estado vazio (RNF-05):** paciente sem documentos → *"Você ainda não enviou documentos"*; fila vazia → *"Nenhum resumo aguardando revisão"*.
10. **Responsivo (RNF-04):** `LazyColumn` na lista; campo de observação com `imePadding()`; botões Corrigir/Confirmar empilham em tela estreita ou com fonte grande.

> **Regras do Firestore (seção 11):** `documentos` só pode ser **criado** pelo próprio paciente (`pacienteId == uid`) com `status == 'aguardando_medico'`, e só o **médico do documento** (`medicoId == uid`) pode atualizar. Teste os dois perfis depois que o líder publicar as regras.

## Erros encontrados no código atual (corrigir nesta versão)

> Análise do líder em 22/11/2026, na branch `feature/chat-ia` (código igual ao da `main`). Os números de linha valem para essa versão; se o arquivo já mudou, procure pelo trecho citado. **Cada erro corrigido = 1 commit** `fix(documentos): ... (D-XX)`. Gravidade: 🔴 alta (perde dado, fecha o app ou quebra regra de negócio) · 🟠 média (comportamento errado que o usuário percebe) · 🟡 baixa (acabamento, responsividade, código).

| ID | Grav. | Onde | O que acontece | Como corrigir |
|---|---|---|---|---|
| D-01 | 🔴 | [`RevisaoScreen.kt:160`](RevisaoScreen.kt#L160) e [`:173`](RevisaoScreen.kt#L173) | O médico consegue **corrigir sem escrever observação** (o campo diz "opcional"), o que fere a **RN-13**: o paciente vê "O médico corrigiu este resumo" sem saber o que mudou. | Botão Corrigir com `enabled = observacao.isNotBlank()`; rótulo "Observação (obrigatória para corrigir)"; validar também no Repository (ver 6.4). |
| D-02 | 🔴 | [`DocumentosViewModel.kt:75-92`](DocumentosViewModel.kt#L75) · [`RevisaoScreen.kt:166`](RevisaoScreen.kt#L166) e [`:173`](RevisaoScreen.kt#L173) | Não há estado "salvando": os botões só dependem do `bloqueio`. Dois toques em "Enviar arquivo" geram **documentos duplicados**; dois toques em Confirmar gravam duas vezes e **sobrescrevem `confirmadoEm`**. | Flag `salvando`; botões desabilitados com indicador enquanto grava (**RNF-03**). |
| D-03 | 🔴 | [`DocumentosScreen.kt:135`](DocumentosScreen.kt#L135) | `tirarFoto.launch(null)` sem tratamento: em aparelho **sem app de câmera** (alguns emuladores, perfis de trabalho), o Android lança `ActivityNotFoundException` e **o app fecha**. | `try { tirarFoto.launch(null) } catch (e: ActivityNotFoundException) { vm.avisar("Não encontramos a câmera…") }`. |
| D-04 | 🟠 | [`DocumentosScreen.kt:74`](DocumentosScreen.kt#L74) · [`DocumentosViewModel.kt:118`](DocumentosViewModel.kt#L118) | A foto não é reduzida e o limite é **900 KB** (a v1.6 pede máx. 1280 px e **700 KB**). A mensagem ("mais luz ou mais perto") é diferente da padrão e orienta errado: chegar mais perto não diminui o arquivo. | Usar `fotoParaBase64` (ver 6.2) e a mensagem padrão "A foto é grande demais. Tente de novo com menos zoom ou mais longe." |
| D-05 | 🟠 | [`RevisaoScreen.kt:38`](RevisaoScreen.kt#L38) | As observações ficam num `remember { mutableStateMapOf() }`: **girar o celular apaga** o que o médico escreveu. | Guardar as observações na `RevisaoViewModel` (ou em `rememberSaveable`). |
| D-06 | 🟠 | `RevisaoScreen` (card do documento) | Não existe **"Ver no documento"** (RF-23): o médico confirma o resumo de uma foto **sem conseguir ver a foto**. | `FotoDoDocumento` com Coil (ver 6.3). |
| D-07 | 🟠 | [`DocumentosScreen.kt:205`](DocumentosScreen.kt#L205) | Com o documento pendente, o paciente vê "O resumo fica disponível depois que o médico confirmar". A v1.6 pede **"Aguardando revisão do Dr. X"** (RN-05), com o nome do médico. | Buscar o nome do médico pela ViewModel e mostrar o texto da v1.6 (ver 6.5). |
| D-08 | 🟡 | [`DocumentosViewModel.kt:92`](DocumentosViewModel.kt#L92) | Sem internet, o `add()` fica na fila do Firestore: **não aparece nem sucesso nem erro**, e o paciente toca de novo. | `withTimeout(10_000)` + "Sem conexão…" (o documento continua na fila e sobe quando a internet voltar). |
| D-09 | 🟡 | [`DocumentosViewModel.kt:99`](DocumentosViewModel.kt#L99) · [`RevisaoViewModel.kt:84`](RevisaoViewModel.kt#L84) | "Documento registrado!" fica na tela até sair dela, e "Resumo confirmado." **nunca some**. | Mostrar num Snackbar (some sozinho) e limpar a mensagem no estado depois de exibida. |
| D-10 | 🟡 | [`DocumentosScreen.kt:92`](DocumentosScreen.kt#L92) e [`:164`](DocumentosScreen.kt#L164) · [`RevisaoScreen.kt:40`](RevisaoScreen.kt#L40) | O cabeçalho e os botões ficam fixos, e só a lista rola: com o celular **deitado** sobra pouco espaço para os documentos. As duas telas abrem um `Scaffold` dentro do `Scaffold` da navegação, o que costuma dar **espaço em dobro** em cima e embaixo (confira no aparelho). | Tudo dentro de um único `LazyColumn` (cabeçalho como `item {}`); trocar o `Scaffold` interno por `Box`/`Column` (**RNF-04**). |
| D-11 | 🟡 | [`RevisaoViewModel.kt:40`](RevisaoViewModel.kt#L40) · [`DocumentosViewModel.kt:55`](DocumentosViewModel.kt#L55) | A Revisão não confere se o usuário é médico. O `catch (e: Exception)` do init também captura o cancelamento da coroutine. | Conferir `perfil == "medico"`; com `runCatching`/`Result` do Repository o `catch` genérico sai. |
| D-12 | 🟡 | `DocumentosScreen` / `RevisaoScreen` | O visual ainda é o Material padrão (card cinza, sem etiqueta colorida de status, sem destaque do documento mais recente) e **não segue o protótipo**. | Seguir o protótipo: card do mais recente em destaque, etiqueta de status colorida, cores do `theme/`. |


---

## 4. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-15 | Selecionar PDF ou imagem; gravar nome, tipo e data (sem enviar o arquivo) | Must |
| RF-16 | Tirar foto; reduzida a 1280 px / JPEG 70%; se ≤ 700 KB, salvar em Base64 | Should |
| RF-17 | Listar documentos do mais recente ao mais antigo | Must |
| RF-18 | Mostrar o resumo do Goianinho com a fonte de cada ponto | Must |
| RF-19 | Mostrar status: Aguardando médico, Confirmado, Corrigido ou Arquivado | Must |
| RF-20 | Médico vê a fila com status "Aguardando médico" | Must |
| RF-21 | Médico confirma ou corrige, com observação | Must |
| RF-22 | Ao confirmar, registrar data, médico e apoio de IA | Must |
| RF-23 | "Ver no documento" mostra a foto (Coil) ou os dados | Could |

- **RN-05:** o resumo só vale para o paciente depois da confirmação do médico.
- **RN-06:** todo documento confirmado guarda `confirmadoPor`, `confirmadoEm` e `apoioIA = true` (Resolução CFM 2.454/2026).
- **RN-07:** o médico só vê documentos de pacientes vinculados a ele.
- **RN-13 (nova):** corrigir um resumo **exige** observação do médico; o botão Corrigir fica desabilitado com o campo vazio.

## 5. Dados no Firestore

```kotlin
// data/model/Documento.kt (sem mudanças na v1.6)
data class Documento(
    @DocumentId val id: String = "",
    val pacienteId: String = "",
    val medicoId: String = "",
    val nome: String = "",
    val tipo: String = "",                     // "pdf" ou "imagem"
    @ServerTimestamp val enviadoEm: Timestamp? = null,
    val imagemBase64: String = "",             // opcional, ≤ 700 KB de JPEG
    val resumo: List<PontoResumo> = emptyList(),
    val status: String = StatusDocumento.AGUARDANDO_MEDICO,
    val observacao: String = "",
    val confirmadoPor: String = "",
    val confirmadoEm: Timestamp? = null,
    val apoioIA: Boolean = false
)
```

> **Use sempre `StatusDocumento.*`.** O Início (Módulo 1), o Chat (Módulo 2) e a Agenda do dia (Módulo 4) filtram por esses textos.

---

## 6. Como implementar (v1.6)

### 6.1. Repository — `Flow` + `Result`

```kotlin
class DocumentoRepository(private val db: FirebaseFirestore) {
    private val col = db.collection(COL_DOCUMENTOS)

    fun ouvirDoPaciente(pacienteId: String): Flow<List<Documento>> = callbackFlow {
        val registro = col.whereEqualTo("pacienteId", pacienteId)
            .orderBy("enviadoEm", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, erro ->
                if (erro != null) { close(erro); return@addSnapshotListener }
                trySend(snap?.toObjects(Documento::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    fun ouvirPendentes(medicoId: String): Flow<List<Documento>> = callbackFlow {
        val registro = col.whereEqualTo("medicoId", medicoId)                 // RN-07
            .whereEqualTo("status", StatusDocumento.AGUARDANDO_MEDICO)
            .addSnapshotListener { snap, erro ->
                if (erro != null) { close(erro); return@addSnapshotListener }
                trySend(snap?.toObjects(Documento::class.java).orEmpty())
            }
        awaitClose { registro.remove() }
    }

    suspend fun registrar(doc: Documento): Result<Unit> = runCatching {
        withTimeout(10_000) {
            col.add(doc.copy(resumo = RESUMO_SIMULADO, status = StatusDocumento.AGUARDANDO_MEDICO)).await()
        }
    }

    suspend fun confirmar(id: String, medicoId: String, observacao: String) =
        decidir(id, StatusDocumento.CONFIRMADO, medicoId, observacao)

    suspend fun corrigir(id: String, medicoId: String, observacao: String): Result<Unit> {
        if (observacao.isBlank()) return Result.failure(IllegalArgumentException("Observação obrigatória"))  // RN-13
        return decidir(id, StatusDocumento.CORRIGIDO, medicoId, observacao)
    }

    private suspend fun decidir(id: String, status: String, medicoId: String, obs: String): Result<Unit> =
        runCatching {
            withTimeout(10_000) {
                col.document(id).update(
                    mapOf(
                        "status" to status, "observacao" to obs,
                        "confirmadoPor" to medicoId,
                        "confirmadoEm" to FieldValue.serverTimestamp(),
                        "apoioIA" to true                                     // RN-06
                    )
                ).await()
            }
        }
}
```

- `ouvirDoPaciente` e `ouvirPendentes` também são usadas pelo **Início** (Módulo 1) e pela **Agenda do dia** (Módulo 4). **Suba o Repository cedo, num PR pequeno.**
- O resumo simulado (`RESUMO_SIMULADO`) continua com 3 pontos e fontes fictícias.

### 6.2. Foto: reduzir para 1280 px e limitar a 700 KB (RF-16)

```kotlin
// Fora da tela (ex.: ui/documentos/Foto.kt) — função pura, fácil de testar
private const val LADO_MAX = 1280
private const val LIMITE_BYTES = 700_000

fun fotoParaBase64(original: Bitmap): String? {
    val escala = minOf(1f, LADO_MAX.toFloat() / maxOf(original.width, original.height))
    val reduzida = if (escala < 1f)
        Bitmap.createScaledBitmap(original, (original.width * escala).toInt(), (original.height * escala).toInt(), true)
    else original
    val bytes = ByteArrayOutputStream().also { reduzida.compress(Bitmap.CompressFormat.JPEG, 70, it) }.toByteArray()
    return if (bytes.size > LIMITE_BYTES) null else Base64.encodeToString(bytes, Base64.NO_WRAP)
}

// Na tela
val tirarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
    if (bmp != null) {
        val base64 = fotoParaBase64(bmp)
        if (base64 == null) vm.avisarFotoGrande()          // "A foto é grande demais…"
        else vm.registrarDocumento("Foto do documento", "imagem", base64)
    }
}
```

### 6.3. "Ver no documento" com Coil (RF-23)

```kotlin
@Composable
fun FotoDoDocumento(doc: Documento) {
    if (doc.imagemBase64.isBlank()) {
        // Sem foto: mostra os dados (nome, tipo, data)
        Text("${doc.nome} · ${doc.tipo.uppercase()}")
        return
    }
    val bytes = remember(doc.id) { Base64.decode(doc.imagemBase64, Base64.DEFAULT) }
    AsyncImage(
        model = bytes,                                    // Coil aceita ByteArray
        contentDescription = "Foto do documento ${doc.nome}",
        placeholder = painterResource(R.drawable.ic_documento),
        error = painterResource(R.drawable.ic_documento),
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)
    )
}
```

### 6.4. Revisão — Corrigir só com observação (RN-13)

```kotlin
var observacao by rememberSaveable { mutableStateOf("") }

OutlinedTextField(
    value = observacao, onValueChange = { observacao = it },
    label = { Text("Observação do médico") },
    supportingText = { Text("Obrigatória para corrigir") },
    modifier = Modifier.fillMaxWidth()
)
Button(
    onClick = { vm.confirmar(doc.id, observacao) },
    enabled = !state.salvando,                                  // RNF-03
    modifier = Modifier.fillMaxWidth()
) { Text("Confirmar resumo") }
OutlinedButton(
    onClick = { vm.corrigir(doc.id, observacao) },
    enabled = !state.salvando && observacao.isNotBlank(),       // RN-13
    modifier = Modifier.fillMaxWidth()
) { Text("Corrigir resumo") }
```

### 6.5. Paciente — "Aguardando revisão do Dr. X" (RN-05)

```kotlin
when (doc.status) {
    StatusDocumento.AGUARDANDO_MEDICO -> Text("Aguardando revisão do ${nomeDoMedico}")
    StatusDocumento.CONFIRMADO, StatusDocumento.CORRIGIDO -> CardResumo(doc.resumo, doc.observacao)
    StatusDocumento.ARQUIVADO -> Text("Este documento foi arquivado.")
}
```

O nome do médico vem do `AuthRepository.buscarUsuario(medicoId)`, pela ViewModel.

---

## 7. O que fica simulado

- A leitura do exame: resumo fixo de 3 pontos, com fontes fictícias.
- O armazenamento do arquivo: só os dados vão para o Firestore (foto pequena em Base64).
- Não use valores clínicos reais em nenhum momento.

## 8. Critérios de aceite

- [ ] **Todos os erros da seção "Erros encontrados" corrigidos (D-01 a D-12)**, cada um no seu commit `fix(...)`
- [ ] Documento selecionado aparece no Firestore e na lista do paciente
- [ ] Documento novo chega na fila do médico como "Aguardando médico"
- [ ] Paciente vê "Aguardando revisão do Dr. X" até o médico decidir (RN-05)
- [ ] Confirmar e Corrigir mudam o status nas duas telas, em tempo real
- [ ] Os campos de registro são gravados ao confirmar (RN-06)
- [ ] Corrigir fica desabilitado sem observação (RN-13)
- [ ] Foto acima de 700 KB é recusada com aviso; nenhum documento passa de 1 MB
- [ ] "Ver no documento" mostra a foto com carregamento e erro tratados
- [ ] Modo avião: mensagem de erro, botões voltam a habilitar e o app não fecha
- [ ] Funciona em 360 dp, deitado e com fonte grande
- [ ] Visual das duas telas igual ao protótipo

## 9. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| UiState Carregando/Sucesso/Erro | [Codelab: Get data from the internet (Mars Photos)](https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet) | Padrão de estado da v1.6 |
| Imagens com Coil | [Codelab: Load and display images from the internet](https://developer.android.com/codelabs/basic-android-kotlin-compose-load-images) | `AsyncImage`, placeholder e erro |
| Injeção manual + Factory | [Codelab: Add repository and Manual DI](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository) | `DocumentoRepository` vindo do `AppContainer` |
| Flow e callbackFlow | [Guia: Kotlin flows on Android](https://developer.android.com/kotlin/flow) | Fila e lista em tempo real |
| Abrir arquivo / câmera | [Guia: Get a result from an activity](https://developer.android.com/training/basics/intents/result) | `OpenDocument`, `TakePicturePreview` |
| Bitmaps grandes | [Guia: Loading large bitmaps efficiently](https://developer.android.com/topic/performance/graphics/load-bitmap) | Reduzir a foto para 1280 px |
| Gravar dados | [Firebase: Add data](https://firebase.google.com/docs/firestore/manage-data/add-data) | `add()`, `update()`, `FieldValue.serverTimestamp()` |
| Tempo real | [Firebase: Get realtime updates](https://firebase.google.com/docs/firestore/query-data/listen) | Fila do médico atualizando sozinha |
| Regras de segurança | [Firebase: Get started with Security Rules](https://firebase.google.com/docs/firestore/security/get-started) | Entender quem cria e quem atualiza |
| Limites do Firestore | [Firebase: Usage and limits](https://firebase.google.com/docs/firestore/quotas) | Limite de 1 MB por documento |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
