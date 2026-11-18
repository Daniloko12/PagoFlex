package com.pagoflex.compromisos.service;

import com.pagoflex.compromisos.model.EmpresaCliente;
import com.pagoflex.compromisos.model.Rubro;
import com.pagoflex.compromisos.repository.EmpresaClienteRepository;
import com.pagoflex.compromisos.repository.RubroRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizacionService {

    private final EmpresaClienteRepository empresas;
    private final RubroRepository rubros;

    public OrganizacionService(EmpresaClienteRepository empresas, RubroRepository rubros) {
        this.empresas = empresas;
        this.rubros = rubros;
    }

    @Transactional(readOnly = true)
    public List<EmpresaCliente> empresas() {
        return empresas.findAllByOrderByIdEmpresaAsc();
    }

    @Transactional(readOnly = true)
    public List<Rubro> rubros() {
        return rubros.findAllByOrderByIdRubroAsc();
    }
}
