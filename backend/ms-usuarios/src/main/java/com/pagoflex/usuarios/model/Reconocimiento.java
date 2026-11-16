package com.pagoflex.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reconocimiento")
public class Reconocimiento {

    @Id
    private Integer idReconocimiento;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String descripcion;

    public Integer getIdReconocimiento() {
        return idReconocimiento;
    }

    public void setIdReconocimiento(Integer idReconocimiento) {
        this.idReconocimiento = idReconocimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
