package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.Novedad
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

sealed interface ValidacionNovedadResult {
    data object Valido : ValidacionNovedadResult
    data class Invalido(val mensaje: String) : ValidacionNovedadResult
}

/**
 * Caso de uso de dominio para las reglas de negocio RN-23 y RN-24:
 *
 * - RN-23: Toda novedad debe asociarse a un turno específico y reportar tipo y descripción obligatorios.
 *   No se admiten dos novedades del mismo tipo sobre el mismo turno por el mismo empleado.
 *   El plazo máximo para reportar novedades es de 48 horas exactas posteriores a la finalización del turno;
 *   y si el turno corresponde al día actual, no es posible reportar incidencias de jornada antes del inicio del turno.
 *
 * - RN-24: En novedades que requieren hora real (llegada tarde, salida antes, ampliación de turno),
 *   se valida la coherencia con el turno programado (llegada tarde > inicio, salida antes < fin,
 *   ampliación > fin) y que la hora real reportada se mantenga estrictamente dentro de los límites
 *   operativos del establecimiento (06:00 a 22:00).
 */
class ValidarReporteNovedadUseCase {

    fun obtenerMinutosActuales(): Int {
        return try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            now.hour * 60 + now.minute
        } catch (_: Throwable) {
            0
        }
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

    fun parsearHorario(horario: String): Pair<Int, Int>? {
        val partes = horario.replace("\r", "").replace("\n", " ").trim().split("-")
        if (partes.size >= 2) {
            val ini = parsearMinutos(partes[0].trim()) ?: return null
            val fin = parsearMinutos(partes[1].trim()) ?: return null
            return Pair(ini, fin)
        }
        return null
    }

    /**
     * Valida los requisitos de negocio para el reporte de novedades (RN-23).
     */
    fun validarReporteNovedad(
        empleado: Empleado?,
        tipoNovedad: String,
        descripcion: String,
        fechaReferencia: String,
        horaReal: String = "",
        horarioProgramado: String = "",
        asignacionTurno: AsignacionTurno? = null,
        novedadesExistentes: List<Novedad> = emptyList(),
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        horaActualMinutos: Int = obtenerMinutosActuales()
    ): ValidacionNovedadResult {
        // 1. Validar colaborador activo (RN-11)
        if (empleado == null) {
            return ValidacionNovedadResult.Invalido("El colaborador no se encuentra registrado.")
        }
        if (empleado.estado != EstadoUsuario.ACTIVO) {
            return ValidacionNovedadResult.Invalido("Tu cuenta se encuentra inactiva y no puedes registrar novedades.")
        }

        // 2. Tipo y descripción obligatorios
        if (tipoNovedad.isBlank() || tipoNovedad.equals("Selecciona...", ignoreCase = true)) {
            return ValidacionNovedadResult.Invalido("Por favor selecciona el tipo de novedad.")
        }
        if (descripcion.trim().isBlank()) {
            return ValidacionNovedadResult.Invalido("La descripción de la novedad es obligatoria.")
        }

        // 3. No admitir dos novedades del mismo tipo sobre el mismo turno / fecha para el mismo colaborador
        val empId = empleado.id
        val empNombre = if (empleado.apellido.isNotBlank()) "${empleado.nombre} ${empleado.apellido}".trim() else empleado.nombre
        val fechaRefLimpia = fechaReferencia.trim()

        val yaExisteMismoTipo = novedadesExistentes.any { nov ->
            val coincideEmpleado = (empId.isNotBlank() && nov.empleadoId == empId) ||
                    (empNombre.isNotBlank() && nov.empleado.equals(empNombre, ignoreCase = true)) ||
                    (empleado.nombre.isNotBlank() && nov.empleado.equals(empleado.nombre, ignoreCase = true))

            val coincideTipo = nov.tipo.trim().equals(tipoNovedad.trim(), ignoreCase = true)

            val coincideTurnoOFecha = (asignacionTurno != null && asignacionTurno.id.isNotBlank() && nov.asignacionTurnoId == asignacionTurno.id) ||
                    (fechaRefLimpia.isNotBlank() && (nov.fechaReferencia == fechaRefLimpia || nov.detalleTurno.contains(fechaRefLimpia)))

            coincideEmpleado && coincideTipo && coincideTurnoOFecha
        }

        if (yaExisteMismoTipo) {
            return ValidacionNovedadResult.Invalido("Ya existe una novedad registrada de tipo '$tipoNovedad' para este turno o fecha.")
        }

        // 4. Parsear fecha del turno
        val fechaTurno = try {
            LocalDate.parse(fechaRefLimpia)
        } catch (_: Throwable) {
            fechaActual
        }

        val horarioEfectivo = if (horarioProgramado.isNotBlank()) {
            horarioProgramado
        } else {
            asignacionTurno?.horario ?: ""
        }
        val rangoHoras = parsearHorario(horarioEfectivo)
        val horaInicio = rangoHoras?.first ?: 0
        val horaFin = rangoHoras?.second ?: 1439 // 23:59 por defecto

        val tiposDeJornadaRealizada = listOf(
            "Llegada tarde",
            "Salida antes de la hora",
            "Ampliación del turno",
            "Ausencia injustificada",
            "Ausencia justificada"
        )
        val esNovedadDeJornada = tipoNovedad in tiposDeJornadaRealizada

        // 5. El turno no puede ser futuro y debe haber iniciado si corresponde al día actual
        if (fechaTurno > fechaActual) {
            return ValidacionNovedadResult.Invalido("No es posible reportar novedades sobre turnos futuros.")
        }

        if (esNovedadDeJornada && fechaTurno == fechaActual && horaActualMinutos < horaInicio) {
            return ValidacionNovedadResult.Invalido("No es posible reportar la novedad debido a que el turno programado aún no ha iniciado.")
        }

        // 6. Plazo máximo de 48 horas exactas posteriores a la hora de fin del turno
        val diasDiferencia = fechaActual.toEpochDays() - fechaTurno.toEpochDays()
        val minutosDiferencia = (diasDiferencia * 1440) + (horaActualMinutos - horaFin)
        val limiteMinutos48h = 48 * 60

        if (minutosDiferencia > limiteMinutos48h) {
            return ValidacionNovedadResult.Invalido("El plazo máximo para registrar novedades es de 48 horas posteriores a la finalización del turno.")
        }

        // 7. Validación de coherencia si requiere hora real (RN-24)
        val requiereHoraReal = tipoNovedad in listOf("Llegada tarde", "Salida antes de la hora", "Ampliación del turno")
        if (requiereHoraReal) {
            if (horaReal.isBlank()) {
                return ValidacionNovedadResult.Invalido("Por favor ingresa la hora real para calcular la diferencia de tiempo.")
            }
            val realMin = parsearMinutos(horaReal)
                ?: return ValidacionNovedadResult.Invalido("El formato de la hora real es inválido (debe ser HH:mm).")

            // Regla RN-24: El horario operativo del establecimiento es de 06:00 (360m) a 22:00 (1320m)
            val horaMinOperativa = 6 * 60 // 06:00
            val horaMaxOperativa = 22 * 60 // 22:00

            if (realMin < horaMinOperativa) {
                return ValidacionNovedadResult.Invalido("La hora real no puede ser anterior a la apertura operativa (06:00).")
            }
            if (realMin > horaMaxOperativa) {
                return ValidacionNovedadResult.Invalido("La hora real no puede superar el límite operativo de las 22:00.")
            }

            if (rangoHoras != null) {
                when (tipoNovedad) {
                    "Llegada tarde" -> {
                        if (realMin <= horaInicio) {
                            return ValidacionNovedadResult.Invalido("La llegada tarde debe ser posterior a la hora de inicio programada.")
                        }
                    }
                    "Salida antes de la hora" -> {
                        if (realMin >= horaFin) {
                            return ValidacionNovedadResult.Invalido("La salida antes de tiempo debe ser anterior a la hora de finalización programada.")
                        }
                    }
                    "Ampliación del turno" -> {
                        if (realMin <= horaFin) {
                            return ValidacionNovedadResult.Invalido("La ampliación de turno debe ser posterior a la hora de finalización programada.")
                        }
                    }
                }
            }
        }

        return ValidacionNovedadResult.Valido
    }
}
