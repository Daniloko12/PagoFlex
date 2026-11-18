package com.pagoflex.compromisos.controller;

import com.pagoflex.compromisos.dto.CambioEstadoRecomendacion;
import com.pagoflex.compromisos.dto.RegistroRecomendacion;
import com.pagoflex.compromisos.model.ContactoRecomendacion;
import com.pagoflex.compromisos.model.HistorialRecomendacion;
import com.pagoflex.compromisos.model.Recomendacion;
import com.pagoflex.compromisos.service.RecomendacionService;
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
@RequestMapping("/api/recomendaciones")
@Tag(name = "Recomendaciones", description = "Organizaciones que los clientes recomiendan sumar a PagoFlex")
public class RecomendacionController {

    private final RecomendacionService servicio;

    public RecomendacionController(RecomendacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista las recomendaciones")
    public List<Recomendacion> listar() {
        return servicio.listar();
    }

    @GetMapping("/contactos")
    @Operation(summary = "Lista los contactos de las recomendaciones")
    public List<ContactoRecomendacion> contactos() {
        return servicio.contactos();
    }

    @GetMapping("/historial")
    @Operation(summary = "Lista los cambios de estado de las recomendaciones")
    public List<HistorialRecomendacion> historial() {
        return servicio.historial();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una recomendación")
    public Recomendacion registrar(@RequestBody RegistroRecomendacion registro) {
        return servicio.registrar(registro);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Avanza una recomendación de estado; si corresponde, avisa a ms-usuarios del reconocimiento")
    public Recomendacion cambiarEstado(@PathVariable("id") Integer id, @RequestBody CambioEstadoRecomendacion cambio) {
        return servicio.cambiarEstado(id, cambio);
    }
}
