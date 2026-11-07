package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.EstadoAsistencia
import com.itm.gestordeturnos.domain.model.PausaDescanso
import com.itm.gestordeturnos.domain.model.RegistroAsistencia

data class JornadaEvaluada(
    val estadoTexto: String,
    val estadoAsistencia: EstadoAsistencia,
    val tiempoEfectivoMinutos: Int,
    val esPendienteDeCierre: Boolean
)

/**
 * Caso de uso de dominio para el cálculo de tiempo efectivo de trabajo y control de cierre de jornada (RN-30).
 *
 * Reglas de negocio:
 * 1. Fórmula de cálculo de tiempo efectivo: (horaSalida - horaEntrada) - minutosTotalesDescanso.
 * 2. Un turno sin marca de salida registrada NO pasa a FINALIZADO; permanece en estado no cerrado.
 * 3. Si termina el día o expira el horario del turno programado sin registrar marca de salida,
 *    la jornada se marca explícitamente como "PENDIENTE DE CIERRE".
 * 4. El tiempo efectivo de una jornada pendiente de cierre permanece en 0 hasta que sea cerrada formalmente.
 */
class CalcularJornadaAsistenciaUseCase {

    /**
     * Calcula los minutos netos trabajados descontando todas las pausas de descanso tomadas.
     */
    fun calcularMinutosTrabajados(
        horaEntrada: String,
        horaSalida: String,
        descansos: List<PausaDescanso>
    ): Int {
        val ini = parsearMinutos(horaEntrada) ?: return 0
        val fin = parsearMinutos(horaSalida) ?: return 0
        var totalBruto = fin - ini
        if (totalBruto < 0) totalBruto += 24 * 60

        val totalDescanso = calcularMinutosDescanso(descansos)
        return maxOf(0, totalBruto - totalDescanso)
    }

    /**
     * Calcula los minutos acumulados de una lista de pausas de descanso.
     */
    fun calcularMinutosDescanso(descansos: List<PausaDescanso>): Int {
        var total = 0
        for (desc in descansos) {
            val ini = parsearMinutos(desc.horaInicio)
            val fin = parsearMinutos(desc.horaFin)
            if (ini != null && fin != null) {
                var diff = fin - ini
                if (diff < 0) diff += 24 * 60
                total += diff
            }
        }
        return total
    }

    /**
     * Evalúa el estado de cierre y el tiempo efectivo de un registro de asistencia.
     *
     * @param registro Registro de asistencia a evaluar.
     * @param fechaActualIso Fecha del sistema en formato YYYY-MM-DD.
     * @param horaActualMinutos Minuto del día actual (0..1439).
     */
    fun evaluarJornada(
        registro: RegistroAsistencia,
        fechaActualIso: String,
        horaActualMinutos: Int
    ): JornadaEvaluada {
        val tieneEntrada = registro.horaEntrada != "-" && registro.horaEntrada != "--:--"
        val tieneSalida = registro.horaSalida != "-" && registro.horaSalida != "--:--"

        // 1. Jornada completada con ambas marcas
        if (tieneEntrada && tieneSalida) {
            val minutos = if (registro.tiempoEfectivoMinutos > 0) {
                registro.tiempoEfectivoMinutos
            } else {
                calcularMinutosTrabajados(registro.horaEntrada, registro.horaSalida, registro.listaPausas)
            }
            return JornadaEvaluada(
                estadoTexto = "FINALIZADO",
                estadoAsistencia = EstadoAsistencia.FINALIZADO,
                tiempoEfectivoMinutos = minutos,
                esPendienteDeCierre = false
            )
        }

        // 2. Ingresó pero no ha registrado salida
        if (tieneEntrada && !tieneSalida) {
            val fechaRegistro = registro.fechaIso
            val esDiaAnterior = fechaRegistro.isNotBlank() && fechaRegistro < fechaActualIso

            val horaFinProgMinutos = parsearFinHorario(registro.horarioProgramado)
            val pasoHorarioHoy = (fechaRegistro == fechaActualIso || fechaRegistro.isBlank()) &&
                    (horaFinProgMinutos != null && horaActualMinutos > (horaFinProgMinutos + 30))

            if (esDiaAnterior || pasoHorarioHoy) {
                // RN-30: Terminó el día o el turno sin marca de salida -> PENDIENTE DE CIERRE
                return JornadaEvaluada(
                    estadoTexto = "PENDIENTE DE CIERRE",
                    estadoAsistencia = EstadoAsistencia.EN_TURNO,
                    tiempoEfectivoMinutos = 0,
                    esPendienteDeCierre = true
                )
            }

            // Aún dentro del horario de la jornada
            val estadoActual = when {
                registro.estadoAsistencia == EstadoAsistencia.EN_DESCANSO -> "EN DESCANSO"
                registro.minutosTarde > 0 -> "TARDE"
                else -> "PRESENTE"
            }
            return JornadaEvaluada(
                estadoTexto = estadoActual,
                estadoAsistencia = registro.estadoAsistencia,
                tiempoEfectivoMinutos = 0,
                esPendienteDeCierre = false
            )
        }

        // 3. No ha iniciado jornada
        val estadoSinEntrada = if (registro.fechaIso.isNotBlank() && registro.fechaIso < fechaActualIso) {
            "NO ASISTIÓ"
        } else {
            "PENDIENTE"
        }
        return JornadaEvaluada(
            estadoTexto = estadoSinEntrada,
            estadoAsistencia = EstadoAsistencia.SIN_INICIAR,
            tiempoEfectivoMinutos = 0,
            esPendienteDeCierre = false
        )
    }

    private fun parsearFinHorario(horarioProg: String): Int? {
        val partes = horarioProg.split("-")
        if (partes.size == 2) {
            return parsearMinutos(partes[1])
        }
        return null
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
