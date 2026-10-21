package com.example.pagoflex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.MarcaTarjeta
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.formatoNumeroTarjeta
import com.example.pagoflex.model.formatoVencimiento
import com.example.pagoflex.model.largoCvv
import com.example.pagoflex.model.marcaDe
import com.example.pagoflex.model.parsearVencimientoTarjeta
import com.example.pagoflex.ui.theme.Blanco
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.PrimarioClaro
import com.example.pagoflex.ui.theme.PrimarioOscuro
import com.example.pagoflex.ui.theme.VerdeIntenso
import java.time.LocalDateTime
import java.time.YearMonth

@Composable
fun CamposTarjeta(
    numero: String,
    onNumeroCambia: (String) -> Unit,
    titular: String,
    onTitularCambia: (String) -> Unit,
    vencimiento: String,
    onVencimientoCambia: (String) -> Unit,
    cvv: String,
    onCvvCambia: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorNumero: String? = null,
    errorTitular: String? = null,
    errorVencimiento: String? = null,
    errorCvv: String? = null
) {
    var cvvVisible by remember { mutableStateOf(false) }
    val mesVence = parsearVencimientoTarjeta(vencimiento)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
        CampoTexto(
            valor = numero,
            onValorCambia = onNumeroCambia,
            etiqueta = "Número de la tarjeta",
            icono = R.drawable.ic_tarjeta,
            error = errorNumero,
            ayuda = if (numero.isEmpty()) "Los números del frente de la tarjeta"
            else "${marcaDe(numero).etiqueta} · ${formatoNumeroTarjeta(numero)}",
            tipoTeclado = KeyboardType.Number
        )
        CampoTexto(
            valor = titular,
            onValorCambia = onTitularCambia,
            etiqueta = "Nombre del titular",
            icono = R.drawable.ic_persona,
            error = errorTitular,
            ayuda = "Tal como aparece en la tarjeta"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            CampoTexto(
                valor = vencimiento,
                onValorCambia = onVencimientoCambia,
                etiqueta = "Vence (MMAA)",
                icono = R.drawable.ic_calendario,
                error = errorVencimiento,
                ayuda = mesVence?.let { "Vence en ${formatoVencimiento(it)}" } ?: "Ej: 0828",
                tipoTeclado = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            CampoContrasena(
                valor = cvv,
                onValorCambia = onCvvCambia,
                etiqueta = "CVV",
                visible = cvvVisible,
                onAlternarVisible = { cvvVisible = !cvvVisible },
                error = errorCvv,
                ayuda = "${largoCvv(numero)} dígitos, al reverso",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TarjetaDePago(
    tarjeta: Tarjeta,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radioTarjeta))
            .background(Brush.linearGradient(listOf(PrimarioOscuro, VerdeIntenso)))
            .padding(Dimens.espacioGrande)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_tarjeta),
                contentDescription = null,
                tint = PrimarioClaro,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Text(
                text = if (tarjeta.medioPago == MedioPago.CREDITO) "Crédito" else "Débito",
                style = MaterialTheme.typography.labelLarge,
                color = PrimarioClaro,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.espacioChico)
            )
            Text(
                text = tarjeta.marca.etiqueta,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Blanco
            )
        }
        Spacer(Modifier.height(Dimens.espacioGrande))
        Text(
            text = "••••  ••••  ••••  ${tarjeta.ultimos4}",
            style = MaterialTheme.typography.titleLarge,
            color = Blanco
        )
        Spacer(Modifier.height(Dimens.espacioMedio))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = tarjeta.titular,
                style = MaterialTheme.typography.bodyMedium,
                color = Blanco,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Vence ${formatoVencimiento(tarjeta.vencimiento)}",
                style = MaterialTheme.typography.bodySmall,
                color = PrimarioClaro
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CamposTarjetaPreview() {
    PagoFlexTheme(modoOscuro = false) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TarjetaDePago(
                Tarjeta(1, 1, MedioPago.DEBITO, MarcaTarjeta.VISA, "4821", "CAMILA ROJAS",
                    YearMonth.of(2028, 8), true, LocalDateTime.of(2026, 8, 20, 10, 0))
            )
            CamposTarjeta(
                numero = "4111111111111111", onNumeroCambia = {},
                titular = "Camila Rojas", onTitularCambia = {},
                vencimiento = "0828", onVencimientoCambia = {},
                cvv = "123", onCvvCambia = {}
            )
        }
    }
}
