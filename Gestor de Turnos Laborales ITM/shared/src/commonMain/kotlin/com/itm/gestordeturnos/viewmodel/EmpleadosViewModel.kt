package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.domain.model.Empleado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EmpleadosState(
    val empleadosOriginales: List<Empleado> = emptyList(),
    val empleadosFiltrados: List<Empleado> = emptyList(),
    val textoBusqueda: String = "",
    val filtroRol: String = "Todos",
    val filtroSucursal: String = "Todas",
    val filtroEstado: String = "Todos",
    val empleadoEnEdicion: Empleado? = null,
    val esNuevoEmpleado: Boolean = false,
    val mostrarDrawerFormulario: Boolean = false,
    val mensajeError: String? = null
)

class EmpleadosViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EmpleadosState())
    val uiState: StateFlow<EmpleadosState> = _uiState.asStateFlow()

    init {
        observarEmpleados()
    }

    private fun observarEmpleados() {
        viewModelScope.launch {
            EmpleadoRepository.empleados.collect { lista ->
                _uiState.update { state ->
                    aplicarFiltros(state.copy(empleadosOriginales = lista))
                }
            }
        }
    }

    private fun calcularIniciales(nombre: String, apellido: String): String {
        val n = nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
        val a = apellido.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
        return if (n.isNotEmpty() || a.isNotEmpty()) "$n$a" else "??"
    }

    fun onBuscarTextoCambiado(nuevoTexto: String) {
        _uiState.update { state ->
            aplicarFiltros(state.copy(textoBusqueda = nuevoTexto))
        }
    }

    fun onFiltroRolCambiado(nuevoRol: String) {
        _uiState.update { state ->
            aplicarFiltros(state.copy(filtroRol = nuevoRol))
        }
    }

    fun onFiltroSucursalCambiado(nuevaSucursal: String) {
        _uiState.update { state ->
            aplicarFiltros(state.copy(filtroSucursal = nuevaSucursal))
        }
    }

    fun onFiltroEstadoCambiado(nuevoEstado: String) {
        _uiState.update { state ->
            aplicarFiltros(state.copy(filtroEstado = nuevoEstado))
        }
    }

    private fun aplicarFiltros(state: EmpleadosState): EmpleadosState {
        val filtrados = state.empleadosOriginales.filter { emp ->
            val coincideTexto = state.textoBusqueda.isBlank() ||
                    emp.nombre.contains(state.textoBusqueda, ignoreCase = true) ||
                    emp.apellido.contains(state.textoBusqueda, ignoreCase = true) ||
                    emp.documento.contains(state.textoBusqueda, ignoreCase = true) ||
                    emp.correo.contains(state.textoBusqueda, ignoreCase = true)

            val coincideRol = state.filtroRol == "Todos" ||
                    emp.cargo.equals(state.filtroRol, ignoreCase = true)

            val coincideSucursal = state.filtroSucursal == "Todas" ||
                    emp.sucursal.equals(state.filtroSucursal, ignoreCase = true)

            val coincideEstado = state.filtroEstado == "Todos" ||
                    emp.estado.equals(state.filtroEstado, ignoreCase = true)

            coincideTexto && coincideRol && coincideSucursal && coincideEstado
        }
        return state.copy(empleadosFiltrados = filtrados)
    }

    fun abrirCrearEmpleado() {
        val nuevo = Empleado(
            id = (uiState.value.empleadosOriginales.size + 1).toString(),
            nombre = "",
            apellido = "",
            documento = "",
            correo = "",
            telefono = "",
            cargo = "BARISTA",
            sucursal = "CENTRO",
            fechaIngreso = "15/01/2024",
            estado = "ACTIVO",
            iniciales = ""
        )
        _uiState.update {
            it.copy(
                empleadoEnEdicion = nuevo,
                esNuevoEmpleado = true,
                mostrarDrawerFormulario = true,
                mensajeError = null
            )
        }
    }

    fun abrirEditarEmpleado(empleado: Empleado) {
        _uiState.update {
            it.copy(
                empleadoEnEdicion = empleado,
                esNuevoEmpleado = false,
                mostrarDrawerFormulario = true,
                mensajeError = null
            )
        }
    }

    fun cerrarFormulario() {
        _uiState.update {
            it.copy(
                mostrarDrawerFormulario = false,
                empleadoEnEdicion = null,
                mensajeError = null
            )
        }
    }

    fun guardarEmpleado(empleadoActualizado: Empleado) {
        val estadoActual = _uiState.value

        // Validaciones de negocio
        if (empleadoActualizado.nombre.isBlank() || empleadoActualizado.apellido.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Nombre y apellido son obligatorios.") }
            return
        }
        if (empleadoActualizado.documento.isBlank()) {
            _uiState.update { it.copy(mensajeError = "El número de documento es obligatorio.") }
            return
        }
        if (empleadoActualizado.correo.isBlank() || !empleadoActualizado.correo.contains("@")) {
            _uiState.update { it.copy(mensajeError = "Ingrese un correo electrónico válido.") }
            return
        }

        // Validación de documento único si es nuevo o si cambió
        val documentoExiste = estadoActual.empleadosOriginales.any {
            it.documento.equals(empleadoActualizado.documento, ignoreCase = true) && it.id != empleadoActualizado.id
        }
        if (documentoExiste) {
            _uiState.update { it.copy(mensajeError = "Ya existe un empleado con este número de documento.") }
            return
        }

        // Regla CA-04.5: Proteger último Administrador General activo
        if (!estadoActual.esNuevoEmpleado) {
            val empleadoPrevio = estadoActual.empleadosOriginales.find { it.id == empleadoActualizado.id }
            val eraAdminGeneralActivo = empleadoPrevio?.cargo == "ADMINISTRADOR GENERAL" && empleadoPrevio.estado == "ACTIVO"
            val dejaraDeSerlo = empleadoActualizado.cargo != "ADMINISTRADOR GENERAL" || empleadoActualizado.estado != "ACTIVO"

            if (eraAdminGeneralActivo && dejaraDeSerlo) {
                val otrosAdminGeneralActivos = estadoActual.empleadosOriginales.count {
                    it.cargo == "ADMINISTRADOR GENERAL" && it.estado == "ACTIVO" && it.id != empleadoActualizado.id
                }
                if (otrosAdminGeneralActivos == 0) {
                    _uiState.update { it.copy(mensajeError = "No se puede inactivar o cambiar el rol del único Administrador General activo del sistema.") }
                    return
                }
            }
        }

        val empleadoFinal = empleadoActualizado.copy(
            iniciales = calcularIniciales(empleadoActualizado.nombre, empleadoActualizado.apellido)
        )

        EmpleadoRepository.guardarEmpleado(empleadoFinal, estadoActual.esNuevoEmpleado)

        _uiState.update { state ->
            state.copy(
                mostrarDrawerFormulario = false,
                empleadoEnEdicion = null,
                mensajeError = null
            )
        }
    }
}
