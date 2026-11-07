package com.itm.gestordeturnos.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TipoAccionAuditoria {
    LOGIN_EXITOSO,
    LOGIN_FALLIDO,
    CREAR_EMPLEADO,
    MODIFICAR_EMPLEADO,
    CREAR_TURNO,
    MODIFICAR_TURNO,
    PUBLICAR_MALLA,
    APROBAR_CAMBIO_TURNO,
    RECHAZAR_CAMBIO_TURNO,
    CANCELAR_CAMBIO_TURNO,
    MARCA_SUPERVISOR,
    REGISTRAR_NOVEDAD,
    REVISAR_NOVEDAD
}

@Serializable
data class EventoAuditoria(
    val id: String = "",
    val fechaHoraIso: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val rol: String = "",
    val sucursal: String = "",
    val tipoAccion: TipoAccionAuditoria = TipoAccionAuditoria.LOGIN_EXITOSO,
    val entidad: String = "",
    val entidadId: String = "",
    val valorAnterior: String = "",
    val valorNuevo: String = "",
    val detalles: String = "",
    val ipODispositivo: String = ""
)
