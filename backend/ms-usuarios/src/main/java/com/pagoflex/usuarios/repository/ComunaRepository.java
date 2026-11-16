package com.pagoflex.usuarios.repository;

import com.pagoflex.usuarios.model.Comuna;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunaRepository extends JpaRepository<Comuna, Integer> {

    List<Comuna> findAllByOrderByNombreAsc();
}
