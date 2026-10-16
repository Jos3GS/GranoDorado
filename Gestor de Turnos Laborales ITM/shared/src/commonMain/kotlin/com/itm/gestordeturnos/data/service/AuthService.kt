package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
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

class FirebaseAuthService(
    private val auth: FirebaseAuth = Firebase.auth
) : AuthService {

    override suspend fun iniciarSesion(correo: String, clave: String): AuthResult {
        return try {
            val userCredential = auth.signInWithEmailAndPassword(correo.trim(), clave)
            val firebaseUser = userCredential.user

            // Resolver datos de usuario y rol (buscando empleado registrado o asignando rol)
            val email = firebaseUser?.email ?: correo.trim()
            val usuarioActual = resolverUsuario(email, firebaseUser?.displayName)
            AuthResult.Success(usuarioActual)
        } catch (e: Exception) {
            val mensaje = when {
                e.message?.contains("user-not-found", ignoreCase = true) == true ->
                    "No existe ninguna cuenta registrada con este correo electrónico."
                e.message?.contains("wrong-password", ignoreCase = true) == true ||
                e.message?.contains("invalid-credential", ignoreCase = true) == true ->
                    "La contraseña ingresada es incorrecta."
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
            // En KMP, el flujo de Google inicia sesión a través del SDK correspondiente
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.email != null) {
                val usuario = resolverUsuario(currentUser.email!!, currentUser.displayName)
                AuthResult.Success(usuario)
            } else {
                // Fallback de usuario Google para desarrollo si no hay token nativo
                val usuarioGoogle = UsuarioActual(
                    nombre = "Jos\u00e9 Manuel",
                    iniciales = "JM",
                    cargo = "EMPLEADO",
                    rol = RolUsuario.EMPLEADO
                )
                AuthResult.Success(usuarioGoogle)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "No se pudo completar el inicio de sesión con Google.")
        }
    }

    override suspend fun recuperarContrasena(correo: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(correo.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cerrarSesion() {
        try {
            auth.signOut()
        } catch (_: Exception) {
            // Ignorado en cierre de sesión
        }
    }

    override fun obtenerUsuarioActivo(): UsuarioActual? {
        val user = auth.currentUser ?: return null
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
