package com.pagoflex.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_reconocimiento")
public class UsuarioReconocimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer idUsuarioFinal;

    @Column(nullable = false)
    private Integer idReconocimiento;

    @Column(nullable = false)
    private LocalDateTime fechaObtencion;

    @Column(nullable = false, length = 255)
    private String motivo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdUsuarioFinal() {
        return idUsuarioFinal;
    }

    public void setIdUsuarioFinal(Integer idUsuarioFinal) {
        this.idUsuarioFinal = idUsuarioFinal;
    }

    public Integer getIdReconocimiento() {
        return idReconocimiento;
    }

    public void setIdReconocimiento(Integer idReconocimiento) {
        this.idReconocimiento = idReconocimiento;
    }

    public LocalDateTime getFechaObtencion() {
        return fechaObtencion;
    }

    public void setFechaObtencion(LocalDateTime fechaObtencion) {
        this.fechaObtencion = fechaObtencion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
