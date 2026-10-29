package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.service.FirestoreService
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object EmpleadoRepository : EmpleadoRepositoryContract {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _empleados = MutableStateFlow(MockDataProviders.inicialEmpleados())
    override val empleados: StateFlow<List<Empleado>> = _empleados.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    override fun iniciarSincronizacionFirestore(forzar: Boolean) {
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

    override fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    override fun guardarEmpleado(empleado: Empleado, esNuevo: Boolean) {
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

    override suspend fun buscarPorCorreo(correo: String): Empleado? {
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
