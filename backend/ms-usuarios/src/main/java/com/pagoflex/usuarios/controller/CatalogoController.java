package com.pagoflex.usuarios.controller;

import com.pagoflex.usuarios.model.Comuna;
import com.pagoflex.usuarios.model.Reconocimiento;
import com.pagoflex.usuarios.model.UsuarioReconocimiento;
import com.pagoflex.usuarios.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Catálogos y reconocimientos", description = "Comunas y reconocimientos que ganan los clientes")
public class CatalogoController {

    private final CatalogoService servicio;

    public CatalogoController(CatalogoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/comunas")
    @Operation(summary = "Lista las comunas")
    public List<Comuna> comunas() {
        return servicio.comunas();
    }

    @GetMapping("/reconocimientos")
    @Operation(summary = "Lista los reconocimientos que se pueden ganar")
    public List<Reconocimiento> reconocimientos() {
        return servicio.reconocimientos();
    }

    @GetMapping("/reconocimientos/obtenidos")
    @Operation(summary = "Lista los reconocimientos que ya ganó cada cliente")
    public List<UsuarioReconocimiento> obtenidos() {
        return servicio.obtenidos();
    }

    @PostMapping("/reconocimientos/obtenidos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra reconocimientos ganados; los repetidos se ignoran")
    public List<UsuarioReconocimiento> registrarObtenidos(@RequestBody List<UsuarioReconocimiento> nuevos) {
        return servicio.registrarObtenidos(nuevos);
    }
}
