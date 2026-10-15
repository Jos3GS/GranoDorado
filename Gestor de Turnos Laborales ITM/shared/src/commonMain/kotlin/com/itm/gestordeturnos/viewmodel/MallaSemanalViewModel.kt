package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.FilaMalla
import com.itm.gestordeturnos.domain.model.TurnoCelda
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MallaSemanalState(
    val rangoSemana: String = "Semana del 14 al 20 de septiembre de 2026",
    val sucursal: String = "CENTRO",
    val estadoMalla: String = "BORRADOR",
    val diasSemana: List<String> = listOf("LUN 14", "MAR 15", "MIÉ 16", "JUE 17", "VIE 18", "SÁB 19", "DOM 20"),
    val filas: List<FilaMalla> = emptyList()
)

class MallaSemanalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MallaSemanalState())
    val uiState: StateFlow<MallaSemanalState> = _uiState.asStateFlow()

    init{
        observarFilasMalla()
    }

    private fun observarFilasMalla() {
        viewModelScope.launch {
            TurnoRepository.filasMalla.collect { filas ->
                _uiState.update { it.copy(filas = filas) }
            }
        }
    }
}
