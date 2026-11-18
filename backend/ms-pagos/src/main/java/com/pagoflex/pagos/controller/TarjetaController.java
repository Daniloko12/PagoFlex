package com.pagoflex.pagos.controller;

import com.pagoflex.pagos.model.Tarjeta;
import com.pagoflex.pagos.service.TarjetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tarjetas")
@Tag(name = "Tarjetas", description = "Tarjetas guardadas; solo se guardan la marca y los 4 últimos dígitos")
public class TarjetaController {

    private final TarjetaService servicio;

    public TarjetaController(TarjetaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista las tarjetas")
    public List<Tarjeta> listar() {
        return servicio.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Guarda una tarjeta")
    public Tarjeta registrar(@RequestBody Tarjeta tarjeta) {
        return servicio.registrar(tarjeta);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Quita una tarjeta de las guardadas")
    public Tarjeta quitar(@PathVariable("id") Integer id) {
        return servicio.quitar(id);
    }
}
