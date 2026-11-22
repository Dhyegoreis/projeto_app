# Módulo 4 — Agenda

> **Versão v1.6 — Fase 2: backend no Firebase.** A Etapa 1 (telas visuais) está concluída. Este doc diz **o que já existe** e **o que você precisa fazer agora** (seções 2 e 3) e **os erros do código atual que você precisa corrigir** (seção "Erros encontrados"). Leia também os *padrões obrigatórios da fase 2*, iguais para todo o time.

| | |
|---|---|
| **Responsável** | [INTEGRANTE 4] |
| **Branch** | `feature/agenda` |
| **Pasta** | `ui/agenda` |
| **Telas do protótipo** | 4 · Agenda (paciente) e 4 · Agenda do dia (médico) · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 9 e **Seção 11 (Fase 2 — Backend)** da Documentação v1.6 (`docs/Documentacao-Goianinho-Medicina-v1.6.pdf`) |
| **Etapa atual** | Etapa 2 — Integração com Firebase (⏳ em andamento) |

> **Duas visões, um dono:** você entrega a **Agenda do paciente** e a **Agenda do dia** do médico. O módulo só está pronto quando as duas passam nos critérios de aceite.

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

**Você trabalha somente na branch `feature/agenda`.** Ela é a única branch deste módulo, e é a única em que você pode fazer commit e push.

**A `main` não pode ser alterada por você de maneira alguma.** Isso inclui:
- ❌ fazer commit ou `git push` direto na `main`;
- ❌ fazer merge de qualquer branch na `main`, nem pelo botão do GitHub;
- ❌ usar `git push --force` em qualquer branch;
- ❌ trabalhar, fazer commit ou push na branch de outro integrante;
- ❌ alterar arquivos fora das suas pastas (`ui/agenda` e o seu Repository em `data/repository/`) sem combinar com o líder;
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
git checkout feature/agenda

# Sempre antes de começar: confirmar que está na branch certa
git branch --show-current          # tem que mostrar: feature/agenda

# Trazer as novidades da main para a sua branch (faça isso ANTES de começar a v1.6)
git pull origin main

# Salvar e enviar o seu trabalho (sempre para a SUA branch)
git add .
git commit -m "feat(modulo): o que você fez"      # bug de Issue: "fix(modulo): ... Corrige #12"
git push origin feature/agenda
```

Terminou uma etapa? No GitHub, abra **Pull request → base: `main` ← compare: `feature/agenda`** e avise o líder. **Não clique em "Merge".**

> Se o `git branch --show-current` mostrar `main`, **pare** e rode `git checkout feature/agenda` antes de qualquer commit.

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

> **Módulo 4 (Agenda):** a classe `HorarioOcupadoException` **já existe** em `ui/comum/Mensagens.kt` e `mensagemDeErro` já a reconhece (inclusive dentro de `cause`). Não crie a classe de novo: só lance-a na transação.

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

O paciente marca e acompanha consultas; o médico vê o dia com um briefing pronto de cada paciente.

**Paciente:** card da próxima consulta com "Entrar na chamada"; seletor de dias; grade de horários; botão de confirmar que mostra a escolha ("Confirmar Qui, 24/09 às 09:30"); dica para marcar falando com o Goianinho.

**Médico:** card do paciente atual com o briefing do Goianinho e os botões "Ver resumo" e "Iniciar"; lista das próximas consultas com status do resumo e dica de engajamento.

## 2. Onde estamos (fim da Etapa 1)

| Item | Situação |
|---|---|
| `AgendaPacienteScreen`, `AgendaMedicoScreen`, `ChamadaEmBreveScreen`, `AgendaViewModel` | ✅ Telas prontas, lendo e gravando no Firestore |
| Reserva com transação (RN-08) e mensagem de horário ocupado | ✅ |
| Horários que já passaram somem da grade (RN-14) | ✅ Filtrado na ViewModel e reaplicado a cada minuto — manter |
| Status do resumo vindo do Módulo 3 (RF-28) | ✅ |
| `AgendaRepository` | ⚠️ Usa `Firebase.firestore`, callbacks e `ListenerRegistration` — migrar para `Flow` + `Result` |
| Mensagens de erro | ⚠️ Vêm de `traduzirErroFirestore` — passam a vir de `mensagemDeErro` |
| "Entrar na chamada" / "Iniciar" | ⚠️ Abrem a Chamada em breve — falta o texto padrão do aviso RNF-01 |
| Botão Confirmar durante a reserva | ⚠️ Conferir que fica desabilitado (clique duplo = duas consultas) |
| Responsividade | ⚠️ Grade com `heightIn(max = 200.dp)` fixa; conferir deitado e fonte grande |

## 3. O que fazer na v1.6 (sua lista de tarefas)

1. **Repository com injeção:** `AgendaRepository(db)` vindo do `AppContainer`; `AgendaViewModel` com `Factory`.
2. **Listas como `Flow`:** `ouvirHorariosLivres(medicoId, data)`, `ouvirConsultasDoDia(medicoId, inicio, fim)` e `ouvirProximaConsulta(pacienteId)` passam a devolver `Flow`. Saem os parâmetros `onErro`/`onMudou`.
3. **Reservar como `suspend ... : Result<Unit>`:** mesma transação (confere `livre == true`, marca `livre = false` e cria a `consulta`, tudo junto). Conflito → *"Esse horário acabou de ser reservado. Escolha outro."*
4. **Horários livres só no futuro (RN-14):** manter o filtro de hora de hoje e **não mostrar dias passados** no seletor.
5. **Próxima consulta:** em tempo real, no topo da Agenda **e** no Início (o Módulo 1 consome a sua função).
6. **Agenda do médico:** consultas do dia em ordem de hora, com status do resumo vindo do `DocumentoRepository.ouvirPendentes` (Módulo 3) — combinar os dois `Flow` com `combine`.
7. **Entrar na chamada / Iniciar (RNF-01):** continua abrindo **Chamada em breve**, que mostra *"A videochamada ainda não está disponível nesta versão."*
8. **Carregamento (RNF-03):** botão Confirmar desabilitado com indicador enquanto a transação roda; grade não aceita outro toque durante a reserva.
9. **Estado vazio (RNF-05):** dia sem horários → *"Sem horários livres neste dia. Escolha outro dia."*; médico sem consultas → *"Nenhuma consulta hoje."*
10. **Responsivo (RNF-04):** troque a grade de altura fixa por `FlowRow` (ou grade com nº de colunas pelo espaço: `GridCells.Adaptive(minSize = 96.dp)`) dentro de uma tela com scroll; `widthIn(max = 600.dp)` deitado.

> **Regras do Firestore (seção 11):** em `horarios` o app só pode mudar `livre` de `true` para `false`, e só o paciente vinculado ao médico dono do horário pode fazer isso (RN-08); `consultas` só pode ser criada com `pacienteId == uid` e só é lida por quem é o paciente ou o médico dela. Por isso **toda** consulta precisa filtrar por `pacienteId` ou `medicoId`.

## Erros encontrados no código atual (corrigir nesta versão)

> Análise do líder em 22/11/2026, na branch `feature/chat-ia` (código igual ao da `main`). Os números de linha valem para essa versão; se o arquivo já mudou, procure pelo trecho citado. **Cada erro corrigido = 1 commit** `fix(agenda): ... (A-XX)`. Gravidade: 🔴 alta (perde dado, fecha o app ou quebra regra de negócio) · 🟠 média (comportamento errado que o usuário percebe) · 🟡 baixa (acabamento, responsividade, código).

| ID | Grav. | Onde | O que acontece | Como corrigir |
|---|---|---|---|---|
| A-01 | 🔴 | [`AgendaViewModel.kt:225-226`](AgendaViewModel.kt#L225) e [`:256`](AgendaViewModel.kt#L256) | **Consulta marcada no dia errado.** Ao trocar de dia, a seleção é limpa, mas a grade do dia **anterior** continua na tela até o novo snapshot chegar. Se o paciente tocar num horário nesse intervalo e confirmar, a consulta é criada com a data do dia **novo** (`estado.diaSelecionado`), enquanto o horário ocupado no Firestore é o do dia **antigo**. | Em `selecionarDia`, zerar `horarios` (mostrar Carregando) e, na reserva, usar a data e a hora **do próprio `Horario`** (guardar `data` no `HorarioUi`). |
| A-02 | 🔴 | [`AgendaViewModel.kt:475-476`](AgendaViewModel.kt#L475) | `SimpleDateFormat.parse` **lança `ParseException`** (não devolve `null`). Um horário cadastrado à mão no console (RN-09) com formato errado, como "9h30", vazio ou data inválida, **fecha o app** ao abrir a Agenda. | `runCatching { FORMATO_DATA_HORA.parse(...) }.getOrNull()` e ignorar o horário inválido com `Log.e`. |
| A-03 | 🟠 | [`AgendaPacienteScreen.kt:246-250`](AgendaPacienteScreen.kt#L246) | A grade (`LazyVerticalGrid`) tem altura fixa de **112 dp**, o que mostra só 2 linhas (6 horários). O resto fica escondido numa rolagem **dentro** de uma tela que também rola, e o paciente acha que não há outros horários. Com fonte grande, os chips são cortados. | `FlowRow` sem altura fixa (ver 6.4). |
| A-04 | 🟠 | [`AgendaPacienteScreen.kt:103`](AgendaPacienteScreen.kt#L103) · [`AgendaMedicoScreen.kt:65`](AgendaMedicoScreen.kt#L65) e [`:84`](AgendaMedicoScreen.kt#L84) | **Estado vazio falso** enquanto carrega: "Nenhum horário livre nesse dia.", "Nenhuma consulta hoje." e "0 consultas" aparecem antes dos dados chegarem. | `UiState` com `Carregando` (**RNF-03 / RNF-05**). |
| A-05 | 🟠 | [`AgendaViewModel.kt:261-262`](AgendaViewModel.kt#L261) | Depois de reservar, a seleção some e **nenhuma mensagem aparece**: o paciente não sabe se a consulta foi marcada. | Snackbar "Consulta marcada para Qui, 24/10 às 09:30". |
| A-06 | 🟠 | [`AgendaRepository.kt:111-118`](../../data/repository/AgendaRepository.kt#L111) | `ouvirProximaConsulta` usa `Timestamp.now()` **fixado** quando o listener nasce e **não filtra o status**: uma consulta **cancelada** aparece como "próxima", e uma que já passou continua no card até reabrir a tela. Afeta também o **Início** e o **Chat**. | Filtrar `status != "cancelada"` na ViewModel e reaplicar `consultaDoMomento` a cada minuto, como já é feito na agenda do médico. |
| A-07 | 🟡 | [`AgendaViewModel.kt:159`](AgendaViewModel.kt#L159) | Um erro no listener de horários vira `bloqueio` **permanente**: a área de marcação some e não volta nem trocando de dia. | Estado `Erro` só da grade, com "Tentar de novo"; limpar ao trocar de dia. |
| A-08 | 🟡 | [`AgendaPacienteScreen.kt:120`](AgendaPacienteScreen.kt#L120), [`:179`](AgendaPacienteScreen.kt#L179) e [`:256`](AgendaPacienteScreen.kt#L256) | Botões e chips com **altura fixa** (`height(52.dp)`, `height(48.dp)`): com fonte grande, "Confirmar Qui, 24 às 09:30" fica **cortado** (**RNF-04**). | `heightIn(min = ...)`. |
| A-09 | 🟡 | [`AgendaPacienteScreen.kt:171`](AgendaPacienteScreen.kt#L171) | "Dr. $medicoNome": mostra **"Dr. "** vazio enquanto o nome carrega, e "Dr. Dr. Fulano" se o nome já tem o título. | Helper `nomeDoMedico` em `ui/comum` (combinar com o Módulo 1, ver H-04). |
| A-10 | 🟡 | [`AgendaMedicoScreen.kt:65`](AgendaMedicoScreen.kt#L65) | Plural fixo: "**1 consultas**". | `if (n == 1) "1 consulta" else "$n consultas"`. |
| A-11 | 🟡 | [`AgendaViewModel.kt:126`](AgendaViewModel.kt#L126) e [`:328`](AgendaViewModel.kt#L328) | Os dias do seletor e o intervalo "hoje" do médico são calculados uma vez: depois da meia-noite, o primeiro dia do seletor já passou e a agenda do médico mostra o dia anterior. | Recalcular ao voltar para a tela (mesma correção do H-05). |
| A-12 | 🟡 | [`ChamadaEmBreveScreen.kt:43`](ChamadaEmBreveScreen.kt#L43) | O texto difere do aviso padrão da v1.6. | Usar o aviso de `Mensagens.kt`: "A videochamada ainda não está disponível nesta versão." |


---

## 4. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-24 | Paciente vê os horários livres do médico por dia | Must |
| RF-25 | Paciente reserva um horário, que fica indisponível para os outros | Must |
| RF-26 | Próxima consulta do paciente no topo | Must |
| RF-27 | Médico vê as consultas do dia em ordem de horário | Must |
| RF-28 | Cada consulta do médico mostra o status do resumo (Módulo 3) | Should |
| RF-29 | Cada consulta do médico mostra uma dica de engajamento | Could |
| RF-30 | "Entrar na chamada" e "Iniciar" abrem a videochamada | Could (nesta fase: Chamada em breve) |

- **RN-08:** horário reservado não pode ser reservado de novo — **transação** do Firestore.
- **RN-09:** nesta fase, os horários livres são cadastrados à mão no console do Firestore (o líder cria horários de exemplo para o médico de teste).
- **RN-14 (nova):** o paciente só vê e reserva horários **a partir de agora**; horário que já passou não aparece.

## 5. Dados no Firestore

```kotlin
// data/model/Horario.kt e Consulta.kt (sem mudanças na v1.6)
data class Horario(
    @DocumentId val id: String = "",
    val medicoId: String = "",
    val data: String = "",       // "2026-09-24"
    val hora: String = "",       // "09:30"
    val livre: Boolean = true
)

data class Consulta(
    @DocumentId val id: String = "",
    val pacienteId: String = "",
    val medicoId: String = "",
    val dataHora: Timestamp? = null,
    val tipo: String = "teleconsulta",
    val status: String = "agendada",    // agendada | realizada | cancelada
    val motivo: String = ""
)
```

O `medicoId` do paciente vem de `usuarios/{uid}` — na v1.6 é sempre o `MEDICO_TESTE_ID` (RN-12).

---

## 6. Como implementar (v1.6)

### 6.1. Repository — `Flow` + `Result`

```kotlin
class AgendaRepository(private val db: FirebaseFirestore) {

    fun ouvirHorariosLivres(medicoId: String, data: String): Flow<List<Horario>> = callbackFlow {
        val registro = db.collection(COL_HORARIOS)
            .whereEqualTo("medicoId", medicoId)
            .whereEqualTo("data", data)
            .whereEqualTo("livre", true)
            .addSnapshotListener { snap, erro ->
                if (erro != null) { close(erro); return@addSnapshotListener }
                trySend(snap?.toObjects(Horario::class.java).orEmpty().sortedBy { it.hora })
            }
        awaitClose { registro.remove() }
    }

    // RF-25 e RN-08
    suspend fun reservar(horario: Horario, pacienteId: String, dataHora: Timestamp): Result<Unit> =
        runCatching {
            withTimeout(10_000) {
                val refHorario = db.collection(COL_HORARIOS).document(horario.id)
                val refConsulta = db.collection(COL_CONSULTAS).document()
                db.runTransaction { tx ->
                    if (tx.get(refHorario).getBoolean("livre") != true) throw HorarioOcupadoException()
                    tx.update(refHorario, "livre", false)
                    tx.set(refConsulta, Consulta(pacienteId = pacienteId, medicoId = horario.medicoId, dataHora = dataHora))
                }.await()
            }
        }

    // ouvirConsultasDoDia(medicoId, inicio, fim) e ouvirProximaConsulta(pacienteId):
    // mesmas consultas da Etapa 1, no formato callbackFlow acima.
}

// HorarioOcupadoException já existe em ui/comum/Mensagens.kt (importe de lá; não declare de novo)
```

- `mensagemDeErro(e)` reconhece `HorarioOcupadoException` (procurando também em `e.cause`, porque a transação embrulha o erro) e devolve *"Esse horário acabou de ser reservado. Escolha outro."* — a classe e esse reconhecimento já estão em `ui/comum/Mensagens.kt`, mantido pelo líder.
- `ouvirConsultasDoDia` e `ouvirProximaConsulta` são usadas pelo **Início** (Módulo 1). **Suba o Repository cedo, num PR pequeno.**

### 6.2. ViewModel — reserva sem clique duplo e RN-14

```kotlin
fun confirmarReserva() {
    val s = _paciente.value
    val horario = s.horarioSelecionado ?: return
    if (s.reservando) return                                       // RNF-03
    viewModelScope.launch {
        _paciente.update { it.copy(reservando = true, erro = null) }
        repo.reservar(horario, uid, horarioParaTimestamp(s.dia, horario.hora))
            .onSuccess { _paciente.update { it.copy(reservando = false, horarioSelecionado = null, aviso = "Consulta marcada!") } }
            .onFailure { e ->
                Log.e("Goianinho", "reservar falhou", e)
                _paciente.update { it.copy(reservando = false, horarioSelecionado = null, erro = mensagemDeErro(e)) }
            }
    }
}

// RN-14: dias do seletor começam hoje; horários de hoje só a partir de agora
val dias = (0 until 14).map { LocalDate.now().plusDays(it.toLong()) }
```

### 6.3. Agenda do médico com status do resumo (RF-27, RF-28)

```kotlin
val medico: StateFlow<AgendaMedicoUiState> = combine(
    agendaRepo.ouvirConsultasDoDia(uid, inicio, fim),
    documentoRepo.ouvirPendentes(uid)
) { consultas, pendentes ->
    val comPendencia = pendentes.map { it.pacienteId }.toSet()
    AgendaMedicoUiState.Sucesso(
        consultas.filter { it.status != "cancelada" }.map { c ->
            ConsultaUi(c, resumoPendente = c.pacienteId in comPendencia)
        }
    ) as AgendaMedicoUiState
}.catch { emit(AgendaMedicoUiState.Erro(mensagemDeErro(it))) }
 .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AgendaMedicoUiState.Carregando)
```

### 6.4. Grade de horários responsiva

```kotlin
@OptIn(ExperimentalLayoutApi::class)
FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth()
) {
    horarios.forEach { h ->
        FilterChip(
            selected = h.id == selecionado?.id,
            onClick = { onHorarioClick(h) },
            enabled = !reservando,
            label = { Text(h.hora) },
            modifier = Modifier.heightIn(min = 48.dp)          // área de toque
        )
    }
}
```

`FlowRow` quebra a linha sozinho conforme a largura, sem altura fixa — funciona em 360 dp, deitado e com fonte grande, dentro de uma tela com `verticalScroll`.

---

## 7. O que fica simulado

- A videochamada: "Entrar na chamada" e "Iniciar" abrem a `ChamadaEmBreveScreen` com o aviso RNF-01.
- As dicas de engajamento ("prefere ligação", "costuma confirmar tarde"): texto fixo por paciente.
- O briefing do Goianinho no card do paciente atual: texto fixo, com o status real do resumo.

## 8. Critérios de aceite

- [ ] **Todos os erros da seção "Erros encontrados" corrigidos (A-01 a A-12)**, cada um no seu commit `fix(...)`
- [ ] Horário reservado some da grade para outros pacientes, em tempo real
- [ ] Dois toques simultâneos no mesmo horário: só um consegue; o outro vê a mensagem de horário reservado
- [ ] Clique duplo em Confirmar não cria duas consultas
- [ ] Horário que já passou e dias passados não aparecem (RN-14)
- [ ] Consulta reservada aparece na Agenda do dia do médico e no Início do paciente
- [ ] Status do resumo bate com o que está no Módulo 3
- [ ] "Entrar na chamada" / "Iniciar" mostram o aviso da videochamada
- [ ] Modo avião: mensagem "Sem conexão…" e o app não fecha
- [ ] Grade funciona em 360 dp, deitado e com fonte grande
- [ ] Visual das duas telas igual ao protótipo

## 9. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| UiState Carregando/Sucesso/Erro | [Codelab: Get data from the internet (Mars Photos)](https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet) | Padrão de estado da v1.6 |
| Injeção manual + Factory | [Codelab: Add repository and Manual DI](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository) | `AgendaRepository` vindo do `AppContainer` |
| Flow | [Guia: Kotlin flows on Android](https://developer.android.com/kotlin/flow) | `callbackFlow`, `combine` com o Módulo 3 |
| Flow layouts | [Guia: Flow layouts](https://developer.android.com/develop/ui/compose/layouts/flow) | `FlowRow` na grade de horários |
| Listas e grades | [Guia: Lists and grids](https://developer.android.com/develop/ui/compose/lists) | `LazyRow` dos dias, `LazyColumn` das consultas |
| Chips | [Guia: Chip](https://developer.android.com/develop/ui/compose/components/chip) | `FilterChip` de dia e horário |
| Transações | [Firebase: Transactions and batched writes](https://firebase.google.com/docs/firestore/manage-data/transactions) | Reserva sem conflito (RN-08) |
| Filtros por intervalo | [Firebase: Perform simple and compound queries](https://firebase.google.com/docs/firestore/query-data/queries) | Consultas do dia |
| Tempo real | [Firebase: Get realtime updates](https://firebase.google.com/docs/firestore/query-data/listen) | Grade e agenda atualizando sozinhas |
| Regras de segurança | [Firebase: Get started with Security Rules](https://firebase.google.com/docs/firestore/security/get-started) | Por que toda consulta filtra por `pacienteId`/`medicoId` |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
