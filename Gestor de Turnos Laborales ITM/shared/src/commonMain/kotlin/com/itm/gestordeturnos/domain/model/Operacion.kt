package com.itm.gestordeturnos.domain.model

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class Sucursal(
    val id: String = "",
    val nombre: String = "",
    val activa: Boolean = true
) {
    fun habilitar(): Sucursal = copy(activa = true)
    fun deshabilitar(): Sucursal = copy(activa = false)
}

@Serializable
data class Descanso(
    val id: String = "",
    val horaInicio: LocalTime = LocalTime(0, 0),
    val horaFin: LocalTime = LocalTime(0, 0)
) {
    fun validarIntervalo(): Boolean {
        return horaFin.toSecondOfDay() > horaInicio.toSecondOfDay()
    }

    fun duracionHoras(): Double {
        val segundos = horaFin.toSecondOfDay() - horaInicio.toSecondOfDay()
        return if (segundos > 0) segundos / 3600.0 else 0.0
    }
}

@Serializable
data class Turno(
    val id: String = "",
    val nombre: String = "",
    val horaInicio: LocalTime = LocalTime(0, 0),
    val horaFin: LocalTime = LocalTime(0, 0),
    val estado: EstadoTurno = EstadoTurno.ACTIVO,
    val descansos: List<Descanso> = emptyList(),
    val sucursalId: String = ""
) {
    fun calcularDuracionEfectiva(): Double {
        val segundosTotales = horaFin.toSecondOfDay() - horaInicio.toSecondOfDay()
        val segundosDescanso = descansos.sumOf { d ->
            val seg = d.horaFin.toSecondOfDay() - d.horaInicio.toSecondOfDay()
            if (seg > 0) seg else 0
        }
        val efectivos = segundosTotales - segundosDescanso
        return if (efectivos > 0) (efectivos / 3600.0) else 0.0
    }

    fun inactivar(): Turno = copy(estado = EstadoTurno.INACTIVO)

    fun agregarDescanso(d: Descanso): Turno = copy(descansos = descansos + d)
}
