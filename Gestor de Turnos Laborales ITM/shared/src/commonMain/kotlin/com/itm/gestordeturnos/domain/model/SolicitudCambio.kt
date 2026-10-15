package com.itm.gestordeturnos.domain.model

data class SolicitudCambio(
    val id: String,
    val numeroSolicitud: String,
    val sucursal: String,
    val estadoResumen: String,
    val nombreEntrega: String,
    val tipoTurnoEntrega: String,
    val detalleTurnoEntrega: String,
    val nombreRecibe: String,
    val tipoTurnoRecibe: String,
    val detalleTurnoRecibe: String,
    val motivo: String,
    val validacionesOk: Boolean,
    val esRemitente: Boolean = false
)
