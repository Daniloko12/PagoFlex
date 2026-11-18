package com.pagoflex.compromisos.service;

import static com.pagoflex.compromisos.service.Validaciones.conCompromiso;
import static com.pagoflex.compromisos.service.Validaciones.exigir;
import static com.pagoflex.compromisos.service.Validaciones.sinId;
import static com.pagoflex.compromisos.service.Validaciones.vacio;

import com.pagoflex.compromisos.client.MovimientosClient;
import com.pagoflex.compromisos.dto.ActualizacionCompromisos;
import com.pagoflex.compromisos.dto.RegistroAnulacion;
import com.pagoflex.compromisos.dto.RegistroCompromiso;
import com.pagoflex.compromisos.model.Anulacion;
import com.pagoflex.compromisos.model.Boleta;
import com.pagoflex.compromisos.model.Compromiso;
import com.pagoflex.compromisos.repository.AnulacionRepository;
import com.pagoflex.compromisos.repository.BoletaRepository;
import com.pagoflex.compromisos.repository.CompromisoRepository;
import com.pagoflex.compromisos.repository.EmpresaClienteRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompromisoService {

    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "VENCIDO", "PAGADO", "EN_REVISION", "ANULADO");
    private static final int TAMANO_MAXIMO_BOLETA = 5 * 1024 * 1024;

    private final CompromisoRepository compromisos;
    private final AnulacionRepository anulaciones;
    private final BoletaRepository boletas;
    private final EmpresaClienteRepository empresas;
    private final MovimientosClient movimientos;

    public CompromisoService(CompromisoRepository compromisos, AnulacionRepository anulaciones, BoletaRepository boletas,
                             EmpresaClienteRepository empresas, MovimientosClient movimientos) {
        this.compromisos = compromisos;
        this.anulaciones = anulaciones;
        this.boletas = boletas;
        this.empresas = empresas;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public List<Compromiso> listar() {
        return compromisos.findAllByOrderByIdCompromisoAsc();
    }

    @Transactional(readOnly = true)
    public Compromiso obtener(Integer id) {
        return compromisos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el compromiso " + id));
    }

    @Transactional(readOnly = true)
    public Boleta boletaDe(Integer idCompromiso) {
        return boletas.findById(idCompromiso)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El compromiso no tiene boleta"));
    }

    @Transactional
    public synchronized Compromiso registrar(RegistroCompromiso registro) {
        exigir(registro.compromiso() != null, "Faltan los datos del compromiso");
        Compromiso guardado = compromisos.save(prepararNuevo(registro.compromiso()));
        if (registro.boleta() != null && registro.boleta().length > 0) {
            if (registro.boleta().length > TAMANO_MAXIMO_BOLETA) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La foto de la boleta pesa más de 5 MB");
            }
            Boleta boleta = new Boleta();
            boleta.setIdCompromiso(guardado.getIdCompromiso());
            boleta.setImagen(registro.boleta());
            boleta.setFechaRegistro(guardado.getFechaRegistro());
            boletas.save(boleta);
        }
        movimientos.registrar(conCompromiso(registro.movimientos(), guardado.getIdCompromiso()));
        return guardado;
    }

    @Transactional
    public synchronized List<Compromiso> actualizar(ActualizacionCompromisos actualizacion) {
        exigir(actualizacion.compromisos() != null && !actualizacion.compromisos().isEmpty(), "Envía al menos un compromiso");
        List<Compromiso> guardados = new ArrayList<>();
        for (Compromiso compromiso : actualizacion.compromisos()) {
            if (sinId(compromiso.getIdCompromiso()) || !compromisos.existsById(compromiso.getIdCompromiso())) {
                guardados.add(compromisos.save(prepararNuevo(compromiso)));
            } else {
                validar(compromiso);
                guardados.add(compromisos.save(compromiso));
            }
        }
        movimientos.registrar(actualizacion.movimientos());
        return guardados;
    }

    @Transactional(readOnly = true)
    public List<Anulacion> anulaciones() {
        return anulaciones.findAllByOrderByIdCompromisoAsc();
    }

    @Transactional
    public Anulacion anular(RegistroAnulacion registro) {
        Anulacion anulacion = registro.anulacion();
        exigir(anulacion != null && anulacion.getIdCompromiso() != null && anulacion.getIdAdministrador() != null
                && !vacio(anulacion.getMotivo()), "La anulación necesita compromiso, administrador y motivo");
        Compromiso actual = obtener(anulacion.getIdCompromiso());
        if (anulaciones.existsById(anulacion.getIdCompromiso()) || "ANULADO".equals(actual.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El compromiso ya está anulado");
        }
        if ("PAGADO".equals(actual.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede anular un compromiso pagado");
        }
        if (anulacion.getFecha() == null) {
            anulacion.setFecha(LocalDateTime.now());
        }
        Anulacion guardada = anulaciones.save(anulacion);
        Compromiso compromiso = registro.compromiso() != null ? registro.compromiso() : actual;
        compromiso.setIdCompromiso(actual.getIdCompromiso());
        compromiso.setEstado("ANULADO");
        if (compromiso.getFechaCambioEstado() == null || registro.compromiso() == null) {
            compromiso.setFechaCambioEstado(anulacion.getFecha());
        }
        validar(compromiso);
        compromisos.save(compromiso);
        movimientos.registrar(conCompromiso(registro.movimientos(), actual.getIdCompromiso()));
        return guardada;
    }

    Compromiso guardarExistente(Compromiso compromiso) {
        exigir(compromiso.getIdCompromiso() != null && compromisos.existsById(compromiso.getIdCompromiso()),
                "No existe el compromiso " + compromiso.getIdCompromiso());
        validar(compromiso);
        return compromisos.save(compromiso);
    }

    private Compromiso prepararNuevo(Compromiso compromiso) {
        if (sinId(compromiso.getIdCompromiso())) {
            compromiso.setIdCompromiso(compromisos.maximoId() + 1);
        } else if (compromisos.existsById(compromiso.getIdCompromiso())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe el compromiso " + compromiso.getIdCompromiso());
        }
        if (compromiso.getFechaRegistro() == null) {
            compromiso.setFechaRegistro(LocalDateTime.now());
        }
        if (compromiso.getFechaCambioEstado() == null) {
            compromiso.setFechaCambioEstado(compromiso.getFechaRegistro());
        }
        if (vacio(compromiso.getEstado())) {
            compromiso.setEstado("PENDIENTE");
        }
        if (compromiso.getRecargoAplicado() == null) {
            compromiso.setRecargoAplicado(0);
        }
        if (vacio(compromiso.getFolio())) {
            compromiso.setFolio(String.format("CP-%d-%04d", compromiso.getFechaRegistro().getYear(), compromiso.getIdCompromiso()));
        }
        validar(compromiso);
        return compromiso;
    }

    private void validar(Compromiso compromiso) {
        exigir(compromiso.getIdUsuarioFinal() != null, "Falta la persona a la que se le cobra");
        exigir(compromiso.getIdEmpresa() != null && empresas.existsById(compromiso.getIdEmpresa()), "No existe la organización indicada");
        exigir(!vacio(compromiso.getConcepto()), "Falta el concepto");
        exigir(compromiso.getMonto() != null && compromiso.getMonto() > 0, "El monto debe ser mayor a cero");
        exigir(compromiso.getFechaVencimiento() != null, "Falta la fecha de vencimiento");
        exigir(ESTADOS.contains(compromiso.getEstado()), "Estado no válido: " + compromiso.getEstado());
        exigir(!vacio(compromiso.getFolio()), "Falta el folio");
        exigir(compromiso.getFechaRegistro() != null && compromiso.getFechaCambioEstado() != null, "Faltan las fechas del compromiso");
        if (compromiso.getRecargoAplicado() == null) {
            compromiso.setRecargoAplicado(0);
        }
    }
}
