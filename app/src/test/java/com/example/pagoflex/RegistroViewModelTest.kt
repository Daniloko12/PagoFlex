package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.viewmodel.RegistroViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RegistroViewModelTest {
    private val datos = BaseDatos()

    private fun formularioCompleto() = RegistroViewModel().apply {
        onRutCambia("11.111.111-1")
        onNombresCambia("Elena")
        onApellidosCambia("Rivas Soto")
        onCorreoCambia("erivas@gmail.com")
        onTelefonoCambia("+56 9 1234 5678")
        onClaveCambia("Nueva#2027")
        onConfirmacionCambia("Nueva#2027")
        onAceptaTerminosCambia(true)
    }

    @Test
    fun rutYCorreoYaRegistrados_marcanError() {
        val registro = formularioCompleto()
        registro.onRutCambia("15.834.207-3")
        registro.onCorreoCambia("crojas@correo.ejemplo")
        assertNull(registro.onCrearCuenta(datos))
        assertNotNull(registro.uiState.value.errorRut)
        assertNotNull(registro.uiState.value.errorCorreo)
    }

    @Test
    fun claveDebilYSinAceptarTerminos_noCreaLaCuenta() {
        val registro = formularioCompleto()
        registro.onClaveCambia("12345")
        registro.onConfirmacionCambia("12345")
        registro.onAceptaTerminosCambia(false)
        assertNull(registro.onCrearCuenta(datos))
        assertNotNull(registro.uiState.value.errorClave)
        assertEquals("Debes aceptar los términos para crear tu cuenta", registro.uiState.value.errorTerminos)
    }

    @Test
    fun confirmacionDistinta_avisaQueNoCoinciden() {
        val registro = formularioCompleto()
        registro.onConfirmacionCambia("Nueva#2028")
        assertNull(registro.onCrearCuenta(datos))
        assertEquals("Las contraseñas no coinciden", registro.uiState.value.errorConfirmacion)
    }

    @Test
    fun datosCorrectos_creanLaCuentaSinEspaciosDeMas() {
        val registro = formularioCompleto()
        registro.onNombresCambia("  Elena ")
        val cuenta = registro.onCrearCuenta(datos)
        assertEquals("Elena", cuenta?.nombres)
        assertEquals("erivas@gmail.com", cuenta?.correo)
        assertEquals("Nueva#2027", cuenta?.clave)
        assertNull(registro.uiState.value.errorRegistro)
    }
}
