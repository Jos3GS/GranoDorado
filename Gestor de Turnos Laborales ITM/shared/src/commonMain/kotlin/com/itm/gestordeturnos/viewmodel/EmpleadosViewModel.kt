package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.data.service.AuthService
import com.itm.gestordeturnos.data.service.FirebaseAuthService
import com.itm.gestordeturnos.domain.model.Cargo
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoEmpleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

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
    val mensajeError: String? = null,
    val isGuardando: Boolean = false,
    val mensajeExito: String? = null
)

class EmpleadosViewModel(
    private val authService: AuthService = FirebaseAuthService()
) : ViewModel() {
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

    private fun obtenerFechaActual(): String {
        val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dia = hoy.dayOfMonth.toString().padStart(2, '0')
        val mes = hoy.monthNumber.toString().padStart(2, '0')
        val anio = hoy.year
        return "$dia/$mes/$anio"
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

    fun limpiarMensajeExito() {
        _uiState.update { it.copy(mensajeExito = null) }
    }

    private fun aplicarFiltros(state: EmpleadosState): EmpleadosState {
        val filtrados = state.empleadosOriginales.filter { emp ->
            val coincideTexto = state.textoBusqueda.isBlank() ||
                    emp.nombre.contains(state.textoBusqueda, ignoreCase = true) ||
                    emp.apellido.contains(state.textoBusqueda, ignoreCase = true) ||
                    emp.documento.toString().contains(state.textoBusqueda, ignoreCase = true) ||
                    emp.correo.contains(state.textoBusqueda, ignoreCase = true)

            val coincideRol = state.filtroRol == "Todos" ||
                    emp.cargo.displayName.equals(state.filtroRol, ignoreCase = true) ||
                    emp.cargo.name.equals(state.filtroRol, ignoreCase = true)

            val coincideSucursal = state.filtroSucursal == "Todas" ||
                    emp.sucursal.equals(state.filtroSucursal, ignoreCase = true)

            val coincideEstado = state.filtroEstado == "Todos" ||
                    emp.estado.displayName.equals(state.filtroEstado, ignoreCase = true) ||
                    emp.estado.name.equals(state.filtroEstado, ignoreCase = true)

            coincideTexto && coincideRol && coincideSucursal && coincideEstado
        }
        return state.copy(empleadosFiltrados = filtrados)
    }

    fun abrirCrearEmpleado() {
        val idGenerado = "emp_${Random.nextInt(10000, 99999)}"
        val nuevo = Empleado(
            id = idGenerado,
            nombre = "",
            apellido = "",
            documento = 0L,
            correo = "",
            telefono = "",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO",
            sucursalId = "CENTRO",
            fechaIngreso = obtenerFechaActual(),
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
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
        if (empleadoActualizado.documento <= 0L) {
            _uiState.update { it.copy(mensajeError = "El número de documento es obligatorio.") }
            return
        }
        if (empleadoActualizado.correo.isBlank() || !empleadoActualizado.correo.contains("@")) {
            _uiState.update { it.copy(mensajeError = "Ingrese un correo electrónico válido.") }
            return
        }

        // Validación de documento único
        val documentoExiste = estadoActual.empleadosOriginales.any {
            it.documento == empleadoActualizado.documento && it.id != empleadoActualizado.id
        }
        if (documentoExiste) {
            _uiState.update { it.copy(mensajeError = "Ya existe un empleado con este número de documento.") }
            return
        }

        // Validación de correo único
        val correoExiste = estadoActual.empleadosOriginales.any {
            it.correo.equals(empleadoActualizado.correo, ignoreCase = true) && it.id != empleadoActualizado.id
        }
        if (correoExiste) {
            _uiState.update { it.copy(mensajeError = "Ya existe un empleado con este correo electrónico.") }
            return
        }

        // Regla CA-04.5: Proteger último Administrador General activo
        if (!estadoActual.esNuevoEmpleado) {
            val empleadoPrevio = estadoActual.empleadosOriginales.find { it.id == empleadoActualizado.id }
            val eraAdminGeneralActivo = empleadoPrevio?.cargo == Cargo.ADMINISTRADOR_GENERAL && empleadoPrevio.estado == EstadoUsuario.ACTIVO
            val dejaraDeSerlo = empleadoActualizado.cargo != Cargo.ADMINISTRADOR_GENERAL || empleadoActualizado.estado != EstadoUsuario.ACTIVO

            if (eraAdminGeneralActivo && dejaraDeSerlo) {
                val otrosAdminGeneralActivos = estadoActual.empleadosOriginales.count {
                    it.cargo == Cargo.ADMINISTRADOR_GENERAL && it.estado == EstadoUsuario.ACTIVO && it.id != empleadoActualizado.id
                }
                if (otrosAdminGeneralActivos == 0) {
                    _uiState.update { it.copy(mensajeError = "No se puede inactivar o cambiar el rol del único Administrador General activo del sistema.") }
                    return
                }
            }
        }

        val empleadoPrevio = if (!estadoActual.esNuevoEmpleado) {
            estadoActual.empleadosOriginales.find { it.id == empleadoActualizado.id }
        } else null

        val sucursalCambio = empleadoPrevio != null &&
            !empleadoPrevio.sucursal.equals(empleadoActualizado.sucursal, ignoreCase = true)

        val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val diasDesdeLunes = hoy.dayOfWeek.ordinal
        val lunesActual = hoy.minus(DatePeriod(days = diasDesdeLunes))
        val lunesProximaSemana = lunesActual.plus(DatePeriod(days = 7))
        val fechaLunesProximaSemanaIso = lunesProximaSemana.toString()

        val empleadoFinal = if (sucursalCambio && empleadoPrevio != null) {
            // El cambio de sucursal regirá a partir de la próxima semana, conservando registros pasados y actuales
            empleadoActualizado.copy(
                sucursal = empleadoActualizado.sucursal,
                sucursalId = empleadoActualizado.sucursal,
                sucursalAnterior = empleadoPrevio.sucursal,
                fechaCambioSucursal = fechaLunesProximaSemanaIso,
                iniciales = calcularIniciales(empleadoActualizado.nombre, empleadoActualizado.apellido)
            )
        } else {
            empleadoActualizado.copy(
                sucursalId = empleadoActualizado.sucursal,
                iniciales = calcularIniciales(empleadoActualizado.nombre, empleadoActualizado.apellido)
            )
        }

        val esNuevo = estadoActual.esNuevoEmpleado

        viewModelScope.launch {
            _uiState.update { it.copy(isGuardando = true, mensajeError = null) }

            // Si es un nuevo empleado, creamos la cuenta en Firebase Auth y enviamos el correo de activación
            if (esNuevo) {
                val authResult = authService.registrarCuentaEmpleado(empleadoFinal.correo)
                if (authResult.isFailure) {
                    val ex = authResult.exceptionOrNull()?.message ?: ""
                    if (ex.contains("email-already-in-use", ignoreCase = true)) {
                        // Enviar correo de restablecimiento si ya existía
                        authService.recuperarContrasena(empleadoFinal.correo)
                    }
                }
            }

            // Si la sucursal cambió, migramos cualquier asignación futura (semana siguiente en adelante)
            // del empleado a su nueva sucursal, asegurando que un empleado no tenga sucursales distintas
            // en una misma semana y que se refleje inmediatamente en la nueva sede.
            if (sucursalCambio && empleadoPrevio != null) {
                val todasAsigs = TurnoRepository.asignaciones.value
                val asigsFuturas = todasAsigs.filter {
                    it.empleadoId == empleadoFinal.id && it.fechaIso >= fechaLunesProximaSemanaIso
                }
                if (asigsFuturas.isNotEmpty()) {
                    val asigsMigradas = asigsFuturas.map { asig ->
                        val fechaDia = try { LocalDate.parse(asig.fechaIso) } catch (_: Exception) { null }
                        val lunesDeAsig = fechaDia?.let { it.minus(DatePeriod(days = it.dayOfWeek.ordinal)) }
                        val progId = if (lunesDeAsig != null) "${lunesDeAsig}_${empleadoFinal.sucursal}" else asig.programacionId
                        asig.copy(
                            sucursalId = empleadoFinal.sucursal,
                            programacionId = progId
                        )
                    }
                    TurnoRepository.guardarAsignacionesEnBackground(asigsMigradas)
                }
            }

            EmpleadoRepository.guardarEmpleado(empleadoFinal, esNuevo)

            _uiState.update { state ->
                state.copy(
                    isGuardando = false,
                    mostrarDrawerFormulario = false,
                    empleadoEnEdicion = null,
                    mensajeError = null,
                    mensajeExito = if (esNuevo) {
                        "Empleado registrado con éxito. Se ha enviado un enlace a ${empleadoFinal.correo} para establecer su contraseña."
                    } else if (sucursalCambio) {
                        "Empleado actualizado con éxito. El cambio de sede a ${empleadoFinal.sucursal} se aplicará a partir de la próxima semana ($fechaLunesProximaSemanaIso)."
                    } else {
                        "Empleado actualizado con éxito."
                    }
                )
            }
        }
    }
}
