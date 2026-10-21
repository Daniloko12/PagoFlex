package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.comunaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.recomendacionesOrdenadas
import com.example.pagoflex.model.rubroPorId
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun RecomendacionesScreen(
    datos: BaseDatos,
    onVerRecomendacion: (Int) -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val recomendaciones = datos.recomendacionesOrdenadas()

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_recomendaciones),
        menu = menu,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            item {
                TituloLista(
                    texto = "Organizaciones recomendadas",
                    subtitulo = "Avanzan por etapas: recibida, contactada, en evaluación y se unió. Toca una para atenderla."
                )
            }
            if (recomendaciones.isEmpty()) {
                item { MensajeVacio("Todavía no hay recomendaciones.") }
            }
            items(recomendaciones, key = { it.idRecomendacion }) { recomendacion ->
                val quien = datos.usuarioPorId(recomendacion.idUsuarioFinal)
                TarjetaLista(
                    iniciales = inicialesEmpresa(recomendacion.nombreOrganizacion),
                    colorAvatar = colorAvatarDe(recomendacion.idRubro),
                    titulo = recomendacion.nombreOrganizacion,
                    linea2 = "Recomendada por ${quien?.let { nombreCorto(it) } ?: ""}",
                    linea3 = "${datos.rubroPorId(recomendacion.idRubro)?.nombre ?: ""} · ${datos.comunaPorId(recomendacion.idComuna)?.nombre ?: ""}",
                    lineaFinal = "Recibida el ${formatoFecha(recomendacion.fecha.toLocalDate())}",
                    etiqueta = recomendacion.estado.etiqueta,
                    colorEtiqueta = colorRecomendacion(recomendacion.estado),
                    onClick = { onVerRecomendacion(recomendacion.idRecomendacion) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecomendacionesPreview() {
    PagoFlexTheme {
        RecomendacionesScreen(datos = BaseDatos(), onVerRecomendacion = {})
    }
}
