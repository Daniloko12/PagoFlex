package com.example.pagoflex.notificaciones

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ProgramadorRecordatorios {
    private const val TRABAJO_DIARIO = "recordatorios_vencimiento"
    private const val TRABAJO_PRUEBA = "recordatorio_prueba"
    val HORA_AVISO: LocalTime = LocalTime.of(9, 0)

    fun minutosHastaElAviso(ahora: LocalDateTime): Long {
        var siguiente = ahora.toLocalDate().atTime(HORA_AVISO)
        if (!siguiente.isAfter(ahora)) siguiente = siguiente.plusDays(1)
        return Duration.between(ahora, siguiente).toMinutes()
    }

    fun programar(context: Context) {
        val pedido = PeriodicWorkRequestBuilder<RecordatorioWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(minutosHastaElAviso(LocalDateTime.now()), TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(TRABAJO_DIARIO, ExistingPeriodicWorkPolicy.KEEP, pedido)
    }

    fun cancelar(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(TRABAJO_DIARIO)
    }

    fun probarAhora(context: Context) {
        val pedido = OneTimeWorkRequestBuilder<RecordatorioWorker>()
            .setInputData(workDataOf(RecordatorioWorker.DATO_PRUEBA to true))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(TRABAJO_PRUEBA, ExistingWorkPolicy.REPLACE, pedido)
    }
}
