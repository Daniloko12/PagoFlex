package com.pagoflex.usuarios.dto;

import com.pagoflex.usuarios.model.Usuario;
import java.util.List;

public record RegistroUsuario(Usuario usuario, String clave, List<MovimientoDto> movimientos) {
}
