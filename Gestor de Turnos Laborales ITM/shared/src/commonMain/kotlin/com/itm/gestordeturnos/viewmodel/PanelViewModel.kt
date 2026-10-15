package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.CambiosTurnoRepository
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.NovedadRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PanelState(
    val saludo: String = "HOLA",
    val empleadosActivos: Int = 0,
    val turnosCatalogo: Int = 0,
    val novedadesPendientes: Int = 0,
    val cambiosPorAprobar: Int = 0,
    val semanaEnCurso: String = "SEMANA EN CURSO 14 - 20 SEP 2026",
    val estadoMalla: String = "La malla esta en borrador",
    val rondaDelDia: List<String> = emptyList()
)

class PanelViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PanelState())
    val uiState: StateFlow<PanelState> = _uiState.asStateFlow()

    fun cargarDatos(usuario: UsuarioActual) {
        viewModelScope.launch {
            combine(
                EmpleadoRepository.empleados,
                TurnoRepository.turnosCatalogo,
                NovedadRepository.novedades,
                CambiosTurnoRepository.solicitudes
            ) { empleados, turnos, novedades, solicitudes ->
                val empActivos = if (usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL) {
                    empleados.count { it.estado.equals("ACTIVO", ignoreCase = true) }
                } else {
                    empleados.count { it.estado.equals("ACTIVO", ignoreCase = true) && it.sucursal.equals("CENTRO", ignoreCase = true) }
                }

                val turnosActivos = turnos.count { it.estado.equals("ACTIVO", ignoreCase = true) }
                val novPendientes = novedades.count { it.estado.equals("REPORTADA", ignoreCase = true) }
                val cambiosPendientes = solicitudes.count {
                    it.estadoResumen == "SOLICITADA" || it.estadoResumen == "ACEPTADA POR EL COMPAÑERO"
                }

                val ronda = listOf(
                    "$novPendientes novedades esperan revisión",
                    "$cambiosPendientes solicitudes de cambio por decidir",
                    "Publicar la semana antes del domingo"
                )

                PanelState(
                    saludo = "BUENAS TARDES",
                    empleadosActivos = empActivos,
                    turnosCatalogo = turnosActivos,
                    novedadesPendientes = novPendientes,
                    cambiosPorAprobar = cambiosPendientes,
                    semanaEnCurso = "SEMANA EN CURSO 14 - 20 SEP 2026",
                    estadoMalla = "La malla esta en borrador",
                    rondaDelDia = ronda
                )
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }
}
