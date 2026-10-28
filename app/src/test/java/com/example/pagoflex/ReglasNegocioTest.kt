package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.model.CategoriaCumplimiento
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.EstadoRecomendacion
import com.example.pagoflex.model.EstadoReporte
import com.example.pagoflex.model.MarcaTarjeta
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.MotivoAnulacion
import com.example.pagoflex.model.MotivoReporte
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.avancePorEmpresa
import com.example.pagoflex.model.calcularIndicador
import com.example.pagoflex.model.categoriaCumplimiento
import com.example.pagoflex.model.claveValida
import com.example.pagoflex.model.clientes
import com.example.pagoflex.model.codigoConfirmacionDe
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.conceptoSiguiente
import com.example.pagoflex.model.cuentaParaRecuperar
import com.example.pagoflex.model.deudaDe
import com.example.pagoflex.model.errorClaveNueva
import com.example.pagoflex.model.errorConfirmacionClave
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.formatoPesosCorto
import com.example.pagoflex.model.hashClave
import com.example.pagoflex.model.luhnValido
import com.example.pagoflex.model.marcaDe
import com.example.pagoflex.model.motivoParaNoAnular
import com.example.pagoflex.model.motivoParaNoCambiarEstado
import com.example.pagoflex.model.motivoParaNoEditar
import com.example.pagoflex.model.motivoParaNoPagar
import com.example.pagoflex.model.motivoParaNoReportar
import com.example.pagoflex.model.motivoParaNoResolver
import com.example.pagoflex.model.motivoParaNoTomar
import com.example.pagoflex.model.ocultarRut
import com.example.pagoflex.model.parsearVencimientoTarjeta
import com.example.pagoflex.model.recomendacionesDe
import com.example.pagoflex.model.reporteAtrasado
import com.example.pagoflex.model.resumenCobranza
import com.example.pagoflex.model.resumenPanel
import com.example.pagoflex.model.resumenSituacion
import com.example.pagoflex.model.siguienteCuota
import com.example.pagoflex.model.tarjetaNuevaDe
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.tasaCobranza
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.model.validarPersona
import com.example.pagoflex.model.validarRut
import com.example.pagoflex.model.validarTarjeta
import com.example.pagoflex.viewmodel.CambiarClaveViewModel
import com.example.pagoflex.viewmodel.ClienteViewModel
import com.example.pagoflex.viewmodel.EditarCompromisoViewModel
import com.example.pagoflex.viewmodel.EditarPerfilViewModel
import com.example.pagoflex.viewmodel.FormularioViewModel
import com.example.pagoflex.viewmodel.IngresoViewModel
import com.example.pagoflex.viewmodel.PagoViewModel
import com.example.pagoflex.viewmodel.RecomendacionViewModel
import com.example.pagoflex.viewmodel.RecuperarClaveViewModel
import com.example.pagoflex.viewmodel.RegistroViewModel
import com.example.pagoflex.viewmodel.ReporteViewModel
import com.example.pagoflex.viewmodel.TarjetaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ReglasNegocioTest {
    private val datos = BaseDatos()
    private fun usuario(id: Int) = datos.usuarioPorId(id)

    @Test
    fun rut_modulo11_RN18() {
        assertTrue(validarRut("15.834.207-3"))
        assertTrue(validarRut("16.248.317-K"))
        assertFalse(validarRut("15.834.207-4"))
        assertEquals("15.•••.•••-3", ocultarRut("15.834.207-3"))
    }

    @Test
    fun formatos_RNF02_RN10_RN24() {
        assertEquals("$\u00A0520.000", formatoPesos(520_000))
        assertEquals(91.7, calcularIndicador(36, 33)!!, 0.0)
        assertNull(calcularIndicador(2, 2))
        assertEquals(CategoriaCumplimiento.EJEMPLAR, categoriaCumplimiento(95.0))
        assertEquals(88.1, tasaCobranza(104, 120, 2)!!, 0.0)
    }

    @Test
    fun contrasena_5digitos_y_hash_RN19() {
        assertTrue(claveValida("12345"))
        assertFalse(claveValida("1234"))
        assertFalse(claveValida("12a45"))
        val huella = hashClave("15.834.207-3", "12345")
        assertEquals(64, huella.length)
        assertFalse(huella.contains("12345"))
        assertEquals(huella, hashClave("158342073", "12345"))
        assertNotEquals(huella, hashClave("12.476.318-5", "12345"))
    }

    @Test
    fun ingreso_soloDosRoles() {
        assertEquals(listOf(Rol.USUARIO_FINAL, Rol.ADMINISTRADOR), Rol.entries)
        val ingreso = IngresoViewModel()
        ingreso.onCorreoCambia("A@gmail")
        ingreso.onClaveCambia("12a3")
        assertEquals("123", ingreso.uiState.value.clave)
        assertNull(ingreso.onIngresar())
        assertNotNull(ingreso.uiState.value.errorCorreo)
        assertNotNull(ingreso.uiState.value.errorClave)

        ingreso.onCorreoCambia("A@gmail.com")
        ingreso.onClaveCambia("123456")
        val credenciales = ingreso.onIngresar()
        assertEquals("12345", credenciales?.clave)
        assertEquals("A@gmail.com", credenciales?.correo)
    }

    @Test
    fun crearCuenta_validaciones() {
        val registro = RegistroViewModel()
        registro.onRutCambia("15.834.207-3")
        registro.onNombresCambia("Elena")
        registro.onApellidosCambia("Rivas Soto")
        registro.onCorreoCambia("crojas@correo.ejemplo")
        registro.onTelefonoCambia("+56 9 1234 5678")
        registro.onClaveCambia("12345")
        registro.onConfirmacionCambia("12345")
        assertNull(registro.onCrearCuenta(datos))
        val estado = registro.uiState.value
        assertNotNull(estado.errorRut)
        assertNotNull(estado.errorCorreo)
        assertNotNull(estado.errorClave)
        assertNotNull(estado.errorTerminos)

        registro.onRutCambia("11.111.111-1")
        registro.onCorreoCambia("erivas@gmail.com")
        registro.onClaveCambia("73915")
        registro.onConfirmacionCambia("73916")
        registro.onAceptaTerminosCambia(true)
        assertNull(registro.onCrearCuenta(datos))
        assertEquals("Las contraseñas no coinciden", registro.uiState.value.errorConfirmacion)

        registro.onConfirmacionCambia("73915")
        val cuenta = registro.onCrearCuenta(datos)
        assertEquals("erivas@gmail.com", cuenta?.correo)
        assertEquals("73915", cuenta?.clave)
    }

    @Test
    fun hu01_resumenDeCamila() {
        val resumen = datos.resumenSituacion(1)
        assertEquals(1, resumen.cantidadVencidos)
        assertEquals("CP-2026-0101", resumen.proximoVencimiento?.folio)
    }

    @Test
    fun hu03_pagoConTarjetaGuardada_RNF06() {
        val pago = PagoViewModel()
        val debitos = datos.tarjetasGuardadas(1, MedioPago.DEBITO)
        pago.onMedioPagoSeleccionado(MedioPago.DEBITO, debitos)
        assertEquals(1, pago.uiState.value.idTarjeta)
        pago.onCodigoCambia("1111")
        pago.onConfirmaTotalCambia(true)
        val codigoCamila = codigoConfirmacionDe(usuario(1)!!.rut)
        assertEquals("4207", codigoCamila)
        assertNull(pago.onPagar(codigoCamila, datos.motivoParaNoPagar(101), debitos, datos.fechaHoy))
        assertNotNull(pago.uiState.value.errorCodigo)

        pago.onCodigoCambia("4207")
        val solicitud = pago.onPagar(codigoCamila, datos.motivoParaNoPagar(101), debitos, datos.fechaHoy)
        assertEquals(MedioPago.DEBITO, solicitud?.medioPago)
        assertEquals(1, solicitud?.idTarjeta)
        assertNull(solicitud?.tarjetaNueva)
    }

    @Test
    fun tarjetas_luhnMarcaVencimiento() {
        assertTrue(luhnValido("4111 1111 1111 1111"))
        assertFalse(luhnValido("4111111111111112"))
        assertEquals(MarcaTarjeta.VISA, marcaDe("4111111111111111"))
        assertEquals(MarcaTarjeta.MASTERCARD, marcaDe("5555555555554444"))
        assertEquals(MarcaTarjeta.AMEX, marcaDe("378282246310005"))
        assertEquals(YearMonth.of(2028, 8), parsearVencimientoTarjeta("08/28"))
        assertNull(parsearVencimientoTarjeta("1328"))

        val vencida = validarTarjeta("4111111111111111", "Camila Rojas", "0925", "123", datos.fechaHoy)
        assertEquals("Esta tarjeta está vencida", vencida.vencimiento)
        val amex = validarTarjeta("378282246310005", "Camila Rojas", "0828", "123", datos.fechaHoy)
        assertNotNull(amex.cvv)
        assertFalse(validarTarjeta("4111111111111111", "Camila Rojas", "0828", "123", datos.fechaHoy).hayErrores)
    }

    @Test
    fun tarjetaNueva_soloViajanMarcaYUltimos4_RN19() {
        val nueva = tarjetaNuevaDe(MedioPago.CREDITO, "5555555555554444", "Camila Rojas", "1129")!!
        assertEquals("4444", nueva.ultimos4)
        assertEquals(MarcaTarjeta.MASTERCARD, nueva.marca)
        assertFalse(nueva.toString().contains("555555555555"))

        val pago = PagoViewModel()
        pago.onMedioPagoSeleccionado(MedioPago.CREDITO, datos.tarjetasGuardadas(3, MedioPago.CREDITO))
        assertTrue(pago.uiState.value.usarTarjetaNueva)
        pago.onNumeroTarjetaCambia("5555 5555 5555 4444")
        pago.onTitularTarjetaCambia("Rosa Contreras")
        pago.onVencimientoTarjetaCambia("11/29")
        pago.onCvvCambia("123")
        pago.onCodigoCambia(codigoConfirmacionDe(usuario(3)!!.rut))
        pago.onConfirmaTotalCambia(true)
        val solicitud = pago.onPagar(codigoConfirmacionDe(usuario(3)!!.rut), null, emptyList(), datos.fechaHoy)
        assertEquals("4444", solicitud?.tarjetaNueva?.ultimos4)
        assertTrue(solicitud?.guardarTarjeta == true)

        val formulario = TarjetaViewModel()
        formulario.onNumeroCambia("4111111111111112")
        assertNull(formulario.onAgregar(datos.fechaHoy))
        assertNotNull(formulario.uiState.value.errorNumero)
    }

    @Test
    fun aclaracion5_y_RN04_cuandoNoSePaga() {
        assertNotNull(datos.motivoParaNoPagar(101, sinConexion = true))
        assertTrue(datos.motivoParaNoPagar(107)!!.contains("14/36"))
        assertNull(datos.motivoParaNoPagar(106))
        assertNotNull(datos.motivoParaNoPagar(104))
    }

    @Test
    fun hu06_reporte_RN09() {
        val formulario = ReporteViewModel()
        formulario.onMotivoSeleccionado(MotivoReporte.OTRO)
        assertNull(formulario.onEnviar(datos.motivoParaNoReportar(106)))
        formulario.onComentarioCambia("Me cobraron dos veces")
        assertNotNull(formulario.onEnviar(datos.motivoParaNoReportar(106)))
        assertNotNull(datos.motivoParaNoReportar(109))
    }

    @Test
    fun hu10_administradorTomaYResuelve_RN23() {
        val admin = usuario(9)
        assertEquals(Rol.ADMINISTRADOR, admin?.rol)
        assertNull(datos.motivoParaNoTomar(10, admin))
        assertNotNull(datos.motivoParaNoTomar(10, usuario(1)))
        val enGestion = datos.reportes.first { it.estado == EstadoReporte.EN_GESTION }
        val responsable = usuario(enGestion.idAdministrador!!)
        assertNotNull(datos.motivoParaNoResolver(enGestion.idReporte, responsable, " "))
        assertNull(datos.motivoParaNoResolver(enGestion.idReporte, responsable, "Listo, lo revisamos."))
        assertNotNull(datos.motivoParaNoResolver(enGestion.idReporte, admin, "Listo"))
        val rep10 = datos.reportes.first { it.numero == "REP-0010" }
        assertTrue(reporteAtrasado(rep10, datos.fechaHoy))
    }

    @Test
    fun hu10_ca4_noSeSaltanEtapas_RN13() {
        assertNotNull(datos.motivoParaNoCambiarEstado(4, EstadoRecomendacion.SE_UNIO, null))
        assertNotNull(datos.motivoParaNoCambiarEstado(4, EstadoRecomendacion.DESCARTADA, " "))
        assertNull(datos.motivoParaNoCambiarEstado(4, EstadoRecomendacion.CONTACTADA, null))
        assertNull(datos.motivoParaNoCambiarEstado(3, EstadoRecomendacion.SE_UNIO, null))
    }

    @Test
    fun hu08_recomendacion_RN12() {
        val propias = datos.recomendacionesDe(1)
        val formulario = RecomendacionViewModel()
        formulario.onNombreOrganizacionCambia("Jardín Infantil Pequeños Exploradores")
        formulario.onRubroSeleccionado(DatosAnexo9.rubros.first())
        formulario.onComunaSeleccionada(DatosAnexo9.comunas.first())
        assertNull(formulario.onEnviar(propias, datos.fechaHoy))
        assertNotNull(formulario.uiState.value.errorRecomendacion)

        formulario.onNombreOrganizacionCambia("Taller de Pintura Arcoíris")
        formulario.onNombreContactoCambia("Marta")
        assertNull(formulario.onEnviar(propias, datos.fechaHoy))
        formulario.onAutorizaContactoCambia(true)
        assertNotNull(formulario.onEnviar(propias, datos.fechaHoy))
    }

    @Test
    fun hu11_empresaDuplicadoYPersonaNueva_RN20() {
        val formulario = FormularioViewModel()
        formulario.onRutPersonaCambia("12.476.318-5", datos)
        assertEquals("Luis Alberto Muñoz Tapia", formulario.uiState.value.personaEncontrada)
        formulario.onConceptoCambia("Cuota pie departamento")
        formulario.onMontoCambia("310000")
        formulario.onFechaVencimientoCambia("30-11-2026")
        formulario.onNumeroCuotaCambia("14")
        formulario.onTotalCuotasCambia("36")
        assertNull(formulario.onRegistrar(datos))
        assertNotNull(formulario.uiState.value.errorEmpresa)
        formulario.onEmpresaSeleccionada(datos.empresas.first())
        assertNull(formulario.onRegistrar(datos))
        assertNotNull(formulario.uiState.value.errorDuplicado)
        formulario.onNumeroCuotaCambia("16")
        assertEquals(1, formulario.onRegistrar(datos)?.idEmpresa)

        val nuevo = FormularioViewModel()
        nuevo.onEmpresaSeleccionada(datos.empresas.first())
        nuevo.onRutPersonaCambia("11.111.111-1", datos)
        assertTrue(nuevo.uiState.value.personaNoExiste)
        nuevo.onConceptoCambia("Gasto común noviembre")
        nuevo.onMontoCambia("45000")
        nuevo.onFechaVencimientoCambia("10-11-2026")
        assertNull(nuevo.onRegistrar(datos))
        nuevo.onNombresPersonaCambia("Elena")
        nuevo.onApellidosPersonaCambia("Rivas Soto")
        nuevo.onCorreoPersonaCambia("erivas@gmail.com")
        nuevo.onTelefonoPersonaCambia("+56 9 1234 5678")
        assertEquals("Elena", nuevo.onRegistrar(datos)?.personaNueva?.nombres)
    }

    @Test
    fun anular_soloAdministrador_RN21() {
        val admin = usuario(9)
        assertNotNull(datos.motivoParaNoAnular(admin, 72, MotivoAnulacion.ERROR_REGISTRO, null))
        assertNotNull(datos.motivoParaNoAnular(admin, 107, MotivoAnulacion.OTRO, null))
        assertNotNull(datos.motivoParaNoAnular(usuario(1), 107, MotivoAnulacion.ERROR_REGISTRO, null))
        assertNull(datos.motivoParaNoAnular(admin, 107, MotivoAnulacion.ERROR_REGISTRO, null))
    }

    @Test
    fun modificarCompromiso_reglas() {
        assertNotNull(datos.motivoParaNoEditar(104))
        assertNotNull(datos.motivoParaNoEditar(109))
        assertNull(datos.motivoParaNoEditar(106))

        val vencido = datos.compromisoPorId(106)!!
        val editar = EditarCompromisoViewModel()
        editar.onCargar(vencido)
        assertEquals("30-09-2026", editar.uiState.value.fechaVencimiento)
        editar.onMontoCambia("300000")
        assertEquals(300_000, editar.onGuardar(vencido, datos)?.monto)

        editar.onFechaVencimientoCambia("01-10-2026")
        assertNull(editar.onGuardar(vencido, datos))
        editar.onFechaVencimientoCambia("30-10-2026")
        editar.onNumeroCuotaCambia("15")
        assertNull(editar.onGuardar(vencido, datos))
        assertNotNull(editar.uiState.value.errorDuplicado)
    }

    @Test
    fun clientes_buscarYValidar() {
        assertTrue(datos.clientes().all { it.rol == Rol.USUARIO_FINAL })
        assertEquals(listOf(2), datos.clientes("muñoz").map { it.idUsuario })
        assertEquals(listOf(1), datos.clientes("15834").map { it.idUsuario })
        assertEquals(17_000 + 520_000 + 10_000 + 86_400, datos.deudaDe(1))

        val camila = usuario(1)!!
        assertFalse(datos.validarPersona(camila.rut, "Camila", "Rojas", camila.correo, "+56 9 0000 0101", idPropio = 1).hayErrores)
        assertNotNull(datos.validarPersona(camila.rut, "Camila", "Rojas", "lmunoz@correo.ejemplo", "+56 9 0000 0101", idPropio = 1).correo)

        val formulario = ClienteViewModel()
        formulario.onCargar(camila, datos)
        assertEquals("Maipú", formulario.uiState.value.comuna?.nombre)
        formulario.onCorreoCambia("camila.rojas@gmail.com")
        assertEquals("camila.rojas@gmail.com", formulario.onGuardar(datos)?.correo)
    }

    @Test
    fun hu12_tasaSeptiembreAltosDelMaipo() {
        val resumen = datos.resumenCobranza(1, YearMonth.of(2026, 9))
        assertEquals(104, resumen.pagados)
        assertEquals(14, resumen.vencidos)
        assertEquals(88.1, resumen.tasa!!, 0.0)
    }

    @Test
    fun cuotas_alPagarSeGeneraLaSiguiente() {
        assertEquals("Arriendo departamento noviembre", conceptoSiguiente("Arriendo departamento octubre"))
        assertEquals("Cuota social enero", conceptoSiguiente("Cuota social diciembre"))
        assertEquals("Mensualidad Noviembre", conceptoSiguiente("Mensualidad Octubre"))
        assertEquals("Crédito de consumo", conceptoSiguiente("Crédito de consumo"))

        val arriendo = datos.compromisoPorId(101)!!
        val siguiente = datos.siguienteCuota(arriendo, DatosAnexo9.ULTIMA_ACTUALIZACION)
        assertEquals(11, siguiente?.numeroCuota)
        assertEquals(12, siguiente?.totalCuotas)
        assertEquals("Arriendo departamento noviembre", siguiente?.concepto)
        assertEquals(LocalDate.of(2026, 11, 10), siguiente?.fechaVencimiento)
        assertEquals(EstadoCompromiso.PENDIENTE, siguiente?.estado)
        assertEquals("CP-2026-0113", siguiente?.folio)
        assertNull(datos.siguienteCuota(datos.compromisoPorId(106)!!, DatosAnexo9.ULTIMA_ACTUALIZACION))
        assertNull(datos.siguienteCuota(datos.compromisoPorId(103)!!, DatosAnexo9.ULTIMA_ACTUALIZACION))
        assertNull(datos.siguienteCuota(arriendo.copy(numeroCuota = 12), DatosAnexo9.ULTIMA_ACTUALIZACION))
    }

    @Test
    fun inicio_unaTarjetaPorEmpresa() {
        val avances = datos.avancePorEmpresa(1)
        assertEquals(listOf(6, 8, 5, 3), avances.map { it.empresa.idEmpresa })
        assertEquals(102, avances.first().actual?.idCompromiso)
        assertEquals(101, avances[1].actual?.idCompromiso)
        assertEquals(89, avances[1].ultimoPagado?.idCompromiso)
        assertTrue(avances.none { it.alDia })

        val rosa = datos.avancePorEmpresa(3)
        assertTrue(rosa.single().alDia)
        assertEquals(108, rosa.single().ultimoPagado?.idCompromiso)
    }

    @Test
    fun panel_resumenParaElAdministrador() {
        val panel = datos.resumenPanel()
        assertEquals(datos.compromisos.size, panel.porEstado.sumOf { it.cantidad })
        assertEquals(2, panel.cantidadVencidos)
        assertEquals(335_500L, panel.montoVencido)
        assertEquals(listOf(0L, 15_000L, 1_155_800L, 15_000L), panel.recaudacionPorMes.map { it.monto })
        assertEquals(YearMonth.of(2026, 10), panel.recaudacionPorMes.last().mes)
        assertEquals(8, panel.clientesActivos)
        assertEquals(1, panel.deudaPorEmpresa.first().empresa.idEmpresa)
        assertEquals(106, panel.vencidosAntiguos.first().idCompromiso)
        assertEquals("$1,2 M", formatoPesosCorto(1_155_800))
        assertEquals("$15 mil", formatoPesosCorto(15_000))
        assertEquals("$2 M", formatoPesosCorto(2_000_000))
        assertEquals("$0", formatoPesosCorto(0))
    }

    @Test
    fun claves_recuperarYCambiar() {
        assertNotNull(errorClaveNueva("12345"))
        assertNotNull(errorClaveNueva("77777"))
        assertNotNull(errorClaveNueva("7391"))
        assertNull(errorClaveNueva("73915"))
        assertNotNull(errorConfirmacionClave("73915", "73916"))
        assertNull(errorConfirmacionClave("73915", "73915"))

        assertEquals(1, datos.cuentaParaRecuperar("crojas@correo.ejemplo", "15834207-3")?.idUsuario)
        assertNull(datos.cuentaParaRecuperar("crojas@correo.ejemplo", "12.476.318-5"))

        val recuperar = RecuperarClaveViewModel()
        recuperar.onCorreoCambia("crojas@correo.ejemplo")
        recuperar.onRutCambia("15.834.207-4")
        assertNull(recuperar.onVerificar())
        assertNotNull(recuperar.uiState.value.errorRut)
        recuperar.onRutCambia("15.834.207-3")
        assertEquals("15.834.207-3", recuperar.onVerificar()?.rut)
        recuperar.onClaveNuevaCambia("73915")
        recuperar.onConfirmacionCambia("73915")
        assertNull(recuperar.onGuardarClave())
        recuperar.onCuentaVerificada()
        assertEquals("73915", recuperar.onGuardarClave()?.claveNueva)
        recuperar.onCorreoCambia("otra@gmail.com")
        assertFalse(recuperar.uiState.value.cuentaVerificada)

        val cambiar = CambiarClaveViewModel()
        cambiar.onClaveActualCambia("12345")
        cambiar.onClaveNuevaCambia("12345")
        cambiar.onConfirmacionCambia("12345")
        assertNull(cambiar.onGuardar())
        cambiar.onClaveNuevaCambia("73915")
        cambiar.onConfirmacionCambia("73915")
        assertEquals(CambioClave("12345", "73915"), cambiar.onGuardar())
    }

    @Test
    fun perfil_editarMisDatos() {
        val camila = usuario(1)!!
        val editar = EditarPerfilViewModel()
        editar.onCargar(camila, datos)
        assertEquals("Maipú", editar.uiState.value.comuna?.nombre)
        editar.onCorreoCambia("lmunoz@correo.ejemplo")
        assertNull(editar.onGuardar(camila, datos))
        assertNotNull(editar.uiState.value.errorCorreo)
        editar.onCorreoCambia("camila.rojas@gmail.com")
        editar.onTelefonoCambia("+56 9 1111 2222")
        val misDatos = editar.onGuardar(camila, datos)
        assertEquals("camila.rojas@gmail.com", misDatos?.correo)
        assertEquals(1, misDatos?.idComuna)

        val admin = usuario(9)!!
        val editarAdmin = EditarPerfilViewModel()
        editarAdmin.onCargar(admin, datos)
        assertNotNull(editarAdmin.onGuardar(admin, datos))
    }
}
