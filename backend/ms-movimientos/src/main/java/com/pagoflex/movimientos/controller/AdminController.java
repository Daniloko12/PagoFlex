package com.pagoflex.movimientos.controller;

import com.pagoflex.movimientos.config.DatosIniciales;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administración", description = "Herramientas para las pruebas")
public class AdminController {

    private final DatosIniciales datosIniciales;

    public AdminController(DatosIniciales datosIniciales) {
        this.datosIniciales = datosIniciales;
    }

    @PostMapping("/restablecer")
    @Operation(summary = "Borra los datos y vuelve a cargar los datos de prueba")
    public Map<String, String> restablecer() {
        datosIniciales.restablecer();
        return Map.of("estado", "Datos de prueba cargados");
    }
}
