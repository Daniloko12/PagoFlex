package com.example.pagoflex

import com.example.pagoflex.model.SERVIDOR_EMULADOR
import com.example.pagoflex.model.errorDireccionServidor
import com.example.pagoflex.model.normalizarServidor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DireccionServidorTest {

    @Test
    fun ipDeOtroPc_esValida() {
        assertNull(errorDireccionServidor("192.168.1.20"))
        assertNull(errorDireccionServidor(SERVIDOR_EMULADOR))
        assertNull(errorDireccionServidor("localhost"))
    }

    @Test
    fun seLimpiaLoQueSobra() {
        assertEquals("192.168.1.20", normalizarServidor("  http://192.168.1.20:8081/api/usuarios "))
        assertEquals("10.0.2.2", normalizarServidor("10.0.2.2"))
    }

    @Test
    fun ipMalEscrita_muestraError() {
        assertNotNull(errorDireccionServidor(""))
        assertNotNull(errorDireccionServidor("192.168.1"))
        assertNotNull(errorDireccionServidor("192.168.1.300"))
        assertNotNull(errorDireccionServidor("mi pc"))
    }
}
