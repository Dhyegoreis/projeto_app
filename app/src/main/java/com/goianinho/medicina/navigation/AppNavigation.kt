package com.goianinho.medicina.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.goianinho.medicina.R
import com.goianinho.medicina.ui.agenda.AgendaMedicoScreen
import com.goianinho.medicina.ui.agenda.AgendaPacienteScreen
import com.goianinho.medicina.ui.agenda.ChamadaEmBreveScreen
import com.goianinho.medicina.ui.chat.ChatMedicoScreen
import com.goianinho.medicina.ui.chat.ChatPacienteScreen
import com.goianinho.medicina.ui.documentos.DocumentosScreen
import com.goianinho.medicina.ui.documentos.RevisaoScreen
import com.goianinho.medicina.ui.home.Aba
import com.goianinho.medicina.ui.home.BarraInferior
import com.goianinho.medicina.ui.home.HomeMedicoScreen
import com.goianinho.medicina.ui.home.HomePacienteScreen
import com.goianinho.medicina.ui.login.CadastroScreen
import com.goianinho.medicina.ui.login.LoginScreen

// Nomes de todas as rotas do app. Ninguém escreve rota "na mão": use sempre Rotas.X
object Rotas {
    const val LOGIN = "login"
    const val CADASTRO = "cadastro"

    const val HOME_PACIENTE = "paciente/inicio"
    const val CHAT_PACIENTE = "paciente/chat"
    const val DOCUMENTOS = "paciente/documentos"
    const val AGENDA_PACIENTE = "paciente/agenda"

    const val HOME_MEDICO = "medico/inicio"
    const val CHAT_MEDICO = "medico/chat"
    const val REVISAO = "medico/revisao"
    const val AGENDA_MEDICO = "medico/agenda"

    const val CHAMADA = "chamada"
}

private val abasPaciente = listOf(
    Aba(Rotas.HOME_PACIENTE, "Início", R.drawable.ic_home),
    Aba(Rotas.CHAT_PACIENTE, "Assistente", R.drawable.ic_chat),
    Aba(Rotas.DOCUMENTOS, "Documentos", R.drawable.ic_documento),
    Aba(Rotas.AGENDA_PACIENTE, "Agenda", R.drawable.ic_agenda)
)

private val abasMedico = listOf(
    Aba(Rotas.HOME_MEDICO, "Início", R.drawable.ic_home),
    Aba(Rotas.CHAT_MEDICO, "Goianinho", R.drawable.ic_chat),
    Aba(Rotas.REVISAO, "Revisões", R.drawable.ic_revisao),
    Aba(Rotas.AGENDA_MEDICO, "Agenda", R.drawable.ic_agenda)
)

// Troca de aba sem empilhar telas: volta ao Início do perfil e guarda o estado de cada aba
private fun NavHostController.irParaAba(rota: String, inicioDoPerfil: String) {
    navigate(rota) {
        popUpTo(inicioDoPerfil) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// Depois do login, limpa a pilha para o botão "voltar" não retornar ao login
private fun NavHostController.entrarNoPerfil(inicioDoPerfil: String) {
    navigate(inicioDoPerfil) {
        popUpTo(Rotas.LOGIN) { inclusive = true }
    }
}

@Composable
fun AppNavigation(rotaInicial: String = Rotas.LOGIN) {
    val navController = rememberNavController()
    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route

    // A barra só aparece nas telas que são abas
    val abas = when {
        abasPaciente.any { it.rota == rotaAtual } -> abasPaciente
        abasMedico.any { it.rota == rotaAtual } -> abasMedico
        else -> null
    }

    Scaffold(
        bottomBar = {
            if (abas != null) {
                BarraInferior(abas, rotaAtual) { aba ->
                    navController.irParaAba(aba.rota, abas.first().rota)
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = rotaInicial,
            modifier = Modifier.padding(padding)
        ) {
            // ---------- Módulo 1: Login e Início ----------
            composable(Rotas.LOGIN) {
                LoginScreen(
                    onEntrouComoPaciente = { navController.entrarNoPerfil(Rotas.HOME_PACIENTE) },
                    onEntrouComoMedico = { navController.entrarNoPerfil(Rotas.HOME_MEDICO) },
                    onCriarConta = { navController.navigate(Rotas.CADASTRO) }
                )
            }
            composable(Rotas.CADASTRO) {
                CadastroScreen(
                    onContaCriada = { navController.popBackStack() },
                    onVoltar = { navController.popBackStack() }
                )
            }
            composable(Rotas.HOME_PACIENTE) {
                HomePacienteScreen(
                    onAbrirChat = { navController.irParaAba(Rotas.CHAT_PACIENTE, Rotas.HOME_PACIENTE) },
                    onAbrirAgenda = { navController.irParaAba(Rotas.AGENDA_PACIENTE, Rotas.HOME_PACIENTE) },
                    onAbrirDocumentos = { navController.irParaAba(Rotas.DOCUMENTOS, Rotas.HOME_PACIENTE) }
                )
            }
            composable(Rotas.HOME_MEDICO) {
                HomeMedicoScreen(
                    onAbrirChat = { navController.irParaAba(Rotas.CHAT_MEDICO, Rotas.HOME_MEDICO) },
                    onAbrirRevisoes = { navController.irParaAba(Rotas.REVISAO, Rotas.HOME_MEDICO) },
                    onAbrirAgenda = { navController.irParaAba(Rotas.AGENDA_MEDICO, Rotas.HOME_MEDICO) }
                )
            }

            // ---------- Módulo 2: Chat ----------
            composable(Rotas.CHAT_PACIENTE) { ChatPacienteScreen() }
            composable(Rotas.CHAT_MEDICO) {
                ChatMedicoScreen(
                    onAbrirRevisao = { navController.irParaAba(Rotas.REVISAO, Rotas.HOME_MEDICO) }
                )
            }

            // ---------- Módulo 3: Documentos ----------
            composable(Rotas.DOCUMENTOS) { DocumentosScreen() }
            composable(Rotas.REVISAO) { RevisaoScreen() }

            // ---------- Módulo 4: Agenda ----------
            composable(Rotas.AGENDA_PACIENTE) {
                AgendaPacienteScreen(onEntrarNaChamada = { navController.navigate(Rotas.CHAMADA) })
            }
            composable(Rotas.AGENDA_MEDICO) {
                AgendaMedicoScreen(
                    onVerResumo = { navController.irParaAba(Rotas.REVISAO, Rotas.HOME_MEDICO) },
                    onIniciarChamada = { navController.navigate(Rotas.CHAMADA) }
                )
            }
            composable(Rotas.CHAMADA) {
                ChamadaEmBreveScreen(onVoltar = { navController.popBackStack() })
            }
        }
    }
}
