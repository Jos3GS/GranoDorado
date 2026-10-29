package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.TurnoAsignado
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

private val MESES_COMPLETOS = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private val MESES_CORTOS = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic"
)

private fun obtenerMesCorto(mes: Int): String =
    MESES_CORTOS.getOrElse(mes - 1) { "" }

private fun calcularLunesSemanaActual(): LocalDate {
    val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val diasDesdeLunes = hoy.dayOfWeek.ordinal
    return hoy.minus(DatePeriod(days = diasDesdeLunes))
}

private fun formatearRangoSemanaCompleto(lunes: LocalDate): String {
    val domingo = lunes.plus(DatePeriod(days = 6))
    val mesLunes = MESES_COMPLETOS.getOrElse(lunes.monthNumber - 1) { "" }
    val mesDomingo = MESES_COMPLETOS.getOrElse(domingo.monthNumber - 1) { "" }

    return if (lunes.monthNumber == domingo.monthNumber) {
        "Semana del ${lunes.dayOfMonth} al ${domingo.dayOfMonth} de $mesDomingo de ${domingo.year}."
    } else {
        "Semana del ${lunes.dayOfMonth} de $mesLunes al ${domingo.dayOfMonth} de $mesDomingo de ${domingo.year}."
    }
}

private fun formatearRangoSemanaCorto(lunes: LocalDate): String {
    val domingo = lunes.plus(DatePeriod(days = 6))
    val mesLunes = obtenerMesCorto(lunes.monthNumber)
    val mesDomingo = obtenerMesCorto(domingo.monthNumber)
    return if (lunes.monthNumber == domingo.monthNumber) {
        "${lunes.dayOfMonth} – ${domingo.dayOfMonth} $mesDomingo ${domingo.year}"
    } else {
        "${lunes.dayOfMonth} $mesLunes – ${domingo.dayOfMonth} $mesDomingo ${domingo.year}"
    }
}

data class MiHorarioState(
    val fechaInicioSemana: LocalDate = calcularLunesSemanaActual(),
    val rangoSemana: String = formatearRangoSemanaCompleto(fechaInicioSemana),
    val rangoSemanaCorto: String = formatearRangoSemanaCorto(fechaInicioSemana),
    val fechaPublicacion: String = "HORARIO PUBLICADO",
    val estaPublicado: Boolean = true,
    val totalHoras: String = "0 h 00 min",
    val turnos: List<TurnoAsignado> = emptyList(),
    val nombreEmpleado: String = "",
    val cargoEmpleado: String = ""
)

class MiHorarioViewModel(
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MiHorarioState())
    val uiState: StateFlow<MiHorarioState> = _uiState.asStateFlow()

    init {
        empleadoRepository.iniciarSincronizacionFirestore()
        turnoRepository.iniciarSincronizacionFirestore()
        observarAsignaciones()
    }

    private fun observarAsignaciones() {
        viewModelScope.launch {
            combine(
                turnoRepository.asignaciones,
                empleadoRepository.empleados
            ) { _, _ ->
                recalcularHorarioSemana(_uiState.value.fechaInicioSemana)
            }.collect { }
        }
    }

    fun cambiarSemana(deltaSemanas: Int) {
        val actual = _uiState.value.fechaInicioSemana
        val nueva = actual.plus(DatePeriod(days = deltaSemanas * 7))
        actualizarSemana(nueva)
    }

    fun seleccionarFechaSemana(fecha: LocalDate) {
        val diasDesdeLunes = fecha.dayOfWeek.ordinal
        val lunes = fecha.minus(DatePeriod(days = diasDesdeLunes))
        actualizarSemana(lunes)
    }

    private fun actualizarSemana(lunes: LocalDate) {
        _uiState.update {
            it.copy(
                fechaInicioSemana = lunes,
                rangoSemana = formatearRangoSemanaCompleto(lunes),
                rangoSemanaCorto = formatearRangoSemanaCorto(lunes)
            )
        }
        recalcularHorarioSemana(lunes)
    }

    private fun recalcularHorarioSemana(lunes: LocalDate) {
        val emp = resolverEmpleado()
        val empId = emp?.id ?: "emp_1"
        val empNombre = emp?.nombre ?: (SessionManager.usuarioActivo?.nombre ?: "")
        val empCargo = emp?.cargo?.displayName ?: (SessionManager.usuarioActivo?.cargo ?: "")
        val empSucursal = emp?.sucursal ?: (SessionManager.usuarioActivo?.sucursal ?: "CENTRO")
        val esAdmin = SessionManager.usuarioActivo?.rol != RolUsuario.EMPLEADO

        val fechasSemana = (0..6).map { lunes.plus(DatePeriod(days = it)) }
        val fechasIsoSemana = fechasSemana.map { it.toString() }.toSet()
        val nombresDias = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")

        val todasAsigs = turnoRepository.asignaciones.value

        // Asignaciones del empleado en la semana seleccionada
        val asigsEmp = todasAsigs.filter { asig ->
            (asig.empleadoId == empId || (asig.empleadoNombre.isNotBlank() && asig.empleadoNombre.equals(empNombre, ignoreCase = true))) &&
            asig.fechaIso in fechasIsoSemana
        }

        // Determinar si la semana está publicada para la sucursal/empleado
        val semanaPublicada = asigsEmp.any { it.estado.equals("PUBLICADA", ignoreCase = true) } ||
            todasAsigs.any { asig ->
                asig.fechaIso in fechasIsoSemana &&
                asig.estado.equals("PUBLICADA", ignoreCase = true) &&
                (asig.sucursalId.isBlank() || asig.sucursalId.equals(empSucursal, ignoreCase = true) || asig.sucursalId.replace(" ", "").equals(empSucursal.replace(" ", ""), ignoreCase = true))
            }

        var totalMinutos = 0

        val listaTurnos = (0..6).map { i ->
            val fechaDia = fechasSemana[i]
            val fechaIso = fechaDia.toString()
            val diaSem = nombresDias[i]
            val numDia = fechaDia.dayOfMonth.toString()

            val asig = asigsEmp.find { it.fechaIso == fechaIso }

            if (!semanaPublicada && !esAdmin) {
                // Si la semana no está publicada y el usuario es empleado, no se le muestran turnos no confirmados
                TurnoAsignado(
                    id = "sin_${fechaIso}",
                    diaSemana = diaSem,
                    numeroDia = numDia,
                    tipoTurno = null,
                    horario = null,
                    sucursal = null,
                    duracion = null,
                    sinAsignar = true
                )
            } else if (asig == null) {
                TurnoAsignado(
                    id = "sin_${fechaIso}",
                    diaSemana = diaSem,
                    numeroDia = numDia,
                    tipoTurno = null,
                    horario = null,
                    sucursal = null,
                    duracion = null,
                    sinAsignar = true
                )
            } else if (asig.tipoTurno.equals("Incapacidad", ignoreCase = true)) {
                TurnoAsignado(
                    id = asig.id.ifEmpty { "inc_${fechaIso}" },
                    diaSemana = diaSem,
                    numeroDia = numDia,
                    tipoTurno = "Incapacidad",
                    horario = "-",
                    sucursal = asig.sucursalId.ifEmpty { empSucursal },
                    duracion = "-",
                    esDescanso = false,
                    sinAsignar = false
                )
            } else if (asig.esDescanso) {
                TurnoAsignado(
                    id = asig.id.ifEmpty { "desc_${fechaIso}" },
                    diaSemana = diaSem,
                    numeroDia = numDia,
                    tipoTurno = "Descanso",
                    horario = "-",
                    sucursal = asig.sucursalId.ifEmpty { empSucursal },
                    duracion = "-",
                    esDescanso = true,
                    sinAsignar = false
                )
            } else {
                val (durMin, durStr) = calcularDuracion(asig.horario)
                totalMinutos += durMin
                val etiqueta = if (asig.modificadoTrasPublicar) "MODIFICADO TRAS PUBLICAR" else null
                TurnoAsignado(
                    id = asig.id.ifEmpty { "turno_${fechaIso}" },
                    diaSemana = diaSem,
                    numeroDia = numDia,
                    tipoTurno = asig.tipoTurno,
                    horario = asig.horario.replace("\n", " "),
                    sucursal = asig.sucursalId.ifEmpty { empSucursal },
                    duracion = durStr,
                    etiquetaModificado = etiqueta,
                    esDescanso = false,
                    sinAsignar = false
                )
            }
        }

        val totalHoras = totalMinutos / 60
        val totalMins = totalMinutos % 60
        val horasTexto = if (totalMins > 0) "$totalHoras h ${totalMins.toString().padStart(2, '0')} min" else "$totalHoras h 00 min"

        val textoPublicacion = if (semanaPublicada) {
            "HORARIO PUBLICADO Y CONFIRMADO"
        } else {
            "HORARIO EN BORRADOR · PENDIENTE DE PUBLICACIÓN"
        }

        _uiState.update {
            it.copy(
                turnos = listaTurnos,
                totalHoras = horasTexto,
                estaPublicado = semanaPublicada || esAdmin,
                fechaPublicacion = textoPublicacion,
                nombreEmpleado = empNombre,
                cargoEmpleado = empCargo
            )
        }
    }

    private fun resolverEmpleado(): Empleado? {
        val usuario = SessionManager.usuarioActivo
        val lista = empleadoRepository.empleados.value

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

        // Fallback para pruebas o desarrollo
        return lista.firstOrNull { it.estado == EstadoUsuario.ACTIVO }
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

    private fun calcularDuracion(horario: String): Pair<Int, String> {
        val limpio = horario.replace("\r", "").replace("\n", "").trim()
        val partes = limpio.split("-")
        if (partes.size == 2) {
            val ini = parsearMinutos(partes[0])
            val fin = parsearMinutos(partes[1])
            if (ini != null && fin != null) {
                var duracionMin = fin - ini
                if (duracionMin < 0) duracionMin += 24 * 60
                val duracionEfectiva = if (duracionMin >= 240) duracionMin - 30 else duracionMin
                val h = duracionEfectiva / 60
                val m = duracionEfectiva % 60
                val str = if (m > 0) "$h h ${m.toString().padStart(2, '0')} min" else "$h h"
                return Pair(duracionEfectiva, str)
            }
        }
        return Pair(450, "7 h 30 min")
    }
}
