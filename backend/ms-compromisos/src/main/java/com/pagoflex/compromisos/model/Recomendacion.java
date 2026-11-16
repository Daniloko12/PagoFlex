package com.pagoflex.compromisos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "recomendacion")
public class Recomendacion {

    @Id
    private Integer idRecomendacion;

    @Column(nullable = false, length = 20, unique = true)
    private String numero;

    @Column(nullable = false)
    private Integer idUsuarioFinal;

    @Column(nullable = false, length = 120)
    private String nombreOrganizacion;

    @Column(length = 12)
    private String rutOrganizacion;

    @Column(nullable = false)
    private Integer idRubro;

    @Column(nullable = false)
    private Integer idComuna;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 20)
    private String estado;

    public Integer getIdRecomendacion() {
        return idRecomendacion;
    }

    public void setIdRecomendacion(Integer idRecomendacion) {
        this.idRecomendacion = idRecomendacion;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getIdUsuarioFinal() {
        return idUsuarioFinal;
    }

    public void setIdUsuarioFinal(Integer idUsuarioFinal) {
        this.idUsuarioFinal = idUsuarioFinal;
    }

    public String getNombreOrganizacion() {
        return nombreOrganizacion;
    }

    public void setNombreOrganizacion(String nombreOrganizacion) {
        this.nombreOrganizacion = nombreOrganizacion;
    }

    public String getRutOrganizacion() {
        return rutOrganizacion;
    }

    public void setRutOrganizacion(String rutOrganizacion) {
        this.rutOrganizacion = rutOrganizacion;
    }

    public Integer getIdRubro() {
        return idRubro;
    }

    public void setIdRubro(Integer idRubro) {
        this.idRubro = idRubro;
    }

    public Integer getIdComuna() {
        return idComuna;
    }

    public void setIdComuna(Integer idComuna) {
        this.idComuna = idComuna;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
