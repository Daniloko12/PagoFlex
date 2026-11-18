package com.pagoflex.pagos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarjeta")
public class Tarjeta {

    @Id
    private Integer idTarjeta;

    @Column(nullable = false)
    private Integer idUsuario;

    @Column(nullable = false, length = 30)
    private String medioPago;

    @Column(nullable = false, length = 20)
    private String marca;

    @Column(nullable = false, length = 4)
    private String ultimos4;

    @Column(nullable = false, length = 80)
    private String titular;

    @Column(nullable = false, length = 7)
    private String vencimiento;

    @Column(nullable = false)
    private Boolean guardada = true;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    public Integer getIdTarjeta() {
        return idTarjeta;
    }

    public void setIdTarjeta(Integer idTarjeta) {
        this.idTarjeta = idTarjeta;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getUltimos4() {
        return ultimos4;
    }

    public void setUltimos4(String ultimos4) {
        this.ultimos4 = ultimos4;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getVencimiento() {
        return vencimiento;
    }

    public void setVencimiento(String vencimiento) {
        this.vencimiento = vencimiento;
    }

    public Boolean getGuardada() {
        return guardada;
    }

    public void setGuardada(Boolean guardada) {
        this.guardada = guardada;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
