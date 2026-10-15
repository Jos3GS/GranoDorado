package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.TurnoAsignado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MiHorarioState(
    val rangoSemana: String = "Semana del 14 al 20 de septiembre de 2026.",
    val fechaPublicacion: String = "HORARIO PUBLICADO EL 07/09/2026 A LAS 18:40",
    val totalHoras: String = "37 h 30 min",
    val turnos: List<TurnoAsignado> = emptyList()
)

class MiHorarioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MiHorarioState())
    val uiState: StateFlow<MiHorarioState> = _uiState.asStateFlow()

    init {
        observarHorarioSemana()
    }

    private fun observarHorarioSemana() {
        viewModelScope.launch {
            TurnoRepository.horarioSemana.collect { lista ->
                _uiState.update { it.copy(turnos = lista) }
            }
        }
    }
}
