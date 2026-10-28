package com.example.pagoflex.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.ui.theme.PagoFlexTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    @DrawableRes icono: Int,
    descripcionIcono: String,
    onIconoClick: () -> Unit,
    subtitulo: String? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(text = titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitulo != null) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onIconoClick) {
                Icon(painter = painterResource(icono), contentDescription = descripcionIcono)
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun BarraSuperiorPreview() {
    PagoFlexTheme {
        Column {
            BarraSuperior(titulo = "Hola, Camila", icono = R.drawable.ic_menu, descripcionIcono = "", onIconoClick = {})
            BarraSuperior(
                titulo = "Detalle del compromiso",
                subtitulo = "Altos del Maipo",
                icono = R.drawable.ic_volver,
                descripcionIcono = "",
                onIconoClick = {}
            )
        }
    }
}
