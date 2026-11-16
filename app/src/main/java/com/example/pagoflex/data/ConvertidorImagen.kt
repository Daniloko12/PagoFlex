package com.example.pagoflex.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

object ConvertidorImagen {
    private const val LADO_MAXIMO = 400

    const val LADO_BOLETA = 1280

    private const val CALIDAD_JPEG = 80

    fun desdeUri(context: Context, uri: Uri, ladoMaximo: Int? = null): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val origen = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(origen) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val ladoMayor = maxOf(info.size.width, info.size.height)
                if (ladoMaximo != null && ladoMayor > ladoMaximo) decoder.setTargetSampleSize(ladoMayor / ladoMaximo)
            }
        } else {
            context.contentResolver.openInputStream(uri).use { flujo ->
                BitmapFactory.decodeStream(flujo)
            }
        }
    } catch (e: Exception) {
        null
    }

    fun aBytes(bitmap: Bitmap, ladoMaximo: Int = LADO_MAXIMO): ByteArray {
        val chico = achicar(bitmap, ladoMaximo)
        val salida = ByteArrayOutputStream()
        chico.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        return salida.toByteArray()
    }

    fun aBitmap(bytes: ByteArray): Bitmap? =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    private fun achicar(bitmap: Bitmap, ladoMaximo: Int): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= ladoMaximo) return bitmap
        val escala = ladoMaximo.toFloat() / ladoMayor
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt(),
            (bitmap.height * escala).toInt(),
            true
        )
    }
}
