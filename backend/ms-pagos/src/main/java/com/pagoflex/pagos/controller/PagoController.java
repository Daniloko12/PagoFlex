package com.pagoflex.pagos.controller;

import com.pagoflex.pagos.dto.RegistroPago;
import com.pagoflex.pagos.model.Pago;
import com.pagoflex.pagos.service.PagoService;
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
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Pagos de compromisos; al pagar se avisa a ms-compromisos, ms-usuarios y ms-movimientos")
public class PagoController {

    private final PagoService servicio;

    public PagoController(PagoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista los pagos")
    public List<Pago> listar() {
        return servicio.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un pago y marca el compromiso como pagado")
    public Pago registrar(@RequestBody RegistroPago registro) {
        return servicio.registrar(registro);
    }
}
