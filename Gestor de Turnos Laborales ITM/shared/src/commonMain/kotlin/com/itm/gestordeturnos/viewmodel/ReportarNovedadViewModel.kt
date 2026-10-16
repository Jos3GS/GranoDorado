package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.NovedadRepository
import com.itm.gestordeturnos.domain.model.EstadoNovedad
import com.itm.gestordeturnos.domain.model.Novedad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportarNovedadState(
    val turnoReferencia: String = "Cierre · 14:00-22:00",
    val fechaReferencia: String = "2026-09-17",
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
        observarNovedades()
    }

    private fun observarNovedades() {
        viewModelScope.launch {
            NovedadRepository.novedades.collect { lista ->
                _uiState.update { it.copy(historial = lista) }
            }
        }
    }

    fun onFechaCambio(nuevaFecha: String) {
        _uiState.update { it.copy(fechaReferencia = nuevaFecha) }
    }

    fun onFechaReferenciaCambiada(nuevaFecha: String) = onFechaCambio(nuevaFecha)

    fun toggleDatePicker(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDatePicker = mostrar) }
    }

    fun onTipoNovedadSeleccionado(tipo: String) {
        _uiState.update {
            it.copy(
                tipoNovedad = tipo,
                menuExpandido = false
            )
        }
    }

    fun toggleMenu(expandir: Boolean) {
        _uiState.update { it.copy(menuExpandido = expandir) }
    }

    fun onDescripcionCambio(nuevaDescripcion: String) {
        _uiState.update { it.copy(descripcion = nuevaDescripcion) }
    }

    fun onDescripcionCambiada(nuevaDescripcion: String) = onDescripcionCambio(nuevaDescripcion)

    fun guardarNovedad() {
        val actual = _uiState.value
        if (actual.tipoNovedad == "Selecciona..." || actual.descripcion.isBlank()) {
            return
        }

        val nueva = Novedad(
            id = (actual.historial.size + 1).toString(),
            tipo = actual.tipoNovedad,
            empleado = SessionManager.usuarioActivo?.nombre ?: "Empleado",
            sucursal = "CENTRO",
            detalleTurno = "${actual.turnoReferencia} · ${actual.fechaReferencia}",
            diferencia = actual.descripcion,
            estado = EstadoNovedad.REPORTADA,
            colorVorde = "VERDE"
        )
        NovedadRepository.reportarNovedad(nueva)

        _uiState.update {
            it.copy(
                tipoNovedad = "Selecciona...",
                descripcion = "",
                fechaReferencia = "2026-09-17"
            )
        }
    }

    fun enviarFormulario() = guardarNovedad()
}
