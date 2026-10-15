package com.itm.gestordeturnos.domain.model

enum class EstadoAsistencia {
    SIN_INICIAR,
    EN_TURNO,
    EN_DESCANSO,
    FINALIZADO
}

data class NotificacionIncumplimiento(
    val id: String,
    val empleado: String,
    val tipo: String,
    val detalleHora: String
)

data class RegistroAsistencia(
    val id: String,
    val empleado: String,
    val sucursal: String,
    val horaEntrada: String,
    val horaSalida: String,
    val estado: String
)
