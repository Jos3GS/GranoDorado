package com.itm.gestordeturnos.viewmodel

import com.itm.gestordeturnos.domain.model.UsuarioActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private val _usuarioActivoFlow = MutableStateFlow<UsuarioActual?>(null)
    val usuarioActivoFlow: StateFlow<UsuarioActual?> = _usuarioActivoFlow.asStateFlow()

    var usuarioActivo: UsuarioActual?
        get() = _usuarioActivoFlow.value
        set(value) {
            _usuarioActivoFlow.value = value
        }

    fun limpiarSesion() {
        _usuarioActivoFlow.value = null
    }
}
