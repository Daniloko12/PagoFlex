package com.pagoflex.pagos.service;

import com.pagoflex.pagos.model.Tarjeta;
import com.pagoflex.pagos.repository.TarjetaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TarjetaService {

    private static final Set<String> MEDIOS = Set.of("DEBITO", "CREDITO");
    private static final Set<String> MARCAS = Set.of("VISA", "MASTERCARD", "AMEX", "OTRA");

    private final TarjetaRepository tarjetas;

    public TarjetaService(TarjetaRepository tarjetas) {
        this.tarjetas = tarjetas;
    }

    @Transactional(readOnly = true)
    public List<Tarjeta> listar() {
        return tarjetas.findAllByOrderByIdTarjetaAsc();
    }

    @Transactional
    public synchronized Tarjeta registrar(Tarjeta tarjeta) {
        return tarjetas.save(prepararNueva(tarjeta));
    }

    @Transactional
    public Tarjeta quitar(Integer id) {
        Tarjeta tarjeta = tarjetas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la tarjeta " + id));
        tarjeta.setGuardada(false);
        return tarjetas.save(tarjeta);
    }

    boolean existe(Integer id) {
        return tarjetas.existsById(id);
    }

    Tarjeta prepararNueva(Tarjeta tarjeta) {
        if (tarjeta == null || tarjeta.getIdUsuario() == null || tarjeta.getTitular() == null || tarjeta.getTitular().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La tarjeta necesita usuario y titular");
        }
        if (tarjeta.getUltimos4() == null || !tarjeta.getUltimos4().matches("\\d{4}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se guardan los 4 últimos dígitos de la tarjeta");
        }
        if (!MEDIOS.contains(tarjeta.getMedioPago()) || !MARCAS.contains(tarjeta.getMarca())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Medio de pago o marca no válidos");
        }
        if (tarjeta.getVencimiento() == null || !tarjeta.getVencimiento().matches("\\d{4}-\\d{2}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El vencimiento va en formato AAAA-MM");
        }
        if (tarjeta.getIdTarjeta() == null || tarjeta.getIdTarjeta() <= 0) {
            tarjeta.setIdTarjeta(tarjetas.maximoId() + 1);
        } else if (tarjetas.existsById(tarjeta.getIdTarjeta())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe la tarjeta " + tarjeta.getIdTarjeta());
        }
        if (tarjeta.getGuardada() == null) {
            tarjeta.setGuardada(true);
        }
        if (tarjeta.getFechaRegistro() == null) {
            tarjeta.setFechaRegistro(LocalDateTime.now());
        }
        return tarjeta;
    }
}
