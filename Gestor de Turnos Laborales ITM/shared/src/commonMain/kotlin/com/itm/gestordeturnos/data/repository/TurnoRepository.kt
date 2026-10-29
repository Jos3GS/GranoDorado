package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.service.FirestoreService
import com.itm.gestordeturnos.domain.model.*
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object TurnoRepository : TurnoRepositoryContract {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _turnosCatalogo = MutableStateFlow(MockDataProviders.inicialTurnosCatalogo())
    override val turnosCatalogo: StateFlow<List<TurnoTemplate>> = _turnosCatalogo.asStateFlow()

    private val _asignaciones = MutableStateFlow<List<AsignacionTurno>>(emptyList())
    override val asignaciones: StateFlow<List<AsignacionTurno>> = _asignaciones.asStateFlow()

    private val _horarioSemana = MutableStateFlow(MockDataProviders.inicialHorarioSemana())
    override val horarioSemana: StateFlow<List<TurnoAsignado>> = _horarioSemana.asStateFlow()

    private val _filasMalla = MutableStateFlow(MockDataProviders.inicialFilasMalla())
    override val filasMalla: StateFlow<List<FilaMalla>> = _filasMalla.asStateFlow()

    private val _turnosPropios = MutableStateFlow(MockDataProviders.inicialTurnosPropios())
    override val turnosPropios: StateFlow<List<TurnoSeleccionable>> = _turnosPropios.asStateFlow()

    private val _turnosCompaneros = MutableStateFlow(MockDataProviders.inicialTurnosCompaneros())
    override val turnosCompaneros: StateFlow<List<TurnoCompaneroSeleccionable>> = _turnosCompaneros.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    override fun iniciarSincronizacionFirestore(forzar: Boolean) {
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

    override fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    override fun refrescarDesdeFirestore() {
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

    override suspend fun guardarAsignacion(asignacion: AsignacionTurno): Result<Unit> {
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

    override suspend fun guardarAsignaciones(lista: List<AsignacionTurno>): Result<Unit> {
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

    override fun guardarAsignacionesEnBackground(lista: List<AsignacionTurno>) {
        if (lista.isEmpty()) return
        repositoryScope.launch {
            guardarAsignaciones(lista)
        }
    }

    override suspend fun forzarSincronizacionCatalogo(): Result<Unit> {
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

    override suspend fun guardarTurnoCatalogo(turno: TurnoTemplate, esNuevo: Boolean): Result<Unit> {
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

    override fun alternarEstadoTurno(idTurno: String) {
        val turnoActual = _turnosCatalogo.value.find { it.id == idTurno } ?: return
        val nuevoEstado = if (turnoActual.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
        val turnoActualizado = turnoActual.copy(estado = nuevoEstado)
        repositoryScope.launch {
            guardarTurnoCatalogo(turnoActualizado, esNuevo = false)
        }
    }

    override fun agregarTurnoCatalogo(turno: TurnoTemplate) {
        repositoryScope.launch {
            guardarTurnoCatalogo(turno, esNuevo = true)
        }
    }

    override fun getTurnoHoy(): TurnoAsignado? {
        // Retorna el turno de hoy de forma dinámica según la lista semanal
        return _horarioSemana.value.firstOrNull { !it.esDescanso && !it.sinAsignar }
    }
}
