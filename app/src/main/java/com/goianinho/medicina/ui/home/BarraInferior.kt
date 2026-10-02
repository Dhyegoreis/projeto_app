package com.goianinho.medicina.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

// Barra inferior de 4 abas (RF-36). Dono: Módulo 1.
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
