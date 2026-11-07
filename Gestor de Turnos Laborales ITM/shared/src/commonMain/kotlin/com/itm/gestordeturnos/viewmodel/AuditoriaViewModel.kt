package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.AuditoriaRepository
import com.itm.gestordeturnos.domain.model.EventoAuditoria
import com.itm.gestordeturnos.domain.model.TipoAccionAuditoria
import com.itm.gestordeturnos.domain.repository.AuditoriaRepositoryContract
import com.itm.gestordeturnos.domain.usecase.FiltrarAuditoriaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditoriaState(
    val eventosOriginales: List<EventoAuditoria> = emptyList(),
    val eventosFiltrados: List<EventoAuditoria> = emptyList(),
    val filtroTipoAccion: TipoAccionAuditoria? = null,
    val filtroSucursal: String = "TODAS",
    val filtroFecha: String = "",
    val filtroTexto: String = "",
    val sucursalesDisponibles: List<String> = listOf("TODAS", "CENTRO", "NORTE", "SUR"),
    val tiposAccionDisponibles: List<TipoAccionAuditoria> = TipoAccionAuditoria.entries,
    val cargando: Boolean = false
)

class AuditoriaViewModel(
    private val auditoriaRepository: AuditoriaRepositoryContract = AuditoriaRepository,
    private val filtrarAuditoriaUseCase: FiltrarAuditoriaUseCase = FiltrarAuditoriaUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditoriaState())
    val uiState: StateFlow<AuditoriaState> = _uiState.asStateFlow()

    init {
        auditoriaRepository.iniciarSincronizacionFirestore()
        observarAuditoria()
    }

    private fun observarAuditoria() {
        viewModelScope.launch {
            auditoriaRepository.eventos.collect { lista ->
                _uiState.update { actual ->
                    val filtrados = filtrarAuditoriaUseCase.filtrar(
                        eventos = lista,
                        tipoAccion = actual.filtroTipoAccion,
                        sucursal = actual.filtroSucursal,
                        filtroFecha = actual.filtroFecha,
                        filtroTexto = actual.filtroTexto
                    )
                    actual.copy(
                        eventosOriginales = lista,
                        eventosFiltrados = filtrados
                    )
                }
            }
        }
    }

    fun cambiarFiltroTipo(tipo: TipoAccionAuditoria?) {
        _uiState.update { actual ->
            val filtrados = filtrarAuditoriaUseCase.filtrar(
                eventos = actual.eventosOriginales,
                tipoAccion = tipo,
                sucursal = actual.filtroSucursal,
                filtroFecha = actual.filtroFecha,
                filtroTexto = actual.filtroTexto
            )
            actual.copy(filtroTipoAccion = tipo, eventosFiltrados = filtrados)
        }
    }

    fun cambiarFiltroSucursal(sucursal: String) {
        _uiState.update { actual ->
            val filtrados = filtrarAuditoriaUseCase.filtrar(
                eventos = actual.eventosOriginales,
                tipoAccion = actual.filtroTipoAccion,
                sucursal = sucursal,
                filtroFecha = actual.filtroFecha,
                filtroTexto = actual.filtroTexto
            )
            actual.copy(filtroSucursal = sucursal, eventosFiltrados = filtrados)
        }
    }

    fun cambiarFiltroFecha(fecha: String) {
        _uiState.update { actual ->
            val filtrados = filtrarAuditoriaUseCase.filtrar(
                eventos = actual.eventosOriginales,
                tipoAccion = actual.filtroTipoAccion,
                sucursal = actual.filtroSucursal,
                filtroFecha = fecha,
                filtroTexto = actual.filtroTexto
            )
            actual.copy(filtroFecha = fecha, eventosFiltrados = filtrados)
        }
    }

    fun cambiarFiltroTexto(texto: String) {
        _uiState.update { actual ->
            val filtrados = filtrarAuditoriaUseCase.filtrar(
                eventos = actual.eventosOriginales,
                tipoAccion = actual.filtroTipoAccion,
                sucursal = actual.filtroSucursal,
                filtroFecha = actual.filtroFecha,
                filtroTexto = texto
            )
            actual.copy(filtroTexto = texto, eventosFiltrados = filtrados)
        }
    }

    fun limpiarFiltros() {
        _uiState.update { actual ->
            val filtrados = filtrarAuditoriaUseCase.filtrar(
                eventos = actual.eventosOriginales,
                tipoAccion = null,
                sucursal = "TODAS",
                filtroFecha = "",
                filtroTexto = ""
            )
            actual.copy(
                filtroTipoAccion = null,
                filtroSucursal = "TODAS",
                filtroFecha = "",
                filtroTexto = "",
                eventosFiltrados = filtrados
            )
        }
    }
}
