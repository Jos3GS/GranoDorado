package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract

/**
 * Resultado de la solicitud de recuperación de contraseña bajo la RN-05.
 */
sealed interface SolicitudRecuperacionResult {
    /**
     * El correo fue enviado efectivamente al empleado activo.
     */
    data object Enviado : SolicitudRecuperacionResult

    /**
     * La cuenta no existe o se encuentra inactiva/deshabilitada.
     * Bajo la RN-05 no se envía ningún correo de recuperación.
     */
    data class NoEnviado(val motivo: String) : SolicitudRecuperacionResult

    /**
     * Error de red o servicio al intentar enviar el correo.
     */
    data class ErrorServicio(val mensaje: String) : SolicitudRecuperacionResult
}

/**
 * Caso de uso de dominio para la Regla de Negocio RN-05:
 * "El enlace de recuperación de contraseña es de un solo uso y su vigencia será la predeterminada
 * por la plataforma de autenticación (aprox. 60 minutos). El sistema regulará el envío excesivo
 * de solicitudes mediante las políticas antispam nativas del servicio, y una cuenta inactiva o
 * deshabilitada no recibirá correos de recuperación."
 */
class ValidarRecuperacionContrasenaUseCase(
    private val empleadoRepository: EmpleadoRepositoryContract
) {
    companion object {
        const val MENSAJE_CUENTA_INACTIVA =
            "La cuenta ingresada se encuentra inactiva o deshabilitada. No es posible enviar un enlace de recuperación."
        const val MENSAJE_CUENTA_NO_REGISTRADA =
            "El correo ingresado no se encuentra registrado en el sistema."
    }

    /**
     * Evalúa si un correo es elegible para recibir el enlace de recuperación de contraseña.
     * Si la cuenta no está registrada o su estado es distinto a ACTIVO, deniega el envío.
     */
    suspend fun evaluarElegibilidad(correo: String): Result<Unit> {
        val emailNormalizado = correo.trim()
        if (emailNormalizado.isBlank()) {
            return Result.failure(IllegalArgumentException("El correo electrónico es requerido."))
        }

        val empleado = empleadoRepository.buscarPorCorreo(emailNormalizado)
            ?: return Result.failure(IllegalStateException(MENSAJE_CUENTA_NO_REGISTRADA))

        if (empleado.estado != EstadoUsuario.ACTIVO) {
            return Result.failure(IllegalStateException(MENSAJE_CUENTA_INACTIVA))
        }

        return Result.success(Unit)
    }
}
