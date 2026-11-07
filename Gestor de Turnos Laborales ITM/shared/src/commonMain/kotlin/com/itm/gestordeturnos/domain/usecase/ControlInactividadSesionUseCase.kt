package com.itm.gestordeturnos.domain.usecase

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Representa el estado del monitor de inactividad de la sesión.
 */
data class EstadoInactividadSesion(
    val tiempoUltimaActividadMillis: Long = 0L,
    val sesionExpirada: Boolean = false,
    val tiempoInactivoMillis: Long = 0L
)

/**
 * Caso de uso de dominio para la Regla de Negocio RN-03:
 * "La sesión se cierra automáticamente tras 30 minutos sin interacción del usuario."
 */
@OptIn(ExperimentalTime::class)
class ControlInactividadSesionUseCase(
    val duracionInactividadMillis: Long = TIEMPO_MAXIMO_INACTIVIDAD_MILLIS,
    private val relojMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    companion object {
        const val MINUTOS_MAXIMO_INACTIVIDAD = 30
        const val TIEMPO_MAXIMO_INACTIVIDAD_MILLIS = MINUTOS_MAXIMO_INACTIVIDAD * 60_000L // 1.800.000 ms
        const val INTERVALO_SONDEO_SEGUNDOS = 15L

        const val MENSAJE_SESION_EXPIRADA =
            "Tu sesión ha expirado por 30 minutos de inactividad. Por favor, inicia sesión nuevamente."
    }

    private var ultimaActividadMillis: Long = relojMillis()
    private var sesionActiva: Boolean = false
    private var expirada: Boolean = false

    /**
     * Inicia el seguimiento de inactividad al iniciar sesión.
     */
    fun iniciarMonitoreo() {
        ultimaActividadMillis = relojMillis()
        sesionActiva = true
        expirada = false
    }

    /**
     * Registra una interacción del usuario (click, touch, scroll, tecleo o cambio de vista).
     * Si la sesión ya había expirado, no se reinicia sin un nuevo inicio de sesión.
     */
    fun registrarInteraccion() {
        if (!sesionActiva || expirada) return
        ultimaActividadMillis = relojMillis()
    }

    /**
     * Evalúa si ha transcurrido el tiempo límite de inactividad (30 min).
     * @return true si la sesión acaba de expirar o ya expiró por inactividad.
     */
    fun verificarSiExpiro(): Boolean {
        if (!sesionActiva) return false
        if (expirada) return true

        val ahora = relojMillis()
        val transcurrido = ahora - ultimaActividadMillis
        if (transcurrido >= duracionInactividadMillis) {
            expirada = true
            return true
        }
        return false
    }

    /**
     * Detiene el monitoreo al cerrar sesión voluntaria o automáticamente.
     */
    fun detenerMonitoreo() {
        sesionActiva = false
        expirada = false
        ultimaActividadMillis = 0L
    }

    fun obtenerEstado(): EstadoInactividadSesion {
        val ahora = relojMillis()
        val transcurrido = if (sesionActiva && ultimaActividadMillis > 0L) {
            (ahora - ultimaActividadMillis).coerceAtLeast(0L)
        } else {
            0L
        }
        return EstadoInactividadSesion(
            tiempoUltimaActividadMillis = ultimaActividadMillis,
            sesionExpirada = expirada || (sesionActiva && transcurrido >= duracionInactividadMillis),
            tiempoInactivoMillis = transcurrido
        )
    }
}
