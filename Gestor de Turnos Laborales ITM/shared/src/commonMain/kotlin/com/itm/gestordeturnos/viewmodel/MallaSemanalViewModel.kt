package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.DiaColumnaMalla
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.FilaMalla
import com.itm.gestordeturnos.domain.model.TipoCeldaMalla
import com.itm.gestordeturnos.domain.model.TurnoCelda
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class MallaSemanalState(
    val titulo: String = "Malla semanal",
    val subtitulo: String = "Verifica que la jornada de 06:00 a 22:00 queda cubierta antes de publicar. Los vacíos de cobertura se resaltan.",
    val fechaInicioSemana: LocalDate = LocalDate(2026, 9, 14),
    val rangoSemana: String = "14 – 20 sep 2026",
    val sucursal: String = "Centro",
    val opcionesSucursal: List<String> = listOf("Centro", "Norte", "Sur"),
    val estadoMalla: String = "BORRADOR - NO VISIBLE PARA LOS EMPLEADOS",
    val diasSemana: List<DiaColumnaMalla> = listOf(
        DiaColumnaMalla("LUN", "14", cubierto = true),
        DiaColumnaMalla("MAR", "15", cubierto = true),
        DiaColumnaMalla("MIÉ", "16", cubierto = true),
        DiaColumnaMalla("JUE", "17", cubierto = true),
        DiaColumnaMalla("VIE", "18", cubierto = true),
        DiaColumnaMalla("SÁB", "19", cubierto = false), // SIN CUBRIR
        DiaColumnaMalla("DOM", "20", cubierto = true)
    ),
    val filas: List<FilaMalla> = emptyList(),
    val turnosCatalogoActivos: List<TurnoTemplate> = emptyList(),
    val publicado: Boolean = false
)

class MallaSemanalViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MallaSemanalState())
    val uiState: StateFlow<MallaSemanalState> = _uiState.asStateFlow()

    // Memoria de turnos asignados por empleado (idEmpleado -> lista de 7 celdas)
    private val turnosPorEmpleado = mutableMapOf<String, MutableList<TurnoCelda>>()

    // Colección de todos los empleados en la base de datos (sincronizados desde Firestore / EmpleadoRepository)
    private var todosLosEmpleados: List<Empleado> = emptyList()

    init {
        // Cargar en memoria inicial asignaciones previas si existen
        MockDataProviders.inicialFilasMalla().forEach { fila ->
            turnosPorEmpleado[fila.idEmpleado] = fila.turnos.toMutableList()
        }

        // Conectar y sincronizar con Firestore
        EmpleadoRepository.iniciarSincronizacionFirestore()
        TurnoRepository.iniciarSincronizacionFirestore()

        observarEmpleadosBackend()
        observarTurnosCatalogo()
    }

    private fun observarEmpleadosBackend() {
        viewModelScope.launch {
            EmpleadoRepository.empleados.collect { listaEmpleados ->
                todosLosEmpleados = listaEmpleados
                actualizarFilasPorSucursal(_uiState.value.sucursal)
            }
        }
    }

    private fun observarTurnosCatalogo() {
        viewModelScope.launch {
            TurnoRepository.turnosCatalogo.collect { catalogo ->
                val activos = catalogo.filter { it.estado.equals("ACTIVO", ignoreCase = true) }
                _uiState.update { it.copy(turnosCatalogoActivos = activos) }
            }
        }
    }

    fun onSucursalCambio(nuevaSucursal: String) {
        _uiState.update { it.copy(sucursal = nuevaSucursal) }
        actualizarFilasPorSucursal(nuevaSucursal)
    }

    fun publicarSemana() {
        _uiState.update {
            it.copy(
                publicado = true,
                estadoMalla = "PUBLICADA - VISIBLE PARA LOS EMPLEADOS"
            )
        }
    }

    fun seleccionarFechaSemana(fecha: LocalDate) {
        val diasDesdeLunes = fecha.dayOfWeek.ordinal
        val lunes = fecha.minus(DatePeriod(days = diasDesdeLunes))
        actualizarSemana(lunes)
    }

    fun cambiarSemana(deltaSemanas: Int) {
        val actual = _uiState.value.fechaInicioSemana
        val nueva = actual.plus(DatePeriod(days = deltaSemanas * 7))
        actualizarSemana(nueva)
    }

    private fun actualizarFilasPorSucursal(sucursal: String) {
        // Filtrar estrictamente solo empleados ACTIVOS de la base de datos que pertenezcan a la sucursal actual
        val empleadosFiltrados = todosLosEmpleados.filter { emp ->
            esEmpleadoActivo(emp) && perteneceASucursal(emp, sucursal)
        }

        val nuevasFilas = empleadosFiltrados.map { emp ->
            val turnosEmpleado = turnosPorEmpleado.getOrPut(emp.id) {
                MutableList(7) {
                    TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR)
                }
            }

            val nombreCompleto = if (emp.apellido.isNotBlank()) {
                "${emp.nombre} ${emp.apellido}".trim()
            } else {
                emp.nombre
            }

            val cargoStr = emp.cargo.displayName.uppercase()

            FilaMalla(
                idEmpleado = emp.id,
                nombre = nombreCompleto,
                cargo = cargoStr,
                turnos = turnosEmpleado.toList(),
                totalHoras = calcularTotalHoras(turnosEmpleado)
            )
        }

        _uiState.update { state ->
            val nuevosDias = recalcularDiasSemana(state.fechaInicioSemana, nuevasFilas)
            state.copy(filas = nuevasFilas, diasSemana = nuevosDias)
        }
    }

    private fun esEmpleadoActivo(emp: Empleado): Boolean {
        return emp.estado == EstadoUsuario.ACTIVO ||
               emp.estado.name.equals("ACTIVO", ignoreCase = true) ||
               emp.estado.displayName.equals("Activo", ignoreCase = true)
    }

    private fun perteneceASucursal(emp: Empleado, sucursal: String): Boolean {
        val sucLimpia = sucursal.trim()
        val empSucursal = emp.sucursal.trim()
        val empSucursalId = emp.sucursalId.trim()
        return empSucursal.equals(sucLimpia, ignoreCase = true) ||
               empSucursalId.equals(sucLimpia, ignoreCase = true) ||
               empSucursal.replace(" ", "").equals(sucLimpia.replace(" ", ""), ignoreCase = true)
    }

    fun asignarTurnoAEmpleado(
        idEmpleado: String,
        diaIndex: Int,
        turnoTemplate: TurnoTemplate
    ) {
        val horarioFormateado = if (turnoTemplate.horaInicio.isNotBlank() && turnoTemplate.horaFin.isNotBlank()) {
            "${turnoTemplate.horaInicio}-${turnoTemplate.horaFin}"
        } else {
            turnoTemplate.horario.replace("\n", "")
        }

        val celdas = turnosPorEmpleado.getOrPut(idEmpleado) {
            MutableList(7) { TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR) }
        }
        if (diaIndex in celdas.indices) {
            celdas[diaIndex] = TurnoCelda(
                tipo = turnoTemplate.nombre,
                horario = horarioFormateado,
                tipoCelda = TipoCeldaMalla.TURNO,
                modificadoTrasPublicar = _uiState.value.publicado
            )
        }

        actualizarFilasPorSucursal(_uiState.value.sucursal)
    }

    fun asignarDescansoAEmpleado(
        idEmpleado: String,
        diaIndex: Int
    ) {
        val celdas = turnosPorEmpleado.getOrPut(idEmpleado) {
            MutableList(7) { TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR) }
        }
        if (diaIndex in celdas.indices) {
            celdas[diaIndex] = TurnoCelda(
                tipo = "Descanso",
                horario = "-",
                tipoCelda = TipoCeldaMalla.DESCANSO,
                esDescanso = true
            )
        }

        actualizarFilasPorSucursal(_uiState.value.sucursal)
    }

    private fun recalcularDiasSemana(
        lunes: LocalDate,
        filas: List<FilaMalla>
    ): List<DiaColumnaMalla> {
        val nombresDias = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")
        return (0..6).map { i ->
            val diaFecha = lunes.plus(DatePeriod(days = i))
            val estaCubierto = verificarDiaCubierto(diaIndex = i, filas = filas)
            DiaColumnaMalla(
                diaSemana = nombresDias[i],
                numeroDia = "${diaFecha.dayOfMonth}",
                cubierto = estaCubierto
            )
        }
    }

    private fun verificarDiaCubierto(diaIndex: Int, filas: List<FilaMalla>): Boolean {
        val jornadaInicio = 6 * 60
        val jornadaFin = 22 * 60

        val intervalos = mutableListOf<Pair<Int, Int>>()

        for (fila in filas) {
            if (diaIndex in fila.turnos.indices) {
                val turno = fila.turnos[diaIndex]
                if (turno.tipoCelda == TipoCeldaMalla.TURNO) {
                    var ini: Int? = null
                    var fin: Int? = null

                    val partes = turno.horario.split("-")
                    if (partes.size == 2) {
                        ini = parsearMinutos(partes[0])
                        fin = parsearMinutos(partes[1])
                    }

                    if (ini == null || fin == null) {
                        when (turno.tipo.lowercase().trim()) {
                            "apertura" -> { ini = 6 * 60; fin = 14 * 60 }
                            "intermedio" -> { ini = 10 * 60; fin = 18 * 60 }
                            "cierre" -> { ini = 14 * 60; fin = 22 * 60 }
                        }
                    }

                    if (ini != null && fin != null) {
                        if (fin > ini) {
                            val s = maxOf(jornadaInicio, ini)
                            val e = minOf(jornadaFin, fin)
                            if (e > s) intervalos.add(s to e)
                        } else {
                            val s1 = maxOf(jornadaInicio, ini)
                            val e1 = minOf(jornadaFin, 1440)
                            if (e1 > s1) intervalos.add(s1 to e1)
                            val s2 = maxOf(jornadaInicio, 0)
                            val e2 = minOf(jornadaFin, fin)
                            if (e2 > s2) intervalos.add(s2 to e2)
                        }
                    }
                }
            }
        }

        if (intervalos.isEmpty()) return false

        val ordenados = intervalos.sortedBy { it.first }

        var finAlcanzado = jornadaInicio
        for ((start, end) in ordenados) {
            if (start > finAlcanzado) {
                return false
            }
            finAlcanzado = maxOf(finAlcanzado, end)
            if (finAlcanzado >= jornadaFin) {
                return true
            }
        }

        return finAlcanzado >= jornadaFin
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

    private fun calcularTotalHoras(turnos: List<TurnoCelda>): String {
        var minutosTotales = 0
        turnos.forEach { t ->
            if (t.tipoCelda == TipoCeldaMalla.TURNO) {
                val partes = t.horario.split("-")
                if (partes.size == 2) {
                    val ini = parsearMinutos(partes[0])
                    val fin = parsearMinutos(partes[1])
                    if (ini != null && fin != null) {
                        var duracionMin = fin - ini
                        if (duracionMin < 0) duracionMin += 24 * 60
                        val duracionEfectiva = if (duracionMin >= 240) duracionMin - 30 else duracionMin
                        minutosTotales += duracionEfectiva
                    } else {
                        minutosTotales += 450
                    }
                } else {
                    minutosTotales += 450
                }
            }
        }
        val horas = minutosTotales / 60
        val minutos = minutosTotales % 60
        return if (minutos > 0) {
            "$horas h ${minutos.toString().padStart(2, '0')} min"
        } else {
            "$horas h 00 min"
        }
    }

    private fun actualizarSemana(lunes: LocalDate) {
        val domingo = lunes.plus(DatePeriod(days = 6))
        val nuevosDias = recalcularDiasSemana(lunes, _uiState.value.filas)

        val rangoTexto = if (lunes.monthNumber == domingo.monthNumber) {
            "${lunes.dayOfMonth} – ${domingo.dayOfMonth} ${obtenerMesCorto(domingo.monthNumber)} ${domingo.year}"
        } else {
            "${lunes.dayOfMonth} ${obtenerMesCorto(lunes.monthNumber)} – ${domingo.dayOfMonth} ${obtenerMesCorto(domingo.monthNumber)} ${domingo.year}"
        }

        _uiState.update {
            it.copy(
                fechaInicioSemana = lunes,
                rangoSemana = rangoTexto,
                diasSemana = nuevosDias
            )
        }
    }

    private fun obtenerMesCorto(mes: Int): String = when (mes) {
        1 -> "ene"
        2 -> "feb"
        3 -> "mar"
        4 -> "abr"
        5 -> "may"
        6 -> "jun"
        7 -> "jul"
        8 -> "ago"
        9 -> "sep"
        10 -> "oct"
        11 -> "nov"
        12 -> "dic"
        else -> ""
    }
}
