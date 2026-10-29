package com.itm.gestordeturnos

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
