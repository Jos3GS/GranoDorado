package com.itm.gestordeturnos.domain.usecase

/**
 * Resultado de la validación de la nueva contraseña según las reglas de Firebase Authentication.
 * Requisito oficial de Firebase Auth: longitud mínima de 6 caracteres.
 */
sealed interface ValidacionClaveResult {
    data object Valida : ValidacionClaveResult
    data class Invalida(val motivo: String) : ValidacionClaveResult
}

/**
 * Caso de uso para validar la nueva contraseña conforme a los requisitos de Firebase Authentication.
 */
class ValidarContrasenaFirebaseAuthUseCase {

    companion object {
        const val LONGITUD_MINIMA_FIREBASE = 6
    }

    /**
     * Valida si la nueva contraseña cumple los requisitos exigidos por Firebase Auth y reglas de consistencia.
     */
    fun validar(claveActual: String, nuevaClave: String, confirmacionClave: String): ValidacionClaveResult {
        if (claveActual.isBlank()) {
            return ValidacionClaveResult.Invalida("Debes ingresar tu contraseña actual.")
        }
        if (nuevaClave.isBlank()) {
            return ValidacionClaveResult.Invalida("Debes ingresar la nueva contraseña.")
        }
        if (nuevaClave.length < LONGITUD_MINIMA_FIREBASE) {
            return ValidacionClaveResult.Invalida("La nueva contraseña debe tener al menos $LONGITUD_MINIMA_FIREBASE caracteres (requisito de Firebase).")
        }
        if (nuevaClave == claveActual) {
            return ValidacionClaveResult.Invalida("La nueva contraseña debe ser distinta a la contraseña actual.")
        }
        if (nuevaClave != confirmacionClave) {
            return ValidacionClaveResult.Invalida("La confirmación no coincide con la nueva contraseña.")
        }
        return ValidacionClaveResult.Valida
    }
}
