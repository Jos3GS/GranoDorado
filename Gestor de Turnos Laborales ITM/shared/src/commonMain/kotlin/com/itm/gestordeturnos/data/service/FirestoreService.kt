package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.model.NotificacionIncumplimiento
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import com.itm.gestordeturnos.initPlatformFirebase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

object FirestoreService {

    private val db: FirebaseFirestore?
        get() = try {
            initPlatformFirebase()
            Firebase.firestore
        } catch (_: Throwable) {
            null
        }

    // --- Empleados ---
    fun observarEmpleados(): Flow<List<Empleado>> {
        val firestore = db ?: return emptyFlow()
        return firestore.collection("empleados").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val emp = doc.data<Empleado>()
                        if (emp.id.isBlank()) emp.copy(id = doc.id) else emp
                    }.getOrNull()
                }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun obtenerEmpleados(): List<Empleado> {
        val firestore = db ?: return emptyList()
        return try {
            firestore.collection("empleados").get().documents.mapNotNull { doc ->
                runCatching {
                    val emp = doc.data<Empleado>()
                    if (emp.id.isBlank()) emp.copy(id = doc.id) else emp
                }.getOrNull()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun guardarEmpleado(empleado: Empleado): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firebase Firestore no está inicializado"))
        return try {
            val id = empleado.id.ifBlank { "emp_${Random.nextInt(10000, 99999)}" }
            val normalizado = empleado.copy(
                id = id,
                sucursalId = empleado.sucursalId.ifBlank { empleado.sucursal }
            )
            firestore.collection("empleados").document(id).set(normalizado)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun inicializarSiEstaVacio(): Boolean {
        return try {
            val existentes = obtenerEmpleados()
            if (existentes.isEmpty()) {
                val iniciales = MockDataProviders.inicialEmpleados()
                var algunGuardado = false
                for (emp in iniciales) {
                    val res = guardarEmpleado(emp)
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
    fun observarTurnos(): Flow<List<TurnoTemplate>> {
        val firestore = db ?: return emptyFlow()
        return firestore.collection("turnos").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val turno = doc.data<TurnoTemplate>()
                        if (turno.id.isBlank()) turno.copy(id = doc.id) else turno
                    }.getOrNull()
                }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun obtenerTurnos(): List<TurnoTemplate> {
        val firestore = db ?: return emptyList()
        return try {
            firestore.collection("turnos").get().documents.mapNotNull { doc ->
                runCatching {
                    val turno = doc.data<TurnoTemplate>()
                    if (turno.id.isBlank()) turno.copy(id = doc.id) else turno
                }.getOrNull()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun guardarTurno(turno: TurnoTemplate): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firebase Firestore no está inicializado"))
        return try {
            val id = turno.id.ifBlank { "t_${Random.nextInt(1000, 9999)}" }
            val normalizado = turno.copy(id = id)
            firestore.collection("turnos").document(id).set(normalizado)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun inicializarTurnosSiEstaVacio(): Boolean {
        return try {
            val existentes = obtenerTurnos()
            if (existentes.isEmpty()) {
                val iniciales = MockDataProviders.inicialTurnosCatalogo()
                var algunGuardado = false
                for (t in iniciales) {
                    val res = guardarTurno(t)
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
            val res = guardarTurno(t)
            if (res.isFailure) return res
        }
        return Result.success(Unit)
    }

    // --- Asignaciones Diarias Individuales ---
    fun observarAsignaciones(): Flow<List<AsignacionTurno>> {
        val firestore = db ?: return emptyFlow()
        return firestore.collection("asignaciones").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val asig = doc.data<AsignacionTurno>()
                        if (asig.id.isBlank()) asig.copy(id = doc.id) else asig
                    }.getOrNull()
                }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun obtenerAsignaciones(): List<AsignacionTurno> {
        val firestore = db ?: return emptyList()
        return try {
            firestore.collection("asignaciones").get().documents.mapNotNull { doc ->
                runCatching {
                    val asig = doc.data<AsignacionTurno>()
                    if (asig.id.isBlank()) asig.copy(id = doc.id) else asig
                }.getOrNull()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun guardarAsignacion(asignacion: AsignacionTurno): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firebase Firestore no está inicializado"))
        return try {
            val id = asignacion.id.ifBlank { "${asignacion.empleadoId}_${asignacion.fechaIso}" }
            val normalizada = asignacion.copy(id = id)
            firestore.collection("asignaciones").document(id).set(normalizada)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun guardarAsignacionesBatch(asignaciones: List<AsignacionTurno>): Result<Unit> {
        if (asignaciones.isEmpty()) return Result.success(Unit)
        val firestore = db ?: return Result.failure(IllegalStateException("Firebase Firestore no está inicializado"))
        return try {
            val batch = firestore.batch()
            val col = firestore.collection("asignaciones")
            for (asig in asignaciones) {
                val id = asig.id.ifBlank { "${asig.empleadoId}_${asig.fechaIso}" }
                val normalizada = asig.copy(id = id)
                batch.set(col.document(id), normalizada)
            }
            batch.commit()
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // --- Asistencia y Marcaciones ---
    fun observarAsistencia(): Flow<List<RegistroAsistencia>> {
        val firestore = db ?: return emptyFlow()
        return firestore.collection("asistencia").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val reg = doc.data<RegistroAsistencia>()
                        if (reg.id.isBlank()) reg.copy(id = doc.id) else reg
                    }.getOrNull()
                }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun obtenerAsistencia(): List<RegistroAsistencia> {
        val firestore = db ?: return emptyList()
        return try {
            firestore.collection("asistencia").get().documents.mapNotNull { doc ->
                runCatching {
                    val reg = doc.data<RegistroAsistencia>()
                    if (reg.id.isBlank()) reg.copy(id = doc.id) else reg
                }.getOrNull()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun guardarAsistencia(registro: RegistroAsistencia): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firebase Firestore no está inicializado"))
        return try {
            val id = registro.id.ifBlank { "${registro.empleadoId}_${registro.fechaIso}" }
            val normalizado = registro.copy(id = id)
            firestore.collection("asistencia").document(id).set(normalizado)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // --- Novedades ---
    fun observarNovedades(): Flow<List<Novedad>> {
        val firestore = db ?: return emptyFlow()
        return firestore.collection("novedades").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val nov = doc.data<Novedad>()
                        if (nov.id.isBlank()) nov.copy(id = doc.id) else nov
                    }.getOrNull()
                }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun obtenerNovedades(): List<Novedad> {
        val firestore = db ?: return emptyList()
        return try {
            firestore.collection("novedades").get().documents.mapNotNull { doc ->
                runCatching {
                    val nov = doc.data<Novedad>()
                    if (nov.id.isBlank()) nov.copy(id = doc.id) else nov
                }.getOrNull()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun guardarNovedad(novedad: Novedad): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firebase Firestore no está inicializado"))
        return try {
            val id = novedad.id.ifBlank { "nov_${Random.nextInt(100000, 999999)}" }
            val normalizada = novedad.copy(id = id)
            firestore.collection("novedades").document(id).set(normalizada)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun inicializarNovedadesSiEstaVacio(): Boolean {
        return try {
            val existentes = obtenerNovedades()
            if (existentes.isEmpty()) {
                val iniciales = MockDataProviders.inicialNovedades()
                var algunGuardado = false
                for (n in iniciales) {
                    val res = guardarNovedad(n)
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
    fun observarSolicitudesCambio(): Flow<List<SolicitudCambioTurno>> {
        val firestore = db ?: return emptyFlow()
        return firestore.collection("solicitudes_cambio").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val sol = doc.data<SolicitudCambioTurno>()
                        if (sol.id.isBlank()) sol.copy(id = doc.id) else sol
                    }.getOrNull()
                }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun obtenerSolicitudesCambio(): List<SolicitudCambioTurno> {
        val firestore = db ?: return emptyList()
        return try {
            firestore.collection("solicitudes_cambio").get().documents.mapNotNull { doc ->
                runCatching {
                    val sol = doc.data<SolicitudCambioTurno>()
                    if (sol.id.isBlank()) sol.copy(id = doc.id) else sol
                }.getOrNull()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun guardarSolicitudCambio(solicitud: SolicitudCambioTurno): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firebase Firestore no está inicializado"))
        return try {
            val id = solicitud.id.ifBlank { "sol_${Random.nextInt(100000, 999999)}" }
            val numSol = solicitud.numeroSolicitud.ifBlank { "SOL-${Random.nextInt(1000, 9999)}" }
            val normalizada = solicitud.copy(id = id, numeroSolicitud = numSol)
            firestore.collection("solicitudes_cambio").document(id).set(normalizada)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    // --- Incumplimientos ---
    fun observarIncumplimientos(): Flow<List<NotificacionIncumplimiento>> {
        val firestore = db ?: return emptyFlow()
        return firestore.collection("incumplimientos").snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val inc = doc.data<NotificacionIncumplimiento>()
                        if (inc.id.isBlank()) inc.copy(id = doc.id) else inc
                    }.getOrNull()
                }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun obtenerIncumplimientos(): List<NotificacionIncumplimiento> {
        val firestore = db ?: return emptyList()
        return try {
            firestore.collection("incumplimientos").get().documents.mapNotNull { doc ->
                runCatching {
                    val inc = doc.data<NotificacionIncumplimiento>()
                    if (inc.id.isBlank()) inc.copy(id = doc.id) else inc
                }.getOrNull()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun guardarIncumplimiento(notificacion: NotificacionIncumplimiento): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firebase Firestore no está inicializado"))
        return try {
            val id = notificacion.id.ifBlank { "${notificacion.empleadoId}_${notificacion.fechaIso}_${notificacion.tipo}" }
            val normalizada = notificacion.copy(id = id)
            firestore.collection("incumplimientos").document(id).set(normalizada)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun inicializarIncumplimientosSiEstaVacio(): Boolean {
        return try {
            val existentes = obtenerIncumplimientos()
            if (existentes.isEmpty()) {
                val iniciales = MockDataProviders.inicialIncumplimientos()
                for (inc in iniciales) {
                    guardarIncumplimiento(inc)
                }
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }
}
