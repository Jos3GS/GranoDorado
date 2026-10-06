package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)

class LoginViewModel: ViewModel(){
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onEmailChanged(nuevoEmail: String) {
        _uiState.update { it.copy(email = nuevoEmail) }
    }

    fun onPasswordChanged(nuevoPassword: String) {
        _uiState.update { it.copy(password = nuevoPassword) }
    }

    fun iniciarSesion(){
        println("Intentando iniciar sesión con correo: ${_uiState.value.email}")
    }
}