package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Modelo para la caja superior de notificaciones
data class NotificacionIncumplimiento(
    val id: String,
    val empleado: String,
    val tipo: String,       // Ej: "llegada tarde", "sin marcar entrada"
    val detalleHora: String // Ej: "(10:12)", "", o "(06:15)"
)

// Modelo para la tabla inferior
data class RegistroAsistencia(
    val id: String,
    val empleado: String,
    val sucursal: String,
    val horaEntrada: String, // Ej: "05:58", "-"
    val horaSalida: String,  // Ej: "14:00", "-"
    val estado: String       // "PRESENTE", "AUSENTE", "TARDE"
)

data class AsistenciaState(
    val notificaciones: List<NotificacionIncumplimiento> = emptyList(),
    val registrosDiarios: List<RegistroAsistencia> = emptyList()
)

class AsistenciaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AsistenciaState())
    val uiState: StateFlow<AsistenciaState> = _uiState.asStateFlow()

    init {
        cargarDatosFigma()
    }

    private fun cargarDatosFigma() {
        // Datos de la alerta superior
        val alertas = listOf(
            NotificacionIncumplimiento("1", "Sofía Ramos", "llegada tarde", "(06:15)"),
            NotificacionIncumplimiento("2", "Marta León", "llegada tarde", "(10:12)"),
            NotificacionIncumplimiento("3", "Diego Salas", "llegada tarde", "(14:20)"),
            NotificacionIncumplimiento("4", "Carlos Ruiz", "sin marcar entrada", ""),
            NotificacionIncumplimiento("5", "María Torres", "llegada tarde", "(08:05)"),
            NotificacionIncumplimiento("6", "Laura Cano", "llegada tarde", "(06:10)")
        )

        // Datos de la tabla
        val registros = listOf(
            RegistroAsistencia("1", "Ana Pérez", "CENTRO", "05:58", "-", "PRESENTE"),
            RegistroAsistencia("2", "Sofía Ramos", "CENTRO", "-", "-", "AUSENTE"),
            RegistroAsistencia("3", "Marta León", "CENTRO", "10:12", "-", "TARDE"),
            RegistroAsistencia("4", "Diego Salas", "CENTRO", "-", "-", "AUSENTE"),
            RegistroAsistencia("5", "Julián Vera", "CENTRO", "06:03", "14:00", "PRESENTE"),
            RegistroAsistencia("6", "Carlos Ruiz", "NORTE", "-", "-", "AUSENTE"),
            RegistroAsistencia("7", "María Torres", "NORTE", "-", "-", "AUSENTE")
        )

        _uiState.update {
            it.copy(
                notificaciones = alertas,
                registrosDiarios = registros
            )
        }
    }
}