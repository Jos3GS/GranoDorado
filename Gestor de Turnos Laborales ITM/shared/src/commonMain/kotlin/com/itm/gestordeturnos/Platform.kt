package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.Empleado

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
