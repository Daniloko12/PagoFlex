package com.example.pagoflex.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pagoflex.data.SesionPreferencias
import com.example.pagoflex.data.remoto.ServidorPagoFlex
import com.example.pagoflex.data.remoto.cargarDatosDelServidor
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.proximoRecordatorio
import com.example.pagoflex.model.recordatoriosDeHoy
import com.example.pagoflex.model.usuarioPorId
import kotlinx.coroutines.CancellationException

class RecordatorioWorker(context: Context, parametros: WorkerParameters) : CoroutineWorker(context, parametros) {
    companion object {
        const val DATO_PRUEBA = "prueba"
    }

    override suspend fun doWork(): Result {
        val esPrueba = inputData.getBoolean(DATO_PRUEBA, false)
        val sesion = SesionPreferencias(applicationContext)
        ServidorPagoFlex.configurar(sesion.leerServidor())
        val datos = try {
            cargarDatosDelServidor(DatosAnexo9.FECHA_HOY)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return if (esPrueba) Result.failure() else Result.retry()
        }
        val usuario = datos.usuarioPorId(sesion.leer()) ?: return Result.success()
        if (esPrueba) {
            val proximo = datos.proximoRecordatorio(usuario)
            if (proximo != null) Notificaciones.mostrarRecordatorio(applicationContext, proximo)
            else Notificaciones.mostrarSinAvisos(applicationContext)
        } else {
            datos.recordatoriosDeHoy(usuario).forEach { Notificaciones.mostrarRecordatorio(applicationContext, it) }
        }
        return Result.success()
    }
}
