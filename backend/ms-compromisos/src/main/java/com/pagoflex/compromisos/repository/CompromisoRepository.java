package com.pagoflex.compromisos.repository;

import com.pagoflex.compromisos.model.Compromiso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompromisoRepository extends JpaRepository<Compromiso, Integer> {

    List<Compromiso> findAllByOrderByIdCompromisoAsc();

    @Query("select coalesce(max(c.idCompromiso), 0) from Compromiso c")
    Integer maximoId();
}
