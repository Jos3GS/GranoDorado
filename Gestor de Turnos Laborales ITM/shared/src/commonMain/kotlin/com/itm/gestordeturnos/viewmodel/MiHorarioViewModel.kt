package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TurnoAsignado(
    val id: String,
    val diaSemana: String,
    val numeroDia: String,
    val tipoTurno: String?,
    val horario: String?,
    val sucursal: String?,
    val duracion: String?,
    val etiquetaModificado: String? = null,
    val esDescanso: Boolean = false,
    val sinAsignar: Boolean = false
)

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
        cargarHorarioSemana()
    }

    private fun cargarHorarioSemana() {
        val semanaDemo = listOf(
            TurnoAsignado("1", "LUN", "14", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
            TurnoAsignado("2", "MAR", "15", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
            TurnoAsignado("3", "MIÉ", "16", null, null, null, null, esDescanso = true),
            TurnoAsignado("4", "JUE", "17", "Cierre", "14:00 - 22:00", "CENTRO", "7 h 30 min", etiquetaModificado = "MODIFICADO 09/09/2026 11:15"),
            TurnoAsignado("5", "VIE", "18", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
            TurnoAsignado("6", "SÁB", "19", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
            TurnoAsignado("7", "DOM", "20", null, null, null, null, sinAsignar = true)
        )
        _uiState.update { it.copy(turnos = semanaDemo) }
    }
}