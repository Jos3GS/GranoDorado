package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TurnoCelda(
    val tipo: String,
    val horario: String,
    val esDescanso: Boolean = false
)

data class FilaMalla(
    val idEmpleado: String,
    val nombre: String,
    val cargo: String,
    val turnos: List<TurnoCelda>,
    val totalHoras: String
)

data class MallaSemanalState(
    val rangoSemana: String = "Semana del 14 al 20 de septiembre de 2026",
    val sucursal: String = "CENTRO",
    val estadoMalla: String ="BORRADOR",
    val diasSemana: List<String> = listOf("LUN 14", "MAR 15", "MIÉ 16", "JUE 17", "VIE 18", "SÁB 19", "DOM 20"),
    val filas: List<FilaMalla> = emptyList()
)

class MallaSemanalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MallaSemanalState())
    val uiState: StateFlow<MallaSemanalState> = _uiState.asStateFlow()

    init{
        cargarMallaBorrador()
    }

    private fun cargarMallaBorrador() {
        val apertura = TurnoCelda("Apertura", "06 - 14")
        val medio = TurnoCelda("Medio", "10 - 18")
        val cierre = TurnoCelda("Cierre", "14 - 22")
        val descanso = TurnoCelda("Descanso", "-", esDescanso = true)

        val empleadosDemo = listOf(
            FilaMalla(
                idEmpleado = "1",
                nombre = "Ana Pérez",
                cargo = "BARISTA",
                turnos = listOf(apertura, apertura, descanso, cierre, apertura, medio, descanso),
                totalHoras = "37.5 h"
            ),
            FilaMalla(
                idEmpleado = "2",
                nombre = "Carlos Ruiz",
                cargo = "CAJERO",
                turnos = listOf(cierre, descanso, cierre, apertura, cierre, cierre, descanso),
                totalHoras = "37.5 h"
            ),
            FilaMalla(
                idEmpleado = "3",
                nombre = "María Gómez",
                cargo = "COCINERO",
                turnos = listOf(descanso, apertura, apertura, medio, descanso, apertura, apertura),
                totalHoras = "37.5 h"
            ),
            FilaMalla(
                idEmpleado = "4",
                nombre = "Jorge Luis",
                cargo = "BARISTA",
                turnos = listOf(medio, cierre, cierre, descanso, medio, descanso, cierre),
                totalHoras = "37.5 h"
            )
        )

        _uiState.value = _uiState.value.copy(filas = empleadosDemo)
    }
}