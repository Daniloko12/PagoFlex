package com.pagoflex.usuarios.dto;

import java.time.LocalDateTime;

public record MovimientoDto(
        Integer idMovimiento,
        String tipo,
        LocalDateTime fecha,
        Integer idUsuario,
        Integer idAutor,
        Integer idCompromiso,
        String detalle
) {

    public MovimientoDto conUsuario(Integer id) {
        Integer usuario = idUsuario == null || idUsuario <= 0 ? id : idUsuario;
        return new MovimientoDto(idMovimiento, tipo, fecha, usuario, idAutor, idCompromiso, detalle);
    }

    public MovimientoDto conCompromiso(Integer id) {
        Integer compromiso = idCompromiso == null || idCompromiso <= 0 ? id : idCompromiso;
        return new MovimientoDto(idMovimiento, tipo, fecha, idUsuario, idAutor, compromiso, detalle);
    }
}
