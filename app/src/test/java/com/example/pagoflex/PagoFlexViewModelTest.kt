package com.example.pagoflex

import android.content.SharedPreferences
import com.example.pagoflex.data.EstadoCarga
import com.example.pagoflex.data.PagoFlexRepositorio
import com.example.pagoflex.data.SesionPreferencias
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.viewmodel.PagoFlexViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

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

@OptIn(ExperimentalCoroutinesApi::class)
class ReglaCorrutinas(private val despachador: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(despachador)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class PreferenciasEnMemoria : SharedPreferences {
    private val valores = mutableMapOf<String, Any>()

    private fun valor(key: String?): Any? = key?.let { valores[it] }

    override fun getAll(): MutableMap<String, *> = valores.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? = valor(key) as? String ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        valor(key) as? MutableSet<String> ?: defValues

    override fun getInt(key: String?, defValue: Int): Int = valor(key) as? Int ?: defValue

    override fun getLong(key: String?, defValue: Long): Long = valor(key) as? Long ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float = valor(key) as? Float ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = valor(key) as? Boolean ?: defValue

    override fun contains(key: String?): Boolean = valor(key) != null

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private inner class Editor : SharedPreferences.Editor {
        private val cambios = mutableMapOf<String, Any?>()
        private var borrarTodo = false

        private fun poner(key: String?, valor: Any?): SharedPreferences.Editor {
            if (key != null) cambios[key] = valor
            return this
        }

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = poner(key, value)

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = poner(key, values)

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = poner(key, value)

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = poner(key, value)

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = poner(key, value)

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = poner(key, value)

        override fun remove(key: String?): SharedPreferences.Editor = poner(key, null)

        override fun clear(): SharedPreferences.Editor {
            borrarTodo = true
            return this
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (borrarTodo) valores.clear()
            cambios.forEach { (clave, valor) -> if (valor == null) valores.remove(clave) else valores[clave] = valor }
        }
    }
}
