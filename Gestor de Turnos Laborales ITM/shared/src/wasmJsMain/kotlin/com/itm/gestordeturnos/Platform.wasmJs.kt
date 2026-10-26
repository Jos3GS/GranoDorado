package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.model.TurnoTemplate

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun initPlatformFirebase() {
}

actual suspend fun iniciarSesionGooglePlataforma(): GoogleUserData? = null

actual suspend fun enviarRecuperacionPlataforma(correo: String): Result<Unit> = Result.success(Unit)

actual suspend fun registrarCuentaEmpleadoPlataforma(correo: String): Result<Unit> = Result.success(Unit)

actual fun cerrarSesionPlataforma() {
}

actual suspend fun guardarEmpleadoFirestore(empleado: Empleado): Result<Unit> = Result.success(Unit)

actual suspend fun obtenerEmpleadosFirestore(): List<Empleado> = emptyList()

actual fun observarEmpleadosFirestore(onUpdate: (List<Empleado>) -> Unit) {
}

actual suspend fun guardarTurnoFirestore(turno: TurnoTemplate): Result<Unit> = Result.success(Unit)

actual suspend fun obtenerTurnosFirestore(): List<TurnoTemplate> = emptyList()

actual fun observarTurnosFirestore(onUpdate: (List<TurnoTemplate>) -> Unit) {
}

actual suspend fun guardarAsignacionFirestore(asignacion: AsignacionTurno): Result<Unit> = Result.success(Unit)

actual suspend fun guardarAsignacionesBatchFirestore(asignaciones: List<AsignacionTurno>): Result<Unit> = Result.success(Unit)

actual suspend fun obtenerAsignacionesFirestore(): List<AsignacionTurno> = emptyList()

actual fun observarAsignacionesFirestore(onUpdate: (List<AsignacionTurno>) -> Unit) {
}

actual suspend fun guardarRegistroAsistenciaFirestore(registro: RegistroAsistencia): Result<Unit> = Result.success(Unit)

actual suspend fun obtenerRegistrosAsistenciaFirestore(): List<RegistroAsistencia> = emptyList()

actual fun observarRegistrosAsistenciaFirestore(onUpdate: (List<RegistroAsistencia>) -> Unit) {
}

actual suspend fun guardarNovedadFirestore(novedad: Novedad): Result<Unit> = Result.success(Unit)

actual suspend fun obtenerNovedadesFirestore(): List<Novedad> = emptyList()

actual fun observarNovedadesFirestore(onUpdate: (List<Novedad>) -> Unit) {
}

actual suspend fun guardarSolicitudCambioFirestore(solicitud: SolicitudCambioTurno): Result<Unit> = Result.success(Unit)

actual suspend fun obtenerSolicitudesCambioFirestore(): List<SolicitudCambioTurno> = emptyList()

actual fun observarSolicitudesCambioFirestore(onUpdate: (List<SolicitudCambioTurno>) -> Unit) {
}
