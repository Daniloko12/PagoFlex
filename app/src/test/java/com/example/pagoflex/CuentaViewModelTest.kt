package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.CambiarClaveUiState
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.model.Credenciales
import com.example.pagoflex.viewmodel.CuentaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CuentaViewModelTest {
    private val datos = BaseDatos()

    @Test
    fun ingreso_camposVaciosOCorreoMalEscrito_noDejanEntrar() {
        val viewModel = CuentaViewModel()
        assertNull(viewModel.onIngresar())
        assertEquals("Ingresa tu correo", viewModel.ingreso.value.errorCorreo)
        assertEquals("Ingresa tu contraseña", viewModel.ingreso.value.errorClave)

        viewModel.onCorreoIngresoCambia("  crojas@correo  ")
        viewModel.onClaveIngresoCambia(CLAVE_DE_PRUEBA)
        assertNull(viewModel.onIngresar())
        assertEquals("Ingresa un correo válido, por ejemplo nombre@gmail.com", viewModel.ingreso.value.errorCorreo)

        viewModel.onCorreoIngresoCambia("crojas@correo.ejemplo")
        assertEquals(Credenciales("crojas@correo.ejemplo", CLAVE_DE_PRUEBA), viewModel.onIngresar())
    }

    @Test
    fun registro_rutRepetidoYSinTerminos_noCreaLaCuentaHastaCorregir() {
        val viewModel = CuentaViewModel()
        viewModel.onRutRegistroCambia("15.834.207-3")
        viewModel.onNombresRegistroCambia("Elena")
        viewModel.onApellidosRegistroCambia("Rivas Soto")
        viewModel.onCorreoRegistroCambia("erivas@gmail.com")
        viewModel.onTelefonoRegistroCambia("+56 9 1234 5678")
        viewModel.onClaveRegistroCambia("Nueva#2027")
        viewModel.onConfirmacionRegistroCambia("Nueva#2027")
        assertNull(viewModel.onCrearCuenta(datos))
        assertNotNull(viewModel.registro.value.errorRut)
        assertEquals("Debes aceptar los términos para crear tu cuenta", viewModel.registro.value.errorTerminos)

        viewModel.onRutRegistroCambia("11.111.111-1")
        viewModel.onAceptaTerminosCambia(true)
        val nueva = viewModel.onCrearCuenta(datos)
        assertEquals("erivas@gmail.com", nueva?.correo)
        assertEquals("Nueva#2027", nueva?.clave)
    }

    @Test
    fun cambiarClave_vaciaIgualALaActualOConEspacios_seRechaza() {
        val viewModel = CuentaViewModel()
        assertNull(viewModel.onGuardarCambioClave())
        assertEquals("Ingresa tu contraseña actual", viewModel.cambioClave.value.errorClaveActual)

        viewModel.onClaveActualCambia(CLAVE_DE_PRUEBA)
        viewModel.onClaveNuevaCambia(CLAVE_DE_PRUEBA)
        viewModel.onConfirmacionClaveNuevaCambia(CLAVE_DE_PRUEBA)
        assertNull(viewModel.onGuardarCambioClave())
        assertEquals("La nueva contraseña debe ser distinta a la actual", viewModel.cambioClave.value.errorClaveNueva)

        viewModel.onClaveNuevaCambia("Pago #2027")
        assertNull(viewModel.onGuardarCambioClave())
        assertEquals("La contraseña no puede tener espacios", viewModel.cambioClave.value.errorClaveNueva)
    }

    @Test
    fun cambiarClave_valida_seEntregaYAlLimpiarQuedaVacia() {
        val viewModel = CuentaViewModel()
        viewModel.onClaveActualCambia(CLAVE_DE_PRUEBA)
        viewModel.onClaveNuevaCambia("Nueva#2027")
        viewModel.onConfirmacionClaveNuevaCambia("Nueva#2028")
        assertNull(viewModel.onGuardarCambioClave())
        assertEquals("Las contraseñas no coinciden", viewModel.cambioClave.value.errorConfirmacion)

        viewModel.onConfirmacionClaveNuevaCambia("Nueva#2027")
        assertEquals(CambioClave(CLAVE_DE_PRUEBA, "Nueva#2027"), viewModel.onGuardarCambioClave())

        viewModel.onErrorCambioClave("La contraseña actual no es correcta")
        assertEquals("La contraseña actual no es correcta", viewModel.cambioClave.value.errorCambio)
        viewModel.onLimpiarCambioClave()
        assertEquals(CambiarClaveUiState(), viewModel.cambioClave.value)
    }
}
