package com.example.pagoflex.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.ui.styles.EstilosCampo
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun CasillaConTexto(
    texto: String,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = marcado, onValueChange = onCambio, role = Role.Checkbox)
                .padding(vertical = Dimens.espacioMini),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = marcado, onCheckedChange = null)
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = Dimens.espacioMedio)
            )
        }
    }
}

@Composable
fun InterruptorConTexto(
    texto: String,
    activado: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    descripcion: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = activado, onValueChange = onCambio, role = Role.Switch)
            .padding(vertical = Dimens.espacioMini),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = texto, style = MaterialTheme.typography.bodyMedium)
            if (descripcion != null) {
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = activado, onCheckedChange = null)
    }
}

@Composable
fun <T> GrupoRadio(
    opciones: List<T>,
    seleccionada: T?,
    onSeleccionar: (T) -> Unit,
    textoDe: (T) -> String,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    error: String? = null
) {
    Column(modifier = modifier.selectableGroup()) {
        opciones.forEach { opcion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = opcion == seleccionada,
                        enabled = habilitado,
                        onClick = { onSeleccionar(opcion) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = Dimens.espacioMini),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = opcion == seleccionada, onClick = null, enabled = habilitado)
                Text(
                    text = textoDe(opcion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (habilitado) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = Dimens.espacioChico)
                )
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = Dimens.espacioMedio)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ListaDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T?,
    onSeleccionar: (T) -> Unit,
    textoDe: (T) -> String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    textoVacio: String = ""
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (seleccionada != null) textoDe(seleccionada) else textoVacio,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = icono),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else null,
            singleLine = true,
            shape = EstilosCampo.forma,
            colors = EstilosCampo.colores(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcion)) },
                    onClick = {
                        onSeleccionar(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
fun <T> SelectorSegmentado(
    opciones: List<T>,
    seleccionada: T,
    onSeleccionar: (T) -> Unit,
    textoDe: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radioBoton))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(Dimens.espacioMini)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
    ) {
        opciones.forEach { opcion ->
            val activa = opcion == seleccionada
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(Dimens.radioCampo))
                    .background(if (activa) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .selectable(selected = activa, onClick = { onSeleccionar(opcion) }, role = Role.Tab),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = textoDe(opcion),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (activa) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun OpcionCuenta(
    nombre: String,
    detalle: String,
    iniciales: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorAvatar: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (seleccionada) MaterialTheme.colorScheme.primary.copy(alpha = 0.07f) else Color.Transparent)
            .selectable(selected = seleccionada, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = Dimens.espacioMedio, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarIniciales(iniciales = iniciales, color = colorAvatar)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.espacioMedio)
        ) {
            Text(text = nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (seleccionada) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circulo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ControlesSeleccionPreview() {
    PagoFlexTheme {
        Column(Modifier.padding(16.dp)) {
            SelectorSegmentado(
                opciones = listOf("Personas", "PagoFlex", "Empresas"),
                seleccionada = "Personas",
                onSeleccionar = {},
                textoDe = { it }
            )
            OpcionCuenta(nombre = "Camila Rojas", detalle = "RUT 15.*.*-3", iniciales = "CR", seleccionada = true, onClick = {})
            OpcionCuenta(nombre = "Luis Muñoz", detalle = "RUT 12.*.*-5", iniciales = "LM", seleccionada = false, onClick = {})
            CasillaConTexto(texto = "Revisé el total a pagar", marcado = true, onCambio = {})
            InterruptorConTexto(
                texto = "Avisos de vencimiento",
                descripcion = "Nunca entre las 22:00 y las 08:00",
                activado = true,
                onCambio = {}
            )
            GrupoRadio(
                opciones = listOf("1 día antes", "3 días antes", "5 días antes"),
                seleccionada = "3 días antes",
                onSeleccionar = {},
                textoDe = { it }
            )
            ListaDesplegable(
                etiqueta = "Comuna",
                opciones = listOf("Maipú", "Ñuñoa"),
                seleccionada = "Maipú",
                onSeleccionar = {},
                textoDe = { it },
                icono = R.drawable.ic_ubicacion
            )
        }
    }
}
