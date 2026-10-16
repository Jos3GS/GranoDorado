package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.service.AuthResult
import com.itm.gestordeturnos.data.service.AuthService
import com.itm.gestordeturnos.data.service.FirebaseAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representa los diferentes estados de feedback para la recuperación de contraseña.
 */
sealed interface FeedbackRecuperacion {
    data class Exito(
        val titulo: String = "Correo enviado con éxito",
        val mensaje: String
    ) : FeedbackRecuperacion

    data class ErrorNoEncontrado(
        val titulo: String = "Correo no encontrado",
        val mensaje: String
    ) : FeedbackRecuperacion
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val mensajeError: String? = null,
    val feedbackRecuperacion: FeedbackRecuperacion? = null,
    val isRecuperandoClave: Boolean = false
)

class LoginViewModel(
    private val authService: AuthService = FirebaseAuthService()
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChanged(nuevoEmail: String) {
        _uiState.update { it.copy(email = nuevoEmail, mensajeError = null) }
    }

    fun onPasswordChanged(nuevoPassword: String) {
        _uiState.update { it.copy(password = nuevoPassword, mensajeError = null) }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    // Iniciar sesión con Correo y Contraseña
    fun iniciarSesion() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Por favor ingresa tu correo y contraseña.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }

            when (val resultado = authService.iniciarSesion(email, password)) {
                is AuthResult.Success -> {
                    SessionManager.usuarioActivo = resultado.usuario
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, mensajeError = null) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, mensajeError = resultado.mensaje) }
                }
            }
        }
    }

    // Iniciar sesión con Google
    fun loginConGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }

            when (val resultado = authService.loginConGoogle()) {
                is AuthResult.Success -> {
                    SessionManager.usuarioActivo = resultado.usuario
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, mensajeError = null) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, mensajeError = resultado.mensaje) }
                }
            }
        }
    }

    /**
     * Inicia el proceso de recuperación de contraseña en Firebase Auth.
     */
    fun recuperarContrasena() {
        val emailIngresado = _uiState.value.email.trim()
        if (emailIngresado.isBlank()) {
            _uiState.update {
                it.copy(
                    feedbackRecuperacion = FeedbackRecuperacion.ErrorNoEncontrado(
                        titulo = "Correo requerido",
                        mensaje = "Ingresa tu correo electrónico en el campo superior para enviarte las instrucciones."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRecuperandoClave = true) }

            val resultado = authService.recuperarContrasena(emailIngresado)
            val feedback = if (resultado.isSuccess) {
                FeedbackRecuperacion.Exito(
                    titulo = "Correo enviado con éxito",
                    mensaje = "Hemos enviado las instrucciones para restablecer tu contraseña a $emailIngresado. Revisa tu bandeja de entrada y la carpeta de spam."
                )
            } else {
                val errorMsg = resultado.exceptionOrNull()?.message ?: ""
                val detalle = when {
                    errorMsg.contains("invalid-email", ignoreCase = true) ->
                        "El formato del correo electrónico ingresado no es válido."
                    errorMsg.contains("user-not-found", ignoreCase = true) ->
                        "No se encontró ninguna cuenta asociada a '$emailIngresado'."
                    else ->
                        "No se encontró ninguna cuenta asociada a '$emailIngresado' o hubo un error al procesar la solicitud."
                }
                FeedbackRecuperacion.ErrorNoEncontrado(
                    titulo = "No se pudo enviar",
                    mensaje = detalle
                )
            }

            _uiState.update {
                it.copy(
                    isRecuperandoClave = false,
                    feedbackRecuperacion = feedback
                )
            }
        }
    }

    /**
     * Cierra la ventana emergente de feedback de recuperación.
     */
    fun cerrarFeedbackRecuperacion() {
        _uiState.update { it.copy(feedbackRecuperacion = null) }
    }
}
