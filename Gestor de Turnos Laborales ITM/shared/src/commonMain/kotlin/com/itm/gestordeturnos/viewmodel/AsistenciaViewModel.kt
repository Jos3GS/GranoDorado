package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.AsistenciaRepository
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.NotificacionIncumplimiento
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.repository.AsistenciaRepositoryContract
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

data class AsistenciaState(
    val notificaciones: List<NotificacionIncumplimiento> = emptyList(),
    val registrosDiarios: List<RegistroAsistencia> = emptyList(),
    val fechaHoyTexto: String = "",
    val sucursalFiltro: String = "TODAS",
    val sucursalesDisponibles: List<String> = listOf("TODAS", "CENTRO", "NORTE", "SUR"),
    val esAdminGeneral: Boolean = false,
    val sucursalUsuario: String = "CENTRO",
    val mensajeFeedback: String? = null
)

class AsistenciaViewModel(
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository,
    private val asistenciaRepository: AsistenciaRepositoryContract = AsistenciaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AsistenciaState())
    val uiState: StateFlow<AsistenciaState> = _uiState.asStateFlow()
    private val _sucursalFiltro = MutableStateFlow("TODAS")

    init {
        empleadoRepository.iniciarSincronizacionFirestore()
        turnoRepository.iniciarSincronizacionFirestore()
        asistenciaRepository.iniciarSincronizacionFirestore()
        observarAsistencia()
    }

    private fun observarAsistencia() {
        viewModelScope.launch {
            combine(
                turnoRepository.asignaciones,
                empleadoRepository.empleados,
                asistenciaRepository.registrosDiarios,
                asistenciaRepository.notificaciones,
                _sucursalFiltro
            ) { asignaciones, empleados, registros, notificacionesRepositorio, filtroSucursal ->
                recalcularAsistencia(asignaciones, empleados, registros, notificacionesRepositorio, filtroSucursal)
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    private fun obtenerFechaHoy(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    private fun obtenerHoraActual(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val h = now.hour.toString().padStart(2, '0')
        val m = now.minute.toString().padStart(2, '0')
        return "$h:$m"
    }

    private fun recalcularAsistencia(
        asignaciones: List<AsignacionTurno>,
        empleados: List<Empleado>,
        registros: List<RegistroAsistencia>,
        notificacionesRepositorio: List<NotificacionIncumplimiento>,
        filtroSucursal: String
    ): AsistenciaState {
        val hoy = obtenerFechaHoy()
        val hoyIso = hoy.toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val minutosActuales = now.hour * 60 + now.minute
        val horaActualStr = obtenerHoraActual()

        val usuario = SessionManager.usuarioActivo
        val esAdminGeneral = usuario?.rol == RolUsuario.ADMINISTRADOR_GENERAL
        val sucursalUsuario = usuario?.sucursal?.ifBlank { "CENTRO" } ?: "CENTRO"

        val nombresDias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val diaSem = nombresDias.getOrElse(hoy.dayOfWeek.ordinal) { "" }
        val fechaTexto = "$diaSem, ${hoy.dayOfMonth}/${hoy.monthNumber}/${hoy.year}"

        // Asignaciones de hoy
        val asigsHoy = asignaciones.filter { it.fechaIso == hoyIso && !it.esDescanso }

        // Registros reales de asistencia de hoy
        val registrosDeHoy = registros.filter { it.fechaIso == hoyIso }

        val idsProcesados = mutableSetOf<String>()
        val listaFilas = mutableListOf<RegistroAsistencia>()
        val notificacionesGeneradas = mutableListOf<NotificacionIncumplimiento>()

        // 1. Agregar registros de hoy guardados en Firestore
        for (reg in registrosDeHoy) {
            idsProcesados.add(reg.empleadoId)
            listaFilas.add(reg)

            // Si llegó tarde, generar notificación
            if (reg.estado == "TARDE" || reg.minutosTarde > 10) {
                val notifId = "inc_tarde_${reg.id}"
                val atendidaExistente = notificacionesRepositorio.find { it.id == notifId }
                notificacionesGeneradas.add(
                    NotificacionIncumplimiento(
                        id = notifId,
                        empleadoId = reg.empleadoId,
                        empleado = reg.empleado,
                        tipo = "Llegada tarde (${reg.minutosTarde} min de retraso)",
                        detalleHora = "Marcó: ${reg.horaEntrada} (Prog: ${reg.horarioProgramado})",
                        fechaIso = hoyIso,
                        sucursal = reg.sucursal,
                        atendida = atendidaExistente?.atendida ?: false,
                        motivoAtencion = atendidaExistente?.motivoAtencion ?: "",
                        horaAtencion = atendidaExistente?.horaAtencion ?: ""
                    )
                )
            }
        }

        // 2. Para los turnos asignados de hoy que aún no tengan marcación de asistencia
        for (asig in asigsHoy) {
            if (asig.empleadoId !in idsProcesados) {
                idsProcesados.add(asig.empleadoId)

                val empInfo = empleados.find { it.id == asig.empleadoId }
                val empNombre = if (asig.empleadoNombre.isNotBlank()) asig.empleadoNombre
                    else empInfo?.let { if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}" else it.nombre } ?: "Empleado"

                val filaPendiente = RegistroAsistencia(
                    id = "${asig.empleadoId}_${hoyIso}",
                    empleadoId = asig.empleadoId,
                    empleado = empNombre,
                    sucursal = asig.sucursalId.ifBlank { empInfo?.sucursal ?: "CENTRO" },
                    fechaIso = hoyIso,
                    tipoTurno = asig.tipoTurno,
                    horarioProgramado = asig.horario.replace("\n", " "),
                    horaEntrada = "--:--",
                    horaSalida = "--:--",
                    estado = "PENDIENTE"
                )
                listaFilas.add(filaPendiente)

                // CA-17.1: Notificación automática si pasaron 15 minutos de la hora de inicio programada sin marcación
                val partesHorario = asig.horario.split("-")
                if (partesHorario.isNotEmpty()) {
                    val horaInicioProg = parsearMinutos(partesHorario[0])
                    if (horaInicioProg != null && minutosActuales >= (horaInicioProg + 15)) {
                        val notifAutoId = "inc_retraso_${asig.empleadoId}_${hoyIso}"
                        val atendidaExistente = notificacionesRepositorio.find { it.id == notifAutoId }
                        val notifAuto = NotificacionIncumplimiento(
                            id = notifAutoId,
                            empleadoId = asig.empleadoId,
                            empleado = empNombre,
                            tipo = "Retraso > 15 min sin marcación de entrada",
                            detalleHora = "Programado a las ${partesHorario[0].trim()}. Sin ingreso registrado a las $horaActualStr.",
                            fechaIso = hoyIso,
                            sucursal = asig.sucursalId.ifBlank { empInfo?.sucursal ?: "CENTRO" },
                            atendida = atendidaExistente?.atendida ?: false,
                            motivoAtencion = atendidaExistente?.motivoAtencion ?: "",
                            horaAtencion = atendidaExistente?.horaAtencion ?: ""
                        )
                        notificacionesGeneradas.add(notifAuto)
                        if (atendidaExistente == null) {
                            asistenciaRepository.registrarIncumplimiento(notifAuto)
                        }
                    }
                }
            }
        }

        // Si no hay asignaciones ni registros para hoy, mostrar registros generales existentes
        val filasFinales = if (listaFilas.isNotEmpty()) {
            listaFilas
        } else {
            registros
        }

        // Notificaciones: fusionar dinámicas y de repositorio respetando persistencia
        val mapaNotifs = (notificacionesRepositorio + notificacionesGeneradas).associateBy { it.id }
        var notifsFinales = mapaNotifs.values.toList()

        // Filtrado por sucursal según perfil y selección
        val sucursalEfectiva = if (esAdminGeneral) filtroSucursal else sucursalUsuario
        val filasFiltradas = if (sucursalEfectiva == "TODAS") {
            filasFinales
        } else {
            filasFinales.filter { it.sucursal.equals(sucursalEfectiva, ignoreCase = true) }
        }

        if (sucursalEfectiva != "TODAS") {
            notifsFinales = notifsFinales.filter { it.sucursal.equals(sucursalEfectiva, ignoreCase = true) }
        }

        return AsistenciaState(
            notificaciones = notifsFinales,
            registrosDiarios = filasFiltradas,
            fechaHoyTexto = fechaTexto,
            sucursalFiltro = sucursalEfectiva,
            esAdminGeneral = esAdminGeneral,
            sucursalUsuario = sucursalUsuario
        )
    }

    fun cambiarSucursalFiltro(sucursal: String) {
        _sucursalFiltro.value = sucursal
    }

    fun atenderIncumplimiento(id: String, motivo: String) {
        if (motivo.isBlank()) return
        val hora = obtenerHoraActual()
        viewModelScope.launch {
            asistenciaRepository.atenderIncumplimiento(id, motivo, hora)
        }
    }

    fun registrarMarcaSupervisor(
        registroId: String,
        empleadoId: String,
        empleadoNombre: String,
        sucursal: String,
        fechaIso: String,
        tipoTurno: String,
        horarioProg: String,
        tipoMarca: String,
        horaMarca: String,
        motivo: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val usuario = SessionManager.usuarioActivo
        val esAdminGeneral = usuario?.rol == RolUsuario.ADMINISTRADOR_GENERAL
        val sucursalUsuario = usuario?.sucursal?.ifBlank { "CENTRO" } ?: "CENTRO"

        // CA-16.7: Ámbito por sucursal
        if (!esAdminGeneral && !sucursal.equals(sucursalUsuario, ignoreCase = true)) {
            onResult(false, "Solo puedes registrar marcaciones para tu sucursal ($sucursalUsuario).")
            return
        }

        viewModelScope.launch {
            val res = asistenciaRepository.registrarMarcaSupervisor(
                registroId = registroId,
                empleadoId = empleadoId,
                empleadoNombre = empleadoNombre,
                sucursal = sucursal,
                fechaIso = fechaIso,
                tipoTurno = tipoTurno,
                horarioProg = horarioProg,
                tipoMarca = tipoMarca,
                horaMarca = horaMarca,
                motivo = motivo
            )
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Error al guardar la marcación")
            }
        }
    }

    private fun parsearMinutos(horaStr: String): Int? {
        val limpia = horaStr.trim()
        val partes = limpia.split(":")
        if (partes.size == 2) {
            val h = partes[0].toIntOrNull() ?: return null
            val m = partes[1].toIntOrNull() ?: return null
            return h * 60 + m
        }
        return null
    }
}
