# Módulo 1 — Início (parte 2 de 2)

| | |
|---|---|
| **Responsável** | [INTEGRANTE 1] (o mesmo do Login) |
| **Branch** | `feature/login` |
| **Pasta** | `ui/home` |
| **Telas do protótipo** | 1 · Início do paciente e 1 · Início do médico · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 6 da Documentação do Projeto |

> **Duas visões, um dono:** você entrega o **Início do paciente** e o **Início do médico**. O módulo só está pronto quando as duas passam nos critérios de aceite.

---

## Arquitetura obrigatória (vale para todo o projeto)

O projeto segue o **MVVM recomendado pelo Google** ([Guide to app architecture](https://developer.android.com/topic/architecture)). Todo código deste módulo precisa respeitar as três camadas:

```
Tela (Screen)  ──evento──▶  ViewModel  ──▶  Repository  ──▶  Firebase
     ▲                          │
     └────── estado (StateFlow) ┘
```

| Camada | Arquivo | Pode | Não pode |
|---|---|---|---|
| **Tela** | `XxxScreen.kt` | Desenhar a interface, ler o estado e avisar cliques | Chamar o Firebase ou navegar sozinha |
| **ViewModel** | `XxxViewModel.kt` | Guardar o estado (`UiState` + `StateFlow`) e decidir o que fazer em cada ação | Ter código de interface ou chamar `Firebase.*` diretamente |
| **Repository** | `data/repository/` | Ser **o único** que fala com o Firebase (Auth e Firestore) | Saber que tela existe |

**Três regras que derivam disso:**
- **Fluxo em mão única:** o estado desce (ViewModel → Tela) e os eventos sobem (Tela → ViewModel).
- **Fonte única da verdade:** o dado oficial está no Firestore, e o acesso a ele passa pelo Repository dono da coleção.
- **Telas independentes:** a tela recebe funções (`onAbrirChat`, `onVoltar`...) e quem liga as telas é o `navigation/`.

**Teste rápido antes de abrir o PR:** procure `Firebase.` no seu código. Se aparecer em algum arquivo fora de `data/repository/`, está no lugar errado.

Para estudar: [Camada de UI](https://developer.android.com/topic/architecture/ui-layer) · [Camada de dados](https://developer.android.com/topic/architecture/data-layer) · [Codelab: ViewModel and State in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state)

---

## Regras do repositório (obrigatórias)

**Você trabalha somente na branch `feature/login`.** Ela é a única branch deste módulo, e é a única em que você pode fazer commit e push.

**A `main` não pode ser alterada por você de maneira alguma.** Isso inclui:
- ❌ fazer commit ou `git push` direto na `main`;
- ❌ fazer merge de qualquer branch na `main`, nem pelo botão do GitHub;
- ❌ usar `git push --force` em qualquer branch;
- ❌ trabalhar, fazer commit ou push na branch de outro integrante;
- ❌ alterar arquivos fora das suas pastas (`ui/login` e `ui/home` e o seu Repository em `data/repository/`) sem combinar com o líder.

**O que você pode fazer:**
- ✅ trazer o que já entrou na `main` **para a sua branch** (isso não altera a `main`);
- ✅ abrir um **Pull Request** da sua branch para a `main` quando terminar uma etapa;
- ✅ ajustar o PR quando o líder pedir mudanças.

**Quem altera a `main`:** só o **líder do projeto**. Ele revisa o seu Pull Request, aprova e faz o merge. Se precisar mudar algo compartilhado (`theme/`, `navigation/`, `data/model/`, Gradle), avise no grupo e o líder decide como entra.

### Comandos do dia a dia

```bash
# 1x no início: baixar o projeto e entrar na SUA branch
git clone <url-do-repositorio>
cd <pasta-do-projeto>
git checkout feature/login

# Sempre antes de começar: confirmar que está na branch certa
git branch --show-current          # tem que mostrar: feature/login

# Trazer as novidades da main para a sua branch
git pull origin main

# Salvar e enviar o seu trabalho (sempre para a SUA branch)
git add .
git commit -m "feat(modulo): o que você fez"
git push origin feature/login
```

Terminou uma etapa? No GitHub, abra **Pull request → base: `main` ← compare: `feature/login`** e avise o líder. **Não clique em "Merge".**

> Se o `git branch --show-current` mostrar `main`, **pare** e rode `git checkout feature/login` antes de qualquer commit.

---

## 1. O que estas telas fazem

O Início é o **painel** de cada perfil: resume o que importa agora e leva às outras abas. Ele **só lê** dados (RN-10). Quem grava consultas e documentos são os Módulos 3 e 4.

**Início do paciente:** saudação com nome e data; card do Goianinho com o campo "Fale ou escreva" e botão de voz (abrem o Chat); chips Marcar consulta, Enviar exame e Tirar dúvida; card da próxima consulta; lista "Acompanhamento" com o status dos documentos.

**Início do médico:** saudação com data; três contadores (consultas hoje, para revisar, sem resposta); card da próxima consulta com briefing e os botões Ver briefing / Iniciar; lista "Precisa de você"; campo "Pergunte ao Goianinho".

**Também é seu:** a **barra inferior de 4 abas** (RF-36), usada pelas outras telas.

## 2. Arquivos que você cria

```
ui/home/
 ├── HomePacienteScreen.kt
 ├── HomeMedicoScreen.kt
 ├── HomeViewModel.kt          → um ViewModel serve as duas telas
 └── BarraInferior.kt          → NavigationBar de 4 abas (RF-36)
```

## 3. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-31 | Início do paciente: saudação, atalho para o Goianinho e chips | Must |
| RF-32 | Início do paciente: próxima consulta e status dos documentos | Must |
| RF-33 | Início do médico: três contadores do dia | Must |
| RF-34 | Início do médico: próxima consulta com briefing e lista "Precisa de você" | Must |
| RF-35 | Cada card e chip leva à aba correspondente | Should |
| RF-36 | Barra inferior com 4 abas nos dois perfis, começando pelo Início | Must |

- **RN-10:** o Início só lê. Nada de `set()` ou `update()` aqui.

## 4. Dados que você lê (sempre via Repository)

| Coleção | Filtro | Para mostrar |
|---|---|---|
| `usuarios` | documento `{uid}` | Nome na saudação |
| `consultas` | paciente: `pacienteId == uid` · médico: `medicoId == uid` | Próxima consulta e contador "consultas hoje" |
| `documentos` | paciente: `pacienteId == uid` · médico: `medicoId == uid` e `status == "aguardando_medico"` | Lista de acompanhamento e contador "para revisar" |

> O Início **não acessa essas coleções diretamente**: ele chama as funções dos repositórios dos Módulos 1, 3 e 4 (seção 5.1).

---

## 5. Como implementar

Padrões das aulas **"Display lists and use Material Design"** (Unidade 3), **"Navigation and app architecture"** (Unidade 4) e **"Read data in real time"** do Firebase (links na seção 8).

### 5.1. De onde vêm os dados: repositórios dos outros módulos

O Início **não tem repositório próprio**. Ele só lê, então usa as funções que os donos de cada coleção já criam (fonte única da verdade):

| Dado | Repository (dono) | Função usada pelo Início |
|---|---|---|
| Nome do usuário | `AuthRepository` (Módulo 1, você) | `buscarUsuario(uid, onResultado)` |
| Documentos do paciente | `DocumentoRepository` (Módulo 3) | `ouvirDoPaciente(pacienteId, limite, onMudou)` |
| Resumos para revisar | `DocumentoRepository` (Módulo 3) | `ouvirPendentes(medicoId, onMudou)` |
| Próxima consulta | `AgendaRepository` (Módulo 4) | `ouvirProximaConsulta(pacienteId, onMudou)` |
| Consultas de hoje | `AgendaRepository` (Módulo 4) | `ouvirConsultasDoDia(medicoId, inicio, fim, onMudou)` |

> **Combinado entre módulos:** essas funções já estão descritas nos `doc.md` dos Módulos 3 e 4. Enquanto elas não chegam na `main`, use **dados fixos** na ViewModel (lista de exemplo) para montar as telas. Quando o PR dos colegas entrar, você só troca o dado fixo pela chamada.

### 5.2. ViewModel — só conversa com repositórios

```kotlin
data class HomeUiState(
    val nome: String = "",
    val proximaConsulta: Consulta? = null,
    val documentos: List<Documento> = emptyList(),
    val paraRevisar: Int = 0,
    val consultasHoje: Int = 0
)

class HomeViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val documentoRepo: DocumentoRepository = DocumentoRepository(),
    private val agendaRepo: AgendaRepository = AgendaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val uid = authRepo.usuarioLogado()?.uid.orEmpty()
    private val ouvintes = mutableListOf<ListenerRegistration>()

    fun carregarPaciente() {
        authRepo.buscarUsuario(uid) { usuario ->
            _uiState.update { it.copy(nome = usuario?.nome.orEmpty()) }
        }
        ouvintes += documentoRepo.ouvirDoPaciente(uid, limite = 3) { docs ->
            _uiState.update { it.copy(documentos = docs) }
        }
        ouvintes += agendaRepo.ouvirProximaConsulta(uid) { consulta ->
            _uiState.update { it.copy(proximaConsulta = consulta) }
        }
    }

    fun carregarMedico() {
        authRepo.buscarUsuario(uid) { usuario ->
            _uiState.update { it.copy(nome = usuario?.nome.orEmpty()) }
        }
        ouvintes += documentoRepo.ouvirPendentes(uid) { pendentes ->
            _uiState.update { it.copy(paraRevisar = pendentes.size) }
        }
        val (inicio, fim) = intervaloDeHoje()
        ouvintes += agendaRepo.ouvirConsultasDoDia(uid, inicio, fim) { consultas ->
            _uiState.update { it.copy(consultasHoje = consultas.size, proximaConsulta = consultas.firstOrNull()) }
        }
    }

    override fun onCleared() {
        ouvintes.forEach { it.remove() }   // para de ouvir quando a tela sai
    }
}

private fun intervaloDeHoje(): Pair<Timestamp, Timestamp> {
    val inicio = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
    return Timestamp(Date.from(inicio.toInstant())) to Timestamp(Date.from(inicio.plusDays(1).toInstant()))
}
```

- Repare que **não existe `Firebase.` neste arquivo**: tudo passa pelos repositórios.
- Os repositórios devolvem um `ListenerRegistration` (o "ouvinte"). A ViewModel guarda e remove todos em `onCleared()`, para não gastar bateria nem leituras quando a tela sai.
- Como as funções usam `addSnapshotListener`, o Início **atualiza sozinho**: quando o médico confirma um resumo no Módulo 3, o card do paciente muda na hora.

### 5.3. Tela do paciente

```kotlin
@Composable
fun HomePacienteScreen(
    onAbrirChat: () -> Unit,
    onAbrirAgenda: () -> Unit,
    onAbrirDocumentos: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.carregarPaciente() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Olá, ${state.nome}", style = MaterialTheme.typography.headlineSmall)

        // Card do Goianinho
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            onClick = onAbrirChat
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Em que posso ajudar hoje?", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = onAbrirAgenda, label = { Text("Marcar consulta") })
                    AssistChip(onClick = onAbrirDocumentos, label = { Text("Enviar exame") })
                    AssistChip(onClick = onAbrirChat, label = { Text("Tirar dúvida") })
                }
            }
        }

        state.proximaConsulta?.let { consulta ->
            OutlinedCard(onClick = onAbrirAgenda) {
                // Data, médico e horário — siga o protótipo
            }
        }

        Text("ACOMPANHAMENTO", style = MaterialTheme.typography.labelLarge)
        state.documentos.forEach { doc ->
            OutlinedCard(onClick = onAbrirDocumentos, modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(doc.nome) },
                    supportingContent = { Text(textoDoStatus(doc.status)) }
                )
            }
        }
    }
}
```

> Por que `forEach` dentro de `Column` e não `LazyColumn`? Aqui são no máximo 3 itens, então tanto faz. Use `LazyColumn` quando a lista puder crescer (como no Chat e na Agenda).

A tela do médico segue o mesmo modelo: uma `Row` com três `Card`s de contador, um `Card` escuro da próxima consulta e a lista "Precisa de você".

### 5.4. Barra inferior de 4 abas (RF-36)

```kotlin
data class Aba(val rota: String, val titulo: String, @DrawableRes val icone: Int)

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

- **Abas do paciente:** Início · Assistente · Documentos · Agenda.
- **Abas do médico:** Início · Goianinho · Revisões · Agenda.
- **Ícones:** baixe do [Material Symbols](https://fonts.google.com/icons) (Home, Chat, Description, Check, Calendar) e importe em `res/drawable` com **New → Vector Asset**.
- Quem monta a navegação coloca a `BarraInferior` no `Scaffold(bottomBar = { ... })` das telas logadas.

---

## 6. O que fica simulado

- Os textos de briefing e as dicas de engajamento ("sem resposta há 2 dias") podem ser fixos nesta fase.
- O botão "Iniciar" da teleconsulta abre a tela "Chamada em breve" (o Módulo 4 cria).

## 7. Critérios de aceite

- [ ] Início do paciente mostra o nome, a próxima consulta e os documentos reais do usuário logado
- [ ] Início do médico mostra os contadores e a lista "Precisa de você" com dados reais
- [ ] Ao confirmar um resumo no Módulo 3, o Início muda sozinho (tempo real)
- [ ] Barra de 4 abas funciona nos dois perfis
- [ ] Visual das duas telas igual ao protótipo

## 8. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| Layouts básicos | [Codelab: Jetpack Compose basics](https://developer.android.com/codelabs/jetpack-compose-basics) | `Column`, `Row`, `Modifier` |
| Material 3 e cards | [Curso Android Basics with Compose — Unidade 3](https://developer.android.com/courses/android-basics-compose/unit-3) | Cards, cores, tema |
| Tema Material 3 | [Guia: Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3) | Cores do protótipo no tema |
| Chips | [Guia: Chip](https://developer.android.com/develop/ui/compose/components/chip) | Marcar consulta, Enviar exame |
| Barra inferior | [Guia: Navigation bar](https://developer.android.com/develop/ui/compose/components/navigation-bar) | As 4 abas |
| Navegação | [Codelab: Navigate between screens with Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-navigation) | Receber `onAbrirX` em vez de navegar sozinho |
| Arquitetura | [Guide to app architecture](https://developer.android.com/topic/architecture) | ViewModel que só conversa com repositórios |
| Ler em tempo real | [Firebase: Get realtime updates](https://firebase.google.com/docs/firestore/query-data/listen) | Entender o `ListenerRegistration` devolvido pelos repositórios |
| Filtros e ordem | [Firebase: Order and limit data](https://firebase.google.com/docs/firestore/query-data/order-limit-data) | Próxima consulta, últimos documentos |
| Curso com Firebase | [Android Basics with Compose and Firebase](https://developer.android.com/courses/android-basics-compose-firebase/course) | Arquitetura + Firestore |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
