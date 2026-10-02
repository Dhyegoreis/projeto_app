# Módulo 2 — Chat com o Goianinho

| | |
|---|---|
| **Responsável** | [INTEGRANTE 2] |
| **Branch** | `feature/chat-ia` |
| **Pasta** | `ui/chat` |
| **Telas do protótipo** | 2 · Chat com o Goianinho (paciente) e 2 · Goianinho do médico · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 7 da Documentação do Projeto |

> **Duas visões, um dono:** você entrega o **chat do paciente** e o **Goianinho do médico**. O módulo só está pronto quando as duas passam nos critérios de aceite.

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

**Você trabalha somente na branch `feature/chat-ia`.** Ela é a única branch deste módulo, e é a única em que você pode fazer commit e push.

**A `main` não pode ser alterada por você de maneira alguma.** Isso inclui:
- ❌ fazer commit ou `git push` direto na `main`;
- ❌ fazer merge de qualquer branch na `main`, nem pelo botão do GitHub;
- ❌ usar `git push --force` em qualquer branch;
- ❌ trabalhar, fazer commit ou push na branch de outro integrante;
- ❌ alterar arquivos fora das suas pastas (`ui/chat` e o seu Repository em `data/repository/`) sem combinar com o líder.

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
git checkout feature/chat-ia

# Sempre antes de começar: confirmar que está na branch certa
git branch --show-current          # tem que mostrar: feature/chat-ia

# Trazer as novidades da main para a sua branch
git pull origin main

# Salvar e enviar o seu trabalho (sempre para a SUA branch)
git add .
git commit -m "feat(modulo): o que você fez"
git push origin feature/chat-ia
```

Terminou uma etapa? No GitHub, abra **Pull request → base: `main` ← compare: `feature/chat-ia`** e avise o líder. **Não clique em "Merge".**

> Se o `git branch --show-current` mostrar `main`, **pare** e rode `git checkout feature/chat-ia` antes de qualquer commit.

---

## 1. O que estas telas fazem

É o coração do projeto: a conversa em linguagem natural com o Goianinho.

**Paciente:** cabeçalho com avatar do Goianinho e "Assistente do Dr. X"; mensagens em bolhas; card de **fonte** dentro das respostas que citam documento; chips de ação rápida; campo de texto; botões enviar e voz.

**Médico:** cabeçalho escuro; faixa com os contadores do dia; fonte **clicável** que abre a Revisão (Módulo 3); chips "Próximo paciente", "Pendências do dia" e "Abrir revisões".

## 2. Arquivos que você cria

```
ui/chat/
 ├── ChatPacienteScreen.kt
 ├── ChatMedicoScreen.kt
 ├── ChatConteudo.kt          → lista + chips + campo, usado pelas DUAS telas
 ├── BolhaMensagem.kt         → uma bolha (com ou sem card de fonte)
 └── ChatViewModel.kt
data/repository/
 ├── MensagemRepository.kt    → lê e grava em "mensagens"
 └── RespostasSimuladas.kt    → gerarResposta(mensagem) — a "IA" desta fase
```

> **Dica de reaproveitamento:** as duas telas têm a mesma estrutura e só mudam cores, cabeçalho e chips. Faça um `ChatConteudo` que recebe esses detalhes por parâmetro, e as duas telas ficam pequenas.

## 3. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-08 | Exibir o histórico de mensagens, salvo no Firestore | Must |
| RF-09 | Enviar mensagem e mostrar uma resposta do Goianinho | Must |
| RF-10 | Respostas que citam documento mostram o card de fonte | Must |
| RF-11 | Chips enviam a frase como mensagem | Must |
| RF-12 | Médico: três contadores do dia no topo | Should |
| RF-13 | Médico: tocar na fonte abre a Revisão | Should |
| RF-14 | Botão de voz converte fala em texto | Could |

- **RN-03:** o Goianinho **nunca** afirma diagnóstico. Toda resposta sobre exame lembra que o médico confirma.
- **RN-04:** respostas vêm de uma lista fixa "frase-gatilho → resposta". Sem gatilho, vai a resposta padrão.

## 4. Dados no Firestore

```kotlin
// data/model/Mensagem.kt (criado na fundação)
data class Mensagem(
    @DocumentId val id: String = "",
    val pacienteId: String = "",
    val medicoId: String = "",
    val autor: String = "usuario",     // "usuario" ou "goianinho"
    val texto: String = "",
    val fonte: String = "",            // vazio = sem card de fonte
    @ServerTimestamp val criadaEm: Timestamp? = null
)
```

- `@DocumentId` preenche o `id` com o id do documento ao ler. Não é gravado como campo.
- `@ServerTimestamp` faz o **servidor** preencher a hora, então a ordem das mensagens não depende do relógio do celular.

---

## 5. Como implementar

Padrões das aulas **"Build a scrollable list"** (Unidade 3), **"Intro to state in Compose"** (Unidade 2) e **"Get realtime updates"** do Firebase (links na seção 8).

### 5.1. A "IA" simulada — isolada numa função

```kotlin
object RespostasSimuladas {
    data class Resposta(val texto: String, val fonte: String = "")

    private val gatilhos = listOf(
        "exame" to Resposta(
            "Recebi seu exame e organizei os pontos principais num resumo. " +
            "Enviei para o seu médico revisar — só ele confirma o resultado.",
            fonte = "Hemograma_set2026.pdf · pág. 1, linhas 8–14"
        ),
        "consulta" to Resposta("O médico tem horários na quinta às 09:30 ou 14:00. Quer que eu reserve?"),
        "horário" to Resposta("Posso ver os horários livres na aba Agenda ou reservar por aqui.")
    )

    fun gerarResposta(mensagem: String): Resposta =
        gatilhos.firstOrNull { (palavra, _) -> mensagem.contains(palavra, ignoreCase = true) }?.second
            ?: Resposta("Entendi. Vou organizar isso e te aviso. Qualquer decisão clínica fica com o seu médico.")
}
```

**Por que isolar:** no futuro, a IA real entra **só aqui dentro**. A tela e o ViewModel não mudam.

### 5.2. Repository

```kotlin
class MensagemRepository {
    private val col = Firebase.firestore.collection("mensagens")

    fun ouvir(pacienteId: String, onMudou: (List<Mensagem>) -> Unit): ListenerRegistration =
        col.whereEqualTo("pacienteId", pacienteId)
            .orderBy("criadaEm")
            .addSnapshotListener { snap, _ ->
                onMudou(snap?.toObjects(Mensagem::class.java).orEmpty())
            }

    fun enviar(mensagem: Mensagem) {
        col.add(mensagem)
        if (mensagem.autor == "usuario") {
            val r = RespostasSimuladas.gerarResposta(mensagem.texto)
            col.add(mensagem.copy(autor = "goianinho", texto = r.texto, fonte = r.fonte))
        }
    }
}
```

> Essa consulta (`whereEqualTo` + `orderBy` em campos diferentes) pede **índice composto**. Na primeira execução, abra o link que aparece no Logcat e clique em "Criar índice".

### 5.3. Lista de mensagens com `LazyColumn`

```kotlin
@Composable
fun ChatConteudo(
    mensagens: List<Mensagem>,
    chips: List<String>,
    onEnviar: (String) -> Unit,
    onFonteClick: ((String) -> Unit)? = null      // só o médico passa algo aqui
) {
    var texto by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Rola para a última mensagem sempre que chega uma nova
    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) listState.animateScrollToItem(mensagens.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mensagens, key = { it.id.ifEmpty { it.hashCode().toString() } }) { msg ->
                BolhaMensagem(msg, onFonteClick)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chips) { chip ->
                SuggestionChip(onClick = { onEnviar(chip) }, label = { Text(chip) })
            }
        }

        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Fale com o Goianinho…") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(onClick = {
                if (texto.isNotBlank()) { onEnviar(texto); texto = "" }
            }) {
                Icon(painterResource(R.drawable.ic_send), contentDescription = "Enviar mensagem")
            }
        }
    }
}
```

- `items(..., key = ...)` ajuda o Compose a não redesenhar a lista inteira a cada mensagem.
- `Modifier.weight(1f)` faz a lista ocupar o espaço que sobra, deixando chips e campo fixos embaixo.
- `rememberSaveable` mantém o texto digitado se o celular girar.

### 5.4. Bolha com card de fonte (RF-10 e RF-13)

```kotlin
@Composable
fun BolhaMensagem(msg: Mensagem, onFonteClick: ((String) -> Unit)?) {
    val doUsuario = msg.autor == "usuario"
    Box(Modifier.fillMaxWidth(), contentAlignment = if (doUsuario) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            color = if (doUsuario) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(msg.texto)
                if (msg.fonte.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        onClick = { onFonteClick?.invoke(msg.fonte) },
                        enabled = onFonteClick != null
                    ) {
                        Text("Fonte: ${msg.fonte}", style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}
```

### 5.5. Voz para texto (RF-14, opcional)

```kotlin
val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
    val falado = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
    if (!falado.isNullOrBlank()) onEnviar(falado)
}
// No botão do microfone:
launcher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
})
```

Usa o reconhecimento de voz do próprio Android, sem biblioteca extra.

---

## 6. O que fica simulado

- Toda a inteligência do Goianinho (`RespostasSimuladas`).
- A fonte citada é texto fixo de exemplo. Não use valores clínicos reais.

## 7. Critérios de aceite

- [ ] Mensagem enviada aparece na hora e continua lá ao reabrir o app
- [ ] Cada chip gera a resposta esperada
- [ ] Card de fonte aparece só nas respostas que citam documento
- [ ] No médico, tocar na fonte abre a Revisão
- [ ] Paciente e médico veem cada um a sua versão da tela
- [ ] Visual das duas telas igual ao protótipo

## 8. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| Estado em Compose | [Codelab: State in Jetpack Compose](https://developer.android.com/codelabs/jetpack-compose-state) | `remember`, `rememberSaveable`, state hoisting |
| Listas | [Guia: Lists and grids](https://developer.android.com/develop/ui/compose/lists) | `LazyColumn`, `LazyRow`, `key`, scroll |
| Lista na prática | [Codelab: Add a scrollable list](https://developer.android.com/codelabs/basic-android-kotlin-compose-training-add-scrollable-list) | Montar a lista de mensagens |
| ViewModel | [Codelab: ViewModel and State in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state) | `ChatViewModel` com `StateFlow` |
| Chips | [Guia: Chip](https://developer.android.com/develop/ui/compose/components/chip) | Ações rápidas |
| Campos de texto | [Guia: Text fields](https://developer.android.com/develop/ui/compose/text/user-input) | Campo de mensagem |
| Tempo real | [Firebase: Get realtime updates](https://firebase.google.com/docs/firestore/query-data/listen) | Histórico que atualiza sozinho |
| Gravar dados | [Firebase: Add data](https://firebase.google.com/docs/firestore/manage-data/add-data) | `add()` e `@ServerTimestamp` |
| Resultado de outra tela | [Guia: Get a result from an activity](https://developer.android.com/training/basics/intents/result) | Reconhecimento de voz |
| Reconhecimento de voz | [Referência: RecognizerIntent](https://developer.android.com/reference/android/speech/RecognizerIntent) | Fala → texto |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
