package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

sealed interface ValidacionAsignacionResult {
    data object Valido : ValidacionAsignacionResult
    data class Invalido(val mensaje: String) : ValidacionAsignacionResult
}

/**
 * RN-15: Solo se asignan turnos a empleados en estado Activo y pertenecientes a la sucursal de la
 * programación, y no se crean asignaciones con fecha anterior a la fecha actual.
 * Asimismo, solo se permiten ajustes en la semana actual o en semanas futuras.
 */
class ValidarAsignacionTurnoUseCase {

    fun esSemanaModificable(
        lunesSemana: LocalDate,
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ): Boolean {
        val diasDesdeLunes = fechaActual.dayOfWeek.ordinal
        val lunesActual = fechaActual.minus(DatePeriod(days = diasDesdeLunes))
        return lunesSemana >= lunesActual
    }

    fun validarAsignacion(
        empleado: Empleado?,
        sucursalEmpleado: String,
        sucursalProgramacion: String,
        fechaAsignacion: LocalDate,
        lunesSemana: LocalDate = fechaAsignacion.minus(DatePeriod(days = fechaAsignacion.dayOfWeek.ordinal)),
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ): ValidacionAsignacionResult {
        // 1. Validar semana modificable (semana actual o futuras)
        val diasDesdeLunes = fechaActual.dayOfWeek.ordinal
        val lunesActual = fechaActual.minus(DatePeriod(days = diasDesdeLunes))

        if (lunesSemana < lunesActual) {
            return ValidacionAsignacionResult.Invalido(
                "Solo se permiten ajustes en la semana actual o en semanas futuras."
            )
        }

        // 2. Validar que la fecha de asignación no sea anterior a la fecha actual
        if (fechaAsignacion < fechaActual) {
            return ValidacionAsignacionResult.Invalido(
                "No se pueden crear ni ajustar asignaciones con fecha anterior a la fecha actual."
            )
        }

        // 3. Validar empleado existente
        if (empleado == null) {
            return ValidacionAsignacionResult.Invalido("El empleado no se encuentra registrado.")
        }

        // 4. Validar que el empleado esté en estado Activo
        val esActivo = empleado.estado == EstadoUsuario.ACTIVO ||
                empleado.estado.name.equals("ACTIVO", ignoreCase = true) ||
                empleado.estado.displayName.equals("Activo", ignoreCase = true)

        if (!esActivo) {
            return ValidacionAsignacionResult.Invalido("Solo se asignan turnos a empleados en estado Activo.")
        }

        // 5. Validar que el empleado pertenezca a la sucursal de la programación
        val sucursalEfectiva = if (sucursalEmpleado.isNotBlank()) sucursalEmpleado else empleado.sucursal
        val sucursalNormalizadaEmp = sucursalEfectiva.trim().replace(" ", "")
        val sucursalNormalizadaProg = sucursalProgramacion.trim().replace(" ", "")

        if (!sucursalNormalizadaEmp.equals(sucursalNormalizadaProg, ignoreCase = true)) {
            return ValidacionAsignacionResult.Invalido(
                "El empleado solo puede ser asignado a turnos de su sucursal ($sucursalEfectiva)."
            )
        }

        return ValidacionAsignacionResult.Valido
    }
}
