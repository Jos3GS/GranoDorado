package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.EventoAuditoria
import com.itm.gestordeturnos.domain.model.TipoAccionAuditoria

/**
 * Caso de uso de dominio para filtrar y consultar eventos de auditoría (RN-32).
 */
class FiltrarAuditoriaUseCase {

    fun filtrar(
        eventos: List<EventoAuditoria>,
        tipoAccion: TipoAccionAuditoria? = null,
        sucursal: String = "TODAS",
        filtroFecha: String = "",
        filtroTexto: String = ""
    ): List<EventoAuditoria> {
        val query = filtroTexto.trim().lowercase()

        return eventos.filter { ev ->
            // Filtro por tipo de acción
            val cumpleTipo = tipoAccion == null || ev.tipoAccion == tipoAccion

            // Filtro por sucursal
            val cumpleSucursal = sucursal == "TODAS" || ev.sucursal.equals(sucursal, ignoreCase = true)

            // Filtro por fecha (YYYY-MM-DD)
            val cumpleFecha = filtroFecha.isBlank() || ev.fechaHoraIso.startsWith(filtroFecha)

            // Filtro por texto libre
            val cumpleTexto = query.isBlank() ||
                    ev.usuarioNombre.lowercase().contains(query) ||
                    ev.usuarioId.lowercase().contains(query) ||
                    ev.detalles.lowercase().contains(query) ||
                    ev.entidad.lowercase().contains(query) ||
                    ev.entidadId.lowercase().contains(query) ||
                    ev.valorAnterior.lowercase().contains(query) ||
                    ev.valorNuevo.lowercase().contains(query)

            cumpleTipo && cumpleSucursal && cumpleFecha && cumpleTexto
        }.sortedByDescending { it.fechaHoraIso }
    }
}
