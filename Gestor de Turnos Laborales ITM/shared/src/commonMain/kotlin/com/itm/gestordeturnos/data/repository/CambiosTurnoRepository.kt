package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.service.FirestoreService
import com.itm.gestordeturnos.domain.model.EstadoSolicitudCambio
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.repository.CambiosTurnoRepositoryContract
import com.itm.gestordeturnos.domain.usecase.IntercambiarTurnosUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object CambiosTurnoRepository : CambiosTurnoRepositoryContract {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _solicitudes = MutableStateFlow<List<SolicitudCambioTurno>>(emptyList())
    override val solicitudes: StateFlow<List<SolicitudCambioTurno>> = _solicitudes.asStateFlow()

    private val intercambiarTurnosUseCase by lazy {
        IntercambiarTurnosUseCase(turnoRepository = TurnoRepository)
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

    override fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    override suspend fun guardarSolicitud(solicitud: SolicitudCambioTurno): Result<Unit> {
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

    override fun crearSolicitud(solicitud: SolicitudCambioTurno) {
        repositoryScope.launch {
            guardarSolicitud(solicitud)
        }
    }

    override fun aceptarPorCompanero(idSolicitud: String) {
        val actual = _solicitudes.value.find { it.id == idSolicitud } ?: return
        val actualizada = actual.aceptarCompanero()
        repositoryScope.launch {
            guardarSolicitud(actualizada)
        }
    }

    override fun rechazarSolicitud(idSolicitud: String, motivo: String) {
        val actual = _solicitudes.value.find { it.id == idSolicitud } ?: return
        val actualizada = actual.rechazar(motivo)
        repositoryScope.launch {
            guardarSolicitud(actualizada)
        }
    }

    override fun cancelarSolicitud(idSolicitud: String) {
        val actual = _solicitudes.value.find { it.id == idSolicitud } ?: return
        val actualizada = actual.copy(
            estado = EstadoSolicitudCambio.RECHAZADA,
            motivoRechazo = "Cancelada por el solicitante"
        )
        repositoryScope.launch {
            guardarSolicitud(actualizada)
        }
    }

    override fun aprobarEIntercambiar(solicitud: SolicitudCambioTurno) {
        repositoryScope.launch {
            val aprobada = solicitud.aprobar()
            guardarSolicitud(aprobada)
            intercambiarTurnosUseCase(solicitud)
        }
    }
}
