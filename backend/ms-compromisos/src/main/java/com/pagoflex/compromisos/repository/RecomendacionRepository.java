package com.pagoflex.compromisos.repository;

import com.pagoflex.compromisos.model.Recomendacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Integer> {

    List<Recomendacion> findAllByOrderByIdRecomendacionAsc();

    @Query("select coalesce(max(r.idRecomendacion), 0) from Recomendacion r")
    Integer maximoId();
}
