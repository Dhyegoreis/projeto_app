# Base do líder da Fase 2 (v1.6) e correção da documentação

- **Data:** 22/11/2026
- **Branch:** `lider/atualizacao-v1.6` (criada a partir da `main` em d231e97)
- **Autor:** Dhyego Ferreira dos Reis (líder e arquitetura)
- **Referência:** `docs/Documentacao-Goianinho-Medicina-v1.6.pdf`, seções 5, 10 e 11 (ordem de trabalho na p.45)

## 1. Intenção

A v1.6 abriu a Fase 2: as telas passam a usar dados reais, com tratamento de erro e regras de negócio. A documentação de cada módulo já foi escrita no commit d88efcb (README e os 5 `doc.md`, com 56 erros mapeados). Ainda assim, o time não consegue começar.

O motivo é o passo 1 da "Ordem de trabalho" (p.45): **o líder precisa entregar na main a base compartilhada**, e ela ainda não existe no código. Enquanto isso não acontece:
- o README e os `doc.md` citam arquivos que não existem;
- os integrantes não têm o `AppContainer`, as `Constantes` nem a função `mensagemDeErro` em que os padrões obrigatórios se apoiam.

Esta entrega tem três partes:
1. o código da base do líder;
2. as regras do Firestore no padrão v1.6, escritas mas **não publicadas**;
3. a correção das inconsistências da documentação (PDF no Claude Docs e documentos do repositório), sem mudar de versão: continua **v1.6**.

## 2. Objetivos

1. Criar na `main` (via PR desta branch) tudo o que o PDF atribui ao líder no passo 1, de forma que cada integrante faça `git pull origin main` e trabalhe só na sua pasta.
2. O app continua compilando e funcionando exatamente como hoje depois do merge. A base só **acrescenta**; nenhuma tela muda de comportamento.
3. Deixar o `firestore.rules` com as regras por usuário da v1.6, corrigidas, pronto para o líder publicar depois que os 4 módulos migrarem.
4. Corrigir a documentação: o PDF (fonte no Claude Docs), o README e os `doc.md`.

## 3. Não objetivos

- **Migrar os repositórios e as ViewModels dos módulos para o `AppContainer`.** Pela decisão B, cada integrante faz isso na própria branch.
- Corrigir os 56 erros listados nos `doc.md`. São do respectivo módulo.
- Remover "Assumir paciente" das telas. É do Módulo 1 (H-xx), e o código continua lá até o módulo migrar.
- Publicar as regras no console agora.
- Criar o médico de teste e os horários no Firestore. É um passo manual do líder no console, só descrito aqui (seção 7).
- IA real, Storage, videochamada (seção 12 do PDF).

## 4. Anti-objetivos (a entrega falha, mesmo funcionando, se…)

- **Quebrar a main.** Isso acontece se o app deixar de compilar ou se alguma tela atual mudar de comportamento depois do merge.
- **Forçar conflito nas branches `feature/*`.** Esta entrega não altera código (`.kt`) dentro de `ui/login`, `ui/home`, `ui/chat`, `ui/documentos`, `ui/agenda` nem `data/repository/`. **Exceção única: os 5 `doc.md`**, que são documentação do líder. Hoje as `feature/*` não têm commits além da `main`, então o risco de conflito nesses arquivos é baixo; os integrantes fazem `git pull origin main` depois do merge.
- **Publicar regras que derrubem telas.** Isso inclui o chat do médico com `pacienteId == ""` e o "Assumir paciente", que ainda estão em uso.
- **Deixar a documentação dizendo que algo está pronto quando não está.** Isso vale, por exemplo, para a migração dos módulos.

## 5. Abordagens consideradas

| Abordagem | O que faz | Prós | Contras |
|---|---|---|---|
| A. Base + ligar os repositórios existentes | O líder cria a base e já muda os construtores dos repositórios e as `Factory` das ViewModels | A main fica coerente de uma vez | Mexe em pastas de módulo e gera conflito com `feature/*` |
| **B. Só a base (escolhida)** | O líder cria a base; cada módulo migra o próprio repositório e as próprias ViewModels | Respeita "cada um na sua pasta"; é o que os `doc.md` já mandam | A main convive um tempo com dois padrões (`getInstance()` direto e `AppContainer`) |

**Escolhida: B**, decisão do líder em 22/11/2026.

## 6. O que será construído

### 6.1 Gradle (`gradle/libs.versions.toml` e `app/build.gradle.kts`)

- `coroutinesPlayServices`: de `1.8.1` para **`1.10.2`**.
- Novo `coil = "2.7.0"` e a biblioteca `coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }`.
- Em `app/build.gradle.kts`: `implementation(libs.coil.compose)`. O `kotlinx-coroutines-play-services` já está declarado.
- O projeto usa o Kotlin embutido no AGP 9.1.1 (não aplica `org.jetbrains.kotlin.android`); o `kotlin = "2.0.21"` do catálogo só alimenta o plugin do Compose. Conferir a versão real do compilador (`./gradlew buildEnvironment` ou a saída do build). Se `coroutines-play-services` 1.10.2 ou o Coil 2.7.0 forem incompatíveis com ela e o build falhar, **parar e reportar** ao líder. Não mexer em versões de Kotlin/AGP por conta própria.

### 6.2 `GoianinhoApp.kt` (raiz do pacote `com.goianinho.medicina`)

```kotlin
class GoianinhoApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
```

Registrar `android:name=".GoianinhoApp"` na tag `<application>` do `AndroidManifest.xml`.

**Regra geral de nomes:** onde os `doc.md` (já entregues ao time) usam um nome, o código usa **o mesmo nome**. Os nomes abaixo já seguem os `doc.md`.

### 6.3 `data/AppContainer.kt`

- Cria **uma vez** `private val auth = FirebaseAuth.getInstance()` e `private val db = FirebaseFirestore.getInstance()` (privados, como no Padrão 1 dos `doc.md`).
- Expõe os 4 repositórios **com as classes que já existem hoje**, sem alterá-las:
  - `val authRepository = AuthRepository(auth, db)`. O construtor atual já aceita `auth` e `db` (com valores padrão), então essa linha já está no formato final.
  - `val chatRepository = MensagemRepository()`
  - `val documentoRepository = DocumentoRepository()`
  - `val agendaRepository = AgendaRepository()`

  Os nomes das propriedades seguem o PDF e os `doc.md`. Os três últimos ainda não recebem parâmetros; o módulo troca a própria linha quando migrar (por exemplo `ChatRepository(db)`, `DocumentoRepository(db)`, `AgendaRepository(db)`).
- Comentário no topo explicando a migração (decisão B). Quando o módulo X migrar, ele:
  1. muda o construtor do próprio repositório para receber `db` (Módulos 2, 3 e 4; o Módulo 1 já recebe);
  2. troca a **sua própria linha** neste arquivo, por exemplo `AuthRepository(auth, db)`;
  3. passa a usar `viewModel(factory = XViewModel.Factory)`.

  Essa linha é a exceção autorizada pelo líder à regra dos arquivos compartilhados: cada módulo edita só a sua linha.
- `MensagemRepository` mantém o nome por enquanto. O `ui/chat/doc.md` já manda o Módulo 2 renomear para `ChatRepository(db)` na migração. A propriedade já se chama `chatRepository`.

### 6.4 `data/Constantes.kt`

Constantes **de nível de arquivo** (sem `object`), porque os `doc.md` as usam sem prefixo (`db.collection(COL_MENSAGENS)`):

```kotlin
package com.goianinho.medicina.data

// RN-12: todo paciente novo é vinculado a este médico.
// TODO(líder): trocar pelo UID real do médico de teste criado no console (ver spec, seção 7).
const val MEDICO_TESTE_ID = "COLOQUE_AQUI_O_UID_DO_MEDICO_TESTE"

const val COL_USUARIOS = "usuarios"
const val COL_MENSAGENS = "mensagens"
const val COL_DOCUMENTOS = "documentos"
const val COL_HORARIOS = "horarios"
const val COL_CONSULTAS = "consultas"
```

Se o UID real já estiver disponível quando a tarefa for executada, ele entra no lugar do placeholder.

### 6.5 `ui/comum/Mensagens.kt`

Contém a função `fun mensagemDeErro(erro: Throwable): String` e as constantes de aviso. Os textos são **exatamente** os da tabela "Mensagens padrão" do PDF (p.38):

| Entrada | Saída |
|---|---|
| `FirebaseAuthWeakPasswordException` (checar **antes** de InvalidCredentials, que é a superclasse) | "A senha precisa ter pelo menos 6 caracteres." |
| `FirebaseAuthInvalidCredentialsException`, `FirebaseAuthInvalidUserException` | "E-mail ou senha incorretos." |
| `FirebaseAuthUserCollisionException` | "Este e-mail já tem uma conta. Tente entrar." |
| `FirebaseNetworkException`, `TimeoutCancellationException`, `FirebaseFirestoreException` com código `UNAVAILABLE` ou `DEADLINE_EXCEEDED` | "Sem conexão. Verifique sua internet e tente de novo." |
| `FirebaseFirestoreException` com código `PERMISSION_DENIED` | "Você não tem acesso a esta informação." |
| `HorarioOcupadoException` (nome do `ui/agenda/doc.md`; a classe é criada **nesta base**, em `ui/comum/Mensagens.kt`, e o Módulo 4 só a lança na transação, RN-08) | "Esse horário acabou de ser reservado. Escolha outro." |
| qualquer outro | "Algo deu errado. Tente de novo." |

Regras da função:
- Procura a causa na cadeia (`cause`, até 10 níveis), como já faz `traduzirErroFirestore`, porque transações embrulham o erro.
- Para `FAILED_PRECONDITION`, que é o índice faltando, devolve a mensagem genérica e grava `Log.e("Goianinho", …)` com a mensagem técnica. Assim o desenvolvedor vê o link do índice no Logcat (RNF-07). O log não pode conter senha nem dado de saúde (RNF-08).

A foto grande **não** é exceção: o `ui/documentos/doc.md` trata como retorno `null` e `vm.avisarFotoGrande()`. A base só fornece o texto.

Constantes de nível de arquivo (os `doc.md` usam `AVISO_VOZ` sem prefixo):

```kotlin
const val AVISO_GOOGLE = "Entrar com Google chega em breve. Use e-mail e senha."
const val AVISO_VOZ = "O comando de voz chega em breve. Por enquanto, digite sua mensagem."
const val AVISO_VIDEOCHAMADA = "A videochamada ainda não está disponível nesta versão."
const val ERRO_FOTO_GRANDE = "A foto é grande demais. Tente de novo com menos zoom ou mais longe."
```

### 6.5.1 `ui/comum/EstadosDeTela.kt`

O Padrão 2 dos `doc.md` usa `TelaCarregando()`, `TelaErro(mensagem, onTentarDeNovo)` e `EstadoVazio(texto)`, mas nenhum arquivo os fornece. Como todas as telas vão usar, entram na base (decisão do líder):
- `TelaCarregando()`: indicador centralizado.
- `TelaErro(mensagem: String, onTentarDeNovo: () -> Unit)`: texto e botão "Tentar de novo" (área de toque ≥ 48 dp).
- `EstadoVazio(texto: String)`: texto explicativo centralizado (RNF-05).

Usam as cores e tipografia de `ui/theme`, `Modifier.fillMaxSize()` com conteúdo centralizado e nada de altura fixa (RNF-04).

`ErrosAuth.kt` e `ErrosFirestore.kt`, em `data/repository/`, **continuam existindo sem nenhuma alteração**, porque os módulos ainda os usam e mexer neles geraria conflito com as branches `feature/*` (anti-objetivo). A orientação de migrar de `traduzirErroAuth`/`traduzirErroFirestore` para `mensagemDeErro` vai nos `doc.md` (seção 6.8).

### 6.6 `data/model/Mensagem.kt`

Acrescentar `val donoId: String = ""` (o uid de quem conversa: o paciente na conversa dele, o médico na conversa dele com o Goianinho), logo após `id`. O valor padrão vazio mantém compatíveis a leitura dos documentos antigos e o código atual que cria `Mensagem(...)` com argumentos nomeados.

### 6.7 `firestore.rules`, escrito e não publicado

O arquivo é reescrito para o modelo v1.6. O cabeçalho deve avisar em destaque:

> NÃO PUBLICAR antes de os 4 módulos migrarem (donoId no chat, remoção do Assumir paciente, vínculo RN-12). Quem publica é o líder.

A base continua sendo o arquivo atual (`logado()`, `eu()`, `meuPerfil()`, `souMedico()`, `soMudou()`), com estas mudanças:

- **usuarios**
  - **read:** `eu() == uid`, ou o documento lido tem `medicoId == eu()` (o médico lê os pacientes dele), ou `uid == meuPerfil().medicoId` (o paciente lê o próprio médico). Isso corrige a lacuna "qualquer logado lê todos".
  - **create:** `eu() == uid` e `perfil in ["paciente","medico"]`. Para um médico, `medicoId == ""`. Para um paciente, `medicoId != ""` e `get(usuarios/medicoId).data.perfil == "medico"` (RN-12, sem precisar escrever o UID nas regras).
  - **update:** o próprio usuário, sem mudar `perfil` nem `medicoId` (RN-01). **O ramo "Assumir paciente" sai.**
  - **delete:** proibido.
- **mensagens**
  - **read:** `resource.data.donoId == eu()`.
  - **create:** `request.resource.data.donoId == eu()` e `autor in ["usuario","goianinho"]`, e ainda:
    - se quem grava é paciente: `pacienteId == eu()` e `medicoId == meuPerfil().medicoId`;
    - se quem grava é médico (conversa dele com o Goianinho): `medicoId == eu()` e `pacienteId == ""`.
  - **update/delete:** proibido, **de propósito**. Nenhuma tela edita mensagem; isso precisa ficar escrito como decisão.
- **documentos:** como hoje, que já atende RN-06 e RN-07. Acrescentar a RN-13 no update: `status == "corrigido"` exige `observacao` não vazia.
- **horarios:**
  - read se logado;
  - update de paciente só `livre: true → false`, mudando apenas `livre` (RN-08);
  - o médico mantém create/update/delete dos próprios horários (já existe e não contradiz a RN-09, que trata do cadastro manual).
- **consultas:**
  - read e create como hoje;
  - **update proibido, de propósito**, porque nenhuma funcionalidade da v1.6 altera uma consulta. Quando surgir "cancelar" ou "concluir", a regra entra junto. Isso precisa ficar escrito como decisão.

Validação: não há emulador no projeto. O executor revisa as regras contra cada operação que os repositórios fazem **depois** da migração descrita nos `doc.md` e lista no PR as operações conferidas. Se o Firebase CLI estiver disponível, roda também uma checagem de sintaxe com o emulador; se não estiver, registra que não rodou.

### 6.8 Documentação do repositório

- **`README.md`:**
  - A estrutura de pastas passa a refletir o que existe de fato.
  - Na seção "Fase atual — v1.6", o passo 1 (Líder) fica marcado como entregue e ganha uma nota curta sobre a decisão B.
  - Acrescentar uma nota sobre as regras: estão escritas em `firestore.rules` e publicadas só depois da migração.
- **Os 5 `doc.md`, só na seção "O que o líder entrega na main":**
  - marcar como entregue;
  - explicar a migração do módulo: construtor do repositório, a própria linha no `AppContainer`, `Factory`, troca de `traduzirErro*` por `mensagemDeErro`;
  - avisar que o `MEDICO_TESTE_ID` pode estar como placeholder até o líder criar o médico de teste.

  No `ui/chat/doc.md`, deixar claro que `donoId` já existe no modelo e que gravá-lo é tarefa do módulo.
  Esta entrega **não** reescreve as tabelas de erros nem os critérios.
- **`ui/agenda/doc.md`:** o trecho que manda o Módulo 4 criar `HorarioOcupadoException` e "combinar com o líder" passa a dizer que a classe já existe em `ui/comum/Mensagens.kt`.
- **Todos os `doc.md`:** onde o Padrão 2 usa `TelaCarregando`/`TelaErro`/`EstadoVazio`, citar que vêm de `ui/comum/EstadosDeTela.kt`.
- Conferir que todo nome da base citado nos `doc.md` e no README existe no código (`COL_*`, `AVISO_*`, `MEDICO_TESTE_ID`, `mensagemDeErro`, `HorarioOcupadoException`, `AppContainer` com `authRepository`/`chatRepository`/`documentoRepository`/`agendaRepository`).

### 6.9 Documentação no Claude Docs (fonte do PDF), mantendo v1.6

Fonte: https://claude.ai/artifact/AvNfDgcfVoRwGY94n2YeMm

Correções:
1. **RF-37 (Sair)** na tabela de requisitos do Módulo 1 e **RF-38 ("Goianinho está digitando…")** na do Módulo 2, com a prioridade MoSCoW. Proposta: RF-37 Must, RF-38 Should.
2. **Regras de segurança** (seções 10 e 11): alinhar o texto ao `firestore.rules` da seção 6.7, incluindo:
   - a leitura restrita de `usuarios`;
   - a validação de vínculo da RN-12;
   - a RN-13 nas regras;
   - as decisões explícitas sobre update em `mensagens` e `consultas`.
3. **Ordem de trabalho (p.45):** acrescentar a decisão B. O líder entrega a base; cada módulo migra o próprio repositório, a própria linha do `AppContainer` e a troca para `mensagemDeErro`; as regras são publicadas só depois da migração dos 4 módulos.
4. **Mensagens padrão:** citar a `HorarioOcupadoException` (na base) e o texto `ERRO_FOTO_GRANDE`; citar `TelaCarregando`/`TelaErro`/`EstadoVazio` em `ui/comum/EstadosDeTela.kt`.
5. **Capa:** manter a data de criação e acrescentar "Atualizado em 22/11/2026".
6. **Histórico:** complementar a linha da 1.6 com "revisão: RF-37/38 nas tabelas, regras de segurança completas, decisão de migração por módulo".

Depois disso, o líder exporta o PDF de novo e substitui `docs/Documentacao-Goianinho-Medicina-v1.6.pdf` (mesmo nome). Essa troca entra num commit desta branch.

## 7. Passo manual do líder (fora do código)

1. Criar no app (Criar conta, perfil Médico) ou no console Auth a conta `medico.teste@goianinho.com`.
2. Se a conta foi criada pelo console, criar também `usuarios/{uid}` com `nome`, `email`, `perfil: "medico"`, `medicoId: ""`, `criadoEm`.
3. Copiar o UID para `MEDICO_TESTE_ID` em `data/Constantes.kt` (commit `chore: médico de teste`).
4. Criar horários de exemplo em `horarios`: `{ medicoId: <uid>, data: "AAAA-MM-DD", hora: "HH:MM", livre: true }`, de 3 a 5 por dia nos próximos 5 dias úteis.

   Antes de cadastrar, conferir o formato de `data` e `hora` que o `AgendaRepository` atual espera.
5. Antes de publicar as regras, ajustar os `usuarios` de **paciente** que já existem (incluindo `paciente.teste@goianinho.com`) para `medicoId = MEDICO_TESTE_ID` pelo console. Sem isso, eles ficam bloqueados no chat, nos documentos e nas consultas.
6. Antes de publicar as regras, **apagar a coleção `mensagens`** pelo console. São dados de teste sem `donoId` e ficariam invisíveis (decisão do líder: o histórico pode ser perdido).
7. Publicar `firestore.rules` só depois que os 4 PRs de migração estiverem na main e os passos 5 e 6 estiverem feitos.

## 8. Como saber que ficou pronto

- `./gradlew assembleDebug` passa.
- `./gradlew test` passa, com testes unitários de `mensagemDeErro` cobrindo cada linha da tabela 6.5, inclusive a precedência WeakPassword → InvalidCredentials e o erro embrulhado em `cause`. Se as exceções do Firebase não puderem ser instanciadas na JVM pura, testar o que for possível e registrar no PR o que ficou de fora.
- O app abre e as telas atuais funcionam como antes. Conferência manual: Login e um fluxo de cada módulo.
- Nenhum arquivo dentro de `ui/login`, `ui/home`, `ui/chat`, `ui/documentos`, `ui/agenda` ou `data/repository/` aparece no diff (`git diff --stat main`), **exceto os 5 `doc.md`**.
- Documentação: README e `doc.md` atualizados conforme 6.8; Claude Docs corrigido conforme 6.9; PDF reexportado.

## 9. Decisões deixadas para quem implementar

- Detalhes de Kotlin: os imports, os nomes de parâmetros internos e se as exceções novas ficam em `Mensagens.kt` ou num arquivo vizinho em `ui/comum/`.
- Como organizar os commits (sugestão: um commit por item da seção 6, no formato `feat(base): …`, `docs: …`).
