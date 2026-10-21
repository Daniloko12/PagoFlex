package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.Canal
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CategoriaCumplimiento
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.DiasAnticipacion
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.EstadoRecomendacion
import com.example.pagoflex.model.EstadoReporte
import com.example.pagoflex.model.MotivoReporte
import com.example.pagoflex.model.Pago
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.diasAtraso
import com.example.pagoflex.model.diasAtrasoDelPago
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoFechaHora
import com.example.pagoflex.model.pagoATiempo
import com.example.pagoflex.model.primerNombre
import com.example.pagoflex.model.tarjetaPorId
import com.example.pagoflex.model.textoTarjeta
import com.example.pagoflex.ui.styles.EstilosTarjeta
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.coloresExtra
import java.time.LocalDate
import java.time.LocalDateTime

val ESPACIO_BOTON_FLOTANTE = 88.dp

@Composable
fun colorEstado(estado: EstadoCompromiso): Color {
    val colores = MaterialTheme.coloresExtra
    return when (estado) {
        EstadoCompromiso.PENDIENTE -> colores.pendiente
        EstadoCompromiso.VENCIDO -> colores.vencido
        EstadoCompromiso.PAGADO -> colores.pagado
        EstadoCompromiso.EN_REVISION -> colores.enRevision
        EstadoCompromiso.ANULADO -> colores.anulado
    }
}

@Composable
fun colorReporte(estado: EstadoReporte): Color {
    val colores = MaterialTheme.coloresExtra
    return when (estado) {
        EstadoReporte.ABIERTO -> colores.pendiente
        EstadoReporte.EN_GESTION -> colores.enRevision
        EstadoReporte.RESUELTO -> colores.exito
    }
}

@Composable
fun colorRecomendacion(estado: EstadoRecomendacion): Color {
    val colores = MaterialTheme.coloresExtra
    return when (estado) {
        EstadoRecomendacion.RECIBIDA -> colores.pendiente
        EstadoRecomendacion.CONTACTADA -> colores.enRevision
        EstadoRecomendacion.EN_EVALUACION -> colores.enRevision
        EstadoRecomendacion.SE_UNIO -> colores.exito
        EstadoRecomendacion.DESCARTADA -> colores.anulado
    }
}

@Composable
fun colorCategoria(categoria: CategoriaCumplimiento): Color {
    val colores = MaterialTheme.coloresExtra
    return when (categoria) {
        CategoriaCumplimiento.EJEMPLAR -> colores.exito
        CategoriaCumplimiento.AL_DIA -> colores.exito
        CategoriaCumplimiento.EN_CAMINO -> colores.advertencia
        CategoriaCumplimiento.INSUFICIENTE -> colores.pendiente
    }
}

@Composable
fun colorAvatarDe(id: Int): Color {
    val avatares = MaterialTheme.coloresExtra.avatares
    return avatares[id % avatares.size]
}

fun textoDias(dias: Int): String = if (dias == 1) "1 día" else "$dias días"

fun textoVencimiento(compromiso: Compromiso, hoy: LocalDate): String = when {
    compromiso.estado == EstadoCompromiso.VENCIDO ->
        "Venció el ${formatoFecha(compromiso.fechaVencimiento)} (hace ${textoDias(diasAtraso(compromiso, hoy))})"
    compromiso.fechaVencimiento == hoy -> "Vence hoy"
    compromiso.fechaVencimiento.isBefore(hoy) -> "Vencía el ${formatoFecha(compromiso.fechaVencimiento)}"
    else -> "Vence el ${formatoFecha(compromiso.fechaVencimiento)}"
}

fun conceptoConCuota(compromiso: Compromiso): String =
    if (compromiso.numeroCuota != null && compromiso.totalCuotas != null)
        "${compromiso.concepto} (cuota ${compromiso.numeroCuota} de ${compromiso.totalCuotas})"
    else compromiso.concepto

fun tituloMotivo(motivo: MotivoReporte): String = when (motivo) {
    MotivoReporte.YA_PAGUE -> "Dice que ya pagó por otro medio"
    MotivoReporte.NO_RECONOZCO -> "No reconoce este cobro"
    MotivoReporte.MONTO_INCORRECTO -> "Dice que el monto no corresponde"
    MotivoReporte.OTRO -> "Otro problema con el cobro"
}

fun textoCanal(canal: Canal): String = when (canal) {
    Canal.APP -> "desde la app"
    Canal.PORTAL -> "en el portal de pagos"
    Canal.AGENTE_CONVERSACIONAL -> "con el agente conversacional"
}

fun textoMedioPago(datos: BaseDatos, pago: Pago): String =
    datos.tarjetaPorId(pago.idTarjeta)?.let { "${pago.medioPago.etiqueta} · ${textoTarjeta(it)}" }
        ?: pago.medioPago.etiqueta

fun textoATiempo(pago: Pago, compromiso: Compromiso): String =
    if (pagoATiempo(pago, compromiso)) "A tiempo"
    else "Con ${textoDias(diasAtrasoDelPago(pago, compromiso))} de atraso"

fun nombreCorto(usuario: Usuario): String =
    "${primerNombre(usuario)} ${usuario.apellidos.substringBefore(" ")}"

fun inicialesDe(usuario: Usuario): String =
    "${usuario.nombres.first()}${usuario.apellidos.first()}".uppercase()

fun inicialesEmpresa(nombre: String): String {
    val conectores = setOf("de", "del", "la", "las", "los", "el", "y")
    val palabras = nombre.split(" ").filter { it.isNotBlank() && it.lowercase() !in conectores }
    return palabras.take(2).joinToString("") { it.first().uppercase() }
}

fun nombreAcceso(rol: Rol): String = when (rol) {
    Rol.USUARIO_FINAL -> "Usuario"
    Rol.ADMINISTRADOR -> "Administrador"
}

fun anticipacionDe(usuario: Usuario): DiasAnticipacion =
    DiasAnticipacion.entries.find { it.dias == usuario.diasAnticipacion } ?: DiasAnticipacion.TRES

fun detalleSesion(usuario: Usuario): String = when (usuario.rol) {
    Rol.USUARIO_FINAL -> usuario.fechaIncorporacion?.let { "Cliente PagoFlex desde ${it.year}" } ?: "Cliente PagoFlex"
    Rol.ADMINISTRADOR -> "Administrador PagoFlex"
}

@Composable
fun TituloLista(texto: String, modifier: Modifier = Modifier, subtitulo: String? = null) {
    Column(modifier = modifier.padding(top = Dimens.espacioChico)) {
        Text(text = texto, style = MaterialTheme.typography.titleMedium)
        if (subtitulo != null) {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MensajeVacio(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.espacioGrande)
    )
}

@Composable
fun EstadoConexion(
    sinConexion: Boolean,
    ultimaActualizacion: LocalDateTime,
    onActualizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.coloresExtra
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        color = if (sinConexion) colores.advertenciaFondo else MaterialTheme.colorScheme.surface,
        contentColor = if (sinConexion) colores.advertencia else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioMini),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (sinConexion)
                    "Sin conexión. Esta es tu última situación conocida, del ${formatoFechaHora(ultimaActualizacion)}."
                else "Actualizado: ${formatoFechaHora(ultimaActualizacion)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onActualizar) {
                Icon(
                    painter = painterResource(R.drawable.ic_actualizar),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = stringResource(R.string.btn_actualizar), modifier = Modifier.padding(start = Dimens.espacioMini))
            }
        }
    }
}
