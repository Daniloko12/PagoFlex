package com.pagoflex.compromisos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "contacto_recomendacion")
public class ContactoRecomendacion {

    @Id
    private Integer idRecomendacion;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 120)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private Boolean autorizaContacto = false;

    public Integer getIdRecomendacion() {
        return idRecomendacion;
    }

    public void setIdRecomendacion(Integer idRecomendacion) {
        this.idRecomendacion = idRecomendacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getAutorizaContacto() {
        return autorizaContacto;
    }

    public void setAutorizaContacto(Boolean autorizaContacto) {
        this.autorizaContacto = autorizaContacto;
    }
}
