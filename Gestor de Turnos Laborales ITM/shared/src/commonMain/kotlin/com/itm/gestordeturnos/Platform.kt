package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.model.TurnoTemplate

interface Platform {
    val name: String
}

data class GoogleUserData(
    val email: String,
    val displayName: String?,
    val idToken: String? = null
)

expect fun getPlatform(): Platform

expect fun initPlatformFirebase()

expect suspend fun iniciarSesionGooglePlataforma(): GoogleUserData?

expect suspend fun enviarRecuperacionPlataforma(correo: String): Result<Unit>

expect suspend fun registrarCuentaEmpleadoPlataforma(correo: String): Result<Unit>

expect fun cerrarSesionPlataforma()

expect suspend fun guardarEmpleadoFirestore(empleado: Empleado): Result<Unit>

expect suspend fun obtenerEmpleadosFirestore(): List<Empleado>

expect fun observarEmpleadosFirestore(onUpdate: (List<Empleado>) -> Unit)

expect suspend fun guardarTurnoFirestore(turno: TurnoTemplate): Result<Unit>

expect suspend fun obtenerTurnosFirestore(): List<TurnoTemplate>

expect fun observarTurnosFirestore(onUpdate: (List<TurnoTemplate>) -> Unit)

expect suspend fun guardarAsignacionFirestore(asignacion: AsignacionTurno): Result<Unit>

expect suspend fun guardarAsignacionesBatchFirestore(asignaciones: List<AsignacionTurno>): Result<Unit>

expect suspend fun obtenerAsignacionesFirestore(): List<AsignacionTurno>

expect fun observarAsignacionesFirestore(onUpdate: (List<AsignacionTurno>) -> Unit)

expect suspend fun guardarRegistroAsistenciaFirestore(registro: RegistroAsistencia): Result<Unit>

expect suspend fun obtenerRegistrosAsistenciaFirestore(): List<RegistroAsistencia>

expect fun observarRegistrosAsistenciaFirestore(onUpdate: (List<RegistroAsistencia>) -> Unit)
