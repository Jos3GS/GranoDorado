package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReportarNovedadState(
    val turnoReferencia: String = "Cierre · 14:00-22:00",
    val fechaReferencia: String = "2026-09-17",

    // Nueva variable para controlar el calendario
    val mostrarDatePicker: Boolean = false,

    val tipoNovedad: String = "Selecciona...",
    val descripcion: String = "",
    val menuExpandido: Boolean = false,
    val opcionesNovedad: List<String> = listOf(
        "Llegada tarde",
        "Salida antes de la hora",
        "Ampliación del turno",
        "Incapacidad médica",
        "Permiso especial"
    ),
    val historial: List<Novedad> = emptyList()
)

class ReportarNovedadViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportarNovedadState())
    val uiState: StateFlow<ReportarNovedadState> = _uiState.asStateFlow()

    init {
        cargarHistorial()
    }

    fun onTipoNovedadSeleccionado(tipo: String) {
        // Si no es incapacidad o permiso, forzamos la fecha al turno original
        val fechaRestaurada = if (tipo == "Incapacidad médica" || tipo == "Permiso especial") {
            _uiState.value.fechaReferencia
        } else {
            "2026-09-17" // Fecha por defecto del turno
        }

        _uiState.update {
            it.copy(
                tipoNovedad = tipo,
                menuExpandido = false,
                fechaReferencia = fechaRestaurada
            )
        }
    }

    fun onDescripcionCambiada(texto: String) {
        _uiState.update { it.copy(descripcion = texto) }
    }

    fun toggleMenu(expandido: Boolean) {
        _uiState.update { it.copy(menuExpandido = expandido) }
    }

    // --- NUEVAS FUNCIONES PARA EL CALENDARIO ---
    fun toggleDatePicker(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDatePicker = mostrar) }
    }

    fun onFechaReferenciaCambiada(nuevaFecha: String) {
        _uiState.update { it.copy(fechaReferencia = nuevaFecha, mostrarDatePicker = false) }
    }
    // -------------------------------------------

    fun enviarFormulario() {
        _uiState.update { it.copy(tipoNovedad = "Selecciona...", descripcion = "", fechaReferencia = "2026-09-17") }
    }

    private fun cargarHistorial() {
        val demo = listOf(
            Novedad("1", "Salida antes de la hora", "", "CENTRO", "Cierre, 14:00 a 22:00 · 2026-09-08", "90 minutos menos que el turno programado", "REPORTADA", "ROJO")
        )
        _uiState.update { it.copy(historial = demo) }
    }
}