package com.example.pagoflex.ui.screens

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.R
import com.example.pagoflex.data.ConvertidorImagen
import com.example.pagoflex.escaner.EscanerBoleta
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.EmpresaCliente
import com.example.pagoflex.model.FormularioUiState
import com.example.pagoflex.model.NuevoCompromiso
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.leerBoleta
import com.example.pagoflex.permisos.Permisos
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.MensajeAnimado
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.FormularioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun AgregarPagoScreen(
    datos: BaseDatos,
    usuario: Usuario,
    onAgregar: (NuevoCompromiso) -> Unit,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: FormularioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imagenBoleta by viewModel.imagenBoleta.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val empresas = datos.empresas.filter { it.activa }
    var uriFoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    val textoCamaraDenegada = stringResource(R.string.permiso_camara_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

    LaunchedEffect(usuario.idUsuario) {
        viewModel.onUsarRut(usuario.rut, datos)
    }

    fun procesarBoleta(uri: Uri) {
        viewModel.onLeyendoBoleta()
        scope.launch {
            val imagen = withContext(Dispatchers.Default) {
                ConvertidorImagen.desdeUri(context, uri, ConvertidorImagen.LADO_BOLETA)
                    ?.let { ConvertidorImagen.aBytes(it, ConvertidorImagen.LADO_BOLETA) }
            }
            EscanerBoleta.leerTexto(context, uri) { texto ->
                val lectura = texto?.let { leerBoleta(it, empresas, datos.fechaHoy) }
                viewModel.onBoletaLeida(lectura, imagen, datos)
            }
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { guardada: Boolean ->
        val uri = uriFoto
        if (guardada && uri != null) procesarBoleta(uri)
    }

    fun abrirCamara() {
        val uri = EscanerBoleta.nuevaUriParaFoto(context)
        uriFoto = uri
        try {
            lanzadorCamara.launch(uri)
        } catch (e: ActivityNotFoundException) {
            scope.launch { snackbarHostState.showSnackbar("No encontramos una app de cámara en este teléfono.") }
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) {
            abrirCamara()
        } else {
            scope.launch {
                val resultado = snackbarHostState.showSnackbar(
                    message = textoCamaraDenegada,
                    actionLabel = textoAjustes,
                    duration = SnackbarDuration.Long
                )
                if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
            }
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) procesarBoleta(uri)
    }

    AgregarPagoContent(
        uiState = uiState,
        empresas = empresas,
        hoy = datos.fechaHoy,
        imagenBoleta = fotoDe(imagenBoleta),
        onTomarFotoBoleta = {
            if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
            else pedirPermisoCamara.launch(Permisos.CAMARA)
        },
        onElegirFotoBoleta = {
            lanzadorGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onEmpresaSeleccionada = viewModel::onEmpresaSeleccionada,
        onConceptoCambia = viewModel::onConceptoCambia,
        onMontoCambia = viewModel::onMontoCambia,
        onFechaVencimientoCambia = viewModel::onFechaVencimientoCambia,
        onNumeroCuotaCambia = viewModel::onNumeroCuotaCambia,
        onTotalCuotasCambia = viewModel::onTotalCuotasCambia,
        onAgregar = { viewModel.onRegistrar(datos)?.let { nuevo -> onAgregar(nuevo) } },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun AgregarPagoContent(
    uiState: FormularioUiState,
    empresas: List<EmpresaCliente>,
    hoy: LocalDate,
    imagenBoleta: ImageBitmap? = null,
    onTomarFotoBoleta: () -> Unit = {},
    onElegirFotoBoleta: () -> Unit = {},
    onEmpresaSeleccionada: (EmpresaCliente) -> Unit,
    onConceptoCambia: (String) -> Unit,
    onMontoCambia: (String) -> Unit,
    onFechaVencimientoCambia: (String) -> Unit,
    onNumeroCuotaCambia: (String) -> Unit,
    onTotalCuotasCambia: (String) -> Unit,
    onAgregar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val montoEscrito = uiState.monto.toIntOrNull()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_agregar_pago),
                subtitulo = uiState.empresa?.nombreFantasia,
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
                texto = "Agrega una boleta o cuota que tengas que pagar. Quedará Pendiente en tu inicio y podrás pagarla desde PagoFlex.",
                icono = R.drawable.ic_info
            )

            TarjetaSeccion(
                titulo = "Escanear boleta",
                subtitulo = "Saca una foto a la boleta y llenamos el monto, el vencimiento y la organización. También puedes escribirlos abajo."
            ) {
                if (imagenBoleta != null) {
                    Image(
                        bitmap = imagenBoleta,
                        contentDescription = "Foto de la boleta",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(Dimens.radioCampo))
                    )
                }
                if (uiState.leyendoBoleta) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(Dimens.tamanoIcono), strokeWidth = 2.dp)
                        Text(text = "Leyendo la boleta…", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                MensajeAnimado(texto = uiState.mensajeBoleta, exito = uiState.boletaConDatos)
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    BotonSecundario(
                        texto = "Tomar foto",
                        onClick = onTomarFotoBoleta,
                        habilitado = !uiState.leyendoBoleta,
                        modifier = Modifier.weight(1f)
                    )
                    BotonSecundario(
                        texto = "Elegir foto",
                        onClick = onElegirFotoBoleta,
                        habilitado = !uiState.leyendoBoleta,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            TarjetaSeccion(titulo = "¿Quién te cobra?") {
                ListaDesplegable(
                    etiqueta = "Organización *",
                    opciones = empresas,
                    seleccionada = uiState.empresa,
                    onSeleccionar = onEmpresaSeleccionada,
                    textoDe = { it.nombreFantasia },
                    icono = R.drawable.ic_empresa,
                    error = uiState.errorEmpresa
                )
            }

            TarjetaSeccion(titulo = "Datos del pago") {
                CampoTexto(
                    valor = uiState.concepto,
                    onValorCambia = onConceptoCambia,
                    etiqueta = "Concepto *",
                    icono = R.drawable.ic_editar,
                    error = uiState.errorConcepto,
                    ayuda = "Ej: Boleta de agua de noviembre"
                )
                CampoTexto(
                    valor = uiState.monto,
                    onValorCambia = onMontoCambia,
                    etiqueta = "Monto en pesos *",
                    icono = R.drawable.ic_dinero,
                    error = uiState.errorMonto,
                    ayuda = montoEscrito?.let { formatoPesos(it) } ?: "Solo números, sin puntos",
                    tipoTeclado = KeyboardType.Number
                )
                CampoTexto(
                    valor = uiState.fechaVencimiento,
                    onValorCambia = onFechaVencimientoCambia,
                    etiqueta = "Vencimiento (DD-MM-AAAA) *",
                    icono = R.drawable.ic_calendario,
                    error = uiState.errorFechaVencimiento,
                    ayuda = "No puede ser anterior a hoy (${formatoFecha(hoy)})"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    CampoTexto(
                        valor = uiState.numeroCuota,
                        onValorCambia = onNumeroCuotaCambia,
                        etiqueta = "Cuota N°",
                        icono = R.drawable.ic_numero,
                        ayuda = "Opcional",
                        tipoTeclado = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    CampoTexto(
                        valor = uiState.totalCuotas,
                        onValorCambia = onTotalCuotasCambia,
                        etiqueta = "Total cuotas",
                        icono = R.drawable.ic_numero,
                        ayuda = "Opcional",
                        tipoTeclado = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                MensajeAnimado(texto = uiState.errorCuota)
            }

            MensajeAnimado(texto = uiState.errorDuplicado)

            BotonPrincipal(
                texto = "Agregar a mis pagos",
                onClick = onAgregar,
                icono = R.drawable.ic_agregar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgregarPagoPreview() {
    PagoFlexTheme {
        AgregarPagoContent(
            uiState = FormularioUiState(rutPersona = DatosAnexo9.usuarios.first().rut),
            empresas = BaseDatos().empresas,
            hoy = LocalDate.of(2026, 10, 6),
            onEmpresaSeleccionada = {}, onConceptoCambia = {}, onMontoCambia = {}, onFechaVencimientoCambia = {},
            onNumeroCuotaCambia = {}, onTotalCuotasCambia = {}, onAgregar = {}
        )
    }
}
