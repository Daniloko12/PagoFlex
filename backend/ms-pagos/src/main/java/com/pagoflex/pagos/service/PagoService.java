package com.pagoflex.pagos.service;

import com.pagoflex.pagos.client.CompromisosClient;
import com.pagoflex.pagos.client.MovimientosClient;
import com.pagoflex.pagos.client.UsuariosClient;
import com.pagoflex.pagos.dto.CompromisoDto;
import com.pagoflex.pagos.dto.RegistroPago;
import com.pagoflex.pagos.model.Pago;
import com.pagoflex.pagos.model.Tarjeta;
import com.pagoflex.pagos.repository.PagoRepository;
import com.pagoflex.pagos.repository.TarjetaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PagoService {

    private static final Set<String> CANALES = Set.of("APP", "PORTAL", "AGENTE_CONVERSACIONAL");
    private static final Set<String> MEDIOS = Set.of("DEBITO", "CREDITO", "TRANSFERENCIA", "SIMULADO");

    private final PagoRepository pagos;
    private final TarjetaRepository tarjetas;
    private final TarjetaService tarjetaService;
    private final CompromisosClient compromisos;
    private final UsuariosClient usuarios;
    private final MovimientosClient movimientos;

    public PagoService(PagoRepository pagos, TarjetaRepository tarjetas, TarjetaService tarjetaService,
                       CompromisosClient compromisos, UsuariosClient usuarios, MovimientosClient movimientos) {
        this.pagos = pagos;
        this.tarjetas = tarjetas;
        this.tarjetaService = tarjetaService;
        this.compromisos = compromisos;
        this.usuarios = usuarios;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public List<Pago> listar() {
        return pagos.findAllByOrderByIdPagoAsc();
    }

    @Transactional
    public synchronized Pago registrar(RegistroPago registro) {
        Pago pago = registro.pago();
        validar(pago);
        if (pagos.existsByIdCompromiso(pago.getIdCompromiso())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese compromiso ya está pagado");
        }
        CompromisoDto actual = compromisos.obtener(pago.getIdCompromiso());
        if ("PAGADO".equals(actual.estado()) || "ANULADO".equals(actual.estado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El compromiso está " + actual.estado().toLowerCase() + " y no se puede pagar");
        }
        Tarjeta nueva = registro.tarjetaNueva();
        if (nueva != null) {
            tarjetas.save(tarjetaService.prepararNueva(nueva));
            if (pago.getIdTarjeta() == null) {
                pago.setIdTarjeta(nueva.getIdTarjeta());
            }
        } else if (pago.getIdTarjeta() != null && !tarjetaService.existe(pago.getIdTarjeta())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la tarjeta " + pago.getIdTarjeta());
        }
        if (pago.getIdPago() == null || pago.getIdPago() <= 0) {
            pago.setIdPago(pagos.maximoId() + 1);
        } else if (pagos.existsById(pago.getIdPago())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe el pago " + pago.getIdPago());
        }
        if (pago.getNumeroComprobante() == null || pago.getNumeroComprobante().isBlank()) {
            pago.setNumeroComprobante(siguienteComprobante());
        }
        Pago guardado = pagos.save(pago);
        List<CompromisoDto> cambios = new ArrayList<>();
        cambios.add(registro.compromiso() != null ? registro.compromiso() : actual.pagado(pago.getFechaHora()));
        if (registro.siguienteCuota() != null) {
            cambios.add(registro.siguienteCuota());
        }
        compromisos.actualizar(cambios);
        usuarios.registrarReconocimientos(registro.reconocimientos());
        movimientos.registrar(registro.movimientos());
        return guardado;
    }

    private void validar(Pago pago) {
        if (pago == null || pago.getIdCompromiso() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago necesita el compromiso que se paga");
        }
        if (pago.getMontoPagado() == null || pago.getMontoPagado() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto pagado debe ser mayor a cero");
        }
        if (!CANALES.contains(pago.getCanal()) || !MEDIOS.contains(pago.getMedioPago())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Canal o medio de pago no válidos");
        }
        if (pago.getFechaHora() == null) {
            pago.setFechaHora(LocalDateTime.now());
        }
    }

    private String siguienteComprobante() {
        int ultimo = pagos.findAll().stream()
                .map(Pago::getNumeroComprobante)
                .filter(numero -> numero != null && numero.matches("CPR-\\d+"))
                .mapToInt(numero -> Integer.parseInt(numero.substring(4)))
                .max()
                .orElse(0);
        return String.format("CPR-%06d", ultimo + 1);
    }
}
