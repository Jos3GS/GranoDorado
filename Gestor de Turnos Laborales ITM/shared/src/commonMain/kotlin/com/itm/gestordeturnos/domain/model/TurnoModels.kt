package com.itm.gestordeturnos.domain.model

import kotlin.uuid.Uuid

data class TurnoAsignado(
    val id: String,
    val diaSemana: String,
    val numeroDia: String,
    val tipoTurno: String?,
    val horario: String?,
    val sucursal: String?,
    val duracion: String?,
    val etiquetaModificado: String? = null,
    val esDescanso: Boolean = false,
    val sinAsignar: Boolean = false
)

data class RangoDescanso(
    val id: String = Uuid.random().toString(),
    val horaInicio: String = "",
    val horaFin: String = ""
)

data class TurnoTemplate(
    val id: String,
    val nombre: String,
    val horario: String,
    val descansos: String,
    val horasEfectivas: String,
    val estado: String,
    val asignaciones: Int,
    val horaInicio: String = "",
    val horaFin: String = "",
    val listaDescansos: List<RangoDescanso> = emptyList()
)

data class TurnoCelda(
    val tipo: String,
    val horario: String,
    val esDescanso: Boolean = false
)

data class FilaMalla(
    val idEmpleado: String,
    val nombre: String,
    val cargo: String,
    val turnos: List<TurnoCelda>,
    val totalHoras: String
)

data class TurnoSeleccionable(
    val id: String,
    val descripcion: String,
    val horario: String
)

data class TurnoCompaneroSeleccionable(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val horario: String
)
