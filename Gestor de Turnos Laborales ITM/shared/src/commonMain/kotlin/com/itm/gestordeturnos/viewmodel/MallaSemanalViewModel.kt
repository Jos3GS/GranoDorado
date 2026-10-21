package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.DiaColumnaMalla
import com.itm.gestordeturnos.domain.model.FilaMalla
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MallaSemanalState(
    val titulo: String = "Malla semanal",
    val subtitulo: String = "Verifica que la jornada de 06:00 a 22:00 queda cubierta antes de publicar. Los vacíos de cobertura se resaltan.",
    val rangoSemana: String = "14 – 20 sep 2026",
    val sucursal: String = "Centro",
    val opcionesSucursal: List<String> = listOf("Centro", "Norte", "Sur"),
    val estadoMalla: String = "BORRADOR - NO VISIBLE PARA LOS EMPLEADOS",
    val diasSemana: List<DiaColumnaMalla> = listOf(
        DiaColumnaMalla("LUN", "14", cubierto = true),
        DiaColumnaMalla("MAR", "15", cubierto = true),
        DiaColumnaMalla("MIÉ", "16", cubierto = true),
        DiaColumnaMalla("JUE", "17", cubierto = true),
        DiaColumnaMalla("VIE", "18", cubierto = true),
        DiaColumnaMalla("SÁB", "19", cubierto = false), // SIN CUBRIR
        DiaColumnaMalla("DOM", "20", cubierto = true)
    ),
    val filas: List<FilaMalla> = emptyList(),
    val publicado: Boolean = false
)

class MallaSemanalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MallaSemanalState())
    val uiState: StateFlow<MallaSemanalState> = _uiState.asStateFlow()

    init {
        observarFilasMalla()
    }

    private fun observarFilasMalla() {
        viewModelScope.launch {
            TurnoRepository.filasMalla.collect { filas ->
                _uiState.update { it.copy(filas = filas) }
            }
        }
    }

    fun onSucursalCambio(nuevaSucursal: String) {
        _uiState.update { it.copy(sucursal = nuevaSucursal) }
    }

    fun publicarSemana() {
        _uiState.update {
            it.copy(
                publicado = true,
                estadoMalla = "PUBLICADA - VISIBLE PARA LOS EMPLEADOS"
            )
        }
    }
}
