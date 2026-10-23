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
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class ReportarNovedadState(
    val turnoReferencia: String = "Cierre · 14:00-22:00",
    val fechaReferencia: String = "",
    val mostrarDatePicker: Boolean = false,
    val tipoNovedad: String = "Selecciona...",
    val descripcion: String = "",
    val menuExpandido: Boolean = false,
    val opcionesNovedad: List<String> = listOf(
        "Llegada tarde",
        "Salida antes de la hora",
        "Ampliación del turno",
        "Incapacidad médica",
        "Permiso especial"
    ),
    val historial: List<Novedad> = emptyList()
)

class ReportarNovedadViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportarNovedadState())
    val uiState: StateFlow<ReportarNovedadState> = _uiState.asStateFlow()

    private var todasLasAsignaciones: List<AsignacionTurno> = emptyList()
    private var listaEmpleados: List<Empleado> = emptyList()

    init {
        EmpleadoRepository.iniciarSincronizacionFirestore()
        TurnoRepository.iniciarSincronizacionFirestore()
        NovedadRepository.iniciarSincronizacionFirestore()

        val hoyIso = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        _uiState.update { it.copy(fechaReferencia = hoyIso) }

        observarDatos()
    }

    private fun observarDatos() {
        viewModelScope.launch {
            combine(
                NovedadRepository.novedades,
                TurnoRepository.asignaciones,
                EmpleadoRepository.empleados
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

        // Filtrar novedades para el empleado logueado
        val misNovedades = novedades.filter { nov ->
            nov.empleadoId == empId ||
            (nov.empleado.isNotBlank() && nov.empleado.equals(empNombre, ignoreCase = true)) ||
            (SessionManager.usuarioActivo != null && nov.empleado.equals(SessionManager.usuarioActivo?.nombre, ignoreCase = true))
        }

        _uiState.update {
            it.copy(
                turnoReferencia = turnoTexto,
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
        _uiState.update { it.copy(turnoReferencia = turnoTexto) }
    }

    fun onFechaReferenciaCambiada(nuevaFecha: String) = onFechaCambio(nuevaFecha)

    fun toggleDatePicker(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarDatePicker = mostrar) }
    }

    fun onTipoNovedadSeleccionado(tipo: String) {
        _uiState.update {
            it.copy(
                tipoNovedad = tipo,
                menuExpandido = false
            )
        }
    }

    fun toggleMenu(expandir: Boolean) {
        _uiState.update { it.copy(menuExpandido = expandir) }
    }

    fun onDescripcionCambio(nuevaDescripcion: String) {
        _uiState.update { it.copy(descripcion = nuevaDescripcion) }
    }

    fun onDescripcionCambiada(nuevaDescripcion: String) = onDescripcionCambio(nuevaDescripcion)

    fun guardarNovedad() {
        val actual = _uiState.value
        if (actual.tipoNovedad == "Selecciona..." || actual.descripcion.isBlank()) {
            return
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
        val fechaRef = actual.fechaReferencia.ifBlank { hoyIso }

        val nueva = Novedad(
            id = "nov_${timestamp}_${empId}",
            tipo = actual.tipoNovedad,
            descripcion = actual.descripcion,
            empleado = empNombre,
            empleadoId = empId,
            sucursal = empSucursal,
            detalleTurno = "${actual.turnoReferencia} · $fechaRef",
            diferencia = actual.descripcion,
            estado = EstadoNovedad.REPORTADA,
            colorBorde = colorBorde,
            colorVorde = colorBorde,
            fechaReferencia = fechaRef,
            fechaCreacionIso = hoyIso
        )

        NovedadRepository.reportarNovedad(nueva)

        _uiState.update {
            it.copy(
                tipoNovedad = "Selecciona...",
                descripcion = "",
                fechaReferencia = hoyIso
            )
        }
        val turnoTexto = resolverTurnoTexto(empId, empNombre, hoyIso, todasLasAsignaciones)
        _uiState.update { it.copy(turnoReferencia = turnoTexto) }
    }

    fun enviarFormulario() = guardarNovedad()
}
