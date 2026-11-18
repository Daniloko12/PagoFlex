package com.example.pagoflex

import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.LARGO_MAXIMO_CLAVE
import com.example.pagoflex.viewmodel.IngresoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IngresoViewModelTest {

    @Test
    fun camposVacios_muestranErrorEnCorreoYClave() {
        val ingreso = IngresoViewModel()
        assertNull(ingreso.onIngresar())
        assertEquals("Ingresa tu correo", ingreso.uiState.value.errorCorreo)
        assertEquals("Ingresa tu contraseña", ingreso.uiState.value.errorClave)
    }

    @Test
    fun correoSinDominio_noDejaIngresar() {
        val ingreso = IngresoViewModel()
        ingreso.onCorreoCambia("camila@gmail")
        ingreso.onClaveCambia(CLAVE_DE_PRUEBA)
        assertNull(ingreso.onIngresar())
        assertNotNull(ingreso.uiState.value.errorCorreo)
        assertNull(ingreso.uiState.value.errorClave)
    }

    @Test
    fun claveLarga_seCortaYCorreoSinEspacios() {
        val ingreso = IngresoViewModel()
        ingreso.onCorreoCambia("  crojas@correo.ejemplo  ")
        ingreso.onClaveCambia(CLAVE_DE_PRUEBA + "x".repeat(LARGO_MAXIMO_CLAVE))
        assertEquals("crojas@correo.ejemplo", ingreso.uiState.value.correo)
        assertEquals(LARGO_MAXIMO_CLAVE, ingreso.uiState.value.clave.length)
    }

    @Test
    fun cuentaDePrueba_entregaCredencialesYElErrorDelServidorSeBorraAlEscribir() {
        val ingreso = IngresoViewModel()
        ingreso.onUsarCuentaDePrueba("crojas@correo.ejemplo")
        val credenciales = ingreso.onIngresar()
        assertEquals("crojas@correo.ejemplo", credenciales?.correo)
        assertEquals(CLAVE_DE_PRUEBA, credenciales?.clave)

        ingreso.onErrorIngreso("Correo o contraseña incorrectos")
        assertEquals("Correo o contraseña incorrectos", ingreso.uiState.value.errorIngreso)
        ingreso.onClaveCambia("Otra#2026")
        assertNull(ingreso.uiState.value.errorIngreso)
    }
}
