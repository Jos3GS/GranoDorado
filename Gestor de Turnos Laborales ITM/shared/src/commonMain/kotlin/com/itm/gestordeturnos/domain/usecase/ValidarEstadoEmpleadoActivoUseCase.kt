package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract

sealed class ValidacionEstadoEmpleadoResult {
    data object Activo : ValidacionEstadoEmpleadoResult()
    data class Inactivo(val mensaje: String) : ValidacionEstadoEmpleadoResult()
}

/**
 * Caso de uso para la regla de negocio RN-11:
 * "Al inactivar un empleado se conserva todo su historial y se impide realizar acciones
 * operativas en el sistema (cambiar turno, reportar novedades, marcar asistencia, etc.)
 * mientras la cuenta permanezca inactiva."
 */
class ValidarEstadoEmpleadoActivoUseCase(
    private val empleadoRepository: EmpleadoRepositoryContract? = null
) {
    companion object {
        const val MENSAJE_CUENTA_INACTIVA_GENERICO =
            "Tu cuenta de colaborador se encuentra inactiva. No tienes autorización para realizar esta acción (RN-11)."
        const val MENSAJE_CUENTA_INACTIVA_CAMBIO_TURNO =
            "No puedes solicitar ni gestionar cambios de turno porque tu cuenta se encuentra inactiva (RN-11)."
        const val MENSAJE_CUENTA_INACTIVA_NOVEDAD =
            "No puedes reportar ni modificar novedades porque tu cuenta se encuentra inactiva (RN-11)."
        const val MENSAJE_CUENTA_INACTIVA_ASISTENCIA =
            "No puedes registrar marcas de asistencia porque tu cuenta se encuentra inactiva (RN-11)."
        const val MENSAJE_CUENTA_INACTIVA_CLAVE =
            "No puedes cambiar tu contraseña porque tu cuenta se encuentra inactiva (RN-11)."
    }

    /**
     * Valida si la cuenta del empleado está activa.
     */
    fun validar(empleado: Empleado?, accion: String = ""): ValidacionEstadoEmpleadoResult {
        if (empleado != null && empleado.estado != EstadoUsuario.ACTIVO) {
            val mensaje = when (accion.lowercase()) {
                "cambio_turno" -> MENSAJE_CUENTA_INACTIVA_CAMBIO_TURNO
                "novedad" -> MENSAJE_CUENTA_INACTIVA_NOVEDAD
                "asistencia" -> MENSAJE_CUENTA_INACTIVA_ASISTENCIA
                "clave" -> MENSAJE_CUENTA_INACTIVA_CLAVE
                else -> MENSAJE_CUENTA_INACTIVA_GENERICO
            }
            return ValidacionEstadoEmpleadoResult.Inactivo(mensaje)
        }
        return ValidacionEstadoEmpleadoResult.Activo
    }

    /**
     * Valida si el usuario en sesión está activo consultándolo en la lista de empleados proporcionada.
     */
    fun validarPorUsuario(
        usuario: UsuarioActual?,
        empleados: List<Empleado>,
        accion: String = ""
    ): ValidacionEstadoEmpleadoResult {
        if (usuario == null) return ValidacionEstadoEmpleadoResult.Activo
        val emp = empleados.find {
            (usuario.id.isNotBlank() && it.id == usuario.id) ||
            (usuario.correo.isNotBlank() && it.correo.equals(usuario.correo, ignoreCase = true))
        }
        return validar(emp, accion)
    }

    /**
     * Valida si el usuario está activo consultando el repositorio de empleados disponible.
     */
    fun validarPorIdOCorreo(
        id: String?,
        correo: String?,
        accion: String = ""
    ): ValidacionEstadoEmpleadoResult {
        val lista = empleadoRepository?.empleados?.value ?: emptyList()
        val emp = lista.find {
            (id != null && it.id == id) ||
            (correo != null && it.correo.equals(correo, ignoreCase = true))
        }
        return validar(emp, accion)
    }
}
