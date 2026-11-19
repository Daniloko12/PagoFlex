package com.example.pagoflex.data.remoto

import com.example.pagoflex.model.Movimiento
import retrofit2.http.GET
import retrofit2.http.POST

interface MovimientosApi {
    @GET("api/movimientos")
    suspend fun movimientos(): List<Movimiento>

    @POST("api/admin/movimientos/restablecer")
    suspend fun restablecer(): Map<String, String>
}
