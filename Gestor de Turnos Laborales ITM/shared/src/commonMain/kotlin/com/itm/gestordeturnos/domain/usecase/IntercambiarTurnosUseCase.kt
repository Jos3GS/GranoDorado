package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract

/**
 * Caso de uso de dominio para orquestar el intercambio de asignaciones de turno
 * entre dos colaboradores cuando una solicitud de cambio es aprobada por administración.
 */
class IntercambiarTurnosUseCase(
    private val turnoRepository: TurnoRepositoryContract
) {
    suspend operator fun invoke(solicitud: SolicitudCambioTurno): Result<Unit> {
        val asignacionesActuales = turnoRepository.asignaciones.value
        val empAId = solicitud.empleadoSolicitanteId
        val empANombre = solicitud.nombreSolicitante
        val empBId = solicitud.empleadoCompaneroId
        val empBNombre = solicitud.nombreCompanero

        var asigEntrega = asignacionesActuales.find {
            (it.id.isNotBlank() && it.id == solicitud.turnoEntregaId) ||
            (it.empleadoId == empAId && (it.id.endsWith(solicitud.turnoEntregaId) || solicitud.detalleTurnoEntrega.contains(it.fechaIso)))
        }
        var asigRecibe = asignacionesActuales.find {
            (it.id.isNotBlank() && it.id == solicitud.turnoRecibeId) ||
            (it.empleadoId == empBId && (it.id.endsWith(solicitud.turnoRecibeId) || solicitud.detalleTurnoRecibe.contains(it.fechaIso)))
        }

        val fechaEntregaIso = asigEntrega?.fechaIso ?: extraerFechaIso(solicitud.turnoEntregaId).ifBlank { extraerFechaIso(solicitud.detalleTurnoEntrega) }
        val fechaRecibeIso = asigRecibe?.fechaIso ?: extraerFechaIso(solicitud.turnoRecibeId).ifBlank { extraerFechaIso(solicitud.detalleTurnoRecibe) }

        if (asigEntrega == null && fechaEntregaIso.isNotBlank()) {
            asigEntrega = asignacionesActuales.find { it.empleadoId == empAId && it.fechaIso == fechaEntregaIso }
        }
        if (asigRecibe == null && fechaRecibeIso.isNotBlank()) {
            asigRecibe = asignacionesActuales.find { it.empleadoId == empBId && it.fechaIso == fechaRecibeIso }
        }

        val nuevasAsignaciones = mutableListOf<AsignacionTurno>()

        if (fechaEntregaIso == fechaRecibeIso && asigEntrega != null && asigRecibe != null) {
            nuevasAsignaciones.add(
                asigEntrega.copy(
                    tipoTurno = asigRecibe.tipoTurno,
                    horario = asigRecibe.horario,
                    turnoId = asigRecibe.turnoId,
                    turno = asigRecibe.turno,
                    modificadoTrasPublicar = true
                )
            )
            nuevasAsignaciones.add(
                asigRecibe.copy(
                    tipoTurno = asigEntrega.tipoTurno,
                    horario = asigEntrega.horario,
                    turnoId = asigEntrega.turnoId,
                    turno = asigEntrega.turno,
                    modificadoTrasPublicar = true
                )
            )
        } else {
            if (fechaEntregaIso.isNotBlank() && asigEntrega != null) {
                val bEnFechaEntrega = asignacionesActuales.find { it.empleadoId == empBId && it.fechaIso == fechaEntregaIso }
                val nuevoTurnoB = if (bEnFechaEntrega != null) {
                    bEnFechaEntrega.copy(
                        tipoTurno = asigEntrega.tipoTurno,
                        horario = asigEntrega.horario,
                        turnoId = asigEntrega.turnoId,
                        turno = asigEntrega.turno,
                        esDescanso = false,
                        modificadoTrasPublicar = true
                    )
                } else {
                    AsignacionTurno(
                        id = "${empBId}_$fechaEntregaIso",
                        fechaIso = fechaEntregaIso,
                        empleadoId = empBId,
                        empleadoNombre = empBNombre,
                        tipoTurno = asigEntrega.tipoTurno,
                        horario = asigEntrega.horario,
                        turnoId = asigEntrega.turnoId,
                        turno = asigEntrega.turno,
                        sucursalId = asigEntrega.sucursalId,
                        estado = "PUBLICADA",
                        esDescanso = false,
                        modificadoTrasPublicar = true
                    )
                }
                val nuevoTurnoA = asigEntrega.copy(
                    tipoTurno = "Descanso",
                    horario = "-",
                    esDescanso = true,
                    modificadoTrasPublicar = true
                )
                nuevasAsignaciones.add(nuevoTurnoB)
                nuevasAsignaciones.add(nuevoTurnoA)
            }

            if (fechaRecibeIso.isNotBlank() && asigRecibe != null) {
                val aEnFechaRecibe = asignacionesActuales.find { it.empleadoId == empAId && it.fechaIso == fechaRecibeIso }
                val nuevoTurnoA = if (aEnFechaRecibe != null) {
                    aEnFechaRecibe.copy(
                        tipoTurno = asigRecibe.tipoTurno,
                        horario = asigRecibe.horario,
                        turnoId = asigRecibe.turnoId,
                        turno = asigRecibe.turno,
                        esDescanso = false,
                        modificadoTrasPublicar = true
                    )
                } else {
                    AsignacionTurno(
                        id = "${empAId}_$fechaRecibeIso",
                        fechaIso = fechaRecibeIso,
                        empleadoId = empAId,
                        empleadoNombre = empANombre,
                        tipoTurno = asigRecibe.tipoTurno,
                        horario = asigRecibe.horario,
                        turnoId = asigRecibe.turnoId,
                        turno = asigRecibe.turno,
                        sucursalId = asigRecibe.sucursalId,
                        estado = "PUBLICADA",
                        esDescanso = false,
                        modificadoTrasPublicar = true
                    )
                }
                val nuevoTurnoB = asigRecibe.copy(
                    tipoTurno = "Descanso",
                    horario = "-",
                    esDescanso = true,
                    modificadoTrasPublicar = true
                )
                nuevasAsignaciones.add(nuevoTurnoA)
                nuevasAsignaciones.add(nuevoTurnoB)
            }
        }

        return if (nuevasAsignaciones.isNotEmpty()) {
            turnoRepository.guardarAsignaciones(nuevasAsignaciones)
        } else {
            Result.success(Unit)
        }
    }

    private fun extraerFechaIso(texto: String): String {
        val regex = Regex("""\d{4}-\d{2}-\d{2}""")
        return regex.find(texto)?.value ?: ""
    }
}
