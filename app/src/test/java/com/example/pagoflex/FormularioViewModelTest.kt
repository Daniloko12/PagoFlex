package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.LecturaBoleta
import com.example.pagoflex.viewmodel.FormularioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FormularioViewModelTest {
    private val datos = BaseDatos()

    private fun compromisoDeLuis() = FormularioViewModel().apply {
        onRutPersonaCambia("12.476.318-5", datos)
        onConceptoCambia("Cuota pie departamento")
        onMontoCambia("310000")
        onFechaVencimientoCambia("30-11-2026")
        onNumeroCuotaCambia("14")
        onTotalCuotasCambia("36")
    }

    @Test
    fun sinOrganizacion_noSeRegistra() {
        val formulario = compromisoDeLuis()
        assertEquals("Luis Alberto Muñoz Tapia", formulario.uiState.value.personaEncontrada)
        assertNull(formulario.onRegistrar(datos))
        assertNotNull(formulario.uiState.value.errorEmpresa)
    }

    @Test
    fun cuotaRepetida_avisaDuplicadoYConOtraCuotaSeRegistra() {
        val formulario = compromisoDeLuis()
        formulario.onEmpresaSeleccionada(datos.empresas.first())
        assertNull(formulario.onRegistrar(datos))
        assertNotNull(formulario.uiState.value.errorDuplicado)
        formulario.onNumeroCuotaCambia("16")
        assertEquals(1, formulario.onRegistrar(datos)?.idEmpresa)
    }

    @Test
    fun personaNueva_pideSusDatosAntesDeRegistrar() {
        val formulario = FormularioViewModel()
        formulario.onEmpresaSeleccionada(datos.empresas.first())
        formulario.onRutPersonaCambia("11.111.111-1", datos)
        assertTrue(formulario.uiState.value.personaNoExiste)
        formulario.onConceptoCambia("Gasto común noviembre")
        formulario.onMontoCambia("45000")
        formulario.onFechaVencimientoCambia("10-11-2026")
        assertNull(formulario.onRegistrar(datos))
        formulario.onNombresPersonaCambia("Elena")
        formulario.onApellidosPersonaCambia("Rivas Soto")
        formulario.onCorreoPersonaCambia("erivas@gmail.com")
        formulario.onTelefonoPersonaCambia("+56 9 1234 5678")
        assertEquals("Elena", formulario.onRegistrar(datos)?.personaNueva?.nombres)
    }

    @Test
    fun boletaLeida_llenaElFormularioSinBorrarElConcepto() {
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

        formulario.onLeyendoBoleta()
        formulario.onBoletaLeida(null, null, datos)
        assertEquals("No pudimos leer la foto. Completa los datos a mano.", formulario.uiState.value.mensajeBoleta)
        assertEquals("45990", formulario.uiState.value.monto)
    }
}
