package com.pagoflex.compromisos.repository;

import com.pagoflex.compromisos.model.Rubro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RubroRepository extends JpaRepository<Rubro, Integer> {

    List<Rubro> findAllByOrderByIdRubroAsc();
}
