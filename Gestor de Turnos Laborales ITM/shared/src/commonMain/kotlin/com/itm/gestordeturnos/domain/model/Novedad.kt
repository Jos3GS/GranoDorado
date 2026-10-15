package com.itm.gestordeturnos.domain.model

data class Novedad(
    val id: String,
    val tipo: String,
    val empleado: String,
    val sucursal: String,
    val detalleTurno: String,
    val diferencia: String,
    val estado: String,
    val colorVorde: String
)
