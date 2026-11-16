package com.pagoflex.compromisos.repository;

import com.pagoflex.compromisos.model.HistorialRecomendacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistorialRecomendacionRepository extends JpaRepository<HistorialRecomendacion, Integer> {

    List<HistorialRecomendacion> findAllByOrderByIdHistorialAsc();

    @Query("select coalesce(max(h.idHistorial), 0) from HistorialRecomendacion h")
    Integer maximoId();
}
