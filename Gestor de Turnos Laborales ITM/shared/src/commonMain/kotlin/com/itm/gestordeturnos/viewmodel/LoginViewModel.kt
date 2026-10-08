package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
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
}