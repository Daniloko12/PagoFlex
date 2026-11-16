package com.pagoflex.usuarios.repository;

import com.pagoflex.usuarios.model.CodigoRecuperacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Integer> {

    List<CodigoRecuperacion> findByIdUsuarioAndUsadoFalse(Integer idUsuario);

    Optional<CodigoRecuperacion> findFirstByIdUsuarioAndUsadoFalseOrderByIdCodigoDesc(Integer idUsuario);
}
