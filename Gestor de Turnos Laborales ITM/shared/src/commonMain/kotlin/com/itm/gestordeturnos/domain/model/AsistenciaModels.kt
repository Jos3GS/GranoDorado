package com.itm.gestordeturnos.domain.model

import kotlinx.serialization.Serializable

@Serializable
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
data class PausaDescanso(
    val id: String = "",
    val horaInicio: String = "-",
    val horaFin: String = "-"
)

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
    val descansos: List<PausaDescanso> = emptyList(),
    val estado: String = "PENDIENTE",
    val estadoAsistencia: EstadoAsistencia = EstadoAsistencia.SIN_INICIAR,
    val tiempoEfectivoMinutos: Int = 0,
    val minutosTarde: Int = 0,
    val esDescanso: Boolean = false,
    val registradoPorSupervisor: Boolean = false,
    val motivoSupervisor: String = "",
    // RN-28: Trazabilidad individual de origen por marca para garantizar inmutabilidad
    val entradaPorSupervisor: Boolean = false,
    val salidaPorSupervisor: Boolean = false,
    val inicioDescansoPorSupervisor: Boolean = false,
    val finDescansoPorSupervisor: Boolean = false,
    val supervisorId: String = "",
    val supervisorNombre: String = ""
) {
    val listaPausas: List<PausaDescanso>
        get() = if (descansos.isNotEmpty()) {
            descansos
        } else if (inicioDescanso != "-") {
            listOf(PausaDescanso(id = "p_1", horaInicio = inicioDescanso, horaFin = finDescanso))
        } else {
            emptyList()
        }

    fun fueRegistradaPorSupervisor(tipoMarca: String): Boolean {
        val especifico = when (tipoMarca.trim().uppercase()) {
            "ENTRADA" -> entradaPorSupervisor
            "SALIDA" -> salidaPorSupervisor
            "INICIO_DESCANSO" -> inicioDescansoPorSupervisor
            "FIN_DESCANSO" -> finDescansoPorSupervisor
            else -> false
        }
        if (especifico) return true
        if (registradoPorSupervisor && !entradaPorSupervisor && !salidaPorSupervisor && !inicioDescansoPorSupervisor && !finDescansoPorSupervisor) {
            return true
        }
        return false
    }

    fun fueRegistradaPorEmpleado(tipoMarca: String): Boolean {
        val tieneMarca = when (tipoMarca.trim().uppercase()) {
            "ENTRADA" -> horaEntrada != "-" && horaEntrada != "--:--"
            "SALIDA" -> horaSalida != "-" && horaSalida != "--:--"
            "INICIO_DESCANSO" -> inicioDescanso != "-" && inicioDescanso != "--:--"
            "FIN_DESCANSO" -> finDescanso != "-" && finDescanso != "--:--"
            else -> false
        }
        return tieneMarca && !fueRegistradaPorSupervisor(tipoMarca)
    }
}
