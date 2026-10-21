package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.DiasAnticipacion
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.avisosProgramados
import com.example.pagoflex.model.formatoFechaHora
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.GrupoRadio
import com.example.pagoflex.ui.components.InterruptorConTexto
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import java.time.LocalTime

@Composable
fun AvisosScreen(
    datos: BaseDatos,
    usuario: Usuario,
    sinConexion: Boolean,
    onGuardar: (Boolean, DiasAnticipacion) -> Unit,
    onCambiarConexion: (Boolean) -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var activos by rememberSaveable { mutableStateOf(usuario.avisosActivos ?: true) }
    var anticipacion by rememberSaveable { mutableStateOf(anticipacionDe(usuario)) }

    val avisos = datos.avisosProgramados(usuario, activos, anticipacion, LocalTime.now())

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_avisos),
        menu = menu,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item {
                TarjetaSeccion(
                    titulo = "Avisos de vencimiento",
                    subtitulo = "Tú eliges cuándo enterarte. Nuestros mensajes siempre son positivos."
                ) {
                    InterruptorConTexto(
                        texto = "Recibir avisos",
                        activado = activos,
                        onCambio = { activos = it },
                        descripcion = "Máximo un aviso por compromiso al día y nunca entre las 22:00 y las 08:00"
                    )
                    Text(text = "¿Con cuánta anticipación?", style = MaterialTheme.typography.titleMedium)
                    GrupoRadio(
                        opciones = DiasAnticipacion.entries,
                        seleccionada = anticipacion,
                        onSeleccionar = { anticipacion = it },
                        textoDe = { it.etiqueta },
                        habilitado = activos
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_guardar_preferencias),
                        onClick = { onGuardar(activos, anticipacion) },
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                TarjetaSeccion(titulo = "Tus próximos avisos", subtitulo = "Así te llegarán") {
                    when {
                        !activos ->
                            Text(text = "Desactivaste los avisos. Puedes volver a activarlos cuando quieras.", style = MaterialTheme.typography.bodyMedium)
                        avisos.isEmpty() ->
                            Text(text = "No tienes vencimientos por avisar.", style = MaterialTheme.typography.bodyMedium)
                    }
                    avisos.forEachIndexed { indice, aviso ->
                        if (indice > 0) HorizontalDivider()
                        Text(
                            text = formatoFechaHora(aviso.fechaEntrega),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = aviso.mensaje, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                TarjetaSeccion(
                    titulo = "Modo sin conexión",
                    subtitulo = "Versión de demostración: muestra cómo se ve la app sin señal, por ejemplo en el Metro."
                ) {
                    InterruptorConTexto(
                        texto = "Simular sin conexión",
                        activado = sinConexion,
                        onCambio = onCambiarConexion,
                        descripcion = "Verás tu última situación conocida y los pagos no se podrán confirmar"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AvisosPreview() {
    PagoFlexTheme {
        AvisosScreen(
            datos = BaseDatos(),
            usuario = DatosAnexo9.usuarios.first(),
            sinConexion = false,
            onGuardar = { _, _ -> },
            onCambiarConexion = {}
        )
    }
}
