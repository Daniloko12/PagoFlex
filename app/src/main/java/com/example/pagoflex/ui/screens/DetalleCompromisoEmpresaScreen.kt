package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.MotivoAnulacion
import com.example.pagoflex.model.anulacionDe
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.diasAtraso
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoFechaHora
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.motivoParaNoEditar
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.ocultarRut
import com.example.pagoflex.model.pagoDe
import com.example.pagoflex.model.reporteActivoDe
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.GrupoRadio
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun DetalleCompromisoEmpresaScreen(
    datos: BaseDatos,
    compromiso: Compromiso,
    onAnular: (MotivoAnulacion, String?) -> Unit,
    onVolver: () -> Unit,
    onEditar: () -> Unit = {},
    onVerCliente: (Int) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val persona = datos.usuarioPorId(compromiso.idUsuarioFinal)
    val empresa = datos.empresaPorId(compromiso.idEmpresa)
    val registradoPor = datos.usuarioPorId(compromiso.idAdminRegistro)
    val sePuedeEditar = datos.motivoParaNoEditar(compromiso.idCompromiso) == null
    val pago = datos.pagoDe(compromiso.idCompromiso)
    val anulacion = datos.anulacionDe(compromiso.idCompromiso)
    val reporte = datos.reporteActivoDe(compromiso.idCompromiso)
    val sePuedeAnular = compromiso.estado == EstadoCompromiso.PENDIENTE ||
        compromiso.estado == EstadoCompromiso.VENCIDO || compromiso.estado == EstadoCompromiso.EN_REVISION

    var motivo by rememberSaveable { mutableStateOf<MotivoAnulacion?>(null) }
    var comentario by rememberSaveable { mutableStateOf("") }
    var errorMotivo by rememberSaveable { mutableStateOf<String?>(null) }
    var errorComentario by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon = { Icon(painterResource(R.drawable.ic_info), contentDescription = null) },
            title = { Text(stringResource(R.string.dialogo_anular_titulo)) },
            text = { Text(stringResource(R.string.dialogo_anular_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    motivo?.let { onAnular(it, comentario.trim().ifBlank { null }) }
                }) { Text(stringResource(R.string.btn_anular)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text(stringResource(R.string.dialogo_cancelar)) }
            }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_detalle),
                subtitulo = persona?.let { nombreCorto(it) },
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
            TarjetaSeccion(titulo = conceptoConCuota(compromiso)) {
                EtiquetaInteres(texto = compromiso.estado.etiqueta, color = colorEstado(compromiso.estado))
                FilaDato("Empresa", empresa?.nombreFantasia ?: "")
                FilaDato("Vencimiento", formatoFecha(compromiso.fechaVencimiento))
                FilaDato("Monto", formatoPesos(compromiso.monto))
                if (compromiso.estado == EstadoCompromiso.VENCIDO) {
                    FilaDato("Días de atraso", textoDias(diasAtraso(compromiso, datos.fechaHoy)))
                    FilaDato("Total con recargo", formatoPesos(totalAPagar(compromiso)), destacado = true)
                }
                FilaDato("Registrado por", registradoPor?.let { nombreCorto(it) } ?: "Equipo PagoFlex (planilla)")
                FilaDato("Registrado el", formatoFecha(compromiso.fechaRegistro.toLocalDate()))
                FilaDato("Folio", compromiso.folio)
            }

            if (sePuedeEditar) {
                BotonPrincipal(
                    texto = "Modificar compromiso",
                    onClick = onEditar,
                    icono = R.drawable.ic_editar,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (persona != null) {
                TarjetaSeccion(titulo = "Persona") {
                    FilaDato("Nombre", nombreCompleto(persona))
                    FilaDato("RUT", ocultarRut(persona.rut))
                    FilaDato("Correo", persona.correo)
                    persona.telefono?.let { FilaDato("Teléfono", it) }
                    BotonSecundario(
                        texto = "Ver cliente",
                        onClick = { onVerCliente(persona.idUsuario) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (pago != null) {
                TarjetaSeccion(titulo = "Pago") {
                    FilaDato("Pagado el", formatoFechaHora(pago.fechaHora))
                    FilaDato("Monto pagado", formatoPesos(pago.montoPagado))
                    FilaDato("Pagado", textoCanal(pago.canal))
                    FilaDato("Medio de pago", textoMedioPago(datos, pago))
                    FilaDato("N° de comprobante", pago.numeroComprobante)
                }
                TarjetaMensaje(texto = "Este compromiso está pagado: un compromiso pagado nunca se anula.")
            }
            if (reporte != null) {
                TarjetaSeccion(titulo = "La persona reportó un problema") {
                    FilaDato("Motivo", reporte.motivo.etiqueta)
                    reporte.comentario?.let { FilaDato("Comentario", it) }
                    FilaDato("Estado del reporte", reporte.estado.etiqueta)
                }
            }
            if (anulacion != null) {
                TarjetaSeccion(titulo = "Anulación") {
                    FilaDato("Anulado el", formatoFechaHora(anulacion.fecha))
                    FilaDato("Motivo", anulacion.motivo.etiqueta)
                    anulacion.comentario?.let { FilaDato("Comentario", it) }
                    FilaDato("Anulado por", datos.usuarioPorId(anulacion.idAdministrador)?.let { nombreCompleto(it) } ?: "")
                }
            }

            if (sePuedeAnular) {
                TarjetaSeccion(
                    titulo = "Anular este compromiso",
                    subtitulo = "Si no corresponde cobrarlo. Para corregir un dato, usa \"Modificar\"."
                ) {
                    GrupoRadio(
                        opciones = MotivoAnulacion.entries,
                        seleccionada = motivo,
                        onSeleccionar = {
                            motivo = it
                            errorMotivo = null
                            errorComentario = null
                        },
                        textoDe = { it.etiqueta },
                        error = errorMotivo
                    )
                    CampoTexto(
                        valor = comentario,
                        onValorCambia = {
                            comentario = it
                            errorComentario = null
                        },
                        etiqueta = if (motivo == MotivoAnulacion.OTRO) "Comentario (obligatorio)" else "Comentario (opcional)",
                        icono = R.drawable.ic_editar,
                        error = errorComentario,
                        unaLinea = false
                    )
                    HorizontalDivider()
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_anular),
                        onClick = {
                            errorMotivo = if (motivo == null) "Elige el motivo de la anulación" else null
                            errorComentario = when {
                                motivo == MotivoAnulacion.OTRO && comentario.isBlank() ->
                                    "El comentario es obligatorio cuando el motivo es \"Otro\""
                                comentario.length > 255 -> "Máximo 255 caracteres"
                                else -> null
                            }
                            if (errorMotivo == null && errorComentario == null) mostrarDialogo = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleCompromisoEmpresaPreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        DetalleCompromisoEmpresaScreen(
            datos = datos,
            compromiso = datos.compromisoPorId(105) ?: datos.compromisos.first(),
            onAnular = { _, _ -> },
            onVolver = {}
        )
    }
}
