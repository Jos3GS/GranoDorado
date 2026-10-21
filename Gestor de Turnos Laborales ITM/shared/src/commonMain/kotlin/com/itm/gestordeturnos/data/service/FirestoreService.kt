package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import com.itm.gestordeturnos.guardarAsignacionFirestore
import com.itm.gestordeturnos.guardarEmpleadoFirestore
import com.itm.gestordeturnos.guardarTurnoFirestore
import com.itm.gestordeturnos.observarAsignacionesFirestore
import com.itm.gestordeturnos.observarEmpleadosFirestore
import com.itm.gestordeturnos.observarTurnosFirestore
import com.itm.gestordeturnos.obtenerAsignacionesFirestore
import com.itm.gestordeturnos.obtenerEmpleadosFirestore
import com.itm.gestordeturnos.obtenerTurnosFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FirestoreService {

    // --- Empleados ---
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
                val iniciales = MockDataProviders.inicialEmpleados()
                var algunGuardado = false
                for (emp in iniciales) {
                    val res = guardarEmpleadoFirestore(emp)
                    if (res.isSuccess) {
                        algunGuardado = true
                    }
                }
                algunGuardado
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    // --- Turnos de Catálogo ---
    fun observarTurnos(): Flow<List<TurnoTemplate>> = callbackFlow {
        observarTurnosFirestore { lista ->
            trySend(lista)
        }
        awaitClose { }
    }

    suspend fun obtenerTurnos(): List<TurnoTemplate> {
        return obtenerTurnosFirestore()
    }

    suspend fun guardarTurno(turno: TurnoTemplate): Result<Unit> {
        return guardarTurnoFirestore(turno)
    }

    suspend fun inicializarTurnosSiEstaVacio(): Boolean {
        return try {
            val existentes = obtenerTurnosFirestore()
            if (existentes.isEmpty()) {
                val iniciales = MockDataProviders.inicialTurnosCatalogo()
                var algunGuardado = false
                for (t in iniciales) {
                    val res = guardarTurnoFirestore(t)
                    if (res.isSuccess) {
                        algunGuardado = true
                    }
                }
                algunGuardado
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun forzarPobladoTurnosCatalogo(): Result<Unit> {
        val iniciales = MockDataProviders.inicialTurnosCatalogo()
        for (t in iniciales) {
            val res = guardarTurnoFirestore(t)
            if (res.isFailure) return res
        }
        return Result.success(Unit)
    }

    // --- Asignaciones Diarias Individuales ---
    fun observarAsignaciones(): Flow<List<AsignacionTurno>> = callbackFlow {
        observarAsignacionesFirestore { lista ->
            trySend(lista)
        }
        awaitClose { }
    }

    suspend fun obtenerAsignaciones(): List<AsignacionTurno> {
        return obtenerAsignacionesFirestore()
    }

    suspend fun guardarAsignacion(asignacion: AsignacionTurno): Result<Unit> {
        return guardarAsignacionFirestore(asignacion)
    }
}
