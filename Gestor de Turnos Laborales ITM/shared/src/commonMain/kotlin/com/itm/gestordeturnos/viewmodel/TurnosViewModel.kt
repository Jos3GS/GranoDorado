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

data class TurnosState(
    // Estado de la lista principal (Catálogo)
    val turnos: List<TurnoTemplate> = emptyList(),

    // Estado del Modal de Creación
    val mostrarModalCrearTurno: Boolean = false,
    val nombreTurnoNuevo: String = "",
    val horaInicioNuevo: String = "",
    val horaFinNuevo: String = "",
    val descansosNuevo: List<RangoDescanso> = emptyList()
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
                it.copy(mostrarModalCrearTurno = true)
            } else {
                it.copy(
                    mostrarModalCrearTurno = false,
                    nombreTurnoNuevo = "",
                    horaInicioNuevo = "",
                    horaFinNuevo = "",
                    descansosNuevo = emptyList()
                )
            }
        }
    }

    // --- Actualización de campos principales ---
    fun onNombreTurnoCambio(nombre: String) {
        _uiState.update { it.copy(nombreTurnoNuevo = nombre) }
    }

    fun onHoraInicioCambio(hora: String) {
        _uiState.update { it.copy(horaInicioNuevo = hora) }
    }

    fun onHoraFinCambio(hora: String) {
        _uiState.update { it.copy(horaFinNuevo = hora) }
    }

    // --- Gestión dinámica de descansos ---
    fun agregarDescanso() {
        _uiState.update {
            val nuevosDescansos = it.descansosNuevo.toMutableList()
            nuevosDescansos.add(RangoDescanso())
            it.copy(descansosNuevo = nuevosDescansos)
        }
    }

    fun quitarDescanso(id: String) {
        _uiState.update {
            val nuevosDescansos = it.descansosNuevo.filterNot { descanso -> descanso.id == id }
            it.copy(descansosNuevo = nuevosDescansos)
        }
    }

    fun onDescansoInicioCambio(id: String, hora: String) {
        _uiState.update { state ->
            val actualizados = state.descansosNuevo.map { descanso ->
                if (descanso.id == id) descanso.copy(horaInicio = hora) else descanso
            }
            state.copy(descansosNuevo = actualizados)
        }
    }

    fun onDescansoFinCambio(id: String, hora: String) {
        _uiState.update { state ->
            val actualizados = state.descansosNuevo.map { descanso ->
                if (descanso.id == id) descanso.copy(horaFin = hora) else descanso
            }
            state.copy(descansosNuevo = actualizados)
        }
    }

    // --- Acción de Guardar ---
    fun guardarTurno() {
        val actual = _uiState.value
        val nombre = actual.nombreTurnoNuevo.trim().ifEmpty { "Turno Personalizado" }
        val horarioStr = if (actual.horaInicioNuevo.isNotBlank() && actual.horaFinNuevo.isNotBlank()) {
            "${actual.horaInicioNuevo}-\n${actual.horaFinNuevo}"
        } else {
            "08:00-\n16:00"
        }
        val descansosStr = if (actual.descansosNuevo.isNotEmpty()) {
            actual.descansosNuevo.joinToString("\n") { "${it.horaInicio}-${it.horaFin}" }
        } else {
            "-"
        }

        val nuevo = TurnoTemplate(
            id = (actual.turnos.size + 1).toString(),
            nombre = nombre,
            horario = horarioStr,
            descansos = descansosStr,
            horasEfectivas = "8 h",
            estado = "ACTIVO",
            asignaciones = 0
        )

        TurnoRepository.agregarTurnoCatalogo(nuevo)
        toggleModalCrearTurno(false)
    }
}
