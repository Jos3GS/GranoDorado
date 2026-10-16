package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.RangoDescanso
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class TurnosState(
    // Estado de la lista principal (Catálogo)
    val turnos: List<TurnoTemplate> = emptyList(),

    // Estado del Modal de Creación / Edición
    val mostrarModalCrearTurno: Boolean = false,
    val esEdicion: Boolean = false,
    val idTurnoEditando: String? = null,
    val estadoTurnoNuevo: String = "ACTIVO",
    val asignacionesTurnoEditando: Int = 0,
    val nombreTurnoNuevo: String = "",
    val horaInicioNuevo: String = "",
    val horaFinNuevo: String = "",
    val descansosNuevo: List<RangoDescanso> = emptyList(),
    val horasEfectivasCalculadas: String = "",
    val mensajeErrorNuevo: String? = null
)

class TurnosViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TurnosState())
    val uiState: StateFlow<TurnosState> = _uiState.asStateFlow()

    init {
        observarTurnosCatalogo()
    }

    private fun observarTurnosCatalogo() {
        viewModelScope.launch {
            TurnoRepository.turnosCatalogo.collect { lista ->
                _uiState.update { it.copy(turnos = lista) }
            }
        }
    }

    // --- Control del Modal ---
    fun toggleModalCrearTurno(mostrar: Boolean) {
        _uiState.update {
            if (mostrar) {
                val inicioDefault = "08:00"
                val finDefault = "16:00"
                val descansosAuto = generarDescansosAutomaticos(inicioDefault, finDefault)
                val horasEfectivas = calcularHorasEfectivas(inicioDefault, finDefault, descansosAuto)
                it.copy(
                    mostrarModalCrearTurno = true,
                    esEdicion = false,
                    idTurnoEditando = null,
                    estadoTurnoNuevo = "ACTIVO",
                    asignacionesTurnoEditando = 0,
                    nombreTurnoNuevo = "",
                    horaInicioNuevo = inicioDefault,
                    horaFinNuevo = finDefault,
                    descansosNuevo = descansosAuto,
                    horasEfectivasCalculadas = horasEfectivas,
                    mensajeErrorNuevo = null
                )
            } else {
                it.copy(
                    mostrarModalCrearTurno = false,
                    esEdicion = false,
                    idTurnoEditando = null,
                    estadoTurnoNuevo = "ACTIVO",
                    asignacionesTurnoEditando = 0,
                    nombreTurnoNuevo = "",
                    horaInicioNuevo = "",
                    horaFinNuevo = "",
                    descansosNuevo = emptyList(),
                    horasEfectivasCalculadas = "",
                    mensajeErrorNuevo = null
                )
            }
        }
    }

    fun abrirEditarTurno(turno: TurnoTemplate) {
        val (iniParse, fnParse) = extraerHorasDeTexto(turno.horario)
        val inicio = if (turno.horaInicio.isNotBlank()) turno.horaInicio else if (iniParse.isNotBlank()) iniParse else "08:00"
        val fin = if (turno.horaFin.isNotBlank()) turno.horaFin else if (fnParse.isNotBlank()) fnParse else "16:00"

        val descansos = if (turno.listaDescansos.isNotEmpty()) {
            turno.listaDescansos
        } else {
            extraerDescansosDeTexto(turno.descansos)
        }

        val horasEf = calcularHorasEfectivas(inicio, fin, descansos)

        _uiState.update {
            it.copy(
                mostrarModalCrearTurno = true,
                esEdicion = true,
                idTurnoEditando = turno.id,
                estadoTurnoNuevo = turno.estado,
                asignacionesTurnoEditando = turno.asignaciones,
                nombreTurnoNuevo = turno.nombre,
                horaInicioNuevo = inicio,
                horaFinNuevo = fin,
                descansosNuevo = descansos,
                horasEfectivasCalculadas = horasEf,
                mensajeErrorNuevo = null
            )
        }
    }

    fun onEstadoTurnoCambio(nuevoEstado: String) {
        _uiState.update { it.copy(estadoTurnoNuevo = nuevoEstado) }
    }

    fun alternarEstadoTurno(idTurno: String) {
        TurnoRepository.alternarEstadoTurno(idTurno)
    }

    // --- Actualización de campos principales ---
    fun onNombreTurnoCambio(nombre: String) {
        _uiState.update { it.copy(nombreTurnoNuevo = nombre, mensajeErrorNuevo = null) }
    }

    fun onHoraInicioCambio(hora: String) {
        _uiState.update { state ->
            val actualizados = state.copy(horaInicioNuevo = hora, mensajeErrorNuevo = null)
            recalcularDescansosSiAplica(actualizados)
        }
    }

    fun onHoraFinCambio(hora: String) {
        _uiState.update { state ->
            val actualizados = state.copy(horaFinNuevo = hora, mensajeErrorNuevo = null)
            recalcularDescansosSiAplica(actualizados)
        }
    }

    private fun recalcularDescansosSiAplica(state: TurnosState): TurnosState {
        val inicio = state.horaInicioNuevo
        val fin = state.horaFinNuevo
        if (inicio.isNotBlank() && fin.isNotBlank()) {
            val iMin = parsearHoraAMinutos(inicio)
            val fMin = parsearHoraAMinutos(fin)
            if (iMin != null && fMin != null && fMin > iMin) {
                val autoDescansos = generarDescansosAutomaticos(inicio, fin)
                val horasEfectivas = calcularHorasEfectivas(inicio, fin, autoDescansos)
                return state.copy(
                    descansosNuevo = autoDescansos,
                    horasEfectivasCalculadas = horasEfectivas
                )
            }
        }
        return state.copy(horasEfectivasCalculadas = "")
    }

    // --- Gestión dinámica de descansos ---
    fun agregarDescanso() {
        _uiState.update { state ->
            val nuevosDescansos = state.descansosNuevo.toMutableList()
            val sugerido = sugerirNuevoDescanso(state.horaInicioNuevo, state.horaFinNuevo, nuevosDescansos)
            nuevosDescansos.add(sugerido)
            val horasEf = calcularHorasEfectivas(state.horaInicioNuevo, state.horaFinNuevo, nuevosDescansos)
            state.copy(
                descansosNuevo = nuevosDescansos,
                horasEfectivasCalculadas = horasEf,
                mensajeErrorNuevo = null
            )
        }
    }

    private fun sugerirNuevoDescanso(inicioStr: String, finStr: String, actuales: List<RangoDescanso>): RangoDescanso {
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

    fun quitarDescanso(id: String) {
        _uiState.update { state ->
            val nuevosDescansos = state.descansosNuevo.filterNot { it.id == id }
            val horasEf = calcularHorasEfectivas(state.horaInicioNuevo, state.horaFinNuevo, nuevosDescansos)
            state.copy(
                descansosNuevo = nuevosDescansos,
                horasEfectivasCalculadas = horasEf,
                mensajeErrorNuevo = null
            )
        }
    }

    fun onDescansoInicioCambio(id: String, hora: String) {
        _uiState.update { state ->
            val actualizados = state.descansosNuevo.map { descanso ->
                if (descanso.id == id) {
                    val iniMin = parsearHoraAMinutos(hora)
                    val duracionActual = parsearHoraAMinutos(descanso.horaFin)?.let { fn ->
                        parsearHoraAMinutos(descanso.horaInicio)?.let { ini -> (fn - ini).takeIf { it >= 15 } }
                    } ?: 15
                    val finAuto = if (iniMin != null) formatearMinutosAHora(iniMin + duracionActual) else descanso.horaFin
                    descanso.copy(horaInicio = hora, horaFin = finAuto)
                } else descanso
            }
            val horasEf = calcularHorasEfectivas(state.horaInicioNuevo, state.horaFinNuevo, actualizados)
            state.copy(
                descansosNuevo = actualizados,
                horasEfectivasCalculadas = horasEf,
                mensajeErrorNuevo = null
            )
        }
    }

    fun onDescansoFinCambio(id: String, hora: String) {
        _uiState.update { state ->
            val actualizados = state.descansosNuevo.map { descanso ->
                if (descanso.id == id) descanso.copy(horaFin = hora) else descanso
            }
            val horasEf = calcularHorasEfectivas(state.horaInicioNuevo, state.horaFinNuevo, actualizados)
            state.copy(
                descansosNuevo = actualizados,
                horasEfectivasCalculadas = horasEf,
                mensajeErrorNuevo = null
            )
        }
    }

    // --- Acción de Guardar con Validaciones ---
    fun guardarTurno() {
        val actual = _uiState.value

        // 1. Nombre
        if (actual.nombreTurnoNuevo.isBlank()) {
            _uiState.update { it.copy(mensajeErrorNuevo = "El nombre del turno es obligatorio.") }
            return
        }

        // 2. Horas de inicio y fin
        if (actual.horaInicioNuevo.isBlank() || actual.horaFinNuevo.isBlank()) {
            _uiState.update { it.copy(mensajeErrorNuevo = "Debe especificar la hora de inicio y fin del turno.") }
            return
        }

        val inicioMin = parsearHoraAMinutos(actual.horaInicioNuevo)
        val finMin = parsearHoraAMinutos(actual.horaFinNuevo)
        if (inicioMin == null || finMin == null) {
            _uiState.update { it.copy(mensajeErrorNuevo = "Formato de hora inválido en inicio o fin.") }
            return
        }

        if (finMin <= inicioMin) {
            _uiState.update { it.copy(mensajeErrorNuevo = "La hora de fin debe ser posterior a la hora de inicio.") }
            return
        }

        val duracionTotalMin = finMin - inicioMin

        // Regla 1: No exceder 10 horas al día (600 minutos)
        if (duracionTotalMin > 600) {
            _uiState.update {
                it.copy(mensajeErrorNuevo = "Un turno no puede exceder las 10 horas al día (duración actual: ${formatearMinutosADuracion(duracionTotalMin)}).")
            }
            return
        }

        // Parsear y ordenar descansos
        data class DescansoParsed(val id: String, val inicio: Int, val fin: Int, val original: RangoDescanso)
        val descansosParsed = mutableListOf<DescansoParsed>()

        for (d in actual.descansosNuevo) {
            val dIni = parsearHoraAMinutos(d.horaInicio)
            val dFn = parsearHoraAMinutos(d.horaFin)
            if (dIni == null || dFn == null) {
                _uiState.update { it.copy(mensajeErrorNuevo = "Todos los descansos deben tener horas válidas.") }
                return
            }

            if (dIni < inicioMin || dFn > finMin) {
                _uiState.update {
                    it.copy(mensajeErrorNuevo = "Los descansos deben estar dentro del horario del turno (${actual.horaInicioNuevo} - ${actual.horaFinNuevo}).")
                }
                return
            }

            if (dFn <= dIni) {
                _uiState.update { it.copy(mensajeErrorNuevo = "La hora de fin del descanso (${d.horaFin}) debe ser posterior a su inicio (${d.horaInicio}).") }
                return
            }

            val duracionDesc = dFn - dIni
            if (duracionDesc < 15) {
                _uiState.update {
                    it.copy(mensajeErrorNuevo = "Cada descanso debe durar al menos 15 minutos (el descanso ${d.horaInicio}-${d.horaFin} dura $duracionDesc min).")
                }
                return
            }

            descansosParsed.add(DescansoParsed(d.id, dIni, dFn, d))
        }

        descansosParsed.sortBy { it.inicio }

        // Reglas de descansos y bloques de trabajo:
        // - Máximo tiempo continuo de trabajo: 2 horas y 45 minutos (165 min)
        // - Mínimo tiempo continuo de trabajo: 30 minutos
        if (descansosParsed.isEmpty()) {
            if (duracionTotalMin > 165) {
                _uiState.update {
                    it.copy(mensajeErrorNuevo = "El turno dura más de 2 h 45 min (${formatearMinutosADuracion(duracionTotalMin)}). Debe incluir descansos de al menos 15 minutos máximo cada 2 h 45 min.")
                }
                return
            }
        } else {
            // Validar bloque inicial (inicio del turno -> primer descanso)
            val bloqueInicial = descansosParsed.first().inicio - inicioMin
            if (bloqueInicial > 165) {
                _uiState.update {
                    it.copy(mensajeErrorNuevo = "El tiempo de trabajo antes del primer descanso (${formatearMinutosADuracion(bloqueInicial)}) excede el máximo permitido de 2 horas y 45 minutos.")
                }
                return
            }
            if (bloqueInicial < 30) {
                _uiState.update {
                    it.copy(mensajeErrorNuevo = "El tiempo de trabajo antes del primer descanso (${formatearMinutosADuracion(bloqueInicial)}) debe ser de al menos 30 minutos.")
                }
                return
            }

            // Validar bloques intermedios entre descansos sucesivos
            for (i in 0 until descansosParsed.size - 1) {
                val finActual = descansosParsed[i].fin
                val inicioSiguiente = descansosParsed[i + 1].inicio
                val bloqueIntermedio = inicioSiguiente - finActual

                if (bloqueIntermedio < 0) {
                    _uiState.update { it.copy(mensajeErrorNuevo = "Los descansos no pueden solaparse entre sí.") }
                    return
                }
                if (bloqueIntermedio > 165) {
                    _uiState.update {
                        it.copy(mensajeErrorNuevo = "El tiempo de trabajo entre descansos (${formatearMinutosADuracion(bloqueIntermedio)}) excede el máximo de 2 horas y 45 minutos.")
                    }
                    return
                }
                if (bloqueIntermedio < 30) {
                    _uiState.update {
                        it.copy(mensajeErrorNuevo = "El tiempo de trabajo entre descansos (${formatearMinutosADuracion(bloqueIntermedio)}) debe ser de al menos 30 minutos.")
                    }
                    return
                }
            }

            // Validar bloque final (último descanso -> fin del turno)
            val bloqueFinal = finMin - descansosParsed.last().fin
            if (bloqueFinal > 165) {
                _uiState.update {
                    it.copy(mensajeErrorNuevo = "El tiempo de trabajo después del último descanso (${formatearMinutosADuracion(bloqueFinal)}) excede el máximo permitido de 2 horas y 45 minutos.")
                }
                return
            }
            if (bloqueFinal < 30) {
                _uiState.update {
                    it.copy(mensajeErrorNuevo = "El tiempo de trabajo después del último descanso (${formatearMinutosADuracion(bloqueFinal)}) debe ser de al menos 30 minutos.")
                }
                return
            }
        }

        // Cálculos de horas efectivas y formato
        val totalDescansoMin = descansosParsed.sumOf { it.fin - it.inicio }
        val minutosEfectivos = duracionTotalMin - totalDescansoMin
        val horasEfectivasStr = formatearMinutosADuracion(minutosEfectivos)

        val horarioStr = "${actual.horaInicioNuevo}-\n${actual.horaFinNuevo}"
        val descansosStr = if (descansosParsed.isNotEmpty()) {
            descansosParsed.joinToString("\n") { "${it.original.horaInicio}-${it.original.horaFin}" }
        } else {
            "-"
        }

        val esEdicion = actual.esEdicion
        val idTurno = if (esEdicion && !actual.idTurnoEditando.isNullOrBlank()) {
            actual.idTurnoEditando
        } else {
            "t_${Random.nextInt(1000, 9999)}"
        }

        val turnoGuardado = TurnoTemplate(
            id = idTurno,
            nombre = actual.nombreTurnoNuevo.trim(),
            horario = horarioStr,
            descansos = descansosStr,
            horasEfectivas = horasEfectivasStr,
            estado = actual.estadoTurnoNuevo,
            asignaciones = if (esEdicion) actual.asignacionesTurnoEditando else 0,
            horaInicio = actual.horaInicioNuevo,
            horaFin = actual.horaFinNuevo,
            listaDescansos = descansosParsed.map { it.original }
        )

        TurnoRepository.guardarTurnoCatalogo(turnoGuardado, esNuevo = !esEdicion)
        toggleModalCrearTurno(false)
    }

    companion object {
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
    }
}
