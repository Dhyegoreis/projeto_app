# Módulo 4 — Agenda

| | |
|---|---|
| **Responsável** | [INTEGRANTE 4] |
| **Branch** | `feature/agenda` |
| **Pasta** | `ui/agenda` |
| **Telas do protótipo** | 4 · Agenda (paciente) e 4 · Agenda do dia (médico) · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 9 da Documentação do Projeto |

> **Duas visões, um dono:** você entrega a **Agenda do paciente** e a **Agenda do dia** do médico. O módulo só está pronto quando as duas passam nos critérios de aceite.

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

**Você trabalha somente na branch `feature/agenda`.** Ela é a única branch deste módulo, e é a única em que você pode fazer commit e push.

**A `main` não pode ser alterada por você de maneira alguma.** Isso inclui:
- ❌ fazer commit ou `git push` direto na `main`;
- ❌ fazer merge de qualquer branch na `main`, nem pelo botão do GitHub;
- ❌ usar `git push --force` em qualquer branch;
- ❌ trabalhar, fazer commit ou push na branch de outro integrante;
- ❌ alterar arquivos fora das suas pastas (`ui/agenda` e o seu Repository em `data/repository/`) sem combinar com o líder.

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
git checkout feature/agenda

# Sempre antes de começar: confirmar que está na branch certa
git branch --show-current          # tem que mostrar: feature/agenda

# Trazer as novidades da main para a sua branch
git pull origin main

# Salvar e enviar o seu trabalho (sempre para a SUA branch)
git add .
git commit -m "feat(modulo): o que você fez"
git push origin feature/agenda
```

Terminou uma etapa? No GitHub, abra **Pull request → base: `main` ← compare: `feature/agenda`** e avise o líder. **Não clique em "Merge".**

> Se o `git branch --show-current` mostrar `main`, **pare** e rode `git checkout feature/agenda` antes de qualquer commit.

---

## 1. O que estas telas fazem

O paciente marca e acompanha consultas; o médico vê o dia com um briefing pronto de cada paciente.

**Paciente:** card da próxima consulta com "Entrar na chamada"; seletor de dias; grade de horários; botão de confirmar que mostra a escolha ("Confirmar Qui, 24/09 às 09:30"); dica para marcar falando com o Goianinho.

**Médico:** card do paciente atual com o briefing do Goianinho e os botões "Ver resumo" e "Iniciar"; lista das próximas consultas com status do resumo e dica de engajamento.

## 2. Arquivos que você cria

```
ui/agenda/
 ├── AgendaPacienteScreen.kt
 ├── AgendaMedicoScreen.kt
 ├── ChamadaEmBreveScreen.kt   → tela simples para "Entrar na chamada" / "Iniciar"
 └── AgendaViewModel.kt
data/repository/
 └── AgendaRepository.kt
```

## 3. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-24 | Paciente vê os horários livres do médico por dia | Must |
| RF-25 | Paciente reserva um horário, que fica indisponível para os outros | Must |
| RF-26 | Próxima consulta do paciente no topo | Must |
| RF-27 | Médico vê as consultas do dia em ordem de horário | Must |
| RF-28 | Cada consulta do médico mostra o status do resumo (Módulo 3) | Should |
| RF-29 | Cada consulta do médico mostra uma dica de engajamento | Could |
| RF-30 | "Entrar na chamada" e "Iniciar" abrem a videochamada | Could |

- **RN-08:** horário reservado não pode ser reservado de novo — usar **transação** do Firestore.
- **RN-09:** nesta fase, os horários livres são cadastrados à mão no console do Firestore.

## 4. Dados no Firestore

```kotlin
// data/model/Horario.kt e Consulta.kt (criados na fundação)
data class Horario(
    @DocumentId val id: String = "",
    val medicoId: String = "",
    val data: String = "",       // "2026-09-24" — texto facilita filtrar por dia
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

**Cadastrando horários de teste (RN-09):** no console, em Firestore → Dados → Iniciar coleção `horarios`, crie alguns documentos com `medicoId` (o uid do médico de teste), `data`, `hora` e `livre = true`.

---

## 5. Como implementar

Padrões das aulas **"Display lists"** (Unidade 3) e da página **Transactions and batched writes** do Firestore (links na seção 8).

### 5.1. Seletor de dias e grade de horários

```kotlin
@Composable
fun SeletorDeHorario(
    dias: List<Pair<String, String>>,            // ("2026-09-24", "Qui\n24")
    diaSelecionado: String,
    horarios: List<Horario>,
    horarioSelecionado: Horario?,
    onDiaClick: (String) -> Unit,
    onHorarioClick: (Horario) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(dias) { (data, rotulo) ->
            FilterChip(
                selected = data == diaSelecionado,
                onClick = { onDiaClick(data) },
                label = { Text(rotulo, textAlign = TextAlign.Center) }
            )
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        items(horarios, key = { it.id }) { h ->
            FilterChip(
                selected = h.id == horarioSelecionado?.id,
                onClick = { onHorarioClick(h) },
                label = { Text(h.hora, Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            )
        }
    }
}
```

- `FilterChip` já tem o visual de "selecionado / não selecionado" do Material 3.
- `LazyVerticalGrid` com `GridCells.Fixed(3)` monta a grade de 3 colunas do protótipo.

### 5.2. Horários livres do dia (RF-24)

Os itens 5.2, 5.3 e 5.4 são funções **dentro de `class AgendaRepository`** — é o único arquivo do módulo que fala com o Firebase.

```kotlin
fun ouvirHorariosLivres(medicoId: String, data: String, onMudou: (List<Horario>) -> Unit): ListenerRegistration =
    Firebase.firestore.collection("horarios")
        .whereEqualTo("medicoId", medicoId)
        .whereEqualTo("data", data)
        .whereEqualTo("livre", true)
        .addSnapshotListener { snap, _ ->
            onMudou(snap?.toObjects(Horario::class.java).orEmpty().sortedBy { it.hora })
        }
```

Ordenar com `sortedBy` no próprio app evita a necessidade de criar um índice composto.

### 5.3. Reservar com transação (RF-25 e RN-08)

```kotlin
fun reservar(
    horario: Horario, pacienteId: String, dataHora: Timestamp,
    onSucesso: () -> Unit, onErro: (String) -> Unit
) {
    val db = Firebase.firestore
    val refHorario = db.collection("horarios").document(horario.id)
    val refConsulta = db.collection("consultas").document()

    db.runTransaction { tx ->
        val atual = tx.get(refHorario)
        if (atual.getBoolean("livre") != true) {
            throw FirebaseFirestoreException("Horário já reservado",
                FirebaseFirestoreException.Code.ABORTED)
        }
        tx.update(refHorario, "livre", false)
        tx.set(refConsulta, Consulta(
            pacienteId = pacienteId, medicoId = horario.medicoId, dataHora = dataHora
        ))
    }
        .addOnSuccessListener { onSucesso() }
        .addOnFailureListener { onErro("Esse horário acabou de ser reservado. Escolha outro.") }
}
```

**Por que transação:** se dois pacientes tocarem no mesmo horário ao mesmo tempo, a transação garante que **só um** consiga. O segundo recebe o erro e escolhe outro horário.

### 5.4. Agenda do dia do médico (RF-27 e RF-28)

```kotlin
fun ouvirConsultasDoDia(medicoId: String, inicio: Timestamp, fim: Timestamp, onMudou: (List<Consulta>) -> Unit): ListenerRegistration =
    Firebase.firestore.collection("consultas")
        .whereEqualTo("medicoId", medicoId)
        .whereGreaterThanOrEqualTo("dataHora", inicio)
        .whereLessThan("dataHora", fim)
        .orderBy("dataHora")
        .addSnapshotListener { snap, _ ->
            onMudou(snap?.toObjects(Consulta::class.java).orEmpty())
        }
```

- Essa consulta pede **índice composto**. Na primeira execução, abra o link do Logcat e clique em "Criar índice".
- Para o **status do resumo** (RF-28), **não** consulte `documentos` direto: a `AgendaViewModel` usa o `DocumentoRepository` do Módulo 3 (`ouvirPendentes(medicoId)`) e cruza o `pacienteId` de cada consulta com a lista de pendentes. Os textos de status estão no doc do Módulo 3.

### 5.5. Função que o Início usa

O Início do paciente (Módulo 1) mostra a próxima consulta **pelo seu repositório**. Crie também, dentro de `AgendaRepository`:

```kotlin
fun ouvirProximaConsulta(pacienteId: String, onMudou: (Consulta?) -> Unit): ListenerRegistration =
    Firebase.firestore.collection("consultas")
        .whereEqualTo("pacienteId", pacienteId)
        .whereGreaterThanOrEqualTo("dataHora", Timestamp.now())
        .orderBy("dataHora")
        .limit(1)
        .addSnapshotListener { snap, _ ->
            onMudou(snap?.toObjects(Consulta::class.java)?.firstOrNull())
        }
```

O `ouvirConsultasDoDia` do item 5.4 também é usado pelo Início do médico. **Suba essas duas funções cedo** (num PR pequeno), porque o Módulo 1 depende delas.

### 5.6. Lista das próximas consultas

```kotlin
LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    items(consultas, key = { it.id }) { c ->
        OutlinedCard(Modifier.fillMaxWidth()) {
            // nomeDoPaciente e dicaDeEngajamento vêm do UiState da ViewModel, não de consultas na tela
            ListItem(
                leadingContent = { Text(horaDe(c.dataHora), style = MaterialTheme.typography.titleMedium) },
                headlineContent = { Text(nomeDoPaciente(c.pacienteId)) },
                supportingContent = { Text(dicaDeEngajamento(c.pacienteId)) }  // texto fixo nesta fase
            )
        }
    }
}
```

---

## 6. O que fica simulado

- A videochamada: "Entrar na chamada" e "Iniciar" abrem a `ChamadaEmBreveScreen`.
- As dicas de engajamento ("prefere ligação", "costuma confirmar tarde"): texto fixo por paciente.
- O briefing do Goianinho no card do paciente atual: texto fixo, com o status real do resumo.

## 7. Critérios de aceite

- [ ] Horário reservado some da grade para outros pacientes
- [ ] Dois toques simultâneos no mesmo horário: só um consegue
- [ ] Consulta reservada aparece na Agenda do dia do médico
- [ ] Status do resumo bate com o que está no Módulo 3
- [ ] Visual das duas telas igual ao protótipo

## 8. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| Listas e grades | [Guia: Lists and grids](https://developer.android.com/develop/ui/compose/lists) | `LazyRow`, `LazyVerticalGrid`, `LazyColumn` |
| Chips | [Guia: Chip](https://developer.android.com/develop/ui/compose/components/chip) | `FilterChip` de dia e horário |
| Cards e Material | [Curso Android Basics with Compose — Unidade 3](https://developer.android.com/courses/android-basics-compose/unit-3) | Cards da agenda |
| ViewModel | [Codelab: ViewModel and State in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state) | Dia e horário selecionados no estado |
| Transações | [Firebase: Transactions and batched writes](https://firebase.google.com/docs/firestore/manage-data/transactions) | Reserva sem conflito (RN-08) |
| Filtros por intervalo | [Firebase: Perform simple and compound queries](https://firebase.google.com/docs/firestore/query-data/queries) | Consultas do dia |
| Ordenar | [Firebase: Order and limit data](https://firebase.google.com/docs/firestore/query-data/order-limit-data) | Ordem por horário |
| Tempo real | [Firebase: Get realtime updates](https://firebase.google.com/docs/firestore/query-data/listen) | Grade e agenda atualizando sozinhas |
| Curso com Firebase | [Android Basics with Compose and Firebase](https://developer.android.com/courses/android-basics-compose-firebase/course) | Arquitetura + Firestore |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
