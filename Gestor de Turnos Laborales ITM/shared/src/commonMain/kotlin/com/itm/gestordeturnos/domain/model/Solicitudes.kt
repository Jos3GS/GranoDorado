package com.itm.gestordeturnos.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

typealias SolicitudCambio = SolicitudCambioTurno

@Serializable
data class SolicitudCambioTurno(
    val id: String = "",
    val numeroSolicitud: String = "",
    val fechaSolicitud: LocalDate? = null,
    val estado: EstadoSolicitudCambio = EstadoSolicitudCambio.PENDIENTE,
    val motivoRechazo: String = "",
    val motivo: String = "",
    val empleadoSolicitanteId: String = "",
    val nombreSolicitante: String = "",
    val empleadoCompaneroId: String = "",
    val nombreCompanero: String = "",
    val turnoEntregaId: String = "",
    val tipoTurnoEntrega: String = "",
    val detalleTurnoEntrega: String = "",
    val turnoRecibeId: String = "",
    val tipoTurnoRecibe: String = "",
    val detalleTurnoRecibe: String = "",
    val sucursalNombre: String = "",
    val validacionesOk: Boolean = true,
    val esRemitente: Boolean = false
) {
    val sucursal: String get() = sucursalNombre
    val nombreEntrega: String get() = nombreSolicitante
    val nombreRecibe: String get() = nombreCompanero
    val estadoResumen: String get() = estado.displayName

    fun aceptarCompanero(): SolicitudCambioTurno = copy(
        estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO
    )

    fun aprobar(): SolicitudCambioTurno = copy(
        estado = EstadoSolicitudCambio.APROBADA
    )

    fun rechazar(motivo: String): SolicitudCambioTurno = copy(
        estado = EstadoSolicitudCambio.RECHAZADA,
        motivoRechazo = motivo
    )

    fun verificarVencimiento(fechaActual: LocalDate): Boolean {
        if (fechaSolicitud == null) return false
        return fechaActual > fechaSolicitud && estado == EstadoSolicitudCambio.PENDIENTE
    }
}

@Serializable
data class Ausencia(
    val id: String = "",
    val tipo: TipoAusencia = TipoAusencia.PERMISO,
    val fechaInicio: LocalDate? = null,
    val fechaFin: LocalDate? = null,
    val motivo: String = "",
    val empleadoId: String = "",
    val tramitada: Boolean = false
) {
    fun darTramite(): Ausencia = copy(tramitada = true)
}
