package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import com.example.pagoflex.model.EstadoRecomendacion
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.comunaPorId
import com.example.pagoflex.model.contactoDe
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.historialDe
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.rubroPorId
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
fun DetalleRecomendacionScreen(
    datos: BaseDatos,
    recomendacion: Recomendacion,
    onAvanzar: () -> Unit,
    onDescartar: (String) -> Unit,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val quienRecomienda = datos.usuarioPorId(recomendacion.idUsuarioFinal)
    val contacto = datos.contactoDe(recomendacion.idRecomendacion)
    val historial = datos.historialDe(recomendacion.idRecomendacion)
    val cerrada = recomendacion.estado == EstadoRecomendacion.SE_UNIO || recomendacion.estado == EstadoRecomendacion.DESCARTADA
    val siguiente = EstadoRecomendacion.entries.firstOrNull {
        it != EstadoRecomendacion.DESCARTADA && it.orden == recomendacion.estado.orden + 1
    }

    var descartando by rememberSaveable { mutableStateOf(false) }
    var motivo by rememberSaveable { mutableStateOf("") }
    var errorMotivo by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_detalle_recomendacion),
                subtitulo = recomendacion.nombreOrganizacion,
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
            TarjetaSeccion(titulo = recomendacion.nombreOrganizacion) {
                EtiquetaInteres(texto = recomendacion.estado.etiqueta, color = colorRecomendacion(recomendacion.estado))
                if (recomendacion.estado != EstadoRecomendacion.DESCARTADA) {
                    Text(
                        text = "Etapa ${recomendacion.estado.orden} de 4: ${recomendacion.estado.etiqueta}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    LinearProgressIndicator(
                        progress = { recomendacion.estado.orden / 4f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                FilaDato("Recomendada por", quienRecomienda?.let { nombreCompleto(it) } ?: "")
                FilaDato("Rubro", datos.rubroPorId(recomendacion.idRubro)?.nombre ?: "")
                FilaDato("Comuna", datos.comunaPorId(recomendacion.idComuna)?.nombre ?: "")
                recomendacion.rutOrganizacion?.let { FilaDato("RUT de la organización", it) }
                FilaDato("Recibida el", formatoFecha(recomendacion.fecha.toLocalDate()))
                FilaDato("N° de recomendación", recomendacion.numero)
            }

            if (contacto != null) {
                TarjetaSeccion(titulo = "Contacto (autorizado)") {
                    FilaDato("Nombre", contacto.nombre)
                    contacto.correo?.let { FilaDato("Correo", it) }
                    contacto.telefono?.let { FilaDato("Teléfono", it) }
                }
            }

            if (historial.isNotEmpty()) {
                TarjetaSeccion(titulo = "Historial") {
                    historial.forEach { cambio ->
                        val admin = datos.usuarioPorId(cambio.idAdministrador)?.let { nombreCorto(it) } ?: ""
                        Text(
                            text = "${formatoFecha(cambio.fecha.toLocalDate())}: ${cambio.estado.etiqueta}, por $admin." +
                                (cambio.motivo?.let { " Motivo: $it." } ?: ""),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            when {
                cerrada -> TarjetaMensaje(texto = "Esta recomendación ya terminó: ${recomendacion.estado.etiqueta.lowercase()}.")
                descartando -> {
                    CampoTexto(
                        valor = motivo,
                        onValorCambia = {
                            motivo = it
                            errorMotivo = null
                        },
                        etiqueta = "¿Por qué se descarta? *",
                        icono = R.drawable.ic_editar,
                        error = errorMotivo
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_confirmar_descarte),
                        onClick = {
                            errorMotivo = if (motivo.isBlank()) "Indica el motivo del descarte" else null
                            if (errorMotivo == null) onDescartar(motivo.trim())
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.btn_cancelar),
                        onClick = {
                            descartando = false
                            motivo = ""
                            errorMotivo = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    if (siguiente != null) {
                        BotonPrincipal(
                            texto = "Pasar a \"${siguiente.etiqueta}\"",
                            onClick = onAvanzar,
                            icono = R.drawable.ic_check,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BotonSecundario(
                        texto = stringResource(R.string.btn_descartar),
                        onClick = { descartando = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleRecomendacionPreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        DetalleRecomendacionScreen(
            datos = datos,
            recomendacion = datos.recomendaciones.first { it.estado == EstadoRecomendacion.EN_EVALUACION },
            onAvanzar = {}, onDescartar = {}, onVolver = {}
        )
    }
}
