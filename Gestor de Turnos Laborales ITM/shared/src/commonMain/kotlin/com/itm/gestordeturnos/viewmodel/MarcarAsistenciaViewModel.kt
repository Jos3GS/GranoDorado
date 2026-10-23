package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.AsistenciaRepository
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoAsistencia
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

data class MarcarAsistenciaState(
    val diaTexto: String = "TURNO DE HOY",
    val tipoTurno: String = "Sin turno asignado",
    val horario: String = "--:-- - --:--",
    val sucursal: String = "CENTRO",
    val estadoActual: EstadoAsistencia = EstadoAsistencia.SIN_INICIAR,
    val horaEntrada: String = "-",
    val horaSalida: String = "-",
    val inicioDescanso: String = "-",
    val finDescanso: String = "-",
    val esDescanso: Boolean = false,
    val tieneTurnoAsignado: Boolean = false,
    val empleadoNombre: String = "",
    val fechaIso: String = "",
    val mensajeAviso: String? = null
)

class MarcarAsistenciaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MarcarAsistenciaState())
    val uiState: StateFlow<MarcarAsistenciaState> = _uiState.asStateFlow()

    private var registroActualId: String = ""
    private var empleadoActivo: Empleado? = null
    private var asignacionDeHoy: AsignacionTurno? = null

    init {
        EmpleadoRepository.iniciarSincronizacionFirestore()
        TurnoRepository.iniciarSincronizacionFirestore()
        AsistenciaRepository.iniciarSincronizacionFirestore()
        observarDatos()
    }

    private fun observarDatos() {
        viewModelScope.launch {
            combine(
                TurnoRepository.asignaciones,
                EmpleadoRepository.empleados,
                AsistenciaRepository.registrosDiarios
            ) { asignaciones, empleados, registros ->
                Triple(asignaciones, empleados, registros)
            }.collect { (asignaciones, empleados, registros) ->
                recalcularEstadoHoy(asignaciones, empleados, registros)
            }
        }
    }

    private fun obtenerFechaHoy(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    private fun obtenerHoraActualTexto(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val h = now.hour.toString().padStart(2, '0')
        val m = now.minute.toString().padStart(2, '0')
        return "$h:$m"
    }

    private fun recalcularEstadoHoy(
        asignaciones: List<AsignacionTurno>,
        empleados: List<Empleado>,
        registros: List<RegistroAsistencia>
    ) {
        val emp = resolverEmpleado(empleados)
        empleadoActivo = emp
        val empId = emp?.id ?: "emp_1"
        val empNombre = emp?.let {
            if (it.apellido.isNotBlank()) "${it.nombre} ${it.apellido}".trim() else it.nombre
        } ?: (SessionManager.usuarioActivo?.nombre ?: "Empleado")
        val empSucursal = emp?.sucursal ?: (SessionManager.usuarioActivo?.sucursal ?: "CENTRO")

        val hoy = obtenerFechaHoy()
        val hoyIso = hoy.toString()
        val nombresDias = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")
        val diaSem = nombresDias.getOrElse(hoy.dayOfWeek.ordinal) { "" }
        val diaNum = hoy.dayOfMonth.toString()
        val diaTexto = "TURNO DE HOY · $diaSem $diaNum"

        registroActualId = "${empId}_${hoyIso}"

        // Buscar turno programado para hoy
        val asigHoy = asignaciones.find { asig ->
            (asig.empleadoId == empId || (asig.empleadoNombre.isNotBlank() && asig.empleadoNombre.equals(empNombre, ignoreCase = true))) &&
            asig.fechaIso == hoyIso
        }
        asignacionDeHoy = asigHoy

        // Buscar registro de asistencia de hoy en Firestore
        val registroHoy = registros.find {
            it.id == registroActualId || (it.empleadoId == empId && it.fechaIso == hoyIso)
        }

        val esDescanso = asigHoy?.esDescanso == true
        val tieneTurno = asigHoy != null && !esDescanso && asigHoy.tipoTurno.isNotBlank()
        val tipoTurnoTexto = when {
            esDescanso -> "Día de Descanso"
            tieneTurno -> asigHoy?.tipoTurno ?: "Turno General"
            else -> "Sin turno programado"
        }
        val horarioTexto = when {
            esDescanso -> "-"
            tieneTurno -> asigHoy?.horario?.replace("\n", " ") ?: "--:-- - --:--"
            else -> "--:-- - --:--"
        }
        val sucursalTexto = asigHoy?.sucursalId?.ifEmpty { empSucursal } ?: empSucursal

        // Si ya hay un registro de asistencia en Firestore para hoy, restaurar sus datos
        if (registroHoy != null) {
            _uiState.update {
                it.copy(
                    diaTexto = diaTexto,
                    tipoTurno = tipoTurnoTexto,
                    horario = horarioTexto,
                    sucursal = sucursalTexto,
                    estadoActual = registroHoy.estadoAsistencia,
                    horaEntrada = registroHoy.horaEntrada,
                    horaSalida = registroHoy.horaSalida,
                    inicioDescanso = registroHoy.inicioDescanso,
                    finDescanso = registroHoy.finDescanso,
                    esDescanso = esDescanso,
                    tieneTurnoAsignado = tieneTurno,
                    empleadoNombre = empNombre,
                    fechaIso = hoyIso,
                    mensajeAviso = when (registroHoy.estadoAsistencia) {
                        EstadoAsistencia.EN_TURNO -> "Marcaste entrada a las ${registroHoy.horaEntrada}"
                        EstadoAsistencia.EN_DESCANSO -> "En pausa de descanso desde las ${registroHoy.inicioDescanso}"
                        EstadoAsistencia.FINALIZADO -> "Jornada completada (${registroHoy.horaEntrada} - ${registroHoy.horaSalida})"
                        EstadoAsistencia.SIN_INICIAR -> null
                    }
                )
            }
        } else {
            // Aún no hay marcaciones para hoy
            _uiState.update {
                it.copy(
                    diaTexto = diaTexto,
                    tipoTurno = tipoTurnoTexto,
                    horario = horarioTexto,
                    sucursal = sucursalTexto,
                    estadoActual = EstadoAsistencia.SIN_INICIAR,
                    horaEntrada = "-",
                    horaSalida = "-",
                    inicioDescanso = "-",
                    finDescanso = "-",
                    esDescanso = esDescanso,
                    tieneTurnoAsignado = tieneTurno,
                    empleadoNombre = empNombre,
                    fechaIso = hoyIso,
                    mensajeAviso = if (esDescanso) "Hoy es tu día libre programado." else null
                )
            }
        }
    }

    fun marcarEntrada() {
        val horaActual = obtenerHoraActualTexto()
        val estadoActual = _uiState.value
        val empId = empleadoActivo?.id ?: "emp_1"
        val empNombre = estadoActual.empleadoNombre.ifBlank { "Empleado" }
        val hoyIso = estadoActual.fechaIso.ifBlank { obtenerFechaHoy().toString() }

        // Calcular minutos de retraso si corresponde
        val horarioProg = estadoActual.horario
        val minutosTarde = calcularRetraso(horarioProg, horaActual)
        val estadoRegistro = if (minutosTarde > 10) "TARDE" else "PRESENTE"

        val nuevoRegistro = RegistroAsistencia(
            id = registroActualId.ifBlank { "${empId}_${hoyIso}" },
            empleadoId = empId,
            empleado = empNombre,
            sucursal = estadoActual.sucursal,
            fechaIso = hoyIso,
            tipoTurno = estadoActual.tipoTurno,
            horarioProgramado = horarioProg,
            horaEntrada = horaActual,
            horaSalida = "-",
            inicioDescanso = "-",
            finDescanso = "-",
            estado = estadoRegistro,
            estadoAsistencia = EstadoAsistencia.EN_TURNO,
            tiempoEfectivoMinutos = 0,
            minutosTarde = minutosTarde,
            esDescanso = estadoActual.esDescanso
        )

        _uiState.update {
            it.copy(
                estadoActual = EstadoAsistencia.EN_TURNO,
                horaEntrada = horaActual,
                mensajeAviso = "Entrada registrada a las $horaActual ($estadoRegistro)"
            )
        }

        AsistenciaRepository.guardarRegistroAsistenciaEnBackground(nuevoRegistro)
    }

    fun iniciarDescanso() {
        val horaActual = obtenerHoraActualTexto()
        val estadoActual = _uiState.value
        val empId = empleadoActivo?.id ?: "emp_1"
        val hoyIso = estadoActual.fechaIso.ifBlank { obtenerFechaHoy().toString() }

        val registroActualizado = RegistroAsistencia(
            id = registroActualId.ifBlank { "${empId}_${hoyIso}" },
            empleadoId = empId,
            empleado = estadoActual.empleadoNombre,
            sucursal = estadoActual.sucursal,
            fechaIso = hoyIso,
            tipoTurno = estadoActual.tipoTurno,
            horarioProgramado = estadoActual.horario,
            horaEntrada = estadoActual.horaEntrada,
            horaSalida = estadoActual.horaSalida,
            inicioDescanso = horaActual,
            finDescanso = estadoActual.finDescanso,
            estado = "EN DESCANSO",
            estadoAsistencia = EstadoAsistencia.EN_DESCANSO,
            tiempoEfectivoMinutos = 0,
            esDescanso = estadoActual.esDescanso
        )

        _uiState.update {
            it.copy(
                estadoActual = EstadoAsistencia.EN_DESCANSO,
                inicioDescanso = horaActual,
                mensajeAviso = "Descanso iniciado a las $horaActual"
            )
        }

        AsistenciaRepository.guardarRegistroAsistenciaEnBackground(registroActualizado)
    }

    fun finalizarDescanso() {
        val horaActual = obtenerHoraActualTexto()
        val estadoActual = _uiState.value
        val empId = empleadoActivo?.id ?: "emp_1"
        val hoyIso = estadoActual.fechaIso.ifBlank { obtenerFechaHoy().toString() }

        val registroActualizado = RegistroAsistencia(
            id = registroActualId.ifBlank { "${empId}_${hoyIso}" },
            empleadoId = empId,
            empleado = estadoActual.empleadoNombre,
            sucursal = estadoActual.sucursal,
            fechaIso = hoyIso,
            tipoTurno = estadoActual.tipoTurno,
            horarioProgramado = estadoActual.horario,
            horaEntrada = estadoActual.horaEntrada,
            horaSalida = estadoActual.horaSalida,
            inicioDescanso = estadoActual.inicioDescanso,
            finDescanso = horaActual,
            estado = "PRESENTE",
            estadoAsistencia = EstadoAsistencia.EN_TURNO,
            tiempoEfectivoMinutos = 0,
            esDescanso = estadoActual.esDescanso
        )

        _uiState.update {
            it.copy(
                estadoActual = EstadoAsistencia.EN_TURNO,
                finDescanso = horaActual,
                mensajeAviso = "Descanso finalizado a las $horaActual. Turno reanudado."
            )
        }

        AsistenciaRepository.guardarRegistroAsistenciaEnBackground(registroActualizado)
    }

    fun marcarSalida() {
        val horaActual = obtenerHoraActualTexto()
        val estadoActual = _uiState.value
        val empId = empleadoActivo?.id ?: "emp_1"
        val hoyIso = estadoActual.fechaIso.ifBlank { obtenerFechaHoy().toString() }

        val minutosTrabajados = calcularMinutosTrabajados(
            entrada = estadoActual.horaEntrada,
            salida = horaActual,
            inicioDesc = estadoActual.inicioDescanso,
            finDesc = estadoActual.finDescanso
        )

        val registroFinal = RegistroAsistencia(
            id = registroActualId.ifBlank { "${empId}_${hoyIso}" },
            empleadoId = empId,
            empleado = estadoActual.empleadoNombre,
            sucursal = estadoActual.sucursal,
            fechaIso = hoyIso,
            tipoTurno = estadoActual.tipoTurno,
            horarioProgramado = estadoActual.horario,
            horaEntrada = estadoActual.horaEntrada,
            horaSalida = horaActual,
            inicioDescanso = estadoActual.inicioDescanso,
            finDescanso = estadoActual.finDescanso,
            estado = "FINALIZADO",
            estadoAsistencia = EstadoAsistencia.FINALIZADO,
            tiempoEfectivoMinutos = minutosTrabajados,
            esDescanso = estadoActual.esDescanso
        )

        _uiState.update {
            it.copy(
                estadoActual = EstadoAsistencia.FINALIZADO,
                horaSalida = horaActual,
                mensajeAviso = "Jornada finalizada a las $horaActual. ¡Buen descanso!"
            )
        }

        AsistenciaRepository.guardarRegistroAsistenciaEnBackground(registroFinal)
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

    private fun calcularRetraso(horarioProg: String, horaEntrada: String): Int {
        val partes = horarioProg.split("-")
        if (partes.size == 2) {
            val horaInicioProg = parsearMinutos(partes[0]) ?: return 0
            val horaEntradaMin = parsearMinutos(horaEntrada) ?: return 0
            val diff = horaEntradaMin - horaInicioProg
            return if (diff > 0) diff else 0
        }
        return 0
    }

    private fun calcularMinutosTrabajados(
        entrada: String,
        salida: String,
        inicioDesc: String,
        finDesc: String
    ): Int {
        val ini = parsearMinutos(entrada) ?: return 0
        val fin = parsearMinutos(salida) ?: return 0
        var total = fin - ini
        if (total < 0) total += 24 * 60

        val dIni = parsearMinutos(inicioDesc)
        val dFin = parsearMinutos(finDesc)
        if (dIni != null && dFin != null) {
            var durDesc = dFin - dIni
            if (durDesc < 0) durDesc += 24 * 60
            total = maxOf(0, total - durDesc)
        }
        return total
    }
}
