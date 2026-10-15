package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.EstadoAsistencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MarcarAsistenciaState(
    val diaTexto: String = "TURNO DE HOY",
    val tipoTurno: String = "Sin turno",
    val horario: String = "--:-- - --:--",
    val sucursal: String = "CENTRO",
    val estadoActual: EstadoAsistencia = EstadoAsistencia.SIN_INICIAR
)

class MarcarAsistenciaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MarcarAsistenciaState())
    val uiState: StateFlow<MarcarAsistenciaState> = _uiState.asStateFlow()

    init {
        cargarTurnoDeHoy()
    }

    private fun cargarTurnoDeHoy() {
        val turnoHoy = TurnoRepository.getTurnoHoy()
        if (turnoHoy != null) {
            _uiState.update {
                it.copy(
                    diaTexto = "TURNO DE HOY · ${turnoHoy.diaSemana} ${turnoHoy.numeroDia}",
                    tipoTurno = turnoHoy.tipoTurno ?: "General",
                    horario = turnoHoy.horario ?: "08:00 - 16:00",
                    sucursal = turnoHoy.sucursal ?: "CENTRO"
                )
            }
        }
    }

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
