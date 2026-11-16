package com.pagoflex.usuarios.repository;

import com.pagoflex.usuarios.model.Reconocimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconocimientoRepository extends JpaRepository<Reconocimiento, Integer> {

    List<Reconocimiento> findAllByOrderByIdReconocimientoAsc();
}
