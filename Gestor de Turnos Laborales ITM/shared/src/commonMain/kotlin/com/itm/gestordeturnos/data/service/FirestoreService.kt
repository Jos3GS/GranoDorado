package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.model.NotificacionIncumplimiento
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import com.itm.gestordeturnos.guardarAsignacionFirestore
import com.itm.gestordeturnos.guardarAsignacionesBatchFirestore
import com.itm.gestordeturnos.guardarEmpleadoFirestore
import com.itm.gestordeturnos.guardarNovedadFirestore
import com.itm.gestordeturnos.guardarIncumplimientoFirestore
import com.itm.gestordeturnos.guardarSolicitudCambioFirestore
import com.itm.gestordeturnos.guardarRegistroAsistenciaFirestore
import com.itm.gestordeturnos.guardarTurnoFirestore
import com.itm.gestordeturnos.observarAsignacionesFirestore
import com.itm.gestordeturnos.observarEmpleadosFirestore
import com.itm.gestordeturnos.observarNovedadesFirestore
import com.itm.gestordeturnos.observarIncumplimientosFirestore
import com.itm.gestordeturnos.observarSolicitudesCambioFirestore
import com.itm.gestordeturnos.observarRegistrosAsistenciaFirestore
import com.itm.gestordeturnos.observarTurnosFirestore
import com.itm.gestordeturnos.obtenerAsignacionesFirestore
import com.itm.gestordeturnos.obtenerEmpleadosFirestore
import com.itm.gestordeturnos.obtenerNovedadesFirestore
import com.itm.gestordeturnos.obtenerIncumplimientosFirestore
import com.itm.gestordeturnos.obtenerSolicitudesCambioFirestore
import com.itm.gestordeturnos.obtenerRegistrosAsistenciaFirestore
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

    suspend fun guardarAsignacionesBatch(asignaciones: List<AsignacionTurno>): Result<Unit> {
        return guardarAsignacionesBatchFirestore(asignaciones)
    }

    // --- Asistencia y Marcaciones ---
    fun observarAsistencia(): Flow<List<RegistroAsistencia>> = callbackFlow {
        observarRegistrosAsistenciaFirestore { lista ->
            trySend(lista)
        }
        awaitClose { }
    }

    suspend fun obtenerAsistencia(): List<RegistroAsistencia> {
        return obtenerRegistrosAsistenciaFirestore()
    }

    suspend fun guardarAsistencia(registro: RegistroAsistencia): Result<Unit> {
        return guardarRegistroAsistenciaFirestore(registro)
    }

    // --- Novedades ---
    fun observarNovedades(): Flow<List<Novedad>> = callbackFlow {
        observarNovedadesFirestore { lista ->
            trySend(lista)
        }
        awaitClose { }
    }

    suspend fun obtenerNovedades(): List<Novedad> {
        return obtenerNovedadesFirestore()
    }

    suspend fun guardarNovedad(novedad: Novedad): Result<Unit> {
        return guardarNovedadFirestore(novedad)
    }

    suspend fun inicializarNovedadesSiEstaVacio(): Boolean {
        return try {
            val existentes = obtenerNovedadesFirestore()
            if (existentes.isEmpty()) {
                val iniciales = MockDataProviders.inicialNovedades()
                var algunGuardado = false
                for (n in iniciales) {
                    val res = guardarNovedadFirestore(n)
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

    // --- Solicitudes de Cambio de Turno ---
    fun observarSolicitudesCambio(): Flow<List<SolicitudCambioTurno>> = callbackFlow {
        observarSolicitudesCambioFirestore { lista ->
            trySend(lista)
        }
        awaitClose { }
    }

    suspend fun obtenerSolicitudesCambio(): List<SolicitudCambioTurno> {
        return obtenerSolicitudesCambioFirestore()
    }

    suspend fun guardarSolicitudCambio(solicitud: SolicitudCambioTurno): Result<Unit> {
        return guardarSolicitudCambioFirestore(solicitud)
    }

    // --- Incumplimientos ---
    fun observarIncumplimientos(): Flow<List<NotificacionIncumplimiento>> = callbackFlow {
        observarIncumplimientosFirestore { lista ->
            trySend(lista)
        }
        awaitClose { }
    }

    suspend fun obtenerIncumplimientos(): List<NotificacionIncumplimiento> {
        return obtenerIncumplimientosFirestore()
    }

    suspend fun guardarIncumplimiento(notificacion: NotificacionIncumplimiento): Result<Unit> {
        return guardarIncumplimientoFirestore(notificacion)
    }

    suspend fun inicializarIncumplimientosSiEstaVacio(): Boolean {
        return try {
            val existentes = obtenerIncumplimientosFirestore()
            if (existentes.isEmpty()) {
                val iniciales = MockDataProviders.inicialIncumplimientos()
                for (inc in iniciales) {
                    guardarIncumplimientoFirestore(inc)
                }
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }
}
