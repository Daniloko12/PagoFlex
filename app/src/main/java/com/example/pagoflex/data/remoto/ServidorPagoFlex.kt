package com.example.pagoflex.data.remoto

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
    const val HOST = "10.0.2.2"
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

    private fun retrofit(puerto: Int): Retrofit = Retrofit.Builder()
        .baseUrl("http://$HOST:$puerto/")
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val usuarios: UsuariosApi by lazy { retrofit(PUERTO_USUARIOS).create(UsuariosApi::class.java) }

    val compromisos: CompromisosApi by lazy { retrofit(PUERTO_COMPROMISOS).create(CompromisosApi::class.java) }

    val pagos: PagosApi by lazy { retrofit(PUERTO_PAGOS).create(PagosApi::class.java) }

    val movimientos: MovimientosApi by lazy { retrofit(PUERTO_MOVIMIENTOS).create(MovimientosApi::class.java) }
}
