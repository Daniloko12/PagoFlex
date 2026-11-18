package com.example.pagoflex.data.remoto

import com.example.pagoflex.model.Anulacion
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.ContactoRecomendacion
import com.example.pagoflex.model.EmpresaCliente
import com.example.pagoflex.model.HistorialRecomendacion
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.Reporte
import com.example.pagoflex.model.Rubro
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CompromisosApi {
    @GET("api/empresas")
    suspend fun empresas(): List<EmpresaCliente>

    @GET("api/rubros")
    suspend fun rubros(): List<Rubro>

    @GET("api/compromisos")
    suspend fun compromisos(): List<Compromiso>

    @POST("api/compromisos")
    suspend fun registrar(@Body registro: RegistroCompromiso): Compromiso

    @PUT("api/compromisos")
    suspend fun actualizar(@Body actualizacion: ActualizacionCompromisos): List<Compromiso>

    @GET("api/compromisos/{id}/boleta")
    suspend fun boleta(@Path("id") idCompromiso: Int): BoletaGuardada

    @GET("api/anulaciones")
    suspend fun anulaciones(): List<Anulacion>

    @POST("api/anulaciones")
    suspend fun anular(@Body registro: RegistroAnulacion): Anulacion

    @GET("api/reportes")
    suspend fun reportes(): List<Reporte>

    @POST("api/reportes")
    suspend fun registrarReporte(@Body registro: RegistroReporte): Reporte

    @PUT("api/reportes/{id}")
    suspend fun actualizarReporte(@Path("id") idReporte: Int, @Body actualizacion: ActualizacionReporte): Reporte

    @GET("api/recomendaciones")
    suspend fun recomendaciones(): List<Recomendacion>

    @GET("api/recomendaciones/contactos")
    suspend fun contactos(): List<ContactoRecomendacion>

    @GET("api/recomendaciones/historial")
    suspend fun historial(): List<HistorialRecomendacion>

    @POST("api/recomendaciones")
    suspend fun registrarRecomendacion(@Body registro: RegistroRecomendacion): Recomendacion

    @PUT("api/recomendaciones/{id}/estado")
    suspend fun cambiarEstadoRecomendacion(@Path("id") idRecomendacion: Int, @Body cambio: CambioEstadoRecomendacion): Recomendacion

    @POST("api/admin/restablecer")
    suspend fun restablecer(): Map<String, String>
}
