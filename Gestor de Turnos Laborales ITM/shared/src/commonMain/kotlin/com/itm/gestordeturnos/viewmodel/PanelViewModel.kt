package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.AsistenciaRepository
import com.itm.gestordeturnos.data.repository.CambiosTurnoRepository
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.NovedadRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoNovedad
import com.itm.gestordeturnos.domain.model.EstadoSolicitudCambio
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.model.NotificacionIncumplimiento
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import com.itm.gestordeturnos.domain.model.UsuarioActual
import com.itm.gestordeturnos.domain.repository.AsistenciaRepositoryContract
import com.itm.gestordeturnos.domain.repository.CambiosTurnoRepositoryContract
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.NovedadRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

data class SucursalMetrica(
    val nombre: String,
    val empleadosActivos: Int,
    val turnosHoy: Int,
    val enTurnoAhora: Int,
    val novedadesPendientes: Int,
    val estadoMalla: String
)

data class TareaRonda(
    val texto: String,
    val ruta: String,
    val tipo: String = "INFO" // "URGENTE", "PENDIENTE", "INFO"
)

data class ResumenAsistenciaHoy(
    val turnosProgramados: Int = 0,
    val enTurnoAhora: Int = 0,
    val aTiempo: Int = 0,
    val conRetraso: Int = 0,
    val pendientesInicio: Int = 0,
    val novedadesHoy: Int = 0
)

data class PanelState(
    val saludo: String = "BUENOS DÍAS",
    val fechaHoyTexto: String = "",
    val horaActualTexto: String = "",
    val semanaEnCurso: String = "",
    val estadoMalla: String = "La malla semanal está en borrador",
    val esMallaPublicada: Boolean = false,
    val sucursalSeleccionada: String = "TODAS",
    val sucursalesDisponibles: List<String> = listOf("TODAS", "CENTRO", "NORTE", "SUR"),
    val esAdminGeneral: Boolean = true,
    val sucursalUsuario: String = "CENTRO",

    // Métricas principales
    val empleadosActivos: Int = 0,
    val turnosCatalogo: Int = 0,
    val novedadesPendientes: Int = 0,
    val cambiosPorAprobar: Int = 0,
    val turnosHoyTotal: Int = 0,
    val enTurnoAhoraTotal: Int = 0,
    val incumplimientosHoy: Int = 0,

    // Resumen Operativo de Hoy
    val asistenciaHoy: ResumenAsistenciaHoy = ResumenAsistenciaHoy(),

    // Desglose por sucursales (Para Admin General)
    val metricasPorSucursal: List<SucursalMetrica> = emptyList(),

    // Acciones prioritarias / Ronda del día interactivas
    val rondaDelDia: List<TareaRonda> = emptyList()
)

class PanelViewModel(
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository,
    private val novedadRepository: NovedadRepositoryContract = NovedadRepository,
    private val cambiosTurnoRepository: CambiosTurnoRepositoryContract = CambiosTurnoRepository,
    private val asistenciaRepository: AsistenciaRepositoryContract = AsistenciaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PanelState())
    val uiState: StateFlow<PanelState> = _uiState.asStateFlow()

    private val _sucursalFiltro = MutableStateFlow("TODAS")

    init {
        empleadoRepository.iniciarSincronizacionFirestore()
        turnoRepository.iniciarSincronizacionFirestore()
        novedadRepository.iniciarSincronizacionFirestore()
        cambiosTurnoRepository.iniciarSincronizacionFirestore()
        asistenciaRepository.iniciarSincronizacionFirestore()
    }

    fun cargarDatos(usuario: UsuarioActual) {
        val esAdminGen = usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL
        val sucUsuario = usuario.sucursal.ifBlank { "CENTRO" }
        _sucursalFiltro.value = if (esAdminGen) "TODAS" else sucUsuario

        viewModelScope.launch {
            val flujo1 = combine(
                empleadoRepository.empleados,
                turnoRepository.turnosCatalogo,
                turnoRepository.asignaciones
            ) { emp, tur, asig -> Triple(emp, tur, asig) }

            val flujo2 = combine(
                novedadRepository.novedades,
                cambiosTurnoRepository.solicitudes,
                asistenciaRepository.registrosDiarios
            ) { nov, sol, reg -> Triple(nov, sol, reg) }

            val flujo3 = combine(
                asistenciaRepository.notificaciones,
                _sucursalFiltro
            ) { notif, filtro -> Pair(notif, filtro) }

            combine(flujo1, flujo2, flujo3) { g1, g2, g3 ->
                recalcularPanel(
                    usuario = usuario,
                    sucursalFiltro = g3.second,
                    empleados = g1.first,
                    turnos = g1.second,
                    asignaciones = g1.third,
                    novedades = g2.first,
                    solicitudes = g2.second,
                    registros = g2.third,
                    notificaciones = g3.first
                )
            }.collect { nuevoEstado ->
                _uiState.value = nuevoEstado
            }
        }
    }

    fun cambiarFiltroSucursal(sucursal: String) {
        _sucursalFiltro.value = sucursal
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

    private fun extraerRangoHorario(horarioStr: String): Pair<Int, Int>? {
        val partes = horarioStr.split("-")
        if (partes.size == 2) {
            val ini = parsearMinutos(partes[0].trim())
            val fin = parsearMinutos(partes[1].trim())
            if (ini != null && fin != null) {
                return Pair(ini, fin)
            }
        }
        return null
    }

    private fun recalcularPanel(
        usuario: UsuarioActual,
        sucursalFiltro: String,
        empleados: List<Empleado>,
        turnos: List<TurnoTemplate>,
        asignaciones: List<AsignacionTurno>,
        novedades: List<Novedad>,
        solicitudes: List<SolicitudCambioTurno>,
        registros: List<RegistroAsistencia>,
        notificaciones: List<NotificacionIncumplimiento>
    ): PanelState {
        val esAdminGen = usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL
        val sucUsuario = usuario.sucursal.ifBlank { "CENTRO" }
        val sucursalActual = if (esAdminGen) sucursalFiltro else sucUsuario

        val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val hoyIso = hoy.toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val horaActualStr = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
        val minutosActuales = now.hour * 60 + now.minute

        val saludo = when {
            now.hour < 12 -> "BUENOS DÍAS"
            now.hour < 18 -> "BUENAS TARDES"
            else -> "BUENAS NOCHES"
        }

        val nombresDias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val nombresMeses = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val diaSemana = nombresDias.getOrElse(hoy.dayOfWeek.ordinal) { "" }
        val mesNombre = nombresMeses.getOrElse(hoy.monthNumber - 1) { "" }
        val fechaHoyTexto = "$diaSemana, ${hoy.dayOfMonth} $mesNombre ${hoy.year}"

        // Calcular semana en curso (Lunes a Domingo)
        val diaSemNum = (hoy.dayOfWeek.ordinal) % 7
        val lunesSemana = LocalDate.fromEpochDays(hoy.toEpochDays() - diaSemNum)
        val domingoSemana = lunesSemana.plus(DatePeriod(days = 6))
        val mesLunes = nombresMeses.getOrElse(lunesSemana.monthNumber - 1) { "" }
        val mesDomingo = nombresMeses.getOrElse(domingoSemana.monthNumber - 1) { "" }
        val semanaEnCursoTexto = if (lunesSemana.monthNumber == domingoSemana.monthNumber) {
            "SEMANA EN CURSO ${lunesSemana.dayOfMonth} - ${domingoSemana.dayOfMonth} ${mesDomingo.uppercase()} ${domingoSemana.year}"
        } else {
            "SEMANA EN CURSO ${lunesSemana.dayOfMonth} $mesLunes - ${domingoSemana.dayOfMonth} $mesDomingo ${domingoSemana.year}".uppercase()
        }

        // Filtro de sucursal helper
        fun coincideSucursal(suc: String): Boolean {
            return sucursalActual.equals("TODAS", ignoreCase = true) ||
                   suc.isBlank() ||
                   suc.equals(sucursalActual, ignoreCase = true)
        }

        // 1. Empleados Activos en el alcance
        val empleadosEnAlcance = empleados.filter { emp ->
            emp.estado == EstadoUsuario.ACTIVO && coincideSucursal(emp.sucursal)
        }
        val empActivosCount = empleadosEnAlcance.size

        // 2. Turnos en catálogo activos
        val turnosActivos = turnos.count { it.estado.equals("ACTIVO", ignoreCase = true) }

        // 3. Novedades pendientes de revisión
        val novedadesEnAlcance = novedades.filter { nov ->
            coincideSucursal(nov.sucursal)
        }
        val novPendientes = novedadesEnAlcance.count { it.estado == EstadoNovedad.REPORTADA }

        // 4. Cambios por aprobar
        val cambiosEnAlcance = solicitudes.filter { sol ->
            coincideSucursal(sol.sucursal)
        }
        val cambiosPendientes = cambiosEnAlcance.count {
            it.estado == EstadoSolicitudCambio.PENDIENTE || it.estado == EstadoSolicitudCambio.ACEPTADA_COMPANERO
        }

        // 5. Asignaciones de hoy
        val asigsHoy = asignaciones.filter { asig ->
            asig.fechaIso == hoyIso && !asig.esDescanso
        }
        val asigsHoyEnAlcance = asigsHoy.filter { asig ->
            val emp = empleados.find { it.id == asig.empleadoId }
            val suc = emp?.sucursal ?: ""
            coincideSucursal(suc)
        }
        val turnosHoyCount = asigsHoyEnAlcance.size

        // 6. En turno ahora mismo
        var enTurnoAhoraCount = 0
        for (asig in asigsHoyEnAlcance) {
            val rango = extraerRangoHorario(asig.horario)
            if (rango != null && minutosActuales in rango.first..rango.second) {
                enTurnoAhoraCount++
            }
        }

        // 7. Estado de la malla semanal para el alcance
        val fechasSemanaIso = (0..6).map { i -> lunesSemana.plus(DatePeriod(days = i)).toString() }
        val asigsSemanaAlcance = asignaciones.filter { asig ->
            asig.fechaIso in fechasSemanaIso && asig.empleadoId in empleadosEnAlcance.map { it.id }
        }
        val esMallaPublicada = asigsSemanaAlcance.isNotEmpty() && asigsSemanaAlcance.any {
            it.estado.equals("PUBLICADA", ignoreCase = true)
        }
        val estadoMallaTexto = if (esMallaPublicada) {
            "La malla semanal está publicada"
        } else {
            "La malla semanal está en borrador"
        }

        // 8. Resumen Asistencia Hoy
        val registrosHoyAlcance = registros.filter { reg ->
            reg.fechaIso == hoyIso && coincideSucursal(reg.sucursal)
        }
        val aTiempoCount = registrosHoyAlcance.count { it.minutosTarde <= 0 && it.estado != "TARDE" }
        val retrasoCount = registrosHoyAlcance.count { it.minutosTarde > 0 || it.estado == "TARDE" }
        val empleadosConMarcaIds = registrosHoyAlcance.map { it.empleadoId }.toSet()
        val pendientesInicioCount = asigsHoyEnAlcance.count { it.empleadoId !in empleadosConMarcaIds }

        val novedadesHoyCount = novedadesEnAlcance.count {
            it.fechaReferencia == hoyIso || it.fechaCreacionIso == hoyIso
        }

        val resumenAsistencia = ResumenAsistenciaHoy(
            turnosProgramados = turnosHoyCount,
            enTurnoAhora = enTurnoAhoraCount,
            aTiempo = aTiempoCount,
            conRetraso = retrasoCount,
            pendientesInicio = pendientesInicioCount,
            novedadesHoy = novedadesHoyCount
        )

        // 9. Incumplimientos no atendidos hoy
        val incumplimientosHoyCount = notificaciones.count { notif ->
            notif.fechaIso == hoyIso && !notif.atendida && coincideSucursal(notif.sucursal)
        }

        // 10. Desglose por sucursales (Para Administrador General)
        val sucursalesList = listOf("CENTRO", "NORTE", "SUR")
        val metricasSucursales = sucursalesList.map { sucNombre ->
            val empSuc = empleados.count { it.estado == EstadoUsuario.ACTIVO && it.sucursal.equals(sucNombre, ignoreCase = true) }
            val asigSucHoy = asigsHoy.filter { asig ->
                val emp = empleados.find { it.id == asig.empleadoId }
                emp?.sucursal.equals(sucNombre, ignoreCase = true)
            }
            val turnosHoySuc = asigSucHoy.size
            val enTurnoSuc = asigSucHoy.count { asig ->
                val rango = extraerRangoHorario(asig.horario)
                rango != null && minutosActuales in rango.first..rango.second
            }
            val novSuc = novedades.count {
                it.sucursal.equals(sucNombre, ignoreCase = true) && it.estado == EstadoNovedad.REPORTADA
            }
            val asigsSemanaSuc = asignaciones.filter { asig ->
                val emp = empleados.find { it.id == asig.empleadoId }
                asig.fechaIso in fechasSemanaIso && emp?.sucursal.equals(sucNombre, ignoreCase = true)
            }
            val pub = asigsSemanaSuc.isNotEmpty() && asigsSemanaSuc.any { it.estado.equals("PUBLICADA", ignoreCase = true) }

            SucursalMetrica(
                nombre = sucNombre,
                empleadosActivos = empSuc,
                turnosHoy = turnosHoySuc,
                enTurnoAhora = enTurnoSuc,
                novedadesPendientes = novSuc,
                estadoMalla = if (pub) "PUBLICADA" else "BORRADOR"
            )
        }

        // 11. Ronda del día con acciones prioritarias
        val ronda = mutableListOf<TareaRonda>()
        if (novPendientes > 0) {
            ronda.add(TareaRonda("$novPendientes novedades esperan revisión", "novedades", "URGENTE"))
        } else {
            ronda.add(TareaRonda("Todas las novedades al día", "novedades", "INFO"))
        }

        if (cambiosPendientes > 0) {
            ronda.add(TareaRonda("$cambiosPendientes solicitudes de cambio por decidir", "cambios", "PENDIENTE"))
        } else {
            ronda.add(TareaRonda("Sin solicitudes de cambio pendientes", "cambios", "INFO"))
        }

        if (incumplimientosHoyCount > 0) {
            ronda.add(TareaRonda("$incumplimientosHoyCount alertas de asistencia hoy sin justificar", "asistencia", "URGENTE"))
        } else if (retrasoCount > 0) {
            ronda.add(TareaRonda("$retrasoCount retrasos registrados en la jornada de hoy", "asistencia", "PENDIENTE"))
        }

        if (!esMallaPublicada) {
            ronda.add(TareaRonda("Malla semanal en borrador: verificar y publicar", "malla", "PENDIENTE"))
        } else {
            ronda.add(TareaRonda("Malla semanal publicada y en operación normal", "malla", "INFO"))
        }

        return PanelState(
            saludo = saludo,
            fechaHoyTexto = fechaHoyTexto,
            horaActualTexto = horaActualStr,
            semanaEnCurso = semanaEnCursoTexto,
            estadoMalla = estadoMallaTexto,
            esMallaPublicada = esMallaPublicada,
            sucursalSeleccionada = sucursalActual,
            esAdminGeneral = esAdminGen,
            sucursalUsuario = sucUsuario,
            empleadosActivos = empActivosCount,
            turnosCatalogo = turnosActivos,
            novedadesPendientes = novPendientes,
            cambiosPorAprobar = cambiosPendientes,
            turnosHoyTotal = turnosHoyCount,
            enTurnoAhoraTotal = enTurnoAhoraCount,
            incumplimientosHoy = incumplimientosHoyCount,
            asistenciaHoy = resumenAsistencia,
            metricasPorSucursal = metricasSucursales,
            rondaDelDia = ronda
        )
    }
}
