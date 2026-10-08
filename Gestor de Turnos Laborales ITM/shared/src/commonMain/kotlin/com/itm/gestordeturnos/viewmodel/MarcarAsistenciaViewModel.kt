package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Estados de la jornada laboral
enum class EstadoAsistencia {
    SIN_INICIAR,
    EN_TURNO,
    EN_DESCANSO,
    FINALIZADO
}

data class MarcarAsistenciaState(
    val diaTexto: String = "TURNO DE HOY · MIÉRCOLES 16",
    val tipoTurno: String = "Cierre",
    val horario: String = "14:00 - 22:00",
    val sucursal: String = "CENTRO",
    val estadoActual: EstadoAsistencia = EstadoAsistencia.SIN_INICIAR
)

class MarcarAsistenciaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MarcarAsistenciaState())
    val uiState: StateFlow<MarcarAsistenciaState> = _uiState.asStateFlow()

    // Funciones para cada acción del empleado
    fun marcarEntrada() {
        _uiState.update { it.copy(estadoActual = EstadoAsistencia.EN_TURNO) }
    }

    fun iniciarDescanso() {
        _uiState.update { it.copy(estadoActual = EstadoAsistencia.EN_DESCANSO) }
    }

    fun finalizarDescanso() {
        _uiState.update { it.copy(estadoActual = EstadoAsistencia.EN_TURNO) }
    }

    fun marcarSalida() {
        _uiState.update { it.copy(estadoActual = EstadoAsistencia.FINALIZADO) }
    }
}