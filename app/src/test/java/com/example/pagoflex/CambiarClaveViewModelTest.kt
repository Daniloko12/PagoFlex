package com.example.pagoflex

import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.CambiarClaveUiState
import com.example.pagoflex.viewmodel.CambiarClaveViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CambiarClaveViewModelTest {

    @Test
    fun camposVacios_pidenLasTresContrasenas() {
        val cambio = CambiarClaveViewModel()
        assertNull(cambio.onGuardar())
        val estado = cambio.uiState.value
        assertEquals("Ingresa tu contraseña actual", estado.errorClaveActual)
        assertEquals("Crea tu contraseña", estado.errorClaveNueva)
        assertEquals("Repite tu contraseña", estado.errorConfirmacion)
    }

    @Test
    fun nuevaIgualALaActual_noSeAcepta() {
        val cambio = CambiarClaveViewModel()
        cambio.onClaveActualCambia(CLAVE_DE_PRUEBA)
        cambio.onClaveNuevaCambia(CLAVE_DE_PRUEBA)
        cambio.onConfirmacionCambia(CLAVE_DE_PRUEBA)
        assertNull(cambio.onGuardar())
        assertEquals("La nueva contraseña debe ser distinta a la actual", cambio.uiState.value.errorClaveNueva)
    }

    @Test
    fun claveSinRequisitos_muestraLoQueFalta() {
        val cambio = CambiarClaveViewModel()
        cambio.onClaveActualCambia(CLAVE_DE_PRUEBA)
        cambio.onClaveNuevaCambia("pago 2027")
        cambio.onConfirmacionCambia("pago 2027")
        assertNull(cambio.onGuardar())
        assertEquals("La contraseña no puede tener espacios", cambio.uiState.value.errorClaveNueva)
    }

    @Test
    fun cambioValido_seEntregaYLimpiarDejaElFormularioVacio() {
        val cambio = CambiarClaveViewModel()
        cambio.onClaveActualCambia(CLAVE_DE_PRUEBA)
        cambio.onClaveNuevaCambia("Nueva#2027")
        cambio.onConfirmacionCambia("Nueva#2027")
        val resultado = cambio.onGuardar()
        assertEquals(CLAVE_DE_PRUEBA, resultado?.claveActual)
        assertEquals("Nueva#2027", resultado?.claveNueva)

        cambio.onErrorCambio("La contraseña actual no es correcta")
        assertNotNull(cambio.uiState.value.errorCambio)
        cambio.onLimpiar()
        assertEquals(CambiarClaveUiState(), cambio.uiState.value)
    }
}
