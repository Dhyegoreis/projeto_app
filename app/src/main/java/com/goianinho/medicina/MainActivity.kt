package com.goianinho.medicina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.goianinho.medicina.navigation.AppNavigation
import com.goianinho.medicina.ui.theme.GoianinhoMedicinaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoianinhoMedicinaTheme {
                // RF-07 (manter logado) será ligado pelo Módulo 1 escolhendo a rotaInicial.
                AppNavigation()
            }
        }
    }
}
