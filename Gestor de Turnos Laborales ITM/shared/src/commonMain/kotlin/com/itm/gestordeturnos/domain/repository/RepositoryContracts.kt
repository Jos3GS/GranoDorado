package com.itm.gestordeturnos.domain.repository

import com.itm.gestordeturnos.domain.model.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato de dominio para el repositorio de Empleados (Principio de Inversión de Dependencias - DIP).
 */
interface EmpleadoRepositoryContract {
    val empleados: StateFlow<List<Empleado>>
    fun iniciarSincronizacionFirestore(forzar: Boolean = false)
    fun detenerSincronizacion()
    fun guardarEmpleado(empleado: Empleado, esNuevo: Boolean)
    suspend fun buscarPorCorreo(correo: String): Empleado?
}

/**
 * Contrato de dominio para el repositorio de Turnos y Asignaciones (DIP).
 */
interface TurnoRepositoryContract {
    val turnosCatalogo: StateFlow<List<TurnoTemplate>>
    val asignaciones: StateFlow<List<AsignacionTurno>>
    val horarioSemana: StateFlow<List<TurnoAsignado>>
    val filasMalla: StateFlow<List<FilaMalla>>
    val turnosPropios: StateFlow<List<TurnoSeleccionable>>
    val turnosCompaneros: StateFlow<List<TurnoCompaneroSeleccionable>>

    fun iniciarSincronizacionFirestore(forzar: Boolean = false)
    fun detenerSincronizacion()
    fun refrescarDesdeFirestore()
    suspend fun guardarAsignacion(asignacion: AsignacionTurno): Result<Unit>
    suspend fun guardarAsignaciones(lista: List<AsignacionTurno>): Result<Unit>
    fun guardarAsignacionesEnBackground(lista: List<AsignacionTurno>)
    suspend fun forzarSincronizacionCatalogo(): Result<Unit>
    suspend fun guardarTurnoCatalogo(turno: TurnoTemplate, esNuevo: Boolean): Result<Unit>
    fun alternarEstadoTurno(idTurno: String)
    fun agregarTurnoCatalogo(turno: TurnoTemplate)
    fun getTurnoHoy(): TurnoAsignado?
}

/**
 * Contrato de dominio para el repositorio de Novedades Operativas (DIP).
 */
interface NovedadRepositoryContract {
    val novedades: StateFlow<List<Novedad>>
    fun iniciarSincronizacionFirestore(forzar: Boolean = false)
    fun detenerSincronizacion()
    suspend fun guardarNovedad(novedad: Novedad): Result<Unit>
    fun reportarNovedad(novedad: Novedad)
    fun revisarNovedad(idNovedad: String, comentario: String, revisor: String, fecha: String)
    fun cambiarEstadoNovedad(idNovedad: String, nuevoEstado: EstadoNovedad)
}

/**
 * Contrato de dominio para el repositorio de Solicitudes de Cambio de Turno (DIP).
 */
interface CambiosTurnoRepositoryContract {
    val solicitudes: StateFlow<List<SolicitudCambioTurno>>
    fun iniciarSincronizacionFirestore(forzar: Boolean = false)
    fun detenerSincronizacion()
    suspend fun guardarSolicitud(solicitud: SolicitudCambioTurno): Result<Unit>
    fun crearSolicitud(solicitud: SolicitudCambioTurno)
    fun aceptarPorCompanero(idSolicitud: String)
    fun rechazarSolicitud(idSolicitud: String, motivo: String)
    fun cancelarSolicitud(idSolicitud: String)
    fun aprobarEIntercambiar(solicitud: SolicitudCambioTurno)
}

/**
 * Contrato de dominio para el repositorio de Asistencia e Incumplimientos (DIP).
 */
interface AsistenciaRepositoryContract {
    val notificaciones: StateFlow<List<NotificacionIncumplimiento>>
    val registrosDiarios: StateFlow<List<RegistroAsistencia>>
    fun iniciarSincronizacionFirestore(forzar: Boolean = false)
    fun detenerSincronizacion()
    suspend fun guardarRegistroAsistencia(registro: RegistroAsistencia): Result<Unit>
    fun guardarRegistroAsistenciaEnBackground(registro: RegistroAsistencia)
    fun registrarIncumplimiento(notif: NotificacionIncumplimiento)
    suspend fun guardarIncumplimiento(notif: NotificacionIncumplimiento): Result<Unit>
    suspend fun atenderIncumplimiento(id: String, motivo: String, horaAtencion: String): Result<Unit>
    suspend fun resolverIncumplimientoPorLlegada(empleadoId: String, fechaIso: String): Result<Unit>
    suspend fun registrarMarcaSupervisor(
        registroId: String,
        empleadoId: String,
        empleadoNombre: String,
        sucursal: String,
        fechaIso: String,
        tipoTurno: String,
        horarioProg: String,
        tipoMarca: String,
        horaMarca: String,
        motivo: String
    ): Result<Unit>
    fun actualizarNotificaciones(lista: List<NotificacionIncumplimiento>)
}
