package com.pagoflex.usuarios.dto;

import java.time.LocalDateTime;

public record SolicitudCodigo(Integer idUsuario, String codigo, LocalDateTime ahora) {
}
