package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoSolicitudCambio
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

sealed class ValidacionAprobacionCambioResult {
    object Valido : ValidacionAprobacionCambioResult()
    data class Invalido(val mensaje: String) : ValidacionAprobacionCambioResult()
}

/**
 * Caso de uso de dominio para la regla de negocio RN-20:
 * "Toda solicitud pasa primero por la aceptación del compañero y solo después por la aprobación
 * del Administrador de Sucursal; el horario cambia únicamente cuando el administrador aprueba.
 * Ningún administrador puede resolver una solicitud en la que participe como solicitante o como compañero.
 * Al aprobar, el sistema vuelve a verificar todas las reglas del intercambio y no lo aplica si alguna dejó de cumplirse."
 */
class ValidarAprobacionCambioTurnoUseCase(
    private val validarSolicitudUseCase: ValidarSolicitudCambioTurnoUseCase = ValidarSolicitudCambioTurnoUseCase()
) {

    private fun obtenerMinutosActuales(): Int {
        return try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            now.hour * 60 + now.minute
        } catch (_: Throwable) {
            0
        }
    }

    /**
     * Valida integralmente si un administrador tiene la facultad de aprobar una solicitud de cambio de turno,
     * comprobando:
     * 1. Administrador autenticado, activo y con rol administrativo.
     * 2. Pertenencia a la sucursal de la solicitud (si es Administrador de Sucursal).
     * 3. Conflicto de interés: El administrador no puede ser ni el solicitante ni el compañero.
     * 4. Secuencia de estados: La solicitud debe haber sido aceptada previamente por el compañero (ACEPTADA_COMPANERO).
     * 5. Revalidación total al momento de aprobar: Vuelve a comprobar que ambos empleados sigan activos,
     *    misma sucursal, mismo cargo, turnos publicados, horas de inicio futuras y que el resultado cumpla
     *    todas las reglas de programación (no doble turno, no ausencias, 12h descanso interjornada, máx 42h semanales).
     */
    fun validarAprobacion(
        administrador: Empleado?,
        rolAdministrador: RolUsuario?,
        solicitud: SolicitudCambioTurno,
        empleados: List<Empleado>,
        todasAsignaciones: List<AsignacionTurno>,
        todasSolicitudes: List<SolicitudCambioTurno>,
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        horaActualMinutos: Int = obtenerMinutosActuales()
    ): ValidacionAprobacionCambioResult {
        // 1. Validar administrador activo
        if (administrador == null) {
            return ValidacionAprobacionCambioResult.Invalido("El usuario administrador no se encuentra registrado.")
        }
        if (administrador.estado != EstadoUsuario.ACTIVO) {
            return ValidacionAprobacionCambioResult.Invalido("Tu cuenta de administrador se encuentra inactiva.")
        }

        // 2. Rol administrativo
        if (rolAdministrador == RolUsuario.EMPLEADO) {
            return ValidacionAprobacionCambioResult.Invalido("Solo un administrador o supervisor puede aprobar solicitudes de intercambio.")
        }

        // 3. Alcance de sucursal para Administrador de Sucursal
        if (rolAdministrador == RolUsuario.ADMINISTRADOR_SUCURSAL) {
            val sucAdmin = administrador.sucursal.trim().replace(" ", "")
            val sucSol = solicitud.sucursalNombre.trim().replace(" ", "")
            if (sucSol.isNotBlank() && !sucAdmin.equals(sucSol, ignoreCase = true)) {
                return ValidacionAprobacionCambioResult.Invalido("Un Administrador de Sucursal solo puede resolver solicitudes de su propia sucursal.")
            }
        }

        // 4. Conflicto de intereses (RN-20): Ningún administrador puede resolver una solicitud en la que participe
        val adminNombre = "${administrador.nombre} ${administrador.apellido}".trim()
        val esParticipante = administrador.id == solicitud.empleadoSolicitanteId ||
                             administrador.id == solicitud.empleadoCompaneroId ||
                             adminNombre.equals(solicitud.nombreSolicitante, ignoreCase = true) ||
                             adminNombre.equals(solicitud.nombreCompanero, ignoreCase = true) ||
                             administrador.nombre.equals(solicitud.nombreSolicitante, ignoreCase = true) ||
                             administrador.nombre.equals(solicitud.nombreCompanero, ignoreCase = true)

        if (esParticipante) {
            return ValidacionAprobacionCambioResult.Invalido("Un administrador no puede resolver una solicitud en la que participe como solicitante o como compañero.")
        }

        // 5. Secuencia de aprobación (RN-20): Pasa primero por la aceptación del compañero
        when (solicitud.estado) {
            EstadoSolicitudCambio.PENDIENTE -> {
                return ValidacionAprobacionCambioResult.Invalido("La solicitud debe ser aceptada primero por el compañero antes de ser aprobada por la administración.")
            }
            EstadoSolicitudCambio.APROBADA -> {
                return ValidacionAprobacionCambioResult.Invalido("Esta solicitud ya ha sido aprobada previamente.")
            }
            EstadoSolicitudCambio.RECHAZADA -> {
                return ValidacionAprobacionCambioResult.Invalido("Esta solicitud ya ha sido rechazada o cancelada y no puede ser aprobada.")
            }
            EstadoSolicitudCambio.VENCIDA -> {
                return ValidacionAprobacionCambioResult.Invalido("Esta solicitud se encuentra vencida y ya no puede ser aprobada.")
            }
            EstadoSolicitudCambio.ACEPTADA_COMPANERO -> {
                // Estado correcto para resolución administrativa
            }
        }

        // 6. Revalidación completa de todas las reglas del intercambio al aprobar (RN-20)
        val solicitante = empleados.find { it.id == solicitud.empleadoSolicitanteId }
            ?: empleados.find { it.nombre.equals(solicitud.nombreSolicitante, ignoreCase = true) }
        val companero = empleados.find { it.id == solicitud.empleadoCompaneroId }
            ?: empleados.find { it.nombre.equals(solicitud.nombreCompanero, ignoreCase = true) }

        if (solicitante == null || companero == null) {
            return ValidacionAprobacionCambioResult.Invalido("Uno de los colaboradores involucrados ya no se encuentra registrado.")
        }
        if (solicitante.estado != EstadoUsuario.ACTIVO || companero.estado != EstadoUsuario.ACTIVO) {
            return ValidacionAprobacionCambioResult.Invalido("El intercambio no puede aprobarse porque uno de los colaboradores se encuentra inactivo.")
        }

        val turnoEntrega = todasAsignaciones.find {
            it.id == solicitud.turnoEntregaId || (it.empleadoId == solicitante.id && solicitud.detalleTurnoEntrega.contains(it.fechaIso))
        }
        val turnoRecibe = todasAsignaciones.find {
            it.id == solicitud.turnoRecibeId || (it.empleadoId == companero.id && solicitud.detalleTurnoRecibe.contains(it.fechaIso))
        }

        if (turnoEntrega == null || turnoRecibe == null) {
            return ValidacionAprobacionCambioResult.Invalido("Uno de los turnos a intercambiar no fue encontrado en la programación actual.")
        }

        // Filtrar solicitudes concurrentes excluyendo la que se está aprobando actualmente
        val otrasSolicitudes = todasSolicitudes.filter { it.id != solicitud.id }

        val revalidacion = validarSolicitudUseCase.validarSolicitud(
            solicitante = solicitante,
            companero = companero,
            turnoEntrega = turnoEntrega,
            turnoRecibe = turnoRecibe,
            solicitudesExistentes = otrasSolicitudes,
            todasAsignaciones = todasAsignaciones,
            fechaActual = fechaActual,
            horaActualMinutos = horaActualMinutos
        )

        return when (revalidacion) {
            is ValidacionSolicitudCambioResult.Valido -> ValidacionAprobacionCambioResult.Valido
            is ValidacionSolicitudCambioResult.Invalido -> ValidacionAprobacionCambioResult.Invalido(
                "No se puede aprobar el intercambio porque dejó de cumplirse una regla: ${revalidacion.mensaje}"
            )
        }
    }

    /**
     * Valida si un administrador puede rechazar administrativamente una solicitud:
     * Verifica que no participe como solicitante o compañero y que la solicitud no esté ya finalizada.
     */
    fun validarRechazoAdministrador(
        administrador: Empleado?,
        rolAdministrador: RolUsuario?,
        solicitud: SolicitudCambioTurno
    ): ValidacionAprobacionCambioResult {
        if (administrador == null) {
            return ValidacionAprobacionCambioResult.Invalido("El usuario administrador no se encuentra registrado.")
        }
        if (administrador.estado != EstadoUsuario.ACTIVO) {
            return ValidacionAprobacionCambioResult.Invalido("Tu cuenta de administrador se encuentra inactiva.")
        }
        if (rolAdministrador == RolUsuario.EMPLEADO) {
            return ValidacionAprobacionCambioResult.Invalido("Solo un administrador o supervisor puede resolver solicitudes de intercambio.")
        }
        if (rolAdministrador == RolUsuario.ADMINISTRADOR_SUCURSAL) {
            val sucAdmin = administrador.sucursal.trim().replace(" ", "")
            val sucSol = solicitud.sucursalNombre.trim().replace(" ", "")
            if (sucSol.isNotBlank() && !sucAdmin.equals(sucSol, ignoreCase = true)) {
                return ValidacionAprobacionCambioResult.Invalido("Un Administrador de Sucursal solo puede resolver solicitudes de su propia sucursal.")
            }
        }

        // Conflicto de interés (RN-20)
        val adminNombre = "${administrador.nombre} ${administrador.apellido}".trim()
        val esParticipante = administrador.id == solicitud.empleadoSolicitanteId ||
                             administrador.id == solicitud.empleadoCompaneroId ||
                             adminNombre.equals(solicitud.nombreSolicitante, ignoreCase = true) ||
                             adminNombre.equals(solicitud.nombreCompanero, ignoreCase = true) ||
                             administrador.nombre.equals(solicitud.nombreSolicitante, ignoreCase = true) ||
                             administrador.nombre.equals(solicitud.nombreCompanero, ignoreCase = true)

        if (esParticipante) {
            return ValidacionAprobacionCambioResult.Invalido("Un administrador no puede resolver una solicitud en la que participe como solicitante o como compañero.")
        }

        if (solicitud.estado == EstadoSolicitudCambio.APROBADA ||
            solicitud.estado == EstadoSolicitudCambio.RECHAZADA ||
            solicitud.estado == EstadoSolicitudCambio.VENCIDA) {
            return ValidacionAprobacionCambioResult.Invalido("La solicitud ya ha sido finalizada y no puede resolverse nuevamente.")
        }

        return ValidacionAprobacionCambioResult.Valido
    }
}
