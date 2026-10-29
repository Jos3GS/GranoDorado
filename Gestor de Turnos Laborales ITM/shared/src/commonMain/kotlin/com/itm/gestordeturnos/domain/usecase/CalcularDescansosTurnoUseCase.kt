package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.RangoDescanso

/**
 * Resultado de validación y cálculo de un turno con sus bloques de descanso.
 */
sealed interface ValidacionTurnoResult {
    data class Exito(
        val descansosOrdenados: List<RangoDescanso>,
        val horasEfectivas: String
    ) : ValidacionTurnoResult

    data class Error(val mensaje: String) : ValidacionTurnoResult
}

/**
 * Caso de uso de dominio encargado de las reglas laborales de turnos y descansos:
 * - Duración máxima diaria de 10 horas (600 min).
 * - Bloques de trabajo continuo de máximo 2h 45min (165 min) y mínimo 30 min.
 * - Generación automática de descansos de 15 min y cálculo de horas efectivas.
 */
class CalcularDescansosTurnoUseCase {

    fun parsearHoraAMinutos(hora: String): Int? {
        val partes = hora.trim().split(":")
        if (partes.size != 2) return null
        val h = partes[0].toIntOrNull() ?: return null
        val m = partes[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    fun formatearMinutosAHora(minutos: Int): String {
        val normalizado = ((minutos % 1440) + 1440) % 1440
        val h = (normalizado / 60).toString().padStart(2, '0')
        val m = (normalizado % 60).toString().padStart(2, '0')
        return "$h:$m"
    }

    fun formatearMinutosADuracion(minutos: Int): String {
        val h = minutos / 60
        val m = minutos % 60
        return when {
            m == 0 -> "$h h"
            h == 0 -> "$m min"
            else -> "$h h $m min"
        }
    }

    fun calcularHorasEfectivas(inicioStr: String, finStr: String, descansos: List<RangoDescanso>): String {
        val ini = parsearHoraAMinutos(inicioStr) ?: return ""
        val fn = parsearHoraAMinutos(finStr) ?: return ""
        if (fn <= ini) return ""
        val totalMin = fn - ini
        var descMin = 0
        for (d in descansos) {
            val dIni = parsearHoraAMinutos(d.horaInicio)
            val dFn = parsearHoraAMinutos(d.horaFin)
            if (dIni != null && dFn != null && dFn > dIni) {
                descMin += (dFn - dIni)
            }
        }
        val ef = (totalMin - descMin).coerceAtLeast(0)
        return formatearMinutosADuracion(ef)
    }

    fun generarDescansosAutomaticos(inicioStr: String, finStr: String): List<RangoDescanso> {
        val inicio = parsearHoraAMinutos(inicioStr) ?: return emptyList()
        val fin = parsearHoraAMinutos(finStr) ?: return emptyList()
        val duracion = fin - inicio
        if (duracion <= 165 || duracion > 600) return emptyList()

        val descansos = mutableListOf<RangoDescanso>()
        var cursor = inicio

        while (fin - cursor > 165) {
            var dInicio = cursor + 165
            val dFin = dInicio + 15

            // Si al colocar el descanso a las 2h 45m el bloque restante hasta el fin de turno es < 30m:
            if (fin - dFin in 0..29) {
                dInicio = fin - 45 // 15m descanso + 30m de trabajo final
            }

            if (dInicio - cursor < 30) {
                break
            }

            descansos.add(
                RangoDescanso(
                    horaInicio = formatearMinutosAHora(dInicio),
                    horaFin = formatearMinutosAHora(dInicio + 15)
                )
            )
            cursor = dInicio + 15
        }

        return descansos
    }

    fun sugerirNuevoDescanso(inicioStr: String, finStr: String, actuales: List<RangoDescanso>): RangoDescanso {
        val inicioMin = parsearHoraAMinutos(inicioStr) ?: 480
        val finMin = parsearHoraAMinutos(finStr) ?: 960
        val ultimoFin = actuales.mapNotNull { parsearHoraAMinutos(it.horaFin) }.maxOrNull() ?: inicioMin

        val sugeridoInicio = (ultimoFin + 165).coerceAtMost(finMin - 15).coerceAtLeast(inicioMin)
        val sugeridoFin = (sugeridoInicio + 15).coerceAtMost(finMin)
        return RangoDescanso(
            horaInicio = formatearMinutosAHora(sugeridoInicio),
            horaFin = formatearMinutosAHora(sugeridoFin)
        )
    }

    fun extraerHorasDeTexto(horarioStr: String): Pair<String, String> {
        val limpio = horarioStr.replace("-\r\n", "-").replace("-\n", "-").replace("\r", "").replace("\n", "").trim()
        val partes = limpio.split("-")
        val ini = if (partes.isNotEmpty()) partes[0].trim() else ""
        val fn = if (partes.size > 1) partes[1].trim() else ""
        return Pair(ini, fn)
    }

    fun extraerDescansosDeTexto(descansosStr: String): List<RangoDescanso> {
        if (descansosStr.isBlank() || descansosStr.trim() == "-") return emptyList()
        val normalizado = descansosStr
            .replace("-\r\n", "-")
            .replace("-\n", "-")
            .replace("\r", "")
        return normalizado.split("\n", ";", ",").mapNotNull { item ->
            val limpio = item.trim()
            if (limpio.isBlank() || limpio == "-") null
            else {
                val p = limpio.split("-")
                if (p.size == 2 && p[0].contains(":") && p[1].contains(":")) {
                    RangoDescanso(horaInicio = p[0].trim(), horaFin = p[1].trim())
                } else null
            }
        }
    }

    fun validarYCalcularTurno(
        nombreTurno: String,
        horaInicio: String,
        horaFin: String,
        descansos: List<RangoDescanso>
    ): ValidacionTurnoResult {
        if (nombreTurno.isBlank()) {
            return ValidacionTurnoResult.Error("El nombre del turno es obligatorio.")
        }

        if (horaInicio.isBlank() || horaFin.isBlank()) {
            return ValidacionTurnoResult.Error("Debe especificar la hora de inicio y fin del turno.")
        }

        val inicioMin = parsearHoraAMinutos(horaInicio)
        val finMin = parsearHoraAMinutos(horaFin)
        if (inicioMin == null || finMin == null) {
            return ValidacionTurnoResult.Error("Formato de hora inválido en inicio o fin.")
        }

        if (finMin <= inicioMin) {
            return ValidacionTurnoResult.Error("La hora de fin debe ser posterior a la hora de inicio.")
        }

        val duracionTotalMin = finMin - inicioMin

        // Regla 1: No exceder 10 horas al día (600 minutos)
        if (duracionTotalMin > 600) {
            return ValidacionTurnoResult.Error(
                "Un turno no puede exceder las 10 horas al día (duración actual: ${formatearMinutosADuracion(duracionTotalMin)})."
            )
        }

        data class DescansoParsed(val id: String, val inicio: Int, val fin: Int, val original: RangoDescanso)
        val descansosParsed = mutableListOf<DescansoParsed>()

        for (d in descansos) {
            val dIni = parsearHoraAMinutos(d.horaInicio)
            val dFn = parsearHoraAMinutos(d.horaFin)
            if (dIni == null || dFn == null) {
                return ValidacionTurnoResult.Error("Formato inválido en los descansos.")
            }
            if (dFn <= dIni) {
                return ValidacionTurnoResult.Error(
                    "Cada descanso debe terminar después de su inicio (${d.horaInicio} - ${d.horaFin})."
                )
            }
            if (dIni < inicioMin || dFn > finMin) {
                return ValidacionTurnoResult.Error(
                    "Los descansos deben estar dentro del horario laboral ($horaInicio - $horaFin)."
                )
            }
            descansosParsed.add(DescansoParsed(d.id, dIni, dFn, d))
        }

        descansosParsed.sortBy { it.inicio }

        // Validar no solapamiento entre descansos
        for (i in 0 until descansosParsed.size - 1) {
            if (descansosParsed[i].fin > descansosParsed[i + 1].inicio) {
                return ValidacionTurnoResult.Error("Hay descansos que se solapan entre sí.")
            }
        }

        // Validar reglas de trabajo continuo:
        // Máximo tiempo entre inicio/descanso/fin es de 2h 45min (165 min)
        // Mínimo tiempo continuo de trabajo es de 30 min (excepto si el turno completo es menor a 2h 45m sin descansos)
        if (descansosParsed.isEmpty()) {
            if (duracionTotalMin > 165) {
                return ValidacionTurnoResult.Error(
                    "Máximo tiempo de trabajo continuo es 2 h 45 min. Este turno de ${formatearMinutosADuracion(duracionTotalMin)} requiere descansos."
                )
            }
        } else {
            val primerBloque = descansosParsed.first().inicio - inicioMin
            if (primerBloque > 165) {
                return ValidacionTurnoResult.Error(
                    "El primer bloque de trabajo (${formatearMinutosADuracion(primerBloque)}) excede el límite de 2 h 45 min."
                )
            }
            if (primerBloque < 30) {
                return ValidacionTurnoResult.Error(
                    "El primer bloque de trabajo (${formatearMinutosADuracion(primerBloque)}) debe ser de mínimo 30 minutos."
                )
            }

            for (i in 0 until descansosParsed.size - 1) {
                val bloqueIntermedio = descansosParsed[i + 1].inicio - descansosParsed[i].fin
                if (bloqueIntermedio > 165) {
                    return ValidacionTurnoResult.Error(
                        "Un bloque intermedio de trabajo (${formatearMinutosADuracion(bloqueIntermedio)}) excede el límite de 2 h 45 min."
                    )
                }
                if (bloqueIntermedio < 30) {
                    return ValidacionTurnoResult.Error(
                        "El tiempo de trabajo entre descansos (${formatearMinutosADuracion(bloqueIntermedio)}) debe ser de mínimo 30 minutos."
                    )
                }
            }

            val ultimoBloque = finMin - descansosParsed.last().fin
            if (ultimoBloque > 165) {
                return ValidacionTurnoResult.Error(
                    "El bloque final de trabajo (${formatearMinutosADuracion(ultimoBloque)}) excede el límite de 2 h 45 min."
                )
            }
            if (ultimoBloque < 30) {
                return ValidacionTurnoResult.Error(
                    "El bloque final de trabajo (${formatearMinutosADuracion(ultimoBloque)}) debe ser de mínimo 30 minutos."
                )
            }
        }

        val totalDescansoMin = descansosParsed.sumOf { it.fin - it.inicio }
        val minutosEfectivos = duracionTotalMin - totalDescansoMin
        val horasEfectivasStr = formatearMinutosADuracion(minutosEfectivos)

        return ValidacionTurnoResult.Exito(
            descansosOrdenados = descansosParsed.map { it.original },
            horasEfectivas = horasEfectivasStr
        )
    }
}
