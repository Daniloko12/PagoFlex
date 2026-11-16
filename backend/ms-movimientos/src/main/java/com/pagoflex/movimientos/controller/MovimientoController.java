package com.pagoflex.movimientos.controller;

import com.pagoflex.movimientos.model.Movimiento;
import com.pagoflex.movimientos.service.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimientos")
@Tag(name = "Movimientos", description = "Historial de todo lo que pasa en PagoFlex y quién lo hizo")
public class MovimientoController {

    private final MovimientoService servicio;

    public MovimientoController(MovimientoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista los movimientos; se pueden filtrar por usuario o por compromiso")
    public List<Movimiento> listar(
            @RequestParam(name = "idUsuario", required = false) Integer idUsuario,
            @RequestParam(name = "idCompromiso", required = false) Integer idCompromiso) {
        return servicio.listar(idUsuario, idCompromiso);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uno o varios movimientos")
    public List<Movimiento> registrar(@RequestBody List<Movimiento> movimientos) {
        return servicio.registrar(movimientos);
    }
}
