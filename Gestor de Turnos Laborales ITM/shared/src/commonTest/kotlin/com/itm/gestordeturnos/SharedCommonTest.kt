package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.*
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import com.itm.gestordeturnos.domain.usecase.AplicarIncapacidadEnTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.CalcularDescansosTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.IntercambiarTurnosUseCase
import com.itm.gestordeturnos.domain.usecase.ValidacionTurnoResult
import com.itm.gestordeturnos.domain.usecase.VerificarCoberturaDiariaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Suite de pruebas unitarias determinísticas de la capa de Dominio (Fase 3).
 * Verifica reglas de negocio, cobertura operativa (06:00 - 22:00), normativa de descansos,
 * intercambio de turnos y aplicación de incapacidades médicas usando Fakes en memoria.
 */
class SharedCommonTest {

    private val verificarCoberturaUseCase = VerificarCoberturaDiariaUseCase()
    private val calcularDescansosUseCase = CalcularDescansosTurnoUseCase()

    // =========================================================================
    // 1. PRUEBAS DE COBERTURA DIARIA Y CÁLCULO DE HORAS SEMANALES
    // =========================================================================

    @Test
    fun verificarDiaCubierto_cuandoAperturaYcierreCubren06a22_retornaTrue() {
        val filaApertura = FilaMalla(
            idEmpleado = "emp_1",
            nombre = "Carlos Ruiz",
            cargo = "BARISTA",
            turnos = listOf(
                TurnoCelda(tipo = "Apertura", horario = "06:00-14:00", tipoCelda = TipoCeldaMalla.TURNO)
            ),
            totalHoras = "7 h 30 min"
        )
        val filaCierre = FilaMalla(
            idEmpleado = "emp_2",
            nombre = "Laura Mejía",
            cargo = "BARISTA",
            turnos = listOf(
                TurnoCelda(tipo = "Cierre", horario = "14:00-22:00", tipoCelda = TipoCeldaMalla.TURNO)
            ),
            totalHoras = "7 h 30 min"
        )

        val cubierto = verificarCoberturaUseCase.verificarDiaCubierto(
            diaIndex = 0,
            filas = listOf(filaApertura, filaCierre)
        )

        assertTrue(cubierto, "La franja 06:00-14:00 + 14:00-22:00 debe cubrir el 100% de 06:00 a 22:00")
    }

    @Test
    fun verificarDiaCubierto_cuandoExisteHuecoOperativoEntreTurnos_retornaFalse() {
        val filaAperturaCorta = FilaMalla(
            idEmpleado = "emp_1",
            nombre = "Carlos Ruiz",
            cargo = "BARISTA",
            turnos = listOf(
                TurnoCelda(tipo = "Apertura", horario = "06:00-12:00", tipoCelda = TipoCeldaMalla.TURNO)
            ),
            totalHoras = "5 h 30 min"
        )
        val filaCierre = FilaMalla(
            idEmpleado = "emp_2",
            nombre = "Laura Mejía",
            cargo = "BARISTA",
            turnos = listOf(
                TurnoCelda(tipo = "Cierre", horario = "14:00-22:00", tipoCelda = TipoCeldaMalla.TURNO)
            ),
            totalHoras = "7 h 30 min"
        )

        val cubierto = verificarCoberturaUseCase.verificarDiaCubierto(
            diaIndex = 0,
            filas = listOf(filaAperturaCorta, filaCierre)
        )

        assertFalse(cubierto, "El hueco entre las 12:00 y las 14:00 debe marcar el día como descubierto")
    }

    @Test
    fun calcularTotalHoras_descuenta30MinutosPorTurnosDeCuatroOMasHoras() {
        val turnosSemana = listOf(
            TurnoCelda(tipo = "Apertura", horario = "06:00-14:00", tipoCelda = TipoCeldaMalla.TURNO), // 8h brutas -> 7h 30m efectivas
            TurnoCelda(tipo = "Cierre", horario = "14:00-22:00", tipoCelda = TipoCeldaMalla.TURNO),   // 8h brutas -> 7h 30m efectivas
            TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO)          // 0h
        )

        val total = verificarCoberturaUseCase.calcularTotalHoras(turnosSemana)
        assertEquals("15 h 00 min", total)
    }

    @Test
    fun recalcularDiasSemana_generaSieteColumnasConFechasCorrectas() {
        val lunes = LocalDate(2026, 10, 26)
        val columnas = verificarCoberturaUseCase.recalcularDiasSemana(lunes = lunes, filas = emptyList())

        assertEquals(7, columnas.size)
        assertEquals("LUN", columnas[0].diaSemana)
        assertEquals("26", columnas[0].numeroDia)
        assertEquals("DOM", columnas[6].diaSemana)
        assertEquals("1", columnas[6].numeroDia)
        assertTrue(columnas.all { !it.cubierto })
    }

    // =========================================================================
    // 2. PRUEBAS DE VALIDACIÓN LABORAL DE TURNOS Y DESCANSOS
    // =========================================================================

    @Test
    fun validarYCalcularTurno_rechazaTurnoMayorADiezHoras() {
        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Jornada Extendida",
            horaInicio = "06:00",
            horaFin = "17:00", // 11 horas (660 min > 600 min)
            descansos = emptyList()
        )

        val error = assertIs<ValidacionTurnoResult.Error>(resultado)
        assertTrue(error.mensaje.contains("10 horas"))
    }

    @Test
    fun validarYCalcularTurno_rechazaTurnoMayorA2h45mSinDescansos() {
        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Apertura Sin Pausas",
            horaInicio = "06:00",
            horaFin = "10:00", // 4 horas sin descansos (> 165 min)
            descansos = emptyList()
        )

        val error = assertIs<ValidacionTurnoResult.Error>(resultado)
        assertTrue(error.mensaje.contains("2 h 45 min"))
    }

    @Test
    fun generarDescansosAutomaticos_yValidarTurnoDeOchoHoras_esExitoso() {
        val descansosGenerados = calcularDescansosUseCase.generarDescansosAutomaticos(
            inicioStr = "06:00",
            finStr = "14:00"
        )

        assertTrue(descansosGenerados.isNotEmpty(), "Un turno de 8 horas debe generar descansos automáticos")

        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Apertura",
            horaInicio = "06:00",
            horaFin = "14:00",
            descansos = descansosGenerados
        )

        val exito = assertIs<ValidacionTurnoResult.Exito>(resultado)
        assertEquals(descansosGenerados.size, exito.descansosOrdenados.size)
        assertEquals("7 h 30 min", exito.horasEfectivas)
    }

    @Test
    fun validarYCalcularTurno_rechazaDescansosSolapados() {
        val descansosSolapados = listOf(
            RangoDescanso(id = "1", horaInicio = "08:30", horaFin = "08:50"),
            RangoDescanso(id = "2", horaInicio = "08:45", horaFin = "09:00")
        )

        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Intermedio",
            horaInicio = "06:00",
            horaFin = "11:00",
            descansos = descansosSolapados
        )

        val error = assertIs<ValidacionTurnoResult.Error>(resultado)
        assertTrue(error.mensaje.contains("solapan"))
    }

    // =========================================================================
    // 3. PRUEBAS DE INTERCAMBIO DE TURNOS E INCAPACIDADES (CON FAKES DIP)
    // =========================================================================

    @Test
    fun intercambiarTurnosUseCase_cuandoMismaFecha_intercambiaHorariosDeAmbosEmpleados() = runBlocking {
        val fakeTurnoRepo = FakeTurnoRepository(
            asignacionesIniciales = listOf(
                AsignacionTurno(
                    id = "emp_1_2026-10-29",
                    fechaIso = "2026-10-29",
                    empleadoId = "emp_1",
                    empleadoNombre = "Carlos Ruiz",
                    tipoTurno = "Apertura",
                    horario = "06:00-14:00",
                    turnoId = "t_apertura",
                    sucursalId = "CENTRO"
                ),
                AsignacionTurno(
                    id = "emp_2_2026-10-29",
                    fechaIso = "2026-10-29",
                    empleadoId = "emp_2",
                    empleadoNombre = "Laura Mejía",
                    tipoTurno = "Cierre",
                    horario = "14:00-22:00",
                    turnoId = "t_cierre",
                    sucursalId = "CENTRO"
                )
            )
        )

        val useCase = IntercambiarTurnosUseCase(fakeTurnoRepo)
        val solicitud = SolicitudCambioTurno(
            id = "sol_1",
            numeroSolicitud = "SOL-001",
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "Laura Mejía",
            turnoEntregaId = "emp_1_2026-10-29",
            detalleTurnoEntrega = "Apertura · 2026-10-29",
            turnoRecibeId = "emp_2_2026-10-29",
            detalleTurnoRecibe = "Cierre · 2026-10-29",
            estado = EstadoSolicitudCambio.APROBADA
        )

        val res = useCase(solicitud)
        assertTrue(res.isSuccess)

        val asigEmp1 = fakeTurnoRepo.asignaciones.value.first { it.empleadoId == "emp_1" }
        val asigEmp2 = fakeTurnoRepo.asignaciones.value.first { it.empleadoId == "emp_2" }

        assertEquals("Cierre", asigEmp1.tipoTurno)
        assertEquals("14:00-22:00", asigEmp1.horario)
        assertTrue(asigEmp1.modificadoTrasPublicar)

        assertEquals("Apertura", asigEmp2.tipoTurno)
        assertEquals("06:00-14:00", asigEmp2.horario)
        assertTrue(asigEmp2.modificadoTrasPublicar)
    }

    @Test
    fun aplicarIncapacidadEnTurnoUseCase_reemplazaTurnoPorIncapacidadBloqueada() = runBlocking {
        val fakeEmpleadoRepo = FakeEmpleadoRepository(
            empleadosIniciales = listOf(
                Empleado(
                    id = "emp_1",
                    nombre = "Carlos",
                    apellido = "Ruiz",
                    sucursal = "CENTRO"
                )
            )
        )
        val fakeTurnoRepo = FakeTurnoRepository(
            asignacionesIniciales = listOf(
                AsignacionTurno(
                    id = "emp_1_2026-10-30",
                    fechaIso = "2026-10-30",
                    empleadoId = "emp_1",
                    empleadoNombre = "Carlos Ruiz",
                    tipoTurno = "Apertura",
                    horario = "06:00-14:00",
                    sucursalId = "CENTRO"
                )
            )
        )

        val useCase = AplicarIncapacidadEnTurnoUseCase(
            empleadoRepository = fakeEmpleadoRepo,
            turnoRepository = fakeTurnoRepo
        )

        val novedadIncapacidad = Novedad(
            id = "nov_1",
            empleadoId = "emp_1",
            empleado = "Carlos Ruiz",
            sucursal = "CENTRO",
            tipo = "Incapacidad médica",
            fechaReferencia = "2026-10-30",
            detalleTurno = "Apertura · 2026-10-30"
        )

        val res = useCase(novedadIncapacidad)
        assertTrue(res.isSuccess)

        val asigActualizada = fakeTurnoRepo.asignaciones.value.first {
            it.empleadoId == "emp_1" && it.fechaIso == "2026-10-30"
        }
        assertEquals("Incapacidad", asigActualizada.tipoTurno)
        assertEquals("-", asigActualizada.horario)
        assertTrue(asigActualizada.modificadoTrasPublicar)
    }
}

// =========================================================================
// IMPLEMENTACIONES FAKE EN MEMORIA PARA PRUEBAS AISLADAS (SIN FIREBASE)
// =========================================================================

private class FakeEmpleadoRepository(
    empleadosIniciales: List<Empleado> = emptyList()
) : EmpleadoRepositoryContract {
    override val empleados: StateFlow<List<Empleado>> = MutableStateFlow(empleadosIniciales)
    override fun iniciarSincronizacionFirestore(forzar: Boolean) {}
    override fun detenerSincronizacion() {}
    override fun guardarEmpleado(empleado: Empleado, esNuevo: Boolean) {}
    override suspend fun buscarPorCorreo(correo: String): Empleado? =
        empleados.value.find { it.correo.equals(correo, ignoreCase = true) }
}

private class FakeTurnoRepository(
    asignacionesIniciales: List<AsignacionTurno> = emptyList()
) : TurnoRepositoryContract {
    private val _asignaciones = MutableStateFlow(asignacionesIniciales)
    override val asignaciones: StateFlow<List<AsignacionTurno>> = _asignaciones
    override val turnosCatalogo: StateFlow<List<TurnoTemplate>> = MutableStateFlow(emptyList())
    override val horarioSemana: StateFlow<List<TurnoAsignado>> = MutableStateFlow(emptyList())
    override val filasMalla: StateFlow<List<FilaMalla>> = MutableStateFlow(emptyList())
    override val turnosPropios: StateFlow<List<TurnoSeleccionable>> = MutableStateFlow(emptyList())
    override val turnosCompaneros: StateFlow<List<TurnoCompaneroSeleccionable>> = MutableStateFlow(emptyList())

    override fun iniciarSincronizacionFirestore(forzar: Boolean) {}
    override fun detenerSincronizacion() {}
    override fun refrescarDesdeFirestore() {}

    override suspend fun guardarAsignacion(asignacion: AsignacionTurno): Result<Unit> {
        val actual = _asignaciones.value.toMutableList()
        val idx = actual.indexOfFirst { it.id == asignacion.id }
        if (idx >= 0) actual[idx] = asignacion else actual.add(asignacion)
        _asignaciones.value = actual
        return Result.success(Unit)
    }

    override suspend fun guardarAsignaciones(lista: List<AsignacionTurno>): Result<Unit> {
        val actual = _asignaciones.value.toMutableList()
        for (asig in lista) {
            val idx = actual.indexOfFirst { it.id == asig.id }
            if (idx >= 0) actual[idx] = asig else actual.add(asig)
        }
        _asignaciones.value = actual
        return Result.success(Unit)
    }

    override fun guardarAsignacionesEnBackground(lista: List<AsignacionTurno>) {}
    override suspend fun forzarSincronizacionCatalogo(): Result<Unit> = Result.success(Unit)
    override suspend fun guardarTurnoCatalogo(turno: TurnoTemplate, esNuevo: Boolean): Result<Unit> = Result.success(Unit)
    override fun alternarEstadoTurno(idTurno: String) {}
    override fun agregarTurnoCatalogo(turno: TurnoTemplate) {}
    override fun getTurnoHoy(): TurnoAsignado? = null
}
