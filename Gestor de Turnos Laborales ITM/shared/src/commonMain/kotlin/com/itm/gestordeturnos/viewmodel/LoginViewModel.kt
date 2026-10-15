package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representa los diferentes estados de feedback para la recuperación de contraseña.
 * Diseñado para desacoplar la UI de la respuesta del servicio externo (API/Firebase/etc.).
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
    val feedbackRecuperacion: FeedbackRecuperacion? = null,
    val isRecuperandoClave: Boolean = false
)

class LoginViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChanged(nuevoEmail: String) {
        _uiState.update { it.copy(email = nuevoEmail) }
    }

    fun onPasswordChanged(nuevoPassword: String) {
        _uiState.update { it.copy(password = nuevoPassword) }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    // 1. Botón normal -> Entra como Administrador
    fun iniciarSesion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val admin = UsuarioActual(
                nombre = "Diego Marín",
                iniciales = "DM",
                cargo = "ADMINISTRADOR_GENERAL",
                rol = RolUsuario.ADMINISTRADOR_GENERAL
            )

            SessionManager.usuarioActivo = admin

            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    // 2. Botón de Google -> Entra como Empleado
    fun loginConGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val empleado = UsuarioActual(
                nombre = "José Manuel",
                iniciales = "JM",
                cargo = "EMPLEADO",
                rol = RolUsuario.EMPLEADO
            )

            SessionManager.usuarioActivo = empleado

            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }

    /**
     * Inicia el proceso de recuperación de contraseña.
     * Actualmente emite feedback de Éxito por defecto, pero está 100% preparado
     * para conmutar según la respuesta del servicio externo.
     *
     * @param simularError Útil para pruebas inmediatas del caso de correo no encontrado.
     */
    fun recuperarContrasena(simularError: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecuperandoClave = true) }

            val emailIngresado = _uiState.value.email.trim()

            // --- PREPARADO PARA SERVICIO EXTERNO ---
            // El día de mañana al conectar con backend/Firebase/Supabase:
            // val respuesta = authService.solicitarRecuperacion(emailIngresado)
            // val feedback = when (respuesta) {
            //     is AuthResult.Success -> FeedbackRecuperacion.Exito(...)
            //     is AuthResult.UserNotFound -> FeedbackRecuperacion.ErrorNoEncontrado(...)
            // }
            val feedback = if (!simularError) {
                FeedbackRecuperacion.Exito(
                    titulo = "Correo enviado con éxito",
                    mensaje = if (emailIngresado.isNotEmpty()) {
                        "Hemos enviado las instrucciones para restablecer tu contraseña a $emailIngresado. Revisa tu bandeja de entrada y la carpeta de spam."
                    } else {
                        "Hemos enviado las instrucciones para restablecer tu contraseña a tu correo registrado. Revisa tu bandeja de entrada y la carpeta de spam."
                    }
                )
            } else {
                FeedbackRecuperacion.ErrorNoEncontrado(
                    titulo = "Correo no encontrado",
                    mensaje = if (emailIngresado.isNotEmpty()) {
                        "No se encontró ninguna cuenta asociada a '$emailIngresado' en el sistema de Grano Dorado. Verifica los datos o consulta con administración."
                    } else {
                        "No se encontró ninguna cuenta registrada con este correo electrónico. Por favor verifica la información o contacta al administrador."
                    }
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
