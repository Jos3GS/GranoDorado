package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
import com.itm.gestordeturnos.iniciarSesionGooglePlataforma
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.first

sealed interface AuthResult {
    data class Success(val usuario: UsuarioActual) : AuthResult
    data class Error(val mensaje: String) : AuthResult
}

interface AuthService {
    suspend fun iniciarSesion(correo: String, clave: String): AuthResult
    suspend fun loginConGoogle(): AuthResult
    suspend fun recuperarContrasena(correo: String): Result<Unit>
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

            // Resolver datos de usuario y rol (buscando empleado registrado o asignando rol)
            val email = firebaseUser?.email ?: correo.trim()
            val usuarioActual = resolverUsuario(email, firebaseUser?.displayName)
            AuthResult.Success(usuarioActual)
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
                val usuario = resolverUsuario(googleUser.email, googleUser.displayName)
                AuthResult.Success(usuario)
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
        val authInstance = auth ?: return Result.failure(
            IllegalStateException("Firebase no está inicializado.")
        )
        return try {
            authInstance.sendPasswordResetEmail(correo.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cerrarSesion() {
        try {
            auth?.signOut()
        } catch (_: Exception) {
            // Ignorado en cierre de sesión
        }
    }

    override fun obtenerUsuarioActivo(): UsuarioActual? {
        val user = auth?.currentUser ?: return null
        val email = user.email ?: return null
        return resolverUsuarioSync(email, user.displayName)
    }

    private suspend fun resolverUsuario(correo: String, displayName: String?): UsuarioActual {
        val listaEmpleados = try {
            EmpleadoRepository.empleados.first()
        } catch (_: Exception) {
            emptyList()
        }

        val empleado = listaEmpleados.find { it.correo.equals(correo, ignoreCase = true) }
        return if (empleado != null) {
            val rol = when (empleado.cargo.name) {
                "ADMINISTRADOR_GENERAL" -> RolUsuario.ADMINISTRADOR_GENERAL
                "ADMINISTRADOR_SUCURSAL" -> RolUsuario.ADMINISTRADOR_SUCURSAL
                else -> RolUsuario.EMPLEADO
            }
            UsuarioActual(
                nombre = "${empleado.nombre} ${empleado.apellido}".trim(),
                iniciales = empleado.iniciales.ifEmpty { "EM" },
                cargo = empleado.cargo.displayName,
                rol = rol
            )
        } else {
            val nombre = displayName ?: correo.substringBefore("@").replace(".", " ")
            val iniciales = nombre.split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2)
                .joinToString("")
                .ifEmpty { "US" }

            UsuarioActual(
                nombre = nombre,
                iniciales = iniciales,
                cargo = "EMPLEADO",
                rol = RolUsuario.EMPLEADO
            )
        }
    }

    private fun resolverUsuarioSync(correo: String, displayName: String?): UsuarioActual {
        val listaEmpleados = EmpleadoRepository.empleados.value
        val empleado = listaEmpleados.find { it.correo.equals(correo, ignoreCase = true) }
        return if (empleado != null) {
            val rol = when (empleado.cargo.name) {
                "ADMINISTRADOR_GENERAL" -> RolUsuario.ADMINISTRADOR_GENERAL
                "ADMINISTRADOR_SUCURSAL" -> RolUsuario.ADMINISTRADOR_SUCURSAL
                else -> RolUsuario.EMPLEADO
            }
            UsuarioActual(
                nombre = "${empleado.nombre} ${empleado.apellido}".trim(),
                iniciales = empleado.iniciales.ifEmpty { "EM" },
                cargo = empleado.cargo.displayName,
                rol = rol
            )
        } else {
            val nombre = displayName ?: correo.substringBefore("@").replace(".", " ")
            UsuarioActual(
                nombre = nombre,
                iniciales = "US",
                cargo = "EMPLEADO",
                rol = RolUsuario.EMPLEADO
            )
        }
    }
}
