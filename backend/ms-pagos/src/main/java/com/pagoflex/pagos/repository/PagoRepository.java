package com.pagoflex.pagos.repository;

import com.pagoflex.pagos.model.Pago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findAllByOrderByIdPagoAsc();

    boolean existsByIdCompromiso(Integer idCompromiso);

    @Query("select coalesce(max(p.idPago), 0) from Pago p")
    Integer maximoId();
}
