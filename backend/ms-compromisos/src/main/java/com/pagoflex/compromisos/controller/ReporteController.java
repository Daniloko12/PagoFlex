package com.pagoflex.compromisos.controller;

import com.pagoflex.compromisos.dto.ActualizacionReporte;
import com.pagoflex.compromisos.dto.RegistroReporte;
import com.pagoflex.compromisos.model.Reporte;
import com.pagoflex.compromisos.service.ReporteService;
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
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Cobros que un cliente no reconoce")
public class ReporteController {

    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista los reportes")
    public List<Reporte> listar() {
        return servicio.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un reporte y deja el compromiso en revisión")
    public Reporte registrar(@RequestBody RegistroReporte registro) {
        return servicio.registrar(registro);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Toma o resuelve un reporte")
    public Reporte actualizar(@PathVariable("id") Integer id, @RequestBody ActualizacionReporte actualizacion) {
        return servicio.actualizar(id, actualizacion);
    }
}
