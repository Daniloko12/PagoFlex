package com.example.pagoflex.data.remoto

import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.Reconocimiento
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.UsuarioReconocimiento
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuariosApi {
    @GET("api/usuarios")
    suspend fun usuarios(): List<Usuario>

    @POST("api/usuarios/login")
    suspend fun iniciarSesion(@Body ingreso: Ingreso): Usuario

    @POST("api/usuarios")
    suspend fun registrar(@Body registro: RegistroUsuario): Usuario

    @PUT("api/usuarios/{id}")
    suspend fun actualizar(@Path("id") idUsuario: Int, @Body actualizacion: ActualizacionUsuario): Usuario

    @PUT("api/usuarios/{id}/avatar")
    suspend fun cambiarAvatar(@Path("id") idUsuario: Int, @Body foto: NuevaFoto): Usuario

    @PUT("api/usuarios/{id}/clave")
    suspend fun cambiarClave(@Path("id") idUsuario: Int, @Body clave: NuevaClave): Usuario

    @GET("api/comunas")
    suspend fun comunas(): List<Comuna>

    @GET("api/reconocimientos")
    suspend fun reconocimientos(): List<Reconocimiento>

    @GET("api/reconocimientos/obtenidos")
    suspend fun reconocimientosObtenidos(): List<UsuarioReconocimiento>

    @POST("api/recuperacion/codigos")
    suspend fun solicitarCodigo(@Body solicitud: SolicitudCodigo): CodigoCreado

    @POST("api/recuperacion/verificar")
    suspend fun verificarCodigo(@Body verificacion: VerificacionCodigo): ResultadoVerificacion

    @POST("api/recuperacion/clave")
    suspend fun recuperarClave(@Body recuperacion: RecuperacionClave): ResultadoVerificacion

    @POST("api/admin/usuarios/restablecer")
    suspend fun restablecer(): Map<String, String>
}
