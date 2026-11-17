package com.example.pagoflex.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.R
import com.example.pagoflex.model.ClaveRecuperada
import com.example.pagoflex.model.DatosRecuperacion
import com.example.pagoflex.model.INTENTOS_MAXIMOS_CODIGO
import com.example.pagoflex.model.LARGO_CODIGO_RECUPERACION
import com.example.pagoflex.model.MINUTOS_VIGENCIA_CODIGO
import com.example.pagoflex.model.PasoRecuperacion
import com.example.pagoflex.model.RecuperarClaveUiState
import com.example.pagoflex.notificaciones.Notificaciones
import com.example.pagoflex.permisos.Permisos
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoContrasena
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.MensajeAnimado
import com.example.pagoflex.ui.components.RequisitosClave
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.RecuperarClaveViewModel
import kotlinx.coroutines.launch

@Composable
fun RecuperarClaveScreen(
    onSolicitarCodigo: suspend (DatosRecuperacion) -> String?,
    onVerificarCodigo: suspend (DatosRecuperacion, String) -> String?,
    onClaveValida: suspend (ClaveRecuperada) -> String?,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onPedirAyuda: (String) -> Unit = {},
    viewModel: RecuperarClaveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var codigoSinNotificar by remember { mutableStateOf<String?>(null) }
    var codigoPendiente by remember { mutableStateOf<String?>(null) }

    fun entregarCodigo(codigo: String) {
        if (!Notificaciones.mostrarCodigo(context, codigo)) codigoSinNotificar = codigo
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean ->
        codigoPendiente?.let { entregarCodigo(it) }
        codigoPendiente = null
    }

    fun enviarCodigo(datos: DatosRecuperacion, esReenvio: Boolean) {
        scope.launch {
            val codigo = onSolicitarCodigo(datos)
            viewModel.onCodigoEnviado()
            if (codigo != null) {
                val permiso = Permisos.NOTIFICACIONES
                if (permiso != null && !Permisos.tiene(context, permiso)) {
                    codigoPendiente = codigo
                    pedirPermiso.launch(permiso)
                } else {
                    entregarCodigo(codigo)
                }
            }
            if (esReenvio) snackbarHostState.showSnackbar("Te enviamos un código nuevo. El anterior ya no sirve.")
        }
    }

    RecuperarClaveContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onCodigoCambia = viewModel::onCodigoCambia,
        onClaveNuevaCambia = viewModel::onClaveNuevaCambia,
        onConfirmacionCambia = viewModel::onConfirmacionCambia,
        onSolicitarCodigo = { viewModel.onSolicitarCodigo()?.let { enviarCodigo(it, esReenvio = false) } },
        onReenviarCodigo = { viewModel.onSolicitarCodigo()?.let { enviarCodigo(it, esReenvio = true) } },
        onVerificarCodigo = {
            viewModel.onVerificarCodigo()?.let { codigo ->
                val estado = viewModel.uiState.value
                scope.launch {
                    val error = onVerificarCodigo(DatosRecuperacion(estado.correo), codigo)
                    if (error == null) viewModel.onCodigoCorrecto() else viewModel.onErrorCodigo(error)
                }
            }
        },
        onCambiarDatos = viewModel::onCambiarDatos,
        onGuardar = {
            viewModel.onGuardarClave()?.let { recuperada ->
                scope.launch { onClaveValida(recuperada)?.let { error -> viewModel.onErrorRecuperacion(error) } }
            }
        },
        onVolver = onVolver,
        onPedirAyuda = { onPedirAyuda(viewModel.uiState.value.correo) },
        snackbarHostState = snackbarHostState
    )

    codigoSinNotificar?.let { codigo ->
        CodigoSinNotificacion(codigo = codigo, onCerrar = { codigoSinNotificar = null })
    }
}

@Composable
private fun CodigoSinNotificacion(codigo: String, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Correo de PagoFlex") },
        text = {
            Text(
                "Las notificaciones están desactivadas, así que te mostramos el mensaje aquí. " +
                    "Tu código de verificación es $codigo. Vence en $MINUTOS_VIGENCIA_CODIGO minutos. " +
                    "No lo compartas con nadie: PagoFlex nunca te lo pedirá por teléfono."
            )
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Entendido") } }
    )
}

@Composable
fun RecuperarClaveContent(
    uiState: RecuperarClaveUiState,
    onCorreoCambia: (String) -> Unit,
    onCodigoCambia: (String) -> Unit,
    onClaveNuevaCambia: (String) -> Unit,
    onConfirmacionCambia: (String) -> Unit,
    onSolicitarCodigo: () -> Unit,
    onReenviarCodigo: () -> Unit,
    onVerificarCodigo: () -> Unit,
    onCambiarDatos: () -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit = {},
    onPedirAyuda: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_recuperar_clave),
                icono = R.drawable.ic_volver,
                descripcionIcono = stringResource(R.string.cd_volver),
                onIconoClick = onVolver
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            TarjetaMensaje(
                texto = "Para proteger tu cuenta te enviamos un código de $LARGO_CODIGO_RECUPERACION números. " +
                    "Solo quien recibe ese código puede crear una contraseña nueva.",
                icono = R.drawable.ic_escudo
            )

            IndicadorPasos(pasoActual = uiState.paso)

            AnimatedContent(
                targetState = uiState.paso,
                transitionSpec = {
                    val avanza = targetState.numero > initialState.numero
                    (slideInHorizontally { ancho -> if (avanza) ancho else -ancho } + fadeIn()) togetherWith
                        (slideOutHorizontally { ancho -> if (avanza) -ancho else ancho } + fadeOut())
                },
                label = "pasoRecuperacion"
            ) { paso ->
                when (paso) {
                    PasoRecuperacion.DATOS -> PasoDatos(uiState, onCorreoCambia, onSolicitarCodigo)
                    PasoRecuperacion.CODIGO -> PasoCodigo(uiState, onCodigoCambia, onVerificarCodigo, onReenviarCodigo, onCambiarDatos)
                    PasoRecuperacion.CLAVE -> PasoClave(uiState, onClaveNuevaCambia, onConfirmacionCambia, onGuardar, onCambiarDatos)
                }
            }

            MensajeAnimado(texto = uiState.errorRecuperacion)

            BotonSecundario(
                texto = "Volver a iniciar sesión",
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(onClick = onPedirAyuda, modifier = Modifier.fillMaxWidth()) {
                Text("¿Sigues sin poder entrar? Escríbenos a soporte")
            }
        }
    }
}

@Composable
private fun IndicadorPasos(pasoActual: PasoRecuperacion) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PasoRecuperacion.entries.forEach { paso ->
            val alcanzado = paso.numero <= pasoActual.numero
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = if (alcanzado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (alcanzado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = paso.numero.toString(), fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = paso.titulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (paso == pasoActual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.espacioMini)
                )
            }
        }
    }
}

@Composable
private fun PasoDatos(
    uiState: RecuperarClaveUiState,
    onCorreoCambia: (String) -> Unit,
    onSolicitarCodigo: () -> Unit
) {
    TarjetaSeccion(titulo = "1. ${PasoRecuperacion.DATOS.titulo}", subtitulo = "Usa el correo con el que te registraste") {
        CampoTexto(
            valor = uiState.correo,
            onValorCambia = onCorreoCambia,
            etiqueta = stringResource(R.string.campo_correo),
            icono = R.drawable.ic_correo,
            error = uiState.errorCorreo,
            ayuda = stringResource(R.string.ayuda_correo),
            tipoTeclado = KeyboardType.Email
        )
        BotonPrincipal(
            texto = "Enviar código",
            onClick = onSolicitarCodigo,
            icono = R.drawable.ic_correo,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PasoCodigo(
    uiState: RecuperarClaveUiState,
    onCodigoCambia: (String) -> Unit,
    onVerificarCodigo: () -> Unit,
    onReenviarCodigo: () -> Unit,
    onCambiarDatos: () -> Unit
) {
    TarjetaSeccion(
        titulo = "2. ${PasoRecuperacion.CODIGO.titulo}",
        subtitulo = "Si los datos corresponden a una cuenta activa, te enviamos el código en una notificación, " +
            "como el correo que llegaría a ${uiState.correo}. Vence en $MINUTOS_VIGENCIA_CODIGO minutos " +
            "y tienes $INTENTOS_MAXIMOS_CODIGO intentos."
    ) {
        CampoTexto(
            valor = uiState.codigo,
            onValorCambia = onCodigoCambia,
            etiqueta = "Código de verificación",
            icono = R.drawable.ic_numero,
            error = uiState.errorCodigo,
            ayuda = "${uiState.codigo.length}/$LARGO_CODIGO_RECUPERACION números",
            tipoTeclado = KeyboardType.NumberPassword
        )
        BotonPrincipal(
            texto = "Verificar código",
            onClick = onVerificarCodigo,
            icono = R.drawable.ic_check,
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onCambiarDatos) { Text("Cambiar correo") }
            TextButton(onClick = onReenviarCodigo) { Text("Reenviar código") }
        }
    }
}

@Composable
private fun PasoClave(
    uiState: RecuperarClaveUiState,
    onClaveNuevaCambia: (String) -> Unit,
    onConfirmacionCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onCambiarDatos: () -> Unit
) {
    TarjetaSeccion(titulo = "3. ${PasoRecuperacion.CLAVE.titulo}", subtitulo = "Debe cumplir los 5 requisitos de seguridad") {
        TarjetaMensaje(texto = "Código verificado. Ya puedes crear tu contraseña.", exito = true)
        CampoContrasena(
            valor = uiState.claveNueva,
            onValorCambia = onClaveNuevaCambia,
            etiqueta = "Nueva contraseña",
            error = uiState.errorClaveNueva
        )
        RequisitosClave(clave = uiState.claveNueva)
        CampoContrasena(
            valor = uiState.confirmacionClave,
            onValorCambia = onConfirmacionCambia,
            etiqueta = "Repite la nueva contraseña",
            error = uiState.errorConfirmacion
        )
        BotonPrincipal(
            texto = "Guardar nueva contraseña",
            onClick = onGuardar,
            icono = R.drawable.ic_candado,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = onCambiarDatos, modifier = Modifier.align(Alignment.End)) { Text("Volver a empezar") }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarClavePreview() {
    PagoFlexTheme {
        RecuperarClaveContent(
            uiState = RecuperarClaveUiState(correo = "crojas@correo.ejemplo"),
            onCorreoCambia = {}, onCodigoCambia = {}, onClaveNuevaCambia = {}, onConfirmacionCambia = {},
            onSolicitarCodigo = {}, onReenviarCodigo = {}, onVerificarCodigo = {}, onCambiarDatos = {}, onGuardar = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Paso 2: código")
@Composable
private fun RecuperarClaveCodigoPreview() {
    PagoFlexTheme(modoOscuro = false) {
        RecuperarClaveContent(
            uiState = RecuperarClaveUiState(
                paso = PasoRecuperacion.CODIGO, correo = "crojas@correo.ejemplo",
                codigo = "4829", errorCodigo = "El código no es correcto. Te quedan 2 intentos."
            ),
            onCorreoCambia = {}, onCodigoCambia = {}, onClaveNuevaCambia = {}, onConfirmacionCambia = {},
            onSolicitarCodigo = {}, onReenviarCodigo = {}, onVerificarCodigo = {}, onCambiarDatos = {}, onGuardar = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Paso 3: nueva contraseña")
@Composable
private fun RecuperarClaveNuevaPreview() {
    PagoFlexTheme(modoOscuro = false) {
        RecuperarClaveContent(
            uiState = RecuperarClaveUiState(
                paso = PasoRecuperacion.CLAVE, correo = "crojas@correo.ejemplo",
                claveNueva = "pago2026", errorClaveNueva = "Te faltan 2 de los 5 requisitos de seguridad"
            ),
            onCorreoCambia = {}, onCodigoCambia = {}, onClaveNuevaCambia = {}, onConfirmacionCambia = {},
            onSolicitarCodigo = {}, onReenviarCodigo = {}, onVerificarCodigo = {}, onCambiarDatos = {}, onGuardar = {}
        )
    }
}
