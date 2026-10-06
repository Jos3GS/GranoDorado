package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TurnoAsignado(
    val diaSemana: String,
    val numeroDia: String,
    val nombreTurno: String,
    val rangoHoras: String = "",
    val sucursal: String = "",
    val duracion: String="",
    val esDescanso: Boolean = false,
    val modificadoEtiqueta: String? = null,
)

data class MiHorarioState(
    val rangoSemana: String = "Semana del 14 al 20 de Septiembre de 2026.",
    val fechaPublicacion: String = "07/09/2026 A LAS 18:40",
    val totalHoras: String = "37H 30Min",
    val turnos: List<TurnoAsignado> = emptyList()
)

class MiHorarioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MiHorarioState())
    val uiState: StateFlow<MiHorarioState> = _uiState.asStateFlow()

    init {
        cargarHorarioDemo()
    }

    private fun cargarHorarioDemo() {
        val turnosDemo = listOf(
            TurnoAsignado("LUN", "14", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
            TurnoAsignado("MAR", "15", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
            TurnoAsignado(
                diaSemana = "MIÉ",
                numeroDia = "16",
                nombreTurno = "DESCANSO",
                esDescanso = true
            ),
            TurnoAsignado(
                diaSemana = "JUE",
                numeroDia = "17",
                nombreTurno = "Cierre",
                rangoHoras = "14:00 - 22:00",
                sucursal = "CENTRO",
                duracion = "7 h 30 min",
                modificadoEtiqueta = "MODIFICADO 09/09/2026 11:15"
            ),
            TurnoAsignado("VIE", "18", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min")
        )
        _uiState.value = _uiState.value.copy(turnos = turnosDemo)
    }
}