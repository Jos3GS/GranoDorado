package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.TurnoTemplate

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun initPlatformFirebase() {
    // En Android Firebase se inicializa automáticamente mediante Google Services ContentProvider
}

actual suspend fun iniciarSesionGooglePlataforma(): GoogleUserData? {
    // En Android, retorno para desarrollo o posterior flujo con Credential Manager
    return GoogleUserData(
        email = "empleado.google@granodorado.com",
        displayName = "Usuario Google"
    )
}

actual suspend fun enviarRecuperacionPlataforma(correo: String): Result<Unit> {
    return Result.success(Unit)
}

actual suspend fun registrarCuentaEmpleadoPlataforma(correo: String): Result<Unit> {
    // En Android / Entorno de desarrollo devuelve éxito o se conecta con Cloud Functions/Firebase
    return Result.success(Unit)
}

actual fun cerrarSesionPlataforma() {
    // No-op en Android desarrollo
}

actual suspend fun guardarEmpleadoFirestore(empleado: Empleado): Result<Unit> {
    return Result.success(Unit)
}

actual suspend fun obtenerEmpleadosFirestore(): List<Empleado> {
    return emptyList()
}

actual fun observarEmpleadosFirestore(onUpdate: (List<Empleado>) -> Unit) {
}

actual suspend fun guardarTurnoFirestore(turno: TurnoTemplate): Result<Unit> {
    return Result.success(Unit)
}

actual suspend fun obtenerTurnosFirestore(): List<TurnoTemplate> {
    return emptyList()
}

actual fun observarTurnosFirestore(onUpdate: (List<TurnoTemplate>) -> Unit) {
}
