package com.pagoflex.movimientos.repository;

import com.pagoflex.movimientos.model.Movimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {

    List<Movimiento> findAllByOrderByIdMovimientoAsc();

    List<Movimiento> findByIdUsuarioOrderByFechaDescIdMovimientoDesc(Integer idUsuario);

    List<Movimiento> findByIdCompromisoOrderByFechaAscIdMovimientoAsc(Integer idCompromiso);
}
