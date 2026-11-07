package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.RolUsuario

sealed class ValidacionSoloLecturaResult {
    data object Permitido : ValidacionSoloLecturaResult()
    data class Bloqueado(val mensaje: String) : ValidacionSoloLecturaResult()
}

/**
 * Caso de uso para la regla de negocio RN-18:
 * "El empleado consulta únicamente sus propios turnos, y solo aquellos que ya fueron publicados.
 * El horario del empleado y la malla de la sucursal se presentan en modo de solo lectura:
 * ninguna acción de esas pantallas modifica la programación."
 */
class ConsultarHorarioEmpleadoUseCase {

    companion object {
        const val MENSAJE_SOLO_LECTURA_EMPLEADO =
            "El horario del colaborador y la malla de la sucursal se presentan en modo de solo lectura: no se permite modificar la programación."
        const val MENSAJE_SOLO_LECTURA_SEMANA_PASADA =
            "Esta programación pertenece a una semana pasada y se presenta en modo de solo lectura."
    }

    /**
     * Filtra las asignaciones visibles para un empleado en una semana específica:
     * 1. Consulta únicamente sus propios turnos (por ID o nombre coincidente).
     * 2. Si no es administrador, consulta únicamente los turnos que ya fueron publicados (excluye borradores).
     */
    fun filtrarTurnosVisiblesParaEmpleado(
        empleadoId: String,
        empleadoNombre: String = "",
        todasAsignaciones: List<AsignacionTurno>,
        fechasIsoSemana: Set<String>,
        sucursalEmpleado: String = "",
        esAdmin: Boolean = false
    ): List<AsignacionTurno> {
        val semanaEstaPublicada = esSemanaPublicada(
            todasAsignaciones = todasAsignaciones,
            fechasIsoSemana = fechasIsoSemana,
            sucursalEmpleado = sucursalEmpleado,
            empleadoId = empleadoId
        )

        return todasAsignaciones.filter { asig ->
            val esPropio = asig.empleadoId == empleadoId ||
                (asig.empleadoNombre.isNotBlank() && empleadoNombre.isNotBlank() &&
                 asig.empleadoNombre.equals(empleadoNombre, ignoreCase = true))

            val enSemana = asig.fechaIso in fechasIsoSemana

            if (!esPropio || !enSemana) {
                false
            } else if (esAdmin) {
                true
            } else {
                // Empleado: solo turnos publicados, nunca borradores
                semanaEstaPublicada &&
                    asig.estado.equals("PUBLICADA", ignoreCase = true) &&
                    !asig.estado.equals("BORRADOR", ignoreCase = true)
            }
        }
    }

    /**
     * Determina si la semana se encuentra en estado PUBLICADA para la sucursal o el empleado.
     */
    fun esSemanaPublicada(
        todasAsignaciones: List<AsignacionTurno>,
        fechasIsoSemana: Set<String>,
        sucursalEmpleado: String = "",
        empleadoId: String = ""
    ): Boolean {
        // Si hay alguna asignación de este empleado publicada en la semana
        val asignacionEmpleadoPublicada = todasAsignaciones.any { asig ->
            asig.fechaIso in fechasIsoSemana &&
                asig.empleadoId == empleadoId &&
                asig.estado.equals("PUBLICADA", ignoreCase = true)
        }
        if (asignacionEmpleadoPublicada) return true

        // O si hay asignaciones publicadas para la sucursal del empleado en esa semana
        return todasAsignaciones.any { asig ->
            asig.fechaIso in fechasIsoSemana &&
                asig.estado.equals("PUBLICADA", ignoreCase = true) &&
                (asig.sucursalId.isBlank() ||
                 sucursalEmpleado.isBlank() ||
                 asig.sucursalId.equals(sucursalEmpleado, ignoreCase = true) ||
                 asig.sucursalId.replace(" ", "").equals(sucursalEmpleado.replace(" ", ""), ignoreCase = true))
        }
    }

    /**
     * Evalúa si una pantalla o programación debe presentarse en modo de solo lectura.
     * Retorna true si el usuario tiene rol EMPLEADO o si la semana está en el pasado.
     */
    fun esModoSoloLectura(
        rol: RolUsuario?,
        esSemanaPasada: Boolean = false
    ): Boolean {
        if (rol == RolUsuario.EMPLEADO) return true
        if (esSemanaPasada) return true
        return false
    }

    /**
     * Valida si se permite modificar la programación de turnos.
     */
    fun validarPermisoModificarProgramacion(
        rol: RolUsuario?,
        esSemanaPasada: Boolean = false
    ): ValidacionSoloLecturaResult {
        if (esSemanaPasada) {
            return ValidacionSoloLecturaResult.Bloqueado(MENSAJE_SOLO_LECTURA_SEMANA_PASADA)
        }
        if (rol == RolUsuario.EMPLEADO) {
            return ValidacionSoloLecturaResult.Bloqueado(MENSAJE_SOLO_LECTURA_EMPLEADO)
        }
        return ValidacionSoloLecturaResult.Permitido
    }
}
