package com.example.pagoflex

import com.example.pagoflex.data.EstadoCarga
import com.example.pagoflex.data.PagoFlexRepositorio
import com.example.pagoflex.data.SesionPreferencias
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PagoFlexViewModelTest {
    @get:Rule
    val reglaCorrutinas = ReglaCorrutinas()

    private val preferencias = PreferenciasEnMemoria()
    private val sesion = SesionPreferencias(preferencias)

    private fun abrirApp() = PagoFlexViewModel(
        repositorio = PagoFlexRepositorio(sesion, cargarDatos = { BaseDatos() }),
        leerImagen = { null }
    )

    @Test
    fun alAbrir_cargaLosDatosYRecuperaLaSesionGuardada() {
        sesion.guardar(1)
        val viewModel = abrirApp()
        assertEquals(EstadoCarga.LISTA, viewModel.cargaInicial.value)
        assertEquals("Camila Andrea", viewModel.usuarioActual.value?.nombres)
        assertEquals(BaseDatos().compromisos.size, viewModel.datos.value.compromisos.size)
    }

    @Test
    fun cambiarMiClave_sinSesionOClaveInsegura_noSeEnvia() = runTest {
        val sinSesion = abrirApp()
        assertEquals("Inicia sesión", sinSesion.cambiarMiClave(CambioClave(CLAVE_DE_PRUEBA, "Nueva#2027")))

        sesion.guardar(1)
        val conSesion = abrirApp()
        assertEquals(
            PagoFlexViewModel.MENSAJE_CLAVE_INSEGURA,
            conSesion.cambiarMiClave(CambioClave(CLAVE_DE_PRUEBA, "12345"))
        )
    }

    @Test
    fun motivoParaNoPagar_compromisoAjenoOSinConexion() = runTest {
        sesion.guardar(1)
        val viewModel = abrirApp()
        assertNull(viewModel.motivoParaNoPagar(101))
        assertEquals("Este compromiso no está a tu nombre", viewModel.motivoParaNoPagar(108))

        viewModel.cambiarConexion(true)
        viewModel.sinConexion.first { it }
        assertEquals(
            "No pudimos confirmar el pago porque no hay conexión. No se hizo ningún cobro y tu compromiso sigue igual.",
            viewModel.motivoParaNoPagar(101)
        )
        assertTrue(sesion.leerSinConexion())
    }

    @Test
    fun administrador_buscaCompromisosYAlCerrarSesionSeBorraTodo() {
        sesion.guardar(9)
        val viewModel = abrirApp()
        viewModel.buscarCompromisos("camila")
        assertTrue(viewModel.compromisosEncontrados.value.isNotEmpty())
        assertTrue(viewModel.compromisosEncontrados.value.all { it.idUsuarioFinal == 1 })

        viewModel.cerrarSesion()
        assertNull(viewModel.usuarioActual.value)
        assertEquals("", viewModel.busqueda.value)
        assertTrue(viewModel.compromisosEncontrados.value.isEmpty())
        assertEquals(-1, sesion.leer())
    }
}
