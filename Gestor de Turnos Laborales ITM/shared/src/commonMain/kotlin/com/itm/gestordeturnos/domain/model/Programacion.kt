package com.itm.gestordeturnos.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class AsignacionTurno(
    val id: String = "",
    val fecha: LocalDate? = null,
    val empleadoId: String = "",
    val turnoId: String = "",
    val turno: Turno? = null,
    val sucursalId: String = "",
    val programacionId: String = "",
    val marcas: List<MarcaAsistencia> = emptyList(),
    val novedades: List<Novedad> = emptyList(),
    val incumplimiento: Incumplimiento? = null,
    val esDescanso: Boolean = false
) {
    fun validarDisponibilidad(): Boolean {
        return !esDescanso && empleadoId.isNotBlank()
    }

    fun calcularTiempoEfectivo(): Double {
        return turno?.calcularDuracionEfectiva() ?: 0.0
    }
}

@Serializable
data class ProgramacionSemanal(
    val id: String = "",
    val fechaInicioSemana: LocalDate? = null,
    val estado: EstadoProgramacion = EstadoProgramacion.BORRADOR,
    val version: Int = 1,
    val sucursalId: String = "",
    val asignaciones: List<AsignacionTurno> = emptyList()
) {
    fun publicar(): ProgramacionSemanal = copy(estado = EstadoProgramacion.PUBLICADA)

    fun generarNuevaVersion(): ProgramacionSemanal = copy(
        version = version + 1,
        estado = EstadoProgramacion.BORRADOR
    )
}
