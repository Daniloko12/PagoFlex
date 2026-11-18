package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.codigoConfirmacionDe
import com.example.pagoflex.model.motivoParaNoPagar
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.viewmodel.PagoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PagoViewModelTest {
    private val datos = BaseDatos()
    private val codigoCamila = codigoConfirmacionDe(datos.usuarioPorId(1)!!.rut)

    @Test
    fun conDebito_seEligeSolaLaTarjetaGuardada() {
        val pago = PagoViewModel()
        pago.onMedioPagoSeleccionado(MedioPago.DEBITO, datos.tarjetasGuardadas(1, MedioPago.DEBITO))
        assertEquals(1, pago.uiState.value.idTarjeta)
        assertEquals("4207", codigoCamila)
    }

    @Test
    fun codigoIncorrectoOSinConfirmarTotal_noPaga() {
        val pago = PagoViewModel()
        val debitos = datos.tarjetasGuardadas(1, MedioPago.DEBITO)
        pago.onMedioPagoSeleccionado(MedioPago.DEBITO, debitos)
        pago.onCodigoCambia("1111")
        assertNull(pago.onPagar(codigoCamila, datos.motivoParaNoPagar(101), debitos, datos.fechaHoy))
        assertNotNull(pago.uiState.value.errorCodigo)
        assertEquals("Confirma que revisaste el total a pagar", pago.uiState.value.errorConfirmacion)
    }

    @Test
    fun pagoConTarjetaGuardada_entregaLaSolicitud() {
        val pago = PagoViewModel()
        val debitos = datos.tarjetasGuardadas(1, MedioPago.DEBITO)
        pago.onMedioPagoSeleccionado(MedioPago.DEBITO, debitos)
        pago.onCodigoCambia(codigoCamila)
        pago.onConfirmaTotalCambia(true)
        val solicitud = pago.onPagar(codigoCamila, datos.motivoParaNoPagar(101), debitos, datos.fechaHoy)
        assertEquals(MedioPago.DEBITO, solicitud?.medioPago)
        assertEquals(1, solicitud?.idTarjeta)
        assertNull(solicitud?.tarjetaNueva)
    }

    @Test
    fun tarjetaNueva_soloViajanMarcaYUltimos4() {
        val pago = PagoViewModel()
        pago.onMedioPagoSeleccionado(MedioPago.CREDITO, datos.tarjetasGuardadas(3, MedioPago.CREDITO))
        assertTrue(pago.uiState.value.usarTarjetaNueva)
        pago.onNumeroTarjetaCambia("5555 5555 5555 4444")
        pago.onTitularTarjetaCambia("Rosa Contreras")
        pago.onVencimientoTarjetaCambia("11/29")
        pago.onCvvCambia("123")
        val codigoRosa = codigoConfirmacionDe(datos.usuarioPorId(3)!!.rut)
        pago.onCodigoCambia(codigoRosa)
        pago.onConfirmaTotalCambia(true)
        val solicitud = pago.onPagar(codigoRosa, null, emptyList(), datos.fechaHoy)
        assertEquals("4444", solicitud?.tarjetaNueva?.ultimos4)
        assertTrue(solicitud?.guardarTarjeta == true)
    }
}
