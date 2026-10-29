package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.NovedadRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoNovedad
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.NovedadRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn

data class ReportarNovedadState(
    val turnoReferencia: String = "Cierre · 14:00-22:00",
    val horarioProgramado: String = "",
    val fechaReferencia: String = "",
    val mostrarDatePicker: Boolean = false,
    val tipoNovedad: String = "Selecciona...",
    val horaReal: String = "",
    val descripcion: String = "",
    val diferenciaCalculada: String = "",
    val errorMensaje: String? = null,
    val mensajeExito: String? = null,
    val menuExpandido: Boolean = false,
    val opcionesNovedad: List<String> = listOf(
        "Llegada tarde",
        "Salida antes de la hora",
        "Ampliación del turno",
        "Incapacidad médica",
        "Permiso especial"
    ),
    val historial: List<Novedad> = emptyList()
) {
    val requiereHoraReal: Boolean
        get() = tipoNovedad in listOf("Llegada tarde", "Salida antes de la hora", "Ampliación del turno")
}

class ReportarNovedadViewModel(
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository,
    private val novedadRepository: NovedadRepositoryContract = NovedadRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportarNovedadState())
    val uiState: StateFlow<ReportarNovedadState> = _uiState.asStateFlow()

    private var todasLasAsignaciones: List<AsignacionTurno> = emptyList()
    private var listaEmpleados: List<Empleado> = emptyList()

    init {
        empleadoRepository.iniciarSincronizacionFirestore()
        turnoRepository.iniciarSincronizacionFirestore()
        novedadRepository.iniciarSincronizacionFirestore()

        val hoyIso = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        _uiState.update { it.copy(fechaReferencia = hoyIso) }

        observarDatos()
    }

    private fun observarDatos() {
        viewModelScope.launch {
            combine(
                novedadRepository.novedades,
                turnoRepository.asignaciones,
                empleadoRepository.empleados
            ) { novedades, asignaciones, empleados ->
                Triple(novedades, asignaciones, empleados)
            }.collect { (novedades, asignaciones, empleados) ->
                todasLasAsignaciones = asignaciones
                listaEmpleados = empleados
                actualizarVista(novedades, asignaciones, empleados)
            }
        }
    }

    private fun actualizarVista(
        novedades: List<Novedad>,
        asignaciones: List<AsignacionTurno>,
        empleados: List<Empleado>
    ) {
        val emp = resolverEmpleado(empleados)
        val empId = emp?.id ?: (SessionManager.usuarioActivo?.id ?: "emp_1")
        val empNombre = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: (SessionManager.usuarioActivo?.nombre ?: "Empleado")

        val fecha = _uiState.value.fechaReferencia.ifBlank {
            Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        }

        val turnoTexto = resolverTurnoTexto(empId, empNombre, fecha, asignaciones)
        val horarioProg = resolverHorarioTexto(empId, empNombre, fecha, asignaciones)

        // Filtrar novedades para el empleado logueado
        val misNovedades = novedades.filter { nov ->
            nov.empleadoId == empId ||
            (nov.empleado.isNotBlank() && nov.empleado.equals(empNombre, ignoreCase = true)) ||
            (SessionManager.usuarioActivo != null && nov.empleado.equals(SessionManager.usuarioActivo?.nombre, ignoreCase = true))
        }

        _uiState.update {
            it.copy(
                turnoReferencia = turnoTexto,
                horarioProgramado = horarioProg,
                historial = misNovedades
            )
        }
    }

    private fun resolverTurnoTexto(
        empId: String,
        empNombre: String,
        fechaIso: String,
        asignaciones: List<AsignacionTurno>
    ): String {
        val asig = asignaciones.find { a ->
            (a.empleadoId == empId || (a.empleadoNombre.isNotBlank() && a.empleadoNombre.equals(empNombre, ignoreCase = true))) &&
            a.fechaIso == fechaIso
        }
        return when {
            asig == null -> "Sin turno programado"
            asig.esDescanso -> "Día de Descanso"
            asig.tipoTurno.isNotBlank() -> {
                val hor = asig.horario.replace("\n", " ").trim()
                if (hor.isNotBlank()) "${asig.tipoTurno} · $hor" else asig.tipoTurno
            }
            else -> "Turno General"
        }
    }

    private fun resolverHorarioTexto(
        empId: String,
        empNombre: String,
        fechaIso: String,
        asignaciones: List<AsignacionTurno>
    ): String {
        val asig = asignaciones.find { a ->
            (a.empleadoId == empId || (a.empleadoNombre.isNotBlank() && a.empleadoNombre.equals(empNombre, ignoreCase = true))) &&
            a.fechaIso == fechaIso
        }
        return asig?.horario?.replace("\n", " ")?.trim() ?: ""
    }

    private fun resolverEmpleado(lista: List<Empleado>): Empleado? {
        val usuario = SessionManager.usuarioActivo
        if (usuario != null) {
            if (usuario.id.isNotBlank()) {
                val porId = lista.find { it.id == usuario.id }
                if (porId != null) return porId
            }
            if (usuario.correo.isNotBlank()) {
                val porCorreo = lista.find { it.correo.equals(usuario.correo, ignoreCase = true) }
                if (porCorreo != null) return porCorreo
            }
            val porNombre = lista.find {
                "${it.nombre} ${it.apellido}".trim().equals(usuario.nombre.trim(), ignoreCase = true) ||
                it.nombre.equals(usuario.nombre.trim(), ignoreCase = true)
            }
            if (porNombre != null) return porNombre
        }
        return lista.firstOrNull { it.estado == EstadoUsuario.ACTIVO }
    }

    fun onFechaCambio(nuevaFecha: String) {
        _uiState.update { it.copy(fechaReferencia = nuevaFecha, mostrarDatePicker = false) }

        val emp = resolverEmpleado(listaEmpleados)
        val empId = emp?.id ?: (SessionManager.usuarioActivo?.id ?: "emp_1")
        val empNombre = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: (SessionManager.usuarioActivo?.nombre ?: "Empleado")
        val turnoTexto = resolverTurnoTexto(empId, empNombre, nuevaFecha, todasLasAsignaciones)
        val horarioProg = resolverHorarioTexto(empId, empNombre, nuevaFecha, todasLasAsignaciones)
        _uiState.update { it.copy(turnoReferencia = turnoTexto, horarioProgramado = horarioProg, errorMensaje = null) }
        recalcularDiferencia()
    }

    fun onFechaReferenciaCambiada(nuevaFecha: String) = onFechaCambio(nuevaFecha)

    fun toggleDatePicker(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDatePicker = mostrar) }
    }

    fun onTipoNovedadSeleccionado(tipo: String) {
        _uiState.update {
            it.copy(
                tipoNovedad = tipo,
                menuExpandido = false,
                errorMensaje = null
            )
        }
        recalcularDiferencia()
    }

    fun onHoraRealCambio(nuevaHora: String) {
        _uiState.update { it.copy(horaReal = nuevaHora, errorMensaje = null) }
        recalcularDiferencia()
    }

    fun toggleMenu(expandir: Boolean) {
        _uiState.update { it.copy(menuExpandido = expandir) }
    }

    fun onDescripcionCambio(nuevaDescripcion: String) {
        _uiState.update { it.copy(descripcion = nuevaDescripcion, errorMensaje = null) }
    }

    fun onDescripcionCambiada(nuevaDescripcion: String) = onDescripcionCambio(nuevaDescripcion)

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

    private fun formatearMinutos(minutos: Int): String {
        val absM = kotlin.math.abs(minutos)
        val h = absM / 60
        val m = absM % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}min"
            h > 0 -> "${h}h"
            else -> "${m} min"
        }
    }

    private fun recalcularDiferencia() {
        val actual = _uiState.value
        if (!actual.requiereHoraReal || actual.horaReal.isBlank() || actual.horarioProgramado.isBlank()) {
            _uiState.update { it.copy(diferenciaCalculada = "") }
            return
        }

        val partesHorario = actual.horarioProgramado.split("-")
        if (partesHorario.size < 2) {
            _uiState.update { it.copy(diferenciaCalculada = "") }
            return
        }

        val iniProg = parsearMinutos(partesHorario[0].trim())
        val finProg = parsearMinutos(partesHorario[1].trim())
        val realMin = parsearMinutos(actual.horaReal)

        if (realMin == null) {
            _uiState.update { it.copy(diferenciaCalculada = "") }
            return
        }

        val textoDif = when (actual.tipoNovedad) {
            "Llegada tarde" -> {
                if (iniProg != null) {
                    val diff = realMin - iniProg
                    if (diff > 0) "+${formatearMinutos(diff)} de retraso (Prog: ${partesHorario[0].trim()})"
                    else "Marcó a tiempo o antes de la hora programada"
                } else ""
            }
            "Salida antes de la hora" -> {
                if (finProg != null) {
                    val diff = finProg - realMin
                    if (diff > 0) "-${formatearMinutos(diff)} antes de la hora programada (${partesHorario[1].trim()})"
                    else "Salió en o después de la hora programada"
                } else ""
            }
            "Ampliación del turno" -> {
                if (finProg != null) {
                    val diff = realMin - finProg
                    if (diff > 0) "+${formatearMinutos(diff)} de tiempo extra (Prog: ${partesHorario[1].trim()})"
                    else "Hora real no supera la hora de fin programada"
                } else ""
            }
            else -> ""
        }
        _uiState.update { it.copy(diferenciaCalculada = textoDif) }
    }

    fun guardarNovedad() {
        val actual = _uiState.value
        if (actual.tipoNovedad == "Selecciona...") {
            _uiState.update { it.copy(errorMensaje = "Por favor selecciona el tipo de novedad.") }
            return
        }
        if (actual.descripcion.isBlank()) {
            _uiState.update { it.copy(errorMensaje = "La descripción de la novedad es obligatoria (CA-11.4).") }
            return
        }

        val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val fechaRef = actual.fechaReferencia.ifBlank { hoy.toString() }
        val fechaObj = try { LocalDate.parse(fechaRef) } catch (_: Throwable) { hoy }
        val diffDias = hoy.toEpochDays() - fechaObj.toEpochDays()

        // CA-11.4: Plazo de 48 horas hacia atrás
        if (actual.requiereHoraReal) {
            if (diffDias < 0) {
                _uiState.update { it.copy(errorMensaje = "No se pueden reportar novedades de horarios sobre turnos futuros.") }
                return
            }
            if (diffDias > 2) {
                _uiState.update { it.copy(errorMensaje = "El plazo máximo para reportar novedades es de 48 horas posteriores al turno (CA-11.4).") }
                return
            }
            if (actual.horaReal.isBlank()) {
                _uiState.update { it.copy(errorMensaje = "Por favor ingresa la hora real para calcular la diferencia (CA-11.3).") }
                return
            }

            // CA-11.3: Validación de coherencia
            val partesHorario = actual.horarioProgramado.split("-")
            if (partesHorario.size == 2) {
                val iniProg = parsearMinutos(partesHorario[0].trim())
                val finProg = parsearMinutos(partesHorario[1].trim())
                val real = parsearMinutos(actual.horaReal)
                if (real != null && finProg != null && actual.tipoNovedad == "Salida antes de la hora" && real >= finProg) {
                    _uiState.update { it.copy(errorMensaje = "La salida antes de tiempo debe ser anterior a la hora programada (${partesHorario[1].trim()}).") }
                    return
                }
                if (real != null && finProg != null && actual.tipoNovedad == "Ampliación del turno" && real <= finProg) {
                    _uiState.update { it.copy(errorMensaje = "La ampliación de turno debe ser posterior a la hora programada (${partesHorario[1].trim()}).") }
                    return
                }
                if (real != null && iniProg != null && actual.tipoNovedad == "Llegada tarde" && real <= iniProg) {
                    _uiState.update { it.copy(errorMensaje = "La llegada tarde debe ser posterior a la hora de inicio (${partesHorario[0].trim()}).") }
                    return
                }
            }
        }

        val emp = resolverEmpleado(listaEmpleados)
        val empId = emp?.id ?: (SessionManager.usuarioActivo?.id ?: "emp_1")
        val empNombre = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: (SessionManager.usuarioActivo?.nombre ?: "Empleado")
        val empSucursal = emp?.sucursal ?: (SessionManager.usuarioActivo?.sucursal ?: "CENTRO")

        val colorBorde = when (actual.tipoNovedad) {
            "Llegada tarde" -> "ROJO"
            "Salida antes de la hora" -> "ROJO"
            "Ampliación del turno" -> "VERDE"
            "Incapacidad médica" -> "ROJO_OSCURO"
            "Permiso especial" -> "GRIS"
            else -> "GRIS"
        }

        val timestamp = Clock.System.now().toEpochMilliseconds()
        val hoyIso = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

        val nueva = Novedad(
            id = "nov_${timestamp}_${empId}",
            tipo = actual.tipoNovedad,
            descripcion = actual.descripcion,
            empleado = empNombre,
            empleadoId = empId,
            sucursal = empSucursal,
            detalleTurno = "${actual.turnoReferencia} · $fechaRef",
            diferencia = actual.diferenciaCalculada.ifBlank { actual.descripcion },
            estado = EstadoNovedad.REPORTADA,
            horaReal = actual.horaReal,
            colorBorde = colorBorde,
            colorVorde = colorBorde,
            fechaReferencia = fechaRef,
            fechaCreacionIso = hoyIso
        )

        novedadRepository.reportarNovedad(nueva)

        _uiState.update {
            it.copy(
                tipoNovedad = "Selecciona...",
                horaReal = "",
                descripcion = "",
                diferenciaCalculada = "",
                errorMensaje = null,
                mensajeExito = "Novedad registrada exitosamente para revisión del administrador.",
                fechaReferencia = hoyIso
            )
        }
        val turnoTexto = resolverTurnoTexto(empId, empNombre, hoyIso, todasLasAsignaciones)
        _uiState.update { it.copy(turnoReferencia = turnoTexto) }
    }

    fun enviarFormulario() = guardarNovedad()
}
