package com.example.pagoflex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun MensajeAnimado(
    texto: String?,
    modifier: Modifier = Modifier,
    exito: Boolean = false,
    error: Boolean = !exito
) {
    var ultimoTexto by remember { mutableStateOf(texto ?: "") }
    if (texto != null) ultimoTexto = texto

    AnimatedVisibility(
        visible = texto != null,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        label = "mensaje"
    ) {
        TarjetaMensaje(texto = ultimoTexto, exito = exito, error = error)
    }
}
