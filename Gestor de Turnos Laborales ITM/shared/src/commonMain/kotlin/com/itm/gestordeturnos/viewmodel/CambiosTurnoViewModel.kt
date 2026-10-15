package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.CambiosTurnoRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.SolicitudCambio
import com.itm.gestordeturnos.domain.model.TurnoCompaneroSeleccionable
import com.itm.gestordeturnos.domain.model.TurnoSeleccionable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CambiosTurnoState(
    val solicitudes: List<SolicitudCambio> = emptyList(),
    val mostrarModalSolicitud: Boolean = false,
    val turnoPropioSeleccionado: TurnoSeleccionable? = null,
    val turnosPropios: List<TurnoSeleccionable> = emptyList(),
    val turnosCompaneros: List<TurnoCompaneroSeleccionable> = emptyList(),
)

class CambiosTurnoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CambiosTurnoState())
    val uiState: StateFlow<CambiosTurnoState> = _uiState.asStateFlow()

    init {
        observarDatos()
    }

    private fun observarDatos() {
        viewModelScope.launch {
            combine(
                CambiosTurnoRepository.solicitudes,
                TurnoRepository.turnosPropios,
                TurnoRepository.turnosCompaneros
            ) { solicitudes, propios, companeros ->
                Triple(solicitudes, propios, companeros)
            }.collect { (solicitudes, propios, companeros) ->
                _uiState.update {
                    it.copy(
                        solicitudes = solicitudes,
                        turnosPropios = propios,
                        turnosCompaneros = companeros
                    )
                }
            }
        }
    }

    fun toggleModalSolicitud(mostrar: Boolean) {
        _uiState.update {
            it.copy(
                mostrarModalSolicitud = mostrar,
                turnoPropioSeleccionado = if (!mostrar) null else it.turnoPropioSeleccionado
            )
        }
    }

    fun seleccionarTurnoPropio(turno: TurnoSeleccionable) {
        _uiState.update { it.copy(turnoPropioSeleccionado = turno) }
    }
}
