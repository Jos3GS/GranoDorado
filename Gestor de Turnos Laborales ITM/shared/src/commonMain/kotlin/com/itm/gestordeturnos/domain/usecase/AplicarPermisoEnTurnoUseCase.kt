package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Ausencia
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * RN-17: Al aprobar un permiso, el empleado queda como no disponible esos días
 * y el sistema no admite nuevas asignaciones en ellos.
 */
class AplicarPermisoEnTurnoUseCase(
    private val empleadoRepository: EmpleadoRepositoryContract,
    private val turnoRepository: TurnoRepositoryContract
) {
    suspend operator fun invoke(novedad: Novedad): Result<Unit> {
        val fecha = novedad.fechaReferencia.ifBlank {
            novedad.detalleTurno.split("·").lastOrNull()?.trim() ?: ""
        }
        if (fecha.isBlank()) return Result.success(Unit)

        return aplicarBloqueoPermiso(
            empleadoId = novedad.empleadoId,
            empleadoNombre = novedad.empleado,
            sucursal = novedad.sucursal,
            fechaIso = fecha
        )
    }

    suspend operator fun invoke(ausencia: Ausencia): Result<Unit> {
        val fInicio = ausencia.fechaInicio ?: return Result.success(Unit)
        val fFin = ausencia.fechaFin ?: fInicio

        val emp = empleadoRepository.empleados.value.find { it.id == ausencia.empleadoId }
        val empNombre = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: "Empleado"
        val sucursal = emp?.sucursal ?: "CENTRO"

        var curr = fInicio
        while (curr <= fFin) {
            aplicarBloqueoPermiso(
                empleadoId = ausencia.empleadoId,
                empleadoNombre = empNombre,
                sucursal = sucursal,
                fechaIso = curr.toString()
            )
            curr = curr.plus(DatePeriod(days = 1))
        }
        return Result.success(Unit)
    }

    suspend fun aplicarBloqueoPermiso(
        empleadoId: String,
        empleadoNombre: String,
        sucursal: String,
        fechaIso: String
    ): Result<Unit> {
        val todosEmp = empleadoRepository.empleados.value
        val emp = todosEmp.find {
            (empleadoId.isNotBlank() && it.id == empleadoId) ||
            it.nombre.equals(empleadoNombre, ignoreCase = true) ||
            "${it.nombre} ${it.apellido}".trim().equals(empleadoNombre, ignoreCase = true)
        }

        val empIdEfectivo = emp?.id ?: empleadoId.ifBlank { "emp_1" }
        val empNombreEfectivo = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: empleadoNombre.ifBlank { "Empleado" }
        val empSucursal = emp?.sucursal ?: sucursal.ifBlank { "CENTRO" }

        val todasAsigs = turnoRepository.asignaciones.value
        val asigExistente = todasAsigs.find {
            (it.empleadoId == empIdEfectivo || (it.empleadoNombre.isNotBlank() && it.empleadoNombre.equals(empNombreEfectivo, ignoreCase = true))) &&
            it.fechaIso == fechaIso
        }

        val asigPermiso = if (asigExistente != null) {
            asigExistente.copy(
                tipoTurno = "Permiso",
                horario = "-",
                esDescanso = false,
                modificadoTrasPublicar = true
            )
        } else {
            AsignacionTurno(
                id = "${empIdEfectivo}_${fechaIso}",
                fechaIso = fechaIso,
                empleadoId = empIdEfectivo,
                empleadoNombre = empNombreEfectivo,
                turnoId = "",
                tipoTurno = "Permiso",
                horario = "-",
                sucursalId = empSucursal,
                programacionId = "",
                esDescanso = false,
                estado = "PUBLICADA",
                modificadoTrasPublicar = false
            )
        }

        return turnoRepository.guardarAsignacion(asigPermiso)
    }
}
