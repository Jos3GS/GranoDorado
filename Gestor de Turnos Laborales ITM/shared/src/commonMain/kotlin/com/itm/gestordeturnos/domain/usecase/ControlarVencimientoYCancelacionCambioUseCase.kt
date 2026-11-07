package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoSolicitudCambio
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

sealed interface ValidacionCancelacionResult {
    data object Valido : ValidacionCancelacionResult
    data class Invalido(val mensaje: String) : ValidacionCancelacionResult
}

/**
 * Caso de uso de dominio para la regla de negocio RN-22:
 * "El solicitante puede cancelar una solicitud en estado Pendiente o Aceptada por compañero
 * en cualquier momento antes de que el turno inicie o antes de la aprobación del administrador;
 * una vez aprobada, rechazada, cancelada o vencida, no puede modificarse ni reabrirse.
 * Toda solicitud no resuelta antes del inicio del turno pasa automáticamente a Vencida."
 */
class ControlarVencimientoYCancelacionCambioUseCase {

    fun obtenerMinutosActuales(): Int {
        return try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            now.hour * 60 + now.minute
        } catch (_: Throwable) {
            0
        }
    }

    /**
     * Valida si un colaborador solicitante puede cancelar su solicitud de intercambio.
     */
    fun validarCancelacion(
        solicitante: Empleado?,
        solicitud: SolicitudCambioTurno,
        todasAsignaciones: List<AsignacionTurno> = emptyList(),
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        horaActualMinutos: Int = obtenerMinutosActuales()
    ): ValidacionCancelacionResult {
        // 1. Validar colaborador activo
        if (solicitante == null) {
            return ValidacionCancelacionResult.Invalido("El colaborador no se encuentra registrado.")
        }
        if (solicitante.estado != EstadoUsuario.ACTIVO) {
            return ValidacionCancelacionResult.Invalido("Tu cuenta se encuentra inactiva y no puedes realizar cancelaciones de turno.")
        }

        // 2. Solo el solicitante puede cancelar
        val empId = solicitante.id
        val empNombre = if (solicitante.apellido.isNotBlank()) "${solicitante.nombre} ${solicitante.apellido}".trim() else solicitante.nombre
        val esSolicitante = solicitud.esRemitente ||
                (empId.isNotBlank() && solicitud.empleadoSolicitanteId == empId) ||
                (empNombre.isNotBlank() && solicitud.nombreSolicitante.equals(empNombre, ignoreCase = true)) ||
                (solicitante.nombre.isNotBlank() && solicitud.nombreSolicitante.equals(solicitante.nombre, ignoreCase = true))

        if (!esSolicitante) {
            return ValidacionCancelacionResult.Invalido("Solo el colaborador solicitante tiene la potestad de cancelar la solicitud de intercambio.")
        }

        // 3. Estados terminales: no pueden modificarse ni reabrirse
        when (solicitud.estado) {
            EstadoSolicitudCambio.APROBADA -> {
                return ValidacionCancelacionResult.Invalido("La solicitud ya fue aprobada por la administración y no puede ser cancelada.")
            }
            EstadoSolicitudCambio.RECHAZADA -> {
                return ValidacionCancelacionResult.Invalido("La solicitud ya fue rechazada y no puede ser cancelada.")
            }
            EstadoSolicitudCambio.CANCELADA -> {
                return ValidacionCancelacionResult.Invalido("La solicitud ya se encuentra cancelada.")
            }
            EstadoSolicitudCambio.VENCIDA -> {
                return ValidacionCancelacionResult.Invalido("La solicitud ya se encuentra vencida y no puede ser cancelada.")
            }
            EstadoSolicitudCambio.PENDIENTE,
            EstadoSolicitudCambio.ACEPTADA_COMPANERO -> {
                // Estados válidos para cancelación previa al inicio del turno
            }
        }

        // 4. Si el turno ya inició, no puede cancelarse (ha vencido)
        if (esSolicitudVencida(solicitud, todasAsignaciones, fechaActual, horaActualMinutos)) {
            return ValidacionCancelacionResult.Invalido("No es posible cancelar la solicitud debido a que el turno programado ya inició o la fecha límite ha expirado.")
        }

        return ValidacionCancelacionResult.Valido
    }

    /**
     * Determina si una solicitud de cambio debe considerarse vencida:
     * Toda solicitud no resuelta (PENDIENTE o ACEPTADA_COMPANERO) antes del inicio del turno pasa a VENCIDA.
     */
    fun esSolicitudVencida(
        solicitud: SolicitudCambioTurno,
        todasAsignaciones: List<AsignacionTurno> = emptyList(),
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        horaActualMinutos: Int = obtenerMinutosActuales()
    ): Boolean {
        // Los estados terminales ya tienen su resolución final y no vencen retrospectivamente
        if (solicitud.esTerminal && solicitud.estado != EstadoSolicitudCambio.VENCIDA) {
            return false
        }
        if (solicitud.estado == EstadoSolicitudCambio.VENCIDA) {
            return true
        }

        // Obtener la fecha y hora más temprana entre los turnos a intercambiar
        val turnoEntrega = todasAsignaciones.find {
            it.id == solicitud.turnoEntregaId || (it.empleadoId == solicitud.empleadoSolicitanteId && solicitud.detalleTurnoEntrega.contains(it.fechaIso))
        }
        val turnoRecibe = todasAsignaciones.find {
            it.id == solicitud.turnoRecibeId || (it.empleadoId == solicitud.empleadoCompaneroId && solicitud.detalleTurnoRecibe.contains(it.fechaIso))
        }

        val fechaEntrega = turnoEntrega?.fechaIso ?: extraerFechaIso(solicitud.turnoEntregaId).ifBlank { extraerFechaIso(solicitud.detalleTurnoEntrega) }
        val horarioEntrega = turnoEntrega?.horario ?: extraerHorario(solicitud.detalleTurnoEntrega)

        val fechaRecibe = turnoRecibe?.fechaIso ?: extraerFechaIso(solicitud.turnoRecibeId).ifBlank { extraerFechaIso(solicitud.detalleTurnoRecibe) }
        val horarioRecibe = turnoRecibe?.horario ?: extraerHorario(solicitud.detalleTurnoRecibe)

        val momentoEntrega = parsearMomento(fechaEntrega, horarioEntrega)
        val momentoRecibe = parsearMomento(fechaRecibe, horarioRecibe)

        // Si tenemos ambos momentos, el inicio más temprano de los dos determina el límite de intercambio
        val momentos = listOfNotNull(momentoEntrega, momentoRecibe)
        if (momentos.isNotEmpty()) {
            val momentoMasTemprano = momentos.minWithOrNull { a, b ->
                val compFecha = a.first.compareTo(b.first)
                if (compFecha != 0) compFecha else a.second.compareTo(b.second)
            }!!

            val fechaTurno = momentoMasTemprano.first
            val horaTurnoMinutos = momentoMasTemprano.second

            if (fechaTurno < fechaActual) return true
            if (fechaTurno == fechaActual && horaTurnoMinutos <= horaActualMinutos) return true
            return false
        }

        // Fallback si no hay asignaciones ni horarios en detalle: verificar contra fechaSolicitud
        if (solicitud.fechaSolicitud != null) {
            return fechaActual > solicitud.fechaSolicitud
        }

        return false
    }

    /**
     * Procesa una lista de solicitudes y retorna la lista con las solicitudes vencidas actualizadas a EstadoSolicitudCambio.VENCIDA.
     */
    fun procesarVencimientos(
        solicitudes: List<SolicitudCambioTurno>,
        todasAsignaciones: List<AsignacionTurno> = emptyList(),
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        horaActualMinutos: Int = obtenerMinutosActuales()
    ): Pair<List<SolicitudCambioTurno>, List<SolicitudCambioTurno>> {
        val actualizadas = mutableListOf<SolicitudCambioTurno>()
        val recienVencidas = mutableListOf<SolicitudCambioTurno>()

        for (sol in solicitudes) {
            if (!sol.esTerminal && esSolicitudVencida(sol, todasAsignaciones, fechaActual, horaActualMinutos)) {
                val vencida = sol.marcarVencida()
                actualizadas.add(vencida)
                recienVencidas.add(vencida)
            } else {
                actualizadas.add(sol)
            }
        }

        return Pair(actualizadas, recienVencidas)
    }

    private fun parsearMomento(fechaIso: String, horario: String): Pair<LocalDate, Int>? {
        val fecha = try { LocalDate.parse(fechaIso) } catch (_: Throwable) { return null }
        val minutos = parsearHoraInicioMinutos(horario) ?: 0
        return Pair(fecha, minutos)
    }

    private fun parsearHoraInicioMinutos(horario: String): Int? {
        val partes = horario.replace("\r", "").replace("\n", "").trim().split("-")
        if (partes.isNotEmpty()) {
            val horaStr = partes[0].trim()
            val hm = horaStr.split(":")
            if (hm.size == 2) {
                val h = hm[0].trim().toIntOrNull() ?: return null
                val m = hm[1].trim().toIntOrNull() ?: return null
                return h * 60 + m
            }
        }
        return null
    }

    private fun extraerFechaIso(texto: String): String {
        val regex = Regex("""\b(20\d{2}-\d{2}-\d{2})\b""")
        return regex.find(texto)?.value ?: ""
    }

    private fun extraerHorario(texto: String): String {
        val regex = Regex("""\b\d{1,2}:\d{2}\s*-\s*\d{1,2}:\d{2}\b""")
        return regex.find(texto)?.value ?: ""
    }
}
