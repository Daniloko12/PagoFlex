package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.textoTarjeta
import com.example.pagoflex.ui.components.TarjetaDePago
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun TarjetasScreen(
    datos: BaseDatos,
    idUsuario: Int,
    onAgregar: () -> Unit,
    onQuitar: (Int) -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val tarjetas = datos.tarjetasGuardadas(idUsuario)
    var porQuitar by rememberSaveable { mutableStateOf<Int?>(null) }

    val tarjetaPorQuitar: Tarjeta? = tarjetas.find { it.idTarjeta == porQuitar }
    if (tarjetaPorQuitar != null) {
        AlertDialog(
            onDismissRequest = { porQuitar = null },
            icon = { Icon(painterResource(R.drawable.ic_tarjeta), contentDescription = null) },
            title = { Text(stringResource(R.string.dialogo_quitar_tarjeta_titulo)) },
            text = { Text("${textoTarjeta(tarjetaPorQuitar)} dejará de aparecer al pagar. Tus comprobantes anteriores no cambian.") },
            confirmButton = {
                TextButton(onClick = {
                    porQuitar = null
                    onQuitar(tarjetaPorQuitar.idTarjeta)
                }) { Text(stringResource(R.string.btn_quitar)) }
            },
            dismissButton = {
                TextButton(onClick = { porQuitar = null }) { Text(stringResource(R.string.dialogo_cancelar)) }
            }
        )
    }

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_tarjetas),
        menu = menu,
        snackbarHostState = snackbarHostState,
        botonFlotante = {
            ExtendedFloatingActionButton(
                onClick = onAgregar,
                icon = { Icon(painterResource(R.drawable.ic_agregar), contentDescription = null) },
                text = { Text(stringResource(R.string.btn_agregar_tarjeta)) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Dimens.espacioPantalla, end = Dimens.espacioPantalla,
                top = Dimens.espacioPantalla, bottom = ESPACIO_BOTON_FLOTANTE
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item {
                TarjetaMensaje(
                    texto = "Solo guardamos la marca y los 4 últimos números. El número completo y el código de seguridad nunca se guardan.",
                    icono = R.drawable.ic_escudo
                )
            }
            if (tarjetas.isEmpty()) {
                item { MensajeVacio("Aún no tienes tarjetas guardadas. Agrégalas aquí o al pagar.") }
            }
            items(tarjetas, key = { it.idTarjeta }) { tarjeta ->
                Column(horizontalAlignment = Alignment.End) {
                    TarjetaDePago(tarjeta = tarjeta)
                    TextButton(onClick = { porQuitar = tarjeta.idTarjeta }) {
                        Text(stringResource(R.string.btn_quitar))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TarjetasPreview() {
    PagoFlexTheme {
        TarjetasScreen(datos = BaseDatos(), idUsuario = 1, onAgregar = {}, onQuitar = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Modo claro")
@Composable
private fun TarjetasClaroPreview() {
    PagoFlexTheme(modoOscuro = false) {
        TarjetasScreen(datos = BaseDatos(), idUsuario = 1, onAgregar = {}, onQuitar = {})
    }
}
