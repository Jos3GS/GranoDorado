package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.EstadoNovedad
import com.itm.gestordeturnos.domain.model.Novedad

sealed interface ValidacionGestionNovedadResult {
    data object Valido : ValidacionGestionNovedadResult
    data class Invalido(val mensaje: String) : ValidacionGestionNovedadResult
}

/**
 * Caso de uso de dominio para la gestión del ciclo de vida y estados de las novedades (RN-25).
 *
 * Reglas de negocio:
 * 1. Un colaborador puede editar o eliminar su novedad reportada mientras se encuentre en estado REPORTADA.
 * 2. Una vez que la novedad pase a estado REVISADA, no puede ser editada ni eliminada por el colaborador.
 * 3. Una novedad en estado REVISADA no puede revertirse a REPORTADA.
 * 4. El administrador únicamente puede agregar o editar comentarios de revisión sin modificar ni borrar
 *    la descripción ni los datos originales reportados por el colaborador.
 * 5. Cuando un supervisor registra directamente una novedad, nace inmediatamente en estado REVISADA.
 */
class GestionarEstadoNovedadUseCase {

    /**
     * Valida si un colaborador puede editar una novedad.
     * Solo es editable mientras se encuentre en estado REPORTADA.
     */
    fun validarEdicionColaborador(
        novedad: Novedad,
        usuarioActualId: String
    ): ValidacionGestionNovedadResult {
        if (novedad.estado == EstadoNovedad.REVISADA) {
            return ValidacionGestionNovedadResult.Invalido(
                "La novedad ya ha sido revisada y no puede ser modificada."
            )
        }
        if (usuarioActualId.isNotBlank() && novedad.empleadoId.isNotBlank() && novedad.empleadoId != usuarioActualId) {
            return ValidacionGestionNovedadResult.Invalido(
                "No tienes permisos para editar una novedad reportada por otro colaborador."
            )
        }
        return ValidacionGestionNovedadResult.Valido
    }

    /**
     * Valida si un colaborador puede eliminar una novedad.
     * Solo es eliminable mientras se encuentre en estado REPORTADA.
     */
    fun validarEliminacionColaborador(
        novedad: Novedad,
        usuarioActualId: String
    ): ValidacionGestionNovedadResult {
        if (novedad.estado == EstadoNovedad.REVISADA) {
            return ValidacionGestionNovedadResult.Invalido(
                "La novedad ya ha sido revisada y no puede ser eliminada."
            )
        }
        if (usuarioActualId.isNotBlank() && novedad.empleadoId.isNotBlank() && novedad.empleadoId != usuarioActualId) {
            return ValidacionGestionNovedadResult.Invalido(
                "No tienes permisos para eliminar una novedad reportada por otro colaborador."
            )
        }
        return ValidacionGestionNovedadResult.Valido
    }

    /**
     * Valida la transición de estado de una novedad.
     * Una novedad en estado REVISADA no puede revertirse a REPORTADA.
     */
    fun validarTransicionEstado(
        estadoActual: EstadoNovedad,
        nuevoEstado: EstadoNovedad
    ): ValidacionGestionNovedadResult {
        if (estadoActual == EstadoNovedad.REVISADA && nuevoEstado == EstadoNovedad.REPORTADA) {
            return ValidacionGestionNovedadResult.Invalido(
                "Una novedad que ya fue revisada no puede revertirse al estado reportada."
            )
        }
        return ValidacionGestionNovedadResult.Valido
    }

    /**
     * Valida que la revisión del administrador preserve intactos los datos originales del reporte.
     * El administrador únicamente puede añadir o editar comentarios y datos de revisión.
     */
    fun validarRevisionAdministrador(
        novedadOriginal: Novedad,
        novedadConRevision: Novedad
    ): ValidacionGestionNovedadResult {
        if (novedadOriginal.tipo != novedadConRevision.tipo ||
            novedadOriginal.descripcion.trim() != novedadConRevision.descripcion.trim() ||
            novedadOriginal.horaReal.trim() != novedadConRevision.horaReal.trim() ||
            novedadOriginal.fechaReferencia != novedadConRevision.fechaReferencia ||
            novedadOriginal.empleadoId != novedadConRevision.empleadoId
        ) {
            return ValidacionGestionNovedadResult.Invalido(
                "Durante la revisión administrativa no se puede alterar ni borrar la información original reportada por el colaborador."
            )
        }
        return ValidacionGestionNovedadResult.Valido
    }

    /**
     * Prepara una novedad registrada directamente por un supervisor o administrador.
     * Nace inmediatamente en estado REVISADA con los datos de auditoría de revisión precompletados.
     */
    fun prepararNovedadDirectaSupervisor(
        novedadBase: Novedad,
        revisor: String,
        fechaRevision: String,
        comentarioSupervisor: String = "Registrada y aprobada directamente por supervisión"
    ): Novedad {
        return novedadBase.copy(
            estado = EstadoNovedad.REVISADA,
            revisadoPor = revisor,
            fechaRevision = fechaRevision,
            comentarioRevision = comentarioSupervisor,
            reportadoPorSupervisor = true
        )
    }
}
