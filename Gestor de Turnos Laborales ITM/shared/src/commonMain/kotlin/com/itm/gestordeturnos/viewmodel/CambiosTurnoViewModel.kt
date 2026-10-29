package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.repository.CambiosTurnoRepository
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoSolicitudCambio
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.SolicitudCambio
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.model.TurnoCompaneroSeleccionable
import com.itm.gestordeturnos.domain.model.TurnoSeleccionable
import com.itm.gestordeturnos.domain.repository.CambiosTurnoRepositoryContract
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.random.Random

data class CambiosTurnoState(
    val solicitudes: List<SolicitudCambio> = emptyList(),
    val mostrarModalSolicitud: Boolean = false,
    val turnoPropioSeleccionado: TurnoSeleccionable? = null,
    val turnoCompaneroSeleccionado: TurnoCompaneroSeleccionable? = null,
    val motivo: String = "",
    val turnosPropios: List<TurnoSeleccionable> = emptyList(),
    val turnosCompaneros: List<TurnoCompaneroSeleccionable> = emptyList(),
    val mensajeFeedback: String? = null
)

class CambiosTurnoViewModel(
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository,
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val cambiosTurnoRepository: CambiosTurnoRepositoryContract = CambiosTurnoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CambiosTurnoState())
    val uiState: StateFlow<CambiosTurnoState> = _uiState.asStateFlow()

    init {
        turnoRepository.iniciarSincronizacionFirestore()
        empleadoRepository.iniciarSincronizacionFirestore()
        cambiosTurnoRepository.iniciarSincronizacionFirestore()
        observarDatos()
    }

    private fun observarDatos() {
        viewModelScope.launch {
            combine(
                cambiosTurnoRepository.solicitudes,
                turnoRepository.asignaciones,
                empleadoRepository.empleados
            ) { solicitudes, asignaciones, empleados ->
                Triple(solicitudes, asignaciones, empleados)
            }.collect { (solicitudes, asignaciones, empleados) ->
                val usuario = SessionManager.usuarioActivo
                val emp = resolverEmpleado(usuario, empleados)
                val esAdmin = usuario?.rol != RolUsuario.EMPLEADO
                val sucursalUsuario = emp?.sucursal ?: usuario?.sucursal ?: "CENTRO"
                val empId = emp?.id ?: usuario?.id ?: "emp_1"
                val empNombre = emp?.let {
                    if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
                } ?: usuario?.nombre ?: "Empleado"
                val empCargo = emp?.cargo

                // 1. Filtrar solicitudes según el rol y usuario
                val solicitudesFiltradas = if (esAdmin) {
                    if (usuario?.rol == RolUsuario.ADMINISTRADOR_GENERAL) {
                        solicitudes
                    } else {
                        solicitudes.filter {
                            it.sucursalNombre.isBlank() || it.sucursalNombre.equals(sucursalUsuario, ignoreCase = true)
                        }
                    }
                } else {
                    solicitudes.filter { s ->
                        s.empleadoSolicitanteId == empId ||
                        s.empleadoCompaneroId == empId ||
                        s.nombreSolicitante.equals(empNombre, ignoreCase = true) ||
                        s.nombreCompanero.equals(empNombre, ignoreCase = true)
                    }.map { s ->
                        s.copy(
                            esRemitente = s.empleadoSolicitanteId == empId || s.nombreSolicitante.equals(empNombre, ignoreCase = true)
                        )
                    }
                }

                // 2. Extraer turnos propios publicables
                val propiosDinamicos = extraerTurnosPropios(empId, empNombre, asignaciones)
                val propiosFinales = if (propiosDinamicos.isNotEmpty()) propiosDinamicos else MockDataProviders.inicialTurnosPropios()

                // 3. Extraer turnos de compañeros elegibles (misma sucursal y mismo cargo)
                val companerosDinamicos = extraerTurnosCompaneros(empId, empNombre, sucursalUsuario, empCargo, asignaciones, empleados)
                val companerosFinales = if (companerosDinamicos.isNotEmpty()) companerosDinamicos else generarCompanerosMockFallback(sucursalUsuario)

                _uiState.update { actual ->
                    actual.copy(
                        solicitudes = solicitudesFiltradas,
                        turnosPropios = propiosFinales,
                        turnosCompaneros = companerosFinales
                    )
                }
            }
        }
    }

    private fun extraerTurnosPropios(
        empId: String,
        empNombre: String,
        asignaciones: List<AsignacionTurno>
    ): List<TurnoSeleccionable> {
        return asignaciones.filter { asig ->
            (asig.empleadoId == empId || (asig.empleadoNombre.isNotBlank() && asig.empleadoNombre.equals(empNombre, ignoreCase = true))) &&
            !asig.esDescanso &&
            asig.tipoTurno.isNotBlank() &&
            !asig.tipoTurno.equals("Descanso", ignoreCase = true) &&
            !asig.tipoTurno.contains("incapacidad", ignoreCase = true) &&
            !asig.tipoTurno.contains("vacaciones", ignoreCase = true)
        }.map { asig ->
            val desc = "${formatearFechaCorta(asig.fechaIso)} · ${asig.tipoTurno}"
            val hor = if (asig.horario.isNotBlank() && asig.horario != "-") asig.horario.replace("\n", " ") else "06:00 - 14:00"
            TurnoSeleccionable(
                id = asig.id.ifBlank { "${asig.empleadoId}_${asig.fechaIso}" },
                descripcion = desc,
                horario = hor,
                fechaIso = asig.fechaIso,
                tipoTurno = asig.tipoTurno,
                asignacionId = asig.id
            )
        }.distinctBy { it.id }
    }

    private fun extraerTurnosCompaneros(
        empId: String,
        empNombre: String,
        sucursal: String,
        cargo: com.itm.gestordeturnos.domain.model.Cargo?,
        asignaciones: List<AsignacionTurno>,
        empleados: List<Empleado>
    ): List<TurnoCompaneroSeleccionable> {
        val empleadosElegibles = empleados.filter { e ->
            e.id != empId &&
            !e.nombre.equals(empNombre, ignoreCase = true) &&
            (cargo == null || e.cargo == cargo) &&
            (e.sucursal.isBlank() || e.sucursal.equals(sucursal, ignoreCase = true)) &&
            e.estado == com.itm.gestordeturnos.domain.model.EstadoUsuario.ACTIVO
        }.associateBy { it.id }

        return asignaciones.filter { asig ->
            asig.empleadoId != empId &&
            !asig.empleadoNombre.equals(empNombre, ignoreCase = true) &&
            (asig.sucursalId.isBlank() || asig.sucursalId.equals(sucursal, ignoreCase = true)) &&
            (empleadosElegibles.containsKey(asig.empleadoId) || cargo == null) &&
            !asig.esDescanso &&
            asig.tipoTurno.isNotBlank() &&
            !asig.tipoTurno.equals("Descanso", ignoreCase = true) &&
            !asig.tipoTurno.contains("incapacidad", ignoreCase = true) &&
            !asig.tipoTurno.contains("vacaciones", ignoreCase = true)
        }.map { asig ->
            val emp = empleadosElegibles[asig.empleadoId]
            val nombre = asig.empleadoNombre.ifBlank { emp?.let { "${it.nombre} ${it.apellido}".trim() } ?: "Compañero" }
            val desc = "${formatearFechaCorta(asig.fechaIso)} · ${asig.tipoTurno}"
            val hor = if (asig.horario.isNotBlank() && asig.horario != "-") asig.horario.replace("\n", " ") else "14:00 - 22:00"
            TurnoCompaneroSeleccionable(
                id = asig.id.ifBlank { "${asig.empleadoId}_${asig.fechaIso}" },
                nombre = nombre,
                descripcion = desc,
                horario = hor,
                empleadoId = asig.empleadoId,
                fechaIso = asig.fechaIso,
                tipoTurno = asig.tipoTurno,
                asignacionId = asig.id
            )
        }.distinctBy { "${it.empleadoId}_${it.fechaIso}" }
    }

    private fun generarCompanerosMockFallback(sucursal: String): List<TurnoCompaneroSeleccionable> {
        return listOf(
            TurnoCompaneroSeleccionable(
                id = "mock_comp_1",
                nombre = "Sofía Ramos",
                descripcion = "Jueves 17 · Cierre",
                horario = "14:00 - 22:00",
                empleadoId = "emp_6",
                fechaIso = "2026-09-17",
                tipoTurno = "Cierre",
                asignacionId = "emp_6_2026-09-17"
            ),
            TurnoCompaneroSeleccionable(
                id = "mock_comp_2",
                nombre = "Marta León",
                descripcion = "Viernes 18 · Apertura",
                horario = "06:00 - 14:00",
                empleadoId = "emp_3",
                fechaIso = "2026-09-18",
                tipoTurno = "Apertura",
                asignacionId = "emp_3_2026-09-18"
            ),
            TurnoCompaneroSeleccionable(
                id = "mock_comp_3",
                nombre = "Carlos Ruiz",
                descripcion = "Sábado 19 · Intermedio",
                horario = "10:00 - 18:00",
                empleadoId = "emp_2",
                fechaIso = "2026-09-19",
                tipoTurno = "Intermedio",
                asignacionId = "emp_2_2026-09-19"
            )
        )
    }

    private fun formatearFechaCorta(fechaIso: String): String {
        return try {
            val f = LocalDate.parse(fechaIso)
            val nombresDias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
            "${nombresDias[f.dayOfWeek.ordinal]} ${f.dayOfMonth}"
        } catch (_: Exception) {
            fechaIso
        }
    }

    private fun resolverEmpleado(usuario: com.itm.gestordeturnos.domain.model.UsuarioActual?, empleados: List<Empleado>): Empleado? {
        if (usuario == null) return null
        if (usuario.id.isNotBlank()) {
            val porId = empleados.find { it.id == usuario.id }
            if (porId != null) return porId
        }
        if (usuario.correo.isNotBlank()) {
            val porCorreo = empleados.find { it.correo.equals(usuario.correo, ignoreCase = true) }
            if (porCorreo != null) return porCorreo
        }
        return empleados.find { it.nombre.equals(usuario.nombre, ignoreCase = true) }
    }

    fun toggleModalSolicitud(mostrar: Boolean) {
        _uiState.update {
            it.copy(
                mostrarModalSolicitud = mostrar,
                turnoPropioSeleccionado = if (!mostrar) null else it.turnoPropioSeleccionado,
                turnoCompaneroSeleccionado = if (!mostrar) null else it.turnoCompaneroSeleccionado,
                motivo = if (!mostrar) "" else it.motivo
            )
        }
    }

    fun seleccionarTurnoPropio(turno: TurnoSeleccionable) {
        _uiState.update { it.copy(turnoPropioSeleccionado = turno) }
    }

    fun seleccionarTurnoCompanero(turno: TurnoCompaneroSeleccionable) {
        _uiState.update { it.copy(turnoCompaneroSeleccionado = turno) }
    }

    fun actualizarMotivo(motivo: String) {
        _uiState.update { it.copy(motivo = motivo) }
    }

    fun enviarSolicitud() {
        val state = _uiState.value
        val propio = state.turnoPropioSeleccionado ?: return
        val companero = state.turnoCompaneroSeleccionado ?: return
        val motivo = state.motivo.trim()
        if (motivo.isBlank()) return

        val usuario = SessionManager.usuarioActivo
        val emp = resolverEmpleado(usuario, empleadoRepository.empleados.value)
        val empId = emp?.id ?: usuario?.id ?: "emp_1"
        val empNombre = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: usuario?.nombre ?: "Empleado"
        val sucursal = emp?.sucursal ?: usuario?.sucursal ?: "CENTRO"

        val randomId = Random.nextInt(100000, 999999).toString()
        val numSol = "SOL-${Random.nextInt(1000, 9999)}"
        val hoy = try { Clock.System.todayIn(TimeZone.currentSystemDefault()) } catch (_: Throwable) { null }

        val nuevaSolicitud = SolicitudCambioTurno(
            id = "sol_$randomId",
            numeroSolicitud = numSol,
            fechaSolicitud = hoy,
            estado = EstadoSolicitudCambio.PENDIENTE,
            motivoRechazo = "",
            motivo = motivo,
            empleadoSolicitanteId = empId,
            nombreSolicitante = empNombre,
            empleadoCompaneroId = companero.empleadoId.ifBlank { "emp_comp" },
            nombreCompanero = companero.nombre,
            turnoEntregaId = propio.asignacionId.ifBlank { propio.id },
            tipoTurnoEntrega = propio.tipoTurno.ifBlank { propio.descripcion.split("·").lastOrNull()?.trim() ?: "Turno" },
            detalleTurnoEntrega = "${propio.descripcion} · ${propio.horario}",
            turnoRecibeId = companero.asignacionId.ifBlank { companero.id },
            tipoTurnoRecibe = companero.tipoTurno.ifBlank { companero.descripcion.split("·").lastOrNull()?.trim() ?: "Turno" },
            detalleTurnoRecibe = "${companero.descripcion} · ${companero.horario}",
            sucursalNombre = sucursal,
            validacionesOk = true,
            esRemitente = true
        )

        cambiosTurnoRepository.crearSolicitud(nuevaSolicitud)
        toggleModalSolicitud(false)
    }

    fun aceptarSolicitud(solicitud: SolicitudCambioTurno) {
        cambiosTurnoRepository.aceptarPorCompanero(solicitud.id)
    }

    fun rechazarSolicitud(solicitud: SolicitudCambioTurno, motivo: String) {
        val motivoFinal = motivo.ifBlank { "Rechazada" }
        cambiosTurnoRepository.rechazarSolicitud(solicitud.id, motivoFinal)
    }

    fun cancelarSolicitud(solicitud: SolicitudCambioTurno) {
        cambiosTurnoRepository.cancelarSolicitud(solicitud.id)
    }

    fun aprobarSolicitud(solicitud: SolicitudCambioTurno) {
        cambiosTurnoRepository.aprobarEIntercambiar(solicitud)
    }
}
