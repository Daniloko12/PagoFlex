package com.pagoflex.pagos.repository;

import com.pagoflex.pagos.model.Tarjeta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Integer> {

    List<Tarjeta> findAllByOrderByIdTarjetaAsc();

    @Query("select coalesce(max(t.idTarjeta), 0) from Tarjeta t")
    Integer maximoId();
}
