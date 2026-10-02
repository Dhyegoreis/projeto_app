# Módulo 3 — Documentos e Revisão

| | |
|---|---|
| **Responsável** | [INTEGRANTE 3] |
| **Branch** | `feature/documentos` |
| **Pasta** | `ui/documentos` |
| **Telas do protótipo** | 3 · Documentos (paciente) e 3 · Revisão de resumos (médico) · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 8 da Documentação do Projeto |

> **Duas visões, um dono:** você entrega **Documentos** (paciente) e **Revisão de resumos** (médico). O módulo só está pronto quando as duas passam nos critérios de aceite.

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

**Você trabalha somente na branch `feature/documentos`.** Ela é a única branch deste módulo, e é a única em que você pode fazer commit e push.

**A `main` não pode ser alterada por você de maneira alguma.** Isso inclui:
- ❌ fazer commit ou `git push` direto na `main`;
- ❌ fazer merge de qualquer branch na `main`, nem pelo botão do GitHub;
- ❌ usar `git push --force` em qualquer branch;
- ❌ trabalhar, fazer commit ou push na branch de outro integrante;
- ❌ alterar arquivos fora das suas pastas (`ui/documentos` e o seu Repository em `data/repository/`) sem combinar com o líder.

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
git checkout feature/documentos

# Sempre antes de começar: confirmar que está na branch certa
git branch --show-current          # tem que mostrar: feature/documentos

# Trazer as novidades da main para a sua branch
git pull origin main

# Salvar e enviar o seu trabalho (sempre para a SUA branch)
git add .
git commit -m "feat(modulo): o que você fez"
git push origin feature/documentos
```

Terminou uma etapa? No GitHub, abra **Pull request → base: `main` ← compare: `feature/documentos`** e avise o líder. **Não clique em "Merge".**

> Se o `git branch --show-current` mostrar `main`, **pare** e rode `git checkout feature/documentos` antes de qualquer commit.

---

## 1. O que estas telas fazem

O paciente envia um exame, o Goianinho gera um resumo com a fonte de cada ponto, e o médico **confirma ou corrige**.

**Paciente:** botões "Enviar arquivo" e "Tirar foto"; card do documento mais recente com o "Resumo do Goianinho" (cada ponto com página e linha); etiqueta de status; lista de documentos anteriores.

**Médico:** fila de pacientes com resumo pendente; card do documento com os pontos e o link "Ver no documento"; campo de observação; botões Corrigir e Confirmar; aviso de registro no prontuário.

> **Decisão v1.1 — sem Firebase Storage.** O app **não envia o arquivo**. Grava no Firestore só os dados do documento (nome, tipo, data, status e resumo simulado). Foto pequena pode ir como texto Base64, com menos de 1 MB.

## 2. Arquivos que você cria

```
ui/documentos/
 ├── DocumentosScreen.kt       → paciente
 ├── RevisaoScreen.kt          → médico
 ├── CardResumo.kt             → card com os pontos e fontes (usado nas duas telas)
 └── DocumentosViewModel.kt
data/repository/
 └── DocumentoRepository.kt
```

## 3. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-15 | Selecionar PDF ou imagem; gravar nome, tipo e data (sem enviar o arquivo) | Must |
| RF-16 | Tirar foto; se < 1 MB, salvar em Base64 | Should |
| RF-17 | Listar documentos do mais recente ao mais antigo | Must |
| RF-18 | Mostrar o resumo do Goianinho com a fonte de cada ponto | Must |
| RF-19 | Mostrar status: Aguardando médico, Confirmado, Corrigido ou Arquivado | Must |
| RF-20 | Médico vê a fila com status "Aguardando médico" | Must |
| RF-21 | Médico confirma ou corrige, com observação opcional | Must |
| RF-22 | Ao confirmar, registrar data, médico e apoio de IA | Must |
| RF-23 | "Ver no documento" mostra a foto em Base64 ou os dados | Could |

- **RN-05:** o resumo só vale para o paciente depois da confirmação do médico.
- **RN-06:** todo documento confirmado guarda `confirmadoPor`, `confirmadoEm` e `apoioIA = true` (Resolução CFM 2.454/2026).
- **RN-07:** o médico só vê documentos de pacientes vinculados a ele.

## 4. Dados no Firestore

```kotlin
// data/model/Documento.kt (criado na fundação)
data class PontoResumo(val texto: String = "", val fonte: String = "")

data class Documento(
    @DocumentId val id: String = "",
    val pacienteId: String = "",
    val medicoId: String = "",
    val nome: String = "",
    val tipo: String = "",                 // "pdf" ou "imagem"
    @ServerTimestamp val enviadoEm: Timestamp? = null,
    val imagemBase64: String = "",         // opcional, < 1 MB
    val resumo: List<PontoResumo> = emptyList(),
    val status: String = "aguardando_medico",  // aguardando_medico | confirmado | corrigido | arquivado
    val observacao: String = "",
    val confirmadoPor: String = "",
    val confirmadoEm: Timestamp? = null,
    val apoioIA: Boolean = false
)
```

> **Use exatamente estes textos de status.** O Início (Módulo 1) e a Agenda do dia (Módulo 4) filtram por eles.

---

## 5. Como implementar

Padrões do guia **Activity Result** do Android e das páginas **Add data / Update data** do Firestore (links na seção 8).

### 5.1. Escolher arquivo do celular (RF-15)

```kotlin
val context = LocalContext.current
val escolherArquivo = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    if (uri != null) {
        val nome = nomeDoArquivo(context, uri)
        val tipo = if (context.contentResolver.getType(uri) == "application/pdf") "pdf" else "imagem"
        viewModel.registrarDocumento(nome, tipo)
    }
}

Button(onClick = { escolherArquivo.launch(arrayOf("application/pdf", "image/*")) }) {
    Text("Enviar arquivo")
}

fun nomeDoArquivo(context: Context, uri: Uri): String =
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (c.moveToFirst() && i >= 0) c.getString(i) else null
    } ?: "documento"
```

O `OpenDocument` abre o seletor de arquivos do Android e **não precisa de permissão** de armazenamento.

### 5.2. Tirar foto pequena em Base64 (RF-16)

```kotlin
val tirarFoto = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
) { bitmap: Bitmap? ->
    if (bitmap != null) {
        val bytes = ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it)
        }.toByteArray()
        if (bytes.size < 900_000) {          // margem abaixo de 1 MB
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            viewModel.registrarDocumento("Foto do documento", "imagem", base64)
        }
    }
}
```

O `TakePicturePreview` devolve uma foto em tamanho reduzido, o que ajuda a ficar abaixo do limite de 1 MB do Firestore.

### 5.3. Gravar com o resumo simulado

```kotlin
class DocumentoRepository {
    private val col = Firebase.firestore.collection("documentos")

    fun registrar(pacienteId: String, medicoId: String, nome: String, tipo: String, base64: String = "") {
        val resumoFixo = listOf(
            PontoResumo("[Ponto relevante 1 extraído do exame]", "pág. 1, linha 9"),
            PontoResumo("[Ponto relevante 2 extraído do exame]", "pág. 1, linha 12"),
            PontoResumo("[Ponto relevante 3 extraído do exame]", "pág. 2, linha 3")
        )
        col.add(Documento(
            pacienteId = pacienteId, medicoId = medicoId,
            nome = nome, tipo = tipo, imagemBase64 = base64,
            resumo = resumoFixo
        ))
    }
}
```

O `medicoId` vem do documento `usuarios/{uid}` do paciente (campo `medicoId`).

### 5.4. Fila do médico e confirmação (RF-20 a RF-22)

Estas funções ficam **dentro da mesma `class DocumentoRepository`** do item 5.3.

```kotlin
fun ouvirPendentes(medicoId: String, onMudou: (List<Documento>) -> Unit): ListenerRegistration =
    col.whereEqualTo("medicoId", medicoId)                 // RN-07
        .whereEqualTo("status", "aguardando_medico")
        .addSnapshotListener { snap, _ ->
            onMudou(snap?.toObjects(Documento::class.java).orEmpty())
        }

fun confirmar(docId: String, medicoId: String, observacao: String) {
    col.document(docId).update(
        mapOf(
            "status" to "confirmado",
            "observacao" to observacao,
            "confirmadoPor" to medicoId,
            "confirmadoEm" to FieldValue.serverTimestamp(),
            "apoioIA" to true                              // RN-06
        )
    )
}

fun corrigir(docId: String, medicoId: String, observacao: String) {
    col.document(docId).update(
        mapOf(
            "status" to "corrigido",
            "observacao" to observacao,
            "confirmadoPor" to medicoId,
            "confirmadoEm" to FieldValue.serverTimestamp(),
            "apoioIA" to true
        )
    )
}
```

- `update()` muda **só** os campos listados e mantém o resto do documento.
- `FieldValue.serverTimestamp()` grava a hora do servidor, que vale como registro de quando o médico confirmou.

### 5.5. Funções que outros módulos usam

O Início (Módulo 1) e a Agenda do dia (Módulo 4) **leem documentos pelo seu repositório**, nunca direto no Firestore. Além do `ouvirPendentes` acima, crie também:

```kotlin
// Dentro de DocumentoRepository — usada pelo Início do paciente
fun ouvirDoPaciente(pacienteId: String, limite: Long, onMudou: (List<Documento>) -> Unit): ListenerRegistration =
    col.whereEqualTo("pacienteId", pacienteId)
        .orderBy("enviadoEm", Query.Direction.DESCENDING)
        .limit(limite)
        .addSnapshotListener { snap, _ ->
            onMudou(snap?.toObjects(Documento::class.java).orEmpty())
        }
```

- Todas as funções `ouvir...` devolvem o `ListenerRegistration`, para quem chamou poder parar de ouvir.
- `ouvirDoPaciente` usa `whereEqualTo` + `orderBy` em campos diferentes, então pede **índice composto**: na primeira execução, abra o link do Logcat e clique em "Criar índice".
- **Suba essas funções cedo** (num PR pequeno): os Módulos 1 e 4 dependem delas.

### 5.6. Card do resumo (reaproveitado nas duas telas)

```kotlin
@Composable
fun CardResumo(resumo: List<PontoResumo>) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Resumo do Goianinho", style = MaterialTheme.typography.labelLarge)
        resumo.forEach { ponto ->
            Column {
                Text(ponto.texto, style = MaterialTheme.typography.bodyMedium)
                Text(ponto.fonte, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(
            "Resumo organizado pela IA. O resultado só vale após confirmação do médico.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
```

---

## 6. O que fica simulado

- A leitura do exame: o resumo é sempre o mesmo texto de exemplo, com fontes fictícias.
- O armazenamento do arquivo: só os dados vão para o Firestore.
- Não use valores clínicos reais em nenhum momento.

## 7. Critérios de aceite

- [ ] Documento selecionado aparece no Firestore e na lista do paciente
- [ ] Documento novo chega na fila do médico como "Aguardando médico"
- [ ] Confirmar e Corrigir mudam o status nas duas telas
- [ ] Os campos de registro são gravados ao confirmar
- [ ] Nenhum documento do Firestore passa de 1 MB
- [ ] Visual das duas telas igual ao protótipo

## 8. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| Abrir arquivo / câmera | [Guia: Get a result from an activity](https://developer.android.com/training/basics/intents/result) | `rememberLauncherForActivityResult`, `OpenDocument`, `TakePicturePreview` |
| Seletor de fotos | [Guia: Photo picker](https://developer.android.com/training/data-storage/shared/photopicker) | Alternativa para escolher imagem |
| Cards e listas | [Curso Android Basics with Compose — Unidade 3](https://developer.android.com/courses/android-basics-compose/unit-3) | Card do documento, lista de anteriores |
| ViewModel | [Codelab: ViewModel and State in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state) | `DocumentosViewModel` |
| Gravar dados | [Firebase: Add data](https://firebase.google.com/docs/firestore/manage-data/add-data) | `add()`, `update()`, `FieldValue.serverTimestamp()` |
| Tempo real | [Firebase: Get realtime updates](https://firebase.google.com/docs/firestore/query-data/listen) | Fila do médico atualizando sozinha |
| Filtros | [Firebase: Perform simple and compound queries](https://firebase.google.com/docs/firestore/query-data/queries) | `whereEqualTo` por médico e status |
| Limites do Firestore | [Firebase: Usage and limits](https://firebase.google.com/docs/firestore/quotas) | Limite de 1 MB por documento |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
