# Módulo 1 — Login (parte 1 de 2)

> **Versão v1.6 — Fase 2: backend no Firebase.** A Etapa 1 (telas visuais) está concluída. Este doc diz **o que já existe** e **o que você precisa fazer agora** (seções 2 e 3) e **os erros do código atual que você precisa corrigir** (seção "Erros encontrados"). Leia também os *padrões obrigatórios da fase 2*, iguais para todo o time.

| | |
|---|---|
| **Responsável** | [INTEGRANTE 1] |
| **Branch** | `feature/login` |
| **Pasta** | `ui/login` (este doc) e `ui/home` (ver `ui/home/doc.md`) |
| **Telas do protótipo** | 1 · Login · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 6 e **Seção 11 (Fase 2 — Backend)** da Documentação v1.6 (`docs/Documentacao-Goianinho-Medicina-v1.6.pdf`) |
| **Etapa atual** | Etapa 2 — Integração com Firebase (⏳ em andamento) |

> **Regra do time:** o Módulo 1 entrega **as duas visões**. O Login é compartilhado (serve aos dois perfis) e o Início tem duas versões (ver `ui/home/doc.md`). **Na v1.6 o Módulo 1 entra primeiro na `main`:** os outros módulos dependem do login funcional.

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
- ❌ alterar arquivos fora das suas pastas (`ui/login` (este doc) e `ui/home` (ver `ui/home/doc.md`) e o seu Repository em `data/repository/`) sem combinar com o líder;
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

## 1. O que esta tela faz

É a porta de entrada do app. Identifica o usuário, descobre se ele é **paciente** ou **médico** e manda cada um para o seu Início.

**Elementos (iguais ao protótipo):** seletor Paciente/Médico, campos de e-mail e senha, link "Esqueci minha senha", botão Entrar, botão "Continuar com Google" e link "Criar conta".

**Na v1.6 o objetivo é: login funcional de verdade, não só visual.** O Módulo 1 entra **primeiro** na `main`, porque os Módulos 2, 3 e 4 dependem de usuário logado.

## 2. Onde estamos (fim da Etapa 1)

| Item | Situação |
|---|---|
| `LoginScreen`, `LoginViewModel`, `CadastroScreen`, `CadastroViewModel`, `SeletorPerfil` | ✅ Telas prontas e ligadas ao `AuthRepository` |
| Entrar / Criar conta / Esqueci a senha | ✅ Funcionam, com `suspend` + `await()` |
| `AuthRepository` | ⚠️ Cria `FirebaseAuth`/`FirebaseFirestore` com `getInstance()` e **lança** exceção para a ViewModel (`!!` e `throw`) |
| Mensagens de erro | ⚠️ Vêm de `traduzirErroAuth` — passam a vir de `mensagemDeErro` |
| Manter logado (RF-07) | ✅ Funciona pelo `LoginViewModel.verificarSessao()` (o Login mostra carregamento e pula para o Início). ⚠️ Falha sem aviso (ver L-04) |
| Perfil divergente do seletor | ❌ Não avisa "Sua conta é de médico/paciente" |
| Criar conta | ❌ Não grava `medicoId` nem `criadoEm`; se a gravação em `usuarios` falhar, sobra conta pela metade no Auth (RN-11) |
| Validação | ⚠️ Não valida formato do e-mail e mostra o erro num texto só, não embaixo do campo |
| Botão "Continuar com Google" | ❌ Não faz nada (viola RNF-01) |
| Responsividade | ❌ Sem `imePadding`, sem `widthIn(max = 600.dp)` |

## 3. O que fazer na v1.6 (sua lista de tarefas)

1. **Repository com injeção:** `AuthRepository(auth, db)` recebe as instâncias pelo `AppContainer`; todas as funções viram `suspend ... : Result<...>` com `runCatching` + `withTimeout(10_000)`. Nada de `!!` nem `throw` chegando à tela.
2. **ViewModels com `Factory`:** `LoginViewModel` e `CadastroViewModel` passam a ser criadas com `viewModel(factory = ...Factory)`.
3. **Entrar (RF-01, RN-01):** `signInWithEmailAndPassword` → lê `usuarios/{uid}` → abre o Início do **perfil salvo**. Se o seletor estiver diferente, mostra o aviso *"Sua conta é de médico"* / *"Sua conta é de paciente"* (Snackbar) e segue para o Início certo.
4. **Criar conta (RF-04, RN-11, RN-12):** grava `usuarios/{uid}` com `nome`, `email`, `perfil`, `medicoId` e `criadoEm`. Paciente novo recebe `medicoId = MEDICO_TESTE_ID` (de `data/Constantes.kt`). Se a gravação falhar, **apaga o usuário do Auth** (`currentUser.delete()`) e mostra o erro.
5. **Esqueci minha senha (RF-05):** depois de chamar `sendPasswordResetEmail`, mostra sempre *"Se o e-mail estiver cadastrado, enviamos um link"* (não revela se o e-mail existe).
6. **Continuar logado (RF-07):** já funciona pelo `verificarSessao()` do `LoginViewModel`; mantenha assim, migrando para `Result` e corrigindo o L-04 (erro sem aviso). *Opcional:* mover a decisão para a `MainActivity` (seção 6.4), combinando com o líder.
7. **Validação (RNF-06):** e-mail com `Patterns.EMAIL_ADDRESS`, senha 6+, nome não vazio. Erro **embaixo do campo** (`isError` + `supportingText`), antes de chamar o Firebase.
8. **Botão Google (RNF-01):** Snackbar *"Entrar com Google chega em breve. Use e-mail e senha."*
9. **Carregamento (RNF-03):** botão Entrar / Criar conta desabilitado e com indicador enquanto espera.
10. **Responsivo (RNF-04):** `verticalScroll` + `imePadding()` + `widthIn(max = 600.dp)` centralizado; campos em `rememberSaveable` ou no estado da ViewModel.

> **Atenção às regras do Firestore (seção 11):** `usuarios/{uid}` só pode ser **criado** pelo próprio `uid` e o **perfil não pode ser alterado** depois. Grave o documento logo após o `createUser`, com o usuário recém-criado ainda logado.

## Erros encontrados no código atual (corrigir nesta versão)

> Análise do líder em 22/11/2026, na branch `feature/chat-ia` (código igual ao da `main`). Os números de linha valem para essa versão; se o arquivo já mudou, procure pelo trecho citado. **Cada erro corrigido = 1 commit** `fix(login): ... (L-XX)`. Gravidade: 🔴 alta (perde dado, fecha o app ou quebra regra de negócio) · 🟠 média (comportamento errado que o usuário percebe) · 🟡 baixa (acabamento, responsividade, código).

| ID | Grav. | Onde | O que acontece | Como corrigir |
|---|---|---|---|---|
| L-01 | 🔴 | [`AuthRepository.kt:23-30`](../../data/repository/AuthRepository.kt#L23) | Se a gravação em `usuarios/{uid}` falhar (sem internet, regra do Firestore), o usuário **fica criado e logado no Auth** sem perfil. Ao tentar de novo, aparece "Já existe uma conta com esse e-mail" e a conta nunca funciona (fere a **RN-11**). | Envolver o `set()` em `try`; na falha, `user.delete().await()` e devolver o erro (ver 6.1). |
| L-02 | 🔴 | [`AuthRepository.kt:28`](../../data/repository/AuthRepository.kt#L28) | Paciente novo é gravado **sem `medicoId`**. Resultado: Chat, Documentos e Agenda ficam bloqueados com "Você ainda não tem um médico vinculado" (fere a **RN-12**). | `medicoId = if (perfil == "paciente") MEDICO_TESTE_ID else ""` e gravar `criadoEm`. |
| L-03 | 🟠 | [`AuthRepository.kt:18`](../../data/repository/AuthRepository.kt#L18) e [`:37`](../../data/repository/AuthRepository.kt#L37) · [`LoginViewModel.kt:72`](LoginViewModel.kt#L72) | Se o login no Auth dá certo mas a leitura do perfil falha (sem internet, documento inexistente, sem permissão), a tela mostra "Algo deu errado", porque o erro do **Firestore** passa por `traduzirErroAuth`. E o usuário **continua logado no Auth** sem ter entrado. | Na falha depois do `signIn`, chamar `auth.signOut()`; trocar `traduzirErroAuth` por `mensagemDeErro(e)`, que reconhece erros do Auth e do Firestore. |
| L-04 | 🟠 | [`LoginViewModel.kt:45`](LoginViewModel.kt#L45) (`verificarSessao`) | Ao reabrir o app sem internet (ou com perfil apagado), o `catch` só esconde o carregamento: o usuário cai no Login **sem nenhuma mensagem**, e a sessão continua ativa por trás. | Mostrar `mensagemDeErro(e)` com um botão "Tentar de novo"; se o perfil não existir, fazer `signOut()`. |
| L-05 | 🟠 | [`LoginViewModel.kt:70`](LoginViewModel.kt#L70) | O seletor Paciente/Médico é ignorado **sem aviso**: quem escolhe "Médico" com uma conta de paciente entra como paciente e não entende por quê. | Comparar `usuario.perfil` com `perfilSelecionado` e mostrar "Sua conta é de paciente/médico" (ver 6.2). |
| L-06 | 🟠 | [`LoginScreen.kt:123`](LoginScreen.kt#L123) | "Continuar com Google" **não faz nada** ao tocar (fere o **RNF-01**). | Snackbar "Entrar com Google chega em breve. Use e-mail e senha." |
| L-07 | 🟡 | [`LoginViewModel.kt:86-87`](LoginViewModel.kt#L86) | "Esqueci minha senha" não tem carregamento: cada toque envia **outro e-mail**. O texto "Enviamos um link para o seu e-mail" afirma algo que o app não sabe (o Firebase não revela se o e-mail existe). | Botão desabilitado durante a chamada; texto neutro: "Se o e-mail estiver cadastrado, enviamos um link". |
| L-08 | 🟡 | [`LoginViewModel.kt:58`](LoginViewModel.kt#L58) · [`CadastroViewModel.kt:40`](CadastroViewModel.kt#L40) | O e-mail só é checado com `isBlank()`. "abc" vai para o Firebase e volta "E-mail ou senha incorretos", e no cadastro vem um erro genérico. O erro aparece num texto solto, e não embaixo do campo. | Validar com `Patterns.EMAIL_ADDRESS` e mostrar o erro com `isError` + `supportingText` (**RNF-06**). |
| L-09 | 🟡 | [`LoginScreen.kt:60`](LoginScreen.kt#L60) · [`CadastroScreen.kt:46`](CadastroScreen.kt#L46) | O app usa `enableEdgeToEdge()`, mas as telas não têm `imePadding()`: em celular pequeno, **o teclado cobre** o botão Entrar ou Criar conta. Em tela larga, o formulário estica de ponta a ponta. | `imePadding()` + `widthIn(max = 600.dp)` centralizado (**RNF-04**). |
| L-10 | 🟡 | [`CadastroScreen.kt:42`](CadastroScreen.kt#L42) | Depois de criar a conta, o app volta ao Login **sem mensagem**: o usuário não sabe se deu certo e precisa digitar o e-mail de novo. | Ao voltar, mostrar "Conta criada! Entre com seu e-mail e senha" e já preencher o e-mail. |
| L-11 | 🟡 | [`CadastroViewModel.kt:56`](CadastroViewModel.kt#L56) | Mesmo problema do L-03 no cadastro: erro do Firestore traduzido como erro do Auth ("Algo deu errado"). | `mensagemDeErro(e)`. |
| L-12 | 🟡 | [`LoginScreen.kt:43`](LoginScreen.kt#L43) e `CadastroScreen` | `collectAsState()` continua coletando com o app em segundo plano. | `collectAsStateWithLifecycle()`. |

> **Atenção do líder (não é tarefa do integrante):** qualquer pessoa consegue criar uma conta de **médico** pelo próprio app. Em projeto acadêmico isso é aceitável, mas precisa ficar registrado. Numa versão real, o perfil médico seria liberado só pela clínica.


---

## 4. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | Login com e-mail e senha via Firebase Auth | Must |
| RF-02 | Escolher o perfil (Paciente ou Médico) | Must |
| RF-03 | Após o login, abrir o Início do paciente ou do médico, conforme o perfil **salvo** | Must |
| RF-04 | Criar conta com nome, e-mail, senha e perfil | Must |
| RF-05 | Recuperar senha por e-mail | Should |
| RF-06 | Login com Google | Could (nesta fase: só o aviso RNF-01) |
| RF-07 | Manter o usuário logado ao reabrir | Should |

- **RN-01:** o perfil fica salvo em `usuarios/{uid}`. Na próxima entrada vale o perfil salvo, não o seletor.
- **RN-02:** senha com no mínimo 6 caracteres (validada na tela; o Firebase Auth também recusa).
- **RN-11 (nova):** a conta só existe quando há o usuário no Auth **e** o documento em `usuarios`. Se a gravação falhar, apagar o usuário do Auth e mostrar o erro.
- **RN-12 (nova):** nesta fase, todo paciente novo é vinculado ao médico de teste (`medicoId = MEDICO_TESTE_ID`).

## 5. Dados no Firestore

Coleção `usuarios`, um documento por usuário, **id do documento = uid** do Firebase Auth. O líder acrescenta `criadoEm` no modelo:

```kotlin
// data/model/Usuario.kt (compartilhado — mudança feita pelo líder)
data class Usuario(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val perfil: String = "paciente",   // "paciente" ou "medico"
    val medicoId: String = "",         // paciente: MEDICO_TESTE_ID (RN-12)
    val canalPreferido: String = "",
    @ServerTimestamp val criadoEm: Timestamp? = null
)
```

---

## 6. Como implementar (v1.6)

### 6.1. Repository — `Result`, sem exceção para a tela

```kotlin
class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    val usuarioLogadoUid: String? get() = auth.currentUser?.uid

    // RF-01 + RN-01: devolve o perfil SALVO
    suspend fun entrar(email: String, senha: String): Result<Usuario> = runCatching {
        withTimeout(10_000) {
            val uid = auth.signInWithEmailAndPassword(email, senha).await().user?.uid
                ?: error("Usuário sem uid")
            lerUsuario(uid)
        }
    }

    // RF-04 + RN-11 + RN-12
    suspend fun criarConta(nome: String, email: String, senha: String, perfil: String): Result<Unit> =
        runCatching {
            withTimeout(10_000) {
                val user = auth.createUserWithEmailAndPassword(email, senha).await().user
                    ?: error("Usuário sem uid")
                val usuario = Usuario(
                    uid = user.uid, nome = nome, email = email, perfil = perfil,
                    medicoId = if (perfil == "paciente") MEDICO_TESTE_ID else ""
                )
                try {
                    db.collection(COL_USUARIOS).document(user.uid).set(usuario).await()
                } catch (e: Exception) {
                    runCatching { user.delete().await() }   // RN-11: não deixa conta pela metade
                    throw e
                }
            }
        }

    // RF-07: null = ninguém logado
    suspend fun perfilDoUsuarioLogado(): Result<String?> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching null
        withTimeout(10_000) { lerUsuario(uid).perfil }
    }

    suspend fun recuperarSenha(email: String): Result<Unit> = runCatching {
        withTimeout(10_000) { auth.sendPasswordResetEmail(email).await() }
    }

    // Usada pelo Início (ui/home)
    suspend fun buscarUsuario(uid: String): Result<Usuario> = runCatching {
        withTimeout(10_000) { lerUsuario(uid) }
    }

    fun sair() = auth.signOut()

    private suspend fun lerUsuario(uid: String): Usuario =
        db.collection(COL_USUARIOS).document(uid).get().await()
            .toObject(Usuario::class.java) ?: error("Perfil não encontrado")
}
```

> Os métodos `listarPacientes()` e `assumirPaciente()` (fluxo provisório "Assumir paciente") **deixam de ser usados** na v1.6 — ver `ui/home/doc.md`, item "Assumir paciente".

### 6.2. ViewModel — validação, carregamento e perfil divergente

```kotlin
data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val perfil: String = "paciente",
    val erroEmail: String? = null,       // embaixo do campo (RNF-06)
    val erroSenha: String? = null,
    val carregando: Boolean = false,     // RNF-03
    val erro: String? = null,            // erro do Firebase (mensagemDeErro)
    val aviso: String? = null,           // Snackbar: Google, perfil divergente, senha enviada
    val perfilEntrou: String? = null     // evento: navegar para o Início
)

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun entrar() {
        val s = _uiState.value
        val erroEmail = if (!Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches()) "Digite um e-mail válido." else null
        val erroSenha = if (s.senha.length < 6) "A senha precisa ter pelo menos 6 caracteres." else null
        if (erroEmail != null || erroSenha != null) {
            _uiState.update { it.copy(erroEmail = erroEmail, erroSenha = erroSenha) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(carregando = true, erro = null) }
            repo.entrar(s.email.trim(), s.senha)
                .onSuccess { usuario ->
                    val aviso = if (usuario.perfil != s.perfil)
                        "Sua conta é de ${if (usuario.perfil == "medico") "médico" else "paciente"}." else null
                    _uiState.update { it.copy(carregando = false, aviso = aviso, perfilEntrou = usuario.perfil) }
                }
                .onFailure { e -> _uiState.update { it.copy(carregando = false, erro = mensagemDeErro(e)) } }
        }
    }

    fun onGoogleClick() = _uiState.update { it.copy(aviso = AVISO_GOOGLE) }   // RNF-01
    fun avisoMostrado() = _uiState.update { it.copy(aviso = null) }
    fun navegou() = _uiState.update { it.copy(perfilEntrou = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                LoginViewModel((this[APPLICATION_KEY] as GoianinhoApp).container.authRepository)
            }
        }
    }
}
```

### 6.3. Tela — erro embaixo do campo, Snackbar e responsividade

```kotlin
@Composable
fun LoginScreen(
    onEntrouComoPaciente: () -> Unit,
    onEntrouComoMedico: () -> Unit,
    onCriarConta: () -> Unit,
    vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.aviso) {
        state.aviso?.let { snackbar.showSnackbar(it); vm.avisoMostrado() }
    }
    LaunchedEffect(state.perfilEntrou) {
        when (state.perfilEntrou) {
            "medico" -> { vm.navegou(); onEntrouComoMedico() }
            "paciente" -> { vm.navegou(); onEntrouComoPaciente() }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 600.dp)                    // RNF-04: tela larga
                    .verticalScroll(rememberScrollState())
                    .imePadding()                             // teclado não cobre o botão
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... seletor de perfil (SeletorPerfil) ...
                OutlinedTextField(
                    value = state.email,
                    onValueChange = vm::onEmailChange,
                    label = { Text("E-mail") },
                    isError = state.erroEmail != null,
                    supportingText = { state.erroEmail?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // ... senha igual, com erroSenha ...
                state.erro?.let { AvisoErro(it) }
                Button(
                    onClick = vm::entrar,
                    enabled = !state.carregando,             // RNF-03
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    if (state.carregando) CircularProgressIndicator(Modifier.size(22.dp)) else Text("Entrar")
                }
                OutlinedButton(onClick = vm::onGoogleClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Continuar com Google")
                }
            }
        }
    }
}
```

- Navegação e Snackbar acontecem em `LaunchedEffect` a partir de **eventos no estado**, e a ViewModel limpa o evento depois (`navegou()`, `avisoMostrado()`), para não repetir ao girar a tela.
- `heightIn(min = ...)` em vez de `height(...)`: com fonte grande o botão cresce em vez de cortar o texto.

### 6.4. Manter logado (RF-07) — opcional

Hoje o `LoginViewModel.verificarSessao()` já resolve. A alternativa abaixo evita mostrar a tela de Login por um instante:

```kotlin
// Ideia para a MainActivity (arquivo compartilhado — o líder aplica junto com o seu PR)
var rotaInicial by remember { mutableStateOf<String?>(null) }
LaunchedEffect(Unit) {
    val perfil = container.authRepository.perfilDoUsuarioLogado().getOrNull()
    rotaInicial = when (perfil) {
        "medico" -> Rotas.HOME_MEDICO
        "paciente" -> Rotas.HOME_PACIENTE
        else -> Rotas.LOGIN
    }
}
rotaInicial?.let { AppNavigation(rotaInicial = it) } ?: TelaCarregando()
```

---

## 7. O que fica simulado ou para depois

- Login com Google (RF-06): o botão só mostra o aviso RNF-01. Precisa do SHA-1 no Firebase e fica para depois.
- Vínculo médico–paciente: todo paciente novo vai para o médico de teste (RN-12).

## 8. Critérios de aceite

- [ ] **Todos os erros da seção "Erros encontrados" corrigidos (L-01 a L-12)**, cada um no seu commit `fix(...)`
- [ ] Login com dados corretos abre o Início certo para cada perfil
- [ ] Seletor diferente do perfil salvo: abre o Início do perfil salvo **e** mostra "Sua conta é de…"
- [ ] Senha errada mostra "E-mail ou senha incorretos.", sem fechar o app
- [ ] E-mail inválido ou senha curta mostram o erro embaixo do campo, sem chamar o Firebase
- [ ] Conta criada aparece no Firebase Auth **e** em `usuarios`, com `medicoId` e `criadoEm`
- [ ] Falha ao gravar em `usuarios` não deixa usuário sobrando no Auth (RN-11)
- [ ] Fechar e reabrir o app mantém o usuário logado (RF-07)
- [ ] "Continuar com Google" mostra o aviso "em breve"
- [ ] Modo avião: mensagem "Sem conexão…" e o app não fecha
- [ ] Teclado não cobre o botão; funciona deitado e com fonte grande
- [ ] Visual igual ao protótipo

## 9. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| Curso completo com Firebase | [Android Basics with Compose and Firebase](https://developer.android.com/courses/android-basics-compose-firebase/course) | Visão geral de Compose + Auth + Firestore |
| Estado e ViewModel | [Codelab: ViewModel and State in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state) | `UiState` + `StateFlow` |
| UiState Carregando/Sucesso/Erro | [Codelab: Get data from the internet (Mars Photos)](https://developer.android.com/codelabs/basic-android-kotlin-compose-getting-data-internet) | Padrão de estado da v1.6 |
| Injeção manual + Factory | [Codelab: Add repository and Manual DI](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository) | `AppContainer` e `viewModelFactory` |
| Coroutines no Android | [Guia: Kotlin coroutines on Android](https://developer.android.com/kotlin/coroutines) | `viewModelScope`, `suspend`, `withTimeout` |
| Campos de texto | [Guia: Text fields](https://developer.android.com/develop/ui/compose/text/user-input) | `isError`, `supportingText`, teclado |
| Snackbar | [Guia: Snackbar](https://developer.android.com/develop/ui/compose/components/snackbar) | Avisos RNF-01 e perfil divergente |
| Teclado e insets | [Guia: Window insets in Compose](https://developer.android.com/develop/ui/compose/layouts/insets) | `imePadding()` |
| Botões segmentados | [Guia: Segmented buttons](https://developer.android.com/develop/ui/compose/components/segmented-button) | Seletor Paciente/Médico |
| Login com senha | [Firebase: Password Authentication on Android](https://firebase.google.com/docs/auth/android/password-auth) | `signIn`, `createUser`, reset de senha |
| Apagar usuário | [Firebase: Manage users (Android)](https://firebase.google.com/docs/auth/android/manage-users) | `currentUser.delete()` (RN-11) |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
