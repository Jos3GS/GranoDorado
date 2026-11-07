package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.repository.CambiosTurnoRepository
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.NovedadRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Cargo
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoSolicitudCambio
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.SolicitudCambio
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.model.TurnoCompaneroSeleccionable
import com.itm.gestordeturnos.domain.model.TurnoSeleccionable
import com.itm.gestordeturnos.domain.repository.CambiosTurnoRepositoryContract
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.NovedadRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import com.itm.gestordeturnos.domain.usecase.ValidacionAprobacionCambioResult
import com.itm.gestordeturnos.domain.usecase.ValidacionEstadoEmpleadoResult
import com.itm.gestordeturnos.domain.usecase.ValidacionSolicitudCambioResult
import com.itm.gestordeturnos.domain.usecase.ValidarAprobacionCambioTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarEstadoEmpleadoActivoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarSolicitudCambioTurnoUseCase
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
    val mensajeFeedback: String? = null,
    val usuarioActualId: String = "",
    val usuarioActualNombre: String = ""
)

class CambiosTurnoViewModel(
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository,
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val cambiosTurnoRepository: CambiosTurnoRepositoryContract = CambiosTurnoRepository,
    private val novedadRepository: NovedadRepositoryContract = NovedadRepository,
    private val validarEstadoEmpleadoActivoUseCase: ValidarEstadoEmpleadoActivoUseCase = ValidarEstadoEmpleadoActivoUseCase(),
    private val validarSolicitudCambioUseCase: ValidarSolicitudCambioTurnoUseCase = ValidarSolicitudCambioTurnoUseCase(),
    private val validarAprobacionCambioUseCase: ValidarAprobacionCambioTurnoUseCase = ValidarAprobacionCambioTurnoUseCase()
) : ViewModel() {
    private val _uiState = MutableStateFlow(CambiosTurnoState())
    val uiState: StateFlow<CambiosTurnoState> = _uiState.asStateFlow()

    init {
        turnoRepository.iniciarSincronizacionFirestore()
        empleadoRepository.iniciarSincronizacionFirestore()
        cambiosTurnoRepository.iniciarSincronizacionFirestore()
        novedadRepository.iniciarSincronizacionFirestore()
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

                // 2. Extraer turnos propios publicables y elegibles (RN-19)
                val propiosDinamicos = extraerTurnosPropios(empId, empNombre, asignaciones, solicitudes)
                val propiosFinales = if (propiosDinamicos.isNotEmpty()) propiosDinamicos else MockDataProviders.inicialTurnosPropios()

                // 3. Extraer turnos de compañeros elegibles (misma sucursal, mismo cargo, publicados y sin solicitud en curso) (RN-19)
                val companerosDinamicos = extraerTurnosCompaneros(empId, empNombre, sucursalUsuario, empCargo, asignaciones, empleados, solicitudes)
                val companerosFinales = if (companerosDinamicos.isNotEmpty()) companerosDinamicos else generarCompanerosMockFallback(sucursalUsuario)

                _uiState.update { actual ->
                    actual.copy(
                        solicitudes = solicitudesFiltradas,
                        turnosPropios = propiosFinales,
                        turnosCompaneros = companerosFinales,
                        usuarioActualId = empId,
                        usuarioActualNombre = empNombre
                    )
                }
            }
        }
    }

    private fun extraerTurnosPropios(
        empId: String,
        empNombre: String,
        asignaciones: List<AsignacionTurno>,
        solicitudes: List<SolicitudCambioTurno>
    ): List<TurnoSeleccionable> {
        return asignaciones.filter { asig ->
            val coincideEmp = asig.empleadoId == empId ||
                (asig.empleadoNombre.isNotBlank() && asig.empleadoNombre.equals(empNombre, ignoreCase = true))

            coincideEmp && validarSolicitudCambioUseCase.esTurnoElegibleParaIntercambio(
                asignacion = if (asig.empleadoId.isBlank()) asig.copy(empleadoId = empId) else asig,
                empleadoId = if (asig.empleadoId.isBlank()) empId else asig.empleadoId,
                solicitudesExistentes = solicitudes
            )
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
        empleados: List<Empleado>,
        solicitudes: List<SolicitudCambioTurno>
    ): List<TurnoCompaneroSeleccionable> {
        val sucursalLimpia = sucursal.trim().replace(" ", "")
        val empleadosElegibles = empleados.filter { e ->
            e.id != empId &&
            !e.nombre.equals(empNombre, ignoreCase = true) &&
            (cargo == null || e.cargo == cargo) &&
            (e.sucursal.isBlank() || e.sucursal.trim().replace(" ", "").equals(sucursalLimpia, ignoreCase = true)) &&
            e.estado == com.itm.gestordeturnos.domain.model.EstadoUsuario.ACTIVO
        }.associateBy { it.id }

        return asignaciones.filter { asig ->
            asig.empleadoId != empId &&
            !asig.empleadoNombre.equals(empNombre, ignoreCase = true) &&
            (asig.sucursalId.isBlank() || asig.sucursalId.trim().replace(" ", "").equals(sucursalLimpia, ignoreCase = true)) &&
            empleadosElegibles.containsKey(asig.empleadoId) &&
            validarSolicitudCambioUseCase.esTurnoElegibleParaIntercambio(
                asignacion = asig,
                empleadoId = asig.empleadoId,
                solicitudesExistentes = solicitudes
            )
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
                motivo = if (!mostrar) "" else it.motivo,
                mensajeFeedback = null
            )
        }
    }

    fun limpiarMensajeFeedback() {
        _uiState.update { it.copy(mensajeFeedback = null) }
    }

    fun seleccionarTurnoPropio(turno: TurnoSeleccionable) {
        _uiState.update { it.copy(turnoPropioSeleccionado = turno, mensajeFeedback = null) }
    }

    fun seleccionarTurnoCompanero(turno: TurnoCompaneroSeleccionable) {
        _uiState.update { it.copy(turnoCompaneroSeleccionado = turno, mensajeFeedback = null) }
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
        val empleados = empleadoRepository.empleados.value
        val emp = resolverEmpleado(usuario, empleados)

        // RN-11: El empleado no puede realizar acciones si está inactivo
        val validacionActivo = validarEstadoEmpleadoActivoUseCase.validar(emp, "cambio_turno")
        if (validacionActivo is ValidacionEstadoEmpleadoResult.Inactivo) {
            _uiState.update { it.copy(mensajeFeedback = validacionActivo.mensaje) }
            return
        }

        val compEmpleado = empleados.find { it.id == companero.empleadoId }
        val asignaciones = turnoRepository.asignaciones.value
        val solicitudes = cambiosTurnoRepository.solicitudes.value
        val novedades = novedadRepository.novedades.value

        val empFinal = emp ?: Empleado(
            id = usuario?.id ?: "emp_1",
            nombre = usuario?.nombre ?: "Empleado",
            apellido = "",
            correo = usuario?.correo ?: "",
            sucursal = usuario?.sucursal ?: "CENTRO",
            cargo = Cargo.BARISTA
        )
        val compFinal = compEmpleado ?: Empleado(
            id = companero.empleadoId.ifBlank { "emp_comp" },
            nombre = companero.nombre,
            apellido = "",
            correo = "",
            sucursal = empFinal.sucursal,
            cargo = empFinal.cargo
        )

        val turnoEntregaAsig = asignaciones.find {
            it.id == propio.asignacionId || it.id == propio.id || (it.empleadoId == empFinal.id && it.fechaIso == propio.fechaIso)
        } ?: AsignacionTurno(
            id = propio.asignacionId.ifBlank { propio.id },
            empleadoId = empFinal.id,
            empleadoNombre = "${empFinal.nombre} ${empFinal.apellido}".trim(),
            fechaIso = propio.fechaIso,
            tipoTurno = propio.tipoTurno.ifBlank { propio.descripcion.split("·").lastOrNull()?.trim() ?: "Turno" },
            horario = propio.horario,
            sucursalId = empFinal.sucursal,
            estado = "PUBLICADA"
        )

        val turnoRecibeAsig = asignaciones.find {
            it.id == companero.asignacionId || it.id == companero.id || (it.empleadoId == compFinal.id && it.fechaIso == companero.fechaIso)
        } ?: AsignacionTurno(
            id = companero.asignacionId.ifBlank { companero.id },
            empleadoId = compFinal.id,
            empleadoNombre = "${compFinal.nombre} ${compFinal.apellido}".trim(),
            fechaIso = companero.fechaIso,
            tipoTurno = companero.tipoTurno.ifBlank { companero.descripcion.split("·").lastOrNull()?.trim() ?: "Turno" },
            horario = companero.horario,
            sucursalId = compFinal.sucursal,
            estado = "PUBLICADA"
        )

        // RN-19: Validación rigurosa de intercambio de turnos
        val validacionRN19 = validarSolicitudCambioUseCase.validarSolicitud(
            solicitante = empFinal,
            companero = compFinal,
            turnoEntrega = turnoEntregaAsig,
            turnoRecibe = turnoRecibeAsig,
            solicitudesExistentes = solicitudes,
            todasAsignaciones = asignaciones
        )

        if (validacionRN19 is ValidacionSolicitudCambioResult.Invalido) {
            _uiState.update { it.copy(mensajeFeedback = validacionRN19.mensaje) }
            return
        }

        val empId = empFinal.id
        val empNombre = if (empFinal.apellido.isNotBlank()) "${empFinal.nombre} ${empFinal.apellido}".trim() else empFinal.nombre
        val sucursal = empFinal.sucursal.ifBlank { "CENTRO" }

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
            empleadoCompaneroId = compFinal.id,
            nombreCompanero = compFinal.nombre,
            turnoEntregaId = turnoEntregaAsig.id,
            tipoTurnoEntrega = turnoEntregaAsig.tipoTurno,
            detalleTurnoEntrega = "${propio.descripcion} · ${propio.horario}",
            turnoRecibeId = turnoRecibeAsig.id,
            tipoTurnoRecibe = turnoRecibeAsig.tipoTurno,
            detalleTurnoRecibe = "${companero.descripcion} · ${companero.horario}",
            sucursalNombre = sucursal,
            validacionesOk = true,
            esRemitente = true
        )

        cambiosTurnoRepository.crearSolicitud(nuevaSolicitud)
        toggleModalSolicitud(false)
    }

    fun aceptarSolicitud(solicitud: SolicitudCambioTurno) {
        val usuario = SessionManager.usuarioActivo
        val emp = resolverEmpleado(usuario, empleadoRepository.empleados.value)
        val validacionActivo = validarEstadoEmpleadoActivoUseCase.validar(emp, "cambio_turno")
        if (validacionActivo is ValidacionEstadoEmpleadoResult.Inactivo) {
            _uiState.update { it.copy(mensajeFeedback = validacionActivo.mensaje) }
            return
        }

        if (solicitud.estado != EstadoSolicitudCambio.PENDIENTE) {
            _uiState.update { it.copy(mensajeFeedback = "La solicitud ya no se encuentra pendiente de aceptación.") }
            return
        }

        cambiosTurnoRepository.aceptarPorCompanero(solicitud.id)
    }

    fun rechazarSolicitud(solicitud: SolicitudCambioTurno, motivo: String) {
        val usuario = SessionManager.usuarioActivo
        val emp = resolverEmpleado(usuario, empleadoRepository.empleados.value)
        val validacionActivo = validarEstadoEmpleadoActivoUseCase.validar(emp, "cambio_turno")
        if (validacionActivo is ValidacionEstadoEmpleadoResult.Inactivo) {
            _uiState.update { it.copy(mensajeFeedback = validacionActivo.mensaje) }
            return
        }

        val esAdmin = usuario?.rol != null && usuario.rol != RolUsuario.EMPLEADO
        val empId = emp?.id ?: usuario?.id ?: ""
        val empNombre = emp?.let { "${it.nombre} ${it.apellido}".trim() } ?: usuario?.nombre ?: ""
        val esCompanero = empId == solicitud.empleadoCompaneroId ||
                empNombre.equals(solicitud.nombreCompanero, ignoreCase = true) ||
                (emp != null && emp.nombre.equals(solicitud.nombreCompanero, ignoreCase = true))

        if (esAdmin && (!esCompanero || solicitud.estado == EstadoSolicitudCambio.ACEPTADA_COMPANERO)) {
            val adminParaValidar = emp ?: Empleado(
                id = usuario?.id ?: "admin",
                nombre = usuario?.nombre ?: "Administrador",
                apellido = "",
                correo = usuario?.correo ?: "",
                sucursal = usuario?.sucursal ?: solicitud.sucursalNombre
            )
            val resultadoRechazo = validarAprobacionCambioUseCase.validarRechazoAdministrador(
                administrador = adminParaValidar,
                rolAdministrador = usuario?.rol,
                solicitud = solicitud
            )
            if (resultadoRechazo is ValidacionAprobacionCambioResult.Invalido) {
                _uiState.update { it.copy(mensajeFeedback = resultadoRechazo.mensaje) }
                return
            }
        }

        val motivoFinal = motivo.ifBlank { "Rechazada" }
        cambiosTurnoRepository.rechazarSolicitud(solicitud.id, motivoFinal)
    }

    fun cancelarSolicitud(solicitud: SolicitudCambioTurno) {
        cambiosTurnoRepository.cancelarSolicitud(solicitud.id)
    }

    fun aprobarSolicitud(solicitud: SolicitudCambioTurno) {
        val usuario = SessionManager.usuarioActivo
        val emp = resolverEmpleado(usuario, empleadoRepository.empleados.value)
        val validacionActivo = validarEstadoEmpleadoActivoUseCase.validar(emp, "cambio_turno")
        if (validacionActivo is ValidacionEstadoEmpleadoResult.Inactivo) {
            _uiState.update { it.copy(mensajeFeedback = validacionActivo.mensaje) }
            return
        }

        // RN-20: Revalidación completa de aprobación por administrador
        val adminParaValidar = emp ?: Empleado(
            id = usuario?.id ?: "admin",
            nombre = usuario?.nombre ?: "Administrador",
            apellido = "",
            correo = usuario?.correo ?: "",
            sucursal = usuario?.sucursal ?: solicitud.sucursalNombre
        )

        val resultadoAprobacion = validarAprobacionCambioUseCase.validarAprobacion(
            administrador = adminParaValidar,
            rolAdministrador = usuario?.rol,
            solicitud = solicitud,
            empleados = empleadoRepository.empleados.value,
            todasAsignaciones = turnoRepository.asignaciones.value,
            todasSolicitudes = cambiosTurnoRepository.solicitudes.value
        )

        if (resultadoAprobacion is ValidacionAprobacionCambioResult.Invalido) {
            _uiState.update { it.copy(mensajeFeedback = resultadoAprobacion.mensaje) }
            return
        }

        cambiosTurnoRepository.aprobarEIntercambiar(solicitud)
    }
}
