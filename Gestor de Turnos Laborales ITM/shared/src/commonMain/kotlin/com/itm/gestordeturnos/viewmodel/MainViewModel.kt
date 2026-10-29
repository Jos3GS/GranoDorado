package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.AsistenciaRepository
import com.itm.gestordeturnos.data.repository.CambiosTurnoRepository
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.NovedadRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.data.service.AuthService
import com.itm.gestordeturnos.data.service.FirebaseAuthService
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OpcionMenu(
    val titulo: String,
    val id: String
)

data class MainState(
    val usuario: UsuarioActual? = null,
    val opcionesMenu: List<OpcionMenu> = emptyList(),
    val opcionSeleccionada: String = ""
)

class MainViewModel(
    private val authService: AuthService = FirebaseAuthService()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            SessionManager.usuarioActivoFlow.collect { usuarioLogueado ->
                if (usuarioLogueado != null) {
                    val opciones = obtenerMenuPorRol(usuarioLogueado.rol)
                    _uiState.update { actual ->
                        actual.copy(
                            usuario = usuarioLogueado,
                            opcionesMenu = opciones,
                            opcionSeleccionada = actual.opcionSeleccionada.takeIf { id -> opciones.any { it.id == id } }
                                ?: opciones.firstOrNull()?.id.orEmpty()
                        )
                    }
                } else {
                    _uiState.value = MainState()
                }
            }
        }
    }

    fun cargarDatos() {
        val usuarioLogueado = SessionManager.usuarioActivo ?: authService.obtenerUsuarioActivo()?.also {
            SessionManager.usuarioActivo = it
        }
        if (usuarioLogueado != null) {
            val opciones = obtenerMenuPorRol(usuarioLogueado.rol)
            _uiState.update {
                it.copy(
                    usuario = usuarioLogueado,
                    opcionesMenu = opciones,
                    opcionSeleccionada = opciones.firstOrNull()?.id ?: ""
                )
            }
        }
    }

    fun cerrarSesion(onCompletado: () -> Unit) {
        viewModelScope.launch {
            authService.cerrarSesion()
            SessionManager.limpiarSesion()
            EmpleadoRepository.detenerSincronizacion()
            TurnoRepository.detenerSincronizacion()
            NovedadRepository.detenerSincronizacion()
            CambiosTurnoRepository.detenerSincronizacion()
            AsistenciaRepository.detenerSincronizacion()
            _uiState.value = MainState()
            onCompletado()
        }
    }

    private fun obtenerMenuPorRol(rol: RolUsuario): List<OpcionMenu> {
        return when (rol){
            RolUsuario.ADMINISTRADOR_GENERAL -> listOf(
                OpcionMenu("Panel", "panel"),
                OpcionMenu("Empleados", "empleados"),
                OpcionMenu("Turnos", "turnos"),
                OpcionMenu("Malla semanal", "malla"),
                OpcionMenu("Novedades", "novedades"),
                OpcionMenu("Cambios de turno", "cambios"),
                OpcionMenu("Asistencia", "asistencia")
            )
            RolUsuario.ADMINISTRADOR_SUCURSAL -> listOf(
                OpcionMenu("Panel", "panel"),
                OpcionMenu("Empleados", "empleados"),
                OpcionMenu("Malla semanal", "malla"),
                OpcionMenu("Novedades", "novedades"),
                OpcionMenu("Cambios de turno", "cambios"),
                OpcionMenu("Asistencia", "asistencia")
            )
            RolUsuario.EMPLEADO -> listOf(
                OpcionMenu("Mi horario", "mi_horario"),
                OpcionMenu("Cambios de turno", "cambios_empleado"),
                OpcionMenu("Reportar novedad", "reportar_novedad"),
                OpcionMenu("Marcar asistencia", "marcar_asistencia")
            )
        }
    }

    fun seleccionarOpcion(idOpcion: String) {
        _uiState.update { it.copy(opcionSeleccionada = idOpcion) }
    }
}
