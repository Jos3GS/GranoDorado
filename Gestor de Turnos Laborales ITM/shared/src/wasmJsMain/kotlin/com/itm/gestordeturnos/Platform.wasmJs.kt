package com.itm.gestordeturnos

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun initPlatformFirebase() {
}

actual suspend fun iniciarSesionGooglePlataforma(): GoogleUserData? = null

actual suspend fun enviarRecuperacionPlataforma(correo: String): Result<Unit> = Result.success(Unit)

actual suspend fun registrarCuentaEmpleadoPlataforma(correo: String): Result<Unit> = Result.success(Unit)
