package com.example.pagoflex

import com.example.pagoflex.data.remoto.MensajeError
import com.example.pagoflex.data.remoto.ServidorPagoFlex
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.MarcaTarjeta
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.Usuario
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class JsonServidorTest {
    private val gson = ServidorPagoFlex.gson

    @Test
    fun leeUnUsuarioComoLoEnviaMsUsuarios() {
        val json = """
            {"idUsuario":1,"rol":"USUARIO_FINAL","rut":"15.834.207-3","nombres":"Camila Andrea","apellidos":"Rojas Peña",
             "correo":"crojas@correo.ejemplo","telefono":null,"activo":true,"fechaCreacion":"2023-03-14T00:00:00",
             "idComuna":1,"fechaIncorporacion":"2023-03-14","avisosActivos":true,"diasAnticipacion":3,"avatar":"AQID"}
        """.trimIndent()
        val usuario = gson.fromJson(json, Usuario::class.java)
        assertEquals(Rol.USUARIO_FINAL, usuario.rol)
        assertEquals(LocalDateTime.of(2023, 3, 14, 0, 0), usuario.fechaCreacion)
        assertEquals(LocalDate.of(2023, 3, 14), usuario.fechaIncorporacion)
        assertNull(usuario.telefono)
        assertArrayEquals(byteArrayOf(1, 2, 3), usuario.avatar)
    }

    @Test
    fun leeUnCompromisoConFechasConMilisegundos() {
        val json = """
            {"idCompromiso":103,"folio":"CP-2026-0103","idUsuarioFinal":1,"idEmpresa":5,"concepto":"Aporte mensual octubre",
             "numeroCuota":null,"totalCuotas":null,"monto":10000,"fechaVencimiento":"2026-10-15","estado":"PENDIENTE",
             "recargoAplicado":0,"fechaCambioEstado":"2026-10-01T09:00:00.123456","idAdminRegistro":null,"fechaRegistro":"2026-10-01T09:00:00"}
        """.trimIndent()
        val compromiso = gson.fromJson(json, Compromiso::class.java)
        assertEquals(EstadoCompromiso.PENDIENTE, compromiso.estado)
        assertEquals(LocalDate.of(2026, 10, 15), compromiso.fechaVencimiento)
        assertEquals(9, compromiso.fechaCambioEstado.hour)
    }

    @Test
    fun enviaUnaTarjetaConElFormatoQueEsperaMsPagos() {
        val tarjeta = Tarjeta(
            idTarjeta = 4, idUsuario = 1, medioPago = MedioPago.CREDITO, marca = MarcaTarjeta.VISA, ultimos4 = "1234",
            titular = "CAMILA ROJAS", vencimiento = YearMonth.of(2028, 8), guardada = true,
            fechaRegistro = LocalDateTime.of(2026, 10, 6, 10, 30)
        )
        val json = gson.toJson(tarjeta)
        assertTrue(json.contains("\"vencimiento\":\"2028-08\""))
        assertTrue(json.contains("\"medioPago\":\"CREDITO\""))
        assertTrue(json.contains("\"fechaRegistro\":\"2026-10-06T10:30\""))
        assertEquals(tarjeta, gson.fromJson(json, Tarjeta::class.java))
    }

    @Test
    fun leeElMensajeDeErrorDelServidor() {
        val error = gson.fromJson("{\"error\":\"Ese correo ya está registrado\"}", MensajeError::class.java)
        assertEquals("Ese correo ya está registrado", error.error)
    }
}
