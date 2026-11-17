package com.example.pagoflex.data.remoto

import com.example.pagoflex.model.BaseDatos
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

suspend fun cargarDatosDelServidor(fechaHoy: LocalDate): BaseDatos = coroutineScope {
    val usuariosApi = ServidorPagoFlex.usuarios
    val compromisosApi = ServidorPagoFlex.compromisos
    val pagosApi = ServidorPagoFlex.pagos

    val usuarios = async { usuariosApi.usuarios() }
    val comunas = async { usuariosApi.comunas() }
    val reconocimientos = async { usuariosApi.reconocimientos() }
    val obtenidos = async { usuariosApi.reconocimientosObtenidos() }
    val empresas = async { compromisosApi.empresas() }
    val rubros = async { compromisosApi.rubros() }
    val compromisos = async { compromisosApi.compromisos() }
    val anulaciones = async { compromisosApi.anulaciones() }
    val reportes = async { compromisosApi.reportes() }
    val recomendaciones = async { compromisosApi.recomendaciones() }
    val contactos = async { compromisosApi.contactos() }
    val historial = async { compromisosApi.historial() }
    val pagos = async { pagosApi.pagos() }
    val tarjetas = async { pagosApi.tarjetas() }
    val movimientos = async { ServidorPagoFlex.movimientos.movimientos() }

    BaseDatos(
        usuarios = usuarios.await(),
        empresas = empresas.await(),
        compromisos = compromisos.await(),
        pagos = pagos.await(),
        anulaciones = anulaciones.await(),
        reportes = reportes.await(),
        recomendaciones = recomendaciones.await(),
        contactosRecomendacion = contactos.await(),
        historialRecomendaciones = historial.await(),
        reconocimientosObtenidos = obtenidos.await(),
        tarjetas = tarjetas.await(),
        rubros = rubros.await(),
        comunas = comunas.await(),
        reconocimientos = reconocimientos.await(),
        movimientos = movimientos.await(),
        fechaHoy = fechaHoy
    )
}
