package com.goianinho.medicina.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goianinho.medicina.data.model.Consulta
import com.goianinho.medicina.data.model.Documento
import com.goianinho.medicina.data.repository.AgendaRepository
import com.goianinho.medicina.data.repository.AuthRepository
import com.goianinho.medicina.data.repository.DocumentoRepository
import com.goianinho.medicina.data.repository.RespostasSimuladas
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

// Estado das duas telas de Início (paciente e médico). O Início só lê (RN-10).
data class HomeUiState(
    val perfil: String = "",            // "paciente" | "medico"
    val nome: String = "",
    val dataHoje: String = "",          // "Quinta, 23/10"
    val nomeMedico: String = "",        // paciente: médico vinculado
    val temMedico: Boolean = true,
    val proximaConsulta: Consulta? = null,
    val documentos: List<Documento> = emptyList(),
    val consultasHoje: List<Consulta> = emptyList(),
    val pendentes: List<Documento> = emptyList(),
    val semResposta: Int = RespostasSimuladas.PACIENTES_SEM_RESPOSTA,
    val proximaDoDia: Consulta? = null,
    val nomesPacientes: Map<String, String> = emptyMap(),
    val erro: String? = null
)

class HomeViewModel(
    private val auth: AuthRepository = AuthRepository(),
    private val documentoRepo: DocumentoRepository = DocumentoRepository(),
    private val agendaRepo: AgendaRepository = AgendaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(dataHoje = dataDeHoje()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var listenerDocumentos: ListenerRegistration? = null
    private var listenerConsulta: ListenerRegistration? = null
    private var listenerPendentes: ListenerRegistration? = null
    private var listenerConsultasDia: ListenerRegistration? = null

    private val nomesPacientes = mutableMapOf<String, String>()
    private val nomesEmBusca = mutableSetOf<String>()

    init {
        val logado = auth.usuarioLogadoUid
        if (logado.isNullOrBlank()) {
            _uiState.update { it.copy(erro = "Sessão expirada. Entre novamente.") }
        } else {
            viewModelScope.launch {
                try {
                    val usuario = auth.buscarUsuario(logado)
                    _uiState.update {
                        it.copy(perfil = usuario.perfil, nome = usuario.nome, erro = null)
                    }
                    when (usuario.perfil) {
                        "medico" -> iniciarComoMedico(logado)
                        "paciente" -> iniciarComoPaciente(logado, usuario.medicoId)
                        else -> _uiState.update { it.copy(erro = "Perfil de usuário inválido.") }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("Goianinho", "Início: falha ao buscar o perfil", e)
                    _uiState.update { it.copy(erro = "Não foi possível carregar seu perfil.") }
                }
            }
        }
    }

    private fun iniciarComoPaciente(uid: String, medicoId: String) {
        _uiState.update { it.copy(temMedico = medicoId.isNotBlank()) }
        listenerDocumentos?.remove()
        listenerDocumentos = documentoRepo.ouvirDoPaciente(uid, 5, onErro = ::mostrarErro) { lista ->
            _uiState.update { it.copy(documentos = lista) }
        }
        listenerConsulta?.remove()
        listenerConsulta = agendaRepo.ouvirProximaConsulta(uid, onErro = ::mostrarErro) { consulta ->
            _uiState.update { it.copy(proximaConsulta = consulta) }
        }
        if (medicoId.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val nome = auth.buscarUsuario(medicoId).nome
                    _uiState.update { it.copy(nomeMedico = nome) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("Goianinho", "Início: falha ao buscar o nome do médico", e)
                }
            }
        }
    }

    private fun iniciarComoMedico(uid: String) {
        listenerPendentes?.remove()
        listenerPendentes = documentoRepo.ouvirPendentes(uid, onErro = ::mostrarErro) { lista ->
            _uiState.update { it.copy(pendentes = lista) }
            lista.forEach { buscarNomePaciente(it.pacienteId) }
        }
        listenerConsultasDia?.remove()
        listenerConsultasDia = agendaRepo.ouvirConsultasDoDia(
            uid, inicioDoDia(0), inicioDoDia(1), onErro = ::mostrarErro
        ) { todas ->
            val lista = todas.filter { it.status != "cancelada" }
            val agora = Timestamp.now()
            val proxima = lista.firstOrNull { c -> c.dataHora != null && c.dataHora >= agora }
            _uiState.update {
                it.copy(consultasHoje = lista, proximaDoDia = proxima)
            }
            lista.forEach { buscarNomePaciente(it.pacienteId) }
        }
    }

    // Cache de nomes com guarda de busca em andamento
    private fun buscarNomePaciente(id: String) {
        if (id.isBlank() || nomesPacientes.containsKey(id) || id in nomesEmBusca) return
        nomesEmBusca += id
        viewModelScope.launch {
            try {
                nomesPacientes[id] = auth.buscarUsuario(id).nome
                _uiState.update { it.copy(nomesPacientes = nomesPacientes.toMap()) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Goianinho", "Início: falha ao buscar o nome do paciente", e)
            } finally {
                nomesEmBusca.remove(id)
            }
        }
    }

    private fun mostrarErro(mensagem: String) {
        _uiState.update { it.copy(erro = mensagem) }
    }

    private fun inicioDoDia(diasAFrente: Int): Timestamp {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, diasAFrente)
        }
        return Timestamp(c.time)
    }

    fun sair() {
        listenerDocumentos?.remove()
        listenerConsulta?.remove()
        listenerPendentes?.remove()
        listenerConsultasDia?.remove()
        auth.sair()
    }

    override fun onCleared() {
        listenerDocumentos?.remove()
        listenerConsulta?.remove()
        listenerPendentes?.remove()
        listenerConsultasDia?.remove()
        super.onCleared()
    }
}

// "Quinta, 23/10"
private fun dataDeHoje(): String {
    val locale = Locale("pt", "BR")
    val hoje = LocalDate.now()
    val dia = hoje.dayOfWeek
        .getDisplayName(java.time.format.TextStyle.FULL, locale)
        .substringBefore("-")
        .replaceFirstChar { it.titlecase(locale) }
    return "$dia, ${hoje.format(DateTimeFormatter.ofPattern("dd/MM", locale))}"
}
