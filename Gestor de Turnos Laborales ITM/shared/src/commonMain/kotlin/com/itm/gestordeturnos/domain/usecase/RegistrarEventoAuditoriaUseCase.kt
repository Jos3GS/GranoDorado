package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.data.repository.AuditoriaRepository
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EventoAuditoria
import com.itm.gestordeturnos.domain.model.TipoAccionAuditoria
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import com.itm.gestordeturnos.domain.model.UsuarioActual
import com.itm.gestordeturnos.domain.repository.AuditoriaRepositoryContract
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Caso de uso de dominio para la emisión y registro de eventos de auditoría y trazabilidad (RN-32).
 *
 * Registra formalmente:
 * 1. Intentos de acceso (login exitosos y fallidos).
 * 2. Creación y modificación de empleados (preservando estado anterior vs nuevo).
 * 3. Creación y edición de turnos y catálogo.
 * 4. Publicación de la malla semanal.
 * 5. Aprobación y rechazo de cambios de turno (preservando estado anterior vs nuevo).
 * 6. Marcaciones manuales de supervisor con justificación obligatoria.
 */
class RegistrarEventoAuditoriaUseCase(
    private val auditoriaRepository: AuditoriaRepositoryContract = AuditoriaRepository
) {

    private fun generarTimestampIso(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val fecha = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
        val hora = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}:${now.second.toString().padStart(2, '0')}"
        return "$fecha $hora"
    }

    private fun generarId(): String {
        return "aud_${Clock.System.now().toEpochMilliseconds()}_${Random.nextInt(100, 999)}"
    }

    suspend fun registrarEvento(evento: EventoAuditoria): Result<Unit> {
        val completo = if (evento.id.isBlank() || evento.fechaHoraIso.isBlank()) {
            evento.copy(
                id = evento.id.ifBlank { generarId() },
                fechaHoraIso = evento.fechaHoraIso.ifBlank { generarTimestampIso() }
            )
        } else evento
        return auditoriaRepository.registrarEvento(completo)
    }

    fun registrarEventoEnBackground(evento: EventoAuditoria) {
        val completo = if (evento.id.isBlank() || evento.fechaHoraIso.isBlank()) {
            evento.copy(
                id = evento.id.ifBlank { generarId() },
                fechaHoraIso = evento.fechaHoraIso.ifBlank { generarTimestampIso() }
            )
        } else evento
        auditoriaRepository.registrarEventoEnBackground(completo)
    }

    fun construirEventoLogin(
        correo: String,
        exitoso: Boolean,
        usuario: UsuarioActual? = null,
        motivoFallo: String = ""
    ): EventoAuditoria {
        return EventoAuditoria(
            id = generarId(),
            fechaHoraIso = generarTimestampIso(),
            usuarioId = usuario?.id ?: correo,
            usuarioNombre = usuario?.nombre ?: correo,
            rol = usuario?.rol?.name ?: "ANÓNIMO",
            sucursal = usuario?.sucursal ?: "CENTRO",
            tipoAccion = if (exitoso) TipoAccionAuditoria.LOGIN_EXITOSO else TipoAccionAuditoria.LOGIN_FALLIDO,
            entidad = "AUTH",
            entidadId = correo,
            valorAnterior = if (!exitoso) "Intento fallido" else "",
            valorNuevo = if (exitoso) "Sesión iniciada exitosamente" else motivoFallo.ifBlank { "Credenciales inválidas" },
            detalles = if (exitoso) "Ingreso al sistema concedido" else "Intento de inicio de sesión rechazado"
        )
    }

    fun construirEventoEmpleado(
        actor: UsuarioActual?,
        esNuevo: Boolean,
        empleadoAnterior: Empleado?,
        empleadoNuevo: Empleado
    ): EventoAuditoria {
        val anteriorStr = if (empleadoAnterior != null) {
            "Cargo: ${empleadoAnterior.cargo.name} (${empleadoAnterior.cargo.displayName}), Sucursal: ${empleadoAnterior.sucursal}, Estado: ${empleadoAnterior.estado.name}"
        } else "-"

        val nuevoStr = "Cargo: ${empleadoNuevo.cargo.name} (${empleadoNuevo.cargo.displayName}), Sucursal: ${empleadoNuevo.sucursal}, Estado: ${empleadoNuevo.estado.name}"

        return EventoAuditoria(
            id = generarId(),
            fechaHoraIso = generarTimestampIso(),
            usuarioId = actor?.id ?: "SISTEMA",
            usuarioNombre = actor?.nombre ?: "Administrador",
            rol = actor?.rol?.name ?: "ADMINISTRADOR",
            sucursal = actor?.sucursal ?: empleadoNuevo.sucursal,
            tipoAccion = if (esNuevo) TipoAccionAuditoria.CREAR_EMPLEADO else TipoAccionAuditoria.MODIFICAR_EMPLEADO,
            entidad = "EMPLEADO",
            entidadId = empleadoNuevo.id,
            valorAnterior = anteriorStr,
            valorNuevo = nuevoStr,
            detalles = if (esNuevo) "Creación del colaborador ${empleadoNuevo.nombre} (${empleadoNuevo.correo})"
                       else "Actualización de datos del colaborador ${empleadoNuevo.nombre} (${empleadoNuevo.correo})"
        )
    }

    fun construirEventoTurno(
        actor: UsuarioActual?,
        esNuevo: Boolean,
        turnoAnterior: TurnoTemplate?,
        turnoNuevo: TurnoTemplate
    ): EventoAuditoria {
        val anteriorStr = if (turnoAnterior != null) {
            "Nombre: ${turnoAnterior.nombre}, Horario: ${turnoAnterior.horaInicio}-${turnoAnterior.horaFin}, Estado: ${turnoAnterior.estado}"
        } else "-"

        val nuevoStr = "Nombre: ${turnoNuevo.nombre}, Horario: ${turnoNuevo.horaInicio}-${turnoNuevo.horaFin}, Estado: ${turnoNuevo.estado}"

        return EventoAuditoria(
            id = generarId(),
            fechaHoraIso = generarTimestampIso(),
            usuarioId = actor?.id ?: "SISTEMA",
            usuarioNombre = actor?.nombre ?: "Administrador",
            rol = actor?.rol?.name ?: "ADMINISTRADOR",
            sucursal = actor?.sucursal ?: "CENTRO",
            tipoAccion = if (esNuevo) TipoAccionAuditoria.CREAR_TURNO else TipoAccionAuditoria.MODIFICAR_TURNO,
            entidad = "TURNO",
            entidadId = turnoNuevo.id,
            valorAnterior = anteriorStr,
            valorNuevo = nuevoStr,
            detalles = if (esNuevo) "Alta de plantilla de turno ${turnoNuevo.nombre}"
                       else "Modificación de horario/parámetros del turno ${turnoNuevo.nombre}"
        )
    }

    fun construirEventoPublicacionMalla(
        actor: UsuarioActual?,
        semanaTexto: String,
        sucursal: String,
        totalAsignaciones: Int
    ): EventoAuditoria {
        return EventoAuditoria(
            id = generarId(),
            fechaHoraIso = generarTimestampIso(),
            usuarioId = actor?.id ?: "SISTEMA",
            usuarioNombre = actor?.nombre ?: "Administrador",
            rol = actor?.rol?.name ?: "ADMINISTRADOR",
            sucursal = sucursal,
            tipoAccion = TipoAccionAuditoria.PUBLICAR_MALLA,
            entidad = "MALLA",
            entidadId = "malla_${sucursal}_${semanaTexto}",
            valorAnterior = "BORRADOR",
            valorNuevo = "PUBLICADA",
            detalles = "Publicación de programación operativa para la semana $semanaTexto ($totalAsignaciones turnos asignados)."
        )
    }

    fun construirEventoCambioTurno(
        actor: UsuarioActual?,
        accion: TipoAccionAuditoria,
        solicitudId: String,
        solicitante: String,
        companero: String,
        sucursal: String,
        motivoOComentario: String
    ): EventoAuditoria {
        val valorAnt = when (accion) {
            TipoAccionAuditoria.APROBAR_CAMBIO_TURNO -> "ACEPTADA_COMPANERO"
            TipoAccionAuditoria.RECHAZAR_CAMBIO_TURNO -> "PENDIENTE / ACEPTADA_COMPANERO"
            else -> "PENDIENTE"
        }
        val valorNuev = when (accion) {
            TipoAccionAuditoria.APROBAR_CAMBIO_TURNO -> "APROBADA"
            TipoAccionAuditoria.RECHAZAR_CAMBIO_TURNO -> "RECHAZADA"
            else -> "CANCELADA"
        }

        return EventoAuditoria(
            id = generarId(),
            fechaHoraIso = generarTimestampIso(),
            usuarioId = actor?.id ?: "SISTEMA",
            usuarioNombre = actor?.nombre ?: "Administrador",
            rol = actor?.rol?.name ?: "ADMINISTRADOR",
            sucursal = sucursal,
            tipoAccion = accion,
            entidad = "SOLICITUD_CAMBIO",
            entidadId = solicitudId,
            valorAnterior = valorAnt,
            valorNuevo = valorNuev,
            detalles = "Solicitante: $solicitante · Compañero: $companero · Motivo: $motivoOComentario"
        )
    }
}
