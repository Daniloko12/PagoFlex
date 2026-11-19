package com.example.pagoflex.data.remoto

import com.example.pagoflex.model.SERVIDOR_EMULADOR
import com.example.pagoflex.model.normalizarServidor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.concurrent.TimeUnit

object ServidorPagoFlex {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, AdaptadorTexto { LocalDate.parse(it) })
        .registerTypeAdapter(LocalDateTime::class.java, AdaptadorTexto { LocalDateTime.parse(it) })
        .registerTypeAdapter(YearMonth::class.java, AdaptadorTexto { YearMonth.parse(it) })
        .registerTypeAdapter(ByteArray::class.java, AdaptadorBase64())
        .create()

    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor { cadena ->
            cadena.proceed(cadena.request().newBuilder().header("X-Tunnel-Skip-AntiPhishing-Page", "true").build())
        }
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private class Apis(val url: String) {
        private val retrofit = Retrofit.Builder()
            .baseUrl("$url/")
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val usuarios: UsuariosApi = retrofit.create(UsuariosApi::class.java)
        val compromisos: CompromisosApi = retrofit.create(CompromisosApi::class.java)
        val pagos: PagosApi = retrofit.create(PagosApi::class.java)
        val movimientos: MovimientosApi = retrofit.create(MovimientosApi::class.java)
    }

    @Volatile
    private var apis = Apis(SERVIDOR_EMULADOR)

    val url: String get() = apis.url

    fun configurar(direccion: String) {
        val limpia = normalizarServidor(direccion)
        if (limpia != apis.url) apis = Apis(limpia)
    }

    val usuarios: UsuariosApi get() = apis.usuarios

    val compromisos: CompromisosApi get() = apis.compromisos

    val pagos: PagosApi get() = apis.pagos

    val movimientos: MovimientosApi get() = apis.movimientos
}
