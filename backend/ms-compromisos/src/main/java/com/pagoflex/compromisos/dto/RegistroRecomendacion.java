package com.pagoflex.compromisos.dto;

import com.pagoflex.compromisos.model.ContactoRecomendacion;
import com.pagoflex.compromisos.model.Recomendacion;

public record RegistroRecomendacion(Recomendacion recomendacion, ContactoRecomendacion contacto) {
}
