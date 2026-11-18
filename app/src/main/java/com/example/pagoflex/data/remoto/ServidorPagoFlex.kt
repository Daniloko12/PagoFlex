package com.example.pagoflex.data.remoto

import com.example.pagoflex.model.SERVIDOR_EMULADOR
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
    const val PUERTO_USUARIOS = 8081
    const val PUERTO_COMPROMISOS = 8082
    const val PUERTO_PAGOS = 8083
    const val PUERTO_MOVIMIENTOS = 8084

    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, AdaptadorTexto { LocalDate.parse(it) })
        .registerTypeAdapter(LocalDateTime::class.java, AdaptadorTexto { LocalDateTime.parse(it) })
        .registerTypeAdapter(YearMonth::class.java, AdaptadorTexto { YearMonth.parse(it) })
        .registerTypeAdapter(ByteArray::class.java, AdaptadorBase64())
        .create()

    private val clienteHttp = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun <T> crear(host: String, puerto: Int, api: Class<T>): T = Retrofit.Builder()
        .baseUrl("http://$host:$puerto/")
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(api)

    private class Apis(val host: String) {
        val usuarios: UsuariosApi = crear(host, PUERTO_USUARIOS, UsuariosApi::class.java)
        val compromisos: CompromisosApi = crear(host, PUERTO_COMPROMISOS, CompromisosApi::class.java)
        val pagos: PagosApi = crear(host, PUERTO_PAGOS, PagosApi::class.java)
        val movimientos: MovimientosApi = crear(host, PUERTO_MOVIMIENTOS, MovimientosApi::class.java)
    }

    @Volatile
    private var apis = Apis(SERVIDOR_EMULADOR)

    val host: String get() = apis.host

    fun configurar(host: String) {
        if (host != apis.host) apis = Apis(host)
    }

    val usuarios: UsuariosApi get() = apis.usuarios

    val compromisos: CompromisosApi get() = apis.compromisos

    val pagos: PagosApi get() = apis.pagos

    val movimientos: MovimientosApi get() = apis.movimientos
}
