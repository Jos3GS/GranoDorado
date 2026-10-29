package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.service.FirestoreService
import com.itm.gestordeturnos.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object EmpleadoRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _empleados = MutableStateFlow(MockDataProviders.inicialEmpleados())
    val empleados: StateFlow<List<Empleado>> = _empleados.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    fun iniciarSincronizacionFirestore(forzar: Boolean = false) {
        if (!forzar && jobSincronizacion?.isActive == true) return
        jobSincronizacion?.cancel()
        jobSincronizacion = repositoryScope.launch {
            try {
                // 1. Si la colección de Firestore está vacía, poblarla con el usuario inicial
                FirestoreService.inicializarSiEstaVacio()

                // 2. Escuchar en tiempo real los cambios en la colección "empleados"
                FirestoreService.observarEmpleados().collect { listaRemota ->
                    if (listaRemota.isNotEmpty()) {
                        _empleados.value = listaRemota
                    }
                }
            } catch (_: Exception) {
                // En caso de estar sin conexión o fallo temporal de red, mantiene la memoria local
            }
        }
    }

    fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    fun guardarEmpleado(empleado: Empleado, esNuevo: Boolean) {
        // Actualización optimista inmediata en memoria para fluidez de la UI
        _empleados.update { lista ->
            if (esNuevo) {
                lista + empleado
            } else {
                lista.map { if (it.id == empleado.id) empleado else it }
            }
        }

        // Persistencia asíncrona en Cloud Firestore
        repositoryScope.launch {
            val resultado = FirestoreService.guardarEmpleado(empleado)
            if (resultado.isFailure) {
                println("Fallo al guardar empleado en Firestore: ${resultado.exceptionOrNull()?.message}")
            }
        }
    }

    suspend fun buscarPorCorreo(correo: String): Empleado? {
        val busqueda = correo.trim()
        val local = _empleados.value.find { it.correo.equals(busqueda, ignoreCase = true) }
        if (local != null) return local

        return try {
            val remotos = FirestoreService.obtenerEmpleados()
            if (remotos.isNotEmpty()) {
                _empleados.value = remotos
                remotos.find { it.correo.equals(busqueda, ignoreCase = true) }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}

object TurnoRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _turnosCatalogo = MutableStateFlow(MockDataProviders.inicialTurnosCatalogo())
    val turnosCatalogo: StateFlow<List<TurnoTemplate>> = _turnosCatalogo.asStateFlow()

    private val _asignaciones = MutableStateFlow<List<AsignacionTurno>>(emptyList())
    val asignaciones: StateFlow<List<AsignacionTurno>> = _asignaciones.asStateFlow()

    private val _horarioSemana = MutableStateFlow(MockDataProviders.inicialHorarioSemana())
    val horarioSemana: StateFlow<List<TurnoAsignado>> = _horarioSemana.asStateFlow()

    private val _filasMalla = MutableStateFlow(MockDataProviders.inicialFilasMalla())
    val filasMalla: StateFlow<List<FilaMalla>> = _filasMalla.asStateFlow()

    private val _turnosPropios = MutableStateFlow(MockDataProviders.inicialTurnosPropios())
    val turnosPropios: StateFlow<List<TurnoSeleccionable>> = _turnosPropios.asStateFlow()

    private val _turnosCompaneros = MutableStateFlow(MockDataProviders.inicialTurnosCompaneros())
    val turnosCompaneros: StateFlow<List<TurnoCompaneroSeleccionable>> = _turnosCompaneros.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    fun iniciarSincronizacionFirestore(forzar: Boolean = false) {
        if (!forzar && jobSincronizacion?.isActive == true) return
        jobSincronizacion?.cancel()
        jobSincronizacion = repositoryScope.launch {
            try {
                // 1. Cargar inmediatamente desde Firestore si ya existen turnos
                val remotos = FirestoreService.obtenerTurnos()
                if (remotos.isNotEmpty()) {
                    _turnosCatalogo.value = remotos
                } else {
                    // Si Firestore está vacío, inicializar con los turnos base del catálogo
                    val inicializados = FirestoreService.inicializarTurnosSiEstaVacio()
                    if (inicializados) {
                        val turnosCargados = FirestoreService.obtenerTurnos()
                        if (turnosCargados.isNotEmpty()) {
                            _turnosCatalogo.value = turnosCargados
                        }
                    }
                }

                // Cargar asignaciones guardadas de Firestore
                val asignacionesRemotas = FirestoreService.obtenerAsignaciones()
                if (asignacionesRemotas.isNotEmpty()) {
                    _asignaciones.value = asignacionesRemotas
                }

                // 2. Escuchar en tiempo real los cambios en la colección "turnos"
                launch {
                    FirestoreService.observarTurnos().collect { listaRemota ->
                        if (listaRemota.isNotEmpty()) {
                            _turnosCatalogo.value = listaRemota
                        }
                    }
                }

                // Escuchar en tiempo real los cambios en "asignaciones"
                launch {
                    FirestoreService.observarAsignaciones().collect { listaAsignaciones ->
                        if (listaAsignaciones.isNotEmpty()) {
                            _asignaciones.value = listaAsignaciones
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error en sincronización con Firestore: ${e.message}")
            }
        }
    }

    fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    fun refrescarDesdeFirestore() {
        repositoryScope.launch {
            try {
                val remotos = FirestoreService.obtenerTurnos()
                if (remotos.isNotEmpty()) {
                    _turnosCatalogo.value = remotos
                }
                val asigs = FirestoreService.obtenerAsignaciones()
                if (asigs.isNotEmpty()) {
                    _asignaciones.value = asigs
                }
            } catch (e: Exception) {
                println("Error al refrescar desde Firestore: ${e.message}")
            }
        }
    }

    suspend fun guardarAsignacion(asignacion: AsignacionTurno): Result<Unit> {
        // Optimistic update
        _asignaciones.update { lista ->
            val filtrada = lista.filterNot { it.id == asignacion.id || (it.empleadoId == asignacion.empleadoId && it.fechaIso == asignacion.fechaIso) }
            filtrada + asignacion
        }
        val res = FirestoreService.guardarAsignacion(asignacion)
        if (res.isFailure) {
            println("Error al guardar asignación en Firestore: ${res.exceptionOrNull()?.message}")
        }
        return res
    }

    suspend fun guardarAsignaciones(lista: List<AsignacionTurno>): Result<Unit> {
        if (lista.isEmpty()) return Result.success(Unit)
        // Optimistic update
        val ids = lista.map { it.id }.toSet()
        val empFechas = lista.map { "${it.empleadoId}_${it.fechaIso}" }.toSet()
        _asignaciones.update { existentes ->
            existentes.filterNot { it.id in ids || "${it.empleadoId}_${it.fechaIso}" in empFechas } + lista
        }
        val res = FirestoreService.guardarAsignacionesBatch(lista)
        if (res.isFailure) {
            println("Error al guardar lote de asignaciones en Firestore: ${res.exceptionOrNull()?.message}")
        }
        return res
    }

    fun guardarAsignacionesEnBackground(lista: List<AsignacionTurno>) {
        if (lista.isEmpty()) return
        repositoryScope.launch {
            guardarAsignaciones(lista)
        }
    }

    suspend fun forzarSincronizacionCatalogo(): Result<Unit> {
        return try {
            val remotos = FirestoreService.obtenerTurnos()
            if (remotos.isNotEmpty()) {
                _turnosCatalogo.value = remotos
                Result.success(Unit)
            } else {
                val res = FirestoreService.forzarPobladoTurnosCatalogo()
                if (res.isSuccess) {
                    val turnosCargados = FirestoreService.obtenerTurnos()
                    if (turnosCargados.isNotEmpty()) {
                        _turnosCatalogo.value = turnosCargados
                    }
                }
                res
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarTurnoCatalogo(turno: TurnoTemplate, esNuevo: Boolean): Result<Unit> {
        // Actualización optimista en memoria para que no se pierdan los cambios del usuario
        _turnosCatalogo.update { lista ->
            if (esNuevo) {
                val sinDuplicado = lista.filterNot { it.id == turno.id }
                sinDuplicado + turno
            } else {
                lista.map { if (it.id == turno.id) turno else it }
            }
        }

        val resultado = FirestoreService.guardarTurno(turno)
        if (resultado.isSuccess) {
            println("Turno ${turno.id} persistido exitosamente en Firestore")
        } else {
            val errMsg = resultado.exceptionOrNull()?.message ?: "Error desconocido"
            println("Fallo al guardar turno en Firestore: $errMsg")
        }
        return resultado
    }

    fun alternarEstadoTurno(idTurno: String) {
        val turnoActual = _turnosCatalogo.value.find { it.id == idTurno } ?: return
        val nuevoEstado = if (turnoActual.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
        val turnoActualizado = turnoActual.copy(estado = nuevoEstado)
        repositoryScope.launch {
            guardarTurnoCatalogo(turnoActualizado, esNuevo = false)
        }
    }

    fun agregarTurnoCatalogo(turno: TurnoTemplate) {
        repositoryScope.launch {
            guardarTurnoCatalogo(turno, esNuevo = true)
        }
    }

    fun getTurnoHoy(): TurnoAsignado? {
        // Retorna el turno de hoy de forma dinámica según la lista semanal
        return _horarioSemana.value.firstOrNull { !it.esDescanso && !it.sinAsignar }
    }
}

object NovedadRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _novedades = MutableStateFlow(MockDataProviders.inicialNovedades())
    val novedades: StateFlow<List<Novedad>> = _novedades.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    fun iniciarSincronizacionFirestore(forzar: Boolean = false) {
        if (!forzar && jobSincronizacion?.isActive == true) return
        jobSincronizacion?.cancel()
        jobSincronizacion = repositoryScope.launch {
            try {
                // 1. Cargar desde Firestore o inicializar si está vacío
                val remotos = FirestoreService.obtenerNovedades()
                if (remotos.isNotEmpty()) {
                    _novedades.value = remotos
                } else {
                    FirestoreService.inicializarNovedadesSiEstaVacio()
                    val cargados = FirestoreService.obtenerNovedades()
                    if (cargados.isNotEmpty()) {
                        _novedades.value = cargados
                    }
                }

                // 2. Escuchar en tiempo real cambios en /novedades
                FirestoreService.observarNovedades().collect { listaRemota ->
                    if (listaRemota.isNotEmpty()) {
                        _novedades.value = listaRemota
                    }
                }
            } catch (e: Exception) {
                println("Error en sincronización de Novedades: ${e.message}")
            }
        }
    }

    fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    suspend fun guardarNovedad(novedad: Novedad): Result<Unit> {
        // Optimistic update
        _novedades.update { lista ->
            val sinDuplicado = lista.filterNot { it.id == novedad.id }
            listOf(novedad) + sinDuplicado
        }
        val res = FirestoreService.guardarNovedad(novedad)
        if (res.isFailure) {
            println("Error al guardar novedad en Firestore: ${res.exceptionOrNull()?.message}")
        }
        return res
    }

    fun reportarNovedad(novedad: Novedad) {
        repositoryScope.launch {
            guardarNovedad(novedad)
        }
    }

    fun revisarNovedad(idNovedad: String, comentario: String, revisor: String, fecha: String) {
        val novedadActual = _novedades.value.find { it.id == idNovedad } ?: return
        val novedadActualizada = novedadActual.revisar(
            comentario = comentario,
            revisor = revisor,
            fecha = fecha
        )
        repositoryScope.launch {
            guardarNovedad(novedadActualizada)
            if (novedadActualizada.tipo.contains("incapacidad", ignoreCase = true)) {
                aplicarIncapacidadEnTurno(novedadActualizada)
            }
        }
    }

    fun cambiarEstadoNovedad(idNovedad: String, nuevoEstado: EstadoNovedad) {
        val novedadActual = _novedades.value.find { it.id == idNovedad } ?: return
        val novedadActualizada = novedadActual.copy(estado = nuevoEstado)
        repositoryScope.launch {
            guardarNovedad(novedadActualizada)

            // Si es una novedad de incapacidad médica y fue revisada/aprobada,
            // aplicamos el reemplazo del turno por "Incapacidad" bloqueada
            if (nuevoEstado == EstadoNovedad.REVISADA && novedadActualizada.tipo.contains("incapacidad", ignoreCase = true)) {
                aplicarIncapacidadEnTurno(novedadActualizada)
            }
        }
    }

    private suspend fun aplicarIncapacidadEnTurno(novedad: Novedad) {
        val fecha = novedad.fechaReferencia.ifBlank {
            novedad.detalleTurno.split("·").lastOrNull()?.trim() ?: ""
        }
        if (fecha.isBlank()) return

        val todosEmp = EmpleadoRepository.empleados.value
        val emp = todosEmp.find {
            (novedad.empleadoId.isNotBlank() && it.id == novedad.empleadoId) ||
            it.nombre.equals(novedad.empleado, ignoreCase = true) ||
            "${it.nombre} ${it.apellido}".trim().equals(novedad.empleado, ignoreCase = true)
        }

        val empId = emp?.id ?: novedad.empleadoId.ifBlank { "emp_1" }
        val empNombre = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: novedad.empleado.ifBlank { "Empleado" }
        val empSucursal = emp?.sucursal ?: novedad.sucursal.ifBlank { "CENTRO" }

        val todasAsigs = TurnoRepository.asignaciones.value
        val asigExistente = todasAsigs.find {
            (it.empleadoId == empId || (it.empleadoNombre.isNotBlank() && it.empleadoNombre.equals(empNombre, ignoreCase = true))) &&
            it.fechaIso == fecha
        }

        val asigIncapacidad = if (asigExistente != null) {
            asigExistente.copy(
                tipoTurno = "Incapacidad",
                horario = "-",
                esDescanso = false,
                modificadoTrasPublicar = true
            )
        } else {
            AsignacionTurno(
                id = "${empId}_${fecha}",
                fechaIso = fecha,
                empleadoId = empId,
                empleadoNombre = empNombre,
                turnoId = "",
                tipoTurno = "Incapacidad",
                horario = "-",
                sucursalId = empSucursal,
                programacionId = "",
                esDescanso = false,
                estado = "PUBLICADA",
                modificadoTrasPublicar = false
            )
        }

        TurnoRepository.guardarAsignacion(asigIncapacidad)
    }
}

object CambiosTurnoRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _solicitudes = MutableStateFlow<List<SolicitudCambioTurno>>(emptyList())
    val solicitudes: StateFlow<List<SolicitudCambioTurno>> = _solicitudes.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    fun iniciarSincronizacionFirestore(forzar: Boolean = false) {
        if (!forzar && jobSincronizacion?.isActive == true) return
        jobSincronizacion?.cancel()
        jobSincronizacion = repositoryScope.launch {
            try {
                val remotos = FirestoreService.obtenerSolicitudesCambio()
                if (remotos.isNotEmpty()) {
                    _solicitudes.value = remotos
                }

                FirestoreService.observarSolicitudesCambio().collect { listaRemota ->
                    _solicitudes.value = listaRemota
                }
            } catch (e: Exception) {
                println("Error en sincronización de Solicitudes de Cambio: ${e.message}")
            }
        }
    }

    fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    suspend fun guardarSolicitud(solicitud: SolicitudCambioTurno): Result<Unit> {
        _solicitudes.update { lista ->
            val sinDuplicado = lista.filterNot { it.id == solicitud.id }
            listOf(solicitud) + sinDuplicado
        }
        val res = FirestoreService.guardarSolicitudCambio(solicitud)
        if (res.isFailure) {
            println("Error al guardar solicitud de cambio en Firestore: ${res.exceptionOrNull()?.message}")
        }
        return res
    }

    fun crearSolicitud(solicitud: SolicitudCambioTurno) {
        repositoryScope.launch {
            guardarSolicitud(solicitud)
        }
    }

    fun aceptarPorCompanero(idSolicitud: String) {
        val actual = _solicitudes.value.find { it.id == idSolicitud } ?: return
        val actualizada = actual.aceptarCompanero()
        repositoryScope.launch {
            guardarSolicitud(actualizada)
        }
    }

    fun rechazarSolicitud(idSolicitud: String, motivo: String) {
        val actual = _solicitudes.value.find { it.id == idSolicitud } ?: return
        val actualizada = actual.rechazar(motivo)
        repositoryScope.launch {
            guardarSolicitud(actualizada)
        }
    }

    fun cancelarSolicitud(idSolicitud: String) {
        val actual = _solicitudes.value.find { it.id == idSolicitud } ?: return
        val actualizada = actual.copy(
            estado = EstadoSolicitudCambio.RECHAZADA,
            motivoRechazo = "Cancelada por el solicitante"
        )
        repositoryScope.launch {
            guardarSolicitud(actualizada)
        }
    }

    fun aprobarEIntercambiar(solicitud: SolicitudCambioTurno) {
        repositoryScope.launch {
            val aprobada = solicitud.aprobar()
            guardarSolicitud(aprobada)
            intercambiarTurnos(solicitud)
        }
    }

    private suspend fun intercambiarTurnos(solicitud: SolicitudCambioTurno) {
        val asignacionesActuales = TurnoRepository.asignaciones.value
        val empAId = solicitud.empleadoSolicitanteId
        val empANombre = solicitud.nombreSolicitante
        val empBId = solicitud.empleadoCompaneroId
        val empBNombre = solicitud.nombreCompanero

        var asigEntrega = asignacionesActuales.find {
            (it.id.isNotBlank() && it.id == solicitud.turnoEntregaId) ||
            (it.empleadoId == empAId && (it.id.endsWith(solicitud.turnoEntregaId) || solicitud.detalleTurnoEntrega.contains(it.fechaIso)))
        }
        var asigRecibe = asignacionesActuales.find {
            (it.id.isNotBlank() && it.id == solicitud.turnoRecibeId) ||
            (it.empleadoId == empBId && (it.id.endsWith(solicitud.turnoRecibeId) || solicitud.detalleTurnoRecibe.contains(it.fechaIso)))
        }

        val fechaEntregaIso = asigEntrega?.fechaIso ?: extraerFechaIso(solicitud.turnoEntregaId).ifBlank { extraerFechaIso(solicitud.detalleTurnoEntrega) }
        val fechaRecibeIso = asigRecibe?.fechaIso ?: extraerFechaIso(solicitud.turnoRecibeId).ifBlank { extraerFechaIso(solicitud.detalleTurnoRecibe) }

        if (asigEntrega == null && fechaEntregaIso.isNotBlank()) {
            asigEntrega = asignacionesActuales.find { it.empleadoId == empAId && it.fechaIso == fechaEntregaIso }
        }
        if (asigRecibe == null && fechaRecibeIso.isNotBlank()) {
            asigRecibe = asignacionesActuales.find { it.empleadoId == empBId && it.fechaIso == fechaRecibeIso }
        }

        val nuevasAsignaciones = mutableListOf<AsignacionTurno>()

        if (fechaEntregaIso == fechaRecibeIso && asigEntrega != null && asigRecibe != null) {
            nuevasAsignaciones.add(
                asigEntrega.copy(
                    tipoTurno = asigRecibe.tipoTurno,
                    horario = asigRecibe.horario,
                    turnoId = asigRecibe.turnoId,
                    turno = asigRecibe.turno,
                    modificadoTrasPublicar = true
                )
            )
            nuevasAsignaciones.add(
                asigRecibe.copy(
                    tipoTurno = asigEntrega.tipoTurno,
                    horario = asigEntrega.horario,
                    turnoId = asigEntrega.turnoId,
                    turno = asigEntrega.turno,
                    modificadoTrasPublicar = true
                )
            )
        } else {
            if (fechaEntregaIso.isNotBlank() && asigEntrega != null) {
                val bEnFechaEntrega = asignacionesActuales.find { it.empleadoId == empBId && it.fechaIso == fechaEntregaIso }
                val nuevoTurnoB = if (bEnFechaEntrega != null) {
                    bEnFechaEntrega.copy(
                        tipoTurno = asigEntrega.tipoTurno,
                        horario = asigEntrega.horario,
                        turnoId = asigEntrega.turnoId,
                        turno = asigEntrega.turno,
                        esDescanso = false,
                        modificadoTrasPublicar = true
                    )
                } else {
                    AsignacionTurno(
                        id = "${empBId}_$fechaEntregaIso",
                        fechaIso = fechaEntregaIso,
                        empleadoId = empBId,
                        empleadoNombre = empBNombre,
                        tipoTurno = asigEntrega.tipoTurno,
                        horario = asigEntrega.horario,
                        turnoId = asigEntrega.turnoId,
                        turno = asigEntrega.turno,
                        sucursalId = asigEntrega.sucursalId,
                        estado = "PUBLICADA",
                        esDescanso = false,
                        modificadoTrasPublicar = true
                    )
                }
                val nuevoTurnoA = asigEntrega.copy(
                    tipoTurno = "Descanso",
                    horario = "-",
                    esDescanso = true,
                    modificadoTrasPublicar = true
                )
                nuevasAsignaciones.add(nuevoTurnoB)
                nuevasAsignaciones.add(nuevoTurnoA)
            }

            if (fechaRecibeIso.isNotBlank() && asigRecibe != null) {
                val aEnFechaRecibe = asignacionesActuales.find { it.empleadoId == empAId && it.fechaIso == fechaRecibeIso }
                val nuevoTurnoA = if (aEnFechaRecibe != null) {
                    aEnFechaRecibe.copy(
                        tipoTurno = asigRecibe.tipoTurno,
                        horario = asigRecibe.horario,
                        turnoId = asigRecibe.turnoId,
                        turno = asigRecibe.turno,
                        esDescanso = false,
                        modificadoTrasPublicar = true
                    )
                } else {
                    AsignacionTurno(
                        id = "${empAId}_$fechaRecibeIso",
                        fechaIso = fechaRecibeIso,
                        empleadoId = empAId,
                        empleadoNombre = empANombre,
                        tipoTurno = asigRecibe.tipoTurno,
                        horario = asigRecibe.horario,
                        turnoId = asigRecibe.turnoId,
                        turno = asigRecibe.turno,
                        sucursalId = asigRecibe.sucursalId,
                        estado = "PUBLICADA",
                        esDescanso = false,
                        modificadoTrasPublicar = true
                    )
                }
                val nuevoTurnoB = asigRecibe.copy(
                    tipoTurno = "Descanso",
                    horario = "-",
                    esDescanso = true,
                    modificadoTrasPublicar = true
                )
                nuevasAsignaciones.add(nuevoTurnoA)
                nuevasAsignaciones.add(nuevoTurnoB)
            }
        }

        if (nuevasAsignaciones.isNotEmpty()) {
            TurnoRepository.guardarAsignaciones(nuevasAsignaciones)
        }
    }

    private fun extraerFechaIso(texto: String): String {
        val regex = Regex("""\d{4}-\d{2}-\d{2}""")
        return regex.find(texto)?.value ?: ""
    }
}

object AsistenciaRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _notificaciones = MutableStateFlow(MockDataProviders.inicialIncumplimientos())
    val notificaciones: StateFlow<List<NotificacionIncumplimiento>> = _notificaciones.asStateFlow()

    private val _registrosDiarios = MutableStateFlow(MockDataProviders.inicialRegistrosAsistencia())
    val registrosDiarios: StateFlow<List<RegistroAsistencia>> = _registrosDiarios.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    fun iniciarSincronizacionFirestore(forzar: Boolean = false) {
        if (!forzar && jobSincronizacion?.isActive == true) return
        jobSincronizacion?.cancel()
        jobSincronizacion = repositoryScope.launch {
            try {
                // 1. Cargar registros de asistencia
                val remotos = FirestoreService.obtenerAsistencia()
                if (remotos.isNotEmpty()) {
                    _registrosDiarios.value = remotos
                }
                launch {
                    FirestoreService.observarAsistencia().collect { listaRemota ->
                        if (listaRemota.isNotEmpty()) {
                            _registrosDiarios.value = listaRemota
                        }
                    }
                }

                // 2. Cargar notificaciones de incumplimientos
                val remotosInc = FirestoreService.obtenerIncumplimientos()
                if (remotosInc.isNotEmpty()) {
                    _notificaciones.value = remotosInc
                } else {
                    FirestoreService.inicializarIncumplimientosSiEstaVacio()
                    val cargados = FirestoreService.obtenerIncumplimientos()
                    if (cargados.isNotEmpty()) {
                        _notificaciones.value = cargados
                    }
                }
                launch {
                    FirestoreService.observarIncumplimientos().collect { listaRemota ->
                        if (listaRemota.isNotEmpty()) {
                            _notificaciones.value = listaRemota
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error en sincronización de Asistencia: ${e.message}")
            }
        }
    }

    fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    suspend fun guardarRegistroAsistencia(registro: RegistroAsistencia): Result<Unit> {
        // Optimistic update local
        _registrosDiarios.update { existentes ->
            val filtrados = existentes.filterNot {
                it.id == registro.id || (it.empleadoId == registro.empleadoId && it.fechaIso == registro.fechaIso)
            }
            filtrados + registro
        }
        val res = FirestoreService.guardarAsistencia(registro)
        if (res.isFailure) {
            println("Error al guardar registro de asistencia en Firestore: ${res.exceptionOrNull()?.message}")
        }
        return res
    }

    fun guardarRegistroAsistenciaEnBackground(registro: RegistroAsistencia) {
        repositoryScope.launch {
            guardarRegistroAsistencia(registro)
        }
    }

    fun registrarIncumplimiento(notif: NotificacionIncumplimiento) {
        repositoryScope.launch {
            guardarIncumplimiento(notif)
        }
    }

    suspend fun guardarIncumplimiento(notif: NotificacionIncumplimiento): Result<Unit> {
        _notificaciones.update { existentes ->
            val sinDuplicado = existentes.filterNot { it.id == notif.id }
            listOf(notif) + sinDuplicado
        }
        val res = FirestoreService.guardarIncumplimiento(notif)
        if (res.isFailure) {
            println("Error al guardar incumplimiento en Firestore: ${res.exceptionOrNull()?.message}")
        }
        return res
    }

    suspend fun atenderIncumplimiento(id: String, motivo: String, horaAtencion: String): Result<Unit> {
        val actual = _notificaciones.value.find { it.id == id } ?: return Result.failure(Exception("Notificación no encontrada"))
        val actualizada = actual.copy(
            atendida = true,
            motivoAtencion = motivo,
            horaAtencion = horaAtencion
        )
        return guardarIncumplimiento(actualizada)
    }

    suspend fun resolverIncumplimientoPorLlegada(empleadoId: String, fechaIso: String): Result<Unit> {
        val pendientes = _notificaciones.value.filter {
            it.empleadoId == empleadoId && it.fechaIso == fechaIso && !it.atendida
        }
        for (notif in pendientes) {
            val resuelta = notif.copy(
                atendida = true,
                motivoAtencion = "Empleado marcó ingreso tardío",
                horaAtencion = ""
            )
            guardarIncumplimiento(resuelta)
        }
        return Result.success(Unit)
    }

    suspend fun registrarMarcaSupervisor(
        registroId: String,
        empleadoId: String,
        empleadoNombre: String,
        sucursal: String,
        fechaIso: String,
        tipoTurno: String,
        horarioProg: String,
        tipoMarca: String,
        horaMarca: String,
        motivo: String
    ): Result<Unit> {
        if (motivo.isBlank()) {
            return Result.failure(IllegalArgumentException("El motivo de la marcación por supervisor es obligatorio."))
        }

        val existente = _registrosDiarios.value.find {
            it.id == registroId || (it.empleadoId == empleadoId && it.fechaIso == fechaIso)
        }

        var entrada = existente?.horaEntrada ?: "-"
        var salida = existente?.horaSalida ?: "-"
        var descIni = existente?.inicioDescanso ?: "-"
        var descFin = existente?.finDescanso ?: "-"
        var estadoAsis = existente?.estadoAsistencia ?: EstadoAsistencia.SIN_INICIAR
        var estadoStr = existente?.estado ?: "PENDIENTE"
        var minsTarde = existente?.minutosTarde ?: 0

        when (tipoMarca) {
            "ENTRADA" -> {
                if (existente != null && existente.horaEntrada != "-" && !existente.registradoPorSupervisor) {
                    return Result.failure(IllegalStateException("La marcación de entrada ya fue registrada por el empleado y no puede sobrescribirse."))
                }
                entrada = horaMarca
                estadoAsis = EstadoAsistencia.EN_TURNO
                val partes = horarioProg.split("-")
                if (partes.isNotEmpty()) {
                    val horaIniProgMin = parsearMinutos(partes[0]) ?: 0
                    val horaEntradaMin = parsearMinutos(horaMarca) ?: 0
                    minsTarde = maxOf(0, horaEntradaMin - horaIniProgMin)
                }
                estadoStr = if (minsTarde > 0) "TARDE" else "PRESENTE"
            }
            "INICIO_DESCANSO" -> {
                if (existente != null && existente.inicioDescanso != "-" && !existente.registradoPorSupervisor) {
                    return Result.failure(IllegalStateException("El inicio de descanso ya fue registrado por el empleado."))
                }
                descIni = horaMarca
                estadoAsis = EstadoAsistencia.EN_DESCANSO
                estadoStr = "EN DESCANSO"
            }
            "FIN_DESCANSO" -> {
                if (existente != null && existente.finDescanso != "-" && !existente.registradoPorSupervisor) {
                    return Result.failure(IllegalStateException("El fin de descanso ya fue registrado por el empleado."))
                }
                descFin = horaMarca
                estadoAsis = EstadoAsistencia.EN_TURNO
                estadoStr = if (minsTarde > 0) "TARDE" else "PRESENTE"
            }
            "SALIDA" -> {
                if (existente != null && existente.horaSalida != "-" && !existente.registradoPorSupervisor) {
                    return Result.failure(IllegalStateException("La marcación de salida ya fue registrada por el empleado."))
                }
                salida = horaMarca
                estadoAsis = EstadoAsistencia.FINALIZADO
                estadoStr = "FINALIZADO"
            }
        }

        val minsTrabajados = if (entrada != "-" && salida != "-") {
            calcularMinutosTrabajados(entrada, salida, descIni, descFin)
        } else {
            existente?.tiempoEfectivoMinutos ?: 0
        }

        val registroActualizado = RegistroAsistencia(
            id = if (registroId.isNotBlank()) registroId else "${empleadoId}_${fechaIso}",
            empleadoId = empleadoId,
            empleado = empleadoNombre.ifBlank { existente?.empleado ?: "Empleado" },
            sucursal = sucursal.ifBlank { existente?.sucursal ?: "CENTRO" },
            fechaIso = fechaIso,
            tipoTurno = tipoTurno.ifBlank { existente?.tipoTurno ?: "Turno General" },
            horarioProgramado = horarioProg.ifBlank { existente?.horarioProgramado ?: "" },
            horaEntrada = entrada,
            horaSalida = salida,
            inicioDescanso = descIni,
            finDescanso = descFin,
            estado = estadoStr,
            estadoAsistencia = estadoAsis,
            tiempoEfectivoMinutos = minsTrabajados,
            minutosTarde = minsTarde,
            esDescanso = existente?.esDescanso ?: false,
            registradoPorSupervisor = true,
            motivoSupervisor = motivo
        )

        val resultado = guardarRegistroAsistencia(registroActualizado)
        if (resultado.isSuccess && tipoMarca == "ENTRADA") {
            resolverIncumplimientoPorLlegada(empleadoId, fechaIso)
        }
        return resultado
    }

    private fun parsearMinutos(horaStr: String): Int? {
        val limpia = horaStr.trim()
        val partes = limpia.split(":")
        if (partes.size == 2) {
            val h = partes[0].toIntOrNull() ?: return null
            val m = partes[1].toIntOrNull() ?: return null
            return h * 60 + m
        }
        return null
    }

    private fun calcularMinutosTrabajados(
        entrada: String,
        salida: String,
        inicioDesc: String,
        finDesc: String
    ): Int {
        val ini = parsearMinutos(entrada) ?: return 0
        val fin = parsearMinutos(salida) ?: return 0
        var total = fin - ini
        if (total < 0) total += 24 * 60

        val dIni = parsearMinutos(inicioDesc)
        val dFin = parsearMinutos(finDesc)
        if (dIni != null && dFin != null) {
            var durDesc = dFin - dIni
            if (durDesc < 0) durDesc += 24 * 60
            total = maxOf(0, total - durDesc)
        }
        return total
    }

    fun actualizarNotificaciones(lista: List<NotificacionIncumplimiento>) {
        _notificaciones.value = lista
    }
}
