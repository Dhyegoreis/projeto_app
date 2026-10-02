# Módulo 1 — Login (parte 1 de 2)

| | |
|---|---|
| **Responsável** | [INTEGRANTE 1] |
| **Branch** | `feature/login` |
| **Pastas** | `ui/login` (este doc) e `ui/home` (ver `ui/home/doc.md`) |
| **Telas do protótipo** | 1 · Login · [abrir protótipo](https://claude.ai/artifact/4mYTUtWr2hiNwCJp4pTK9Z) |
| **Documentação completa** | Seção 6 da Documentação do Projeto |

> **Regra do time:** o Módulo 1 entrega **as duas visões**. Aqui o Login é compartilhado (serve aos dois perfis), e o Início é feito em duas versões: paciente e médico (ver `ui/home/doc.md`).

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

## 1. O que esta tela faz

É a porta de entrada do app. Identifica o usuário, descobre se ele é **paciente** ou **médico** e manda cada um para o seu Início.

**Elementos (iguais ao protótipo):** seletor Paciente/Médico, campos de e-mail e senha, link "Esqueci minha senha", botão Entrar, botão "Continuar com Google" e link "Criar conta".

## 2. Arquivos que você cria

```
ui/login/
 ├── LoginScreen.kt        → a tela (Composable)
 ├── LoginViewModel.kt     → estado da tela + ações
 └── CadastroScreen.kt     → tela "Criar conta" (RF-04)
data/repository/
 └── AuthRepository.kt     → única classe que fala com o Firebase Auth
```

## 3. Requisitos

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | Login com e-mail e senha via Firebase Auth | Must |
| RF-02 | Escolher o perfil (Paciente ou Médico) | Must |
| RF-03 | Após o login, abrir o Início do paciente ou do médico, conforme o perfil **salvo** | Must |
| RF-04 | Criar conta com nome, e-mail, senha e perfil | Must |
| RF-05 | Recuperar senha por e-mail | Should |
| RF-06 | Login com Google | Could (fica para depois) |
| RF-07 | Manter o usuário logado ao reabrir | Should |

- **RN-01:** o perfil fica salvo em `usuarios/{uid}`. Na próxima entrada vale o perfil salvo, não o seletor.
- **RN-02:** senha com no mínimo 6 caracteres (limite do Firebase Auth).

## 4. Dados no Firestore

Coleção `usuarios`, um documento por usuário, com o **id do documento = uid** do Firebase Auth.

```kotlin
// data/model/Usuario.kt (criado na fundação — não altere sem PR avisado)
data class Usuario(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val perfil: String = "paciente",   // "paciente" ou "medico"
    val medicoId: String = "",         // só para paciente: o médico vinculado
    val canalPreferido: String = ""    // "whatsapp", "app", "ligacao"
)
```

> Todo campo tem valor padrão (`= ""`) porque o Firestore precisa de um construtor vazio para transformar o documento em objeto com `toObject()`.

---

## 5. Como implementar (passo a passo)

Os padrões abaixo seguem as aulas **"ViewModel and State in Compose"** e **"Add Firebase to an Android app"** dos cursos do Google (links na seção 8).

### 5.1. Repository — fala com o Firebase

```kotlin
class AuthRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    fun usuarioLogado() = auth.currentUser          // null = ninguém logado (RF-07)

    fun entrar(
        email: String, senha: String,
        onSucesso: (perfil: String) -> Unit,
        onErro: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid ?: return@addOnSuccessListener
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc -> onSucesso(doc.getString("perfil") ?: "paciente") }
                    .addOnFailureListener { onErro("Não foi possível carregar seu perfil.") }
            }
            .addOnFailureListener { onErro("E-mail ou senha incorretos.") }
    }

    fun criarConta(
        nome: String, email: String, senha: String, perfil: String,
        onSucesso: () -> Unit, onErro: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid ?: return@addOnSuccessListener
                val usuario = Usuario(uid = uid, nome = nome, email = email, perfil = perfil)
                db.collection("usuarios").document(uid).set(usuario)
                    .addOnSuccessListener { onSucesso() }
                    .addOnFailureListener { onErro("Conta criada, mas o perfil não foi salvo.") }
            }
            .addOnFailureListener { onErro("Não foi possível criar a conta.") }
    }

    fun recuperarSenha(email: String, onFim: (Boolean) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { onFim(it.isSuccessful) }
    }

    fun sair() = auth.signOut()

    // Usada pelo Início (ui/home) para mostrar o nome na saudação
    fun buscarUsuario(uid: String, onResultado: (Usuario?) -> Unit) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc -> onResultado(doc.toObject(Usuario::class.java)) }
            .addOnFailureListener { onResultado(null) }
    }
}
```

**Por que assim:** a tela nunca chama o Firebase direto. Se um dia o login mudar, só esta classe muda.

> `buscarUsuario` também é usada pelo **Início** (ver `ui/home/doc.md`), que lê o nome do usuário por aqui em vez de acessar o Firestore direto.

### 5.2. ViewModel — guarda o estado da tela

```kotlin
data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val perfil: String = "paciente",
    val carregando: Boolean = false,
    val erro: String? = null
)

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(valor: String) = _uiState.update { it.copy(email = valor, erro = null) }
    fun onSenhaChange(valor: String) = _uiState.update { it.copy(senha = valor, erro = null) }
    fun onPerfilChange(valor: String) = _uiState.update { it.copy(perfil = valor) }

    fun entrar(onSucesso: (perfil: String) -> Unit) {
        val s = _uiState.value
        if (s.email.isBlank() || s.senha.length < 6) {
            _uiState.update { it.copy(erro = "Preencha o e-mail e uma senha de 6+ caracteres.") }
            return
        }
        _uiState.update { it.copy(carregando = true, erro = null) }
        repo.entrar(s.email, s.senha,
            onSucesso = { perfil ->
                _uiState.update { it.copy(carregando = false) }
                onSucesso(perfil)
            },
            onErro = { msg -> _uiState.update { it.copy(carregando = false, erro = msg) } }
        )
    }
}
```

- `MutableStateFlow` é privado: só o ViewModel altera o estado.
- `StateFlow` é público e somente leitura: a tela apenas observa.
- `update { it.copy(...) }` troca só os campos necessários e mantém o resto.

### 5.3. Tela — só desenha e repassa cliques

```kotlin
@Composable
fun LoginScreen(
    onEntrouComoPaciente: () -> Unit,
    onEntrouComoMedico: () -> Unit,
    onCriarConta: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Seu cuidado, organizado por quem conversa com você.",
            style = MaterialTheme.typography.headlineSmall
        )

        // Seletor Paciente / Médico (RF-02)
        val opcoes = listOf("paciente" to "Paciente", "medico" to "Médico")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            opcoes.forEachIndexed { index, (valor, rotulo) ->
                SegmentedButton(
                    selected = state.perfil == valor,
                    onClick = { viewModel.onPerfilChange(valor) },
                    shape = SegmentedButtonDefaults.itemShape(index, opcoes.size)
                ) { Text(rotulo) }
            }
        }

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.senha,
            onValueChange = viewModel::onSenhaChange,
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                viewModel.entrar { perfil ->
                    if (perfil == "medico") onEntrouComoMedico() else onEntrouComoPaciente()
                }
            },
            enabled = !state.carregando,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (state.carregando) CircularProgressIndicator(Modifier.size(22.dp))
            else Text("Entrar")
        }

        TextButton(onClick = onCriarConta, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Ainda não tem conta? Criar conta")
        }
    }
}
```

**Pontos importantes:**
- A tela **recebe funções** (`onEntrouComoPaciente`, `onEntrouComoMedico`) em vez de navegar sozinha. Quem decide para onde ir é o `navigation/`. Esse é o padrão que o Google ensina na aula de navegação e evita que os módulos dependam uns dos outros.
- `collectAsStateWithLifecycle()` faz a tela redesenhar sempre que o estado muda.
- RN-01: quem decide a tela de destino é o **perfil salvo** (`perfil` retornado do Firestore), não o seletor.

### 5.4. Manter logado (RF-07)

Combine com quem monta a navegação: ao abrir o app, se `AuthRepository().usuarioLogado() != null`, a rota inicial deixa de ser o login e passa a ser o Início do perfil salvo.

---

## 6. O que fica simulado ou para depois

- Login com Google (RF-06): só o botão visual. Precisa do SHA-1 no Firebase, e vamos fazer depois.

## 7. Critérios de aceite

- [ ] Login com dados corretos abre o Início certo para cada perfil
- [ ] Senha errada mostra mensagem clara, sem fechar o app
- [ ] Conta criada aparece no Firebase Auth **e** em `usuarios`
- [ ] Fechar e reabrir o app mantém o usuário logado
- [ ] Visual igual ao protótipo

## 8. Aulas e documentação do Google

| Tema | Onde estudar | Usar para |
|---|---|---|
| Curso completo com Firebase | [Android Basics with Compose and Firebase](https://developer.android.com/courses/android-basics-compose-firebase/course) — Unidade 5 | Visão geral de Compose + Auth + Firestore |
| Estado e ViewModel | [Codelab: ViewModel and State in Compose](https://developer.android.com/codelabs/basic-android-kotlin-compose-viewmodel-and-state) | Padrão `UiState` + `StateFlow` (5.2) |
| Estado em Compose | [Guia: State and Jetpack Compose](https://developer.android.com/develop/ui/compose/state) | `remember`, state hoisting |
| Campos de texto | [Guia: Text fields](https://developer.android.com/develop/ui/compose/text/user-input) | E-mail, senha, teclado |
| Botões segmentados | [Guia: Segmented buttons](https://developer.android.com/develop/ui/compose/components/segmented-button) | Seletor Paciente/Médico |
| Login com senha | [Firebase: Password Authentication on Android](https://firebase.google.com/docs/auth/android/password-auth) | `signIn`, `createUser`, reset de senha |
| Gravar no Firestore | [Firebase: Add data](https://firebase.google.com/docs/firestore/manage-data/add-data) | `set()` do perfil |

---

*Documento elaborado e arquitetado por **Dhyego Ferreira dos Reis**.*
