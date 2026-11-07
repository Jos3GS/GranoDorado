package com.itm.gestordeturnos.viewmodel

import com.itm.gestordeturnos.domain.model.UsuarioActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private val _usuarioActivoFlow = MutableStateFlow<UsuarioActual?>(null)
    val usuarioActivoFlow: StateFlow<UsuarioActual?> = _usuarioActivoFlow.asStateFlow()

    private val _mensajeSesionExpirada = MutableStateFlow<String?>(null)
    val mensajeSesionExpirada: StateFlow<String?> = _mensajeSesionExpirada.asStateFlow()

    var usuarioActivo: UsuarioActual?
        get() = _usuarioActivoFlow.value
        set(value) {
            _usuarioActivoFlow.value = value
            if (value != null) {
                _mensajeSesionExpirada.value = null
            }
        }

    fun notificarSesionExpirada(mensaje: String) {
        _mensajeSesionExpirada.value = mensaje
        _usuarioActivoFlow.value = null
    }

    fun consumirMensajeExpiracion(): String? {
        val msg = _mensajeSesionExpirada.value
        _mensajeSesionExpirada.value = null
        return msg
    }

    fun limpiarSesion() {
        _usuarioActivoFlow.value = null
        _mensajeSesionExpirada.value = null
    }
}
