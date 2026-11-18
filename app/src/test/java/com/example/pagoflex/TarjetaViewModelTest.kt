package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.MarcaTarjeta
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.viewmodel.TarjetaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class TarjetaViewModelTest {
    private val hoy = BaseDatos().fechaHoy

    @Test
    fun numeroQueNoPasaLuhn_marcaError() {
        val tarjeta = TarjetaViewModel()
        tarjeta.onNumeroCambia("4111 1111 1111 1112")
        tarjeta.onTitularCambia("Camila Rojas")
        tarjeta.onVencimientoCambia("08/28")
        tarjeta.onCvvCambia("123")
        assertNull(tarjeta.onAgregar(hoy))
        assertEquals("Revisa el número: no corresponde a una tarjeta válida", tarjeta.uiState.value.errorNumero)
    }

    @Test
    fun tarjetaVencida_noSeGuarda() {
        val tarjeta = TarjetaViewModel()
        tarjeta.onNumeroCambia("4111111111111111")
        tarjeta.onTitularCambia("Camila Rojas")
        tarjeta.onVencimientoCambia("0925")
        tarjeta.onCvvCambia("123")
        assertNull(tarjeta.onAgregar(hoy))
        assertEquals("Esta tarjeta está vencida", tarjeta.uiState.value.errorVencimiento)
    }

    @Test
    fun tarjetaValida_soloGuardaMarcaYUltimos4() {
        val tarjeta = TarjetaViewModel()
        tarjeta.onMedioPagoSeleccionado(MedioPago.CREDITO)
        tarjeta.onNumeroCambia("5555-5555-5555-4444")
        tarjeta.onTitularCambia("Rosa Contreras")
        tarjeta.onVencimientoCambia("11/29")
        tarjeta.onCvvCambia("123")
        val nueva = tarjeta.onAgregar(hoy)
        assertEquals(MarcaTarjeta.MASTERCARD, nueva?.marca)
        assertEquals("4444", nueva?.ultimos4)
        assertEquals(YearMonth.of(2029, 11), nueva?.vencimiento)
        assertEquals(MedioPago.CREDITO, nueva?.medioPago)
    }

    @Test
    fun tarjetaRepetida_muestraAvisoYSeBorraAlCambiarElNumero() {
        val tarjeta = TarjetaViewModel()
        tarjeta.onTarjetaRepetida()
        assertNotNull(tarjeta.uiState.value.errorTarjeta)
        tarjeta.onNumeroCambia("4")
        assertNull(tarjeta.uiState.value.errorTarjeta)
    }
}
