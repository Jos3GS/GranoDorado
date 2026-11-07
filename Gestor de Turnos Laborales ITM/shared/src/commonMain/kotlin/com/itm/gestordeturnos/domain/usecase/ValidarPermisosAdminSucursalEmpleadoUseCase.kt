package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.Cargo
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual

sealed class ValidacionAdminSucursalEmpleadoResult {
    data object Valida : ValidacionAdminSucursalEmpleadoResult()
    data class Invalida(val mensaje: String) : ValidacionAdminSucursalEmpleadoResult()
}

/**
 * Caso de uso que implementa las validaciones de negocio de la regla RN-10:
 * "El Administrador de Sucursal solo crea y edita empleados de su sucursal,
 * sin cambiarles la sucursal ni asignarles el rol de Administrador General."
 */
class ValidarPermisosAdminSucursalEmpleadoUseCase {

    /**
     * Valida si la operación de creación o edición de un empleado cumple con las restricciones
     * establecidas para un Administrador de Sucursal.
     */
    fun validar(
        usuarioSesion: UsuarioActual?,
        esNuevoEmpleado: Boolean,
        empleadoOriginal: Empleado?,
        empleadoActualizado: Empleado
    ): ValidacionAdminSucursalEmpleadoResult {
        // Si no hay sesión o el usuario es Administrador General, no aplican las restricciones de sucursal de RN-10
        if (usuarioSesion == null || usuarioSesion.rol != RolUsuario.ADMINISTRADOR_SUCURSAL) {
            return ValidacionAdminSucursalEmpleadoResult.Valida
        }

        val sucursalAdmin = usuarioSesion.sucursal.trim()

        // 1. No puede asignar el rol de Administrador General
        if (empleadoActualizado.cargo == Cargo.ADMINISTRADOR_GENERAL) {
            return ValidacionAdminSucursalEmpleadoResult.Invalida(
                "Un Administrador de Sucursal no puede asignar el rol de Administrador General."
            )
        }

        if (esNuevoEmpleado) {
            // 2. Al crear, solo puede registrar empleados para su propia sucursal
            if (sucursalAdmin.isNotBlank() && !empleadoActualizado.sucursal.trim().equals(sucursalAdmin, ignoreCase = true)) {
                return ValidacionAdminSucursalEmpleadoResult.Invalida(
                    "El Administrador de Sucursal solo puede crear empleados en su propia sucursal ($sucursalAdmin)."
                )
            }
        } else {
            // 3. Al editar, el empleado debe pertenecer a su sucursal
            val sucursalOriginal = empleadoOriginal?.sucursal?.trim() ?: ""
            if (sucursalAdmin.isNotBlank() && sucursalOriginal.isNotBlank() &&
                !sucursalOriginal.equals(sucursalAdmin, ignoreCase = true)
            ) {
                return ValidacionAdminSucursalEmpleadoResult.Invalida(
                    "El Administrador de Sucursal no puede editar empleados de otra sucursal ($sucursalOriginal). Solo gestiona su sucursal ($sucursalAdmin)."
                )
            }

            // 4. Al editar, no puede cambiarles la sucursal
            if (sucursalOriginal.isNotBlank() &&
                (!empleadoActualizado.sucursal.trim().equals(sucursalOriginal, ignoreCase = true) ||
                 !empleadoActualizado.sucursal.trim().equals(sucursalAdmin, ignoreCase = true))
            ) {
                return ValidacionAdminSucursalEmpleadoResult.Invalida(
                    "El Administrador de Sucursal no puede cambiar la sucursal de un empleado. El empleado debe pertenecer a $sucursalAdmin."
                )
            }
        }

        return ValidacionAdminSucursalEmpleadoResult.Valida
    }

    /**
     * Verifica si el usuario actual en sesión tiene permisos para abrir la edición de un empleado.
     */
    fun puedeEditarEmpleado(usuarioSesion: UsuarioActual?, empleado: Empleado): Boolean {
        if (usuarioSesion == null) return false
        if (usuarioSesion.rol == RolUsuario.ADMINISTRADOR_GENERAL) return true
        if (usuarioSesion.rol == RolUsuario.ADMINISTRADOR_SUCURSAL) {
            val sucursalAdmin = usuarioSesion.sucursal.trim()
            if (sucursalAdmin.isBlank()) return true
            return empleado.sucursal.trim().equals(sucursalAdmin, ignoreCase = true)
        }
        return false
    }
}
