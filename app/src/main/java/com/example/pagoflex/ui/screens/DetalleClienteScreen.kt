package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.compromisosDe
import com.example.pagoflex.model.comunaPorId
import com.example.pagoflex.model.deudaDe
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.AvatarCircular
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra

@Composable
fun DetalleClienteScreen(
    datos: BaseDatos,
    cliente: Usuario,
    onVerCompromiso: (Int) -> Unit,
    onNuevoCompromiso: () -> Unit,
    onEditar: () -> Unit,
    onRestablecerClave: () -> Unit,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val compromisos = datos.compromisosDe(cliente.idUsuario)
    val porPagar = compromisos.count { it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO }
    var confirmarClave by remember { mutableStateOf(false) }

    if (confirmarClave) {
        AlertDialog(
            onDismissRequest = { confirmarClave = false },
            icon = { Icon(painterResource(R.drawable.ic_candado), contentDescription = null) },
            title = { Text(stringResource(R.string.dialogo_clave_titulo)) },
            text = { Text("La contraseña de ${nombreCorto(cliente)} volverá a ser $CLAVE_DE_PRUEBA. Pídele que la cambie al ingresar.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarClave = false
                    onRestablecerClave()
                }) { Text(stringResource(R.string.btn_restablecer)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarClave = false }) { Text(stringResource(R.string.dialogo_cancelar)) }
            }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_cliente),
                subtitulo = nombreCorto(cliente),
                icono = R.drawable.ic_volver,
                descripcionIcono = stringResource(R.string.cd_volver),
                onIconoClick = onVolver
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item {
                TarjetaSeccion(titulo = nombreCompleto(cliente)) {
                    AvatarCircular(
                        imagen = R.drawable.avatar_usuario,
                        descripcion = stringResource(R.string.cd_avatar),
                        foto = fotoDe(cliente.avatar)
                    )
                    EtiquetaInteres(
                        texto = if (cliente.activo) "Cuenta activa" else "Cuenta inactiva: no puede ingresar",
                        color = if (cliente.activo) MaterialTheme.coloresExtra.pagado else MaterialTheme.coloresExtra.anulado
                    )
                    FilaDato("RUT", cliente.rut)
                    FilaDato("Correo", cliente.correo)
                    cliente.telefono?.let { FilaDato("Teléfono", it) }
                    datos.comunaPorId(cliente.idComuna)?.let { FilaDato("Comuna", it.nombre) }
                    cliente.fechaIncorporacion?.let { FilaDato("Cliente desde", formatoFecha(it)) }
                }
            }
            item {
                TarjetaSeccion(titulo = "Situación de hoy") {
                    FilaDato("Debe hoy", formatoPesos(datos.deudaDe(cliente.idUsuario)), destacado = true)
                    FilaDato("Compromisos por pagar", porPagar.toString())
                    FilaDato("Compromisos en total", compromisos.size.toString())
                }
            }
            item {
                BotonPrincipal(
                    texto = "Nuevo compromiso",
                    onClick = onNuevoCompromiso,
                    icono = R.drawable.ic_agregar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    BotonSecundario(texto = "Editar datos", onClick = onEditar, modifier = Modifier.weight(1f))
                    BotonSecundario(texto = "Contraseña", onClick = { confirmarClave = true }, modifier = Modifier.weight(1f))
                }
            }

            item { TituloLista(texto = "Sus compromisos", subtitulo = "Toca uno para modificarlo o anularlo") }
            if (compromisos.isEmpty()) {
                item { MensajeVacio("Todavía no tiene compromisos registrados.") }
            }
            items(compromisos, key = { it.idCompromiso }) { compromiso ->
                val empresa = datos.empresaPorId(compromiso.idEmpresa)
                TarjetaLista(
                    iniciales = empresa?.let { inicialesEmpresa(it.nombreFantasia) },
                    colorAvatar = colorAvatarDe(compromiso.idEmpresa),
                    titulo = empresa?.nombreFantasia ?: "",
                    datoDerecho = formatoPesos(totalAPagar(compromiso)),
                    linea2 = conceptoConCuota(compromiso),
                    lineaFinal = textoVencimiento(compromiso, datos.fechaHoy),
                    etiqueta = compromiso.estado.etiqueta,
                    colorEtiqueta = colorEstado(compromiso.estado),
                    onClick = { onVerCompromiso(compromiso.idCompromiso) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleClientePreview() {
    val datos = BaseDatos()
    PagoFlexTheme(modoOscuro = false) {
        DetalleClienteScreen(
            datos = datos,
            cliente = datos.usuarioPorId(1) ?: datos.usuarios.first(),
            onVerCompromiso = {}, onNuevoCompromiso = {}, onEditar = {}, onRestablecerClave = {}, onVolver = {}
        )
    }
}
