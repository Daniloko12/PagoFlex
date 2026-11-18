package com.pagoflex.movimientos.service;

import com.pagoflex.movimientos.model.Movimiento;
import com.pagoflex.movimientos.repository.MovimientoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MovimientoService {

    private final MovimientoRepository repositorio;

    public MovimientoService(MovimientoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Movimiento> listar(Integer idUsuario, Integer idCompromiso) {
        if (idUsuario != null) {
            return repositorio.findByIdUsuarioOrderByFechaDescIdMovimientoDesc(idUsuario);
        }
        if (idCompromiso != null) {
            return repositorio.findByIdCompromisoOrderByFechaAscIdMovimientoAsc(idCompromiso);
        }
        return repositorio.findAllByOrderByIdMovimientoAsc();
    }

    @Transactional
    public List<Movimiento> registrar(List<Movimiento> movimientos) {
        if (movimientos == null || movimientos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envía al menos un movimiento");
        }
        for (Movimiento movimiento : movimientos) {
            validar(movimiento);
            movimiento.setIdMovimiento(null);
        }
        return repositorio.saveAll(movimientos);
    }

    private void validar(Movimiento movimiento) {
        if (movimiento.getTipo() == null || movimiento.getTipo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el tipo del movimiento");
        }
        if (movimiento.getFecha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la fecha del movimiento");
        }
        if (movimiento.getIdUsuario() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el usuario del movimiento");
        }
        if (movimiento.getDetalle() == null || movimiento.getDetalle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el detalle del movimiento");
        }
    }
}
