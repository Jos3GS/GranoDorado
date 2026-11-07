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
import com.itm.gestordeturnos.domain.repository.AsistenciaRepositoryContract
import com.itm.gestordeturnos.domain.repository.CambiosTurnoRepositoryContract
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.NovedadRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import com.itm.gestordeturnos.domain.usecase.ControlInactividadSesionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class OpcionMenu(
    val titulo: String,
    val id: String
)

data class MainState(
    val usuario: UsuarioActual? = null,
    val opcionesMenu: List<OpcionMenu> = emptyList(),
    val opcionSeleccionada: String = "",
    val sesionCerradaPorInactividad: Boolean = false
)

class MainViewModel(
    private val authService: AuthService = FirebaseAuthService(),
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository,
    private val novedadRepository: NovedadRepositoryContract = NovedadRepository,
    private val cambiosTurnoRepository: CambiosTurnoRepositoryContract = CambiosTurnoRepository,
    private val asistenciaRepository: AsistenciaRepositoryContract = AsistenciaRepository,
    private val inactividadUseCase: ControlInactividadSesionUseCase = ControlInactividadSesionUseCase()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    private var jobMonitoreoInactividad: Job? = null

    init {
        viewModelScope.launch {
            SessionManager.usuarioActivoFlow.collect { usuarioLogueado ->
                if (usuarioLogueado != null) {
                    iniciarMonitoreoInactividad()
                    val opciones = obtenerMenuPorRol(usuarioLogueado.rol)
                    _uiState.update { actual ->
                        actual.copy(
                            usuario = usuarioLogueado,
                            opcionesMenu = opciones,
                            opcionSeleccionada = actual.opcionSeleccionada.takeIf { id -> opciones.any { it.id == id } }
                                ?: opciones.firstOrNull()?.id.orEmpty(),
                            sesionCerradaPorInactividad = false
                        )
                    }
                } else {
                    detenerMonitoreoInactividad()
                    _uiState.value = MainState()
                }
            }
        }
    }

    /**
     * Registra interacción del usuario para reiniciar el reloj de 30 minutos de inactividad (RN-03).
     */
    fun registrarInteraccionUsuario() {
        inactividadUseCase.registrarInteraccion()
    }

    private fun iniciarMonitoreoInactividad() {
        jobMonitoreoInactividad?.cancel()
        inactividadUseCase.iniciarMonitoreo()

        jobMonitoreoInactividad = viewModelScope.launch {
            while (isActive) {
                delay(ControlInactividadSesionUseCase.INTERVALO_SONDEO_SEGUNDOS * 1000L)
                if (inactividadUseCase.verificarSiExpiro()) {
                    ejecutarCierrePorInactividad()
                    break
                }
            }
        }
    }

    private fun detenerMonitoreoInactividad() {
        jobMonitoreoInactividad?.cancel()
        jobMonitoreoInactividad = null
        inactividadUseCase.detenerMonitoreo()
    }

    private fun ejecutarCierrePorInactividad() {
        viewModelScope.launch {
            authService.cerrarSesion()
            empleadoRepository.detenerSincronizacion()
            turnoRepository.detenerSincronizacion()
            novedadRepository.detenerSincronizacion()
            cambiosTurnoRepository.detenerSincronizacion()
            asistenciaRepository.detenerSincronizacion()
            detenerMonitoreoInactividad()
            _uiState.update { it.copy(sesionCerradaPorInactividad = true) }
            SessionManager.notificarSesionExpirada(ControlInactividadSesionUseCase.MENSAJE_SESION_EXPIRADA)
        }
    }

    fun cargarDatos() {
        val usuarioLogueado = SessionManager.usuarioActivo ?: authService.obtenerUsuarioActivo()?.also {
            SessionManager.usuarioActivo = it
        }
        if (usuarioLogueado != null) {
            iniciarMonitoreoInactividad()
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
            detenerMonitoreoInactividad()
            authService.cerrarSesion()
            SessionManager.limpiarSesion()
            empleadoRepository.detenerSincronizacion()
            turnoRepository.detenerSincronizacion()
            novedadRepository.detenerSincronizacion()
            cambiosTurnoRepository.detenerSincronizacion()
            asistenciaRepository.detenerSincronizacion()
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
                OpcionMenu("Asistencia", "asistencia"),
                OpcionMenu("Auditoría", "auditoria")
            )
            RolUsuario.ADMINISTRADOR_SUCURSAL -> listOf(
                OpcionMenu("Panel", "panel"),
                OpcionMenu("Empleados", "empleados"),
                OpcionMenu("Malla semanal", "malla"),
                OpcionMenu("Novedades", "novedades"),
                OpcionMenu("Cambios de turno", "cambios"),
                OpcionMenu("Asistencia", "asistencia"),
                OpcionMenu("Auditoría", "auditoria")
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
        registrarInteraccionUsuario()
        _uiState.update { it.copy(opcionSeleccionada = idOpcion) }
    }
}
