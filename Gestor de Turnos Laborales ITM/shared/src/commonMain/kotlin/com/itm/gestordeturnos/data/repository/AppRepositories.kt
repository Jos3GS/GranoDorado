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

    init {
        iniciarSincronizacionFirestore()
    }

    fun iniciarSincronizacionFirestore() {
        repositoryScope.launch {
            try {
                // 1. Si la colección de Firestore está vacía, poblarla con el usuario inicial
                FirestoreService.inicializarSiEstaVacio()

                // 2. Escuchar en tiempo real los cambios en la colección "empleados"
                FirestoreService.observarEmpleados().collect { listaRemota ->
                    if (listaRemota.isNotEmpty()) {
                        _empleados.value = listaRemota
                    }
                }
            } catch (e: Exception) {
                // En caso de estar sin conexión o fallo temporal de red, mantiene la memoria local
            }
        }
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

    fun iniciarSincronizacionFirestore() {
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
    private val _novedades = MutableStateFlow(MockDataProviders.inicialNovedades())
    val novedades: StateFlow<List<Novedad>> = _novedades.asStateFlow()

    fun reportarNovedad(novedad: Novedad) {
        _novedades.update { it + novedad }
    }
}

object CambiosTurnoRepository {
    private val _solicitudes = MutableStateFlow(MockDataProviders.inicialSolicitudesCambio())
    val solicitudes: StateFlow<List<SolicitudCambioTurno>> = _solicitudes.asStateFlow()

    fun agregarSolicitud(solicitud: SolicitudCambioTurno) {
        _solicitudes.update { it + solicitud }
    }
}

object AsistenciaRepository {
    private val _notificaciones = MutableStateFlow(MockDataProviders.inicialIncumplimientos())
    val notificaciones: StateFlow<List<NotificacionIncumplimiento>> = _notificaciones.asStateFlow()

    private val _registrosDiarios = MutableStateFlow(MockDataProviders.inicialRegistrosAsistencia())
    val registrosDiarios: StateFlow<List<RegistroAsistencia>> = _registrosDiarios.asStateFlow()

    fun registrarIncumplimiento(notif: NotificacionIncumplimiento) {
        _notificaciones.update { it + notif }
    }
}
