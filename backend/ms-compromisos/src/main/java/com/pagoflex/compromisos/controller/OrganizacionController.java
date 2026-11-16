package com.pagoflex.compromisos.controller;

import com.pagoflex.compromisos.model.EmpresaCliente;
import com.pagoflex.compromisos.model.Rubro;
import com.pagoflex.compromisos.service.OrganizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Organizaciones", description = "Empresas que cobran a través de PagoFlex y sus rubros")
public class OrganizacionController {

    private final OrganizacionService servicio;

    public OrganizacionController(OrganizacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/empresas")
    @Operation(summary = "Lista las organizaciones")
    public List<EmpresaCliente> empresas() {
        return servicio.empresas();
    }

    @GetMapping("/rubros")
    @Operation(summary = "Lista los rubros")
    public List<Rubro> rubros() {
        return servicio.rubros();
    }
}
