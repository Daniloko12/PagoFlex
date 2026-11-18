package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.LecturaBoleta
import com.example.pagoflex.model.MotivoReporte
import com.example.pagoflex.model.SolicitudReporte
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.motivoParaNoReportar
import com.example.pagoflex.viewmodel.CompromisoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CompromisoViewModelTest {
    private val datos = BaseDatos()

    @Test
    fun registrar_sinOrganizacionOCuotaRepetida_noSeRegistra() {
        val viewModel = CompromisoViewModel()
        viewModel.onRutPersonaCambia("12.476.318-5", datos)
        viewModel.onConceptoCambia("Cuota pie departamento")
        viewModel.onMontoCambia("310000")
        viewModel.onFechaVencimientoCambia("30-11-2026")
        viewModel.onNumeroCuotaCambia("14")
        viewModel.onTotalCuotasCambia("36")
        assertEquals("Luis Alberto Muñoz Tapia", viewModel.formulario.value.personaEncontrada)
        assertNull(viewModel.onRegistrar(datos))
        assertEquals("Elige la empresa que cobra", viewModel.formulario.value.errorEmpresa)

        viewModel.onEmpresaSeleccionada(datos.empresas.first())
        assertNull(viewModel.onRegistrar(datos))
        assertNotNull(viewModel.formulario.value.errorDuplicado)

        viewModel.onNumeroCuotaCambia("16")
        assertEquals(16, viewModel.onRegistrar(datos)?.numeroCuota)
    }

    @Test
    fun boletaLeida_llenaLosDatosSinBorrarElConcepto() {
        val viewModel = CompromisoViewModel()
        viewModel.onConceptoCambia("Mensualidad")
        viewModel.onLeyendoBoleta()
        assertTrue(viewModel.formulario.value.leyendoBoleta)

        val lectura = LecturaBoleta(monto = 45_990, vencimiento = LocalDate.of(2026, 11, 15), idEmpresa = 6, concepto = "Boleta de noviembre 2026")
        viewModel.onBoletaLeida(lectura, byteArrayOf(1, 2, 3), datos)
        val estado = viewModel.formulario.value
        assertFalse(estado.leyendoBoleta)
        assertTrue(estado.boletaConDatos)
        assertEquals("Mensualidad", estado.concepto)
        assertEquals("45990", estado.monto)
        assertEquals("15-11-2026", estado.fechaVencimiento)
        assertEquals(6, estado.empresa?.idEmpresa)
        assertNotNull(viewModel.imagenBoleta.value)

        viewModel.onLeyendoBoleta()
        viewModel.onBoletaLeida(null, null, datos)
        assertEquals("No pudimos leer la foto. Completa los datos a mano.", viewModel.formulario.value.mensajeBoleta)
        assertEquals("45990", viewModel.formulario.value.monto)
    }

    @Test
    fun editar_fechaPasadaOCuotaRepetida_noSeGuarda() {
        val vencido = datos.compromisoPorId(106)!!
        val viewModel = CompromisoViewModel()
        viewModel.onCargarEdicion(vencido)
        assertEquals("30-09-2026", viewModel.edicion.value.fechaVencimiento)
        viewModel.onMontoEdicionCambia("300000")
        assertEquals(300_000, viewModel.onGuardarEdicion(vencido, datos)?.monto)

        viewModel.onFechaEdicionCambia("01-10-2026")
        assertNull(viewModel.onGuardarEdicion(vencido, datos))
        assertNotNull(viewModel.edicion.value.errorFechaVencimiento)

        viewModel.onFechaEdicionCambia("30-10-2026")
        viewModel.onNumeroCuotaEdicionCambia("15")
        assertNull(viewModel.onGuardarEdicion(vencido, datos))
        assertNotNull(viewModel.edicion.value.errorDuplicado)
    }

    @Test
    fun reporte_motivoOtroSinComentario_noSeEnvia() {
        val viewModel = CompromisoViewModel()
        assertNull(viewModel.onEnviarReporte(null))
        assertEquals("Elige el motivo del problema", viewModel.reporte.value.errorMotivo)

        viewModel.onMotivoReporteSeleccionado(MotivoReporte.OTRO)
        assertNull(viewModel.onEnviarReporte(datos.motivoParaNoReportar(106)))
        assertNotNull(viewModel.reporte.value.errorComentario)

        viewModel.onComentarioReporteCambia("Me cobraron dos veces")
        assertEquals(
            SolicitudReporte(MotivoReporte.OTRO, "Me cobraron dos veces"),
            viewModel.onEnviarReporte(datos.motivoParaNoReportar(106))
        )
        assertTrue(viewModel.reporte.value.reporteEnviado)
    }
}
