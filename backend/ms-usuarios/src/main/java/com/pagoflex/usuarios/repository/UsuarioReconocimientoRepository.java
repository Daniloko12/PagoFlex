package com.pagoflex.usuarios.repository;

import com.pagoflex.usuarios.model.UsuarioReconocimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioReconocimientoRepository extends JpaRepository<UsuarioReconocimiento, Integer> {

    List<UsuarioReconocimiento> findAllByOrderByIdAsc();

    boolean existsByIdUsuarioFinalAndIdReconocimiento(Integer idUsuarioFinal, Integer idReconocimiento);
}
