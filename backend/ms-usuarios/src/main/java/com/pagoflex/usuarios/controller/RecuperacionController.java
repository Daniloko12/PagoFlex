package com.pagoflex.usuarios.controller;

import com.pagoflex.usuarios.dto.CodigoCreado;
import com.pagoflex.usuarios.dto.RecuperacionClave;
import com.pagoflex.usuarios.dto.ResultadoVerificacion;
import com.pagoflex.usuarios.dto.SolicitudCodigo;
import com.pagoflex.usuarios.dto.VerificacionCodigo;
import com.pagoflex.usuarios.service.RecuperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recuperacion")
@Tag(name = "Recuperar contraseña", description = "Código de 6 dígitos que dura 10 minutos y permite 3 intentos")
public class RecuperacionController {

    private final RecuperacionService servicio;

    public RecuperacionController(RecuperacionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/codigos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Guarda el hash de un código nuevo y anula los anteriores")
    public CodigoCreado solicitar(@RequestBody SolicitudCodigo solicitud) {
        return servicio.solicitar(solicitud);
    }

    @PostMapping("/verificar")
    @Operation(summary = "Revisa un código: CORRECTO, INCORRECTO, VENCIDO, SIN_INTENTOS o SIN_CODIGO")
    public ResultadoVerificacion verificar(@RequestBody VerificacionCodigo verificacion) {
        return servicio.verificar(verificacion);
    }

    @PostMapping("/clave")
    @Operation(summary = "Cambia la contraseña si el código es correcto")
    public ResultadoVerificacion recuperarClave(@RequestBody RecuperacionClave recuperacion) {
        return servicio.recuperarClave(recuperacion);
    }
}
