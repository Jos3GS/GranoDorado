package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.Cargo
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario

/**
 * Resultado de la validación de permisos de edición de rol y estado según la RN-07.
 */
sealed interface ValidacionPermisosEdicionResult {
    data object Valida : ValidacionPermisosEdicionResult
    data class Invalida(val mensaje: String) : ValidacionPermisosEdicionResult
}

/**
 * Caso de uso de dominio para la Regla de Negocio RN-07:
 * "Ningún usuario puede modificar su propio rol ni estado, y el sistema no permite
 * quedarse sin al menos un usuario activo con rol de Administrador General."
 */
class ValidarModificacionRolEstadoUseCase {

    companion object {
        const val MENSAJE_PROPIO_ROL_O_ESTADO =
            "No puedes modificar tu propio rol ni estado de acceso por políticas de seguridad."
        const val MENSAJE_ULTIMO_ADMIN_GENERAL =
            "No se puede inactivar o cambiar el rol del único Administrador General activo del sistema."
    }

    /**
     * Valida si la modificación de un empleado cumple con la RN-07.
     *
     * @param usuarioActualId ID del usuario en sesión que ejecuta la acción (o su correo).
     * @param empleadoOriginal Datos originales del empleado antes de editar (null si es nuevo).
     * @param empleadoActualizado Datos que se pretenden guardar.
     * @param todosLosEmpleados Lista completa de empleados del sistema para verificar Admin General activo.
     */
    fun validar(
        usuarioActualId: String?,
        usuarioActualCorreo: String?,
        empleadoOriginal: Empleado?,
        empleadoActualizado: Empleado,
        todosLosEmpleados: List<Empleado>
    ): ValidacionPermisosEdicionResult {
        // En creación de nuevo empleado no aplica la restricción de propia edición
        if (empleadoOriginal == null) {
            return ValidacionPermisosEdicionResult.Valida
        }

        val esElMismoUsuario = (!usuarioActualId.isNullOrBlank() && empleadoOriginal.id == usuarioActualId) ||
            (!usuarioActualCorreo.isNullOrBlank() && empleadoOriginal.correo.equals(usuarioActualCorreo, ignoreCase = true))

        // 1. "Ningún usuario puede modificar su propio rol ni estado"
        if (esElMismoUsuario) {
            val cambioRol = empleadoOriginal.cargo != empleadoActualizado.cargo
            val cambioEstado = empleadoOriginal.estado != empleadoActualizado.estado

            if (cambioRol || cambioEstado) {
                return ValidacionPermisosEdicionResult.Invalida(MENSAJE_PROPIO_ROL_O_ESTADO)
            }
        }

        // 2. "el sistema no permite quedarse sin al menos un usuario activo con rol de Administrador General."
        val eraAdminGeneralActivo = empleadoOriginal.cargo == Cargo.ADMINISTRADOR_GENERAL &&
            empleadoOriginal.estado == EstadoUsuario.ACTIVO

        val dejaDeSerAdminGeneralActivo = empleadoActualizado.cargo != Cargo.ADMINISTRADOR_GENERAL ||
            empleadoActualizado.estado != EstadoUsuario.ACTIVO

        if (eraAdminGeneralActivo && dejaDeSerAdminGeneralActivo) {
            val otrosAdminGeneralActivos = todosLosEmpleados.count {
                it.id != empleadoOriginal.id &&
                    it.cargo == Cargo.ADMINISTRADOR_GENERAL &&
                    it.estado == EstadoUsuario.ACTIVO
            }
            if (otrosAdminGeneralActivos == 0) {
                return ValidacionPermisosEdicionResult.Invalida(MENSAJE_ULTIMO_ADMIN_GENERAL)
            }
        }

        return ValidacionPermisosEdicionResult.Valida
    }
}
