package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract

/**
 * Caso de uso de dominio encargado de aplicar el bloqueo por "Incapacidad" en la malla semanal
 * cuando una novedad médica es revisada por administración.
 */
class AplicarIncapacidadEnTurnoUseCase(
    private val empleadoRepository: EmpleadoRepositoryContract,
    private val turnoRepository: TurnoRepositoryContract
) {
    suspend operator fun invoke(novedad: Novedad): Result<Unit> {
        val fecha = novedad.fechaReferencia.ifBlank {
            novedad.detalleTurno.split("·").lastOrNull()?.trim() ?: ""
        }
        if (fecha.isBlank()) return Result.success(Unit)

        val todosEmp = empleadoRepository.empleados.value
        val emp = todosEmp.find {
            (novedad.empleadoId.isNotBlank() && it.id == novedad.empleadoId) ||
            it.nombre.equals(novedad.empleado, ignoreCase = true) ||
            "${it.nombre} ${it.apellido}".trim().equals(novedad.empleado, ignoreCase = true)
        }

        val empId = emp?.id ?: novedad.empleadoId.ifBlank { "emp_1" }
        val empNombre = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: novedad.empleado.ifBlank { "Empleado" }
        val empSucursal = emp?.sucursal ?: novedad.sucursal.ifBlank { "CENTRO" }

        val todasAsigs = turnoRepository.asignaciones.value
        val asigExistente = todasAsigs.find {
            (it.empleadoId == empId || (it.empleadoNombre.isNotBlank() && it.empleadoNombre.equals(empNombre, ignoreCase = true))) &&
            it.fechaIso == fecha
        }

        val asigIncapacidad = if (asigExistente != null) {
            asigExistente.copy(
                tipoTurno = "Incapacidad",
                horario = "-",
                esDescanso = false,
                modificadoTrasPublicar = true
            )
        } else {
            AsignacionTurno(
                id = "${empId}_${fecha}",
                fechaIso = fecha,
                empleadoId = empId,
                empleadoNombre = empNombre,
                turnoId = "",
                tipoTurno = "Incapacidad",
                horario = "-",
                sucursalId = empSucursal,
                programacionId = "",
                esDescanso = false,
                estado = "PUBLICADA",
                modificadoTrasPublicar = false
            )
        }

        return turnoRepository.guardarAsignacion(asigIncapacidad)
    }
}
