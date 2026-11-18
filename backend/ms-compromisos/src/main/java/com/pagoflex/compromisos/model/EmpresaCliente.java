package com.pagoflex.compromisos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresa_cliente")
public class EmpresaCliente {

    @Id
    private Integer idEmpresa;

    @Column(nullable = false, length = 10, unique = true)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String razonSocial;

    @Column(nullable = false, length = 80)
    private String nombreFantasia;

    @Column(nullable = false, length = 12)
    private String rut;

    @Column(nullable = false)
    private Integer idRubro;

    @Column(nullable = false, length = 120)
    private String correoAtencion;

    @Column(nullable = false, length = 20)
    private String telefonoAtencion;

    @Column(nullable = false)
    private Integer recargoAtraso = 0;

    @Column(nullable = false)
    private Boolean activa = true;

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreFantasia() {
        return nombreFantasia;
    }

    public void setNombreFantasia(String nombreFantasia) {
        this.nombreFantasia = nombreFantasia;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public Integer getIdRubro() {
        return idRubro;
    }

    public void setIdRubro(Integer idRubro) {
        this.idRubro = idRubro;
    }

    public String getCorreoAtencion() {
        return correoAtencion;
    }

    public void setCorreoAtencion(String correoAtencion) {
        this.correoAtencion = correoAtencion;
    }

    public String getTelefonoAtencion() {
        return telefonoAtencion;
    }

    public void setTelefonoAtencion(String telefonoAtencion) {
        this.telefonoAtencion = telefonoAtencion;
    }

    public Integer getRecargoAtraso() {
        return recargoAtraso;
    }

    public void setRecargoAtraso(Integer recargoAtraso) {
        this.recargoAtraso = recargoAtraso;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }
}
