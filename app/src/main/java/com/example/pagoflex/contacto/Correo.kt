package com.example.pagoflex.contacto

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.pagoflex.model.CorreoArmado

object Correo {
    fun escribir(context: Context, correo: CorreoArmado): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${correo.para}"))
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(correo.para))
            .putExtra(Intent.EXTRA_SUBJECT, correo.asunto)
            .putExtra(Intent.EXTRA_TEXT, correo.mensaje)
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
