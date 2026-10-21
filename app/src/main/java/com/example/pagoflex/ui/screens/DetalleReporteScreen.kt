package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.EstadoReporte
import com.example.pagoflex.model.Reporte
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoFechaHora
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.ocultarRut
import com.example.pagoflex.model.reporteAtrasado
import com.example.pagoflex.model.usuarioDeReporte
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun DetalleReporteScreen(
    datos: BaseDatos,
    reporte: Reporte,
    admin: Usuario,
    onTomar: () -> Unit,
    onResolver: (String) -> Unit,
    onVolver: () -> Unit,
    onVerCompromiso: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val compromiso = datos.compromisoPorId(reporte.idCompromiso)
    val persona = datos.usuarioDeReporte(reporte)
    val empresa = datos.empresaPorId(compromiso?.idEmpresa)
    val responsable = datos.usuarioPorId(reporte.idAdministrador)

    var respuesta by rememberSaveable { mutableStateOf("") }
    var errorRespuesta by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_detalle_reporte),
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
            TarjetaSeccion(titulo = tituloMotivo(reporte.motivo), subtitulo = empresa?.nombreFantasia) {
                EtiquetaInteres(texto = reporte.estado.etiqueta, color = colorReporte(reporte.estado))
                if (reporteAtrasado(reporte, datos.fechaHoy)) {
                    TarjetaMensaje(texto = "Lleva más de 2 días hábiles sin resolver. Atiéndelo primero.")
                }
                if (reporte.comentario != null) {
                    Text(text = "Lo que nos escribió", style = MaterialTheme.typography.titleMedium)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "“${reporte.comentario}”",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            TarjetaSeccion(titulo = "Persona y cobro") {
                if (persona != null) {
                    FilaDato("Persona", nombreCompleto(persona))
                    FilaDato("RUT", ocultarRut(persona.rut))
                }
                if (compromiso != null) {
                    FilaDato("Cobro", conceptoConCuota(compromiso))
                    FilaDato("Monto", formatoPesos(compromiso.monto))
                    FilaDato("Vencimiento", formatoFecha(compromiso.fechaVencimiento))
                    FilaDato("Estado del cobro", compromiso.estado.etiqueta)
                }
                FilaDato("Recibido", formatoFechaHora(reporte.fechaCreacion))
                FilaDato("N° de reporte", reporte.numero)
                if (compromiso != null) FilaDato("Folio del cobro", compromiso.folio)
            }
            if (compromiso != null) {
                BotonSecundario(
                    texto = "Ver el cobro (anular o modificar)",
                    onClick = onVerCompromiso,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            when (reporte.estado) {
                EstadoReporte.ABIERTO -> BotonPrincipal(
                    texto = stringResource(R.string.btn_tomar_reporte),
                    onClick = onTomar,
                    icono = R.drawable.ic_check,
                    modifier = Modifier.fillMaxWidth()
                )
                EstadoReporte.EN_GESTION -> if (reporte.idAdministrador == admin.idUsuario) {
                    TarjetaSeccion(titulo = "Tu respuesta") {
                        CampoTexto(
                            valor = respuesta,
                            onValorCambia = {
                                respuesta = it
                                errorRespuesta = null
                            },
                            etiqueta = "Respuesta para el usuario *",
                            icono = R.drawable.ic_editar,
                            error = errorRespuesta,
                            ayuda = "${respuesta.length}/500",
                            unaLinea = false
                        )
                        TarjetaMensaje(texto = "Si el cobro no corresponde, anúlalo desde \"Ver el cobro\". Para cambiar monto o fecha, primero resuelve este reporte.")
                    }
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_resolver),
                        onClick = {
                            errorRespuesta = when {
                                respuesta.isBlank() -> "Escribe la respuesta para el usuario antes de resolver"
                                respuesta.length > 500 -> "Máximo 500 caracteres"
                                else -> null
                            }
                            if (errorRespuesta == null) onResolver(respuesta.trim())
                        },
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    TarjetaMensaje(texto = "Lo gestiona ${responsable?.let { nombreCompleto(it) } ?: "otro administrador"}.")
                }
                EstadoReporte.RESUELTO -> TarjetaSeccion(titulo = "Respuesta enviada") {
                    Text(text = reporte.respuesta ?: "", style = MaterialTheme.typography.bodyMedium)
                    reporte.fechaResolucion?.let { FilaDato("Resuelto el", formatoFechaHora(it)) }
                    responsable?.let { FilaDato("Por", nombreCorto(it)) }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleReportePreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        DetalleReporteScreen(
            datos = datos,
            reporte = datos.reportes.first(),
            admin = DatosAnexo9.usuarios.first { it.idUsuario == 9 },
            onTomar = {}, onResolver = {}, onVolver = {}
        )
    }
}
