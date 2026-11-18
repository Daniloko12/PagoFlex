package com.pagoflex.usuarios.dto;

import com.pagoflex.usuarios.model.Usuario;
import java.util.List;

public record ActualizacionUsuario(Usuario usuario, List<MovimientoDto> movimientos) {
}
