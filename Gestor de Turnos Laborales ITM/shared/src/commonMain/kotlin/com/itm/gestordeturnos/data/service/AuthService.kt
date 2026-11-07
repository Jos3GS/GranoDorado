package com.itm.gestordeturnos.data.service

import com.itm.gestordeturnos.actualizarContrasenaPlataforma
import com.itm.gestordeturnos.cerrarSesionPlataforma
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.usecase.ControlIntentosLoginUseCase
import com.itm.gestordeturnos.domain.usecase.EvaluacionIntentoLogin
import com.itm.gestordeturnos.enviarRecuperacionPlataforma
import com.itm.gestordeturnos.iniciarSesionGooglePlataforma
import com.itm.gestordeturnos.registrarCuentaEmpleadoPlataforma
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth

sealed interface AuthResult {
    data class Success(val usuario: UsuarioActual) : AuthResult
    data class Error(
        val mensaje: String,
        val intentosFallidos: Int = 0,
        val bloqueada: Boolean = false,
        val minutosRestantesBloqueo: Int = 0
    ) : AuthResult
}

interface AuthService {
    suspend fun iniciarSesion(correo: String, clave: String): AuthResult
    suspend fun loginConGoogle(): AuthResult
    suspend fun recuperarContrasena(correo: String): Result<Unit>
    suspend fun cambiarContrasena(claveActual: String, nuevaClave: String): Result<Unit>
    suspend fun registrarCuentaEmpleado(correo: String): Result<Unit>
    suspend fun cerrarSesion()
    fun obtenerUsuarioActivo(): UsuarioActual?
}

class FirebaseAuthService(
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val controlIntentosUseCase: ControlIntentosLoginUseCase = ControlIntentosLoginUseCase()
) : AuthService {

    private val auth: FirebaseAuth?
        get() = try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }

    override suspend fun iniciarSesion(correo: String, clave: String): AuthResult {
        val correoNormalizado = correo.trim()

        // 1. Sincronizar estado de intentos/bloqueo si el empleado ya está en el repositorio
        val empleadoConocido = empleadoRepository.buscarPorCorreo(correoNormalizado)
        if (empleadoConocido != null) {
            controlIntentosUseCase.sincronizarDesdeEmpleado(
                correo = correoNormalizado,
                intentosPersistidos = empleadoConocido.intentosFallidosLogin,
                bloqueadoHastaPersistido = empleadoConocido.bloqueadoHastaEpochMillis
            )
        }

        // 2. Verificar si la cuenta está bloqueada temporalmente (RN-02: 15 minutos tras 5 intentos)
        when (val preEvaluacion = controlIntentosUseCase.verificarAntesDeIntentar(correoNormalizado)) {
            is EvaluacionIntentoLogin.CuentaBloqueada -> {
                return AuthResult.Error(
                    mensaje = preEvaluacion.mensaje,
                    intentosFallidos = ControlIntentosLoginUseCase.MAX_INTENTOS_FALLIDOS,
                    bloqueada = true,
                    minutosRestantesBloqueo = preEvaluacion.minutosRestantes
                )
            }
            else -> Unit
        }

        val authInstance = auth ?: return AuthResult.Error(
            "Firebase no está inicializado. Por favor verifica tu configuración en firebase-config.js."
        )

        return try {
            val userCredential = authInstance.signInWithEmailAndPassword(correoNormalizado, clave)
            val firebaseUser = userCredential.user
            val email = firebaseUser?.email ?: correoNormalizado

            // Validar que el usuario esté registrado y activo en la base de datos (RN-01 y RN-02)
            val resultadoResolucion = resolverUsuario(email)
            if (resultadoResolucion.isFailure) {
                cerrarSesion()
                return registrarFalloCredencialesOInactiva(email)
            }

            // Inicio de sesión exitoso -> reiniciar contador de intentos fallidos (RN-02)
            reiniciarIntentosDeCuenta(email)

            AuthResult.Success(resultadoResolucion.getOrThrow())
        } catch (e: Exception) {
            val esFalloCredencialesOCuenta =
                e.message?.contains("user-not-found", ignoreCase = true) == true ||
                e.message?.contains("wrong-password", ignoreCase = true) == true ||
                e.message?.contains("invalid-credential", ignoreCase = true) == true ||
                e.message?.contains("invalid-login-credentials", ignoreCase = true) == true ||
                e.message?.contains("user-disabled", ignoreCase = true) == true

            if (esFalloCredencialesOCuenta) {
                return registrarFalloCredencialesOInactiva(correoNormalizado)
            }

            val mensaje = when {
                e.message?.contains("invalid-email", ignoreCase = true) == true ->
                    "El formato del correo electrónico no es válido."
                e.message?.contains("too-many-requests", ignoreCase = true) == true ->
                    ControlIntentosLoginUseCase.construirMensajeBloqueo(
                        ControlIntentosLoginUseCase.DURACION_BLOQUEO_MINUTOS
                    )
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Error de conexión. Verifica tu acceso a internet."
                else ->
                    ControlIntentosLoginUseCase.MENSAJE_CREDENCIALES_O_INACTIVA
            }
            AuthResult.Error(mensaje)
        }
    }

    override suspend fun loginConGoogle(): AuthResult {
        return try {
            val googleUser = iniciarSesionGooglePlataforma()
            if (googleUser != null && googleUser.email.isNotBlank()) {
                val email = googleUser.email.trim()

                val empleadoConocido = empleadoRepository.buscarPorCorreo(email)
                if (empleadoConocido != null) {
                    controlIntentosUseCase.sincronizarDesdeEmpleado(
                        correo = email,
                        intentosPersistidos = empleadoConocido.intentosFallidosLogin,
                        bloqueadoHastaPersistido = empleadoConocido.bloqueadoHastaEpochMillis
                    )
                }

                when (val preEvaluacion = controlIntentosUseCase.verificarAntesDeIntentar(email)) {
                    is EvaluacionIntentoLogin.CuentaBloqueada -> {
                        cerrarSesion()
                        return AuthResult.Error(
                            mensaje = preEvaluacion.mensaje,
                            intentosFallidos = ControlIntentosLoginUseCase.MAX_INTENTOS_FALLIDOS,
                            bloqueada = true,
                            minutosRestantesBloqueo = preEvaluacion.minutosRestantes
                        )
                    }
                    else -> Unit
                }

                // CONDICIONAL DE SEGURIDAD ESTRICTA (RN-01 / RN-02):
                // Si la cuenta no está registrada en el sistema o está inactiva, se deniega el acceso con el mensaje unificado
                val resultadoResolucion = resolverUsuario(email)
                if (resultadoResolucion.isFailure) {
                    cerrarSesion()
                    return AuthResult.Error(ControlIntentosLoginUseCase.MENSAJE_CREDENCIALES_O_INACTIVA)
                }

                reiniciarIntentosDeCuenta(email)
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
        val correoNormalizado = correo.trim()
        val authInstance = auth
        val resultado = try {
            if (authInstance != null) {
                authInstance.sendPasswordResetEmail(correoNormalizado)
                Result.success(Unit)
            } else {
                enviarRecuperacionPlataforma(correoNormalizado)
            }
        } catch (e: Exception) {
            try {
                enviarRecuperacionPlataforma(correoNormalizado)
            } catch (_: Exception) {
                Result.failure(e)
            }
        }

        // RN-02: El contador se reinicia con un cambio/recuperación de contraseña
        if (resultado.isSuccess) {
            reiniciarIntentosDeCuenta(correoNormalizado)
        }
        return resultado
    }

    override suspend fun cambiarContrasena(claveActual: String, nuevaClave: String): Result<Unit> {
        val user = auth?.currentUser
        val email = user?.email
        val authInstance = auth

        val resultado = try {
            if (authInstance != null && user != null && email != null) {
                // Reautenticar con la contraseña actual
                authInstance.signInWithEmailAndPassword(email, claveActual)
                user.updatePassword(nuevaClave)
                Result.success(Unit)
            } else {
                actualizarContrasenaPlataforma(claveActual, nuevaClave)
            }
        } catch (e: Exception) {
            val codeOrMessage = e.message ?: ""
            val mensajeLimpio = when {
                codeOrMessage.contains("wrong-password", ignoreCase = true) ||
                codeOrMessage.contains("invalid-credential", ignoreCase = true) ->
                    "La contraseña actual es incorrecta."
                codeOrMessage.contains("weak-password", ignoreCase = true) ->
                    "La nueva contraseña debe tener al menos 6 caracteres (mínimo exigido por Firebase)."
                codeOrMessage.contains("requires-recent-login", ignoreCase = true) ->
                    "Por seguridad, debes cerrar sesión e iniciarla nuevamente antes de cambiar la contraseña."
                else ->
                    codeOrMessage.ifBlank { "Error al actualizar la contraseña." }
            }
            Result.failure(Exception(mensajeLimpio))
        }

        // RN-02 / RN-04: Al cambiar contraseña exitosamente, reiniciar contador de intentos fallidos
        if (resultado.isSuccess && !email.isNullOrBlank()) {
            reiniciarIntentosDeCuenta(email)
        }
        return resultado
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

    private suspend fun registrarFalloCredencialesOInactiva(correo: String): AuthResult.Error {
        val evaluacion = controlIntentosUseCase.registrarIntentoFallido(correo)
        val estadoActual = controlIntentosUseCase.obtenerEstado(correo)

        val empleado = empleadoRepository.buscarPorCorreo(correo)
        if (empleado != null) {
            empleadoRepository.guardarEmpleado(
                empleado = empleado.copy(
                    intentosFallidosLogin = estadoActual.intentosFallidos,
                    bloqueadoHastaEpochMillis = estadoActual.bloqueadoHastaEpochMillis
                ),
                esNuevo = false
            )
        }

        return when (evaluacion) {
            is EvaluacionIntentoLogin.CuentaBloqueada -> AuthResult.Error(
                mensaje = evaluacion.mensaje,
                intentosFallidos = ControlIntentosLoginUseCase.MAX_INTENTOS_FALLIDOS,
                bloqueada = true,
                minutosRestantesBloqueo = evaluacion.minutosRestantes
            )
            is EvaluacionIntentoLogin.FallidoRegistrado -> AuthResult.Error(
                mensaje = evaluacion.mensaje,
                intentosFallidos = evaluacion.intentosAcumulados,
                bloqueada = false,
                minutosRestantesBloqueo = 0
            )
            is EvaluacionIntentoLogin.Permitido -> AuthResult.Error(
                mensaje = ControlIntentosLoginUseCase.MENSAJE_CREDENCIALES_O_INACTIVA
            )
        }
    }

    private suspend fun reiniciarIntentosDeCuenta(correo: String) {
        controlIntentosUseCase.reiniciarContador(correo)
        val empleado = empleadoRepository.buscarPorCorreo(correo)
        if (empleado != null && (empleado.intentosFallidosLogin != 0 || empleado.bloqueadoHastaEpochMillis != 0L)) {
            empleadoRepository.guardarEmpleado(
                empleado = empleado.copy(
                    intentosFallidosLogin = 0,
                    bloqueadoHastaEpochMillis = 0L
                ),
                esNuevo = false
            )
        }
    }

    private suspend fun resolverUsuario(correo: String): Result<UsuarioActual> {
        val empleado = empleadoRepository.buscarPorCorreo(correo)
            ?: return Result.failure(Exception(ControlIntentosLoginUseCase.MENSAJE_CREDENCIALES_O_INACTIVA))

        if (empleado.estado != EstadoUsuario.ACTIVO) {
            return Result.failure(Exception(ControlIntentosLoginUseCase.MENSAJE_CREDENCIALES_O_INACTIVA))
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
        val listaEmpleados = empleadoRepository.empleados.value
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
