package com.example.pagoflex.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pagoflex.R
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.EstadoRecomendacion
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.codigoConfirmacionDe
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.pagoPorComprobante
import com.example.pagoflex.model.primerNombre
import com.example.pagoflex.model.recomendacionPorId
import com.example.pagoflex.model.recomendacionesDe
import com.example.pagoflex.model.reporteActivoDe
import com.example.pagoflex.model.reportePorId
import com.example.pagoflex.model.textoTarjeta
import com.example.pagoflex.model.usuarioDeReporte
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.screens.AgregarTarjetaScreen
import com.example.pagoflex.ui.screens.AvanceScreen
import com.example.pagoflex.ui.screens.AvisosScreen
import com.example.pagoflex.ui.screens.ClienteFormScreen
import com.example.pagoflex.ui.screens.ClientesScreen
import com.example.pagoflex.ui.screens.CobranzaScreen
import com.example.pagoflex.ui.screens.ComprobanteScreen
import com.example.pagoflex.ui.screens.CompromisosEmpresaScreen
import com.example.pagoflex.ui.screens.DetalleClienteScreen
import com.example.pagoflex.ui.screens.DetalleCompromisoEmpresaScreen
import com.example.pagoflex.ui.screens.DetalleCompromisoScreen
import com.example.pagoflex.ui.screens.DetalleRecomendacionScreen
import com.example.pagoflex.ui.screens.DetalleReporteScreen
import com.example.pagoflex.ui.screens.EditarCompromisoScreen
import com.example.pagoflex.ui.screens.HistorialScreen
import com.example.pagoflex.ui.screens.IngresoScreen
import com.example.pagoflex.ui.screens.InicioScreen
import com.example.pagoflex.ui.screens.MenuSesion
import com.example.pagoflex.ui.screens.OpcionMenu
import com.example.pagoflex.ui.screens.PagoScreen
import com.example.pagoflex.ui.screens.PerfilScreen
import com.example.pagoflex.ui.screens.RecomendacionesScreen
import com.example.pagoflex.ui.screens.RecomendarScreen
import com.example.pagoflex.ui.screens.RegistrarCompromisoScreen
import com.example.pagoflex.ui.screens.RegistroScreen
import com.example.pagoflex.ui.screens.ReporteScreen
import com.example.pagoflex.ui.screens.ReportesScreen
import com.example.pagoflex.ui.screens.TarjetasScreen
import com.example.pagoflex.ui.screens.detalleSesion
import com.example.pagoflex.ui.screens.nombreCorto
import com.example.pagoflex.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavegacion(
    pagoFlexViewModel: PagoFlexViewModel = viewModel()
) {
    val navController = rememberNavController()

    val datos by pagoFlexViewModel.datos.collectAsStateWithLifecycle()
    val usuarioActual by pagoFlexViewModel.usuarioActual.collectAsStateWithLifecycle()
    val sinConexion by pagoFlexViewModel.sinConexion.collectAsStateWithLifecycle()
    val ultimaActualizacion by pagoFlexViewModel.ultimaActualizacion.collectAsStateWithLifecycle()
    val modoOscuro by pagoFlexViewModel.modoOscuro.collectAsStateWithLifecycle()
    val busqueda by pagoFlexViewModel.busqueda.collectAsStateWithLifecycle()
    val compromisosEncontrados by pagoFlexViewModel.compromisosEncontrados.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoSesionCerrada = stringResource(R.string.snackbar_sesion_cerrada)
    val textoDatosRestablecidos = stringResource(R.string.snackbar_datos_restablecidos)
    val textoFotoActualizada = stringResource(R.string.snackbar_foto_actualizada)
    val textoFotoError = stringResource(R.string.snackbar_foto_error)

    fun avisar(texto: String) {
        scope.launch { snackbarHostState.showSnackbar(texto) }
    }

    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    fun irAlIngreso() {
        navController.navigate(Ruta.Ingreso.ruta) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun irAPrincipal(ruta: String) {
        val inicio = usuarioActual?.let { rutaInicialDe(it.rol) } ?: return
        navController.navigate(ruta) {
            popUpTo(inicio.ruta)
            launchSingleTop = true
        }
    }

    fun cerrarSesion() {
        pagoFlexViewModel.cerrarSesion()
        irAlIngreso()
        avisar(textoSesionCerrada)
    }

    fun menuEn(ruta: Ruta) = MenuSesion(
        usuario = usuarioActual,
        detalle = usuarioActual?.let { detalleSesion(it) } ?: "",
        opciones = usuarioActual?.let { opcionesMenu(it.rol) } ?: emptyList(),
        rutaActual = ruta.ruta,
        onIrA = { irAPrincipal(it) },
        onCerrarSesion = { cerrarSesion() },
        modoOscuro = modoOscuro,
        onCambiarModo = pagoFlexViewModel::cambiarModoOscuro
    )

    LaunchedEffect(usuarioActual, rutaActual) {
        val ruta = navController.currentDestination?.route
        if (usuarioActual == null && rutaActual != null &&
            ruta != Ruta.Ingreso.ruta && ruta != Ruta.Registro.ruta
        ) irAlIngreso()
    }

    val pantallaInicial = remember { usuarioActual?.let { rutaInicialDe(it.rol).ruta } ?: Ruta.Ingreso.ruta }

    NavHost(navController = navController, startDestination = pantallaInicial) {
        composable(Ruta.Ingreso.ruta) {
            IngresoScreen(
                datos = datos,
                onIngresar = { credenciales ->
                    val error = pagoFlexViewModel.iniciarSesion(credenciales)
                    if (error == null) {
                        navController.navigate(rutaInicialDe(credenciales.rol).ruta) {
                            popUpTo(Ruta.Ingreso.ruta) { inclusive = true }
                        }
                    }
                    error
                },
                onRestablecer = {
                    pagoFlexViewModel.restablecerDatos()
                    avisar(textoDatosRestablecidos)
                },
                onCrearCuenta = { navController.navigate(Ruta.Registro.ruta) },
                modoOscuro = modoOscuro,
                onCambiarModo = pagoFlexViewModel::cambiarModoOscuro,
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Registro.ruta) {
            RegistroScreen(
                datos = datos,
                onCuentaValida = { nueva ->
                    val error = pagoFlexViewModel.crearCuenta(nueva)
                    if (error == null) {
                        navController.navigate(Ruta.Inicio.ruta) {
                            popUpTo(Ruta.Ingreso.ruta) { inclusive = true }
                        }
                        avisar("¡Bienvenido/a, ${nueva.nombres.substringBefore(" ")}! Tu cuenta quedó creada.")
                    }
                    error
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Perfil.ruta) {
            val usuario = usuarioActual ?: return@composable
            PerfilScreen(
                datos = datos,
                usuario = usuario,
                onFotoTomada = { foto ->
                    avisar(if (pagoFlexViewModel.cambiarAvatarDesdeCamara(foto)) textoFotoActualizada else textoFotoError)
                },
                onImagenElegida = { uri ->
                    avisar(if (pagoFlexViewModel.cambiarAvatarDesdeGaleria(uri)) textoFotoActualizada else textoFotoError)
                },
                menu = menuEn(Ruta.Perfil),
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Inicio.ruta) {
            val usuario = usuarioActual ?: return@composable
            InicioScreen(
                datos = datos,
                usuario = usuario,
                sinConexion = sinConexion,
                ultimaActualizacion = ultimaActualizacion,
                onVerCompromiso = { id -> navController.navigate(Ruta.DetalleCompromiso.crear(id)) },
                onActualizar = { avisar(pagoFlexViewModel.actualizar()) },
                menu = menuEn(Ruta.Inicio),
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.DetalleCompromiso.ruta,
            arguments = listOf(navArgument("idCompromiso") { type = NavType.IntType })
        ) { entrada ->
            val idCompromiso = entrada.arguments?.getInt("idCompromiso") ?: 0
            val usuario = usuarioActual ?: return@composable
            val compromiso = datos.compromisoPorId(idCompromiso)
                ?.takeIf { it.idUsuarioFinal == usuario.idUsuario } ?: return@composable
            DetalleCompromisoScreen(
                datos = datos,
                compromiso = compromiso,
                motivoNoPagar = pagoFlexViewModel.motivoParaNoPagar(idCompromiso, revisarConexion = false),
                puedeReportar = pagoFlexViewModel.motivoParaNoReportar(idCompromiso) == null,
                onVolver = { navController.navigateUp() },
                onPagar = { navController.navigate(Ruta.Pagar.crear(idCompromiso)) },
                onReportar = { navController.navigate(Ruta.Reportar.crear(idCompromiso)) },
                onVerComprobante = { numero -> navController.navigate(Ruta.Comprobante.crear(numero)) },
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.Pagar.ruta,
            arguments = listOf(navArgument("idCompromiso") { type = NavType.IntType })
        ) { entrada ->
            val idCompromiso = entrada.arguments?.getInt("idCompromiso") ?: 0
            val usuario = usuarioActual ?: return@composable
            val compromiso = datos.compromisoPorId(idCompromiso)
                ?.takeIf { it.idUsuarioFinal == usuario.idUsuario } ?: return@composable
            PagoScreen(
                datos = datos,
                compromiso = compromiso,
                codigoCorrecto = codigoConfirmacionDe(usuario.rut),
                motivoNoPagar = { pagoFlexViewModel.motivoParaNoPagar(idCompromiso) },
                onPagoValido = { solicitud ->
                    val resultado = pagoFlexViewModel.pagar(idCompromiso, solicitud)
                    if (resultado == null) {
                        avisar("No pudimos confirmar el pago. No se hizo ningún cobro.")
                    } else {
                        navController.navigate(Ruta.Comprobante.crear(resultado.pago.numeroComprobante)) {
                            popUpTo(Ruta.Inicio.ruta)
                        }
                        avisar(
                            if (resultado.reconocimientos.isEmpty()) "¡Pago confirmado!"
                            else "¡Pago confirmado! Nuevo reconocimiento: ${resultado.reconocimientos.joinToString(", ")}"
                        )
                    }
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.Reportar.ruta,
            arguments = listOf(navArgument("idCompromiso") { type = NavType.IntType })
        ) { entrada ->
            val idCompromiso = entrada.arguments?.getInt("idCompromiso") ?: 0
            val usuario = usuarioActual ?: return@composable
            val compromiso = datos.compromisoPorId(idCompromiso)
                ?.takeIf { it.idUsuarioFinal == usuario.idUsuario } ?: return@composable
            ReporteScreen(
                datos = datos,
                compromiso = compromiso,
                motivoNoReportar = { pagoFlexViewModel.motivoParaNoReportar(idCompromiso) },
                onReporteValido = { solicitud ->
                    if (pagoFlexViewModel.reportar(idCompromiso, solicitud) != null) {
                        navController.navigateUp()
                        avisar("Recibimos tu reporte. Mientras lo revisamos, este cobro no suma días de atraso.")
                    } else {
                        avisar("No pudimos enviar el reporte. Inténtalo de nuevo.")
                    }
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.Comprobante.ruta,
            arguments = listOf(navArgument("numero") { type = NavType.StringType })
        ) { entrada ->
            val numero = entrada.arguments?.getString("numero") ?: ""
            val usuario = usuarioActual ?: return@composable
            val pago = datos.pagoPorComprobante(numero) ?: return@composable
            if (datos.compromisoPorId(pago.idCompromiso)?.idUsuarioFinal != usuario.idUsuario) return@composable
            ComprobanteScreen(
                datos = datos,
                pago = pago,
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Historial.ruta) {
            val usuario = usuarioActual ?: return@composable
            HistorialScreen(
                datos = datos,
                usuario = usuario,
                onVerComprobante = { numero -> navController.navigate(Ruta.Comprobante.crear(numero)) },
                menu = menuEn(Ruta.Historial),
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Avance.ruta) {
            val usuario = usuarioActual ?: return@composable
            AvanceScreen(
                datos = datos,
                usuario = usuario,
                onRecomendar = { navController.navigate(Ruta.Recomendar.ruta) },
                menu = menuEn(Ruta.Avance),
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Tarjetas.ruta) {
            val usuario = usuarioActual?.takeIf { it.rol == Rol.USUARIO_FINAL } ?: return@composable
            TarjetasScreen(
                datos = datos,
                idUsuario = usuario.idUsuario,
                onAgregar = { navController.navigate(Ruta.AgregarTarjeta.ruta) },
                onQuitar = { idTarjeta ->
                    avisar(pagoFlexViewModel.quitarTarjeta(idTarjeta) ?: "Listo, quitamos la tarjeta de \"Mis tarjetas\".")
                },
                menu = menuEn(Ruta.Tarjetas),
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.AgregarTarjeta.ruta) {
            if (usuarioActual?.rol != Rol.USUARIO_FINAL) return@composable
            AgregarTarjetaScreen(
                hoy = datos.fechaHoy,
                onTarjetaValida = { nueva ->
                    if (pagoFlexViewModel.tarjetaRepetida(nueva)) {
                        false
                    } else {
                        val tarjeta = pagoFlexViewModel.agregarTarjeta(nueva)
                        if (tarjeta != null) {
                            navController.navigateUp()
                            avisar("Guardamos tu ${textoTarjeta(tarjeta)}.")
                        } else {
                            avisar("No pudimos guardar la tarjeta. Inténtalo de nuevo.")
                        }
                        true
                    }
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Recomendar.ruta) {
            val usuario = usuarioActual ?: return@composable
            RecomendarScreen(
                rubros = datos.rubros,
                comunas = datos.comunas,
                propias = datos.recomendacionesDe(usuario.idUsuario),
                hoy = datos.fechaHoy,
                onRecomendacionValida = { nueva ->
                    val recomendacion = pagoFlexViewModel.registrarRecomendacion(nueva)
                    if (recomendacion != null) {
                        navController.navigateUp()
                        avisar("¡Gracias! Registramos tu recomendación de ${recomendacion.nombreOrganizacion}. Te contaremos cuando avance.")
                    } else {
                        avisar("No pudimos registrar la recomendación. Inténtalo de nuevo.")
                    }
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Avisos.ruta) {
            val usuario = usuarioActual ?: return@composable
            AvisosScreen(
                datos = datos,
                usuario = usuario,
                sinConexion = sinConexion,
                onGuardar = { activos, anticipacion ->
                    avisar(
                        if (pagoFlexViewModel.guardarAvisos(activos, anticipacion)) "Listo, guardamos tus preferencias de avisos."
                        else "No pudimos guardar tus preferencias. Inténtalo de nuevo."
                    )
                },
                onCambiarConexion = { sinSenal ->
                    pagoFlexViewModel.cambiarConexion(sinSenal)
                    avisar(if (sinSenal) "Modo sin conexión activado" else "Conexión restablecida")
                },
                menu = menuEn(Ruta.Avisos),
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.CompromisosEmpresa.ruta) {
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            CompromisosEmpresaScreen(
                datos = datos,
                encontrados = compromisosEncontrados,
                busqueda = busqueda,
                onBuscar = pagoFlexViewModel::buscarCompromisos,
                onVerCompromiso = { id -> navController.navigate(Ruta.DetalleCompromisoEmpresa.crear(id)) },
                onRegistrar = { navController.navigate(Ruta.RegistrarCompromiso.crear()) },
                menu = menuEn(Ruta.CompromisosEmpresa),
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.DetalleCompromisoEmpresa.ruta,
            arguments = listOf(navArgument("idCompromiso") { type = NavType.IntType })
        ) { entrada ->
            val idCompromiso = entrada.arguments?.getInt("idCompromiso") ?: 0
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            val compromiso = datos.compromisoPorId(idCompromiso) ?: return@composable
            DetalleCompromisoEmpresaScreen(
                datos = datos,
                compromiso = compromiso,
                onAnular = { motivo, comentario ->
                    val tieneReporte = datos.reporteActivoDe(idCompromiso) != null
                    val persona = datos.usuarioPorId(compromiso.idUsuarioFinal)?.let { nombreCompleto(it) } ?: "la persona"
                    val error = pagoFlexViewModel.anularCompromiso(idCompromiso, motivo, comentario)
                    avisar(
                        error ?: ("Se anuló \"${compromiso.concepto}\" de $persona." +
                            if (tieneReporte) " Recuerda responder su reporte en \"Reportes\"." else "")
                    )
                },
                onEditar = { navController.navigate(Ruta.EditarCompromiso.crear(idCompromiso)) },
                onVerCliente = { idUsuario -> navController.navigate(Ruta.DetalleCliente.crear(idUsuario)) },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.RegistrarCompromiso.ruta,
            arguments = listOf(navArgument("rut") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { entrada ->
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            RegistrarCompromisoScreen(
                datos = datos,
                rutInicial = entrada.arguments?.getString("rut"),
                onRegistroExitoso = { nuevo ->
                    val compromiso = pagoFlexViewModel.registrarCompromiso(nuevo)
                    if (compromiso == null) {
                        avisar("No pudimos registrar el compromiso. Inténtalo de nuevo.")
                    } else {
                        val persona = pagoFlexViewModel.datos.value.usuarioPorId(compromiso.idUsuarioFinal)
                        navController.navigateUp()
                        avisar(
                            "Listo: registramos \"${compromiso.concepto}\" para " +
                                "${persona?.let { nombreCorto(it) } ?: "la persona"}. Ya lo ve en su app."
                        )
                    }
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.EditarCompromiso.ruta,
            arguments = listOf(navArgument("idCompromiso") { type = NavType.IntType })
        ) { entrada ->
            val idCompromiso = entrada.arguments?.getInt("idCompromiso") ?: 0
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            val compromiso = datos.compromisoPorId(idCompromiso) ?: return@composable
            EditarCompromisoScreen(
                datos = datos,
                compromiso = compromiso,
                onCambiosValidos = { cambios ->
                    val error = pagoFlexViewModel.editarCompromiso(idCompromiso, cambios)
                    if (error == null) {
                        navController.navigateUp()
                        avisar("Guardamos los cambios de \"${cambios.concepto}\".")
                    }
                    error
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Cobranza.ruta) {
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            CobranzaScreen(
                datos = datos,
                onVerCompromiso = { id -> navController.navigate(Ruta.DetalleCompromisoEmpresa.crear(id)) },
                menu = menuEn(Ruta.Cobranza),
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Clientes.ruta) {
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            ClientesScreen(
                datos = datos,
                onVerCliente = { id -> navController.navigate(Ruta.DetalleCliente.crear(id)) },
                onNuevoCliente = { navController.navigate(Ruta.NuevoCliente.ruta) },
                menu = menuEn(Ruta.Clientes),
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.DetalleCliente.ruta,
            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
        ) { entrada ->
            val idUsuario = entrada.arguments?.getInt("idUsuario") ?: 0
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            val cliente = datos.usuarioPorId(idUsuario)?.takeIf { it.rol == Rol.USUARIO_FINAL } ?: return@composable
            DetalleClienteScreen(
                datos = datos,
                cliente = cliente,
                onVerCompromiso = { id -> navController.navigate(Ruta.DetalleCompromisoEmpresa.crear(id)) },
                onNuevoCompromiso = { navController.navigate(Ruta.RegistrarCompromiso.crear(cliente.rut)) },
                onEditar = { navController.navigate(Ruta.EditarCliente.crear(idUsuario)) },
                onRestablecerClave = {
                    avisar(
                        pagoFlexViewModel.restablecerClaveCliente(idUsuario)
                            ?: "La contraseña de ${primerNombre(cliente)} volvió a ser $CLAVE_DE_PRUEBA."
                    )
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.NuevoCliente.ruta) {
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            ClienteFormScreen(
                datos = datos,
                cliente = null,
                onClienteValido = { datosCliente ->
                    val nuevo = pagoFlexViewModel.crearCliente(datosCliente)
                    if (nuevo == null) {
                        avisar("No pudimos crear el cliente. Revisa que su RUT y su correo no estén registrados.")
                    } else {
                        navController.navigate(Ruta.DetalleCliente.crear(nuevo.idUsuario)) {
                            popUpTo(Ruta.Clientes.ruta)
                        }
                        avisar("Cliente creado. Ingresa con ${nuevo.correo} y la contraseña $CLAVE_DE_PRUEBA.")
                    }
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.EditarCliente.ruta,
            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
        ) { entrada ->
            val idUsuario = entrada.arguments?.getInt("idUsuario") ?: 0
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            val cliente = datos.usuarioPorId(idUsuario)?.takeIf { it.rol == Rol.USUARIO_FINAL } ?: return@composable
            ClienteFormScreen(
                datos = datos,
                cliente = cliente,
                onClienteValido = { datosCliente ->
                    val error = pagoFlexViewModel.editarCliente(idUsuario, datosCliente)
                    if (error == null) {
                        navController.navigateUp()
                        avisar("Guardamos los datos de ${datosCliente.nombres.substringBefore(" ")}.")
                    } else {
                        avisar(error)
                    }
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Reportes.ruta) {
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            ReportesScreen(
                datos = datos,
                onVerReporte = { id -> navController.navigate(Ruta.DetalleReporte.crear(id)) },
                menu = menuEn(Ruta.Reportes),
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.DetalleReporte.ruta,
            arguments = listOf(navArgument("idReporte") { type = NavType.IntType })
        ) { entrada ->
            val idReporte = entrada.arguments?.getInt("idReporte") ?: 0
            val admin = usuarioActual?.takeIf { it.rol == Rol.ADMINISTRADOR } ?: return@composable
            val reporte = datos.reportePorId(idReporte) ?: return@composable
            val persona = datos.usuarioDeReporte(reporte)?.let { nombreCorto(it) } ?: "la persona"
            DetalleReporteScreen(
                datos = datos,
                reporte = reporte,
                admin = admin,
                onTomar = {
                    avisar(pagoFlexViewModel.tomarReporte(idReporte) ?: "Tomaste el reporte de $persona. Quedaste como responsable.")
                },
                onResolver = { respuesta ->
                    val error = pagoFlexViewModel.resolverReporte(idReporte, respuesta)
                    if (error != null) {
                        avisar(error)
                    } else {
                        navController.navigateUp()
                        avisar("Reporte resuelto. $persona ya puede ver tu respuesta.")
                    }
                },
                onVerCompromiso = { navController.navigate(Ruta.DetalleCompromisoEmpresa.crear(reporte.idCompromiso)) },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }

        composable(Ruta.Recomendaciones.ruta) {
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            RecomendacionesScreen(
                datos = datos,
                onVerRecomendacion = { id -> navController.navigate(Ruta.DetalleRecomendacion.crear(id)) },
                menu = menuEn(Ruta.Recomendaciones),
                snackbarHostState = snackbarHostState
            )
        }

        composable(
            route = Ruta.DetalleRecomendacion.ruta,
            arguments = listOf(navArgument("idRecomendacion") { type = NavType.IntType })
        ) { entrada ->
            val idRecomendacion = entrada.arguments?.getInt("idRecomendacion") ?: 0
            if (usuarioActual?.rol != Rol.ADMINISTRADOR) return@composable
            val recomendacion = datos.recomendacionPorId(idRecomendacion) ?: return@composable
            val quien = datos.usuarioPorId(recomendacion.idUsuarioFinal)?.let { nombreCorto(it) } ?: "Quien la recomendó"
            DetalleRecomendacionScreen(
                datos = datos,
                recomendacion = recomendacion,
                onAvanzar = {
                    val reconocimientosAntes = pagoFlexViewModel.datos.value.reconocimientosObtenidos.size
                    val error = pagoFlexViewModel.avanzarRecomendacion(idRecomendacion)
                    if (error != null) {
                        avisar(error)
                    } else {
                        val actual = pagoFlexViewModel.datos.value
                        val nuevoEstado = actual.recomendacionPorId(idRecomendacion)?.estado
                        val reconocimiento = if (nuevoEstado == EstadoRecomendacion.SE_UNIO &&
                            actual.reconocimientosObtenidos.size > reconocimientosAntes
                        ) " $quien recibió el reconocimiento Embajador/a PagoFlex." else ""
                        avisar("${recomendacion.nombreOrganizacion} pasó a \"${nuevoEstado?.etiqueta ?: ""}\".$reconocimiento")
                    }
                },
                onDescartar = { motivo ->
                    val error = pagoFlexViewModel.cambiarEstadoRecomendacion(idRecomendacion, EstadoRecomendacion.DESCARTADA, motivo)
                    if (error != null) {
                        avisar(error)
                    } else {
                        navController.navigateUp()
                        avisar("Se descartó la recomendación de ${recomendacion.nombreOrganizacion}.")
                    }
                },
                onVolver = { navController.navigateUp() },
                snackbarHostState = snackbarHostState
            )
        }
    }
}

fun rutaInicialDe(rol: Rol): Ruta = when (rol) {
    Rol.USUARIO_FINAL -> Ruta.Inicio
    Rol.ADMINISTRADOR -> Ruta.CompromisosEmpresa
}

private fun opcionesMenu(rol: Rol): List<OpcionMenu> = when (rol) {
    Rol.USUARIO_FINAL -> listOf(
        OpcionMenu(Ruta.Inicio.ruta, R.string.menu_inicio, R.drawable.ic_inicio),
        OpcionMenu(Ruta.Historial.ruta, R.string.menu_historial, R.drawable.ic_historial),
        OpcionMenu(Ruta.Avance.ruta, R.string.menu_avance, R.drawable.ic_estrella),
        OpcionMenu(Ruta.Tarjetas.ruta, R.string.menu_tarjetas, R.drawable.ic_tarjeta),
        OpcionMenu(Ruta.Avisos.ruta, R.string.menu_avisos, R.drawable.ic_campana),
        OpcionMenu(Ruta.Perfil.ruta, R.string.menu_perfil, R.drawable.ic_persona)
    )
    Rol.ADMINISTRADOR -> listOf(
        OpcionMenu(Ruta.CompromisosEmpresa.ruta, R.string.menu_compromisos, R.drawable.ic_lista),
        OpcionMenu(Ruta.Clientes.ruta, R.string.menu_clientes, R.drawable.ic_clientes),
        OpcionMenu(Ruta.Cobranza.ruta, R.string.menu_cobranza, R.drawable.ic_grafico),
        OpcionMenu(Ruta.Reportes.ruta, R.string.menu_reportes, R.drawable.ic_reporte),
        OpcionMenu(Ruta.Recomendaciones.ruta, R.string.menu_recomendaciones, R.drawable.ic_recomendar),
        OpcionMenu(Ruta.Perfil.ruta, R.string.menu_perfil, R.drawable.ic_persona)
    )
}
