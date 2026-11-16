package com.pagoflex.compromisos.service;

import static com.pagoflex.compromisos.service.Validaciones.exigir;
import static com.pagoflex.compromisos.service.Validaciones.sinId;
import static com.pagoflex.compromisos.service.Validaciones.vacio;

import com.pagoflex.compromisos.client.UsuariosClient;
import com.pagoflex.compromisos.dto.CambioEstadoRecomendacion;
import com.pagoflex.compromisos.dto.RegistroRecomendacion;
import com.pagoflex.compromisos.model.ContactoRecomendacion;
import com.pagoflex.compromisos.model.HistorialRecomendacion;
import com.pagoflex.compromisos.model.Recomendacion;
import com.pagoflex.compromisos.repository.ContactoRecomendacionRepository;
import com.pagoflex.compromisos.repository.HistorialRecomendacionRepository;
import com.pagoflex.compromisos.repository.RecomendacionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecomendacionService {

    private final RecomendacionRepository recomendaciones;
    private final ContactoRecomendacionRepository contactos;
    private final HistorialRecomendacionRepository historial;
    private final UsuariosClient usuarios;

    public RecomendacionService(RecomendacionRepository recomendaciones, ContactoRecomendacionRepository contactos,
                                HistorialRecomendacionRepository historial, UsuariosClient usuarios) {
        this.recomendaciones = recomendaciones;
        this.contactos = contactos;
        this.historial = historial;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<Recomendacion> listar() {
        return recomendaciones.findAllByOrderByIdRecomendacionAsc();
    }

    @Transactional(readOnly = true)
    public List<ContactoRecomendacion> contactos() {
        return contactos.findAllByOrderByIdRecomendacionAsc();
    }

    @Transactional(readOnly = true)
    public List<HistorialRecomendacion> historial() {
        return historial.findAllByOrderByIdHistorialAsc();
    }

    @Transactional
    public synchronized Recomendacion registrar(RegistroRecomendacion registro) {
        Recomendacion recomendacion = registro.recomendacion();
        exigir(recomendacion != null && recomendacion.getIdUsuarioFinal() != null && !vacio(recomendacion.getNombreOrganizacion())
                && recomendacion.getIdRubro() != null && recomendacion.getIdComuna() != null,
                "La recomendación necesita cliente, organización, rubro y comuna");
        if (sinId(recomendacion.getIdRecomendacion())) {
            recomendacion.setIdRecomendacion(recomendaciones.maximoId() + 1);
        } else if (recomendaciones.existsById(recomendacion.getIdRecomendacion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe la recomendación " + recomendacion.getIdRecomendacion());
        }
        if (vacio(recomendacion.getNumero())) {
            recomendacion.setNumero(String.format("REC-%04d", recomendacion.getIdRecomendacion()));
        }
        if (recomendacion.getFecha() == null) {
            recomendacion.setFecha(LocalDateTime.now());
        }
        if (vacio(recomendacion.getEstado())) {
            recomendacion.setEstado("RECIBIDA");
        }
        Recomendacion guardada = recomendaciones.save(recomendacion);
        ContactoRecomendacion contacto = registro.contacto();
        if (contacto != null) {
            exigir(!vacio(contacto.getNombre()), "Falta el nombre del contacto");
            contacto.setIdRecomendacion(guardada.getIdRecomendacion());
            if (contacto.getAutorizaContacto() == null) {
                contacto.setAutorizaContacto(false);
            }
            contactos.save(contacto);
        }
        return guardada;
    }

    @Transactional
    public synchronized Recomendacion cambiarEstado(Integer id, CambioEstadoRecomendacion cambio) {
        exigir(cambio.recomendacion() != null && cambio.historial() != null, "Faltan la recomendación y su historial");
        if (!recomendaciones.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la recomendación " + id);
        }
        Recomendacion recomendacion = cambio.recomendacion();
        recomendacion.setIdRecomendacion(id);
        exigir(!vacio(recomendacion.getEstado()), "Falta el estado nuevo");
        Recomendacion guardada = recomendaciones.save(recomendacion);
        HistorialRecomendacion registro = cambio.historial();
        if (sinId(registro.getIdHistorial()) || historial.existsById(registro.getIdHistorial())) {
            registro.setIdHistorial(historial.maximoId() + 1);
        }
        registro.setIdRecomendacion(id);
        if (registro.getFecha() == null) {
            registro.setFecha(LocalDateTime.now());
        }
        exigir(registro.getIdAdministrador() != null && !vacio(registro.getEstado()), "El historial necesita administrador y estado");
        historial.save(registro);
        if (cambio.reconocimiento() != null) {
            usuarios.registrarReconocimientos(List.of(cambio.reconocimiento()));
        }
        return guardada;
    }
}
