package com.example.pagoflex.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.IP_EMULADOR
import com.example.pagoflex.model.SERVIDOR_EMULADOR
import com.example.pagoflex.model.errorDireccionServidor
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun ConexionScreen(
    cargando: Boolean,
    servidor: String,
    onReintentar: () -> Unit,
    onCambiarServidor: (String) -> Unit
) {
    var cambiarServidor by remember { mutableStateOf(false) }

    if (cambiarServidor) {
        DialogoServidor(
            actual = servidor,
            onConectar = { direccion ->
                cambiarServidor = false
                onCambiarServidor(direccion)
            },
            onCancelar = { cambiarServidor = false }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.espacioGrande),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_pagoflex),
                contentDescription = stringResource(R.string.cd_logo),
                modifier = Modifier.size(72.dp)
            )
            if (cargando) {
                CircularProgressIndicator()
                Text(
                    text = "Conectando con los servicios de PagoFlex en $servidor…",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "No pudimos conectarnos",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Revisa que Eureka, los microservicios y el API Gateway estén encendidos en $servidor.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                BotonPrincipal(
                    texto = "Reintentar",
                    onClick = onReintentar,
                    modifier = Modifier.fillMaxWidth()
                )
                BotonSecundario(
                    texto = "Cambiar el servidor",
                    onClick = { cambiarServidor = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DialogoServidor(
    actual: String,
    onConectar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var direccion by remember { mutableStateOf(actual) }
    val error = errorDireccionServidor(direccion)

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Servidor de PagoFlex") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                Text(
                    text = "Escribe la IP del PC donde corre el API Gateway o la URL pública de VS Code. " +
                        "Si los microservicios están en este mismo PC, usa $IP_EMULADOR.",
                    style = MaterialTheme.typography.bodyMedium
                )
                CampoTexto(
                    valor = direccion,
                    onValorCambia = { direccion = it },
                    etiqueta = "Servidor",
                    icono = R.drawable.ic_ubicacion,
                    error = error,
                    ayuda = "Ej: 192.168.1.20 o https://…devtunnels.ms",
                    tipoTeclado = KeyboardType.Uri
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConectar(direccion) }, enabled = error == null) { Text("Conectar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.dialogo_cancelar)) }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ConexionCargandoPreview() {
    PagoFlexTheme {
        ConexionScreen(cargando = true, servidor = SERVIDOR_EMULADOR, onReintentar = {}, onCambiarServidor = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ConexionFallidaPreview() {
    PagoFlexTheme {
        ConexionScreen(cargando = false, servidor = "http://192.168.1.20:8080", onReintentar = {}, onCambiarServidor = {})
    }
}
