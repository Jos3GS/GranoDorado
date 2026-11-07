package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.service.AuthService
import com.itm.gestordeturnos.data.service.FirebaseAuthService
import com.itm.gestordeturnos.domain.usecase.ValidacionClaveResult
import com.itm.gestordeturnos.domain.usecase.ValidarContrasenaFirebaseAuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CambiarContrasenaState(
    val claveActual: String = "",
    val nuevaClave: String = "",
    val confirmacionClave: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

class CambiarContrasenaViewModel(
    private val authService: AuthService = FirebaseAuthService(),
    private val validadorClave: ValidarContrasenaFirebaseAuthUseCase = ValidarContrasenaFirebaseAuthUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CambiarContrasenaState())
    val uiState: StateFlow<CambiarContrasenaState> = _uiState.asStateFlow()

    fun onClaveActualChanged(valor: String) {
        _uiState.update { it.copy(claveActual = valor, mensajeError = null) }
    }

    fun onNuevaClaveChanged(valor: String) {
        _uiState.update { it.copy(nuevaClave = valor, mensajeError = null) }
    }

    fun onConfirmacionClaveChanged(valor: String) {
        _uiState.update { it.copy(confirmacionClave = valor, mensajeError = null) }
    }

    fun cambiarContrasena(onExito: (() -> Unit)? = null) {
        val estado = _uiState.value
        val validacion = validadorClave.validar(
            claveActual = estado.claveActual,
            nuevaClave = estado.nuevaClave,
            confirmacionClave = estado.confirmacionClave
        )

        if (validacion is ValidacionClaveResult.Invalida) {
            _uiState.update { it.copy(mensajeError = validacion.motivo) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null, mensajeExito = null) }

            val resultado = authService.cambiarContrasena(
                claveActual = estado.claveActual,
                nuevaClave = estado.nuevaClave
            )

            if (resultado.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        mensajeExito = "Tu contraseña ha sido actualizada con éxito.",
                        claveActual = "",
                        nuevaClave = "",
                        confirmacionClave = ""
                    )
                }
                onExito?.invoke()
            } else {
                val errorMsg = resultado.exceptionOrNull()?.message ?: "Error al cambiar la contraseña."
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = false,
                        mensajeError = errorMsg
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeError = null, mensajeExito = null) }
    }
}
