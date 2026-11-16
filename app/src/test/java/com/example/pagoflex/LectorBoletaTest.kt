package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.LecturaBoleta
import com.example.pagoflex.model.empresaEnTexto
import com.example.pagoflex.model.fechasEn
import com.example.pagoflex.model.leerBoleta
import com.example.pagoflex.model.montosEn
import com.example.pagoflex.model.mensajeLectura
import com.example.pagoflex.model.resumenLectura
import com.example.pagoflex.viewmodel.FormularioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LectorBoletaTest {
    private val datos = BaseDatos()
    private val hoy = LocalDate.of(2026, 10, 6)

    @Test
    fun boletaCompleta_leeMontoVencimientoYEmpresa() {
        val texto = """
            COLEGIO BOSQUE NATIVO
            RUT 65.132.874-8
            Boleta electrónica N° 123456
            Fecha emisión: 01-10-2026
            Fecha de vencimiento: 25/10/2026
            Detalle: Mensualidad octubre
            TOTAL A PAGAR $ 185.000
        """.trimIndent()
        val lectura = leerBoleta(texto, datos.empresas, hoy)
        assertEquals(185_000, lectura.monto)
        assertEquals(LocalDate.of(2026, 10, 25), lectura.vencimiento)
        assertEquals(2, lectura.idEmpresa)
        assertEquals("Boleta de octubre 2026", lectura.concepto)
        assertTrue(lectura.encontroAlgo)
        assertEquals("monto \$ 185.000, vence el 25-10-2026, Colegio Bosque Nativo", resumenLectura(lectura, datos.empresas))
    }

    @Test
    fun montoEnLaLineaSiguienteYFechaEnPalabras() {
        val texto = """
            Club Los Halcones
            Cuota social
            Total
            45990
            Paga antes del 15 de noviembre de 2026
        """.trimIndent()
        val lectura = leerBoleta(texto, datos.empresas, hoy)
        assertEquals(45_990, lectura.monto)
        assertEquals(LocalDate.of(2026, 11, 15), lectura.vencimiento)
        assertEquals(6, lectura.idEmpresa)
    }

    @Test
    fun textoSinDatos_noInventaNada() {
        assertEquals(LecturaBoleta(), leerBoleta("", datos.empresas, hoy))
        val lectura = leerBoleta("Gracias por su preferencia", datos.empresas, hoy)
        assertFalse(lectura.encontroAlgo)
        assertNull(lectura.concepto)
    }

    @Test
    fun reglasDeMontosYFechas() {
        assertEquals(listOf(1_250_000), montosEn("Monto: $ 1.250.000"))
        assertEquals(listOf(12_990, 990), montosEn("$12.990 y $990"))
        assertTrue(montosEn("Folio 123456").isEmpty())
        assertEquals(listOf(123_456), montosEn("Total 123.456", conPalabraClave = true))
        assertEquals(listOf(LocalDate.of(2026, 12, 5)), fechasEn("Vence 05.12.26"))
        assertTrue(fechasEn("31-02-2026").isEmpty())
        assertEquals(1, empresaEnTexto("Inmobiliaria Altos del Maipo SpA", datos.empresas)?.idEmpresa)
        assertNull(empresaEnTexto("Supermercado cualquiera", datos.empresas))
    }

    @Test
    fun formulario_seLlenaConLoLeidoYAvisaQueRevisar() {
        val formulario = FormularioViewModel()
        formulario.onConceptoCambia("Mensualidad")
        formulario.onLeyendoBoleta()
        assertTrue(formulario.uiState.value.leyendoBoleta)

        val lectura = LecturaBoleta(monto = 45_990, vencimiento = LocalDate.of(2026, 11, 15), idEmpresa = 6, concepto = "Boleta de noviembre 2026")
        formulario.onBoletaLeida(lectura, byteArrayOf(1, 2, 3), datos)
        val estado = formulario.uiState.value
        assertFalse(estado.leyendoBoleta)
        assertTrue(estado.boletaConDatos)
        assertEquals("Mensualidad", estado.concepto)
        assertEquals("45990", estado.monto)
        assertEquals("15-11-2026", estado.fechaVencimiento)
        assertEquals(6, estado.empresa?.idEmpresa)
        assertNotNull(formulario.imagenBoleta.value)
        assertTrue(estado.mensajeBoleta!!.startsWith("Leímos monto"))

        formulario.onLeyendoBoleta()
        formulario.onBoletaLeida(null, null, datos)
        assertEquals("No pudimos leer la foto. Completa los datos a mano.", formulario.uiState.value.mensajeBoleta)
        assertFalse(formulario.uiState.value.boletaConDatos)
        assertEquals("45990", formulario.uiState.value.monto)
        assertEquals(mensajeLectura(LecturaBoleta(), datos.empresas), "No encontramos el monto ni la fecha en la foto. Prueba con más luz o escríbelos a mano.")
    }
}
