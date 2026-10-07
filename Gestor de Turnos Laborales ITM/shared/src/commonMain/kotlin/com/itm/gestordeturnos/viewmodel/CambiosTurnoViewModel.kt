package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SolicitudCambio(
    val id: String,
    val numeroSolicitud: String,
    val sucursal: String,
    val estadoResumen: String,
    val nombreEntrega: String,
    val tipoTurnoEntrega: String,
    val detalleTurnoEntrega: String,
    val nombreRecibe: String,
    val tipoTurnoRecibe: String,
    val detalleTurnoRecibe: String,
    val motivo: String,
    val validacionesOk: Boolean,
    val esRemitente: Boolean = false,
)

data class CambiosTurnoState(
    val solicitudes: List<SolicitudCambio> = emptyList()
)
class CambiosTurnoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CambiosTurnoState())
    val uiState: StateFlow<CambiosTurnoState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        val demo = listOf(
            SolicitudCambio(
                id = "1",
                numeroSolicitud = "#S1",
                sucursal = "CENTRO",
                estadoResumen = "ACEPTADA POR EL COMPAÑERO",
                nombreEntrega = "José Manuel",
                tipoTurnoEntrega = "Cierre",
                detalleTurnoEntrega = "2026-09-17 · 14:00 a 22:00",
                nombreRecibe = "Sofía Ramos",
                tipoTurnoRecibe = "Apertura",
                detalleTurnoRecibe = "2026-09-18 · 06:00 a 14:00",
                motivo = "Tengo una cita médica el jueves en la tarde.",
                validacionesOk = true
            ),
            SolicitudCambio(
                id = "2",
                numeroSolicitud = "#S2",
                sucursal = "CENTRO",
                estadoResumen = "SOLICITADA",
                nombreEntrega = "Marta León",
                tipoTurnoEntrega = "Apertura",
                detalleTurnoEntrega = "2026-09-19 · 06:00 a 14:00",
                nombreRecibe = "José Manuel",
                tipoTurnoRecibe = "Cierre",
                detalleTurnoRecibe = "2026-09-20 · 14:00 a 22:00",
                motivo = "Asunto familiar.",
                validacionesOk = true
            )
        )
        _uiState.update { it.copy(solicitudes = demo) }
    }
}