package com.example.pagoflex.model

import java.time.LocalDate

data class FormularioUiState(
    val empresa: EmpresaCliente? = null,
    val rutPersona: String = "",
    val nombresPersona: String = "",
    val apellidosPersona: String = "",
    val correoPersona: String = "",
    val telefonoPersona: String = "",
    val concepto: String = "",
    val monto: String = "",
    val fechaVencimiento: String = "",
    val numeroCuota: String = "",
    val totalCuotas: String = "",

    val personaNoExiste: Boolean = false,
    val personaEncontrada: String? = null,

    val errorEmpresa: String? = null,
    val errorRutPersona: String? = null,
    val errorNombresPersona: String? = null,
    val errorApellidosPersona: String? = null,
    val errorCorreoPersona: String? = null,
    val errorTelefonoPersona: String? = null,
    val errorConcepto: String? = null,
    val errorMonto: String? = null,
    val errorFechaVencimiento: String? = null,
    val errorCuota: String? = null,
    val errorDuplicado: String? = null,

    val registroExitoso: Boolean = false
)

data class PersonaNueva(
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String
)

data class NuevoCompromiso(
    val idEmpresa: Int,
    val idPersona: Int?,
    val personaNueva: PersonaNueva?,
    val concepto: String,
    val monto: Int,
    val fechaVencimiento: LocalDate,
    val numeroCuota: Int?,
    val totalCuotas: Int?
)

data class EditarCompromisoUiState(
    val concepto: String = "",
    val monto: String = "",
    val fechaVencimiento: String = "",
    val numeroCuota: String = "",
    val totalCuotas: String = "",

    val cargado: Boolean = false,

    val errorConcepto: String? = null,
    val errorMonto: String? = null,
    val errorFechaVencimiento: String? = null,
    val errorCuota: String? = null,
    val errorDuplicado: String? = null,
    val errorEdicion: String? = null
)

data class CambiosCompromiso(
    val concepto: String,
    val monto: Int,
    val fechaVencimiento: LocalDate,
    val numeroCuota: Int?,
    val totalCuotas: Int?
)

data class ClienteUiState(
    val rut: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val telefono: String = "",
    val comuna: Comuna? = null,
    val activo: Boolean = true,

    val idEditando: Int? = null,

    val errorRut: String? = null,
    val errorNombres: String? = null,
    val errorApellidos: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorComuna: String? = null
)

data class DatosCliente(
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String,
    val idComuna: Int,
    val activo: Boolean
)

data class IngresoUiState(
    val rol: Rol = Rol.USUARIO_FINAL,
    val correo: String = "",
    val clave: String = "",

    val claveVisible: Boolean = false,

    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorIngreso: String? = null
)

data class Credenciales(
    val correo: String,
    val clave: String,
    val rol: Rol
)

data class RegistroUiState(
    val rut: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val telefono: String = "",
    val clave: String = "",
    val confirmacionClave: String = "",
    val aceptaTerminos: Boolean = false,

    val claveVisible: Boolean = false,

    val errorRut: String? = null,
    val errorNombres: String? = null,
    val errorApellidos: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val errorClave: String? = null,
    val errorConfirmacion: String? = null,
    val errorTerminos: String? = null,
    val errorRegistro: String? = null
)

data class NuevaCuenta(
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String,
    val clave: String
)

data class PagoUiState(
    val medioPago: MedioPago? = null,
    val idTarjeta: Int? = null,
    val usarTarjetaNueva: Boolean = false,
    val numeroTarjeta: String = "",
    val titularTarjeta: String = "",
    val vencimientoTarjeta: String = "",
    val cvv: String = "",
    val guardarTarjeta: Boolean = true,
    val codigo: String = "",
    val confirmaTotal: Boolean = false,

    val codigoVisible: Boolean = false,

    val errorMedioPago: String? = null,
    val errorTarjeta: String? = null,
    val errorNumeroTarjeta: String? = null,
    val errorTitularTarjeta: String? = null,
    val errorVencimientoTarjeta: String? = null,
    val errorCvv: String? = null,
    val errorCodigo: String? = null,
    val errorConfirmacion: String? = null,
    val errorPago: String? = null,

    val pagoExitoso: Boolean = false
)

data class SolicitudPago(
    val medioPago: MedioPago,
    val idTarjeta: Int? = null,
    val tarjetaNueva: TarjetaNueva? = null,
    val guardarTarjeta: Boolean = false
)

data class ResultadoPago(
    val pago: Pago,
    val reconocimientos: List<String>
)

data class TarjetaUiState(
    val medioPago: MedioPago = MedioPago.DEBITO,
    val numero: String = "",
    val titular: String = "",
    val vencimiento: String = "",
    val cvv: String = "",

    val errorNumero: String? = null,
    val errorTitular: String? = null,
    val errorVencimiento: String? = null,
    val errorCvv: String? = null,
    val errorTarjeta: String? = null
)

data class ReporteUiState(
    val motivo: MotivoReporte? = null,
    val comentario: String = "",

    val errorMotivo: String? = null,
    val errorComentario: String? = null,
    val errorReporte: String? = null,

    val reporteEnviado: Boolean = false
)

data class SolicitudReporte(
    val motivo: MotivoReporte,
    val comentario: String?
)

data class RecomendacionUiState(
    val nombreOrganizacion: String = "",
    val rutOrganizacion: String = "",
    val rubro: Rubro? = null,
    val comuna: Comuna? = null,
    val nombreContacto: String = "",
    val correoContacto: String = "",
    val telefonoContacto: String = "",
    val autorizaContacto: Boolean = false,

    val errorNombreOrganizacion: String? = null,
    val errorRutOrganizacion: String? = null,
    val errorRubro: String? = null,
    val errorComuna: String? = null,
    val errorNombreContacto: String? = null,
    val errorCorreoContacto: String? = null,
    val errorAutorizaContacto: String? = null,
    val errorRecomendacion: String? = null,

    val recomendacionEnviada: Boolean = false
)

data class ContactoNuevo(
    val nombre: String,
    val correo: String?,
    val telefono: String?
)

data class NuevaRecomendacion(
    val nombreOrganizacion: String,
    val rutOrganizacion: String?,
    val idRubro: Int,
    val idComuna: Int,
    val contacto: ContactoNuevo?
)
