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
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class AsistenciaState(
    val notificaciones: List<NotificacionIncumplimiento> = emptyList(),
    val registrosDiarios: List<RegistroAsistencia> = emptyList(),
    val fechaHoyTexto: String = ""
)

class AsistenciaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AsistenciaState())
    val uiState: StateFlow<AsistenciaState> = _uiState.asStateFlow()

    init {
        EmpleadoRepository.iniciarSincronizacionFirestore()
        TurnoRepository.iniciarSincronizacionFirestore()
        AsistenciaRepository.iniciarSincronizacionFirestore()
        observarAsistencia()
    }

    private fun observarAsistencia() {
        viewModelScope.launch {
            combine(
                TurnoRepository.asignaciones,
                EmpleadoRepository.empleados,
                AsistenciaRepository.registrosDiarios,
                AsistenciaRepository.notificaciones
            ) { asignaciones, empleados, registros, notificacionesRepositorio ->
                recalcularAsistencia(asignaciones, empleados, registros, notificacionesRepositorio)
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    private fun obtenerFechaHoy(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    private fun recalcularAsistencia(
        asignaciones: List<AsignacionTurno>,
        empleados: List<Empleado>,
        registros: List<RegistroAsistencia>,
        notificacionesRepositorio: List<NotificacionIncumplimiento>
    ): AsistenciaState {
        val hoy = obtenerFechaHoy()
        val hoyIso = hoy.toString()

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
                notificacionesGeneradas.add(
                    NotificacionIncumplimiento(
                        id = "notif_tarde_${reg.id}",
                        empleado = reg.empleado,
                        tipo = "Llegada tarde (${reg.minutosTarde} min de retraso)",
                        detalleHora = "Marcó: ${reg.horaEntrada} (Prog: ${reg.horarioProgramado})",
                        fechaIso = hoyIso,
                        sucursal = reg.sucursal
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
            }
        }

        // Si no hay asignaciones ni registros para hoy, mostrar registros generales existentes
        val filasFinales = if (listaFilas.isNotEmpty()) {
            listaFilas
        } else {
            registros
        }

        // Notificaciones: combinar dinámicas con las del repositorio
        val notifsCombinadas = (notificacionesGeneradas + notificacionesRepositorio)
            .distinctBy { "${it.empleado}_${it.tipo}" }

        return AsistenciaState(
            notificaciones = notifsCombinadas,
            registrosDiarios = filasFinales,
            fechaHoyTexto = fechaTexto
        )
    }
}
