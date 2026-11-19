package com.example.pagoflex.data.remoto

import com.example.pagoflex.model.Pago
import com.example.pagoflex.model.Tarjeta
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PagosApi {
    @GET("api/pagos")
    suspend fun pagos(): List<Pago>

    @POST("api/pagos")
    suspend fun registrarPago(@Body registro: RegistroPago): Pago

    @GET("api/tarjetas")
    suspend fun tarjetas(): List<Tarjeta>

    @POST("api/tarjetas")
    suspend fun registrarTarjeta(@Body tarjeta: Tarjeta): Tarjeta

    @DELETE("api/tarjetas/{id}")
    suspend fun quitarTarjeta(@Path("id") idTarjeta: Int): Tarjeta

    @POST("api/admin/pagos/restablecer")
    suspend fun restablecer(): Map<String, String>
}
