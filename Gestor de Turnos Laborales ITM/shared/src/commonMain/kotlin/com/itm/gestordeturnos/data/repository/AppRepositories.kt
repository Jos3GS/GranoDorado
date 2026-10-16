package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.service.FirestoreService
import com.itm.gestordeturnos.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val _horarioSemana = MutableStateFlow(MockDataProviders.inicialHorarioSemana())
    val horarioSemana: StateFlow<List<TurnoAsignado>> = _horarioSemana.asStateFlow()

    private val _filasMalla = MutableStateFlow(MockDataProviders.inicialFilasMalla())
    val filasMalla: StateFlow<List<FilaMalla>> = _filasMalla.asStateFlow()

    private val _turnosPropios = MutableStateFlow(MockDataProviders.inicialTurnosPropios())
    val turnosPropios: StateFlow<List<TurnoSeleccionable>> = _turnosPropios.asStateFlow()

    private val _turnosCompaneros = MutableStateFlow(MockDataProviders.inicialTurnosCompaneros())
    val turnosCompaneros: StateFlow<List<TurnoCompaneroSeleccionable>> = _turnosCompaneros.asStateFlow()

    init {
        iniciarSincronizacionFirestore()
    }

    fun iniciarSincronizacionFirestore() {
        repositoryScope.launch {
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

                // 2. Escuchar en tiempo real los cambios en la colección "turnos"
                FirestoreService.observarTurnos().collect { listaRemota ->
                    if (listaRemota.isNotEmpty()) {
                        _turnosCatalogo.value = listaRemota
                    }
                }
            } catch (e: Exception) {
                println("Error en sincronización de turnos con Firestore: ${e.message}")
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
            } catch (e: Exception) {
                println("Error al refrescar turnos desde Firestore: ${e.message}")
            }
        }
    }

    fun guardarTurnoCatalogo(turno: TurnoTemplate, esNuevo: Boolean) {
        // Actualización optimista inmediata en memoria
        _turnosCatalogo.update { lista ->
            if (esNuevo) {
                val sinDuplicado = lista.filterNot { it.id == turno.id }
                sinDuplicado + turno
            } else {
                lista.map { if (it.id == turno.id) turno else it }
            }
        }

        // Persistencia asíncrona en Cloud Firestore
        repositoryScope.launch {
            val resultado = FirestoreService.guardarTurno(turno)
            if (resultado.isFailure) {
                println("Fallo al guardar turno en Firestore: ${resultado.exceptionOrNull()?.message}")
            } else {
                println("Turno ${turno.id} persistido exitosamente en Firestore")
            }
        }
    }

    fun alternarEstadoTurno(idTurno: String) {
        val turnoActual = _turnosCatalogo.value.find { it.id == idTurno } ?: return
        val nuevoEstado = if (turnoActual.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
        val turnoActualizado = turnoActual.copy(estado = nuevoEstado)
        guardarTurnoCatalogo(turnoActualizado, esNuevo = false)
    }

    fun agregarTurnoCatalogo(turno: TurnoTemplate) {
        guardarTurnoCatalogo(turno, esNuevo = true)
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
        _novedades.update { listOf(novedad) + it }
    }
}

object CambiosTurnoRepository {
    private val _solicitudes = MutableStateFlow(MockDataProviders.inicialSolicitudesCambio())
    val solicitudes: StateFlow<List<SolicitudCambio>> = _solicitudes.asStateFlow()

    fun solicitarCambio(solicitud: SolicitudCambio) {
        _solicitudes.update { listOf(solicitud) + it }
    }
}

object AsistenciaRepository {
    private val _notificaciones = MutableStateFlow(MockDataProviders.inicialIncumplimientos())
    val notificaciones: StateFlow<List<NotificacionIncumplimiento>> = _notificaciones.asStateFlow()

    private val _registrosDiarios = MutableStateFlow(MockDataProviders.inicialRegistrosAsistencia())
    val registrosDiarios: StateFlow<List<RegistroAsistencia>> = _registrosDiarios.asStateFlow()
}
