package com.example.pagoflex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.DiasAnticipacion
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.avisosProgramados
import com.example.pagoflex.model.formatoFechaHora
import com.example.pagoflex.notificaciones.Notificaciones
import com.example.pagoflex.notificaciones.ProgramadorRecordatorios
import com.example.pagoflex.permisos.Permisos
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.GrupoRadio
import com.example.pagoflex.ui.components.InterruptorConTexto
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import kotlinx.coroutines.launch
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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permitidas by remember { mutableStateOf(Notificaciones.puedeNotificar(context)) }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        permitidas = Notificaciones.puedeNotificar(context)
        if (!concedido) {
            scope.launch {
                val resultado = snackbarHostState.showSnackbar(
                    message = "Sin permiso no podemos avisarte de tus vencimientos. Puedes activarlo en Ajustes.",
                    actionLabel = "Ajustes",
                    duration = SnackbarDuration.Long
                )
                if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
            }
        }
    }

    fun permitirNotificaciones() {
        val permiso = Permisos.NOTIFICACIONES
        if (permiso != null && !Permisos.tiene(context, permiso)) pedirPermiso.launch(permiso)
        else if (!Notificaciones.puedeNotificar(context)) Permisos.abrirAjustes(context)
    }

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
                        onClick = {
                            onGuardar(activos, anticipacion)
                            if (activos && !permitidas) permitirNotificaciones()
                        },
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
                    titulo = "Notificaciones en este teléfono",
                    subtitulo = "Te avisamos todos los días a las 9:00 con WorkManager, aunque la app esté cerrada."
                ) {
                    TarjetaMensaje(
                        texto = if (permitidas) "Las notificaciones de PagoFlex están activadas."
                        else "Las notificaciones están desactivadas. Actívalas para recibir tus avisos.",
                        exito = permitidas,
                        icono = R.drawable.ic_campana
                    )
                    if (!permitidas) {
                        BotonPrincipal(
                            texto = "Permitir notificaciones",
                            onClick = { permitirNotificaciones() },
                            icono = R.drawable.ic_campana,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BotonSecundario(
                        texto = "Probar un aviso ahora",
                        onClick = {
                            ProgramadorRecordatorios.probarAhora(context)
                            scope.launch { snackbarHostState.showSnackbar("En unos segundos te llegará un aviso de prueba.") }
                        },
                        habilitado = activos && permitidas,
                        modifier = Modifier.fillMaxWidth()
                    )
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
