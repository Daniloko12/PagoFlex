package com.pagoflex.compromisos.repository;

import com.pagoflex.compromisos.model.ContactoRecomendacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRecomendacionRepository extends JpaRepository<ContactoRecomendacion, Integer> {

    List<ContactoRecomendacion> findAllByOrderByIdRecomendacionAsc();
}
