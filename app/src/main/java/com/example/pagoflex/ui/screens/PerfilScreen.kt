package com.example.pagoflex.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.compromisosVigentes
import com.example.pagoflex.model.comunaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.permisos.Permisos
import com.example.pagoflex.ui.components.AvatarCircular
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    datos: BaseDatos,
    usuario: Usuario,
    onFotoTomada: (Bitmap) -> Unit,
    onImagenElegida: (Uri) -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val textoCamaraDenegada = stringResource(R.string.permiso_camara_denegado)
    val textoGaleriaDenegada = stringResource(R.string.permiso_galeria_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val resultado = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = textoAjustes,
                duration = SnackbarDuration.Long
            )
            if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) onFotoTomada(foto)
    }

    fun abrirCamara() {
        try {
            lanzadorCamara.launch(null)
        } catch (e: ActivityNotFoundException) {
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) abrirCamara() else avisarPermisoDenegado(textoCamaraDenegada)
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val uri = resultado.data?.data
        if (resultado.resultCode == Activity.RESULT_OK && uri != null) onImagenElegida(uri)
    }

    fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            lanzadorGaleria.launch(intent)
        } catch (e: ActivityNotFoundException) {
        }
    }

    val pedirPermisoImagenes = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (concedido) abrirGaleria() else avisarPermisoDenegado(textoGaleriaDenegada)
    }

    PerfilContent(
        datos = datos,
        usuario = usuario,
        foto = fotoDe(usuario.avatar),
        onTomarFoto = {
            if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
            else pedirPermisoCamara.launch(Permisos.CAMARA)
        },
        onAbrirGaleria = {
            if (Permisos.tiene(context, Permisos.IMAGENES)) abrirGaleria()
            else pedirPermisoImagenes.launch(Permisos.IMAGENES)
        },
        menu = menu,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun PerfilContent(
    datos: BaseDatos,
    usuario: Usuario,
    foto: ImageBitmap? = null,
    onTomarFoto: () -> Unit = {},
    onAbrirGaleria: () -> Unit = {},
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_perfil),
        menu = menu,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EncabezadoPerfil(foto = foto)

            Spacer(Modifier.height(Dimens.espacioChico))
            Text(text = nombreCompleto(usuario), style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${usuario.rol.etiqueta} · ${usuario.correo}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.texto_cambiar_foto),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.espacioMedio)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                modifier = Modifier.padding(top = Dimens.espacioChico)
            ) {
                BotonConIcono(R.drawable.ic_camara, stringResource(R.string.btn_camara), onTomarFoto)
                BotonConIcono(R.drawable.ic_galeria, stringResource(R.string.btn_galeria), onAbrirGaleria)
            }

            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                TarjetaSeccion(titulo = "Mis datos") {
                    FilaDato("RUT", usuario.rut)
                    FilaDato("Correo", usuario.correo)
                    usuario.telefono?.let { FilaDato("Teléfono", it) }
                    datos.comunaPorId(usuario.idComuna)?.let { FilaDato("Comuna", it.nombre) }
                    FilaDato("En PagoFlex desde", formatoFecha(usuario.fechaCreacion.toLocalDate()))
                }
                if (usuario.rol == Rol.USUARIO_FINAL) {
                    val vigentes = datos.compromisosVigentes(usuario.idUsuario)
                    TarjetaSeccion(titulo = "Resumen") {
                        FilaDato("Compromisos por pagar", vigentes.count { it.estado != EstadoCompromiso.EN_REVISION }.toString())
                        FilaDato("Tarjetas guardadas", datos.tarjetasGuardadas(usuario.idUsuario).size.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonConIcono(icono: Int, texto: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(Dimens.tamanoIcono))
        Spacer(Modifier.width(Dimens.espacioChico))
        Text(texto)
    }
}

@Composable
private fun EncabezadoPerfil(foto: ImageBitmap?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTO_PORTADA + TAMANO_FOTO / 2)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ALTO_PORTADA)
                .background(Brush.verticalGradient(MaterialTheme.coloresExtra.encabezado))
                .align(Alignment.TopCenter)
        )
        AvatarCircular(
            imagen = R.drawable.avatar_usuario,
            descripcion = stringResource(R.string.cd_avatar),
            modifier = Modifier.align(Alignment.BottomCenter),
            foto = foto,
            tamano = TAMANO_FOTO
        )
    }
}

private val ALTO_PORTADA = 110.dp
private val TAMANO_FOTO = 104.dp

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PerfilPreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        PerfilContent(datos = datos, usuario = datos.usuarioPorId(1) ?: datos.usuarios.first())
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Modo claro, administrador")
@Composable
private fun PerfilAdminPreview() {
    val datos = BaseDatos()
    PagoFlexTheme(modoOscuro = false) {
        PerfilContent(datos = datos, usuario = datos.usuarioPorId(9) ?: datos.usuarios.last())
    }
}
