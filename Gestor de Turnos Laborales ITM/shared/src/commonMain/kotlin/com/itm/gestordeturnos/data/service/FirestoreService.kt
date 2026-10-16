package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.guardarEmpleadoFirestore
import com.itm.gestordeturnos.observarEmpleadosFirestore
import com.itm.gestordeturnos.obtenerEmpleadosFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirestoreService {

    fun observarEmpleados(): Flow<List<Empleado>> = callbackFlow {
        observarEmpleadosFirestore { lista ->
            trySend(lista)
        }
        awaitClose { }
    }

    suspend fun obtenerEmpleados(): List<Empleado> {
        return obtenerEmpleadosFirestore()
    }

    suspend fun guardarEmpleado(empleado: Empleado): Result<Unit> {
        return guardarEmpleadoFirestore(empleado)
    }

    suspend fun inicializarSiEstaVacio(): Boolean {
        return try {
            val existentes = obtenerEmpleadosFirestore()
            if (existentes.isEmpty()) {
                val adminInicial = MockDataProviders.inicialEmpleados().firstOrNull()
                if (adminInicial != null) {
                    guardarEmpleadoFirestore(adminInicial)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
