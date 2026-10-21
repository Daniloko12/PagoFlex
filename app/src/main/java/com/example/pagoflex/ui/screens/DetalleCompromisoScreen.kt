package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.anulacionDe
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.diasAtraso
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoFechaHora
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.pagoDe
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.model.ultimoReporteDe
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun DetalleCompromisoScreen(
    datos: BaseDatos,
    compromiso: Compromiso,
    motivoNoPagar: String?,
    puedeReportar: Boolean,
    onVolver: () -> Unit,
    onPagar: () -> Unit,
    onReportar: () -> Unit,
    onVerComprobante: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val empresa = datos.empresaPorId(compromiso.idEmpresa)
    val pago = datos.pagoDe(compromiso.idCompromiso)
    val anulacion = datos.anulacionDe(compromiso.idCompromiso)
    val reporte = datos.ultimoReporteDe(compromiso.idCompromiso)
    val sePuedePagar = compromiso.estado == EstadoCompromiso.PENDIENTE || compromiso.estado == EstadoCompromiso.VENCIDO

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_detalle),
                subtitulo = empresa?.nombreFantasia,
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
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            TarjetaSeccion(
                titulo = empresa?.nombreFantasia ?: "",
                subtitulo = compromiso.concepto
            ) {
                EtiquetaInteres(texto = compromiso.estado.etiqueta, color = colorEstado(compromiso.estado))
                if (compromiso.numeroCuota != null && compromiso.totalCuotas != null) {
                    FilaDato("Cuota", "${compromiso.numeroCuota} de ${compromiso.totalCuotas}")
                }
                FilaDato("Vencimiento", formatoFecha(compromiso.fechaVencimiento))
                FilaDato("Monto", formatoPesos(compromiso.monto))
                if (compromiso.estado == EstadoCompromiso.VENCIDO) {
                    FilaDato("Días de atraso", textoDias(diasAtraso(compromiso, datos.fechaHoy)))
                    FilaDato("Recargo por atraso", formatoPesos(compromiso.recargoAplicado))
                }
                if (sePuedePagar) {
                    HorizontalDivider()
                    FilaDato("Total a pagar", formatoPesos(totalAPagar(compromiso)), destacado = true)
                }
                FilaDato("N° de folio", compromiso.folio)
            }

            if (sePuedePagar && motivoNoPagar != null) TarjetaMensaje(texto = motivoNoPagar)

            if (pago != null) {
                TarjetaSeccion(titulo = "Pago registrado") {
                    FilaDato("Fecha y hora", formatoFechaHora(pago.fechaHora))
                    FilaDato("Pagado", textoCanal(pago.canal).replaceFirstChar { it.uppercase() })
                    FilaDato("¿A tiempo?", textoATiempo(pago, compromiso))
                    FilaDato("N° de comprobante", pago.numeroComprobante)
                    BotonSecundario(
                        texto = stringResource(R.string.btn_ver_comprobante),
                        onClick = { onVerComprobante(pago.numeroComprobante) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            if (anulacion != null) {
                TarjetaMensaje(
                    texto = "La empresa dejó sin efecto este compromiso el ${formatoFecha(anulacion.fecha.toLocalDate())} " +
                        "(${anulacion.motivo.etiqueta}). No necesitas pagarlo."
                )
            }
            if (reporte != null) {
                TarjetaSeccion(titulo = "Tu reporte de problema") {
                    EtiquetaInteres(texto = reporte.estado.etiqueta, color = colorReporte(reporte.estado))
                    FilaDato("Motivo", reporte.motivo.etiqueta)
                    reporte.comentario?.let { FilaDato("Tu comentario", it) }
                    FilaDato("Enviado", formatoFechaHora(reporte.fechaCreacion))
                    FilaDato("N° de reporte", reporte.numero)
                    if (reporte.respuesta != null) {
                        HorizontalDivider()
                        Text(text = "Respuesta de PagoFlex", style = MaterialTheme.typography.titleMedium)
                        Text(text = reporte.respuesta, style = MaterialTheme.typography.bodyMedium)
                        FilaDato("Resultado", compromiso.estado.etiqueta)
                    }
                }
            }

            if (empresa != null) {
                TarjetaSeccion(titulo = "Contacto de ${empresa.nombreFantasia}") {
                    FilaDato("Correo", empresa.correoAtencion)
                    FilaDato("Teléfono", empresa.telefonoAtencion)
                }
            }

            if (sePuedePagar) {
                BotonPrincipal(
                    texto = "Pagar ${formatoPesos(totalAPagar(compromiso))}",
                    onClick = onPagar,
                    icono = R.drawable.ic_tarjeta,
                    habilitado = motivoNoPagar == null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (puedeReportar) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_reportar),
                    onClick = onReportar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Vencido")
@Composable
private fun DetalleVencidoPreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        DetalleCompromisoScreen(
            datos = datos,
            compromiso = datos.compromisoPorId(106) ?: datos.compromisos.first(),
            motivoNoPagar = null,
            puedeReportar = true,
            onVolver = {}, onPagar = {}, onReportar = {}, onVerComprobante = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1200, name = "Pagado")
@Composable
private fun DetallePagadoPreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        DetalleCompromisoScreen(
            datos = datos,
            compromiso = datos.compromisoPorId(datos.pagos.first().idCompromiso) ?: datos.compromisos.first(),
            motivoNoPagar = null,
            puedeReportar = false,
            onVolver = {}, onPagar = {}, onReportar = {}, onVerComprobante = {}
        )
    }
}
