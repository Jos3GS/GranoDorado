package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.NovedadRepository
import com.itm.gestordeturnos.domain.model.Novedad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
        observarNovedades()
    }

    private fun observarNovedades() {
        viewModelScope.launch {
            NovedadRepository.novedades.collect { lista ->
                _uiState.update { it.copy(novedades = lista) }
            }
        }
    }

    fun cambiarFiltro(nuevoFiltro: String){
        _uiState.update{ it.copy(filtroActual = nuevoFiltro)}
    }
}
