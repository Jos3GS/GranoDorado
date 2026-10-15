package com.itm.gestordeturnos.domain.model

data class UsuarioActual(
    val nombre: String,
    val iniciales: String,
    val cargo: String,
    val rol: RolUsuario
)
