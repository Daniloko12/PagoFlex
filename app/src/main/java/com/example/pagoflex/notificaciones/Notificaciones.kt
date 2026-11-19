package com.example.pagoflex.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pagoflex.MainActivity
import com.example.pagoflex.R
import com.example.pagoflex.model.AvisoProgramado
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.MINUTOS_VIGENCIA_CODIGO
import com.example.pagoflex.permisos.Permisos

object Notificaciones {
    const val CANAL_VENCIMIENTOS = "vencimientos"
    const val CANAL_SEGURIDAD = "seguridad"
    const val ID_CODIGO = 9000
    const val ID_SIN_AVISOS = 9001

    fun crearCanales(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val vencimientos = NotificationChannel(
            CANAL_VENCIMIENTOS,
            context.getString(R.string.canal_vencimientos),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.canal_vencimientos_descripcion) }
        val seguridad = NotificationChannel(
            CANAL_SEGURIDAD,
            context.getString(R.string.canal_seguridad),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = context.getString(R.string.canal_seguridad_descripcion) }
        manager.createNotificationChannel(vencimientos)
        manager.createNotificationChannel(seguridad)
    }

    fun puedeNotificar(context: Context): Boolean =
        Permisos.tieneNotificaciones(context) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    private fun abrirApp(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun mostrar(context: Context, id: Int, canal: String, titulo: String, texto: String, duracionMs: Long? = null): Boolean {
        if (!puedeNotificar(context)) return false
        crearCanales(context)
        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_campana)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(if (canal == CANAL_SEGURIDAD) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(abrirApp(context))
            .setAutoCancel(true)
            .apply { if (duracionMs != null) setTimeoutAfter(duracionMs) }
            .build()
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(id, notificacion)
                true
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        }
    }

    fun mostrarRecordatorio(context: Context, aviso: AvisoProgramado): Boolean {
        val titulo = if (aviso.compromiso.estado == EstadoCompromiso.VENCIDO) "Tienes un pago pendiente"
        else "Recordatorio de vencimiento"
        return mostrar(context, aviso.compromiso.idCompromiso, CANAL_VENCIMIENTOS, titulo, aviso.mensaje)
    }

    fun mostrarSinAvisos(context: Context): Boolean =
        mostrar(context, ID_SIN_AVISOS, CANAL_VENCIMIENTOS, "Estás al día", "No tienes vencimientos por avisar. ¡Sigue así!")

    fun mostrarCodigo(context: Context, codigo: String): Boolean =
        mostrar(
            context, ID_CODIGO, CANAL_SEGURIDAD, "Código de PagoFlex",
            "Tu código para recuperar la contraseña es $codigo. Vence en $MINUTOS_VIGENCIA_CODIGO minutos. No lo compartas con nadie.",
            duracionMs = MINUTOS_VIGENCIA_CODIGO * 60_000
        )
}
