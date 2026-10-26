package com.itm.gestordeturnos.domain.model

import kotlinx.serialization.Serializable

enum class EstadoAsistencia {
    SIN_INICIAR,
    EN_TURNO,
    EN_DESCANSO,
    FINALIZADO
}

@Serializable
data class NotificacionIncumplimiento(
    val id: String = "",
    val empleadoId: String = "",
    val empleado: String = "",
    val tipo: String = "",
    val detalleHora: String = "",
    val fechaIso: String = "",
    val sucursal: String = "",
    val atendida: Boolean = false,
    val motivoAtencion: String = "",
    val horaAtencion: String = ""
) {
    val empleadoNombre: String get() = empleado
}

@Serializable
data class RegistroAsistencia(
    val id: String = "",
    val empleadoId: String = "",
    val empleado: String = "",
    val sucursal: String = "CENTRO",
    val fechaIso: String = "",
    val tipoTurno: String = "",
    val horarioProgramado: String = "",
    val horaEntrada: String = "-",
    val horaSalida: String = "-",
    val inicioDescanso: String = "-",
    val finDescanso: String = "-",
    val estado: String = "PENDIENTE",
    val estadoAsistencia: EstadoAsistencia = EstadoAsistencia.SIN_INICIAR,
    val tiempoEfectivoMinutos: Int = 0,
    val minutosTarde: Int = 0,
    val esDescanso: Boolean = false,
    val registradoPorSupervisor: Boolean = false,
    val motivoSupervisor: String = ""
)
