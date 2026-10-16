package com.itm.gestordeturnos.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class EstadoTurno(val displayName: String) {
    ACTIVO("Activo"),
    INACTIVO("Inactivo")
}

@Serializable
enum class EstadoEmpleado(val displayName: String) {
    DISPONIBLE("Disponible"),
    DESCANSO("Descanso"),
    VACACIONES("Vacaciones"),
    PERMISO("Permiso"),
    INCAPACITADO("Incapacitado"),
    INACTIVO("Inactivo"),
    ACTIVO("Activo") // Alias de conveniencia para compatibilidad UI
}

@Serializable
enum class Cargo(val displayName: String) {
    BARISTA("Barista"),
    CAJERO("Cajero"),
    SUPERVISOR("Supervisor"),
    ADMINISTRADOR_GENERAL("Administrador General"),
    ADMINISTRADOR_SUCURSAL("Administrador de Sucursal"),
    COCINERO("Cocinero")
}

@Serializable
enum class TipoAdministrador(val displayName: String) {
    ADMINISTRADOR_GENERAL("Administrador General"),
    ADMINISTRADOR_SUCURSAL("Administrador de Sucursal")
}

@Serializable
enum class EstadoUsuario(val displayName: String) {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    BLOQUEADO("Bloqueado")
}

@Serializable
enum class EstadoProgramacion(val displayName: String) {
    BORRADOR("Borrador"),
    PUBLICADA("Publicada")
}

@Serializable
enum class EstadoSolicitudCambio(val displayName: String) {
    PENDIENTE("Pendiente"),
    ACEPTADA_COMPANERO("Aceptada por Compañero"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    VENCIDA("Vencida")
}

@Serializable
enum class TipoAusencia(val displayName: String) {
    DESCANSO("Descanso"),
    VACACIONES("Vacaciones"),
    INCAPACITADO("Incapacidad"),
    PERMISO("Permiso")
}

@Serializable
enum class EstadoNovedad(val displayName: String) {
    REPORTADA("Reportada"),
    REVISADA("Revisada")
}

@Serializable
enum class TipoMarca(val displayName: String) {
    INICIO_TURNO("Inicio de Turno"),
    SALIDA_DESCANSO("Salida a Descanso"),
    REGRESO_DESCANSO("Regreso de Descanso"),
    FIN_TURNO("Fin de Turno")
}

@Serializable
enum class EstadoIncumplimiento(val displayName: String) {
    PENDIENTE("Pendiente"),
    ATENDIDO("Atendido")
}
