package com.example.pagoflex.model

data class Rubro(
    val idRubro: Int,
    val nombre: String
)

data class EmpresaCliente(
    val idEmpresa: Int,
    val codigo: String,
    val razonSocial: String,
    val nombreFantasia: String,
    val rut: String,
    val idRubro: Int,
    val correoAtencion: String,
    val telefonoAtencion: String,
    val recargoAtraso: Int,
    val activa: Boolean = true
)
