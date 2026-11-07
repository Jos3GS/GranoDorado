package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual

sealed interface ValidacionMarcaSupervisorResult {
    data object Valido : ValidacionMarcaSupervisorResult
    data class Invalido(val mensaje: String) : ValidacionMarcaSupervisorResult
}

/**
 * Caso de uso de dominio para las reglas de inmutabilidad, permisos y registro manual de marcas por supervisor (RN-28 y RN-29).
 *
 * Reglas de negocio:
 * 1. Solo administradores (General o de Sucursal) tienen permiso para registrar marcaciones manuales (RN-29).
 * 2. El Administrador de Sucursal solo puede registrar marcas en empleados pertenecientes a su sucursal (RN-29).
 * 3. Se debe registrar obligatoriamente la identificación y nombre del supervisor responsable (RN-29).
 * 4. El motivo o justificación de la marcación por supervisor es estrictamente obligatorio (RN-29).
 * 5. Las marcaciones registradas por el colaborador son inmutables: no pueden ser sobrescritas (RN-28).
 * 6. El supervisor únicamente puede registrar marcas faltantes (omisiones) (RN-28).
 * 7. Trazabilidad individual de origen por cada una de las marcas (entrada, descansos, salida) (RN-28 y RN-29).
 */
class ValidarMarcaSupervisorUseCase {

    /**
     * Valida permisos del usuario supervisor según su rol y sucursal (RN-29).
     */
    fun validarPermisoSupervisor(
        usuarioActivo: UsuarioActual?,
        sucursalRegistro: String
    ): ValidacionMarcaSupervisorResult {
        if (usuarioActivo == null) {
            return ValidacionMarcaSupervisorResult.Invalido("Debes iniciar sesión en el sistema para realizar esta acción.")
        }
        if (usuarioActivo.rol == RolUsuario.EMPLEADO) {
            return ValidacionMarcaSupervisorResult.Invalido("No tienes permisos para registrar marcaciones manuales de asistencia.")
        }
        if (usuarioActivo.rol == RolUsuario.ADMINISTRADOR_SUCURSAL) {
            val sucursalAdmin = usuarioActivo.sucursal.trim()
            if (sucursalAdmin.isNotBlank() && !sucursalAdmin.equals(sucursalRegistro.trim(), ignoreCase = true)) {
                return ValidacionMarcaSupervisorResult.Invalido("Solo tienes autorización para registrar marcaciones en tu sucursal asignada ($sucursalAdmin).")
            }
        }
        return ValidacionMarcaSupervisorResult.Valido
    }

    /**
     * Valida los datos y la inmutabilidad de la marcación manual a registrar (RN-28 y RN-29).
     */
    fun validar(
        registroActual: RegistroAsistencia?,
        tipoMarca: String,
        horaMarca: String,
        motivo: String,
        supervisorNombre: String = ""
    ): ValidacionMarcaSupervisorResult {
        if (motivo.isBlank()) {
            return ValidacionMarcaSupervisorResult.Invalido("El motivo o justificación de la marcación por supervisor es obligatorio.")
        }

        val horaLimpia = horaMarca.trim()
        if (!validarFormatoHora(horaLimpia)) {
            return ValidacionMarcaSupervisorResult.Invalido("La hora ingresada no tiene un formato válido (HH:mm).")
        }

        if (registroActual != null) {
            val tipoNormalizado = tipoMarca.trim().uppercase()
            if (registroActual.fueRegistradaPorEmpleado(tipoNormalizado)) {
                val nombreMarca = when (tipoNormalizado) {
                    "ENTRADA" -> "de entrada"
                    "SALIDA" -> "de salida"
                    "INICIO_DESCANSO" -> "de inicio de descanso"
                    "FIN_DESCANSO" -> "de fin de descanso"
                    else -> ""
                }
                return ValidacionMarcaSupervisorResult.Invalido(
                    "La marcación $nombreMarca ya fue registrada por el colaborador y es inmutable. No puede ser sobrescrita ni modificada."
                )
            }
        }

        return ValidacionMarcaSupervisorResult.Valido
    }

    private fun validarFormatoHora(hora: String): Boolean {
        val partes = hora.split(":")
        if (partes.size != 2) return false
        val h = partes[0].toIntOrNull() ?: return false
        val m = partes[1].toIntOrNull() ?: return false
        return h in 0..23 && m in 0..59
    }
}
