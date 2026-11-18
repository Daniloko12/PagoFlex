package com.pagoflex.compromisos.controller;

import com.pagoflex.compromisos.dto.ActualizacionCompromisos;
import com.pagoflex.compromisos.dto.RegistroAnulacion;
import com.pagoflex.compromisos.dto.RegistroCompromiso;
import com.pagoflex.compromisos.model.Anulacion;
import com.pagoflex.compromisos.model.Boleta;
import com.pagoflex.compromisos.model.Compromiso;
import com.pagoflex.compromisos.service.CompromisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Compromisos", description = "Cuentas por pagar, boletas escaneadas y anulaciones")
public class CompromisoController {

    private final CompromisoService servicio;

    public CompromisoController(CompromisoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/compromisos")
    @Operation(summary = "Lista todos los compromisos")
    public List<Compromiso> listar() {
        return servicio.listar();
    }

    @GetMapping("/compromisos/{id}")
    @Operation(summary = "Busca un compromiso por su id")
    public Compromiso obtener(@PathVariable("id") Integer id) {
        return servicio.obtener(id);
    }

    @PostMapping("/compromisos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un compromiso, con la foto de la boleta si viene")
    public Compromiso registrar(@RequestBody RegistroCompromiso registro) {
        return servicio.registrar(registro);
    }

    @PutMapping("/compromisos")
    @Operation(summary = "Guarda cambios en uno o varios compromisos (modificar, vencer, pagar o siguiente cuota)")
    public List<Compromiso> actualizar(@RequestBody ActualizacionCompromisos actualizacion) {
        return servicio.actualizar(actualizacion);
    }

    @GetMapping("/compromisos/{id}/boleta")
    @Operation(summary = "Devuelve la foto de la boleta escaneada (Base64)")
    public Boleta boleta(@PathVariable("id") Integer id) {
        return servicio.boletaDe(id);
    }

    @GetMapping("/anulaciones")
    @Operation(summary = "Lista las anulaciones")
    public List<Anulacion> anulaciones() {
        return servicio.anulaciones();
    }

    @PostMapping("/anulaciones")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Anula un compromiso indicando el motivo")
    public Anulacion anular(@RequestBody RegistroAnulacion registro) {
        return servicio.anular(registro);
    }
}
