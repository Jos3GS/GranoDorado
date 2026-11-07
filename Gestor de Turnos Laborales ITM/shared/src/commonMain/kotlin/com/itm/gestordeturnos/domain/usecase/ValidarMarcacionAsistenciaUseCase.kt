package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoAsistencia
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.PausaDescanso

sealed interface ValidacionMarcacionResult {
    data object Valido : ValidacionMarcacionResult
    data class Invalido(val mensaje: String) : ValidacionMarcacionResult
}

/**
 * Caso de uso de dominio para las reglas de marcación de asistencia (RN-27).
 *
 * Reglas de negocio:
 * 1. El empleado solo puede registrar asistencia si su usuario se encuentra ACTIVO.
 * 2. Solo puede registrar asistencia para el turno programado de la fecha actual (hoyIso).
 * 3. El turno asignado debe encontrarse publicado (no en estado BORRADOR).
 * 4. No se permite marcación si el día corresponde a descanso programado, incapacidad médica,
 *    vacaciones o permiso especial.
 * 5. Secuencia estricta de estados de jornada:
 *    SIN_INICIAR -> EN_TURNO -> EN_DESCANSO -> EN_TURNO -> FINALIZADO.
 * 6. Soporte de listado de pausas de descanso con hora de inicio y final sin sobrescribirse.
 */
class ValidarMarcacionAsistenciaUseCase {

    /**
     * Valida si un empleado está habilitado para marcar en el turno de hoy.
     */
    fun validarHabilitacionTurno(
        empleado: Empleado?,
        asignacion: AsignacionTurno?,
        fechaMarcacionIso: String
    ): ValidacionMarcacionResult {
        if (empleado != null && empleado.estado == EstadoUsuario.INACTIVO) {
            return ValidacionMarcacionResult.Invalido("Tu usuario se encuentra inactivo en el sistema.")
        }
        if (asignacion == null) {
            return ValidacionMarcacionResult.Invalido("No tienes un turno programado para la fecha de hoy.")
        }
        if (asignacion.fechaIso.isNotBlank() && asignacion.fechaIso != fechaMarcacionIso) {
            return ValidacionMarcacionResult.Invalido("Solo puedes registrar asistencia para el turno correspondiente a la fecha de hoy.")
        }
        if (asignacion.esDescanso) {
            return ValidacionMarcacionResult.Invalido("Hoy es tu día libre programado. No requiere marcación de asistencia.")
        }
        val tipo = asignacion.tipoTurno.lowercase()
        if (tipo.contains("incapacidad")) {
            return ValidacionMarcacionResult.Invalido("Tienes una incapacidad médica registrada para hoy. No debes registrar asistencia.")
        }
        if (tipo.contains("vacaciones")) {
            return ValidacionMarcacionResult.Invalido("Te encuentras en período de vacaciones programadas. No requiere marcación.")
        }
        if (tipo.contains("permiso")) {
            return ValidacionMarcacionResult.Invalido("Tienes un permiso especial aprobado para hoy. No requiere marcación.")
        }
        // Verificar publicación (si tiene estado explícito BORRADOR)
        if (asignacion.estado.equals("BORRADOR", ignoreCase = true)) {
            return ValidacionMarcacionResult.Invalido("El turno de hoy aún se encuentra en borrador y está pendiente de publicación por la administración.")
        }

        return ValidacionMarcacionResult.Valido
    }

    /**
     * Valida la secuencia estricta de transición de estados de asistencia.
     * Secuencia: SIN_INICIAR -> EN_TURNO -> EN_DESCANSO -> EN_TURNO -> FINALIZADO
     */
    fun validarTransicionEstado(
        estadoActual: EstadoAsistencia,
        nuevoEstado: EstadoAsistencia
    ): ValidacionMarcacionResult {
        val transicionValida = when (estadoActual) {
            EstadoAsistencia.SIN_INICIAR -> nuevoEstado == EstadoAsistencia.EN_TURNO
            EstadoAsistencia.EN_TURNO -> nuevoEstado == EstadoAsistencia.EN_DESCANSO || nuevoEstado == EstadoAsistencia.FINALIZADO
            EstadoAsistencia.EN_DESCANSO -> nuevoEstado == EstadoAsistencia.EN_TURNO
            EstadoAsistencia.FINALIZADO -> false
        }

        if (!transicionValida) {
            return ValidacionMarcacionResult.Invalido("La acción no es válida para el estado actual de tu jornada.")
        }
        return ValidacionMarcacionResult.Valido
    }

    /**
     * Agrega una nueva pausa de descanso al listado con hora de inicio, manteniendo las anteriores.
     */
    fun agregarInicioDescanso(
        descansosActuales: List<PausaDescanso>,
        horaActual: String
    ): List<PausaDescanso> {
        val nuevoDescanso = PausaDescanso(
            id = "desc_${descansosActuales.size + 1}",
            horaInicio = horaActual,
            horaFin = "-"
        )
        return descansosActuales + nuevoDescanso
    }

    /**
     * Finaliza la última pausa de descanso abierta (horaFin == "-") estableciendo la hora de finalización.
     */
    fun registrarFinDescanso(
        descansosActuales: List<PausaDescanso>,
        horaActual: String
    ): List<PausaDescanso> {
        val mutable = descansosActuales.toMutableList()
        val ultimoAbiertoIdx = mutable.indexOfLast { it.horaFin == "-" }
        if (ultimoAbiertoIdx >= 0) {
            val pausa = mutable[ultimoAbiertoIdx]
            mutable[ultimoAbiertoIdx] = pausa.copy(horaFin = horaActual)
        } else if (mutable.isNotEmpty()) {
            val ultimo = mutable.last()
            mutable[mutable.size - 1] = ultimo.copy(horaFin = horaActual)
        } else {
            mutable.add(PausaDescanso(id = "desc_1", horaInicio = "-", horaFin = horaActual))
        }
        return mutable
    }

    /**
     * Calcula los minutos acumulados de descanso a partir de todas las pausas completadas.
     */
    fun calcularMinutosDescanso(descansos: List<PausaDescanso>): Int {
        var totalMinutos = 0
        for (desc in descansos) {
            val ini = parsearMinutos(desc.horaInicio)
            val fin = parsearMinutos(desc.horaFin)
            if (ini != null && fin != null) {
                var diff = fin - ini
                if (diff < 0) diff += 24 * 60
                totalMinutos += diff
            }
        }
        return totalMinutos
    }

    fun parsearMinutos(horaStr: String): Int? {
        val limpia = horaStr.trim()
        val partes = limpia.split(":")
        if (partes.size == 2) {
            val h = partes[0].toIntOrNull() ?: return null
            val m = partes[1].toIntOrNull() ?: return null
            return h * 60 + m
        }
        return null
    }
}
