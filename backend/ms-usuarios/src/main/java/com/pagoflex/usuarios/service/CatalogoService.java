package com.pagoflex.usuarios.service;

import com.pagoflex.usuarios.model.Comuna;
import com.pagoflex.usuarios.model.Reconocimiento;
import com.pagoflex.usuarios.model.UsuarioReconocimiento;
import com.pagoflex.usuarios.repository.ComunaRepository;
import com.pagoflex.usuarios.repository.ReconocimientoRepository;
import com.pagoflex.usuarios.repository.UsuarioReconocimientoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogoService {

    private final ComunaRepository comunas;
    private final ReconocimientoRepository reconocimientos;
    private final UsuarioReconocimientoRepository obtenidos;

    public CatalogoService(ComunaRepository comunas, ReconocimientoRepository reconocimientos,
                           UsuarioReconocimientoRepository obtenidos) {
        this.comunas = comunas;
        this.reconocimientos = reconocimientos;
        this.obtenidos = obtenidos;
    }

    @Transactional(readOnly = true)
    public List<Comuna> comunas() {
        return comunas.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Reconocimiento> reconocimientos() {
        return reconocimientos.findAllByOrderByIdReconocimientoAsc();
    }

    @Transactional(readOnly = true)
    public List<UsuarioReconocimiento> obtenidos() {
        return obtenidos.findAllByOrderByIdAsc();
    }

    @Transactional
    public List<UsuarioReconocimiento> registrarObtenidos(List<UsuarioReconocimiento> nuevos) {
        if (nuevos == null) {
            return List.of();
        }
        List<UsuarioReconocimiento> pendientes = nuevos.stream()
                .peek(this::validar)
                .filter(nuevo -> !obtenidos.existsByIdUsuarioFinalAndIdReconocimiento(nuevo.getIdUsuarioFinal(), nuevo.getIdReconocimiento()))
                .toList();
        pendientes.forEach(nuevo -> nuevo.setId(null));
        return obtenidos.saveAll(pendientes);
    }

    private void validar(UsuarioReconocimiento reconocimiento) {
        if (reconocimiento.getIdUsuarioFinal() == null || reconocimiento.getIdReconocimiento() == null
                || reconocimiento.getFechaObtencion() == null || reconocimiento.getMotivo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos del reconocimiento");
        }
        if (!reconocimientos.existsById(reconocimiento.getIdReconocimiento())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el reconocimiento " + reconocimiento.getIdReconocimiento());
        }
    }
}
