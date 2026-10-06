package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class RolUsuario{
    ADMINISTRADOR_GENERAL,
    ADMINISTRADOR_SUCURSAL,
    EMPLEADO
}

data class UsuarioActual(
    val nombre: String,
    val iniciales: String,
    val cargo: String,
    val rol: RolUsuario
)

data class OpcionMenu(
    val titulo: String,
    val id: String
)

data class MainState(
    val usuario: UsuarioActual? = null,
    val opcionesMenu: List<OpcionMenu> = emptyList(),
    val opcionSeleccionada: String = ""
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    init {
        cargarUsuarioPrueba(RolUsuario.ADMINISTRADOR_GENERAL)
    }

    private fun cargarUsuarioPrueba(rol: RolUsuario) {
        val usuario = when (rol){
            RolUsuario.ADMINISTRADOR_GENERAL -> UsuarioActual("Diego Marin", "DM", "ADMINISTRADOR GENERAL", rol)
            RolUsuario.ADMINISTRADOR_SUCURSAL -> UsuarioActual("Carla Ríos", "CR", "ADMINISTRADOR DE SUCURSAL", rol)
            RolUsuario.EMPLEADO -> UsuarioActual("Ana Pérez", "AP", "BARISTA", rol)
        }

        val menu = obtenerMenuPorRol(rol)

        _uiState.update {
            it.copy(
                usuario = usuario,
                opcionesMenu = menu,
                opcionSeleccionada = menu.firstOrNull()?.id ?: ""
            )
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