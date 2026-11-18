package com.pagoflex.compromisos.repository;

import com.pagoflex.compromisos.model.Reporte;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    List<Reporte> findAllByOrderByIdReporteAsc();

    @Query("select coalesce(max(r.idReporte), 0) from Reporte r")
    Integer maximoId();
}
