package com.goianinho.medicina.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goianinho.medicina.R
import com.goianinho.medicina.data.model.Mensagem
import com.goianinho.medicina.ui.theme.AzulNoite
import com.goianinho.medicina.ui.theme.Branco
import com.goianinho.medicina.ui.theme.CinzaBorda
import com.goianinho.medicina.ui.theme.VerdePrincipal
import com.goianinho.medicina.ui.theme.VerdeResumo

// Uma bolha do chat. O card de fonte aparece só quando a mensagem cita um documento.
@Composable
fun BolhaMensagem(msg: Mensagem, onFonteClick: ((String) -> Unit)?) {
    val doUsuario = msg.autor == "usuario"
    val clicavel = onFonteClick != null
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (doUsuario) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = if (doUsuario) VerdePrincipal else Branco,
            contentColor = if (doUsuario) Branco else AzulNoite,
            shape = RoundedCornerShape(18.dp),
            border = if (doUsuario) null else BorderStroke(1.dp, CinzaBorda),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(msg.texto, style = MaterialTheme.typography.bodyMedium)
                if (msg.fonte.isNotEmpty()) {
                    Surface(
                        color = VerdeResumo,
                        contentColor = AzulNoite,
                        shape = RoundedCornerShape(10.dp),
                        onClick = { onFonteClick?.invoke(msg.fonte) },
                        enabled = clicavel
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_documento),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (clicavel) "${msg.fonte} · ver trecho" else "Fonte: ${msg.fonte}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (clicavel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
