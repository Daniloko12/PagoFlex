package com.pagoflex.compromisos.repository;

import com.pagoflex.compromisos.model.EmpresaCliente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaClienteRepository extends JpaRepository<EmpresaCliente, Integer> {

    List<EmpresaCliente> findAllByOrderByIdEmpresaAsc();
}
