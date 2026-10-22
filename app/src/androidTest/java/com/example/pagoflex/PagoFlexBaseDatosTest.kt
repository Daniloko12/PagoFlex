package com.example.pagoflex

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pagoflex.data.ConvertidorImagen
import com.example.pagoflex.data.PagoFlexDbHelper
import com.example.pagoflex.data.SesionPreferencias
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.model.CambiosCompromiso
import com.example.pagoflex.model.ClaveRecuperada
import com.example.pagoflex.model.Credenciales
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.DatosCliente
import com.example.pagoflex.model.DatosRecuperacion
import com.example.pagoflex.model.DiasAnticipacion
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.EstadoRecomendacion
import com.example.pagoflex.model.EstadoReporte
import com.example.pagoflex.model.MarcaTarjeta
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.MisDatos
import com.example.pagoflex.model.MotivoAnulacion
import com.example.pagoflex.model.MotivoReporte
import com.example.pagoflex.model.NuevaCuenta
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.SolicitudPago
import com.example.pagoflex.model.SolicitudReporte
import com.example.pagoflex.model.TarjetaNueva
import com.example.pagoflex.model.avancePorEmpresa
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.resumenPanel
import com.example.pagoflex.model.tarjetaPorId
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.viewmodel.FormularioViewModel
import com.example.pagoflex.viewmodel.PagoFlexViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class PagoFlexBaseDatosTest {
    private lateinit var contexto: Context

    @Before
    fun baseDeDatosLimpia() {
        contexto = InstrumentationRegistry.getInstrumentation().targetContext
        contexto.deleteDatabase(PagoFlexDbHelper.NOMBRE_BD)
        contexto.getSharedPreferences(SesionPreferencias.NOMBRE_ARCHIVO, Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun nuevaApp() = PagoFlexViewModel(contexto.applicationContext as Application)

    private fun entrar(app: PagoFlexViewModel, correo: String, clave: String = CLAVE_DE_PRUEBA) {
        assertNull(app.iniciarSesion(Credenciales(correo, clave)))
    }

    @Test
    fun precarga_lasTablasConElAnexo9() {
        val datos = nuevaApp().datos.value
        assertEquals(DatosAnexo9.usuarios.size, datos.usuarios.size)
        assertEquals(DatosAnexo9.empresas.size, datos.empresas.size)
        assertEquals(DatosAnexo9.compromisos.size, datos.compromisos.size)
        assertEquals(DatosAnexo9.pagos.size, datos.pagos.size)
        assertEquals(DatosAnexo9.reportes.size, datos.reportes.size)
        assertEquals(DatosAnexo9.rubros.size, datos.rubros.size)
        assertEquals(DatosAnexo9.tarjetas.size, datos.tarjetas.size)
        assertEquals(EstadoCompromiso.VENCIDO, datos.compromisoPorId(102)?.estado)
        assertEquals(Rol.ADMINISTRADOR, datos.usuarioPorId(9)?.rol)
        assertEquals(MarcaTarjeta.VISA, datos.tarjetaPorId(1)?.marca)
        assertEquals(YearMonth.of(2028, 8), datos.tarjetaPorId(1)?.vencimiento)
    }

    @Test
    fun inicioDeSesion_correoYContrasena() {
        val app = nuevaApp()
        assertNotNull(app.iniciarSesion(Credenciales("crojas@correo.ejemplo", "99999")))
        assertNotNull(app.iniciarSesion(Credenciales("rsalinas@pagoflex.ejemplo", CLAVE_DE_PRUEBA)))
        assertNull(app.iniciarSesion(Credenciales("alagos@pagoflex.ejemplo", CLAVE_DE_PRUEBA)))
        assertEquals(Rol.ADMINISTRADOR, app.usuarioActual.value?.rol)
        app.cerrarSesion()
        assertNull(app.iniciarSesion(Credenciales("CRojas@Correo.Ejemplo", CLAVE_DE_PRUEBA)))
        assertEquals(1, app.usuarioActual.value?.idUsuario)
        assertEquals(Rol.USUARIO_FINAL, app.usuarioActual.value?.rol)

        assertEquals(1, nuevaApp().usuarioActual.value?.idUsuario)
        app.cerrarSesion()
        assertNull(nuevaApp().usuarioActual.value)
    }

    @Test
    fun modoOscuro_seRecuerda() {
        val app = nuevaApp()
        assertTrue(app.modoOscuro.value)
        app.cambiarModoOscuro(false)
        assertFalse(nuevaApp().modoOscuro.value)
        app.cerrarSesion()
        assertFalse(nuevaApp().modoOscuro.value)
    }

    @Test
    fun crearCuenta_quedaConSesionYSuClave() {
        val app = nuevaApp()
        val nueva = NuevaCuenta("11.111.111-1", "Elena", "Rivas Soto", "erivas@gmail.com", "+56 9 1234 5678", "73915")
        assertNull(app.crearCuenta(nueva))
        assertEquals("Elena", app.usuarioActual.value?.nombres)
        assertEquals(Rol.USUARIO_FINAL, app.usuarioActual.value?.rol)

        app.cerrarSesion()
        assertNotNull(app.iniciarSesion(Credenciales("erivas@gmail.com", CLAVE_DE_PRUEBA)))
        entrar(app, "erivas@gmail.com", clave = "73915")
        app.cerrarSesion()
        assertNotNull(app.crearCuenta(nueva))
    }

    @Test
    fun hu03_pagoConTarjetaGuardada() {
        val app = nuevaApp()
        entrar(app, "crojas@correo.ejemplo")
        val resultado = app.pagar(101, SolicitudPago(MedioPago.DEBITO, idTarjeta = 1))
        assertEquals("CPR-000454", resultado?.pago?.numeroComprobante)
        assertEquals(1, resultado?.pago?.idTarjeta)

        val otra = nuevaApp().datos.value
        assertEquals(EstadoCompromiso.PAGADO, otra.compromisoPorId(101)?.estado)
        assertTrue(otra.pagos.any { it.numeroComprobante == "CPR-000454" && it.idTarjeta == 1 })
        assertNull(app.pagar(101, SolicitudPago(MedioPago.DEBITO, idTarjeta = 1)))
        assertNull(app.pagar(103, SolicitudPago(MedioPago.DEBITO, idTarjeta = 3)))
        assertNull(app.pagar(103, SolicitudPago(MedioPago.CREDITO)))
    }

    @Test
    fun pagoConTarjetaNueva_soloMarcaYUltimos4() {
        val app = nuevaApp()
        entrar(app, "crojas@correo.ejemplo")
        val nueva = TarjetaNueva(MedioPago.CREDITO, MarcaTarjeta.MASTERCARD, "4444", "CAMILA ROJAS", YearMonth.of(2029, 11))
        val resultado = app.pagar(103, SolicitudPago(MedioPago.CREDITO, tarjetaNueva = nueva, guardarTarjeta = false))
        val datos = nuevaApp().datos.value
        val usada = datos.tarjetaPorId(resultado?.pago?.idTarjeta)
        assertEquals("4444", usada?.ultimos4)
        assertFalse(usada!!.guardada)
        assertEquals(2, datos.tarjetasGuardadas(1).size)

        app.pagar(105, SolicitudPago(MedioPago.CREDITO, tarjetaNueva = nueva.copy(ultimos4 = "1881"), guardarTarjeta = true))
        assertEquals(3, nuevaApp().datos.value.tarjetasGuardadas(1).size)
    }

    @Test
    fun misTarjetas_agregarRepetidaYQuitar() {
        val app = nuevaApp()
        entrar(app, "lmunoz@correo.ejemplo")
        val nueva = TarjetaNueva(MedioPago.CREDITO, MarcaTarjeta.VISA, "1111", "LUIS MUNOZ", YearMonth.of(2030, 1))
        val tarjeta = app.agregarTarjeta(nueva)
        assertNotNull(tarjeta)
        assertTrue(app.tarjetaRepetida(nueva))
        assertNull(app.agregarTarjeta(nueva))
        assertNull(app.quitarTarjeta(tarjeta!!.idTarjeta))
        assertFalse(nuevaApp().datos.value.tarjetasGuardadas(2).any { it.idTarjeta == tarjeta.idTarjeta })
        assertNotNull(app.quitarTarjeta(1))
    }

    @Test
    fun aclaracion5_sinConexionNoSePaga() {
        val app = nuevaApp()
        entrar(app, "crojas@correo.ejemplo")
        app.cambiarConexion(true)
        assertNull(app.pagar(101, SolicitudPago(MedioPago.TRANSFERENCIA)))
        assertEquals(EstadoCompromiso.PENDIENTE, nuevaApp().datos.value.compromisoPorId(101)?.estado)
    }

    @Test
    fun hu06_y_hu10_reporteTomadoYResuelto() {
        val app = nuevaApp()
        entrar(app, "lmunoz@correo.ejemplo")
        assertNotNull(app.motivoParaNoPagar(107, revisarConexion = false))
        val reporte = app.reportar(106, SolicitudReporte(MotivoReporte.MONTO_INCORRECTO, null))
        assertEquals("REP-0012", reporte?.numero)
        assertEquals(EstadoCompromiso.EN_REVISION, app.datos.value.compromisoPorId(106)?.estado)

        app.cerrarSesion()
        entrar(app, "alagos@pagoflex.ejemplo")
        assertNotNull(app.resolverReporte(12, "Listo"))
        assertNull(app.tomarReporte(12))
        assertNull(app.resolverReporte(12, "El monto incluye el recargo por atraso de \$8.500."))
        val datos = nuevaApp().datos.value
        assertEquals(EstadoReporte.RESUELTO, datos.reportes.first { it.idReporte == 12 }.estado)
        assertEquals(9, datos.reportes.first { it.idReporte == 12 }.idAdministrador)
        assertEquals(EstadoCompromiso.VENCIDO, datos.compromisoPorId(106)?.estado)
    }

    @Test
    fun rn13_y_rn14_recomendacionPorEtapas() {
        val app = nuevaApp()
        entrar(app, "vparedes@pagoflex.ejemplo")
        assertNotNull(app.cambiarEstadoRecomendacion(4, EstadoRecomendacion.SE_UNIO, null))
        assertNull(app.avanzarRecomendacion(3))
        val datos = app.datos.value
        assertTrue(datos.reconocimientosObtenidos.any {
            it.idUsuarioFinal == 3 && it.idReconocimiento == DatosAnexo9.RECONOCIMIENTO_EMBAJADOR
        })
        assertTrue(datos.historialRecomendaciones.any { it.idRecomendacion == 3 && it.idAdministrador == 12 })
    }

    @Test
    fun hu11_registrarBuscarYAnular() {
        val app = nuevaApp()
        entrar(app, "alagos@pagoflex.ejemplo")
        val formulario = FormularioViewModel()
        formulario.onEmpresaSeleccionada(app.datos.value.empresas.first())
        formulario.onRutPersonaCambia("12.476.318-5", app.datos.value)
        formulario.onConceptoCambia("Cuota pie departamento")
        formulario.onMontoCambia("310000")
        formulario.onFechaVencimientoCambia("30-11-2026")
        formulario.onNumeroCuotaCambia("16")
        formulario.onTotalCuotasCambia("36")
        val nuevo = app.registrarCompromiso(formulario.onRegistrar(app.datos.value)!!)
        assertEquals("CP-2026-0113", nuevo?.folio)
        assertEquals(9, nuevo?.idAdminRegistro)

        app.buscarCompromisos("muñoz")
        assertTrue(app.compromisosEncontrados.value.all { it.idUsuarioFinal == 2 })
        assertTrue(app.compromisosEncontrados.value.any { it.idCompromiso == 113 })
        app.buscarCompromisos("12476318")
        assertTrue(app.compromisosEncontrados.value.isNotEmpty())
        app.buscarCompromisos("halcones")
        assertTrue(app.compromisosEncontrados.value.all { it.idEmpresa == 6 })
        app.buscarCompromisos("")
        assertEquals(app.datos.value.compromisos.size, app.compromisosEncontrados.value.size)

        assertNotNull(app.anularCompromiso(72, MotivoAnulacion.ERROR_REGISTRO, null))
        assertNull(app.anularCompromiso(113, MotivoAnulacion.ERROR_REGISTRO, null))
        assertEquals(EstadoCompromiso.ANULADO, nuevaApp().datos.value.compromisoPorId(113)?.estado)

        val repetido = FormularioViewModel()
        repetido.onEmpresaSeleccionada(app.datos.value.empresas.first())
        repetido.onRutPersonaCambia("12.476.318-5", app.datos.value)
        repetido.onConceptoCambia("Cuota pie departamento")
        repetido.onMontoCambia("305000")
        repetido.onFechaVencimientoCambia("30-11-2026")
        repetido.onNumeroCuotaCambia("16")
        repetido.onTotalCuotasCambia("36")
        assertNotNull(app.registrarCompromiso(repetido.onRegistrar(app.datos.value)!!))
    }

    @Test
    fun administrador_modificaCompromiso() {
        val app = nuevaApp()
        entrar(app, "alagos@pagoflex.ejemplo")
        val cambios = CambiosCompromiso("Cuota pie departamento", 300_000, LocalDate.of(2026, 10, 30), 14, 36)
        assertNull(app.editarCompromiso(106, cambios))
        val editado = nuevaApp().datos.value.compromisoPorId(106)
        assertEquals(EstadoCompromiso.PENDIENTE, editado?.estado)
        assertEquals(0, editado?.recargoAplicado)
        assertEquals(300_000, editado?.monto)

        assertNotNull(app.editarCompromiso(106, cambios))
        assertNotNull(app.editarCompromiso(104, cambios.copy(monto = 1)))
        assertNotNull(app.editarCompromiso(106, cambios.copy(numeroCuota = 15)))
    }

    @Test
    fun administrador_creaYEditaCliente() {
        val app = nuevaApp()
        entrar(app, "alagos@pagoflex.ejemplo")
        val datosCliente = DatosCliente("11.111.111-1", "Elena", "Rivas Soto", "erivas@gmail.com", "+56 9 1234 5678", 5, true)
        val cliente = app.crearCliente(datosCliente)
        assertNotNull(cliente)
        assertNull(app.crearCliente(datosCliente))

        assertNull(app.editarCliente(cliente!!.idUsuario, datosCliente.copy(correo = "elena.rivas@gmail.com")))
        app.cerrarSesion()
        entrar(app, "elena.rivas@gmail.com")

        app.cerrarSesion()
        entrar(app, "alagos@pagoflex.ejemplo")
        assertNull(app.editarCliente(cliente.idUsuario, datosCliente.copy(correo = "elena.rivas@gmail.com", activo = false)))
        app.cerrarSesion()
        assertNotNull(app.iniciarSesion(Credenciales("elena.rivas@gmail.com", CLAVE_DE_PRUEBA)))
    }

    @Test
    fun administrador_restableceClave() {
        val app = nuevaApp()
        assertNull(app.crearCuenta(NuevaCuenta("11.111.111-1", "Elena", "Rivas", "erivas@gmail.com", "+56 9 1234 5678", "73915")))
        val idElena = app.usuarioActual.value!!.idUsuario
        app.cerrarSesion()
        entrar(app, "alagos@pagoflex.ejemplo")
        assertNull(app.restablecerClaveCliente(idElena))
        assertNotNull(app.restablecerClaveCliente(9))
        app.cerrarSesion()
        entrar(app, "erivas@gmail.com")
    }

    @Test
    fun fotoDePerfil_seGuardaComoBlob() {
        val app = nuevaApp()
        entrar(app, "crojas@correo.ejemplo")
        assertNull(app.usuarioActual.value?.avatar)

        val foto = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        assertTrue(app.cambiarAvatarDesdeCamara(foto))
        val guardada = nuevaApp().datos.value.usuarioPorId(1)?.avatar
        assertNotNull(guardada)
        assertEquals(400, ConvertidorImagen.aBitmap(guardada!!)?.width)

        assertTrue(app.guardarAvisos(false, DiasAnticipacion.UNO))
        assertNotNull(nuevaApp().datos.value.usuarioPorId(1)?.avatar)

        app.cerrarSesion()
        assertFalse(app.cambiarAvatarDesdeCamara(foto))
    }

    @Test
    fun avisos_y_restablecerDatos() {
        val app = nuevaApp()
        entrar(app, "crojas@correo.ejemplo")
        assertTrue(app.guardarAvisos(false, DiasAnticipacion.CINCO))
        assertEquals(false, nuevaApp().datos.value.usuarioPorId(1)?.avisosActivos)
        app.pagar(101, SolicitudPago(MedioPago.DEBITO, idTarjeta = 1))

        app.cerrarSesion()
        app.restablecerDatos()
        val datos = app.datos.value
        assertEquals(EstadoCompromiso.PENDIENTE, datos.compromisoPorId(101)?.estado)
        assertEquals(true, datos.usuarioPorId(1)?.avisosActivos)
        assertFalse(datos.pagos.any { it.numeroComprobante == "CPR-000454" })
    }

    @Test
    fun pagarCuotas_avanzaHastaQuedarAlDia() {
        val app = nuevaApp()
        entrar(app, "crojas@correo.ejemplo")
        val recaudadoAntes = app.datos.value.resumenPanel().recaudadoMes

        val primero = app.pagar(101, SolicitudPago(MedioPago.DEBITO, idTarjeta = 1))
        val once = primero?.siguienteCuota
        assertEquals(11, once?.numeroCuota)
        assertEquals("Arriendo departamento noviembre", once?.concepto)
        val plazaOriente = { app.datos.value.avancePorEmpresa(1).first { it.empresa.idEmpresa == 8 } }
        assertEquals(once?.idCompromiso, plazaOriente().actual?.idCompromiso)
        assertEquals(EstadoCompromiso.PENDIENTE, nuevaApp().datos.value.compromisoPorId(once!!.idCompromiso)?.estado)

        val doce = app.pagar(once.idCompromiso, SolicitudPago(MedioPago.DEBITO, idTarjeta = 1))?.siguienteCuota
        assertEquals(12, doce?.numeroCuota)
        assertNull(app.pagar(doce!!.idCompromiso, SolicitudPago(MedioPago.DEBITO, idTarjeta = 1))?.siguienteCuota)
        assertTrue(plazaOriente().alDia)
        assertEquals(12, plazaOriente().ultimoPagado?.numeroCuota)
        assertEquals(recaudadoAntes + 3 * 520_000L, app.datos.value.resumenPanel().recaudadoMes)
    }

    @Test
    fun recuperarClave_conCorreoYRut() {
        val app = nuevaApp()
        assertNotNull(app.verificarCuenta(DatosRecuperacion("crojas@correo.ejemplo", "12.476.318-5")))
        assertNotNull(app.verificarCuenta(DatosRecuperacion("rsalinas@pagoflex.ejemplo", "13.907.264-2")))
        assertNull(app.verificarCuenta(DatosRecuperacion("crojas@correo.ejemplo", "15834207-3")))
        assertNotNull(app.recuperarClave(ClaveRecuperada("crojas@correo.ejemplo", "15.834.207-3", CLAVE_DE_PRUEBA)))
        assertNull(app.recuperarClave(ClaveRecuperada("crojas@correo.ejemplo", "15.834.207-3", "73915")))

        assertNotNull(app.iniciarSesion(Credenciales("crojas@correo.ejemplo", CLAVE_DE_PRUEBA)))
        entrar(app, "crojas@correo.ejemplo", clave = "73915")
        assertEquals(Rol.USUARIO_FINAL, app.usuarioActual.value?.rol)
    }

    @Test
    fun miPerfil_editarDatosYCambiarClave() {
        val app = nuevaApp()
        entrar(app, "crojas@correo.ejemplo")
        val nuevos = MisDatos("Camila Andrea", "Rojas Peña", "camila.rojas@gmail.com", "+56 9 1111 2222", 2)
        assertNull(app.actualizarMisDatos(nuevos))
        assertNotNull(app.actualizarMisDatos(nuevos))
        assertNotNull(app.actualizarMisDatos(nuevos.copy(correo = "lmunoz@correo.ejemplo")))
        val guardada = nuevaApp().datos.value.usuarioPorId(1)
        assertEquals("camila.rojas@gmail.com", guardada?.correo)
        assertEquals(2, guardada?.idComuna)
        assertEquals("15.834.207-3", guardada?.rut)

        assertNotNull(app.cambiarMiClave(CambioClave("99999", "73915")))
        assertNull(app.cambiarMiClave(CambioClave(CLAVE_DE_PRUEBA, "73915")))
        app.cerrarSesion()
        assertNotNull(app.iniciarSesion(Credenciales("camila.rojas@gmail.com", CLAVE_DE_PRUEBA)))
        entrar(app, "camila.rojas@gmail.com", clave = "73915")
    }
}

