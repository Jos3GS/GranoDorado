package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.DiaColumnaMalla
import com.itm.gestordeturnos.domain.model.FilaMalla
import com.itm.gestordeturnos.domain.model.TipoCeldaMalla
import com.itm.gestordeturnos.domain.model.TurnoCelda
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Caso de uso de dominio encargado de:
 * 1. Verificar que la jornada operativa (06:00 a 22:00) esté completamente cubierta en un día dado.
 * 2. Recalcular las columnas de la semana con su indicador de cobertura.
 * 3. Calcular el total de horas efectivas semanales de una fila de turnos.
 */
class VerificarCoberturaDiariaUseCase {

    fun recalcularDiasSemana(
        lunes: LocalDate,
        filas: List<FilaMalla>
    ): List<DiaColumnaMalla> {
        val nombresDias = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")
        return (0..6).map { i ->
            val diaFecha = lunes.plus(DatePeriod(days = i))
            val estaCubierto = verificarDiaCubierto(diaIndex = i, filas = filas)
            DiaColumnaMalla(
                diaSemana = nombresDias[i],
                numeroDia = "${diaFecha.dayOfMonth}",
                cubierto = estaCubierto
            )
        }
    }

    fun verificarDiaCubierto(diaIndex: Int, filas: List<FilaMalla>): Boolean {
        val jornadaInicio = 6 * 60
        val jornadaFin = 22 * 60

        val intervalos = mutableListOf<Pair<Int, Int>>()

        for (fila in filas) {
            if (diaIndex in fila.turnos.indices) {
                val turno = fila.turnos[diaIndex]
                if (turno.tipoCelda == TipoCeldaMalla.TURNO) {
                    var ini: Int? = null
                    var fin: Int? = null

                    val partes = turno.horario.split("-")
                    if (partes.size == 2) {
                        ini = parsearMinutos(partes[0])
                        fin = parsearMinutos(partes[1])
                    }

                    if (ini == null || fin == null) {
                        when (turno.tipo.lowercase().trim()) {
                            "apertura" -> { ini = 6 * 60; fin = 14 * 60 }
                            "intermedio" -> { ini = 10 * 60; fin = 18 * 60 }
                            "cierre" -> { ini = 14 * 60; fin = 22 * 60 }
                        }
                    }

                    if (ini != null && fin != null) {
                        if (fin > ini) {
                            val s = maxOf(jornadaInicio, ini)
                            val e = minOf(jornadaFin, fin)
                            if (e > s) intervalos.add(s to e)
                        } else {
                            val s1 = maxOf(jornadaInicio, ini)
                            val e1 = minOf(jornadaFin, 1440)
                            if (e1 > s1) intervalos.add(s1 to e1)
                            val s2 = maxOf(jornadaInicio, 0)
                            val e2 = minOf(jornadaFin, fin)
                            if (e2 > s2) intervalos.add(s2 to e2)
                        }
                    }
                }
            }
        }

        if (intervalos.isEmpty()) return false

        val ordenados = intervalos.sortedBy { it.first }

        var finAlcanzado = jornadaInicio
        for ((start, end) in ordenados) {
            if (start > finAlcanzado) {
                return false
            }
            finAlcanzado = maxOf(finAlcanzado, end)
            if (finAlcanzado >= jornadaFin) {
                return true
            }
        }

        return finAlcanzado >= jornadaFin
    }

    fun calcularTotalHoras(turnos: List<TurnoCelda>): String {
        var minutosTotales = 0
        turnos.forEach { t ->
            if (t.tipoCelda == TipoCeldaMalla.TURNO) {
                val partes = t.horario.split("-")
                if (partes.size == 2) {
                    val ini = parsearMinutos(partes[0])
                    val fin = parsearMinutos(partes[1])
                    if (ini != null && fin != null) {
                        var duracionMin = fin - ini
                        if (duracionMin < 0) duracionMin += 24 * 60
                        val duracionEfectiva = if (duracionMin >= 240) duracionMin - 30 else duracionMin
                        minutosTotales += duracionEfectiva
                    } else {
                        minutosTotales += 450
                    }
                } else {
                    minutosTotales += 450
                }
            }
        }

        val horas = minutosTotales / 60
        val minutos = minutosTotales % 60
        return if (minutos > 0) {
            "$horas h ${minutos.toString().padStart(2, '0')} min"
        } else {
            "$horas h 00 min"
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
}
