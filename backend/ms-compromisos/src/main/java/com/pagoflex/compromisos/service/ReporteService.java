package com.pagoflex.compromisos.service;

import static com.pagoflex.compromisos.service.Validaciones.conCompromiso;
import static com.pagoflex.compromisos.service.Validaciones.exigir;
import static com.pagoflex.compromisos.service.Validaciones.sinId;
import static com.pagoflex.compromisos.service.Validaciones.vacio;

import com.pagoflex.compromisos.client.MovimientosClient;
import com.pagoflex.compromisos.dto.ActualizacionReporte;
import com.pagoflex.compromisos.dto.RegistroReporte;
import com.pagoflex.compromisos.model.Reporte;
import com.pagoflex.compromisos.repository.ReporteRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReporteService {

    private final ReporteRepository reportes;
    private final CompromisoService compromisos;
    private final MovimientosClient movimientos;

    public ReporteService(ReporteRepository reportes, CompromisoService compromisos, MovimientosClient movimientos) {
        this.reportes = reportes;
        this.compromisos = compromisos;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public List<Reporte> listar() {
        return reportes.findAllByOrderByIdReporteAsc();
    }

    @Transactional
    public synchronized Reporte registrar(RegistroReporte registro) {
        Reporte reporte = registro.reporte();
        exigir(reporte != null && reporte.getIdCompromiso() != null && !vacio(reporte.getMotivo()),
                "El reporte necesita el compromiso y el motivo");
        compromisos.obtener(reporte.getIdCompromiso());
        if (sinId(reporte.getIdReporte())) {
            reporte.setIdReporte(reportes.maximoId() + 1);
        } else if (reportes.existsById(reporte.getIdReporte())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe el reporte " + reporte.getIdReporte());
        }
        if (vacio(reporte.getNumero())) {
            reporte.setNumero(String.format("REP-%04d", reporte.getIdReporte()));
        }
        if (reporte.getFechaCreacion() == null) {
            reporte.setFechaCreacion(LocalDateTime.now());
        }
        if (vacio(reporte.getEstado())) {
            reporte.setEstado("ABIERTO");
        }
        Reporte guardado = reportes.save(reporte);
        if (registro.compromiso() != null) {
            compromisos.guardarExistente(registro.compromiso());
        }
        movimientos.registrar(conCompromiso(registro.movimientos(), reporte.getIdCompromiso()));
        return guardado;
    }

    @Transactional
    public Reporte actualizar(Integer id, ActualizacionReporte actualizacion) {
        exigir(actualizacion.reporte() != null, "Faltan los datos del reporte");
        if (!reportes.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el reporte " + id);
        }
        Reporte reporte = actualizacion.reporte();
        reporte.setIdReporte(id);
        exigir(reporte.getIdCompromiso() != null && !vacio(reporte.getNumero()) && !vacio(reporte.getEstado())
                && reporte.getFechaCreacion() != null, "Faltan datos del reporte");
        Reporte guardado = reportes.save(reporte);
        if (actualizacion.compromiso() != null) {
            compromisos.guardarExistente(actualizacion.compromiso());
        }
        movimientos.registrar(conCompromiso(actualizacion.movimientos(), reporte.getIdCompromiso()));
        return guardado;
    }
}
