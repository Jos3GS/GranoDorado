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

data class TurnoSeleccionable(
    val id: String,
    val descripcion: String,
    val horario: String
)

data class TurnoCompaneroSeleccionable(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val horario: String
)

data class CambiosTurnoState(
    val solicitudes: List<SolicitudCambio> = emptyList(),
    val mostrarModalSolicitud: Boolean = false,
    val turnoPropioSeleccionado: TurnoSeleccionable? = null,
    val turnosPropios: List <TurnoSeleccionable> = emptyList(),
    val turnosCompaneros : List<TurnoCompaneroSeleccionable> = emptyList(),
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
        val turnosPropiosDemo = listOf(
            TurnoSeleccionable("1", "Lunes 14 · Apertura", "06:00-14:00"),
            TurnoSeleccionable("2", "Martes 15 · Apertura", "06:00-14:00"),
            TurnoSeleccionable("3", "Jueves 17 · Cierre", "14:00-22:00"),
            TurnoSeleccionable("4", "Viernes 18 · Apertura", "06:00-14:00"),
            TurnoSeleccionable("5", "Sábado 19 · Apertura", "06:00-14:00")
        )

        val turnosCompanerosDemo = listOf(
            TurnoCompaneroSeleccionable("c1", "Sofía Ramos", "Miércoles 16 · Intermedio", "10:00-18:00"),
            TurnoCompaneroSeleccionable("c2", "Marta León", "Miércoles 16 · Cierre", "14:00-22:00"),
            TurnoCompaneroSeleccionable("c3", "Marta León", "Domingo 20 · Cierre", "14:00-22:00")
        )

        _uiState.update { it.copy(solicitudes = demo, turnosPropios = turnosPropiosDemo, turnosCompaneros = turnosCompanerosDemo) }
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