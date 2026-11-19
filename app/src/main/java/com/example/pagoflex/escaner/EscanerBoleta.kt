package com.example.pagoflex.escaner

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException

object EscanerBoleta {
    private const val CARPETA = "boletas"

    fun nuevaUriParaFoto(context: Context): Uri {
        val carpeta = File(context.cacheDir, CARPETA).apply { mkdirs() }
        val archivo = File(carpeta, "boleta_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.archivos", archivo)
    }

    fun leerTexto(context: Context, uri: Uri, alTerminar: (String?) -> Unit) {
        val imagen = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: IOException) {
            alTerminar(null)
            return
        }
        val lector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        lector.process(imagen)
            .addOnSuccessListener { resultado -> alTerminar(resultado.text) }
            .addOnFailureListener { alTerminar(null) }
            .addOnCompleteListener { lector.close() }
    }
}
