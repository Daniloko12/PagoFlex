package com.pagoflex.usuarios.dto;

import java.time.LocalDateTime;

public record VerificacionCodigo(Integer idUsuario, String codigo, LocalDateTime ahora) {
}
