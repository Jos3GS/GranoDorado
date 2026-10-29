package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.RangoDescanso
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import com.itm.gestordeturnos.domain.usecase.CalcularDescansosTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidacionTurnoResult
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

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

class TurnosViewModel(
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository,
    private val calcularDescansosUseCase: CalcularDescansosTurnoUseCase = defaultUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TurnosState())
    val uiState: StateFlow<TurnosState> = _uiState.asStateFlow()

    init {
        observarTurnosCatalogo()
        turnoRepository.iniciarSincronizacionFirestore()
    }

    private fun observarTurnosCatalogo() {
        viewModelScope.launch {
            combine(
                turnoRepository.turnosCatalogo,
                turnoRepository.asignaciones
            ) { turnos, asignaciones ->
                val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val diasDesdeLunes = hoy.dayOfWeek.ordinal
                val lunesActual = hoy.minus(DatePeriod(days = diasDesdeLunes))
                val lunesIso = lunesActual.toString()

                // Filtrar solo asignaciones válidas (no descansos) de la semana en curso o futuras
                val asignacionesVigentes = asignaciones.filter { asig ->
                    if (asig.esDescanso || asig.empleadoId.isBlank()) return@filter false
                    val fechaAsig = asig.fecha ?: runCatching { LocalDate.parse(asig.fechaIso) }.getOrNull()
                    if (fechaAsig != null) {
                        fechaAsig >= lunesActual
                    } else if (asig.fechaIso.length >= 10) {
                        asig.fechaIso.take(10) >= lunesIso
                    } else {
                        false
                    }
                }

                turnos.map { turno ->
                    val empleadosAsignados = asignacionesVigentes.filter { asig ->
                        (asig.turnoId.isNotBlank() && asig.turnoId == turno.id) ||
                        (asig.tipoTurno.isNotBlank() && asig.tipoTurno.trim().equals(turno.nombre.trim(), ignoreCase = true))
                    }.map { it.empleadoId.trim() }.distinct().size

                    turno.copy(asignaciones = empleadosAsignados)
                }
            }.collect { listaConAsignaciones ->
                _uiState.update { it.copy(turnos = listaConAsignaciones) }
            }
        }
    }

    // --- Control del Modal ---
    fun toggleModalCrearTurno(mostrar: Boolean) {
        _uiState.update {
            if (mostrar) {
                val inicioDefault = "08:00"
                val finDefault = "16:00"
                val descansosAuto = calcularDescansosUseCase.generarDescansosAutomaticos(inicioDefault, finDefault)
                val horasEfectivas = calcularDescansosUseCase.calcularHorasEfectivas(inicioDefault, finDefault, descansosAuto)
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
        val (iniParse, fnParse) = calcularDescansosUseCase.extraerHorasDeTexto(turno.horario)
        val inicio = if (turno.horaInicio.isNotBlank()) turno.horaInicio else if (iniParse.isNotBlank()) iniParse else "08:00"
        val fin = if (turno.horaFin.isNotBlank()) turno.horaFin else if (fnParse.isNotBlank()) fnParse else "16:00"

        val descansos = if (turno.listaDescansos.isNotEmpty()) {
            turno.listaDescansos
        } else {
            calcularDescansosUseCase.extraerDescansosDeTexto(turno.descansos)
        }

        val horasEf = calcularDescansosUseCase.calcularHorasEfectivas(inicio, fin, descansos)

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
        turnoRepository.alternarEstadoTurno(idTurno)
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
            val iMin = calcularDescansosUseCase.parsearHoraAMinutos(inicio)
            val fMin = calcularDescansosUseCase.parsearHoraAMinutos(fin)
            if (iMin != null && fMin != null && fMin > iMin) {
                val autoDescansos = calcularDescansosUseCase.generarDescansosAutomaticos(inicio, fin)
                val horasEfectivas = calcularDescansosUseCase.calcularHorasEfectivas(inicio, fin, autoDescansos)
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
            val sugerido = calcularDescansosUseCase.sugerirNuevoDescanso(state.horaInicioNuevo, state.horaFinNuevo, nuevosDescansos)
            nuevosDescansos.add(sugerido)
            val horasEf = calcularDescansosUseCase.calcularHorasEfectivas(state.horaInicioNuevo, state.horaFinNuevo, nuevosDescansos)
            state.copy(
                descansosNuevo = nuevosDescansos,
                horasEfectivasCalculadas = horasEf,
                mensajeErrorNuevo = null
            )
        }
    }

    fun quitarDescanso(id: String) {
        _uiState.update { state ->
            val nuevosDescansos = state.descansosNuevo.filterNot { it.id == id }
            val horasEf = calcularDescansosUseCase.calcularHorasEfectivas(state.horaInicioNuevo, state.horaFinNuevo, nuevosDescansos)
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
                    val iniMin = calcularDescansosUseCase.parsearHoraAMinutos(hora)
                    val duracionActual = calcularDescansosUseCase.parsearHoraAMinutos(descanso.horaFin)?.let { fn ->
                        calcularDescansosUseCase.parsearHoraAMinutos(descanso.horaInicio)?.let { ini -> (fn - ini).takeIf { it >= 15 } }
                    } ?: 15
                    val finAuto = if (iniMin != null) calcularDescansosUseCase.formatearMinutosAHora(iniMin + duracionActual) else descanso.horaFin
                    descanso.copy(horaInicio = hora, horaFin = finAuto)
                } else descanso
            }
            val horasEf = calcularDescansosUseCase.calcularHorasEfectivas(state.horaInicioNuevo, state.horaFinNuevo, actualizados)
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
            val horasEf = calcularDescansosUseCase.calcularHorasEfectivas(state.horaInicioNuevo, state.horaFinNuevo, actualizados)
            state.copy(
                descansosNuevo = actualizados,
                horasEfectivasCalculadas = horasEf,
                mensajeErrorNuevo = null
            )
        }
    }

    // --- Acción de Guardar con Validaciones de Dominio ---
    fun guardarTurno() {
        val actual = _uiState.value

        when (
            val validacion = calcularDescansosUseCase.validarYCalcularTurno(
                nombreTurno = actual.nombreTurnoNuevo,
                horaInicio = actual.horaInicioNuevo,
                horaFin = actual.horaFinNuevo,
                descansos = actual.descansosNuevo
            )
        ) {
            is ValidacionTurnoResult.Error -> {
                _uiState.update { it.copy(mensajeErrorNuevo = validacion.mensaje) }
            }
            is ValidacionTurnoResult.Exito -> {
                val horarioStr = "${actual.horaInicioNuevo}-\n${actual.horaFinNuevo}"
                val descansosStr = if (validacion.descansosOrdenados.isNotEmpty()) {
                    validacion.descansosOrdenados.joinToString("\n") { "${it.horaInicio}-${it.horaFin}" }
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
                    horasEfectivas = validacion.horasEfectivas,
                    estado = actual.estadoTurnoNuevo,
                    asignaciones = if (esEdicion) actual.asignacionesTurnoEditando else 0,
                    horaInicio = actual.horaInicioNuevo,
                    horaFin = actual.horaFinNuevo,
                    listaDescansos = validacion.descansosOrdenados
                )

                viewModelScope.launch {
                    _uiState.update { it.copy(mensajeErrorNuevo = null) }
                    val resultado = turnoRepository.guardarTurnoCatalogo(turnoGuardado, esNuevo = !esEdicion)
                    if (resultado.isFailure) {
                        val errorMsg = resultado.exceptionOrNull()?.message ?: "Error al guardar en Firestore"
                        _uiState.update { it.copy(mensajeErrorNuevo = "Error guardando en Firestore: $errorMsg. Verifica la conexión y las reglas de seguridad en Firebase Console.") }
                    } else {
                        toggleModalCrearTurno(false)
                    }
                }
            }
        }
    }

    companion object {
        private val defaultUseCase = CalcularDescansosTurnoUseCase()

        fun parsearHoraAMinutos(hora: String): Int? = defaultUseCase.parsearHoraAMinutos(hora)
        fun formatearMinutosAHora(minutos: Int): String = defaultUseCase.formatearMinutosAHora(minutos)
        fun formatearMinutosADuracion(minutos: Int): String = defaultUseCase.formatearMinutosADuracion(minutos)
        fun calcularHorasEfectivas(inicioStr: String, finStr: String, descansos: List<RangoDescanso>): String =
            defaultUseCase.calcularHorasEfectivas(inicioStr, finStr, descansos)
        fun generarDescansosAutomaticos(inicioStr: String, finStr: String): List<RangoDescanso> =
            defaultUseCase.generarDescansosAutomaticos(inicioStr, finStr)
        fun extraerHorasDeTexto(horarioStr: String): Pair<String, String> =
            defaultUseCase.extraerHorasDeTexto(horarioStr)
        fun extraerDescansosDeTexto(descansosStr: String): List<RangoDescanso> =
            defaultUseCase.extraerDescansosDeTexto(descansosStr)
    }
}
