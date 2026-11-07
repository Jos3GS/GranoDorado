package com.itm.gestordeturnos.domain.usecase

import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Estado actual del control de intentos para un correo electrónico según la RN-02.
 */
data class EstadoIntentosLogin(
    val intentosFallidos: Int = 0,
    val bloqueadoHastaEpochMillis: Long = 0L
) {
    fun estaBloqueado(ahoraMillis: Long): Boolean = bloqueadoHastaEpochMillis > ahoraMillis

    fun minutosRestantesBloqueo(ahoraMillis: Long): Int {
        if (!estaBloqueado(ahoraMillis)) return 0
        val diffMillis = bloqueadoHastaEpochMillis - ahoraMillis
        return ceil(diffMillis / 60_000.0).toInt().coerceAtLeast(1)
    }
}

/**
 * Resultado de evaluar o registrar un intento de inicio de sesión bajo la RN-02.
 */
sealed interface EvaluacionIntentoLogin {
    data object Permitido : EvaluacionIntentoLogin
    data class FallidoRegistrado(
        val intentosAcumulados: Int,
        val intentosRestantes: Int,
        val mensaje: String = ControlIntentosLoginUseCase.MENSAJE_CREDENCIALES_O_INACTIVA
    ) : EvaluacionIntentoLogin

    data class CuentaBloqueada(
        val minutosRestantes: Int,
        val bloqueadoHastaEpochMillis: Long,
        val mensaje: String
    ) : EvaluacionIntentoLogin
}

/**
 * Caso de uso de dominio para el cumplimiento de la Regla de Negocio RN-02:
 * - Mensaje unificado: "Los datos de acceso son errados o la cuenta está inactiva."
 * - Bloqueo temporal de 15 minutos tras 5 intentos fallidos consecutivos.
 * - Reinicio del contador al iniciar sesión con éxito o al solicitar cambio/recuperación de contraseña.
 */
@OptIn(ExperimentalTime::class)
class ControlIntentosLoginUseCase(
    private val relojMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    companion object {
        const val MAX_INTENTOS_FALLIDOS = 5
        const val DURACION_BLOQUEO_MINUTOS = 15
        const val DURACION_BLOQUEO_MILLIS = DURACION_BLOQUEO_MINUTOS * 60_000L

        const val MENSAJE_CREDENCIALES_O_INACTIVA =
            "Los datos de acceso son errados o la cuenta está inactiva."

        fun construirMensajeBloqueo(minutosRestantes: Int): String {
            val sufijo = if (minutosRestantes == 1) "1 minuto" else "$minutosRestantes minutos"
            return "La cuenta se encuentra bloqueada temporalmente por superar los $MAX_INTENTOS_FALLIDOS intentos fallidos. Intenta nuevamente en $sufijo."
        }
    }

    private val estadosPorCorreo = mutableMapOf<String, EstadoIntentosLogin>()

    private fun normalizarCorreo(correo: String): String = correo.trim().lowercase()

    /**
     * Sincroniza el estado en memoria con los valores persistidos del empleado (si existen),
     * conservando el estado más restrictivo.
     */
    fun sincronizarDesdeEmpleado(
        correo: String,
        intentosPersistidos: Int,
        bloqueadoHastaPersistido: Long
    ) {
        val key = normalizarCorreo(correo)
        if (key.isBlank()) return
        val actual = estadosPorCorreo[key]
        val intentos = maxOf(actual?.intentosFallidos ?: 0, intentosPersistidos)
        val bloqueadoHasta = maxOf(actual?.bloqueadoHastaEpochMillis ?: 0L, bloqueadoHastaPersistido)
        estadosPorCorreo[key] = EstadoIntentosLogin(
            intentosFallidos = intentos,
            bloqueadoHastaEpochMillis = bloqueadoHasta
        )
    }

    /**
     * Verifica antes de autenticar si el correo tiene un bloqueo de 15 minutos activo.
     * Si el tiempo de bloqueo ya expiró, reinicia el contador automáticamente.
     */
    fun verificarAntesDeIntentar(correo: String): EvaluacionIntentoLogin {
        val key = normalizarCorreo(correo)
        if (key.isBlank()) return EvaluacionIntentoLogin.Permitido

        val ahora = relojMillis()
        val estado = estadosPorCorreo[key] ?: return EvaluacionIntentoLogin.Permitido

        if (estado.bloqueadoHastaEpochMillis > 0L) {
            if (estado.estaBloqueado(ahora)) {
                val minRestantes = estado.minutosRestantesBloqueo(ahora)
                return EvaluacionIntentoLogin.CuentaBloqueada(
                    minutosRestantes = minRestantes,
                    bloqueadoHastaEpochMillis = estado.bloqueadoHastaEpochMillis,
                    mensaje = construirMensajeBloqueo(minRestantes)
                )
            } else {
                // El bloqueo de 15 minutos ya expiró: se limpia para permitir nuevos intentos
                estadosPorCorreo.remove(key)
                return EvaluacionIntentoLogin.Permitido
            }
        }

        return EvaluacionIntentoLogin.Permitido
    }

    /**
     * Registra un intento fallido consecutivo para el correo indicado.
     * Al alcanzar el 5to intento fallido consecutivo, activa el bloqueo por 15 minutos.
     */
    fun registrarIntentoFallido(correo: String): EvaluacionIntentoLogin {
        val key = normalizarCorreo(correo)
        val ahora = relojMillis()
        val previo = estadosPorCorreo[key]

        // Si ya estaba bloqueado y sigue vigente, mantener bloqueo
        if (previo != null && previo.estaBloqueado(ahora)) {
            val minRestantes = previo.minutosRestantesBloqueo(ahora)
            return EvaluacionIntentoLogin.CuentaBloqueada(
                minutosRestantes = minRestantes,
                bloqueadoHastaEpochMillis = previo.bloqueadoHastaEpochMillis,
                mensaje = construirMensajeBloqueo(minRestantes)
            )
        }

        // Si había un bloqueo previo ya vencido, empezar conteo desde 1
        val baseIntentos = if (previo != null && previo.bloqueadoHastaEpochMillis > 0L) {
            0
        } else {
            previo?.intentosFallidos ?: 0
        }

        val nuevosIntentos = baseIntentos + 1
        return if (nuevosIntentos >= MAX_INTENTOS_FALLIDOS) {
            val bloqueadoHasta = ahora + DURACION_BLOQUEO_MILLIS
            estadosPorCorreo[key] = EstadoIntentosLogin(
                intentosFallidos = nuevosIntentos,
                bloqueadoHastaEpochMillis = bloqueadoHasta
            )
            EvaluacionIntentoLogin.CuentaBloqueada(
                minutosRestantes = DURACION_BLOQUEO_MINUTOS,
                bloqueadoHastaEpochMillis = bloqueadoHasta,
                mensaje = construirMensajeBloqueo(DURACION_BLOQUEO_MINUTOS)
            )
        } else {
            estadosPorCorreo[key] = EstadoIntentosLogin(
                intentosFallidos = nuevosIntentos,
                bloqueadoHastaEpochMillis = 0L
            )
            EvaluacionIntentoLogin.FallidoRegistrado(
                intentosAcumulados = nuevosIntentos,
                intentosRestantes = MAX_INTENTOS_FALLIDOS - nuevosIntentos,
                mensaje = MENSAJE_CREDENCIALES_O_INACTIVA
            )
        }
    }

    /**
     * Reinicia el contador y el bloqueo tras un inicio de sesión exitoso o cambio/recuperación de contraseña.
     */
    fun reiniciarContador(correo: String) {
        val key = normalizarCorreo(correo)
        estadosPorCorreo.remove(key)
    }

    fun obtenerEstado(correo: String): EstadoIntentosLogin {
        val key = normalizarCorreo(correo)
        val ahora = relojMillis()
        val estado = estadosPorCorreo[key] ?: return EstadoIntentosLogin()
        if (estado.bloqueadoHastaEpochMillis > 0L && !estado.estaBloqueado(ahora)) {
            estadosPorCorreo.remove(key)
            return EstadoIntentosLogin()
        }
        return estado
    }
}
