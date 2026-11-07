package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.RegistroAsistencia

/**
 * Caso de uso de dominio para el control y generación de alertas por incumplimiento de asistencia (RN-31).
 *
 * Reglas de negocio:
 * 1. Solo se evalúan asignaciones que correspondan a la fecha actual (hoyIso).
 * 2. La asignación debe encontrarse en estado "PUBLICADA" (no "BORRADOR").
 * 3. Se excluyen asignaciones de descanso (esDescanso = true), incapacidades médicas ("Incapacidad"),
 *    vacaciones y permisos aprobados. Ninguna de estas genera ausencias ni alertas de retraso.
 * 4. Para turnos operativos publicados sin marcación de entrada:
 *    - Se genera una alerta automática de incumplimiento si transcurren más de 15 minutos desde la hora programada.
 *    - Se garantiza máximo una notificación por turno asignado (ID determinístico: inc_retraso_{empleadoId}_{fechaIso}).
 * 5. Se genera alerta por llegada tardía si el empleado registra ingreso con retraso mayor a 10 minutos (inc_tarde_{registroId}).
 * 6. Las alertas se resuelven al marcar entrada, al registrar marca manual el supervisor, o al asentar constancia administrativa.
 */
class ControlarIncumplimientosAsistenciaUseCase {

    /**
     * Determina si una asignación es operativa y exigible para control de asistencia presencial.
     * Excluye borradores, descansos, incapacidades médicas, vacaciones y permisos.
     */
    fun esAsignacionExigibleParaAsistencia(asignacion: AsignacionTurno, fechaActualIso: String): Boolean {
        // 1. Debe corresponder a la fecha evaluada
        if (asignacion.fechaIso.isNotBlank() && asignacion.fechaIso != fechaActualIso) {
            return false
        }

        // 2. Debe estar PUBLICADA (las asignaciones en borrador no se operan en control de asistencia)
        if (!asignacion.estado.equals("PUBLICADA", ignoreCase = true)) {
            return false
        }

        // 3. No debe ser descanso programado
        if (asignacion.esDescanso) {
            return false
        }

        // 4. No debe ser incapacidad médica, vacaciones o permiso especial
        val tipo = asignacion.tipoTurno.lowercase().trim()
        if (tipo.contains("incapacidad")) {
            return false
        }
        if (tipo.contains("vacaciones")) {
            return false
        }
        if (tipo.contains("permiso")) {
            return false
        }

        return true
    }

    /**
     * Evalúa si una asignación operativa amerita generar alerta automática por retraso (> 15 min sin marcación de entrada).
     */
    fun debeGenerarAlertaRetrasoInicio(
        asignacion: AsignacionTurno,
        fechaActualIso: String,
        horaActualMinutos: Int,
        tieneEntradaRegistrada: Boolean
    ): Boolean {
        if (!esAsignacionExigibleParaAsistencia(asignacion, fechaActualIso)) {
            return false
        }
        if (tieneEntradaRegistrada) {
            return false
        }

        val partes = asignacion.horario.split("-")
        if (partes.isEmpty()) return false
        val horaInicioProg = parsearMinutos(partes[0]) ?: return false

        return horaActualMinutos >= (horaInicioProg + 15)
    }

    /**
     * Evalúa si un registro de asistencia amerita generar notificación por llegada tarde (> 10 min).
     */
    fun esLlegadaTarde(registro: RegistroAsistencia): Boolean {
        return registro.estado == "TARDE" || registro.minutosTarde > 10
    }

    /**
     * Genera el ID determinístico único por turno para la alerta de retraso de inicio.
     */
    fun generarIdAlertaRetraso(empleadoId: String, fechaIso: String): String {
        return "inc_retraso_${empleadoId}_${fechaIso}"
    }

    /**
     * Genera el ID determinístico único para la alerta de llegada tarde.
     */
    fun generarIdAlertaTarde(registroId: String): String {
        return "inc_tarde_${registroId}"
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
