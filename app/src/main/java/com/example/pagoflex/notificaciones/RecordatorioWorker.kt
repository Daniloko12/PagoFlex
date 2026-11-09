package com.example.pagoflex.notificaciones

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.pagoflex.data.PagoFlexDbHelper
import com.example.pagoflex.data.SesionPreferencias
import com.example.pagoflex.model.proximoRecordatorio
import com.example.pagoflex.model.recordatoriosDeHoy

class RecordatorioWorker(context: Context, parametros: WorkerParameters) : Worker(context, parametros) {
    companion object {
        const val DATO_PRUEBA = "prueba"
    }

    override fun doWork(): Result {
        val esPrueba = inputData.getBoolean(DATO_PRUEBA, false)
        val db = PagoFlexDbHelper(applicationContext)
        try {
            val usuario = db.buscarUsuarioPorId(SesionPreferencias(applicationContext).leer()) ?: return Result.success()
            val datos = db.cargarDatos()
            if (esPrueba) {
                val proximo = datos.proximoRecordatorio(usuario)
                if (proximo != null) Notificaciones.mostrarRecordatorio(applicationContext, proximo)
                else Notificaciones.mostrarSinAvisos(applicationContext)
            } else {
                datos.recordatoriosDeHoy(usuario).forEach { Notificaciones.mostrarRecordatorio(applicationContext, it) }
            }
            return Result.success()
        } finally {
            db.close()
        }
    }
}
