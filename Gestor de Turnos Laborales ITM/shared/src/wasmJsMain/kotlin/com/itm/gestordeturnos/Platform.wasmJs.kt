package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.Empleado
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
