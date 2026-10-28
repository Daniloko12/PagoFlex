package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.aniosHistorial
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.empresasDelUsuario
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.historialFiltrado
import com.example.pagoflex.model.nombreMes
import com.example.pagoflex.model.pagoATiempo
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.coloresExtra
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun HistorialScreen(
    datos: BaseDatos,
    usuario: Usuario,
    onVerComprobante: (String) -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var idEmpresa by rememberSaveable { mutableStateOf<Int?>(null) }
    var anio by rememberSaveable { mutableStateOf<Int?>(null) }
    var mes by rememberSaveable { mutableStateOf<Int?>(null) }

    val movimientos = datos.historialFiltrado(usuario.idUsuario, idEmpresa, anio, mes)

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_historial),
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
                TarjetaSeccion(titulo = "Buscar un pago", subtitulo = "Filtra por empresa y por período") {
                    ListaDesplegable(
                        etiqueta = "Empresa",
                        opciones = listOf<Int?>(null) + datos.empresasDelUsuario(usuario.idUsuario).map { it.idEmpresa },
                        seleccionada = idEmpresa,
                        onSeleccionar = { idEmpresa = it },
                        textoDe = { id -> if (id == null) "Todas" else datos.empresaPorId(id)?.nombreFantasia ?: "" },
                        icono = R.drawable.ic_empresa,
                        textoVacio = "Todas"
                    )
                    ListaDesplegable(
                        etiqueta = "Año",
                        opciones = listOf<Int?>(null) + datos.aniosHistorial(usuario.idUsuario),
                        seleccionada = anio,
                        onSeleccionar = { anio = it },
                        textoDe = { it?.toString() ?: "Todos" },
                        icono = R.drawable.ic_calendario,
                        textoVacio = "Todos"
                    )
                    ListaDesplegable(
                        etiqueta = "Mes",
                        opciones = listOf<Int?>(null) + (1..12).toList(),
                        seleccionada = mes,
                        onSeleccionar = { mes = it },
                        textoDe = { if (it == null) "Todos" else nombreMes(it) },
                        icono = R.drawable.ic_calendario,
                        textoVacio = "Todos"
                    )
                }
            }
            item {
                TituloLista(
                    texto = "Tus pagos",
                    subtitulo = "Incluye los pagos del portal y del agente conversacional. Toca uno para ver su comprobante."
                )
            }
            if (movimientos.isEmpty()) {
                item { MensajeVacio("No hay movimientos con estos filtros.") }
            }
            items(movimientos) { movimiento ->
                val compromiso = movimiento.compromiso
                val empresa = datos.empresaPorId(compromiso.idEmpresa)
                val pago = movimiento.pago
                if (pago != null) {
                    TarjetaLista(
                        iniciales = empresa?.let { inicialesEmpresa(it.nombreFantasia) },
                        colorAvatar = colorAvatarDe(compromiso.idEmpresa),
                        titulo = empresa?.nombreFantasia ?: "",
                        datoDerecho = formatoPesos(pago.montoPagado),
                        linea2 = conceptoConCuota(compromiso),
                        lineaFinal = "Pagado el ${formatoFecha(pago.fechaHora.toLocalDate())} ${textoCanal(pago.canal)}",
                        etiqueta = textoATiempo(pago, compromiso),
                        colorEtiqueta = if (pagoATiempo(pago, compromiso)) MaterialTheme.coloresExtra.pagado else MaterialTheme.coloresExtra.vencido,
                        onClick = { onVerComprobante(pago.numeroComprobante) }
                    )
                } else {
                    TarjetaLista(
                        iniciales = empresa?.let { inicialesEmpresa(it.nombreFantasia) },
                        colorAvatar = colorAvatarDe(compromiso.idEmpresa),
                        titulo = empresa?.nombreFantasia ?: "",
                        linea2 = conceptoConCuota(compromiso),
                        lineaFinal = "Sin efecto: ${movimiento.anulacion?.motivo?.etiqueta ?: ""}",
                        etiqueta = EstadoCompromiso.ANULADO.etiqueta,
                        colorEtiqueta = MaterialTheme.coloresExtra.anulado,
                        onClick = null
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistorialPreview() {
    PagoFlexTheme {
        HistorialScreen(
            datos = BaseDatos(),
            usuario = DatosAnexo9.usuarios.first(),
            onVerComprobante = {}
        )
    }
}
