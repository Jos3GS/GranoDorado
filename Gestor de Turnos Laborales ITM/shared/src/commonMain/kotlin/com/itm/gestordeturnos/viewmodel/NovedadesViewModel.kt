package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Novedad(
    val id: String,
    val tipo: String,
    val empleado: String,
    val sucursal: String,
    val detalleTurno: String,
    val diferencia: String,
    val estado: String,
    val colorVorde: String
)

data class NovedadesState(
    val novedades: List<Novedad> = emptyList(),
    val filtroActual: String = "Todas"
){
    val pendientesCount: Int
        get() = novedades.count { it.estado == "REPORTADA"}

    val novedadesFiltradas: List<Novedad>
        get() = when(filtroActual){
            "Reportada" -> novedades.filter { it.estado == "REPORTADA" }
            "Revisada" -> novedades.filter { it.estado == "REVISADA" }
            else -> novedades
        }
}

class NovedadesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NovedadesState())
    val uiState: StateFlow<NovedadesState> = _uiState.asStateFlow()

    init {
        cargarNovedadesDemo()
    }

    fun cambiarFiltro(nuevoFiltro: String){
        _uiState.update{ it.copy(filtroActual = nuevoFiltro)}
    }

    private fun cargarNovedadesDemo() {
        val demo = listOf(
            Novedad("1", "Salida antes de la hora", "Ana Pérez", "CENTRO", "Cierre, 14:00 a 22:00 · 2026-09-08", "90 minutos menos que el turno programado", "REPORTADA", "ROJO"),
            Novedad("2", "Ampliación del turno", "Marta León", "CENTRO", "Apertura, 06:00 a 14:00 · 2026-09-09", "90 minutos más que el turno programado", "REPORTADA", "VERDE"),
            Novedad("3", "Ampliación del turno", "Carlos Ruiz", "NORTE", "Cierre, 14:00 a 22:00 · 2026-09-08", "sin diferencia", "REPORTADA", "GRIS"),
            Novedad("4", "Llegada tarde", "Julián Vera", "CENTRO", "Intermedio, 10:00 a 18:00 · 2026-09-07", "25 minutos menos que el turno programado", "REVISADA", "VERDE_CLARO")
        )
        _uiState.update { it.copy(novedades = demo) }
    }
}