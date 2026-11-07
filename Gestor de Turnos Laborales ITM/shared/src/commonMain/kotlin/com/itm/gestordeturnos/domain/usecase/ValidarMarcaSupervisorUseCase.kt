package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.RegistroAsistencia

sealed interface ValidacionMarcaSupervisorResult {
    data object Valido : ValidacionMarcaSupervisorResult
    data class Invalido(val mensaje: String) : ValidacionMarcaSupervisorResult
}

/**
 * Caso de uso de dominio para las reglas de inmutabilidad y registro manual de marcas por supervisor (RN-28).
 *
 * Reglas de negocio:
 * 1. Las marcaciones registradas por el colaborador son estrictamente inmutables:
 *    no pueden ser editadas ni eliminadas por el empleado, ni sobrescritas por el supervisor.
 * 2. El supervisor únicamente puede registrar marcas faltantes (omisiones).
 * 3. El motivo o justificación de la marcación por supervisor es obligatorio.
 * 4. La hora ingresada debe tener formato válido de 24 horas (HH:mm).
 * 5. Cada marca registrada por el supervisor queda trazada individualmente para impedir que
 *    completar una omisión habilite la sobrescritura de marcas previas del empleado.
 */
class ValidarMarcaSupervisorUseCase {

    fun validar(
        registroActual: RegistroAsistencia?,
        tipoMarca: String,
        horaMarca: String,
        motivo: String
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
