package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Empleado(
    val id: String,
    val nombre: String,
    val apellido: String = "",
    val documento: String = "",
    val correo: String = "",
    val telefono: String = "",
    val cargo: String, // Rol: "ADMINISTRADOR GENERAL", "ADMINISTRADOR DE SUCURSAL", "BARISTA", "COCINERO", "CAJERO"
    val sucursal: String, // "CENTRO", "NORTE", "SUR"
    val fechaIngreso: String = "15/01/2024",
    val estado: String = "ACTIVO", // "ACTIVO", "INACTIVO"
    val iniciales: String = ""
)

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
        cargarEmpleadosDemo()
    }

    private fun calcularIniciales(nombre: String, apellido: String): String {
        val n = nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
        val a = apellido.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
        return if (n.isNotEmpty() || a.isNotEmpty()) "$n$a" else "??"
    }

    private fun cargarEmpleadosDemo() {
        val listaDemo = listOf(
            Empleado(
                id = "1",
                nombre = "Diego",
                apellido = "Marín",
                documento = "1000112233",
                correo = "admin.general@granodorado.com",
                telefono = "3001112233",
                cargo = "ADMINISTRADOR GENERAL",
                sucursal = "CENTRO",
                fechaIngreso = "15/01/2024",
                estado = "ACTIVO",
                iniciales = "DM"
            ),
            Empleado(
                id = "2",
                nombre = "Carla",
                apellido = "Ríos",
                documento = "1011223344",
                correo = "carla.rios@granodorado.com",
                telefono = "3002223344",
                cargo = "ADMINISTRADOR DE SUCURSAL",
                sucursal = "CENTRO",
                fechaIngreso = "01/02/2024",
                estado = "ACTIVO",
                iniciales = "CR"
            ),
            Empleado(
                id = "3",
                nombre = "Ana",
                apellido = "Pérez",
                documento = "1022334455",
                correo = "ana.perez@granodorado.com",
                telefono = "3003334455",
                cargo = "BARISTA",
                sucursal = "CENTRO",
                fechaIngreso = "15/02/2024",
                estado = "ACTIVO",
                iniciales = "AP"
            ),
            Empleado(
                id = "4",
                nombre = "Sofía",
                apellido = "Ramos",
                documento = "1033445566",
                correo = "sofia.ramos@granodorado.com",
                telefono = "3004445566",
                cargo = "BARISTA",
                sucursal = "CENTRO",
                fechaIngreso = "01/03/2024",
                estado = "ACTIVO",
                iniciales = "SR"
            ),
            Empleado(
                id = "5",
                nombre = "Marta",
                apellido = "León",
                documento = "1044556677",
                correo = "marta.leon@granodorado.com",
                telefono = "3005556677",
                cargo = "BARISTA",
                sucursal = "CENTRO",
                fechaIngreso = "10/03/2024",
                estado = "ACTIVO",
                iniciales = "ML"
            ),
            Empleado(
                id = "6",
                nombre = "Diego",
                apellido = "Salas",
                documento = "1055667788",
                correo = "diego.salas@granodorado.com",
                telefono = "3006667788",
                cargo = "COCINERO",
                sucursal = "CENTRO",
                fechaIngreso = "20/03/2024",
                estado = "ACTIVO",
                iniciales = "DS"
            ),
            Empleado(
                id = "7",
                nombre = "Julián",
                apellido = "Vera",
                documento = "1066778899",
                correo = "julian.vera@granodorado.com",
                telefono = "3007778899",
                cargo = "CAJERO",
                sucursal = "CENTRO",
                fechaIngreso = "01/04/2024",
                estado = "ACTIVO",
                iniciales = "JV"
            ),
            Empleado(
                id = "8",
                nombre = "Luis",
                apellido = "Gómez",
                documento = "1077889900",
                correo = "luis.gomez@granodorado.com",
                telefono = "3008889900",
                cargo = "COCINERO",
                sucursal = "NORTE",
                fechaIngreso = "15/04/2024",
                estado = "INACTIVO",
                iniciales = "LG"
            )
        )

        _uiState.update {
            it.copy(
                empleadosOriginales = listaDemo,
                empleadosFiltrados = listaDemo
            )
        }
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

        val nuevaLista = if (estadoActual.esNuevoEmpleado) {
            estadoActual.empleadosOriginales + empleadoFinal
        } else {
            estadoActual.empleadosOriginales.map {
                if (it.id == empleadoFinal.id) empleadoFinal else it
            }
        }

        _uiState.update { state ->
            val newState = state.copy(
                empleadosOriginales = nuevaLista,
                mostrarDrawerFormulario = false,
                empleadoEnEdicion = null,
                mensajeError = null
            )
            aplicarFiltros(newState)
        }
    }
}
