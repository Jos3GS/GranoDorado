package com.itm.gestordeturnos.domain.model

enum class CargoEmpleado(val displayName: String) {
    ADMINISTRADOR_GENERAL("ADMINISTRADOR GENERAL"),
    ADMINISTRADOR_SUCURSAL("ADMINISTRADOR DE SUCURSAL"),
    BARISTA("BARISTA"),
    COCINERO("COCINERO"),
    CAJERO("CAJERO")
}

enum class Sucursal(val displayName: String) {
    CENTRO("CENTRO"),
    NORTE("NORTE"),
    SUR("SUR")
}

enum class EstadoEmpleado(val displayName: String) {
    ACTIVO("ACTIVO"),
    INACTIVO("INACTIVO")
}

data class Empleado(
    val id: String,
    val nombre: String,
    val apellido: String = "",
    val documento: String = "",
    val correo: String = "",
    val telefono: String = "",
    val cargo: String,
    val sucursal: String,
    val fechaIngreso: String = "15/01/2024",
    val estado: String = "ACTIVO",
    val iniciales: String = ""
)
