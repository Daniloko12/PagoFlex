package com.example.pagoflex.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.Pago
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoFechaHora
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.ocultarRut
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.coloresExtra
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun ComprobanteScreen(
    datos: BaseDatos,
    pago: Pago,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val compromiso = datos.compromisoPorId(pago.idCompromiso) ?: return
    val empresa = datos.empresaPorId(compromiso.idEmpresa)
    val titular = datos.usuarioPorId(compromiso.idUsuarioFinal)
    val recargo = pago.montoPagado - compromiso.monto
    val contexto = LocalContext.current
    val tituloCompartir = stringResource(R.string.btn_compartir)

    val textoCompartir = buildString {
        appendLine("Comprobante de pago N° ${pago.numeroComprobante}")
        appendLine("Fecha y hora: ${formatoFechaHora(pago.fechaHora)}")
        if (titular != null) {
            appendLine("Pagado por: ${nombreCompleto(titular)}")
            appendLine("RUT: ${ocultarRut(titular.rut)}")
        }
        appendLine("Empresa: ${empresa?.nombreFantasia ?: ""} (${empresa?.razonSocial ?: ""})")
        appendLine("Folio del compromiso: ${compromiso.folio}")
        appendLine("Concepto: ${conceptoConCuota(compromiso)}")
        appendLine("Total pagado: ${formatoPesos(pago.montoPagado)}")
        appendLine("Medio de pago: ${textoMedioPago(datos, pago)}")
        appendLine("Canal: ${pago.canal.etiqueta}")
        append("Estado: Pago confirmado")
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_comprobante),
                icono = R.drawable.ic_volver,
                descripcionIcono = stringResource(R.string.cd_volver),
                onIconoClick = onVolver
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val enviar = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, textoCompartir)
                    }
                    contexto.startActivity(Intent.createChooser(enviar, tituloCompartir))
                },
                icon = { Icon(painterResource(R.drawable.ic_compartir), contentDescription = null) },
                text = { Text(tituloCompartir) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
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
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circulo),
                contentDescription = null,
                tint = MaterialTheme.coloresExtra.exito,
                modifier = Modifier.size(56.dp)
            )
            EtiquetaInteres(texto = "Pago confirmado", color = MaterialTheme.coloresExtra.exito)
            Text(
                text = formatoPesos(pago.montoPagado),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${empresa?.nombreFantasia ?: ""}\n${conceptoConCuota(compromiso)}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            TarjetaSeccion(titulo = "Detalle del pago") {
                FilaDato("Fecha y hora", formatoFechaHora(pago.fechaHora))
                if (titular != null) {
                    FilaDato("Pagado por", nombreCompleto(titular))
                    FilaDato("RUT", ocultarRut(titular.rut))
                }
                FilaDato("Empresa", empresa?.razonSocial ?: "")
                FilaDato("Vencimiento", formatoFecha(compromiso.fechaVencimiento))
                FilaDato("Monto de la cuota", formatoPesos(compromiso.monto))
                FilaDato("Recargo por atraso", formatoPesos(recargo))
                FilaDato("Medio de pago", textoMedioPago(datos, pago))
                FilaDato("Canal", pago.canal.etiqueta)
                FilaDato("¿A tiempo?", textoATiempo(pago, compromiso))
                HorizontalDivider()
                FilaDato("N° de comprobante", pago.numeroComprobante)
                FilaDato("Folio del compromiso", compromiso.folio)
            }
            Text(
                text = "Este comprobante no cambia y siempre lo encontrarás en tu historial de pagos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(ESPACIO_BOTON_FLOTANTE))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ComprobantePreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        ComprobanteScreen(datos = datos, pago = datos.pagos.last(), onVolver = {})
    }
}
