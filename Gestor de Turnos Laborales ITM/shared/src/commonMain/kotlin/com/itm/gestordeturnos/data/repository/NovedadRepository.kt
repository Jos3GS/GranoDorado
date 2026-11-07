package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.service.FirestoreService
import com.itm.gestordeturnos.domain.model.EstadoNovedad
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.repository.NovedadRepositoryContract
import com.itm.gestordeturnos.domain.usecase.AplicarIncapacidadEnTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.AplicarPermisoEnTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.GestionarEstadoNovedadUseCase
import com.itm.gestordeturnos.domain.usecase.ValidacionGestionNovedadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object NovedadRepository : NovedadRepositoryContract {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _novedades = MutableStateFlow(MockDataProviders.inicialNovedades())
    override val novedades: StateFlow<List<Novedad>> = _novedades.asStateFlow()

    private val gestionarEstadoNovedadUseCase by lazy {
        GestionarEstadoNovedadUseCase()
    }

    private val aplicarIncapacidadUseCase by lazy {
        AplicarIncapacidadEnTurnoUseCase(
            empleadoRepository = EmpleadoRepository,
            turnoRepository = TurnoRepository
        )
    }

    private val aplicarPermisoUseCase by lazy {
        AplicarPermisoEnTurnoUseCase(
            empleadoRepository = EmpleadoRepository,
            turnoRepository = TurnoRepository
        )
    }

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    override fun iniciarSincronizacionFirestore(forzar: Boolean) {
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

    override fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    override suspend fun guardarNovedad(novedad: Novedad): Result<Unit> {
        // Optimistic update
        _novedades.update { lista ->
            val sinDuplicado = lista.filterNot { it.id == novedad.id }
            listOf(novedad) + sinDuplicado
        }
        val res = FirestoreService.guardarNovedad(novedad)
        if (res.isFailure) {
            println("Error al guardar novedad en Firestore: ${res.exceptionOrNull()?.message}")
        }
        // Si la novedad se guarda/nace en estado REVISADA, aplicar bloqueo de incapacidad o permiso (RN-17, RN-25)
        if (novedad.estado == EstadoNovedad.REVISADA) {
            if (novedad.tipo.contains("incapacidad", ignoreCase = true)) {
                aplicarIncapacidadUseCase(novedad)
            } else if (novedad.tipo.contains("permiso", ignoreCase = true)) {
                aplicarPermisoUseCase(novedad)
            }
        }
        return res
    }

    override fun reportarNovedad(novedad: Novedad) {
        repositoryScope.launch {
            guardarNovedad(novedad)
        }
    }

    override fun revisarNovedad(idNovedad: String, comentario: String, revisor: String, fecha: String) {
        val novedadActual = _novedades.value.find { it.id == idNovedad } ?: return
        val novedadActualizada = novedadActual.revisar(
            comentario = comentario,
            revisor = revisor,
            fecha = fecha
        )
        val validacionIntegridad = gestionarEstadoNovedadUseCase.validarRevisionAdministrador(novedadActual, novedadActualizada)
        if (validacionIntegridad is ValidacionGestionNovedadResult.Invalido) {
            println("Error de integridad en revisión: ${validacionIntegridad.mensaje}")
            return
        }
        repositoryScope.launch {
            guardarNovedad(novedadActualizada)
        }
    }

    override fun cambiarEstadoNovedad(idNovedad: String, nuevoEstado: EstadoNovedad) {
        val novedadActual = _novedades.value.find { it.id == idNovedad } ?: return
        val validacion = gestionarEstadoNovedadUseCase.validarTransicionEstado(novedadActual.estado, nuevoEstado)
        if (validacion is ValidacionGestionNovedadResult.Invalido) {
            println("No se puede cambiar estado de novedad (RN-25): ${validacion.mensaje}")
            return
        }
        val novedadActualizada = novedadActual.copy(estado = nuevoEstado)
        repositoryScope.launch {
            guardarNovedad(novedadActualizada)
        }
    }

    override suspend fun eliminarNovedad(idNovedad: String): Result<Unit> {
        val novedadActual = _novedades.value.find { it.id == idNovedad }
        if (novedadActual != null && novedadActual.estado == EstadoNovedad.REVISADA) {
            return Result.failure(IllegalStateException("La novedad ya ha sido revisada y no puede ser eliminada."))
        }
        _novedades.update { lista ->
            lista.filterNot { it.id == idNovedad }
        }
        return FirestoreService.eliminarNovedad(idNovedad)
    }
}
