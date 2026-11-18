package com.pagoflex.compromisos.repository;

import com.pagoflex.compromisos.model.Anulacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnulacionRepository extends JpaRepository<Anulacion, Integer> {

    List<Anulacion> findAllByOrderByIdCompromisoAsc();
}
