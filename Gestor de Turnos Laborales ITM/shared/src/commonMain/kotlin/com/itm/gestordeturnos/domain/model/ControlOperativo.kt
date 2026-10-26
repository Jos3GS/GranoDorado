package com.itm.gestordeturnos.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class Novedad(
    val id: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val horaInicioReal: LocalTime? = null,
    val horaFinReal: LocalTime? = null,
    val estado: EstadoNovedad = EstadoNovedad.REPORTADA,
    val empleado: String = "",
    val sucursal: String = "",
    val detalleTurno: String = "",
    val diferencia: String = "",
    val colorBorde: String = "GRIS",
    val colorVorde: String = "GRIS",
    val empleadoId: String = "",
    val asignacionTurnoId: String = "",
    val fechaReferencia: String = "",
    val fechaCreacionIso: String = "",
    val horaReal: String = "",
    val comentarioRevision: String = "",
    val revisadoPor: String = "",
    val fechaRevision: String = "",
    val reportadoPorSupervisor: Boolean = false
) {
    fun registrar(): Novedad = copy(estado = EstadoNovedad.REPORTADA)
    fun revisar(comentario: String = "", revisor: String = "", fecha: String = ""): Novedad = copy(
        estado = EstadoNovedad.REVISADA,
        comentarioRevision = comentario,
        revisadoPor = revisor,
        fechaRevision = fecha
    )
    fun corregir(nuevaDescripcion: String): Novedad = copy(descripcion = nuevaDescripcion)
    fun eliminar(): Novedad = copy(estado = EstadoNovedad.REVISADA)
}

@Serializable
data class MarcaAsistencia(
    val id: String = "",
    val tipo: TipoMarca = TipoMarca.INICIO_TURNO,
    val horaRegistro: LocalDateTime? = null,
    val registradaPorAdministrador: Boolean = false,
    val motivoRegistroManual: String = "",
    val asignacionTurnoId: String = "",
    val empleadoId: String = ""
) {
    fun registrarMarca(): MarcaAsistencia = this
}

@Serializable
data class Incumplimiento(
    val id: String = "",
    val horaDeteccion: LocalDateTime? = null,
    val estado: EstadoIncumplimiento = EstadoIncumplimiento.PENDIENTE,
    val comentario: String = "",
    val asignacionTurnoId: String = "",
    val empleadoId: String = "",
    val empleadoNombre: String = ""
) {
    fun atender(nuevoComentario: String): Incumplimiento = copy(
        estado = EstadoIncumplimiento.ATENDIDO,
        comentario = nuevoComentario
    )
}
