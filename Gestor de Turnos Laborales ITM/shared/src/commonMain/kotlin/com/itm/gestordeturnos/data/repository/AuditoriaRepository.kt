package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.service.FirestoreService
import com.itm.gestordeturnos.domain.model.EventoAuditoria
import com.itm.gestordeturnos.domain.repository.AuditoriaRepositoryContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Implementación Singleton del repositorio de Auditoría y Trazabilidad (RN-32).
 */
object AuditoriaRepository : AuditoriaRepositoryContract {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _eventos = MutableStateFlow<List<EventoAuditoria>>(emptyList())
    override val eventos: StateFlow<List<EventoAuditoria>> = _eventos.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    override fun iniciarSincronizacionFirestore(forzar: Boolean) {
        if (!forzar && jobSincronizacion?.isActive == true) return
        jobSincronizacion?.cancel()
        jobSincronizacion = repositoryScope.launch {
            try {
                val remotos = FirestoreService.obtenerEventosAuditoria()
                if (remotos.isNotEmpty()) {
                    _eventos.value = remotos.sortedByDescending { it.fechaHoraIso }
                }

                FirestoreService.observarEventosAuditoria().collect { listaRemota ->
                    _eventos.value = listaRemota.sortedByDescending { it.fechaHoraIso }
                }
            } catch (e: Exception) {
                println("Error en sincronización de Auditoría: ${e.message}")
            }
        }
    }

    override fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    override suspend fun registrarEvento(evento: EventoAuditoria): Result<Unit> {
        _eventos.update { lista ->
            (listOf(evento) + lista.filterNot { it.id == evento.id }).sortedByDescending { it.fechaHoraIso }
        }
        return FirestoreService.guardarEventoAuditoria(evento)
    }

    override fun registrarEventoEnBackground(evento: EventoAuditoria) {
        repositoryScope.launch {
            registrarEvento(evento)
        }
    }
}
