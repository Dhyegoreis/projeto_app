package com.goianinho.medicina.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CoresGoianinho = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = Branco,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeEscuro,

    secondary = AzulNoite,
    onSecondary = Branco,
    secondaryContainer = VerdeResumo,
    onSecondaryContainer = AzulNoite,

    tertiaryContainer = AlertaFundo,
    onTertiaryContainer = AlertaTexto,

    background = FundoApp,
    onBackground = AzulNoite,
    surface = Branco,
    onSurface = AzulNoite,
    onSurfaceVariant = CinzaTexto,
    outline = CinzaBorda
)

// Sem modo escuro e sem cor dinâmica: o app segue sempre as cores do protótipo.
@Composable
fun GoianinhoMedicinaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CoresGoianinho,
        typography = Typography,
        content = content
    )
}
