package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.AsistenciaRepository
import com.itm.gestordeturnos.domain.model.NotificacionIncumplimiento
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AsistenciaState(
    val notificaciones: List<NotificacionIncumplimiento> = emptyList(),
    val registrosDiarios: List<RegistroAsistencia> = emptyList()
)

class AsistenciaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AsistenciaState())
    val uiState: StateFlow<AsistenciaState> = _uiState.asStateFlow()

    init {
        observarAsistencia()
    }

    private fun observarAsistencia() {
        viewModelScope.launch {
            combine(
                AsistenciaRepository.notificaciones,
                AsistenciaRepository.registrosDiarios
            ) { notificaciones, registros ->
                notificaciones to registros
            }.collect { (notificaciones, registros) ->
                _uiState.update {
                    it.copy(
                        notificaciones = notificaciones,
                        registrosDiarios = registros
                    )
                }
            }
        }
    }
}
