package com.pagoflex.compromisos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "compromiso")
public class Compromiso {

    @Id
    private Integer idCompromiso;

    @Column(nullable = false, length = 20, unique = true)
    private String folio;

    @Column(nullable = false)
    private Integer idUsuarioFinal;

    @Column(nullable = false)
    private Integer idEmpresa;

    @Column(nullable = false, length = 120)
    private String concepto;

    private Integer numeroCuota;

    private Integer totalCuotas;

    @Column(nullable = false)
    private Integer monto;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private Integer recargoAplicado = 0;

    @Column(nullable = false)
    private LocalDateTime fechaCambioEstado;

    private Integer idAdminRegistro;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    public Integer getIdCompromiso() {
        return idCompromiso;
    }

    public void setIdCompromiso(Integer idCompromiso) {
        this.idCompromiso = idCompromiso;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Integer getIdUsuarioFinal() {
        return idUsuarioFinal;
    }

    public void setIdUsuarioFinal(Integer idUsuarioFinal) {
        this.idUsuarioFinal = idUsuarioFinal;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Integer getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public Integer getTotalCuotas() {
        return totalCuotas;
    }

    public void setTotalCuotas(Integer totalCuotas) {
        this.totalCuotas = totalCuotas;
    }

    public Integer getMonto() {
        return monto;
    }

    public void setMonto(Integer monto) {
        this.monto = monto;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getRecargoAplicado() {
        return recargoAplicado;
    }

    public void setRecargoAplicado(Integer recargoAplicado) {
        this.recargoAplicado = recargoAplicado;
    }

    public LocalDateTime getFechaCambioEstado() {
        return fechaCambioEstado;
    }

    public void setFechaCambioEstado(LocalDateTime fechaCambioEstado) {
        this.fechaCambioEstado = fechaCambioEstado;
    }

    public Integer getIdAdminRegistro() {
        return idAdminRegistro;
    }

    public void setIdAdminRegistro(Integer idAdminRegistro) {
        this.idAdminRegistro = idAdminRegistro;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
