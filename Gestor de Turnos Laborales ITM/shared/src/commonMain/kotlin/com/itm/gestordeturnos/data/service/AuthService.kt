package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.cerrarSesionPlataforma
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
import com.itm.gestordeturnos.enviarRecuperacionPlataforma
import com.itm.gestordeturnos.iniciarSesionGooglePlataforma
import com.itm.gestordeturnos.registrarCuentaEmpleadoPlataforma
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth

sealed interface AuthResult {
    data class Success(val usuario: UsuarioActual) : AuthResult
    data class Error(val mensaje: String) : AuthResult
}

interface AuthService {
    suspend fun iniciarSesion(correo: String, clave: String): AuthResult
    suspend fun loginConGoogle(): AuthResult
    suspend fun recuperarContrasena(correo: String): Result<Unit>
    suspend fun registrarCuentaEmpleado(correo: String): Result<Unit>
    suspend fun cerrarSesion()
    fun obtenerUsuarioActivo(): UsuarioActual?
}

class FirebaseAuthService : AuthService {

    private val auth: FirebaseAuth?
        get() = try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }

    override suspend fun iniciarSesion(correo: String, clave: String): AuthResult {
        val authInstance = auth ?: return AuthResult.Error(
            "Firebase no está inicializado. Por favor verifica tu configuración en firebase-config.js."
        )

        return try {
            val userCredential = authInstance.signInWithEmailAndPassword(correo.trim(), clave)
            val firebaseUser = userCredential.user
            val email = firebaseUser?.email ?: correo.trim()

            // Validar que el usuario esté registrado y activo en la base de datos
            val resultadoResolucion = resolverUsuario(email)
            if (resultadoResolucion.isFailure) {
                cerrarSesion()
                val detalleError = resultadoResolucion.exceptionOrNull()?.message 
                    ?: "Este correo no se encuentra registrado en el sistema."
                return AuthResult.Error(detalleError)
            }

            AuthResult.Success(resultadoResolucion.getOrThrow() )
        } catch (e: Exception) {
            val mensaje = when {
                e.message?.contains("user-not-found", ignoreCase = true) == true ||
                e.message?.contains("wrong-password", ignoreCase = true) == true ||
                e.message?.contains("invalid-credential", ignoreCase = true) == true ||
                e.message?.contains("invalid-login-credentials", ignoreCase = true) == true ->
                    "Los datos de acceso son errados."
                e.message?.contains("invalid-email", ignoreCase = true) == true ->
                    "El formato del correo electrónico no es válido."
                e.message?.contains("user-disabled", ignoreCase = true) == true ->
                    "Esta cuenta de usuario ha sido desactivada."
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Error de conexión. Verifica tu acceso a internet."
                else ->
                    e.message ?: "Error al iniciar sesión. Intenta nuevamente."
            }
            AuthResult.Error(mensaje)
        }
    }

    override suspend fun loginConGoogle(): AuthResult {
        return try {
            val googleUser = iniciarSesionGooglePlataforma()
            if (googleUser != null && googleUser.email.isNotBlank()) {
                val email = googleUser.email.trim()

                // CONDICIONAL DE SEGURIDAD ESTRICTA:
                // Si la cuenta no está registrada en el sistema o está inactiva, se deniega el acceso y se cierra la sesión
                val resultadoResolucion = resolverUsuario(email)
                if (resultadoResolucion.isFailure) {
                    cerrarSesion()
                    val detalleError = resultadoResolucion.exceptionOrNull()?.message 
                        ?: "La cuenta $email no se encuentra registrada en la plataforma. Contacta al administrador."
                    return AuthResult.Error(detalleError)
                }

                AuthResult.Success(resultadoResolucion.getOrThrow())
            } else {
                AuthResult.Error("Operación de inicio de sesión con Google cancelada.")
            }
        } catch (e: Exception) {
            val mensaje = when {
                e.message?.contains("popup-closed-by-user", ignoreCase = true) == true ->
                    "Ventana de inicio de sesión con Google cerrada."
                e.message?.contains("operation-not-allowed", ignoreCase = true) == true ||
                e.message?.contains("configuration-not-found", ignoreCase = true) == true ->
                    "El proveedor de Google no está habilitado en Firebase Console (Authentication -> Sign-in method)."
                e.message?.contains("unauthorized-domain", ignoreCase = true) == true ->
                    "Este dominio no está autorizado en Firebase Console (Authentication -> Settings -> Authorized domains)."
                else ->
                    e.message ?: "No se pudo completar el inicio de sesión con Google."
            }
            AuthResult.Error(mensaje)
        }
    }

    override suspend fun recuperarContrasena(correo: String): Result<Unit> {
        val authInstance = auth
        return try {
            if (authInstance != null) {
                authInstance.sendPasswordResetEmail(correo.trim())
                Result.success(Unit)
            } else {
                enviarRecuperacionPlataforma(correo.trim())
            }
        } catch (e: Exception) {
            try {
                enviarRecuperacionPlataforma(correo.trim())
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun registrarCuentaEmpleado(correo: String): Result<Unit> {
        return registrarCuentaEmpleadoPlataforma(correo.trim())
    }

    override suspend fun cerrarSesion() {
        try {
            auth?.signOut()
        } catch (_: Exception) {
            // Ignorado en cierre de sesión
        }
        try {
            cerrarSesionPlataforma()
        } catch (_: Exception) {
            // Ignorado
        }
    }

    override fun obtenerUsuarioActivo(): UsuarioActual? {
        val user = auth?.currentUser ?: return null
        val email = user.email ?: return null
        return resolverUsuarioSync(email)
    }

    private suspend fun resolverUsuario(correo: String): Result<UsuarioActual> {
        val empleado = EmpleadoRepository.buscarPorCorreo(correo)
            ?: return Result.failure(Exception("La cuenta $correo no se encuentra registrada en el sistema. Contacta al administrador."))

        if (empleado.estado != EstadoUsuario.ACTIVO) {
            return Result.failure(Exception("La cuenta de este colaborador se encuentra inactiva. Contacta al administrador."))
        }

        val rol = when (empleado.cargo.name) {
            "ADMINISTRADOR_GENERAL" -> RolUsuario.ADMINISTRADOR_GENERAL
            "ADMINISTRADOR_SUCURSAL" -> RolUsuario.ADMINISTRADOR_SUCURSAL
            else -> RolUsuario.EMPLEADO
        }
        val usuario = UsuarioActual(
            id = empleado.id,
            correo = empleado.correo,
            sucursal = empleado.sucursal,
            nombre = "${empleado.nombre} ${empleado.apellido}".trim().ifEmpty { empleado.nombre },
            iniciales = empleado.iniciales.ifEmpty { "EM" },
            cargo = empleado.cargo.displayName,
            rol = rol
        )
        return Result.success(usuario)
    }

    private fun resolverUsuarioSync(correo: String): UsuarioActual? {
        val listaEmpleados = EmpleadoRepository.empleados.value
        val empleado = listaEmpleados.find { it.correo.equals(correo, ignoreCase = true) } ?: return null
        if (empleado.estado != EstadoUsuario.ACTIVO) return null

        val rol = when (empleado.cargo.name) {
            "ADMINISTRADOR_GENERAL" -> RolUsuario.ADMINISTRADOR_GENERAL
            "ADMINISTRADOR_SUCURSAL" -> RolUsuario.ADMINISTRADOR_SUCURSAL
            else -> RolUsuario.EMPLEADO
        }
        return UsuarioActual(
            id = empleado.id,
            correo = empleado.correo,
            sucursal = empleado.sucursal,
            nombre = "${empleado.nombre} ${empleado.apellido}".trim().ifEmpty { empleado.nombre },
            iniciales = empleado.iniciales.ifEmpty { "EM" },
            cargo = empleado.cargo.displayName,
            rol = rol
        )
    }
}
