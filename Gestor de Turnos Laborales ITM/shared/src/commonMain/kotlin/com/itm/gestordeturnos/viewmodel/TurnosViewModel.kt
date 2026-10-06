package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TurnoTemplate(
    val id: String,
    val nombre: String,
    val horario: String,
    val descansos: String,
    val horasEfectivas: String,
    val estado: String,
    val asignaciones: Int
)

data class TurnosState(
    val turnos: List<TurnoTemplate> = emptyList()
)

class TurnosViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TurnosState())
    val uiState: StateFlow<TurnosState> = _uiState.asStateFlow()

    init{
        cargarTurnosDemo()
    }

    private fun cargarTurnosDemo() {
        val demo = listOf(
            TurnoTemplate("1", "Apertura", "06:00-\n14:00", "10:00-\n10:30", "7 h 30\nmin", "ACTIVO", 24),
            TurnoTemplate("2", "Intermedio", "10:00-\n18:00", "13:30-\n14:00", "7 h 30\nmin", "ACTIVO", 16),
            TurnoTemplate("3", "Cierre", "14:00-\n22:00", "18:00-\n18:30", "7 h 30\nmin", "ACTIVO", 12),
            TurnoTemplate("4", "Refuerzo tarde", "16:00-\n22:00", "-", "6 h", "INACTIVO", 3)
        )
        _uiState.value = TurnosState(turnos = demo)
    }
}