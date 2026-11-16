package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CORREO_SOPORTE
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.correoParaCliente
import com.example.pagoflex.model.correoParaEmpresa
import com.example.pagoflex.model.correoParaSoporte
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.usuarioPorId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MensajesCorreoTest {
    private val datos = BaseDatos()
    private val camila = datos.usuarioPorId(1)!!
    private val compromiso = datos.compromisos.first { it.idUsuarioFinal == 1 }
    private val empresa = datos.empresaPorId(compromiso.idEmpresa)!!

    @Test
    fun clienteEscribeALaOrganizacionQueLeCobra() {
        val correo = correoParaEmpresa(empresa, compromiso, camila)
        assertEquals(empresa.correoAtencion, correo.para)
        assertEquals("Consulta por ${compromiso.concepto} (${compromiso.folio})", correo.asunto)
        assertTrue(correo.mensaje.startsWith("Hola, equipo de ${empresa.nombreFantasia}:"))
        assertTrue(correo.mensaje.contains("- Folio: ${compromiso.folio}"))
        assertTrue(correo.mensaje.endsWith(camila.correo))
    }

    @Test
    fun administradorEscribeAlCliente() {
        val compromiso = datos.compromisoPorId(102)!!
        val persona = datos.usuarioPorId(compromiso.idUsuarioFinal)!!
        val correo = correoParaCliente(persona, compromiso, datos.empresaPorId(compromiso.idEmpresa))
        assertEquals(persona.correo, correo.para)
        assertTrue(correo.asunto.startsWith("PagoFlex: "))
        assertTrue(correo.mensaje.contains("- Estado: Vencido"))
        assertTrue(correo.mensaje.endsWith("Equipo PagoFlex"))
    }

    @Test
    fun soporteConYSinSesion() {
        val conSesion = correoParaSoporte(camila)
        assertEquals(CORREO_SOPORTE, conSesion.para)
        assertEquals("Ayuda con PagoFlex", conSesion.asunto)
        assertTrue(conSesion.mensaje.contains("- Correo de la cuenta: ${camila.correo}"))

        val sinSesion = correoParaSoporte(null, " ")
        assertEquals("No puedo entrar a PagoFlex", sinSesion.asunto)
        assertFalse(sinSesion.mensaje.contains("Mis datos"))
        assertTrue(correoParaSoporte(null, "otra@correo.ejemplo").mensaje.endsWith("- Correo de la cuenta: otra@correo.ejemplo"))
    }
}
