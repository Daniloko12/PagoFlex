package com.example.pagoflex

import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.baseDatosVacia
import com.example.pagoflex.model.compromisosQueCoinciden
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BusquedaCompromisosTest {
    private val datos = BaseDatos()

    @Test
    fun sinTexto_muestraTodosOrdenadosPorVencimiento() {
        val todos = datos.compromisosQueCoinciden("")
        assertEquals(datos.compromisos.size, todos.size)
        assertEquals(todos.sortedWith(compareBy({ it.fechaVencimiento }, { it.idCompromiso })), todos)
    }

    @Test
    fun buscaPorNombreRutConceptoYOrganizacion() {
        assertTrue(datos.compromisosQueCoinciden("CAMILA").all { it.idUsuarioFinal == 1 })
        assertEquals(
            datos.compromisosQueCoinciden("camila").map { it.idCompromiso },
            datos.compromisosQueCoinciden("15.834.207-3").map { it.idCompromiso }
        )
        assertTrue(datos.compromisosQueCoinciden("halcones").isNotEmpty())
        assertTrue(datos.compromisosQueCoinciden("halcones").all { it.idEmpresa == 6 })
        assertTrue(datos.compromisosQueCoinciden("Aporte mensual octubre").any { it.idCompromiso == 103 })
        assertTrue(datos.compromisosQueCoinciden("no existe nada así").isEmpty())
    }

    @Test
    fun baseVacia_noTraeDatosDePrueba() {
        val vacia = baseDatosVacia()
        assertTrue(vacia.usuarios.isEmpty() && vacia.compromisos.isEmpty() && vacia.movimientos.isEmpty())
        assertEquals(DatosAnexo9.FECHA_HOY, vacia.fechaHoy)
        assertTrue(vacia.compromisosQueCoinciden("").isEmpty())
    }
}
