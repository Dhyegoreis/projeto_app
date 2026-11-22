# Módulo 1 — Início (parte 2 de 2)

> **Versão v1.6 — Fase 2: backend no Firebase.** A Etapa 1 (telas visuais) está concluída. Este doc diz **o que já existe** e **o que você precisa fazer agora** (seções 2 e 3) e **os erros do código atual que você precisa corrigir** (seção "Erros encontrados"). Leia também os *padrões obrigatórios da fase 2*, iguais para todo o time.

| | |
|---|---|
| **Responsável** | [INTEGRANTE 1] (o mesmo do Login) |
| **Branch** | `feature/login` |
| **Pasta** | `ui/login` e `ui/home` |
| **Telas do protótipo** | 1 · Início do paciente e 1 · Início do médico · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 6 e **Seção 11 (Fase 2 — Backend)** da Documentação v1.6 (`docs/Documentacao-Goianinho-Medicina-v1.6.pdf`) |
| **Etapa atual** | Etapa 2 — Integração com Firebase (⏳ em andamento) |

> **Duas visões, um dono:** você entrega o **Início do paciente** e o **Início do médico**. O módulo só está pronto quando as duas passam nos critérios de aceite.

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

**Você trabalha somente na branch `feature/login`.** Ela é a única branch deste módulo, e é a única em que você pode fazer commit e push.

**A `main` não pode ser alterada por você de maneira alguma.** Isso inclui:
- ❌ fazer commit ou `git push` direto na `main`;
- ❌ fazer merge de qualquer branch na `main`, nem pelo botão do GitHub;
- ❌ usar `git push --force` em qualquer branch;
- ❌ trabalhar, fazer commit ou push na branch de outro integrante;
- ❌ alterar arquivos fora das suas pastas (`ui/login` e `ui/home` e o seu Repository em `data/repository/`) sem combinar com o líder;
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
git checkout feature/login

# Sempre antes de começar: confirmar que está na branch certa
git branch --show-current          # tem que mostrar: feature/login

# Trazer as novidades da main para a sua branch (faça isso ANTES de começar a v1.6)
git pull origin main

# Salvar e enviar o seu trabalho (sempre para a SUA branch)
git add .
git commit -m "feat(modulo): o que você fez"      # bug de Issue: "fix(modulo): ... Corrige #12"
git push origin feature/login
```

Terminou uma etapa? No GitHub, abra **Pull request → base: `main` ← compare: `feature/login`** e avise o líder. **Não clique em "Merge".**

> Se o `git branch --show-current` mostrar `main`, **pare** e rode `git checkout feature/login` antes de qualquer commit.

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

### O que o líder entrega na `main` antes de você começar

```
com/goianinho/medicina/
 ├── GoianinhoApp.kt           (Application: cria o AppContainer uma vez só)
 ├── data/AppContainer.kt      (cria o Firebase e os 4 repositórios)
 ├── data/Constantes.kt        (MEDICO_TESTE_ID e nomes das coleções)
 └── ui/comum/Mensagens.kt     (mensagemDeErro(erro) e os avisos "em breve")
```

Mais: Coil e coroutines no Gradle, campo `donoId` em `Mensagem`, usuário médico de teste e horários de exemplo no Firestore. **Faça `git pull origin main` antes de começar.**

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

O Início é o **painel** de cada perfil: resume o que importa agora e leva às outras abas. Ele **só lê** dados (RN-10). Quem grava consultas e documentos são os Módulos 3 e 4.

**Início do paciente:** saudação com nome e data; card do Goianinho com o campo "Fale ou escreva" e botão de voz (abrem o Chat); chips Marcar consulta, Enviar exame e Tirar dúvida; card da próxima consulta; lista "Acompanhamento" com o status dos documentos; botão **Sair**.

**Início do médico:** saudação com data; três contadores (consultas hoje, para revisar, sem resposta); card da próxima consulta com briefing e os botões Ver briefing / Iniciar; lista "Precisa de você"; campo "Pergunte ao Goianinho"; botão **Sair**.

**Também é seu:** a **barra inferior de 4 abas** (RF-36), usada pelas outras telas.

## 2. Onde estamos (fim da Etapa 1)

| Item | Situação |
|---|---|
| `HomePacienteScreen`, `HomeMedicoScreen`, `HomeViewModel`, `BarraInferior` | ✅ Telas prontas, lendo dados reais pelos repositórios |
| Botão Sair (RF-37) | ✅ Existe nos dois perfis e volta ao Login (`sairDoApp`) — conferir que a pilha é limpa |
| `HomeViewModel` | ⚠️ Usa callbacks `onMudou`/`onErro` e guarda `ListenerRegistration` à mão — migrar para `Flow` + `UiState` |
| `AssumirPacienteViewModel` (médico "assume" paciente) | ❌ **Grava** em `usuarios` de outro usuário: fere a RN-10 e será **bloqueado** pelas regras do Firestore da v1.6 |
| Estado vazio / carregando / erro | ⚠️ Parcial — padronizar com `UiState` |
| Botão de voz e "Iniciar" | ❌ Precisam do aviso "em breve" (RNF-01) |
| Responsividade | ⚠️ Conferir 360 dp, deitado e fonte grande (contadores em `Row` quebram com fonte grande) |

## 3. O que fazer na v1.6 (sua lista de tarefas)

1. **`HomeViewModel` com `Factory`** recebendo `authRepository`, `documentoRepository` e `agendaRepository` do `AppContainer`.
2. **Trocar callbacks por `Flow`:** combine os fluxos dos repositórios (`combine(...)`) num `HomePacienteUiState` / `HomeMedicoUiState` com **Carregando / Sucesso / Erro**. Os `ListenerRegistration` e o `onCleared()` manual saem — o `awaitClose` do Repository cuida disso.
3. **RN-10 (só ler):** remover o fluxo "Assumir paciente" (`AssumirPacienteViewModel` e o diálogo no Início do médico). Na v1.6 o vínculo vem do cadastro (RN-12: todo paciente novo já nasce com `medicoId = MEDICO_TESTE_ID`). Com isso, `listarPacientes()`/`assumirPaciente()` deixam de ser chamados.
4. **Sair (RF-37):** manter `signOut()` + volta ao Login limpando a pilha; conferir que o botão Voltar depois de sair **não** reabre o Início.
5. **Avisos RNF-01:** botão de voz do card do Goianinho → *"O comando de voz chega em breve…"*; "Iniciar" continua abrindo **Chamada em breve** (Módulo 4).
6. **Estado vazio (RNF-05):** sem próxima consulta → "Você não tem consultas marcadas" com atalho para a Agenda; sem documentos → "Você ainda não enviou documentos"; médico sem pendências → "Nada precisa de você agora".
7. **Erro (RNF-02):** se um dos fluxos falhar, mostrar `AvisoErro(mensagemDeErro(e))` no topo com "Tentar de novo"; o resto do painel continua visível.
8. **Responsivo (RNF-04):** `verticalScroll` + `widthIn(max = 600.dp)`; contadores com `Modifier.weight(1f)` e texto que quebra linha (sem altura fixa); área de toque ≥ 48 dp nos chips e botões.
9. **Barra de 4 abas (RF-36):** conferir ícones com `contentDescription` nulo **e** rótulo visível; funciona deitado.

## Erros encontrados no código atual (corrigir nesta versão)

> Análise do líder em 22/11/2026, na branch `feature/chat-ia` (código igual ao da `main`). Os números de linha valem para essa versão; se o arquivo já mudou, procure pelo trecho citado. **Cada erro corrigido = 1 commit** `fix(home): ... (H-XX)`. Gravidade: 🔴 alta (perde dado, fecha o app ou quebra regra de negócio) · 🟠 média (comportamento errado que o usuário percebe) · 🟡 baixa (acabamento, responsividade, código).

| ID | Grav. | Onde | O que acontece | Como corrigir |
|---|---|---|---|---|
| H-01 | 🔴 | [`HomeMedicoScreen.kt:71`](HomeMedicoScreen.kt#L71) e [`:190`](HomeMedicoScreen.kt#L190) · [`AssumirPacienteViewModel.kt`](AssumirPacienteViewModel.kt) · [`AuthRepository.kt:41-49`](../../data/repository/AuthRepository.kt#L41) | O botão "Assumir paciente" **grava** o `medicoId` no `usuarios/{uid}` de outro usuário a partir do Início, o que fere a **RN-10** (o Início só lê). Quando as regras da v1.6 forem publicadas (`usuarios` só é alterado pelo próprio dono), o botão vai dar "Você não tem acesso a esta informação". | Remover o diálogo, o `AssumirPacienteViewModel` e as funções `listarPacientes`/`assumirPaciente`. O vínculo passa a vir do cadastro (RN-12). |
| H-02 | 🟠 | [`HomePacienteScreen.kt:157`](HomePacienteScreen.kt#L157) e [`:195`](HomePacienteScreen.kt#L195) · [`HomeMedicoScreen.kt:179`](HomeMedicoScreen.kt#L179) e [`:203`](HomeMedicoScreen.kt#L203) | **Estado vazio falso** enquanto carrega: aparecem "Olá, " sem nome, "Nenhuma consulta marcada", "Nenhum documento enviado ainda" e os contadores **0**, e um segundo depois os dados reais. O paciente acha que perdeu a consulta. | `UiState` com `Carregando` (ver 6.1): só mostrar o texto de vazio quando a lista chegou vazia (**RNF-03 / RNF-05**). |
| H-03 | 🟠 | [`HomeViewModel.kt:171`](HomeViewModel.kt#L171) · [`DocumentoRepository.kt:74`](../../data/repository/DocumentoRepository.kt#L74) e [`:99`](../../data/repository/DocumentoRepository.kt#L99) | Quando **um** listener falha (ex.: índice não criado), o repositório manda lista vazia **e** o aviso de erro fica fixo para sempre, sem "Tentar de novo". O painel inteiro some até o usuário sair e voltar. | Com `Flow` + `catch`, mostrar `Erro` com botão "Tentar de novo" (`recarregar`). |
| H-04 | 🟠 | [`HomePacienteScreen.kt:62`](HomePacienteScreen.kt#L62) e [`:228`](HomePacienteScreen.kt#L228) · [`HomeMedicoScreen.kt:163`](HomeMedicoScreen.kt#L163) | O "Dr." vem fixo no texto: se o nome cadastrado já começa com "Dr."/"Dra.", aparece **"Dr. Dr. Fulano"** (o Chat já trata isso; o Início não). E o médico vê **"Bom dia"** até às 23h. | Criar `nomeDoMedico(nome)` em `ui/comum` (igual ao `medicoOuGenerico` das `RespostasSimuladas`) e usar Bom dia / Boa tarde / Boa noite conforme a hora. |
| H-05 | 🟡 | [`HomeViewModel.kt:54`](HomeViewModel.kt#L54) e [`:125`](HomeViewModel.kt#L125) | A data e o intervalo "hoje" são calculados **uma vez só**. Com o app aberto depois da meia-noite, o Início mostra a data e as consultas de **ontem**. | Recalcular data e intervalo quando a tela volta ao primeiro plano (ou a cada minuto, no laço que já existe). |
| H-06 | 🟡 | [`HomePacienteScreen.kt:138`](HomePacienteScreen.kt#L138) | Os chips "Marcar consulta" e "Enviar exame" ficam numa `Row` sem quebra de linha: em 360 dp com fonte grande, o segundo chip é **cortado** (**RNF-04**). | `FlowRow` com os três chips. |
| H-07 | 🟡 | [`HomeViewModel.kt:80`](HomeViewModel.kt#L80) | O `medicoId` do paciente é lido uma vez ao abrir. Um paciente vinculado depois continua vendo "sem médico" até reabrir o app. | Com a RN-12 isso deixa de acontecer; se o vínculo mudar, ouvir `usuarios/{uid}` em tempo real. |
| H-08 | 🟡 | [`HomeMedicoScreen.kt:227`](HomeMedicoScreen.kt#L227) | "Ver briefing" sempre abre a lista geral de Revisões, mesmo quando o paciente da vez não tem resumo pendente. | Se não houver pendência, mostrar o briefing sem sair da tela (ou desabilitar o botão). |
| H-09 | 🟡 | Card "Próxima consulta" do paciente | Pode mostrar uma consulta **cancelada** ou que já passou: o erro está no `AgendaRepository.ouvirProximaConsulta` (ver **A-06** em `ui/agenda/doc.md`). | Corrigido pelo Módulo 4. Depois do PR dele, conferir aqui. |


---

## 4. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-31 | Início do paciente: saudação, atalho para o Goianinho e chips | Must |
| RF-32 | Início do paciente: próxima consulta e status dos documentos | Must |
| RF-33 | Início do médico: três contadores do dia | Must |
| RF-34 | Início do médico: próxima consulta com briefing e lista "Precisa de você" | Must |
| RF-35 | Cada card e chip leva à aba correspondente | Should |
| RF-36 | Barra inferior com 4 abas nos dois perfis, começando pelo Início | Must |
| **RF-37 (novo)** | Botão **Sair** no Início dos dois perfis: `signOut()` e volta ao Login, limpando a pilha | Must |

- **RN-10:** o Início só lê. Nada de `set()`, `update()` ou `add()` aqui — nem pelo Repository.

## 5. Dados que você lê (sempre via Repository)

| Dado | Repository (dono) | Função v1.6 |
|---|---|---|
| Nome do usuário | `AuthRepository` (Módulo 1, você) | `suspend buscarUsuario(uid): Result<Usuario>` |
| Documentos do paciente | `DocumentoRepository` (Módulo 3) | `ouvirDoPaciente(pacienteId): Flow<List<Documento>>` |
| Resumos para revisar | `DocumentoRepository` (Módulo 3) | `ouvirPendentes(medicoId): Flow<List<Documento>>` |
| Próxima consulta | `AgendaRepository` (Módulo 4) | `ouvirProximaConsulta(pacienteId): Flow<Consulta?>` |
| Consultas de hoje | `AgendaRepository` (Módulo 4) | `ouvirConsultasDoDia(medicoId, inicio, fim): Flow<List<Consulta>>` |

> **Combinado entre módulos:** enquanto os PRs v1.6 dos Módulos 3 e 4 não entram na `main`, envolva as funções antigas num `callbackFlow` dentro da sua ViewModel **apenas como ponte provisória**, ou use dados fixos. Quando o PR dos colegas entrar, troque pela função nova.

---

## 6. Como implementar (v1.6)

### 6.1. ViewModel do paciente com `combine`

```kotlin
sealed interface HomePacienteUiState {
    data object Carregando : HomePacienteUiState
    data class Sucesso(
        val nome: String,
        val proximaConsulta: Consulta?,
        val documentos: List<Documento>
    ) : HomePacienteUiState
    data class Erro(val mensagem: String) : HomePacienteUiState
}

class HomeViewModel(
    private val authRepo: AuthRepository,
    private val documentoRepo: DocumentoRepository,
    private val agendaRepo: AgendaRepository
) : ViewModel() {

    private val uid = authRepo.usuarioLogadoUid.orEmpty()
    private val recarregar = MutableStateFlow(0)      // "Tentar de novo" reinicia os fluxos

    @OptIn(ExperimentalCoroutinesApi::class)
    val paciente: StateFlow<HomePacienteUiState> = recarregar.flatMapLatest {
        val nome = flow { emit(authRepo.buscarUsuario(uid).getOrNull()?.nome.orEmpty()) }
        combine(
            nome,
            agendaRepo.ouvirProximaConsulta(uid),
            documentoRepo.ouvirDoPaciente(uid).map { it.take(3) }
        ) { n, consulta, docs ->
            HomePacienteUiState.Sucesso(n, consulta, docs) as HomePacienteUiState
        }.catch { e ->
            Log.e("Goianinho", "Início do paciente falhou", e)
            emit(HomePacienteUiState.Erro(mensagemDeErro(e)))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomePacienteUiState.Carregando)

    fun tentarDeNovo() = recarregar.update { it + 1 }

    fun sair() = authRepo.sair()                       // RF-37 (a navegação limpa a pilha)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as GoianinhoApp).container
                HomeViewModel(c.authRepository, c.documentoRepository, c.agendaRepository)
            }
        }
    }
}
```

- **Não existe `Firebase.` nem `set/update` neste arquivo** (RN-10).
- `combine` só emite quando **todos** os fluxos já emitiram uma vez — por isso o estado começa em `Carregando`.
- O médico segue o mesmo modelo: `combine(nome, ouvirPendentes(uid), ouvirConsultasDoDia(uid, inicio, fim))` → contadores, próxima consulta (`consultaDoMomento`, em `ui/comum`) e lista "Precisa de você".

### 6.2. Tela — os três estados

```kotlin
@Composable
fun HomePacienteScreen(
    onAbrirChat: () -> Unit, onAbrirAgenda: () -> Unit,
    onAbrirDocumentos: () -> Unit, onSair: () -> Unit,
    vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val estado by vm.paciente.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 600.dp).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val e = estado) {
                is HomePacienteUiState.Carregando -> CircularProgressIndicator()
                is HomePacienteUiState.Erro -> {
                    AvisoErro(e.mensagem)
                    TextButton(onClick = vm::tentarDeNovo) { Text("Tentar de novo") }
                }
                is HomePacienteUiState.Sucesso -> {
                    Text("Olá, ${e.nome}", style = MaterialTheme.typography.headlineSmall)
                    // ... card do Goianinho e chips (igual ao protótipo) ...
                    e.proximaConsulta?.let { /* card da próxima consulta */ }
                        ?: Text("Você não tem consultas marcadas.")                  // RNF-05
                    if (e.documentos.isEmpty()) Text("Você ainda não enviou documentos.")
                    else e.documentos.forEach { /* item de acompanhamento */ }
                }
            }
            TextButton(onClick = { vm.sair(); onSair() }) { Text("Sair") }        // RF-37
        }
    }
}
```

### 6.3. Barra inferior de 4 abas (RF-36) — sem mudanças

```kotlin
@Composable
fun BarraInferior(abas: List<Aba>, rotaAtual: String?, onAbaClick: (Aba) -> Unit) {
    NavigationBar {
        abas.forEach { aba ->
            NavigationBarItem(
                selected = rotaAtual == aba.rota,
                onClick = { onAbaClick(aba) },
                icon = { Icon(painterResource(aba.icone), contentDescription = null) },
                label = { Text(aba.titulo) }
            )
        }
    }
}
```

- **Paciente:** Início · Assistente · Documentos · Agenda. **Médico:** Início · Goianinho · Revisões · Agenda.

---

## 7. O que fica simulado

- Textos de briefing e o contador "sem resposta" podem ser fixos nesta fase.
- O botão "Iniciar" da teleconsulta abre "Chamada em breve" (Módulo 4) com o aviso RNF-01.
- O botão de voz mostra o aviso "em breve".

## 8. Critérios de aceite

- [ ] **Todos os erros da seção "Erros encontrados" corrigidos (H-01 a H-09)**, cada um no seu commit `fix(...)`
- [ ] Início do paciente mostra o nome, a próxima consulta e os documentos reais do usuário logado
- [ ] Início do médico mostra os contadores e a lista "Precisa de você" com dados reais
- [ ] Ao confirmar um resumo no Módulo 3, o Início muda sozinho (tempo real)
- [ ] Nenhuma gravação no Firestore a partir do Início (RN-10) — fluxo "Assumir paciente" removido
- [ ] Sair volta ao Login e o botão Voltar não reabre o Início (RF-37)
- [ ] Estado vazio com texto explicativo; modo avião mostra erro e o app não fecha
- [ ] Barra de 4 abas funciona nos dois perfis, em pé e deitado
- [ ] Contadores e cards legíveis com fonte grande e em 360 dp
- [ ] Visual das duas telas igual ao protótipo

## 9. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| Arquitetura | [Guide to app architecture](https://developer.android.com/topic/architecture) | ViewModel que só conversa com repositórios |
| UiState Carregando/Sucesso/Erro | [Codelab: Get data from the internet (Mars Photos)](https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet) | Padrão de estado da v1.6 |
| Injeção manual + Factory | [Codelab: Add repository and Manual DI](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository) | `AppContainer` e `viewModelFactory` |
| Flow | [Guia: Kotlin flows on Android](https://developer.android.com/kotlin/flow) | `combine`, `map`, `catch`, `stateIn` |
| StateFlow | [Guia: StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) | `WhileSubscribed(5_000)` |
| Material 3 e cards | [Curso Android Basics with Compose — Unidade 3](https://developer.android.com/courses/android-basics-compose/unit-3) | Cards, cores, tema |
| Barra inferior | [Guia: Navigation bar](https://developer.android.com/develop/ui/compose/components/navigation-bar) | As 4 abas |
| Navegação | [Codelab: Navigate between screens with Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-navigation) | `onAbrirX`, Sair limpando a pilha |
| Telas adaptáveis | [Guia: Build adaptive apps](https://developer.android.com/develop/ui/compose/layouts/adaptive) | Deitado, tablet, `widthIn` |
| Ler em tempo real | [Firebase: Get realtime updates](https://firebase.google.com/docs/firestore/query-data/listen) | Por que o Início atualiza sozinho |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
