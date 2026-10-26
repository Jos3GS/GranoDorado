package com.itm.gestordeturnos.domain.model

data class UsuarioActual(
    val id: String = "",
    val correo: String = "",
    val sucursal: String = "",
    val nombre: String,
    val iniciales: String,
    val cargo: String,
    val rol: RolUsuario
)
