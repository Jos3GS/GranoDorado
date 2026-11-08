package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.TipoCeldaMalla
import com.itm.gestordeturnos.domain.model.TurnoCelda
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
 *
 * RN-16: Un empleado no puede tener dos turnos asignados el mismo día ni turnos que se solapen entre sí.
 * Ningún empleado puede superar 42 horas efectivas de trabajo en una misma semana.
 *
 * RN-17: El sistema rechaza la asignación cuando el empleado tiene registrado ese día un descanso,
 * vacaciones, una incapacidad o un permiso aprobado. Al aprobar un permiso, el empleado queda como no
 * disponible esos días y el sistema no admite nuevas asignaciones en ellos.
 */
class ValidarAsignacionTurnoUseCase(
    private val verificarCoberturaUseCase: VerificarCoberturaDiariaUseCase = VerificarCoberturaDiariaUseCase()
) {

    companion object {
        const val MAXIMO_HORAS_SEMANALES = 42
        const val MAXIMO_MINUTOS_SEMANALES = 42 * 60 // 2520 minutos
    }

    fun esSemanaModificable(
        lunesSemana: LocalDate,
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ): Boolean {
        val diasDesdeLunes = fechaActual.dayOfWeek.ordinal
        val lunesActual = fechaActual.minus(DatePeriod(days = diasDesdeLunes))
        return lunesSemana >= lunesActual
    }

    fun validarMaximoHorasSemanales(turnosSemana: List<TurnoCelda>): ValidacionAsignacionResult {
        val minutosTotales = verificarCoberturaUseCase.calcularTotalMinutosEfectivos(turnosSemana)
        if (minutosTotales > MAXIMO_MINUTOS_SEMANALES) {
            val horas = minutosTotales / 60
            val mins = minutosTotales % 60
            val duracionStr = if (mins > 0) "$horas h ${mins.toString().padStart(2, '0')} min" else "$horas h"
            return ValidacionAsignacionResult.Invalido(
                "Ningún empleado puede superar 42 horas efectivas de trabajo en una misma semana (total resultante: $duracionStr)."
            )
        }
        return ValidacionAsignacionResult.Valido
    }

    fun validarNoDobleTurnoMismoDia(asignacionesEnFecha: List<AsignacionTurno>): ValidacionAsignacionResult {
        val turnosLaborales = asignacionesEnFecha.filter {
            it.tipoTurno.isNotBlank() &&
            !it.esDescanso &&
            !it.tipoTurno.equals("Descanso", ignoreCase = true)
        }
        if (turnosLaborales.size > 1) {
            return ValidacionAsignacionResult.Invalido(
                "Un empleado no puede tener dos turnos asignados el mismo día ni turnos que se solapen entre sí."
            )
        }
        return ValidacionAsignacionResult.Valido
    }

    /**
     * RN-17: El sistema rechaza la asignación cuando el empleado tiene registrado ese día
     * un descanso, vacaciones, una incapacidad o un permiso aprobado.
     */
    fun validarDisponibilidadPorAusenciaODescanso(
        celdaActual: TurnoCelda?,
        asignacionExistente: AsignacionTurno? = null,
        esAsignacionDescanso: Boolean = false,
        permitirModificarDescanso: Boolean = false
    ): ValidacionAsignacionResult {
        val bloquearDescanso = !esAsignacionDescanso && !permitirModificarDescanso

        // 1. Validar por celda actual en la malla semanal
        if (celdaActual != null) {
            when {
                celdaActual.tipoCelda == TipoCeldaMalla.INCAPACIDAD ||
                celdaActual.tipo.contains("incapacidad", ignoreCase = true) -> {
                    return ValidacionAsignacionResult.Invalido(
                        "No se puede asignar turno: el empleado tiene registrada una incapacidad médica para este día."
                    )
                }
                celdaActual.tipoCelda == TipoCeldaMalla.PERMISO ||
                celdaActual.tipo.contains("permiso", ignoreCase = true) -> {
                    return ValidacionAsignacionResult.Invalido(
                        "No se puede asignar turno: el empleado tiene un permiso aprobado para este día."
                    )
                }
                celdaActual.tipoCelda == TipoCeldaMalla.VACACIONES ||
                celdaActual.tipo.contains("vacaciones", ignoreCase = true) -> {
                    return ValidacionAsignacionResult.Invalido(
                        "No se puede asignar turno: el empleado tiene vacaciones registradas para este día."
                    )
                }
                bloquearDescanso && (
                    celdaActual.tipoCelda == TipoCeldaMalla.DESCANSO ||
                    celdaActual.esDescanso ||
                    celdaActual.tipo.equals("Descanso", ignoreCase = true)
                ) -> {
                    return ValidacionAsignacionResult.Invalido(
                        "No se puede asignar turno: el empleado tiene programado un descanso para este día."
                    )
                }
            }
        }

        // 2. Validar por asignación ya guardada en el repositorio
        if (asignacionExistente != null) {
            when {
                asignacionExistente.tipoTurno.contains("incapacidad", ignoreCase = true) -> {
                    return ValidacionAsignacionResult.Invalido(
                        "No se puede asignar turno: el empleado tiene registrada una incapacidad médica para este día."
                    )
                }
                asignacionExistente.tipoTurno.contains("permiso", ignoreCase = true) -> {
                    return ValidacionAsignacionResult.Invalido(
                        "No se puede asignar turno: el empleado tiene un permiso aprobado para este día."
                    )
                }
                asignacionExistente.tipoTurno.contains("vacaciones", ignoreCase = true) -> {
                    return ValidacionAsignacionResult.Invalido(
                        "No se puede asignar turno: el empleado tiene vacaciones registradas para este día."
                    )
                }
                bloquearDescanso && (
                    asignacionExistente.esDescanso ||
                    asignacionExistente.tipoTurno.equals("Descanso", ignoreCase = true)
                ) -> {
                    return ValidacionAsignacionResult.Invalido(
                        "No se puede asignar turno: el empleado tiene programado un descanso para este día."
                    )
                }
            }
        }

        return ValidacionAsignacionResult.Valido
    }

    /**
     * Valida las reglas integrales de la semana al momento de publicar la malla:
     * - Todos los días de cada empleado deben estar asignados (turno, descanso o novedad).
     * - Cada empleado debe contar con sus días de descanso asignados (mínimo 2 días libres/ausencia justificada).
     * - Ningún empleado puede superar las 42 horas efectivas semanales.
     */
    fun validarPublicacionMalla(filas: List<com.itm.gestordeturnos.domain.model.FilaMalla>): ValidacionAsignacionResult {
        if (filas.isEmpty()) {
            return ValidacionAsignacionResult.Invalido(
                "No hay empleados activos en esta sucursal para publicar la programación semanal."
            )
        }

        for (fila in filas) {
            val sinAsignar = fila.turnos.count { it.tipoCelda == TipoCeldaMalla.SIN_ASIGNAR }
            val descansos = fila.turnos.count { it.tipoCelda == TipoCeldaMalla.DESCANSO || it.esDescanso }
            val ausencias = fila.turnos.count {
                it.tipoCelda in listOf(
                    TipoCeldaMalla.INCAPACIDAD,
                    TipoCeldaMalla.PERMISO,
                    TipoCeldaMalla.VACACIONES
                )
            }

            if (sinAsignar > 0) {
                return ValidacionAsignacionResult.Invalido(
                    "No se puede publicar la semana: ${fila.nombre} tiene $sinAsignar día(s) sin asignar. Asígnale sus turnos y días de descanso antes de publicar."
                )
            }

            if (descansos + ausencias < 2) {
                return ValidacionAsignacionResult.Invalido(
                    "No se puede publicar la semana: ${fila.nombre} debe tener al menos 2 días de descanso asignados en la semana."
                )
            }

            val validacionHoras = validarMaximoHorasSemanales(fila.turnos)
            if (validacionHoras is ValidacionAsignacionResult.Invalido) {
                return ValidacionAsignacionResult.Invalido(
                    "No se puede publicar la semana (${fila.nombre}): ${validacionHoras.mensaje}"
                )
            }
        }

        return ValidacionAsignacionResult.Valido
    }

    fun validarAsignacion(
        empleado: Empleado?,
        sucursalEmpleado: String,
        sucursalProgramacion: String,
        fechaAsignacion: LocalDate,
        lunesSemana: LocalDate = fechaAsignacion.minus(DatePeriod(days = fechaAsignacion.dayOfWeek.ordinal)),
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        turnosSemanaActuales: List<TurnoCelda>? = null,
        nuevoTurno: TurnoCelda? = null,
        diaIndex: Int? = null,
        todasAsignaciones: List<AsignacionTurno>? = null,
        permitirModificarDescanso: Boolean = false,
        validarLimiteSemanal: Boolean = true
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

        // 6. RN-16: Un empleado no puede tener dos turnos asignados el mismo día
        if (todasAsignaciones != null && nuevoTurno != null && nuevoTurno.tipoCelda == TipoCeldaMalla.TURNO) {
            val fechaIso = fechaAsignacion.toString()
            val tieneOtroTurnoMismoDia = todasAsignaciones.any {
                it.empleadoId == empleado.id &&
                it.fechaIso == fechaIso &&
                it.tipoTurno.isNotBlank() &&
                !it.esDescanso &&
                !it.tipoTurno.equals("Descanso", ignoreCase = true) &&
                it.sucursalId.isNotBlank() &&
                !it.sucursalId.trim().replace(" ", "").equals(sucursalNormalizadaProg, ignoreCase = true)
            }
            if (tieneOtroTurnoMismoDia) {
                return ValidacionAsignacionResult.Invalido(
                    "Un empleado no puede tener dos turnos asignados el mismo día ni turnos que se solapen entre sí."
                )
            }
        }

        // 7. RN-16: Ningún empleado puede superar 42 horas efectivas de trabajo en una misma semana
        if (validarLimiteSemanal && turnosSemanaActuales != null && nuevoTurno != null && diaIndex != null && nuevoTurno.tipoCelda == TipoCeldaMalla.TURNO) {
            val simulados = turnosSemanaActuales.toMutableList()
            if (diaIndex in simulados.indices) {
                simulados[diaIndex] = nuevoTurno
            } else {
                simulados.add(nuevoTurno)
            }
            val validacionHoras = validarMaximoHorasSemanales(simulados)
            if (validacionHoras is ValidacionAsignacionResult.Invalido) {
                return validacionHoras
            }
        }

        // 8. RN-17: El sistema rechaza la asignación cuando el empleado tiene registrado ese día
        // vacaciones, una incapacidad o un permiso aprobado (y descanso solo si no se permite su edición).
        val esDescanso = nuevoTurno?.esDescanso == true || nuevoTurno?.tipoCelda == TipoCeldaMalla.DESCANSO
        val celdaActual = if (turnosSemanaActuales != null && diaIndex != null && diaIndex in turnosSemanaActuales.indices) {
            turnosSemanaActuales[diaIndex]
        } else null

        val asigExistente = if (todasAsignaciones != null && empleado != null) {
            val fechaIso = fechaAsignacion.toString()
            todasAsignaciones.find {
                it.empleadoId == empleado.id && it.fechaIso == fechaIso
            }
        } else null

        val resDisponibilidad = validarDisponibilidadPorAusenciaODescanso(
            celdaActual = celdaActual,
            asignacionExistente = asigExistente,
            esAsignacionDescanso = esDescanso,
            permitirModificarDescanso = permitirModificarDescanso
        )
        if (resDisponibilidad is ValidacionAsignacionResult.Invalido) {
            return resDisponibilidad
        }

        return ValidacionAsignacionResult.Valido
    }
}
