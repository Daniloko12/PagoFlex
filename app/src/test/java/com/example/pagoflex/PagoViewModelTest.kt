package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.MarcaTarjeta
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.codigoConfirmacionDe
import com.example.pagoflex.model.motivoParaNoPagar
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.viewmodel.PagoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class PagoViewModelTest {
    private val datos = BaseDatos()
    private val codigoCamila = codigoConfirmacionDe(datos.usuarioPorId(1)!!.rut)
    private val debitosCamila = datos.tarjetasGuardadas(1, MedioPago.DEBITO)

    @Test
    fun pago_codigoIncorrectoYSinConfirmarElTotal_noSePaga() {
        val viewModel = PagoViewModel()
        viewModel.onMedioPagoSeleccionado(MedioPago.DEBITO, debitosCamila)
        viewModel.onCodigoCambia("1111")
        assertNull(viewModel.onPagar(codigoCamila, datos.motivoParaNoPagar(101), debitosCamila, datos.fechaHoy))
        assertEquals(
            "El código no coincide. Son los 4 últimos números de tu RUT, antes del guion",
            viewModel.pago.value.errorCodigo
        )
        assertEquals("Confirma que revisaste el total a pagar", viewModel.pago.value.errorConfirmacion)
        assertFalse(viewModel.pago.value.pagoExitoso)
    }

    @Test
    fun pago_conTarjetaGuardada_seEligeSolaYSeEntregaLaSolicitud() {
        val viewModel = PagoViewModel()
        viewModel.onMedioPagoSeleccionado(MedioPago.DEBITO, debitosCamila)
        assertEquals(1, viewModel.pago.value.idTarjeta)
        viewModel.onCodigoCambia("42-07")
        assertEquals("4207", viewModel.pago.value.codigo)
        viewModel.onConfirmaTotalCambia(true)

        val solicitud = viewModel.onPagar(codigoCamila, datos.motivoParaNoPagar(101), debitosCamila, datos.fechaHoy)
        assertEquals(MedioPago.DEBITO, solicitud?.medioPago)
        assertEquals(1, solicitud?.idTarjeta)
        assertNull(solicitud?.tarjetaNueva)
        assertTrue(viewModel.pago.value.pagoExitoso)
    }

    @Test
    fun pago_conTarjetaNueva_soloViajanMarcaYUltimos4() {
        val viewModel = PagoViewModel()
        viewModel.onMedioPagoSeleccionado(MedioPago.CREDITO, datos.tarjetasGuardadas(3, MedioPago.CREDITO))
        assertTrue(viewModel.pago.value.usarTarjetaNueva)
        viewModel.onNumeroTarjetaCambia("5555 5555 5555 4444")
        viewModel.onTitularTarjetaCambia("Rosa Contreras")
        viewModel.onVencimientoTarjetaCambia("11/29")
        viewModel.onCvvCambia("123")
        viewModel.onGuardarTarjetaCambia(true)
        val codigoRosa = codigoConfirmacionDe(datos.usuarioPorId(3)!!.rut)
        viewModel.onCodigoCambia(codigoRosa)
        viewModel.onConfirmaTotalCambia(true)

        val solicitud = viewModel.onPagar(codigoRosa, null, emptyList(), datos.fechaHoy)
        assertEquals("4444", solicitud?.tarjetaNueva?.ultimos4)
        assertEquals(MarcaTarjeta.MASTERCARD, solicitud?.tarjetaNueva?.marca)
        assertFalse(solicitud.toString().contains("555555555555"))
        assertTrue(solicitud?.guardarTarjeta == true)
    }

    @Test
    fun agregarTarjeta_vencidaNoSeGuardaYValidaSi() {
        val viewModel = PagoViewModel()
        viewModel.onMedioTarjetaSeleccionado(MedioPago.CREDITO)
        viewModel.onNumeroTarjetaNuevaCambia("5555-5555-5555-4444")
        viewModel.onTitularTarjetaNuevaCambia("Rosa Contreras")
        viewModel.onVencimientoTarjetaNuevaCambia("09/25")
        viewModel.onCvvTarjetaNuevaCambia("123")
        assertNull(viewModel.onAgregarTarjeta(datos.fechaHoy))
        assertEquals("Esta tarjeta está vencida", viewModel.tarjeta.value.errorVencimiento)

        viewModel.onVencimientoTarjetaNuevaCambia("11/29")
        val nueva = viewModel.onAgregarTarjeta(datos.fechaHoy)
        assertEquals(MarcaTarjeta.MASTERCARD, nueva?.marca)
        assertEquals("4444", nueva?.ultimos4)
        assertEquals(YearMonth.of(2029, 11), nueva?.vencimiento)
        assertEquals(MedioPago.CREDITO, nueva?.medioPago)
    }
}
