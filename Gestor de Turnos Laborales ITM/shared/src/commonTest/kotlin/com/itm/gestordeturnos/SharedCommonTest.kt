package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.*
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import com.itm.gestordeturnos.domain.usecase.AplicarIncapacidadEnTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.AplicarPermisoEnTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.CalcularDescansosTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.ConsultarHorarioEmpleadoUseCase
import com.itm.gestordeturnos.domain.usecase.ControlInactividadSesionUseCase
import com.itm.gestordeturnos.domain.usecase.ControlIntentosLoginUseCase
import com.itm.gestordeturnos.domain.usecase.ControlarVencimientoYCancelacionCambioUseCase
import com.itm.gestordeturnos.domain.usecase.EvaluacionIntentoLogin
import com.itm.gestordeturnos.domain.usecase.IntercambiarTurnosUseCase
import com.itm.gestordeturnos.domain.usecase.ValidacionAdminSucursalEmpleadoResult
import com.itm.gestordeturnos.domain.usecase.ValidacionCancelacionResult
import com.itm.gestordeturnos.domain.usecase.ValidacionAprobacionCambioResult
import com.itm.gestordeturnos.domain.usecase.ValidacionAsignacionResult
import com.itm.gestordeturnos.domain.usecase.ValidacionClaveResult
import com.itm.gestordeturnos.domain.usecase.ValidacionSoloLecturaResult
import com.itm.gestordeturnos.domain.usecase.ValidacionEstadoEmpleadoResult
import com.itm.gestordeturnos.domain.usecase.ValidacionMarcacionResult
import com.itm.gestordeturnos.domain.usecase.ValidarMarcacionAsistenciaUseCase
import com.itm.gestordeturnos.domain.usecase.ValidacionMarcaSupervisorResult
import com.itm.gestordeturnos.domain.usecase.ValidarMarcaSupervisorUseCase
import com.itm.gestordeturnos.domain.usecase.ValidacionNovedadResult
import com.itm.gestordeturnos.domain.usecase.ValidacionPermisosEdicionResult
import com.itm.gestordeturnos.domain.usecase.ValidacionSolicitudCambioResult
import com.itm.gestordeturnos.domain.usecase.ValidacionTurnoResult
import com.itm.gestordeturnos.domain.repository.NovedadRepositoryContract
import com.itm.gestordeturnos.domain.usecase.GestionarEstadoNovedadUseCase
import com.itm.gestordeturnos.domain.usecase.ValidacionGestionNovedadResult
import com.itm.gestordeturnos.domain.usecase.ValidarReporteNovedadUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarAprobacionCambioTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarAsignacionTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarContrasenaFirebaseAuthUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarEstadoEmpleadoActivoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarModificacionRolEstadoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarPermisosAdminSucursalEmpleadoUseCase
import com.itm.gestordeturnos.domain.usecase.CalcularJornadaAsistenciaUseCase
import com.itm.gestordeturnos.domain.usecase.JornadaEvaluada
import com.itm.gestordeturnos.domain.usecase.ValidarRecuperacionContrasenaUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarSolicitudCambioTurnoUseCase
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
    private val validarAsignacionUseCase = ValidarAsignacionTurnoUseCase()
    private val validarMarcacionAsistenciaUseCase = ValidarMarcacionAsistenciaUseCase()
    private val validarMarcaSupervisorUseCase = ValidarMarcaSupervisorUseCase()
    private val calcularJornadaUseCase = CalcularJornadaAsistenciaUseCase()

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

    @Test
    fun validarYCalcularTurno_rechazaTurnoAntesDeLas0600() {
        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Madrugada",
            horaInicio = "05:30",
            horaFin = "13:30",
            descansos = emptyList()
        )

        val error = assertIs<ValidacionTurnoResult.Error>(resultado)
        assertTrue(error.mensaje.contains("06:00"))
        assertFalse(error.mensaje.contains("(RN-12)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarYCalcularTurno_rechazaTurnoDespuesDeLas2200() {
        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Cierre Tardio",
            horaInicio = "15:00",
            horaFin = "22:30",
            descansos = emptyList()
        )

        val error = assertIs<ValidacionTurnoResult.Error>(resultado)
        assertTrue(error.mensaje.contains("22:00"))
        assertFalse(error.mensaje.contains("(RN-12)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarYCalcularTurno_rechazaTurnoQueCruzaMedianoche() {
        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Nocturno Cruzado",
            horaInicio = "20:00",
            horaFin = "04:00",
            descansos = emptyList()
        )

        val error = assertIs<ValidacionTurnoResult.Error>(resultado)
        assertTrue(error.mensaje.contains("medianoche"))
    }

    @Test
    fun validarYCalcularTurno_rechazaNombreDuplicadoEnCatalogo() {
        val catalogoExistente = listOf(
            TurnoTemplate(id = "t1", nombre = "Turno Mañana"),
            TurnoTemplate(id = "t2", nombre = "Turno Tarde")
        )

        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = " turno mañana ",
            horaInicio = "07:00",
            horaFin = "15:00",
            descansos = emptyList(),
            turnosExistentes = catalogoExistente,
            idTurnoActual = null
        )

        val error = assertIs<ValidacionTurnoResult.Error>(resultado)
        assertTrue(error.mensaje.contains("Ya existe un turno en el catálogo"))
    }

    @Test
    fun validarYCalcularTurno_permiteMismoNombreAlEditarElMismoTurno() {
        val catalogoExistente = listOf(
            TurnoTemplate(id = "t1", nombre = "Turno Mañana")
        )

        val descansos = calcularDescansosUseCase.generarDescansosAutomaticos("07:00", "15:00")
        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Turno Mañana",
            horaInicio = "07:00",
            horaFin = "15:00",
            descansos = descansos,
            turnosExistentes = catalogoExistente,
            idTurnoActual = "t1"
        )

        assertIs<ValidacionTurnoResult.Exito>(resultado)
    }

    @Test
    fun validarYCalcularTurno_rechazaDuracionEfectivaMenorOIgualACero() {
        val descansosQueConsumenTodoElTurno = listOf(
            RangoDescanso(id = "d1", horaInicio = "08:00", horaFin = "09:00")
        )

        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Turno Cero Efectivo",
            horaInicio = "08:00",
            horaFin = "09:00",
            descansos = descansosQueConsumenTodoElTurno
        )

        val error = assertIs<ValidacionTurnoResult.Error>(resultado)
        assertTrue(error.mensaje.contains("duración efectiva") && error.mensaje.contains("mayor que cero"))
    }

    @Test
    fun validarYCalcularTurno_turnoValidoEnFranja06a22_esExitoso() {
        val autoDescansos = calcularDescansosUseCase.generarDescansosAutomaticos("14:00", "22:00")
        val resultado = calcularDescansosUseCase.validarYCalcularTurno(
            nombreTurno = "Cierre Oficial",
            horaInicio = "14:00",
            horaFin = "22:00",
            descansos = autoDescansos
        )

        val exito = assertIs<ValidacionTurnoResult.Exito>(resultado)
        assertEquals("7 h 30 min", exito.horasEfectivas)
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

    // =========================================================================
    // 4. PRUEBAS DE CONTROL DE INTENTOS Y BLOQUEO DE LOGIN (RN-02)
    // =========================================================================

    @Test
    fun controlIntentosLogin_bloqueaCuentaPor15MinutosAlQuintoIntentoYDesbloqueaTrasExpirar() {
        var relojSimulado = 1_000_000L
        val useCase = ControlIntentosLoginUseCase(relojMillis = { relojSimulado })
        val correo = "Camila.Rios@granodorado.com"

        // Intentos 1 al 4: devuelve FallidoRegistrado con el mensaje unificado de RN-02
        for (intento in 1..4) {
            val res = useCase.registrarIntentoFallido(correo)
            val fallo = assertIs<EvaluacionIntentoLogin.FallidoRegistrado>(res)
            assertEquals(intento, fallo.intentosAcumulados)
            assertEquals(5 - intento, fallo.intentosRestantes)
            assertEquals(
                "Los datos de acceso son errados o la cuenta está inactiva.",
                fallo.mensaje
            )
        }

        // 5to intento consecutivo (incluso con variación de mayúsculas): activa bloqueo de 15 minutos
        val quintoIntento = useCase.registrarIntentoFallido(correo.lowercase())
        val bloqueo = assertIs<EvaluacionIntentoLogin.CuentaBloqueada>(quintoIntento)
        assertEquals(15, bloqueo.minutosRestantes)
        assertTrue(bloqueo.mensaje.contains("bloqueada temporalmente"))

        // Avanzar 10 minutos (sigue bloqueada con 5 minutos restantes)
        relojSimulado += 10 * 60_000L
        val verificacionDuranteBloqueo = useCase.verificarAntesDeIntentar(correo)
        val sigueBloqueada = assertIs<EvaluacionIntentoLogin.CuentaBloqueada>(verificacionDuranteBloqueo)
        assertEquals(5, sigueBloqueada.minutosRestantes)

        // Avanzar otros 5 minutos + 1 ms (total > 15 minutos): el bloqueo expira y permite intentar
        relojSimulado += (5 * 60_000L) + 1L
        val verificacionPostBloqueo = useCase.verificarAntesDeIntentar(correo)
        assertIs<EvaluacionIntentoLogin.Permitido>(verificacionPostBloqueo)
    }

    @Test
    fun controlIntentosLogin_reiniciaContadorTrasLoginExitosoOCambioDeClave() {
        val useCase = ControlIntentosLoginUseCase(relojMillis = { 1_000_000L })
        val correo = "barista@granodorado.com"

        repeat(4) { useCase.registrarIntentoFallido(correo) }
        assertEquals(4, useCase.obtenerEstado(correo).intentosFallidos)

        // Se reinicia el contador por login exitoso o solicitud de cambio de contraseña
        useCase.reiniciarContador(correo)
        assertEquals(0, useCase.obtenerEstado(correo).intentosFallidos)

        // El siguiente fallo vuelve a contar desde 1
        val nuevoFallo = useCase.registrarIntentoFallido(correo)
        val fallo = assertIs<EvaluacionIntentoLogin.FallidoRegistrado>(nuevoFallo)
        assertEquals(1, fallo.intentosAcumulados)
    }

    // =========================================================================
    // 5. PRUEBAS DE CIERRE AUTOMÁTICO DE SESIÓN POR INACTIVIDAD (RN-03)
    // =========================================================================

    @Test
    fun controlInactividadSesion_cierraSesionTras30MinutosSinInteraccion() {
        var relojSimulado = 5_000_000L
        val useCase = ControlInactividadSesionUseCase(relojMillis = { relojSimulado })

        useCase.iniciarMonitoreo()
        assertFalse(useCase.verificarSiExpiro(), "Recién iniciada la sesión no debe estar expirada")

        // Avanzar 29 minutos (aún dentro del límite)
        relojSimulado += 29 * 60_000L
        assertFalse(useCase.verificarSiExpiro(), "A los 29 minutos de inactividad no debe expirar")

        // Avanzar 1 minuto + 1 ms (total > 30 minutos de inactividad)
        relojSimulado += (1 * 60_000L) + 1L
        assertTrue(useCase.verificarSiExpiro(), "A los 30 minutos de inactividad debe expirar automáticamente")
    }

    @Test
    fun controlInactividadSesion_interaccionDelUsuarioReiniciaTemporizadorDe30Minutos() {
        var relojSimulado = 5_000_000L
        val useCase = ControlInactividadSesionUseCase(relojMillis = { relojSimulado })

        useCase.iniciarMonitoreo()

        // El usuario interactúa a los 20 minutos (click, scroll o cambio de opción)
        relojSimulado += 20 * 60_000L
        useCase.registrarInteraccion()

        // Avanzar 15 minutos más (tiempo total desde inicio = 35 min, pero solo 15 min desde la última interacción)
        relojSimulado += 15 * 60_000L
        assertFalse(useCase.verificarSiExpiro(), "La sesión no debe expirar si hubo interacción hace 15 minutos")

        // Avanzar otros 15 minutos + 1 ms sin interactuar (30 minutos desde la última interacción)
        relojSimulado += (15 * 60_000L) + 1L
        assertTrue(useCase.verificarSiExpiro(), "La sesión debe expirar al cumplirse 30 minutos desde la última interacción")
    }

    // =========================================================================
    // 6. PRUEBAS DE CAMBIO DE CONTRASEÑA CON REGLAS FIREBASE AUTH (RN-04)
    // =========================================================================

    @Test
    fun validarContrasenaFirebaseAuth_rechazaClaveMenorASeisCaracteres() {
        val useCase = ValidarContrasenaFirebaseAuthUseCase()

        val resultado = useCase.validar(
            claveActual = "Clave123",
            nuevaClave = "12345", // Menor a 6 caracteres
            confirmacionClave = "12345"
        )

        val invalida = assertIs<ValidacionClaveResult.Invalida>(resultado)
        assertTrue(invalida.motivo.contains("al menos 6 caracteres"))
    }

    @Test
    fun validarContrasenaFirebaseAuth_rechazaClaveIgualALaActualODiscrepanciaEnConfirmacion() {
        val useCase = ValidarContrasenaFirebaseAuthUseCase()

        // Caso 1: Nueva clave igual a la actual
        val resultadoIgual = useCase.validar(
            claveActual = "MiClave123",
            nuevaClave = "MiClave123",
            confirmacionClave = "MiClave123"
        )
        val invalidaIgual = assertIs<ValidacionClaveResult.Invalida>(resultadoIgual)
        assertTrue(invalidaIgual.motivo.contains("distinta"))

        // Caso 2: Confirmación no coincide
        val resultadoDiscrepante = useCase.validar(
            claveActual = "MiClave123",
            nuevaClave = "NuevaClave456",
            confirmacionClave = "OtraClave789"
        )
        val invalidaDiscrepante = assertIs<ValidacionClaveResult.Invalida>(resultadoDiscrepante)
        assertTrue(invalidaDiscrepante.motivo.contains("no coincide"))
    }

    @Test
    fun validarContrasenaFirebaseAuth_aceptaClaveValidaSegunFirebase() {
        val useCase = ValidarContrasenaFirebaseAuthUseCase()

        val resultado = useCase.validar(
            claveActual = "Antigua123",
            nuevaClave = "Nueva456", // >= 6 caracteres y distinta
            confirmacionClave = "Nueva456"
        )

        assertIs<ValidacionClaveResult.Valida>(resultado)
    }

    // =========================================================================
    // 7. PRUEBAS DE RECUPERACIÓN DE CLAVE Y CUENTAS INACTIVAS (RN-05)
    // =========================================================================

    @Test
    fun validarRecuperacionContrasena_rechazaEnvioACuentasInactivasODeshabilitadas() = runBlocking {
        val fakeRepo = FakeEmpleadoRepository(
            empleadosIniciales = listOf(
                Empleado(
                    id = "emp_inactivo",
                    nombre = "Pedro",
                    correo = "pedro.inactivo@granodorado.com",
                    estado = EstadoUsuario.INACTIVO
                ),
                Empleado(
                    id = "emp_activo",
                    nombre = "María",
                    correo = "maria.activa@granodorado.com",
                    estado = EstadoUsuario.ACTIVO
                )
            )
        )
        val useCase = ValidarRecuperacionContrasenaUseCase(fakeRepo)

        // Caso 1: Cuenta inactiva -> debe fallar y no enviar correo (RN-05)
        val resInactivo = useCase.evaluarElegibilidad("pedro.inactivo@granodorado.com")
        assertTrue(resInactivo.isFailure, "Una cuenta inactiva no debe recibir correos de recuperación")
        assertTrue(resInactivo.exceptionOrNull()?.message?.contains("inactiva") == true)

        // Caso 2: Cuenta no registrada -> debe fallar
        val resNoExiste = useCase.evaluarElegibilidad("desconocido@granodorado.com")
        assertTrue(resNoExiste.isFailure, "Una cuenta no registrada no debe recibir correos")

        // Caso 3: Cuenta activa -> elegible para envío
        val resActivo = useCase.evaluarElegibilidad("maria.activa@granodorado.com")
        assertTrue(resActivo.isSuccess, "Una cuenta activa sí es elegible para recibir correo de recuperación")
    }

    // =========================================================================
    // 8. PRUEBAS DE PROTECCIÓN DE PROPIO ROL/ESTADO Y ÚLTIMO ADMIN GENERAL (RN-07)
    // =========================================================================

    @Test
    fun validarModificacionRolEstado_impideModificarPropioRolOEstado() {
        val useCase = ValidarModificacionRolEstadoUseCase()
        val miUsuario = Empleado(
            id = "emp_yo",
            nombre = "Carlos",
            correo = "carlos.admin@granodorado.com",
            cargo = Cargo.ADMINISTRADOR_GENERAL,
            estado = EstadoUsuario.ACTIVO
        )
        val otroAdmin = Empleado(
            id = "emp_otro",
            nombre = "Laura",
            correo = "laura.admin@granodorado.com",
            cargo = Cargo.ADMINISTRADOR_GENERAL,
            estado = EstadoUsuario.ACTIVO
        )
        val todos = listOf(miUsuario, otroAdmin)

        // Caso 1: Intentar cambiar mi propio rol (ej: a BARISTA)
        val resCambioRol = useCase.validar(
            usuarioActualId = "emp_yo",
            usuarioActualCorreo = "carlos.admin@granodorado.com",
            empleadoOriginal = miUsuario,
            empleadoActualizado = miUsuario.copy(cargo = Cargo.BARISTA),
            todosLosEmpleados = todos
        )
        val falloRol = assertIs<ValidacionPermisosEdicionResult.Invalida>(resCambioRol)
        assertTrue(falloRol.mensaje.contains("propio rol"))

        // Caso 2: Intentar cambiar mi propio estado a INACTIVO
        val resCambioEstado = useCase.validar(
            usuarioActualId = "emp_yo",
            usuarioActualCorreo = "carlos.admin@granodorado.com",
            empleadoOriginal = miUsuario,
            empleadoActualizado = miUsuario.copy(estado = EstadoUsuario.INACTIVO),
            todosLosEmpleados = todos
        )
        val falloEstado = assertIs<ValidacionPermisosEdicionResult.Invalida>(resCambioEstado)
        assertTrue(falloEstado.mensaje.contains("propio rol ni estado"))

        // Caso 3: Modificar mis datos personales sin alterar rol ni estado (permitido)
        val resDatosPersonales = useCase.validar(
            usuarioActualId = "emp_yo",
            usuarioActualCorreo = "carlos.admin@granodorado.com",
            empleadoOriginal = miUsuario,
            empleadoActualizado = miUsuario.copy(telefono = "3009998877"),
            todosLosEmpleados = todos
        )
        assertIs<ValidacionPermisosEdicionResult.Valida>(resDatosPersonales)
    }

    @Test
    fun validarModificacionRolEstado_impideQuedarseSinAlMenosUnAdminGeneralActivo() {
        val useCase = ValidarModificacionRolEstadoUseCase()
        val unicoAdminGeneral = Empleado(
            id = "emp_admin_unico",
            nombre = "Admin Único",
            correo = "admin.unico@granodorado.com",
            cargo = Cargo.ADMINISTRADOR_GENERAL,
            estado = EstadoUsuario.ACTIVO
        )
        val empleadoComun = Empleado(
            id = "emp_comun",
            nombre = "Barista 1",
            correo = "barista1@granodorado.com",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )
        val todos = listOf(unicoAdminGeneral, empleadoComun)

        // Intentar inactivar al único Administrador General activo (incluso si lo intenta otro usuario con permisos)
        val resInactivarUnico = useCase.validar(
            usuarioActualId = "otro_usuario",
            usuarioActualCorreo = "otro@granodorado.com",
            empleadoOriginal = unicoAdminGeneral,
            empleadoActualizado = unicoAdminGeneral.copy(estado = EstadoUsuario.INACTIVO),
            todosLosEmpleados = todos
        )
        val fallo = assertIs<ValidacionPermisosEdicionResult.Invalida>(resInactivarUnico)
        assertTrue(fallo.mensaje.contains("único Administrador General activo"))
    }

    // =========================================================================
    // 9. PRUEBAS DE PERMISOS DE ADMINISTRADOR DE SUCURSAL (RN-10)
    // =========================================================================

    @Test
    fun validarPermisosAdminSucursal_impideAsignarRolAdminGeneral() {
        val useCase = ValidarPermisosAdminSucursalEmpleadoUseCase()
        val sesionAdminSucursal = UsuarioActual(
            id = "admin_suc_1",
            correo = "admin.centro@granodorado.com",
            sucursal = "CENTRO",
            nombre = "Admin Centro",
            iniciales = "AC",
            cargo = "Administrador de Sucursal",
            rol = RolUsuario.ADMINISTRADOR_SUCURSAL
        )

        // Intento de crear un empleado asignándole rol ADMINISTRADOR_GENERAL
        val nuevoConRolGeneral = Empleado(
            id = "emp_nuevo",
            nombre = "Nuevo",
            apellido = "Empleado",
            documento = 123456L,
            correo = "nuevo@granodorado.com",
            cargo = Cargo.ADMINISTRADOR_GENERAL,
            sucursal = "CENTRO"
        )
        val resCreacion = useCase.validar(
            usuarioSesion = sesionAdminSucursal,
            esNuevoEmpleado = true,
            empleadoOriginal = null,
            empleadoActualizado = nuevoConRolGeneral
        )
        val errorCreacion = assertIs<ValidacionAdminSucursalEmpleadoResult.Invalida>(resCreacion)
        assertTrue(errorCreacion.mensaje.contains("no puede asignar el rol de Administrador General"))

        // Intento de editar un empleado asignándole rol ADMINISTRADOR_GENERAL
        val empleadoExistente = Empleado(
            id = "emp_existente",
            nombre = "Existente",
            apellido = "Empleado",
            documento = 654321L,
            correo = "existente@granodorado.com",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO"
        )
        val resEdicion = useCase.validar(
            usuarioSesion = sesionAdminSucursal,
            esNuevoEmpleado = false,
            empleadoOriginal = empleadoExistente,
            empleadoActualizado = empleadoExistente.copy(cargo = Cargo.ADMINISTRADOR_GENERAL)
        )
        val errorEdicion = assertIs<ValidacionAdminSucursalEmpleadoResult.Invalida>(resEdicion)
        assertTrue(errorEdicion.mensaje.contains("no puede asignar el rol de Administrador General"))
    }

    @Test
    fun validarPermisosAdminSucursal_creacionSoloEnSuPropiaSucursal() {
        val useCase = ValidarPermisosAdminSucursalEmpleadoUseCase()
        val sesionAdminCentro = UsuarioActual(
            id = "admin_suc_1",
            correo = "admin.centro@granodorado.com",
            sucursal = "CENTRO",
            nombre = "Admin Centro",
            iniciales = "AC",
            cargo = "Administrador de Sucursal",
            rol = RolUsuario.ADMINISTRADOR_SUCURSAL
        )

        // Intento de crear en sucursal NORTE
        val empleadoEnNorte = Empleado(
            id = "emp_norte",
            nombre = "Juan",
            apellido = "Perez",
            documento = 987654L,
            correo = "juan.perez@granodorado.com",
            cargo = Cargo.BARISTA,
            sucursal = "NORTE"
        )
        val resInvalido = useCase.validar(
            usuarioSesion = sesionAdminCentro,
            esNuevoEmpleado = true,
            empleadoOriginal = null,
            empleadoActualizado = empleadoEnNorte
        )
        val error = assertIs<ValidacionAdminSucursalEmpleadoResult.Invalida>(resInvalido)
        assertTrue(error.mensaje.contains("solo puede crear empleados en su propia sucursal"))

        // Creación válida en sucursal CENTRO
        val empleadoEnCentro = empleadoEnNorte.copy(sucursal = "CENTRO")
        val resValido = useCase.validar(
            usuarioSesion = sesionAdminCentro,
            esNuevoEmpleado = true,
            empleadoOriginal = null,
            empleadoActualizado = empleadoEnCentro
        )
        assertIs<ValidacionAdminSucursalEmpleadoResult.Valida>(resValido)
    }

    @Test
    fun validarPermisosAdminSucursal_edicionSoloEnSuSucursalYSinCambiarSucursal() {
        val useCase = ValidarPermisosAdminSucursalEmpleadoUseCase()
        val sesionAdminCentro = UsuarioActual(
            id = "admin_suc_1",
            correo = "admin.centro@granodorado.com",
            sucursal = "CENTRO",
            nombre = "Admin Centro",
            iniciales = "AC",
            cargo = "Administrador de Sucursal",
            rol = RolUsuario.ADMINISTRADOR_SUCURSAL
        )

        val empleadoDeSur = Empleado(
            id = "emp_sur",
            nombre = "Carlos",
            apellido = "Mora",
            documento = 112233L,
            correo = "carlos.mora@granodorado.com",
            cargo = Cargo.BARISTA,
            sucursal = "SUR"
        )

        // 1. Intento de editar un empleado que pertenece a otra sucursal (SUR)
        assertFalse(useCase.puedeEditarEmpleado(sesionAdminCentro, empleadoDeSur))
        val resEditarOtraSucursal = useCase.validar(
            usuarioSesion = sesionAdminCentro,
            esNuevoEmpleado = false,
            empleadoOriginal = empleadoDeSur,
            empleadoActualizado = empleadoDeSur.copy(nombre = "Carlos Alberto")
        )
        val errorOtra = assertIs<ValidacionAdminSucursalEmpleadoResult.Invalida>(resEditarOtraSucursal)
        assertTrue(errorOtra.mensaje.contains("no puede editar empleados de otra sucursal"))

        // 2. Intento de cambiarle la sucursal a un empleado propio (CENTRO -> NORTE)
        val empleadoDeCentro = Empleado(
            id = "emp_centro",
            nombre = "Mariana",
            apellido = "Lopez",
            documento = 445566L,
            correo = "mariana.lopez@granodorado.com",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO"
        )
        assertTrue(useCase.puedeEditarEmpleado(sesionAdminCentro, empleadoDeCentro))

        val resCambiarSucursal = useCase.validar(
            usuarioSesion = sesionAdminCentro,
            esNuevoEmpleado = false,
            empleadoOriginal = empleadoDeCentro,
            empleadoActualizado = empleadoDeCentro.copy(sucursal = "NORTE")
        )
        val errorCambio = assertIs<ValidacionAdminSucursalEmpleadoResult.Invalida>(resCambiarSucursal)
        assertTrue(errorCambio.mensaje.contains("no puede cambiar la sucursal"))

        // 3. Edición válida en su propia sucursal manteniendo la sede
        val resEdicionValida = useCase.validar(
            usuarioSesion = sesionAdminCentro,
            esNuevoEmpleado = false,
            empleadoOriginal = empleadoDeCentro,
            empleadoActualizado = empleadoDeCentro.copy(telefono = "3009998877", cargo = Cargo.CAJERO)
        )
        assertIs<ValidacionAdminSucursalEmpleadoResult.Valida>(resEdicionValida)
    }

    @Test
    fun validarPermisosAdminSucursal_adminGeneralTieneAlcanceGlobal() {
        val useCase = ValidarPermisosAdminSucursalEmpleadoUseCase()
        val sesionAdminGeneral = UsuarioActual(
            id = "admin_gen",
            correo = "admin.general@granodorado.com",
            sucursal = "CENTRO",
            nombre = "Admin General",
            iniciales = "AG",
            cargo = "Administrador General",
            rol = RolUsuario.ADMINISTRADOR_GENERAL
        )

        val empleadoOriginal = Empleado(
            id = "emp_1",
            nombre = "Andres",
            apellido = "Patiño",
            documento = 778899L,
            correo = "andres@granodorado.com",
            cargo = Cargo.BARISTA,
            sucursal = "NORTE"
        )

        // Puede editar cualquier empleado
        assertTrue(useCase.puedeEditarEmpleado(sesionAdminGeneral, empleadoOriginal))

        // Puede cambiar de sucursal
        val resCambioSede = useCase.validar(
            usuarioSesion = sesionAdminGeneral,
            esNuevoEmpleado = false,
            empleadoOriginal = empleadoOriginal,
            empleadoActualizado = empleadoOriginal.copy(sucursal = "SUR")
        )
        assertIs<ValidacionAdminSucursalEmpleadoResult.Valida>(resCambioSede)

        // Puede asignar cualquier rol (incluido Administrador General)
        val resAsignarRol = useCase.validar(
            usuarioSesion = sesionAdminGeneral,
            esNuevoEmpleado = true,
            empleadoOriginal = null,
            empleadoActualizado = empleadoOriginal.copy(cargo = Cargo.ADMINISTRADOR_GENERAL)
        )
        assertIs<ValidacionAdminSucursalEmpleadoResult.Valida>(resAsignarRol)
    }

    // =========================================================================
    // 10. PRUEBAS DE BLOQUEO DE ACCIONES A EMPLEADOS INACTIVOS (RN-11)
    // =========================================================================

    @Test
    fun validarEstadoEmpleadoActivo_cuandoEmpleadoActivo_permiteAccionesOperativas() {
        val useCase = ValidarEstadoEmpleadoActivoUseCase()
        val empleadoActivo = Empleado(
            id = "emp_activo_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            documento = 102030L,
            correo = "carlos.ruiz@granodorado.com",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        assertIs<ValidacionEstadoEmpleadoResult.Activo>(useCase.validar(empleadoActivo, "cambio_turno"))
        assertIs<ValidacionEstadoEmpleadoResult.Activo>(useCase.validar(empleadoActivo, "novedad"))
        assertIs<ValidacionEstadoEmpleadoResult.Activo>(useCase.validar(empleadoActivo, "asistencia"))
        assertIs<ValidacionEstadoEmpleadoResult.Activo>(useCase.validar(empleadoActivo, "clave"))
    }

    @Test
    fun validarEstadoEmpleadoActivo_cuandoEmpleadoInactivo_bloqueaAccionesConMensajeEspecifico() {
        val useCase = ValidarEstadoEmpleadoActivoUseCase()
        val empleadoInactivo = Empleado(
            id = "emp_inactivo_1",
            nombre = "Laura",
            apellido = "Mejia",
            documento = 405060L,
            correo = "laura.mejia@granodorado.com",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.INACTIVO
        )

        // 1. Bloqueo de cambio de turno
        val resCambio = useCase.validar(empleadoInactivo, "cambio_turno")
        val errorCambio = assertIs<ValidacionEstadoEmpleadoResult.Inactivo>(resCambio)
        assertTrue(errorCambio.mensaje.contains("No puedes solicitar ni gestionar cambios de turno"))

        // 2. Bloqueo de reporte de novedad
        val resNovedad = useCase.validar(empleadoInactivo, "novedad")
        val errorNovedad = assertIs<ValidacionEstadoEmpleadoResult.Inactivo>(resNovedad)
        assertTrue(errorNovedad.mensaje.contains("No puedes reportar ni modificar novedades"))

        // 3. Bloqueo de marcas de asistencia
        val resAsistencia = useCase.validar(empleadoInactivo, "asistencia")
        val errorAsistencia = assertIs<ValidacionEstadoEmpleadoResult.Inactivo>(resAsistencia)
        assertTrue(errorAsistencia.mensaje.contains("No puedes registrar marcas de asistencia"))

        // 4. Bloqueo de cambio de contraseña
        val resClave = useCase.validar(empleadoInactivo, "clave")
        val errorClave = assertIs<ValidacionEstadoEmpleadoResult.Inactivo>(resClave)
        assertTrue(errorClave.mensaje.contains("No puedes cambiar tu contraseña"))
    }

    @Test
    fun validarEstadoEmpleadoActivo_conservaDocumentoCorreoEHistorialAlInactivarYReactivar() {
        val empleadoInicial = Empleado(
            id = "emp_historial_1",
            nombre = "Andres",
            apellido = "Gomez",
            documento = 99887766L,
            correo = "andres.gomez@granodorado.com",
            telefono = "3001112233",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO,
            fechaIngreso = "10/01/2024",
            sucursal = "CENTRO"
        )

        // Se inactiva el empleado
        val empleadoInactivado = empleadoInicial.copy(estado = EstadoUsuario.INACTIVO)
        assertEquals(empleadoInicial.id, empleadoInactivado.id)
        assertEquals(empleadoInicial.documento, empleadoInactivado.documento)
        assertEquals(empleadoInicial.correo, empleadoInactivado.correo)
        assertEquals(empleadoInicial.fechaIngreso, empleadoInactivado.fechaIngreso)
        assertEquals(EstadoUsuario.INACTIVO, empleadoInactivado.estado)

        // Se reactiva el empleado
        val empleadoReactivado = empleadoInactivado.copy(estado = EstadoUsuario.ACTIVO)
        assertEquals(empleadoInicial.id, empleadoReactivado.id)
        assertEquals(empleadoInicial.documento, empleadoReactivado.documento)
        assertEquals(empleadoInicial.correo, empleadoReactivado.correo)
        assertEquals(empleadoInicial.fechaIngreso, empleadoReactivado.fechaIngreso)
        assertEquals(EstadoUsuario.ACTIVO, empleadoReactivado.estado)
    }

    // =========================================================================
    // 8. PRUEBAS DE ASIGNACIÓN DE TURNOS Y PROGRAMACIÓN (RN-15)
    // =========================================================================

    @Test
    fun validarAsignacion_rechazaEmpleadoInactivo() {
        val empleadoInactivo = Empleado(
            id = "emp_inactivo",
            nombre = "Carlos",
            apellido = "Pérez",
            estado = EstadoUsuario.INACTIVO,
            sucursal = "Centro"
        )

        val resultado = validarAsignacionUseCase.validarAsignacion(
            empleado = empleadoInactivo,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = LocalDate(2026, 11, 10),
            fechaActual = LocalDate(2026, 11, 6)
        )

        val invalido = assertIs<ValidacionAsignacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("Activo"))
        assertFalse(invalido.mensaje.contains("(RN-15)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarAsignacion_rechazaEmpleadoDeOtraSucursal() {
        val empleadoNorte = Empleado(
            id = "emp_norte",
            nombre = "Laura",
            apellido = "Gómez",
            estado = EstadoUsuario.ACTIVO,
            sucursal = "Norte"
        )

        val resultado = validarAsignacionUseCase.validarAsignacion(
            empleado = empleadoNorte,
            sucursalEmpleado = "Norte",
            sucursalProgramacion = "Centro",
            fechaAsignacion = LocalDate(2026, 11, 10),
            fechaActual = LocalDate(2026, 11, 6)
        )

        val invalido = assertIs<ValidacionAsignacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("sucursal"))
        assertFalse(invalido.mensaje.contains("(RN-15)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarAsignacion_rechazaFechaAnteriorAFechaActual() {
        val empleadoActivo = Empleado(
            id = "emp_1",
            nombre = "Ana",
            apellido = "Torres",
            estado = EstadoUsuario.ACTIVO,
            sucursal = "Centro"
        )

        val fechaActual = LocalDate(2026, 11, 6) // Viernes
        val fechaAyer = LocalDate(2026, 11, 5)   // Jueves

        val resultado = validarAsignacionUseCase.validarAsignacion(
            empleado = empleadoActivo,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = fechaAyer,
            lunesSemana = LocalDate(2026, 11, 2),
            fechaActual = fechaActual
        )

        val invalido = assertIs<ValidacionAsignacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("anterior a la fecha actual"))
        assertFalse(invalido.mensaje.contains("(RN-15)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarAsignacion_rechazaSemanaPasada() {
        val empleadoActivo = Empleado(
            id = "emp_1",
            nombre = "Ana",
            apellido = "Torres",
            estado = EstadoUsuario.ACTIVO,
            sucursal = "Centro"
        )

        val fechaActual = LocalDate(2026, 11, 6)       // Semana actual (lunes 2026-11-02)
        val lunesSemanaPasada = LocalDate(2026, 10, 26) // Semana pasada

        val resultado = validarAsignacionUseCase.validarAsignacion(
            empleado = empleadoActivo,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = LocalDate(2026, 10, 28),
            lunesSemana = lunesSemanaPasada,
            fechaActual = fechaActual
        )

        val invalido = assertIs<ValidacionAsignacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("semana actual") || invalido.mensaje.contains("semanas futuras"))
        assertFalse(invalido.mensaje.contains("(RN-15)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarAsignacion_permiteFechaActualYSemanasFuturas() {
        val empleadoActivo = Empleado(
            id = "emp_1",
            nombre = "Ana",
            apellido = "Torres",
            estado = EstadoUsuario.ACTIVO,
            sucursal = "Centro"
        )

        val fechaActual = LocalDate(2026, 11, 6)

        // Asignación en la misma fecha actual
        val resHoy = validarAsignacionUseCase.validarAsignacion(
            empleado = empleadoActivo,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = fechaActual,
            lunesSemana = LocalDate(2026, 11, 2),
            fechaActual = fechaActual
        )
        assertIs<ValidacionAsignacionResult.Valido>(resHoy)

        // Asignación en una semana futura
        val resFuturo = validarAsignacionUseCase.validarAsignacion(
            empleado = empleadoActivo,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = LocalDate(2026, 11, 16),
            lunesSemana = LocalDate(2026, 11, 16),
            fechaActual = fechaActual
        )
        assertIs<ValidacionAsignacionResult.Valido>(resFuturo)
    }

    @Test
    fun esSemanaModificable_detectaCorrectamenteSemanasPasadasYFuturas() {
        val fechaActual = LocalDate(2026, 11, 6) // Lunes actual: 2026-11-02

        // Semana pasada
        assertFalse(validarAsignacionUseCase.esSemanaModificable(LocalDate(2026, 10, 26), fechaActual))

        // Semana actual
        assertTrue(validarAsignacionUseCase.esSemanaModificable(LocalDate(2026, 11, 2), fechaActual))

        // Semana futura
        assertTrue(validarAsignacionUseCase.esSemanaModificable(LocalDate(2026, 11, 9), fechaActual))
        assertTrue(validarAsignacionUseCase.esSemanaModificable(LocalDate(2026, 11, 16), fechaActual))
    }

    // =========================================================================
    // 9. PRUEBAS DE LÍMITE DE HORAS SEMANALES Y TURNOS SOLAPADOS (RN-16)
    // =========================================================================

    @Test
    fun validarMaximoHorasSemanales_rechazaCuandoSupera42HorasEfectivas() {
        // 6 turnos de 8 horas (08:00 a 16:00, con descanso de 30m = 7.5h efectivas cada uno = 45 horas = 2700 min > 2520 min)
        val turnosExcedidos = List(6) {
            TurnoCelda(tipo = "Completo", horario = "08:00-16:00", tipoCelda = TipoCeldaMalla.TURNO)
        }

        val resultado = validarAsignacionUseCase.validarMaximoHorasSemanales(turnosExcedidos)

        val invalido = assertIs<ValidacionAsignacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("42 horas efectivas"))
        assertFalse(invalido.mensaje.contains("(RN-16)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarMaximoHorasSemanales_permiteHasta42HorasEfectivas() {
        // 5 turnos de 8 horas (7.5h efectivas cada uno = 37.5 horas = 2250 min <= 2520 min)
        val turnosValidos = List(5) {
            TurnoCelda(tipo = "Completo", horario = "08:00-16:00", tipoCelda = TipoCeldaMalla.TURNO)
        }

        val resultado = validarAsignacionUseCase.validarMaximoHorasSemanales(turnosValidos)

        assertIs<ValidacionAsignacionResult.Valido>(resultado)
    }

    @Test
    fun validarAsignacion_rechazaCuandoTurnoNuevoSupera42Horas() {
        val empleadoActivo = Empleado(
            id = "emp_1",
            nombre = "Ana",
            apellido = "Torres",
            estado = EstadoUsuario.ACTIVO,
            sucursal = "Centro"
        )

        // 5 turnos previos de 8 horas = 37.5 horas efectivas
        val turnosSemana = MutableList(7) {
            TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR)
        }
        for (i in 0..4) {
            turnosSemana[i] = TurnoCelda(tipo = "Completo", horario = "08:00-16:00", tipoCelda = TipoCeldaMalla.TURNO)
        }

        // Se intenta asignar un 6to turno de 8h (7.5h efectivas) -> 37.5 + 7.5 = 45 horas > 42 horas
        val nuevoTurno = TurnoCelda(tipo = "Extra", horario = "08:00-16:00", tipoCelda = TipoCeldaMalla.TURNO)

        val resultado = validarAsignacionUseCase.validarAsignacion(
            empleado = empleadoActivo,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = LocalDate(2026, 11, 7),
            lunesSemana = LocalDate(2026, 11, 2),
            fechaActual = LocalDate(2026, 11, 6),
            turnosSemanaActuales = turnosSemana,
            nuevoTurno = nuevoTurno,
            diaIndex = 5
        )

        val invalido = assertIs<ValidacionAsignacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("42 horas efectivas"))
    }

    @Test
    fun validarAsignacion_rechazaDosTurnosElMismoDiaEnOtraSucursal() {
        val empleadoActivo = Empleado(
            id = "emp_1",
            nombre = "Ana",
            apellido = "Torres",
            estado = EstadoUsuario.ACTIVO,
            sucursal = "Centro"
        )

        val asignacionesExistentes = listOf(
            AsignacionTurno(
                id = "emp_1_2026-11-06",
                fechaIso = "2026-11-06",
                empleadoId = "emp_1",
                empleadoNombre = "Ana Torres",
                tipoTurno = "Apertura",
                horario = "06:00-14:00",
                sucursalId = "Norte"
            )
        )

        val nuevoTurno = TurnoCelda(tipo = "Cierre", horario = "14:00-22:00", tipoCelda = TipoCeldaMalla.TURNO)

        val resultado = validarAsignacionUseCase.validarAsignacion(
            empleado = empleadoActivo,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = LocalDate(2026, 11, 6),
            lunesSemana = LocalDate(2026, 11, 2),
            fechaActual = LocalDate(2026, 11, 6),
            nuevoTurno = nuevoTurno,
            diaIndex = 4,
            todasAsignaciones = asignacionesExistentes
        )

        val invalido = assertIs<ValidacionAsignacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("dos turnos"))
    }

    @Test
    fun intercambiarTurnosUseCase_rechazaIntercambioSiResultariaEnDosTurnosMismoDia() = runBlocking {
        val fakeTurnoRepo = FakeTurnoRepository(
            asignacionesIniciales = listOf(
                AsignacionTurno(
                    id = "emp_1_2026-10-29",
                    fechaIso = "2026-10-29",
                    empleadoId = "emp_1",
                    empleadoNombre = "Carlos Ruiz",
                    tipoTurno = "Apertura",
                    horario = "06:00-14:00",
                    sucursalId = "CENTRO"
                ),
                AsignacionTurno(
                    id = "emp_2_2026-10-30",
                    fechaIso = "2026-10-30",
                    empleadoId = "emp_2",
                    empleadoNombre = "Laura Mejía",
                    tipoTurno = "Cierre",
                    horario = "14:00-22:00",
                    sucursalId = "CENTRO"
                ),
                // Laura ya tiene un turno laboral el 2026-10-29 también!
                AsignacionTurno(
                    id = "emp_2_2026-10-29",
                    fechaIso = "2026-10-29",
                    empleadoId = "emp_2",
                    empleadoNombre = "Laura Mejía",
                    tipoTurno = "Intermedio",
                    horario = "10:00-18:00",
                    sucursalId = "CENTRO"
                )
            )
        )

        val useCase = IntercambiarTurnosUseCase(fakeTurnoRepo)
        val solicitud = SolicitudCambioTurno(
            id = "sol_conflictiva",
            numeroSolicitud = "SOL-002",
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "Laura Mejía",
            turnoEntregaId = "emp_1_2026-10-29",
            detalleTurnoEntrega = "Apertura · 2026-10-29",
            turnoRecibeId = "emp_2_2026-10-30",
            detalleTurnoRecibe = "Cierre · 2026-10-30",
            estado = EstadoSolicitudCambio.APROBADA
        )

        val resultado = useCase(solicitud)
        assertTrue(resultado.isFailure, "El intercambio debe ser rechazado si causa dos turnos el mismo día")
        val errorMsg = resultado.exceptionOrNull()?.message ?: ""
        assertTrue(errorMsg.contains("dos turnos"))
    }

    // =========================================================================
    // 14. PRUEBAS DE BLOQUEO DE ASIGNACIÓN POR NOVEDADES Y AUSENCIAS (RN-17)
    // =========================================================================

    @Test
    fun rn17_rechazaAsignacionCuandoEmpleadoTieneDescanso() {
        val useCase = ValidarAsignacionTurnoUseCase()
        val emp = Empleado(id = "emp_1", nombre = "Carlos", sucursal = "Centro", estado = EstadoUsuario.ACTIVO)
        val fechaLunes = LocalDate(2026, 11, 9)
        val fechaMartes = LocalDate(2026, 11, 10)

        // Escenario A: Celda en malla semanal es DESCANSO
        val celdasSemana = MutableList(7) { TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR) }
        celdasSemana[1] = TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true)

        val nuevoTurno = TurnoCelda(tipo = "Apertura", horario = "06:00-14:00", tipoCelda = TipoCeldaMalla.TURNO)

        val resCelda = useCase.validarAsignacion(
            empleado = emp,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = fechaMartes,
            lunesSemana = fechaLunes,
            fechaActual = fechaLunes,
            turnosSemanaActuales = celdasSemana,
            nuevoTurno = nuevoTurno,
            diaIndex = 1
        )

        val errorCelda = assertIs<ValidacionAsignacionResult.Invalido>(resCelda)
        assertTrue(errorCelda.mensaje.contains("descanso", ignoreCase = true))
        assertFalse(errorCelda.mensaje.contains("RN-17"))

        // Escenario B: Asignación existente en base de datos es descanso
        val asignaciones = listOf(
            AsignacionTurno(
                id = "emp_1_2026-11-10",
                fechaIso = "2026-11-10",
                empleadoId = "emp_1",
                tipoTurno = "Descanso",
                esDescanso = true,
                sucursalId = "Centro"
            )
        )

        val resAsig = useCase.validarAsignacion(
            empleado = emp,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = fechaMartes,
            lunesSemana = fechaLunes,
            fechaActual = fechaLunes,
            nuevoTurno = nuevoTurno,
            todasAsignaciones = asignaciones
        )

        val errorAsig = assertIs<ValidacionAsignacionResult.Invalido>(resAsig)
        assertTrue(errorAsig.mensaje.contains("descanso", ignoreCase = true))
        assertFalse(errorAsig.mensaje.contains("RN-17"))
    }

    @Test
    fun rn17_rechazaAsignacionCuandoEmpleadoTieneVacaciones() {
        val useCase = ValidarAsignacionTurnoUseCase()
        val emp = Empleado(id = "emp_1", nombre = "Carlos", sucursal = "Centro", estado = EstadoUsuario.ACTIVO)
        val fechaLunes = LocalDate(2026, 11, 9)
        val fechaMiercoles = LocalDate(2026, 11, 11)

        val celdasSemana = MutableList(7) { TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR) }
        celdasSemana[2] = TurnoCelda(tipo = "Vacaciones", horario = "-", tipoCelda = TipoCeldaMalla.VACACIONES)

        val nuevoTurno = TurnoCelda(tipo = "Cierre", horario = "14:00-22:00", tipoCelda = TipoCeldaMalla.TURNO)

        val res = useCase.validarAsignacion(
            empleado = emp,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = fechaMiercoles,
            lunesSemana = fechaLunes,
            fechaActual = fechaLunes,
            turnosSemanaActuales = celdasSemana,
            nuevoTurno = nuevoTurno,
            diaIndex = 2
        )

        val error = assertIs<ValidacionAsignacionResult.Invalido>(res)
        assertTrue(error.mensaje.contains("vacaciones", ignoreCase = true))
        assertFalse(error.mensaje.contains("RN-17"))
    }

    @Test
    fun rn17_rechazaAsignacionCuandoEmpleadoTieneIncapacidad() {
        val useCase = ValidarAsignacionTurnoUseCase()
        val emp = Empleado(id = "emp_1", nombre = "Carlos", sucursal = "Centro", estado = EstadoUsuario.ACTIVO)
        val fechaLunes = LocalDate(2026, 11, 9)
        val fechaJueves = LocalDate(2026, 11, 12)

        val celdasSemana = MutableList(7) { TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR) }
        celdasSemana[3] = TurnoCelda(tipo = "Incapacidad", horario = "-", tipoCelda = TipoCeldaMalla.INCAPACIDAD)

        val nuevoTurno = TurnoCelda(tipo = "Apertura", horario = "06:00-14:00", tipoCelda = TipoCeldaMalla.TURNO)

        val res = useCase.validarAsignacion(
            empleado = emp,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = fechaJueves,
            lunesSemana = fechaLunes,
            fechaActual = fechaLunes,
            turnosSemanaActuales = celdasSemana,
            nuevoTurno = nuevoTurno,
            diaIndex = 3
        )

        val error = assertIs<ValidacionAsignacionResult.Invalido>(res)
        assertTrue(error.mensaje.contains("incapacidad", ignoreCase = true))
        assertFalse(error.mensaje.contains("RN-17"))
    }

    @Test
    fun rn17_rechazaAsignacionCuandoEmpleadoTienePermisoAprobado() {
        val useCase = ValidarAsignacionTurnoUseCase()
        val emp = Empleado(id = "emp_1", nombre = "Carlos", sucursal = "Centro", estado = EstadoUsuario.ACTIVO)
        val fechaLunes = LocalDate(2026, 11, 9)
        val fechaViernes = LocalDate(2026, 11, 13)

        val celdasSemana = MutableList(7) { TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR) }
        celdasSemana[4] = TurnoCelda(tipo = "Permiso", horario = "-", tipoCelda = TipoCeldaMalla.PERMISO)

        val nuevoTurno = TurnoCelda(tipo = "Intermedio", horario = "10:00-18:00", tipoCelda = TipoCeldaMalla.TURNO)

        val res = useCase.validarAsignacion(
            empleado = emp,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = fechaViernes,
            lunesSemana = fechaLunes,
            fechaActual = fechaLunes,
            turnosSemanaActuales = celdasSemana,
            nuevoTurno = nuevoTurno,
            diaIndex = 4
        )

        val error = assertIs<ValidacionAsignacionResult.Invalido>(res)
        assertTrue(error.mensaje.contains("permiso", ignoreCase = true))
        assertFalse(error.mensaje.contains("RN-17"))
    }

    @Test
    fun rn17_aplicarPermisoAprobadoBloqueaFechaYRechazaNuevasAsignaciones() = runBlocking {
        val emp = Empleado(id = "emp_10", nombre = "Sofia", apellido = "Castro", sucursal = "Centro", estado = EstadoUsuario.ACTIVO)
        val fakeEmpRepo = FakeEmpleadoRepository(listOf(emp))
        val fakeTurnoRepo = FakeTurnoRepository()

        val aplicarPermisoUseCase = AplicarPermisoEnTurnoUseCase(
            empleadoRepository = fakeEmpRepo,
            turnoRepository = fakeTurnoRepo
        )

        // 1. Empleado tiene un permiso aprobado para el día 2026-11-18
        val novedadPermiso = Novedad(
            id = "nov_permiso_1",
            empleadoId = "emp_10",
            empleado = "Sofia Castro",
            tipo = "Permiso especial",
            fechaReferencia = "2026-11-18",
            sucursal = "Centro",
            estado = EstadoNovedad.REVISADA
        )

        val resAplicacion = aplicarPermisoUseCase(novedadPermiso)
        assertTrue(resAplicacion.isSuccess, "La aplicación del bloqueo por permiso debe ser exitosa")

        // 2. Verificar que se registró una asignación de tipo Permiso
        val asignaciones = fakeTurnoRepo.asignaciones.value
        val asigPermiso = asignaciones.find { it.empleadoId == "emp_10" && it.fechaIso == "2026-11-18" }
        assertEquals("Permiso", asigPermiso?.tipoTurno)

        // 3. El sistema no admite nuevas asignaciones en ese día
        val useCase = ValidarAsignacionTurnoUseCase()
        val nuevoTurno = TurnoCelda(tipo = "Apertura", horario = "06:00-14:00", tipoCelda = TipoCeldaMalla.TURNO)
        val fechaPermiso = LocalDate(2026, 11, 18)
        val lunes = LocalDate(2026, 11, 16)

        val resValidacion = useCase.validarAsignacion(
            empleado = emp,
            sucursalEmpleado = "Centro",
            sucursalProgramacion = "Centro",
            fechaAsignacion = fechaPermiso,
            lunesSemana = lunes,
            fechaActual = lunes,
            nuevoTurno = nuevoTurno,
            todasAsignaciones = asignaciones
        )

        val error = assertIs<ValidacionAsignacionResult.Invalido>(resValidacion)
        assertTrue(error.mensaje.contains("permiso", ignoreCase = true))
        assertFalse(error.mensaje.contains("RN-17"))
    }

    // =========================================================================
    // 17. PRUEBAS DE CONSULTA DE HORARIO PUBLICADO Y MODO SOLO LECTURA (RN-18)
    // =========================================================================

    @Test
    fun rn18_empleadoConsultaUnicamenteSusPropiosTurnos() {
        val useCase = ConsultarHorarioEmpleadoUseCase()
        val fechasSemana = setOf("2026-11-16", "2026-11-17", "2026-11-18", "2026-11-19", "2026-11-20", "2026-11-21", "2026-11-22")

        val todasAsignaciones = listOf(
            AsignacionTurno(
                id = "asig_emp1_1",
                empleadoId = "emp_1",
                empleadoNombre = "Carlos Gómez",
                fechaIso = "2026-11-16",
                tipoTurno = "Apertura",
                horario = "06:00-14:00",
                sucursalId = "Centro",
                estado = "PUBLICADA"
            ),
            AsignacionTurno(
                id = "asig_emp1_2",
                empleadoId = "emp_1",
                empleadoNombre = "Carlos Gómez",
                fechaIso = "2026-11-17",
                tipoTurno = "Cierre",
                horario = "14:00-22:00",
                sucursalId = "Centro",
                estado = "PUBLICADA"
            ),
            AsignacionTurno(
                id = "asig_emp2_1",
                empleadoId = "emp_2",
                empleadoNombre = "Laura Pérez",
                fechaIso = "2026-11-16",
                tipoTurno = "Intermedio",
                horario = "10:00-18:00",
                sucursalId = "Centro",
                estado = "PUBLICADA"
            ),
            AsignacionTurno(
                id = "asig_emp2_2",
                empleadoId = "emp_2",
                empleadoNombre = "Laura Pérez",
                fechaIso = "2026-11-18",
                tipoTurno = "Apertura",
                horario = "06:00-14:00",
                sucursalId = "Centro",
                estado = "PUBLICADA"
            )
        )

        // El empleado 'emp_1' consulta sus turnos
        val turnosVisibles = useCase.filtrarTurnosVisiblesParaEmpleado(
            empleadoId = "emp_1",
            empleadoNombre = "Carlos Gómez",
            todasAsignaciones = todasAsignaciones,
            fechasIsoSemana = fechasSemana,
            sucursalEmpleado = "Centro",
            esAdmin = false
        )

        assertEquals(2, turnosVisibles.size)
        assertTrue(turnosVisibles.all { it.empleadoId == "emp_1" })
        assertFalse(turnosVisibles.any { it.empleadoId == "emp_2" }, "El empleado nunca debe visualizar turnos de otros empleados")
    }

    @Test
    fun rn18_empleadoNoVisualizaTurnosEnBorradorOSemanaNoPublicada() {
        val useCase = ConsultarHorarioEmpleadoUseCase()
        val fechasSemana = setOf("2026-11-16", "2026-11-17")

        val asignacionesEnBorrador = listOf(
            AsignacionTurno(
                id = "asig_borrador_1",
                empleadoId = "emp_1",
                empleadoNombre = "Carlos Gómez",
                fechaIso = "2026-11-16",
                tipoTurno = "Apertura",
                horario = "06:00-14:00",
                sucursalId = "Centro",
                estado = "BORRADOR"
            ),
            AsignacionTurno(
                id = "asig_borrador_2",
                empleadoId = "emp_1",
                empleadoNombre = "Carlos Gómez",
                fechaIso = "2026-11-17",
                tipoTurno = "Cierre",
                horario = "14:00-22:00",
                sucursalId = "Centro",
                estado = "BORRADOR"
            )
        )

        val turnosVisibles = useCase.filtrarTurnosVisiblesParaEmpleado(
            empleadoId = "emp_1",
            empleadoNombre = "Carlos Gómez",
            todasAsignaciones = asignacionesEnBorrador,
            fechasIsoSemana = fechasSemana,
            sucursalEmpleado = "Centro",
            esAdmin = false
        )

        assertTrue(turnosVisibles.isEmpty(), "El empleado no debe visualizar turnos si no han sido publicados o están en borrador")
    }

    @Test
    fun rn18_empleadoSoloVisualizaTurnosPublicadosYExcluyeBorradores() {
        val useCase = ConsultarHorarioEmpleadoUseCase()
        val fechasSemana = setOf("2026-11-16", "2026-11-17")

        val asignacionesMixtas = listOf(
            AsignacionTurno(
                id = "asig_publicada",
                empleadoId = "emp_1",
                empleadoNombre = "Carlos Gómez",
                fechaIso = "2026-11-16",
                tipoTurno = "Apertura",
                horario = "06:00-14:00",
                sucursalId = "Centro",
                estado = "PUBLICADA"
            ),
            AsignacionTurno(
                id = "asig_borrador",
                empleadoId = "emp_1",
                empleadoNombre = "Carlos Gómez",
                fechaIso = "2026-11-17",
                tipoTurno = "Cierre",
                horario = "14:00-22:00",
                sucursalId = "Centro",
                estado = "BORRADOR"
            )
        )

        val turnosVisibles = useCase.filtrarTurnosVisiblesParaEmpleado(
            empleadoId = "emp_1",
            empleadoNombre = "Carlos Gómez",
            todasAsignaciones = asignacionesMixtas,
            fechasIsoSemana = fechasSemana,
            sucursalEmpleado = "Centro",
            esAdmin = false
        )

        assertEquals(1, turnosVisibles.size)
        assertEquals("asig_publicada", turnosVisibles[0].id)
        assertEquals("PUBLICADA", turnosVisibles[0].estado)
    }

    @Test
    fun rn18_administradorPuedeVisualizarTurnosEnBorradorParaPlanificacion() {
        val useCase = ConsultarHorarioEmpleadoUseCase()
        val fechasSemana = setOf("2026-11-16", "2026-11-17")

        val asignacionesBorrador = listOf(
            AsignacionTurno(
                id = "asig_borrador_1",
                empleadoId = "emp_1",
                empleadoNombre = "Carlos Gómez",
                fechaIso = "2026-11-16",
                tipoTurno = "Apertura",
                horario = "06:00-14:00",
                sucursalId = "Centro",
                estado = "BORRADOR"
            )
        )

        // Administrador visualizando la programación
        val turnosVisiblesAdmin = useCase.filtrarTurnosVisiblesParaEmpleado(
            empleadoId = "emp_1",
            empleadoNombre = "Carlos Gómez",
            todasAsignaciones = asignacionesBorrador,
            fechasIsoSemana = fechasSemana,
            sucursalEmpleado = "Centro",
            esAdmin = true
        )

        assertEquals(1, turnosVisiblesAdmin.size)
        assertEquals("asig_borrador_1", turnosVisiblesAdmin[0].id)
    }

    @Test
    fun rn18_horarioYMallaSePresentanEnModoSoloLecturaParaEmpleado() {
        val useCase = ConsultarHorarioEmpleadoUseCase()

        // Modo solo lectura activo para empleado
        assertTrue(useCase.esModoSoloLectura(RolUsuario.EMPLEADO, esSemanaPasada = false))

        // Intentar modificar programación siendo empleado debe ser bloqueado
        val validacion = useCase.validarPermisoModificarProgramacion(
            rol = RolUsuario.EMPLEADO,
            esSemanaPasada = false
        )

        val bloqueado = assertIs<ValidacionSoloLecturaResult.Bloqueado>(validacion)
        assertTrue(bloqueado.mensaje.contains("solo lectura", ignoreCase = true))
        assertFalse(bloqueado.mensaje.contains("RN-18"), "No debe exponer códigos técnicos en mensajes de cara al usuario")
    }

    @Test
    fun rn18_programacionEnSemanaPasadaEsSoloLecturaParaCualquierRol() {
        val useCase = ConsultarHorarioEmpleadoUseCase()

        // Admin General en semana pasada
        assertTrue(useCase.esModoSoloLectura(RolUsuario.ADMINISTRADOR_GENERAL, esSemanaPasada = true))

        val resAdminGeneral = useCase.validarPermisoModificarProgramacion(
            rol = RolUsuario.ADMINISTRADOR_GENERAL,
            esSemanaPasada = true
        )
        val bloqueadoGeneral = assertIs<ValidacionSoloLecturaResult.Bloqueado>(resAdminGeneral)
        assertTrue(bloqueadoGeneral.mensaje.contains("semana pasada", ignoreCase = true))
        assertFalse(bloqueadoGeneral.mensaje.contains("RN-18"))

        // Admin Sucursal en semana pasada
        val resAdminSucursal = useCase.validarPermisoModificarProgramacion(
            rol = RolUsuario.ADMINISTRADOR_SUCURSAL,
            esSemanaPasada = true
        )
        assertIs<ValidacionSoloLecturaResult.Bloqueado>(resAdminSucursal)
    }

    @Test
    fun rn18_administradorEnSemanaActualOFuturaTienePermisoModificar() {
        val useCase = ConsultarHorarioEmpleadoUseCase()

        // Admin General en semana modificable
        assertFalse(useCase.esModoSoloLectura(RolUsuario.ADMINISTRADOR_GENERAL, esSemanaPasada = false))
        val resGeneral = useCase.validarPermisoModificarProgramacion(
            rol = RolUsuario.ADMINISTRADOR_GENERAL,
            esSemanaPasada = false
        )
        assertIs<ValidacionSoloLecturaResult.Permitido>(resGeneral)

        // Admin Sucursal en semana modificable
        assertFalse(useCase.esModoSoloLectura(RolUsuario.ADMINISTRADOR_SUCURSAL, esSemanaPasada = false))
        val resSucursal = useCase.validarPermisoModificarProgramacion(
            rol = RolUsuario.ADMINISTRADOR_SUCURSAL,
            esSemanaPasada = false
        )
        assertIs<ValidacionSoloLecturaResult.Permitido>(resSucursal)
    }

    // =========================================================================
    // 21. PRUEBAS DE REGLAS DE INTERCAMBIO DE TURNOS (RN-19)
    // =========================================================================

    @Test
    fun rn19_solicitudRechazada_siTurnoEntregaNoEstaPublicado() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turnoBorrador = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "BORRADOR"
        )
        val turnoCompanero = AsignacionTurno(
            id = "asig_2",
            empleadoId = "emp_2",
            fechaIso = "2029-10-16",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turnoBorrador,
            turnoRecibe = turnoCompanero,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = listOf(turnoBorrador, turnoCompanero)
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("publicado", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudRechazada_siTurnoRecibeNoEstaPublicado() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turnoPropio = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        val turnoCompaneroBorrador = AsignacionTurno(
            id = "asig_2",
            empleadoId = "emp_2",
            fechaIso = "2029-10-16",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "BORRADOR"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turnoPropio,
            turnoRecibe = turnoCompaneroBorrador,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = listOf(turnoPropio, turnoCompaneroBorrador)
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("publicado", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudRechazada_siFechaYHoraInicioYaPasaron() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turnoPasado = AsignacionTurno(
            id = "asig_pasada",
            empleadoId = "emp_1",
            fechaIso = "2020-01-01",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        val turnoFuturo = AsignacionTurno(
            id = "asig_futura",
            empleadoId = "emp_2",
            fechaIso = "2029-10-16",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turnoPasado,
            turnoRecibe = turnoFuturo,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = listOf(turnoPasado, turnoFuturo)
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("ya ha pasado", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudRechazada_siTurnoTieneSolicitudEnCurso() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turno1 = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        val turno2 = AsignacionTurno(
            id = "asig_2",
            empleadoId = "emp_2",
            fechaIso = "2029-10-16",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        val solicitudEnCurso = SolicitudCambioTurno(
            id = "sol_activa",
            numeroSolicitud = "SOL-101",
            estado = EstadoSolicitudCambio.PENDIENTE,
            turnoEntregaId = "asig_1",
            turnoRecibeId = "otra_asig",
            empleadoSolicitanteId = "emp_1",
            empleadoCompaneroId = "emp_3"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turno1,
            turnoRecibe = turno2,
            solicitudesExistentes = listOf(solicitudEnCurso),
            todasAsignaciones = listOf(turno1, turno2)
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("en curso", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudRechazada_siCompaneroEsDeDistintaSucursal() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "POBLADO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turno1 = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        val turno2 = AsignacionTurno(
            id = "asig_2",
            empleadoId = "emp_2",
            fechaIso = "2029-10-16",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "POBLADO",
            estado = "PUBLICADA"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turno1,
            turnoRecibe = turno2,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = listOf(turno1, turno2)
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("misma sucursal", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudRechazada_siCompaneroTieneDistintoCargo() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.COCINERO, estado = EstadoUsuario.ACTIVO)

        val turno1 = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        val turno2 = AsignacionTurno(
            id = "asig_2",
            empleadoId = "emp_2",
            fechaIso = "2029-10-16",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turno1,
            turnoRecibe = turno2,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = listOf(turno1, turno2)
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("mismo perfil o cargo", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudRechazada_siAlgunEmpleadoEstaInactivo() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val empActivo = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val empInactivo = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.INACTIVO)

        val turno1 = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        val turno2 = AsignacionTurno(
            id = "asig_2",
            empleadoId = "emp_2",
            fechaIso = "2029-10-16",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = empActivo,
            companero = empInactivo,
            turnoEntrega = turno1,
            turnoRecibe = turno2,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = listOf(turno1, turno2)
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("activa", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudRechazada_siResultadoIncumpleReglasDeDescanso12h() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        // emp1 ya tiene asignado un turno el 2029-10-16 de 06:00 a 14:00 (Apertura)
        val turnoExistenteEmp1 = AsignacionTurno(
            id = "asig_emp1_dia2",
            empleadoId = "emp_1",
            fechaIso = "2029-10-16",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        // emp1 quiere entregar su turno de 2029-10-17 y recibir el de emp2 del 2029-10-15 que termina a las 22:00 (Cierre 14:00-22:00).
        // Entre 22:00 del 2029-10-15 y 06:00 del 2029-10-16 hay solo 8 horas (< 12 horas).
        val turnoEntrega = AsignacionTurno(
            id = "asig_entrega",
            empleadoId = "emp_1",
            fechaIso = "2029-10-17",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        val turnoRecibe = AsignacionTurno(
            id = "asig_recibe",
            empleadoId = "emp_2",
            fechaIso = "2029-10-15",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turnoEntrega,
            turnoRecibe = turnoRecibe,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = listOf(turnoExistenteEmp1, turnoEntrega, turnoRecibe)
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("12 horas", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudRechazada_siResultadoExcedeLimite42HorasSemanales() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        // Semana de lunes 2029-10-15 a domingo 2029-10-21
        // emp1 ya tiene 5 turnos de 8h (40h): martes 16, miércoles 17, jueves 18, viernes 19, sábado 20.
        val turnosSemanaEmp1 = (16..20).map { dia ->
            AsignacionTurno(
                id = "asig_emp1_$dia",
                empleadoId = "emp_1",
                fechaIso = "2029-10-$dia",
                tipoTurno = "Intermedio",
                horario = "10:00 - 18:00",
                sucursalId = "CENTRO",
                estado = "PUBLICADA"
            )
        }

        // emp1 entrega un turno corto de 2h el lunes 15 (40h + 2h = 42h)
        val turnoEntrega = AsignacionTurno(
            id = "asig_entrega",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Especial",
            horario = "08:00 - 10:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        // Recibe turno de 8h el domingo 21 -> su total pasaría a 40 + 8 = 48h > 42h
        val turnoRecibe = AsignacionTurno(
            id = "asig_recibe",
            empleadoId = "emp_2",
            fechaIso = "2029-10-21",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        val todasAsignaciones = turnosSemanaEmp1 + turnoEntrega + turnoRecibe

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turnoEntrega,
            turnoRecibe = turnoRecibe,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = todasAsignaciones
        )

        val invalido = assertIs<ValidacionSolicitudCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("42 horas", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-19"))
    }

    @Test
    fun rn19_solicitudValida_cuandoCumpleTodasLasReglas() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turno1 = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )
        val turno2 = AsignacionTurno(
            id = "asig_2",
            empleadoId = "emp_2",
            fechaIso = "2029-10-18",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00",
            sucursalId = "CENTRO",
            estado = "PUBLICADA"
        )

        val resultado = useCase.validarSolicitud(
            solicitante = emp1,
            companero = emp2,
            turnoEntrega = turno1,
            turnoRecibe = turno2,
            solicitudesExistentes = emptyList(),
            todasAsignaciones = listOf(turno1, turno2)
        )

        assertIs<ValidacionSolicitudCambioResult.Valido>(resultado)
    }

    @Test
    fun rn19_esTurnoElegibleParaIntercambio_filtraCorrectamente() {
        val useCase = ValidarSolicitudCambioTurnoUseCase()

        val turnoValido = AsignacionTurno(
            id = "asig_valida",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00",
            estado = "PUBLICADA"
        )
        val turnoBorrador = turnoValido.copy(id = "asig_borrador", estado = "BORRADOR")
        val turnoPasado = turnoValido.copy(id = "asig_pasada", fechaIso = "2020-01-01")
        val turnoConSolicitud = turnoValido.copy(id = "asig_con_sol")

        val solicitud = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.PENDIENTE,
            turnoEntregaId = "asig_con_sol"
        )

        assertTrue(useCase.esTurnoElegibleParaIntercambio(turnoValido, "emp_1", emptyList()))
        assertFalse(useCase.esTurnoElegibleParaIntercambio(turnoValido, "emp_otro", emptyList()), "No pertenece al empleado")
        assertFalse(useCase.esTurnoElegibleParaIntercambio(turnoBorrador, "emp_1", emptyList()), "Está en borrador")
        assertFalse(useCase.esTurnoElegibleParaIntercambio(turnoPasado, "emp_1", emptyList()), "Fecha en el pasado")
        assertFalse(useCase.esTurnoElegibleParaIntercambio(turnoConSolicitud, "emp_1", listOf(solicitud)), "Ya tiene solicitud en curso")
    }

    // =========================================================================
    // 22. PRUEBAS DE FLUJO DE APROBACIÓN Y RESOLUCIÓN DE INTERCAMBIOS (RN-20)
    // =========================================================================

    @Test
    fun rn20_aprobacionRechazada_siSolicitudNoHaSidoAceptadaPorElCompanero() {
        val useCase = ValidarAprobacionCambioTurnoUseCase()
        val admin = Empleado(id = "admin_1", nombre = "Supervisor", apellido = "General", sucursal = "CENTRO", cargo = Cargo.ADMINISTRADOR_SUCURSAL, estado = EstadoUsuario.ACTIVO)
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turno1 = AsignacionTurno(id = "asig_1", empleadoId = "emp_1", fechaIso = "2029-10-15", tipoTurno = "Apertura", horario = "06:00 - 14:00", sucursalId = "CENTRO", estado = "PUBLICADA")
        val turno2 = AsignacionTurno(id = "asig_2", empleadoId = "emp_2", fechaIso = "2029-10-18", tipoTurno = "Cierre", horario = "14:00 - 22:00", sucursalId = "CENTRO", estado = "PUBLICADA")

        val solicitudPendiente = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.PENDIENTE,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "María López",
            turnoEntregaId = "asig_1",
            turnoRecibeId = "asig_2",
            sucursalNombre = "CENTRO"
        )

        val resultado = useCase.validarAprobacion(
            administrador = admin,
            rolAdministrador = RolUsuario.ADMINISTRADOR_SUCURSAL,
            solicitud = solicitudPendiente,
            empleados = listOf(emp1, emp2, admin),
            todasAsignaciones = listOf(turno1, turno2),
            todasSolicitudes = listOf(solicitudPendiente),
            fechaActual = LocalDate(2026, 1, 1),
            horaActualMinutos = 0
        )

        val invalido = assertIs<ValidacionAprobacionCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("aceptada primero por el compañero", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-20"))
    }

    @Test
    fun rn20_aprobacionRechazada_siAdministradorEsElSolicitante_conflictoDeInteres() {
        val useCase = ValidarAprobacionCambioTurnoUseCase()
        // El administrador Carlos también es el solicitante
        val adminSolicitante = Empleado(id = "emp_admin_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.ADMINISTRADOR_SUCURSAL, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turno1 = AsignacionTurno(id = "asig_1", empleadoId = "emp_admin_1", fechaIso = "2029-10-15", tipoTurno = "Apertura", horario = "06:00 - 14:00", sucursalId = "CENTRO", estado = "PUBLICADA")
        val turno2 = AsignacionTurno(id = "asig_2", empleadoId = "emp_2", fechaIso = "2029-10-18", tipoTurno = "Cierre", horario = "14:00 - 22:00", sucursalId = "CENTRO", estado = "PUBLICADA")

        val solicitudAceptada = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            empleadoSolicitanteId = "emp_admin_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "María López",
            turnoEntregaId = "asig_1",
            turnoRecibeId = "asig_2",
            sucursalNombre = "CENTRO"
        )

        val resultado = useCase.validarAprobacion(
            administrador = adminSolicitante,
            rolAdministrador = RolUsuario.ADMINISTRADOR_SUCURSAL,
            solicitud = solicitudAceptada,
            empleados = listOf(adminSolicitante, emp2),
            todasAsignaciones = listOf(turno1, turno2),
            todasSolicitudes = listOf(solicitudAceptada),
            fechaActual = LocalDate(2026, 1, 1),
            horaActualMinutos = 0
        )

        val invalido = assertIs<ValidacionAprobacionCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("no puede resolver una solicitud en la que participe", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-20"))
    }

    @Test
    fun rn20_aprobacionRechazada_siAdministradorEsElCompanero_conflictoDeInteres() {
        val useCase = ValidarAprobacionCambioTurnoUseCase()
        // La administradora María también es la compañera en la solicitud
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val adminCompanero = Empleado(id = "emp_admin_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.ADMINISTRADOR_SUCURSAL, estado = EstadoUsuario.ACTIVO)

        val turno1 = AsignacionTurno(id = "asig_1", empleadoId = "emp_1", fechaIso = "2029-10-15", tipoTurno = "Apertura", horario = "06:00 - 14:00", sucursalId = "CENTRO", estado = "PUBLICADA")
        val turno2 = AsignacionTurno(id = "asig_2", empleadoId = "emp_admin_2", fechaIso = "2029-10-18", tipoTurno = "Cierre", horario = "14:00 - 22:00", sucursalId = "CENTRO", estado = "PUBLICADA")

        val solicitudAceptada = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_admin_2",
            nombreCompanero = "María López",
            turnoEntregaId = "asig_1",
            turnoRecibeId = "asig_2",
            sucursalNombre = "CENTRO"
        )

        val resultado = useCase.validarAprobacion(
            administrador = adminCompanero,
            rolAdministrador = RolUsuario.ADMINISTRADOR_SUCURSAL,
            solicitud = solicitudAceptada,
            empleados = listOf(emp1, adminCompanero),
            todasAsignaciones = listOf(turno1, turno2),
            todasSolicitudes = listOf(solicitudAceptada),
            fechaActual = LocalDate(2026, 1, 1),
            horaActualMinutos = 0
        )

        val invalido = assertIs<ValidacionAprobacionCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("no puede resolver una solicitud en la que participe", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-20"))
    }

    @Test
    fun rn20_rechazoAdministrativoRechazado_siAdministradorParticipaEnLaSolicitud() {
        val useCase = ValidarAprobacionCambioTurnoUseCase()
        val adminParticipante = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.ADMINISTRADOR_SUCURSAL, estado = EstadoUsuario.ACTIVO)

        val solicitud = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "María López"
        )

        val resultado = useCase.validarRechazoAdministrador(
            administrador = adminParticipante,
            rolAdministrador = RolUsuario.ADMINISTRADOR_SUCURSAL,
            solicitud = solicitud
        )

        val invalido = assertIs<ValidacionAprobacionCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("no puede resolver una solicitud en la que participe", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-20"))
    }

    @Test
    fun rn20_aprobacionRechazada_siReglasDeProgramacionDejaronDeCumplirseAlMomentoDeAprobar() {
        val useCase = ValidarAprobacionCambioTurnoUseCase()
        val adminNeutral = Empleado(id = "admin_boss", nombre = "Diana", apellido = "Jefe", sucursal = "CENTRO", cargo = Cargo.ADMINISTRADOR_SUCURSAL, estado = EstadoUsuario.ACTIVO)
        // El compañero María fue desactivado antes de que el administrador aprobara
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2Inactivo = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.INACTIVO)

        val turno1 = AsignacionTurno(id = "asig_1", empleadoId = "emp_1", fechaIso = "2029-10-15", tipoTurno = "Apertura", horario = "06:00 - 14:00", sucursalId = "CENTRO", estado = "PUBLICADA")
        val turno2 = AsignacionTurno(id = "asig_2", empleadoId = "emp_2", fechaIso = "2029-10-18", tipoTurno = "Cierre", horario = "14:00 - 22:00", sucursalId = "CENTRO", estado = "PUBLICADA")

        val solicitudAceptada = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "María López",
            turnoEntregaId = "asig_1",
            turnoRecibeId = "asig_2",
            sucursalNombre = "CENTRO"
        )

        val resultado = useCase.validarAprobacion(
            administrador = adminNeutral,
            rolAdministrador = RolUsuario.ADMINISTRADOR_SUCURSAL,
            solicitud = solicitudAceptada,
            empleados = listOf(emp1, emp2Inactivo, adminNeutral),
            todasAsignaciones = listOf(turno1, turno2),
            todasSolicitudes = listOf(solicitudAceptada),
            fechaActual = LocalDate(2026, 1, 1),
            horaActualMinutos = 0
        )

        val invalido = assertIs<ValidacionAprobacionCambioResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("inactivo", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-20"))
    }

    @Test
    fun rn20_aprobacionExitosa_cuandoCompaneroAceptoAdminEsNeutralYReglasCumplen() {
        val useCase = ValidarAprobacionCambioTurnoUseCase()
        val adminNeutral = Empleado(id = "admin_boss", nombre = "Diana", apellido = "Jefe", sucursal = "CENTRO", cargo = Cargo.ADMINISTRADOR_SUCURSAL, estado = EstadoUsuario.ACTIVO)
        val emp1 = Empleado(id = "emp_1", nombre = "Carlos", apellido = "Ruiz", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)
        val emp2 = Empleado(id = "emp_2", nombre = "María", apellido = "López", sucursal = "CENTRO", cargo = Cargo.BARISTA, estado = EstadoUsuario.ACTIVO)

        val turno1 = AsignacionTurno(id = "asig_1", empleadoId = "emp_1", fechaIso = "2029-10-15", tipoTurno = "Apertura", horario = "06:00 - 14:00", sucursalId = "CENTRO", estado = "PUBLICADA")
        val turno2 = AsignacionTurno(id = "asig_2", empleadoId = "emp_2", fechaIso = "2029-10-18", tipoTurno = "Cierre", horario = "14:00 - 22:00", sucursalId = "CENTRO", estado = "PUBLICADA")

        val solicitudAceptada = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "María López",
            turnoEntregaId = "asig_1",
            turnoRecibeId = "asig_2",
            sucursalNombre = "CENTRO"
        )

        val resultado = useCase.validarAprobacion(
            administrador = adminNeutral,
            rolAdministrador = RolUsuario.ADMINISTRADOR_SUCURSAL,
            solicitud = solicitudAceptada,
            empleados = listOf(emp1, emp2, adminNeutral),
            todasAsignaciones = listOf(turno1, turno2),
            todasSolicitudes = listOf(solicitudAceptada),
            fechaActual = LocalDate(2026, 1, 1),
            horaActualMinutos = 0
        )

        assertIs<ValidacionAprobacionCambioResult.Valido>(resultado)
    }

    // =========================================================================
    // 23. PRUEBAS DE VENCIMIENTO AUTOMÁTICO E INMUTABILIDAD TERMINAL (RN-22)
    // =========================================================================

    @Test
    fun rn22_solicitantePuedeCancelarEnEstadoPendienteAntesDelTurno() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()
        val empSolicitante = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        val turno1 = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00"
        )
        val turno2 = AsignacionTurno(
            id = "asig_2",
            empleadoId = "emp_2",
            fechaIso = "2029-10-18",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00"
        )

        val solicitudPendiente = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.PENDIENTE,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "María López",
            turnoEntregaId = "asig_1",
            turnoRecibeId = "asig_2",
            esRemitente = true
        )

        val resultado = useCase.validarCancelacion(
            solicitante = empSolicitante,
            solicitud = solicitudPendiente,
            todasAsignaciones = listOf(turno1, turno2),
            fechaActual = LocalDate(2026, 1, 1),
            horaActualMinutos = 480
        )

        assertIs<ValidacionCancelacionResult.Valido>(resultado)
    }

    @Test
    fun rn22_solicitantePuedeCancelarEnEstadoAceptadaCompaneroAntesDelTurno() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()
        val empSolicitante = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        val turno1 = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2029-10-15",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00"
        )

        val solicitudAceptada = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "María López",
            turnoEntregaId = "asig_1",
            turnoRecibeId = "asig_2"
        )

        val resultado = useCase.validarCancelacion(
            solicitante = empSolicitante,
            solicitud = solicitudAceptada,
            todasAsignaciones = listOf(turno1),
            fechaActual = LocalDate(2026, 1, 1),
            horaActualMinutos = 480
        )

        assertIs<ValidacionCancelacionResult.Valido>(resultado)
    }

    @Test
    fun rn22_cancelacionRechazada_siEmpleadoEstaInactivo() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()
        val empInactivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.INACTIVO
        )

        val solicitud = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.PENDIENTE,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2"
        )

        val resultado = useCase.validarCancelacion(
            solicitante = empInactivo,
            solicitud = solicitud
        )

        val invalido = assertIs<ValidacionCancelacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("inactiva", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-22"))
    }

    @Test
    fun rn22_cancelacionRechazada_siUsuarioNoEsElSolicitante() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()
        val companero = Empleado(
            id = "emp_2",
            nombre = "María",
            apellido = "López",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        val solicitud = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            empleadoCompaneroId = "emp_2",
            nombreCompanero = "María López",
            esRemitente = false
        )

        val resultado = useCase.validarCancelacion(
            solicitante = companero,
            solicitud = solicitud
        )

        val invalido = assertIs<ValidacionCancelacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("Solo el colaborador solicitante", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-22"))
    }

    @Test
    fun rn22_cancelacionRechazada_enTodosLosEstadosTerminales() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()
        val solicitante = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        val estadosTerminales = listOf(
            EstadoSolicitudCambio.APROBADA,
            EstadoSolicitudCambio.RECHAZADA,
            EstadoSolicitudCambio.CANCELADA,
            EstadoSolicitudCambio.VENCIDA
        )

        for (estado in estadosTerminales) {
            val solicitud = SolicitudCambioTurno(
                id = "sol_${estado.name}",
                estado = estado,
                empleadoSolicitanteId = "emp_1",
                nombreSolicitante = "Carlos Ruiz",
                empleadoCompaneroId = "emp_2",
                nombreCompanero = "María López"
            )

            assertTrue(solicitud.esTerminal, "El estado $estado debe ser terminal")

            val resultado = useCase.validarCancelacion(
                solicitante = solicitante,
                solicitud = solicitud
            )

            val invalido = assertIs<ValidacionCancelacionResult.Invalido>(resultado)
            assertFalse(invalido.mensaje.contains("RN-22"))
        }
    }

    @Test
    fun rn22_cancelacionRechazada_siTurnoYaInicio() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()
        val solicitante = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno hoy a las 08:00
        val turno1 = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            tipoTurno = "Apertura",
            horario = "08:00 - 16:00"
        )

        val solicitud = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.PENDIENTE,
            empleadoSolicitanteId = "emp_1",
            nombreSolicitante = "Carlos Ruiz",
            turnoEntregaId = "asig_1",
            turnoRecibeId = "asig_2",
            detalleTurnoEntrega = "Apertura · 2026-11-07 · 08:00 - 16:00"
        )

        // Son las 08:30 (510 minutos) del 2026-11-07
        val resultado = useCase.validarCancelacion(
            solicitante = solicitante,
            solicitud = solicitud,
            todasAsignaciones = listOf(turno1),
            fechaActual = LocalDate(2026, 11, 7),
            horaActualMinutos = 510
        )

        val invalido = assertIs<ValidacionCancelacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("ya inició o la fecha límite ha expirado", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-22"))
    }

    @Test
    fun rn22_esSolicitudVencida_detectaExpiracionPorFechaYHora() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()

        // Turno del 2026-11-07 a las 06:00
        val turno = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00"
        )

        val solicitudPendiente = SolicitudCambioTurno(
            id = "sol_1",
            estado = EstadoSolicitudCambio.PENDIENTE,
            turnoEntregaId = "asig_1"
        )

        // Caso 1: Fecha pasada (2026-11-08)
        assertTrue(
            useCase.esSolicitudVencida(
                solicitud = solicitudPendiente,
                todasAsignaciones = listOf(turno),
                fechaActual = LocalDate(2026, 11, 8),
                horaActualMinutos = 0
            )
        )

        // Caso 2: Mismo día después de la hora de inicio (06:00 = 360 min, son las 06:01 = 361 min)
        assertTrue(
            useCase.esSolicitudVencida(
                solicitud = solicitudPendiente,
                todasAsignaciones = listOf(turno),
                fechaActual = LocalDate(2026, 11, 7),
                horaActualMinutos = 361
            )
        )

        // Caso 3: Mismo día exactamente a la hora de inicio (360 min)
        assertTrue(
            useCase.esSolicitudVencida(
                solicitud = solicitudPendiente,
                todasAsignaciones = listOf(turno),
                fechaActual = LocalDate(2026, 11, 7),
                horaActualMinutos = 360
            )
        )

        // Caso 4: Mismo día antes de la hora de inicio (05:59 = 359 min)
        assertFalse(
            useCase.esSolicitudVencida(
                solicitud = solicitudPendiente,
                todasAsignaciones = listOf(turno),
                fechaActual = LocalDate(2026, 11, 7),
                horaActualMinutos = 359
            )
        )

        // Caso 5: Fecha futura (2026-11-06)
        assertFalse(
            useCase.esSolicitudVencida(
                solicitud = solicitudPendiente,
                todasAsignaciones = listOf(turno),
                fechaActual = LocalDate(2026, 11, 6),
                horaActualMinutos = 600
            )
        )
    }

    @Test
    fun rn22_esSolicitudVencida_respetaEstadosTerminalesComoAprobada() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()

        val turnoPasado = AsignacionTurno(
            id = "asig_pasada",
            empleadoId = "emp_1",
            fechaIso = "2020-01-01",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00"
        )

        val solicitudAprobada = SolicitudCambioTurno(
            id = "sol_aprobada",
            estado = EstadoSolicitudCambio.APROBADA,
            turnoEntregaId = "asig_pasada"
        )

        // Aunque el turno ya pasó hace años, como ya fue aprobada, no debe cambiar a vencida
        assertFalse(
            useCase.esSolicitudVencida(
                solicitud = solicitudAprobada,
                todasAsignaciones = listOf(turnoPasado),
                fechaActual = LocalDate(2026, 11, 7),
                horaActualMinutos = 600
            )
        )
    }

    @Test
    fun rn22_procesarVencimientos_actualizaUnicamenteSolicitudesExpiradasNoTerminales() {
        val useCase = ControlarVencimientoYCancelacionCambioUseCase()

        val turnoPasado = AsignacionTurno(
            id = "asig_pasada",
            empleadoId = "emp_1",
            fechaIso = "2026-11-01",
            tipoTurno = "Apertura",
            horario = "06:00 - 14:00"
        )
        val turnoFuturo = AsignacionTurno(
            id = "asig_futura",
            empleadoId = "emp_2",
            fechaIso = "2026-11-20",
            tipoTurno = "Cierre",
            horario = "14:00 - 22:00"
        )

        val solPendienteExpirada = SolicitudCambioTurno(
            id = "sol_expirada",
            estado = EstadoSolicitudCambio.PENDIENTE,
            turnoEntregaId = "asig_pasada"
        )
        val solAceptadaCompaneroExpirada = SolicitudCambioTurno(
            id = "sol_aceptada_expirada",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            turnoEntregaId = "asig_pasada"
        )
        val solPendienteFutura = SolicitudCambioTurno(
            id = "sol_futura",
            estado = EstadoSolicitudCambio.PENDIENTE,
            turnoEntregaId = "asig_futura"
        )
        val solAprobadaPasada = SolicitudCambioTurno(
            id = "sol_aprobada_pasada",
            estado = EstadoSolicitudCambio.APROBADA,
            turnoEntregaId = "asig_pasada"
        )

        val lista = listOf(solPendienteExpirada, solAceptadaCompaneroExpirada, solPendienteFutura, solAprobadaPasada)
        val asignaciones = listOf(turnoPasado, turnoFuturo)

        val (actualizadas, recienVencidas) = useCase.procesarVencimientos(
            solicitudes = lista,
            todasAsignaciones = asignaciones,
            fechaActual = LocalDate(2026, 11, 7),
            horaActualMinutos = 600
        )

        assertEquals(2, recienVencidas.size)
        assertTrue(recienVencidas.any { it.id == "sol_expirada" && it.estado == EstadoSolicitudCambio.VENCIDA })
        assertTrue(recienVencidas.any { it.id == "sol_aceptada_expirada" && it.estado == EstadoSolicitudCambio.VENCIDA })

        val mapActualizadas = actualizadas.associateBy { it.id }
        assertEquals(EstadoSolicitudCambio.VENCIDA, mapActualizadas["sol_expirada"]?.estado)
        assertEquals(EstadoSolicitudCambio.VENCIDA, mapActualizadas["sol_aceptada_expirada"]?.estado)
        assertEquals(EstadoSolicitudCambio.PENDIENTE, mapActualizadas["sol_futura"]?.estado)
        assertEquals(EstadoSolicitudCambio.APROBADA, mapActualizadas["sol_aprobada_pasada"]?.estado)
    }

    // =========================================================================
    // 24. PRUEBAS DE REGLAS DE REPORTE DE NOVEDADES (RN-23)
    // =========================================================================

    @Test
    fun rn23_rechazaNovedadSiColaboradorEstaInactivo() {
        val useCase = ValidarReporteNovedadUseCase()
        val empInactivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.INACTIVO
        )

        val resultado = useCase.validarReporteNovedad(
            empleado = empInactivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Retraso en transporte público",
            fechaReferencia = "2026-11-06",
            horaReal = "08:15",
            horarioProgramado = "08:00 - 16:00"
        )

        val invalido = assertIs<ValidacionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("inactiva", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-23"))
    }

    @Test
    fun rn23_rechazaNovedadSiTipoODescripcionEstanVacios() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Caso 1: Tipo en "Selecciona..."
        val resTipoInvalido = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Selecciona...",
            descripcion = "Descripción válida",
            fechaReferencia = "2026-11-06"
        )
        val invTipo = assertIs<ValidacionNovedadResult.Invalido>(resTipoInvalido)
        assertTrue(invTipo.mensaje.contains("selecciona el tipo", ignoreCase = true))

        // Caso 2: Descripción en blanco
        val resDescVacia = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Incapacidad médica",
            descripcion = "   ",
            fechaReferencia = "2026-11-06"
        )
        val invDesc = assertIs<ValidacionNovedadResult.Invalido>(resDescVacia)
        assertTrue(invDesc.mensaje.contains("descripción", ignoreCase = true))
        assertFalse(invDesc.mensaje.contains("CA-11.4"))
        assertFalse(invDesc.mensaje.contains("RN-23"))
    }

    @Test
    fun rn23_rechazaSegundaNovedadDelMismoTipoParaElMismoTurnoOFecha() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        val novedadPrevia = Novedad(
            id = "nov_1",
            tipo = "Llegada tarde",
            descripcion = "Primer retraso",
            empleadoId = "emp_1",
            empleado = "Carlos Ruiz",
            fechaReferencia = "2026-11-06",
            asignacionTurnoId = "asig_10"
        )

        val turno = AsignacionTurno(
            id = "asig_10",
            empleadoId = "emp_1",
            fechaIso = "2026-11-06",
            horario = "08:00 - 16:00"
        )

        // Intento de reportar otra novedad de "Llegada tarde" para el mismo turno/fecha
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Segundo intento de llegada tarde",
            fechaReferencia = "2026-11-06",
            horaReal = "08:20",
            horarioProgramado = "08:00 - 16:00",
            asignacionTurno = turno,
            novedadesExistentes = listOf(novedadPrevia),
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 600
        )

        val invalido = assertIs<ValidacionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("Ya existe una novedad registrada de tipo 'Llegada tarde'", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("CA-14.3"))
        assertFalse(invalido.mensaje.contains("RN-23"))
    }

    @Test
    fun rn23_permiteNovedadesDeDistintoTipoParaElMismoTurno() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Ya existe una llegada tarde
        val novedadPrevia = Novedad(
            id = "nov_1",
            tipo = "Llegada tarde",
            descripcion = "Primer retraso",
            empleadoId = "emp_1",
            empleado = "Carlos Ruiz",
            fechaReferencia = "2026-11-06",
            asignacionTurnoId = "asig_10"
        )

        val turno = AsignacionTurno(
            id = "asig_10",
            empleadoId = "emp_1",
            fechaIso = "2026-11-06",
            horario = "08:00 - 16:00"
        )

        // Se reporta una novedad de distinto tipo ("Ampliación del turno") sobre el mismo turno
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Ampliación del turno",
            descripcion = "Se cubrió cierre por alto flujo de clientes",
            fechaReferencia = "2026-11-06",
            horaReal = "18:00",
            horarioProgramado = "08:00 - 16:00",
            asignacionTurno = turno,
            novedadesExistentes = listOf(novedadPrevia),
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1100
        )

        assertIs<ValidacionNovedadResult.Valido>(resultado)
    }

    @Test
    fun rn23_rechazaNovedadSobreTurnosFuturos() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Fecha actual es 2026-11-06, se intenta reportar novedad sobre turno de 2026-11-07
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Previsión de retraso",
            fechaReferencia = "2026-11-07",
            horaReal = "08:30",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 600
        )

        val invalido = assertIs<ValidacionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("futuros", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-23"))
    }

    @Test
    fun rn23_rechazaNovedadSiTurnoDeHoyAunNoHaIniciado() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno hoy de 08:00 a 16:00 (inicio: 480 min). Hora actual: 07:30 (450 min).
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Intento antes de que empiece",
            fechaReferencia = "2026-11-07",
            horaReal = "08:15",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 7),
            horaActualMinutos = 450
        )

        val invalido = assertIs<ValidacionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("aún no ha iniciado", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-23"))
    }

    @Test
    fun rn23_permiteNovedadSiTurnoDeHoyYaInicio() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno hoy de 08:00 a 16:00 (inicio: 480 min). Hora actual: 08:30 (510 min).
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Llegada con retraso confirmada",
            fechaReferencia = "2026-11-07",
            horaReal = "08:25",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 7),
            horaActualMinutos = 510
        )

        assertIs<ValidacionNovedadResult.Valido>(resultado)
    }

    @Test
    fun rn23_rechazaNovedadSiHanPasadoMasDe48HorasExactasDesdeFinDelTurno() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno finalizó el 2026-11-05 a las 14:00 (840 min).
        // 48 horas exactas se cumplen el 2026-11-07 a las 14:00 (840 min).
        // Hora actual: 2026-11-07 a las 14:05 (845 min) -> 48h y 5min han pasado.
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Reporte extemporáneo",
            fechaReferencia = "2026-11-05",
            horaReal = "06:30",
            horarioProgramado = "06:00 - 14:00",
            fechaActual = LocalDate(2026, 11, 7),
            horaActualMinutos = 845
        )

        val invalido = assertIs<ValidacionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("48 horas", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("CA-11.4"))
        assertFalse(invalido.mensaje.contains("RN-23"))
    }

    @Test
    fun rn23_permiteNovedadDentroDeLas48HorasExactasDesdeFinDelTurno() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno finalizó el 2026-11-05 a las 14:00 (840 min).
        // Hora actual: 2026-11-07 a las 13:50 (830 min) -> 47 horas y 50 minutos (dentro del límite de 48h).
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Reporte dentro del límite de 48h",
            fechaReferencia = "2026-11-05",
            horaReal = "06:30",
            horarioProgramado = "06:00 - 14:00",
            fechaActual = LocalDate(2026, 11, 7),
            horaActualMinutos = 830
        )

        assertIs<ValidacionNovedadResult.Valido>(resultado)
    }

    @Test
    fun rn23_rechazaIncoherenciaEnHorasReales() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno 08:00 - 16:00
        // Llegada tarde reportada a las 07:55 (anterior a la hora de inicio 08:00)
        val resLlegadaIncoherente = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Marcaje",
            fechaReferencia = "2026-11-06",
            horaReal = "07:55",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 600
        )
        val invLlegada = assertIs<ValidacionNovedadResult.Invalido>(resLlegadaIncoherente)
        assertTrue(invLlegada.mensaje.contains("posterior a la hora de inicio", ignoreCase = true))

        // Salida antes de tiempo reportada a las 16:30 (posterior a la hora de fin 16:00)
        val resSalidaIncoherente = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Salida antes de la hora",
            descripcion = "Marcaje",
            fechaReferencia = "2026-11-06",
            horaReal = "16:30",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1100
        )
        val invSalida = assertIs<ValidacionNovedadResult.Invalido>(resSalidaIncoherente)
        assertTrue(invSalida.mensaje.contains("anterior a la hora de finalización", ignoreCase = true))
    }

    // =========================================================================
    // 25. PRUEBAS DE COHERENCIA HORARIA Y LÍMITES OPERATIVOS (RN-24)
    // =========================================================================

    @Test
    fun rn24_ampliacionDeTurnoRechazada_siHoraRealSuperaLimiteOperativo2200() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno de Cierre: 14:00 - 22:00
        // Se reporta ampliación hasta las 22:30 (supera las 22:00)
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Ampliación del turno",
            descripcion = "Horas extra por limpieza profunda",
            fechaReferencia = "2026-11-06",
            horaReal = "22:30",
            horarioProgramado = "14:00 - 22:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1380
        )

        val invalido = assertIs<ValidacionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("límite operativo de las 22:00", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-24"))
    }

    @Test
    fun rn24_llegadaTardeRechazada_siHoraRealEsAnteriorALaAperturaOperativa0600() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Hora real absurda reportada: 05:45 (antes de la apertura operativa de las 06:00)
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Llegada tarde",
            descripcion = "Llegada",
            fechaReferencia = "2026-11-06",
            horaReal = "05:45",
            horarioProgramado = "06:00 - 14:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 600
        )

        val invalido = assertIs<ValidacionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("apertura operativa (06:00)", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-24"))
    }

    @Test
    fun rn24_ampliacionDeTurnoRechazada_siHoraRealNoSuperaFinProgramado() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno: 08:00 - 16:00. Reporta ampliación de turno pero con hora real 15:50 o 16:00
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Ampliación del turno",
            descripcion = "Intento inválido de ampliación",
            fechaReferencia = "2026-11-06",
            horaReal = "16:00",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1000
        )

        val invalido = assertIs<ValidacionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("posterior a la hora de finalización", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("RN-24"))
    }

    @Test
    fun rn24_formularioSupervisorAplicaLasMismasValidacionesDeCoherenciaYLimite2200() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_2",
            nombre = "Laura",
            apellido = "Mejía",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        val turnoCierre = AsignacionTurno(
            id = "asig_20",
            empleadoId = "emp_2",
            fechaIso = "2026-11-06",
            horario = "14:00 - 22:00"
        )

        // El supervisor intenta registrar una ampliación de jornada a las 22:45
        val resExceso22 = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Ampliación del turno",
            descripcion = "Supervisor registra horas extras nocturnas",
            fechaReferencia = "2026-11-06",
            horaReal = "22:45",
            horarioProgramado = "14:00 - 22:00",
            asignacionTurno = turnoCierre,
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1380
        )
        val invExceso = assertIs<ValidacionNovedadResult.Invalido>(resExceso22)
        assertTrue(invExceso.mensaje.contains("22:00"))

        // El supervisor intenta registrar salida antes con hora igual o posterior al fin (22:00)
        val resSalidaIncoherente = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Salida antes de la hora",
            descripcion = "Supervisor registra salida",
            fechaReferencia = "2026-11-06",
            horaReal = "22:00",
            horarioProgramado = "14:00 - 22:00",
            asignacionTurno = turnoCierre,
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1380
        )
        val invSalida = assertIs<ValidacionNovedadResult.Invalido>(resSalidaIncoherente)
        assertTrue(invSalida.mensaje.contains("anterior a la hora de finalización"))
    }

    @Test
    fun rn24_novedadesSinHoraReal_comoIncapacidadOAusencia_noExigenHoraNiLimiteOperativo() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Ausencia injustificada sin hora real debe ser válida si está dentro del plazo
        val resultadoAusencia = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Ausencia injustificada",
            descripcion = "El colaborador no se presentó a su jornada laboral",
            fechaReferencia = "2026-11-06",
            horaReal = "",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1000
        )
        assertIs<ValidacionNovedadResult.Valido>(resultadoAusencia)

        // Incapacidad médica sin hora real debe ser válida
        val resultadoIncapacidad = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Incapacidad médica",
            descripcion = "Certificado de incapacidad EPS remitido",
            fechaReferencia = "2026-11-06",
            horaReal = "",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1000
        )
        assertIs<ValidacionNovedadResult.Valido>(resultadoIncapacidad)
    }

    @Test
    fun rn24_ampliacionDeTurnoValida_dentroDelLimiteOperativo() {
        val useCase = ValidarReporteNovedadUseCase()
        val empActivo = Empleado(
            id = "emp_1",
            nombre = "Carlos",
            apellido = "Ruiz",
            sucursal = "CENTRO",
            cargo = Cargo.BARISTA,
            estado = EstadoUsuario.ACTIVO
        )

        // Turno: 08:00 - 16:00. Ampliación hasta las 18:00 (18:00 <= 22:00)
        val resultado = useCase.validarReporteNovedad(
            empleado = empActivo,
            tipoNovedad = "Ampliación del turno",
            descripcion = "Apoyo en cierre de inventario",
            fechaReferencia = "2026-11-06",
            horaReal = "18:00",
            horarioProgramado = "08:00 - 16:00",
            fechaActual = LocalDate(2026, 11, 6),
            horaActualMinutos = 1100
        )

        assertIs<ValidacionNovedadResult.Valido>(resultado)
    }

    // =========================================================================
    // 25. PRUEBAS DE GESTIÓN DE NOVEDADES Y ESTADOS (RN-25)
    // =========================================================================

    private val gestionarEstadoNovedadUseCase = GestionarEstadoNovedadUseCase()

    @Test
    fun gestionarEstadoNovedad_colaboradorPuedeEditarNovedadEnEstadoReportada() {
        val novedad = Novedad(
            id = "nov_1",
            tipo = "Llegada tarde",
            descripcion = "Retraso en transporte público",
            estado = EstadoNovedad.REPORTADA,
            empleadoId = "emp_1"
        )

        val resultado = gestionarEstadoNovedadUseCase.validarEdicionColaborador(novedad, "emp_1")
        assertIs<ValidacionGestionNovedadResult.Valido>(resultado)
    }

    @Test
    fun gestionarEstadoNovedad_colaboradorNoPuedeEditarNovedadEnEstadoRevisada() {
        val novedad = Novedad(
            id = "nov_2",
            tipo = "Llegada tarde",
            descripcion = "Retraso en transporte",
            estado = EstadoNovedad.REVISADA,
            empleadoId = "emp_1"
        )

        val resultado = gestionarEstadoNovedadUseCase.validarEdicionColaborador(novedad, "emp_1")
        val invalido = assertIs<ValidacionGestionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("revisada", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-25)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun gestionarEstadoNovedad_colaboradorNoPuedeEditarNovedadDeOtroColaborador() {
        val novedad = Novedad(
            id = "nov_3",
            tipo = "Salida antes de la hora",
            descripcion = "Cita médica urgente",
            estado = EstadoNovedad.REPORTADA,
            empleadoId = "emp_otro"
        )

        val resultado = gestionarEstadoNovedadUseCase.validarEdicionColaborador(novedad, "emp_1")
        val invalido = assertIs<ValidacionGestionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("permisos", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-25)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun gestionarEstadoNovedad_colaboradorPuedeEliminarNovedadEnEstadoReportada() {
        val novedad = Novedad(
            id = "nov_4",
            tipo = "Ampliación del turno",
            descripcion = "Apoyo en horas pico",
            estado = EstadoNovedad.REPORTADA,
            empleadoId = "emp_1"
        )

        val resultado = gestionarEstadoNovedadUseCase.validarEliminacionColaborador(novedad, "emp_1")
        assertIs<ValidacionGestionNovedadResult.Valido>(resultado)
    }

    @Test
    fun gestionarEstadoNovedad_colaboradorNoPuedeEliminarNovedadEnEstadoRevisada() {
        val novedad = Novedad(
            id = "nov_5",
            tipo = "Ampliación del turno",
            descripcion = "Apoyo en horas pico",
            estado = EstadoNovedad.REVISADA,
            empleadoId = "emp_1"
        )

        val resultado = gestionarEstadoNovedadUseCase.validarEliminacionColaborador(novedad, "emp_1")
        val invalido = assertIs<ValidacionGestionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("revisada", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-25)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun gestionarEstadoNovedad_noSePuedeRevertirEstadoDeRevisadaAReportada() {
        val resultado = gestionarEstadoNovedadUseCase.validarTransicionEstado(
            estadoActual = EstadoNovedad.REVISADA,
            nuevoEstado = EstadoNovedad.REPORTADA
        )
        val invalido = assertIs<ValidacionGestionNovedadResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("revertirse", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-25)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun gestionarEstadoNovedad_revisionAdministradorConservaDatosOriginales() {
        val novedadOriginal = Novedad(
            id = "nov_6",
            tipo = "Llegada tarde",
            descripcion = "Demora en metro",
            horaReal = "08:20",
            fechaReferencia = "2026-11-06",
            empleadoId = "emp_1",
            estado = EstadoNovedad.REPORTADA
        )

        // Revisión válida: solo agrega comentario y datos de revisor
        val revisionValida = novedadOriginal.revisar(
            comentario = "Justificación aceptada",
            revisor = "Admin",
            fecha = "2026-11-07"
        )
        val resValido = gestionarEstadoNovedadUseCase.validarRevisionAdministrador(novedadOriginal, revisionValida)
        assertIs<ValidacionGestionNovedadResult.Valido>(resValido)

        // Revisión inválida: altera descripción original reportada por el colaborador
        val revisionInvalida = revisionValida.copy(descripcion = "Descripción modificada por el admin")
        val resInvalido = gestionarEstadoNovedadUseCase.validarRevisionAdministrador(novedadOriginal, revisionInvalida)
        val invalido = assertIs<ValidacionGestionNovedadResult.Invalido>(resInvalido)
        assertTrue(invalido.mensaje.contains("alterar ni borrar", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-25)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun gestionarEstadoNovedad_novedadRegistradaPorSupervisorNaceInmediatamenteEnEstadoRevisada() {
        val novedadBase = Novedad(
            id = "nov_sup_1",
            tipo = "Ausencia justificada",
            descripcion = "Calamidad doméstica informada por teléfono",
            empleadoId = "emp_1",
            fechaReferencia = "2026-11-06"
        )

        val preparada = gestionarEstadoNovedadUseCase.prepararNovedadDirectaSupervisor(
            novedadBase = novedadBase,
            revisor = "Supervisor Carlos",
            fechaRevision = "2026-11-06",
            comentarioSupervisor = "Registrada y aprobada directamente por supervisión"
        )

        assertEquals(EstadoNovedad.REVISADA, preparada.estado)
        assertTrue(preparada.reportadoPorSupervisor)
        assertEquals("Supervisor Carlos", preparada.revisadoPor)
        assertEquals("2026-11-06", preparada.fechaRevision)
        assertEquals("Registrada y aprobada directamente por supervisión", preparada.comentarioRevision)
    }

    @Test
    fun novedadRepository_gestionDeNovedades_eliminarBloqueadoEnRevisadaYPermitidoEnReportada() = runBlocking {
        val novReportada = Novedad(
            id = "nov_colab_1",
            tipo = "Llegada tarde",
            descripcion = "Retraso en ruta",
            horaReal = "08:15",
            estado = EstadoNovedad.REPORTADA,
            empleadoId = "emp_1",
            fechaReferencia = "2026-11-06"
        )
        val novRevisada = Novedad(
            id = "nov_colab_2",
            tipo = "Salida antes de la hora",
            descripcion = "Permiso concedido",
            horaReal = "15:30",
            estado = EstadoNovedad.REVISADA,
            empleadoId = "emp_1",
            fechaReferencia = "2026-11-06"
        )

        val fakeRepo = FakeNovedadRepository(listOf(novReportada, novRevisada))

        // 1. Eliminación en REVISADA falla y no elimina
        val resEliminarRevisada = fakeRepo.eliminarNovedad("nov_colab_2")
        assertTrue(resEliminarRevisada.isFailure)
        assertEquals(2, fakeRepo.novedades.value.size)

        // 2. Eliminación en REPORTADA tiene éxito
        val resEliminarReportada = fakeRepo.eliminarNovedad("nov_colab_1")
        assertTrue(resEliminarReportada.isSuccess)
        assertEquals(1, fakeRepo.novedades.value.size)
        assertEquals("nov_colab_2", fakeRepo.novedades.value.first().id)
    }

    @Test
    fun novedadRepository_transicionEstado_noPermiteRevertirDeRevisadaAReportada() {
        val novRevisada = Novedad(
            id = "nov_rev_1",
            tipo = "Llegada tarde",
            descripcion = "Demora en transporte",
            estado = EstadoNovedad.REVISADA,
            empleadoId = "emp_1"
        )
        val fakeRepo = FakeNovedadRepository(listOf(novRevisada))

        // Intento de cambiar estado a REPORTADA
        fakeRepo.cambiarEstadoNovedad("nov_rev_1", EstadoNovedad.REPORTADA)

        // Sigue manteniéndose en REVISADA
        assertEquals(EstadoNovedad.REVISADA, fakeRepo.novedades.value.first().estado)
    }

    // =========================================================================
    // 26. PRUEBAS DE REGISTRO DE ASISTENCIA Y PAUSAS DE DESCANSO (RN-27)
    // =========================================================================

    @Test
    fun validarMarcacion_bloqueaSiEmpleadoInactivo() {
        val empleadoInactivo = Empleado(
            id = "emp_inactivo",
            nombre = "Carlos",
            apellido = "Pérez",
            estado = EstadoUsuario.INACTIVO
        )
        val asignacion = AsignacionTurno(
            id = "asig_1",
            empleadoId = "emp_inactivo",
            fechaIso = "2026-11-07",
            tipoTurno = "Turno Mañana",
            horario = "08:00 - 16:00",
            estado = "PUBLICADO"
        )

        val resultado = validarMarcacionAsistenciaUseCase.validarHabilitacionTurno(
            empleado = empleadoInactivo,
            asignacion = asignacion,
            fechaMarcacionIso = "2026-11-07"
        )

        val invalido = assertIs<ValidacionMarcacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("inactivo", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-27)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarMarcacion_bloqueaSiNoHayTurnoOFechaDiferente() {
        val empleado = Empleado(id = "emp_1", nombre = "Ana", estado = EstadoUsuario.ACTIVO)

        // 1. Sin turno asignado
        val sinTurno = validarMarcacionAsistenciaUseCase.validarHabilitacionTurno(
            empleado = empleado,
            asignacion = null,
            fechaMarcacionIso = "2026-11-07"
        )
        val invSinTurno = assertIs<ValidacionMarcacionResult.Invalido>(sinTurno)
        assertTrue(invSinTurno.mensaje.contains("No tienes un turno programado", ignoreCase = true))
        assertFalse(invSinTurno.mensaje.contains("(RN-27)"))

        // 2. Turno asignado para fecha diferente (ayer o mañana)
        val asignacionOtroDia = AsignacionTurno(
            id = "asig_otro",
            empleadoId = "emp_1",
            fechaIso = "2026-11-08",
            tipoTurno = "Turno Tarde",
            horario = "14:00 - 22:00",
            estado = "PUBLICADO"
        )
        val invFecha = validarMarcacionAsistenciaUseCase.validarHabilitacionTurno(
            empleado = empleado,
            asignacion = asignacionOtroDia,
            fechaMarcacionIso = "2026-11-07"
        )
        val invOtroDia = assertIs<ValidacionMarcacionResult.Invalido>(invFecha)
        assertTrue(invOtroDia.mensaje.contains("Solo puedes registrar asistencia para el turno correspondiente a la fecha de hoy", ignoreCase = true))
        assertFalse(invOtroDia.mensaje.contains("(RN-27)"))
    }

    @Test
    fun validarMarcacion_bloqueaSiTurnoEnBorrador() {
        val empleado = Empleado(id = "emp_1", nombre = "Ana", estado = EstadoUsuario.ACTIVO)
        val asignacionBorrador = AsignacionTurno(
            id = "asig_borrador",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            tipoTurno = "Turno Mañana",
            horario = "06:00 - 14:00",
            estado = "BORRADOR"
        )

        val resultado = validarMarcacionAsistenciaUseCase.validarHabilitacionTurno(
            empleado = empleado,
            asignacion = asignacionBorrador,
            fechaMarcacionIso = "2026-11-07"
        )

        val invalido = assertIs<ValidacionMarcacionResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("borrador", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-27)"))
    }

    @Test
    fun validarMarcacion_bloqueaSiEsDescansoOIncapacidadOVacacionesOPermiso() {
        val empleado = Empleado(id = "emp_1", nombre = "Ana", estado = EstadoUsuario.ACTIVO)

        // Día de descanso
        val asigDescanso = AsignacionTurno(
            id = "asig_desc",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            tipoTurno = "Descanso",
            esDescanso = true,
            estado = "PUBLICADO"
        )
        val resDesc = validarMarcacionAsistenciaUseCase.validarHabilitacionTurno(empleado, asigDescanso, "2026-11-07")
        val invDesc = assertIs<ValidacionMarcacionResult.Invalido>(resDesc)
        assertTrue(invDesc.mensaje.contains("día libre", ignoreCase = true))
        assertFalse(invDesc.mensaje.contains("(RN-27)"))

        // Incapacidad médica
        val asigIncapacidad = AsignacionTurno(
            id = "asig_incap",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            tipoTurno = "Incapacidad médica",
            estado = "PUBLICADO"
        )
        val resIncap = validarMarcacionAsistenciaUseCase.validarHabilitacionTurno(empleado, asigIncapacidad, "2026-11-07")
        val invIncap = assertIs<ValidacionMarcacionResult.Invalido>(resIncap)
        assertTrue(invIncap.mensaje.contains("incapacidad médica", ignoreCase = true))
        assertFalse(invIncap.mensaje.contains("(RN-27)"))

        // Vacaciones
        val asigVacaciones = AsignacionTurno(
            id = "asig_vac",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            tipoTurno = "Vacaciones",
            estado = "PUBLICADO"
        )
        val resVac = validarMarcacionAsistenciaUseCase.validarHabilitacionTurno(empleado, asigVacaciones, "2026-11-07")
        val invVac = assertIs<ValidacionMarcacionResult.Invalido>(resVac)
        assertTrue(invVac.mensaje.contains("vacaciones", ignoreCase = true))
        assertFalse(invVac.mensaje.contains("(RN-27)"))

        // Permiso especial
        val asigPermiso = AsignacionTurno(
            id = "asig_perm",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            tipoTurno = "Permiso especial",
            estado = "PUBLICADO"
        )
        val resPerm = validarMarcacionAsistenciaUseCase.validarHabilitacionTurno(empleado, asigPermiso, "2026-11-07")
        val invPerm = assertIs<ValidacionMarcacionResult.Invalido>(resPerm)
        assertTrue(invPerm.mensaje.contains("permiso especial", ignoreCase = true))
        assertFalse(invPerm.mensaje.contains("(RN-27)"))
    }

    @Test
    fun validarMarcacion_secuenciaEstrictaDeEstados() {
        // Desde SIN_INICIAR: solo se puede pasar a EN_TURNO
        assertIs<ValidacionMarcacionResult.Valido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.SIN_INICIAR, EstadoAsistencia.EN_TURNO)
        )
        assertIs<ValidacionMarcacionResult.Invalido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.SIN_INICIAR, EstadoAsistencia.EN_DESCANSO)
        )
        assertIs<ValidacionMarcacionResult.Invalido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.SIN_INICIAR, EstadoAsistencia.FINALIZADO)
        )

        // Desde EN_TURNO: se puede pausar a EN_DESCANSO o finalizar a FINALIZADO
        assertIs<ValidacionMarcacionResult.Valido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.EN_TURNO, EstadoAsistencia.EN_DESCANSO)
        )
        assertIs<ValidacionMarcacionResult.Valido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.EN_TURNO, EstadoAsistencia.FINALIZADO)
        )
        assertIs<ValidacionMarcacionResult.Invalido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.EN_TURNO, EstadoAsistencia.SIN_INICIAR)
        )

        // Desde EN_DESCANSO: solo se puede reanudar a EN_TURNO
        assertIs<ValidacionMarcacionResult.Valido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.EN_DESCANSO, EstadoAsistencia.EN_TURNO)
        )
        assertIs<ValidacionMarcacionResult.Invalido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.EN_DESCANSO, EstadoAsistencia.FINALIZADO)
        )
        assertIs<ValidacionMarcacionResult.Invalido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.EN_DESCANSO, EstadoAsistencia.SIN_INICIAR)
        )

        // Desde FINALIZADO: estado terminal, ninguna acción posterior es válida
        assertIs<ValidacionMarcacionResult.Invalido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.FINALIZADO, EstadoAsistencia.EN_TURNO)
        )
        assertIs<ValidacionMarcacionResult.Invalido>(
            validarMarcacionAsistenciaUseCase.validarTransicionEstado(EstadoAsistencia.FINALIZADO, EstadoAsistencia.EN_DESCANSO)
        )
    }

    @Test
    fun validarMarcacion_manejoDeMultiplesDescansosSinSobreescribir() {
        val listaInicial = emptyList<PausaDescanso>()

        // 1. Iniciar primer descanso a las 11:00
        val paso1 = validarMarcacionAsistenciaUseCase.agregarInicioDescanso(listaInicial, "11:00")
        assertEquals(1, paso1.size)
        assertEquals("11:00", paso1.first().horaInicio)
        assertEquals("-", paso1.first().horaFin)

        // 2. Finalizar primer descanso a las 11:30
        val paso2 = validarMarcacionAsistenciaUseCase.registrarFinDescanso(paso1, "11:30")
        assertEquals(1, paso2.size)
        assertEquals("11:00", paso2.first().horaInicio)
        assertEquals("11:30", paso2.first().horaFin)

        // 3. Iniciar segundo descanso a las 15:00 sin sobreescribir el primero
        val paso3 = validarMarcacionAsistenciaUseCase.agregarInicioDescanso(paso2, "15:00")
        assertEquals(2, paso3.size)
        // Primer descanso preservado
        assertEquals("11:00", paso3[0].horaInicio)
        assertEquals("11:30", paso3[0].horaFin)
        // Segundo descanso en curso
        assertEquals("15:00", paso3[1].horaInicio)
        assertEquals("-", paso3[1].horaFin)

        // 4. Finalizar segundo descanso a las 15:15
        val paso4 = validarMarcacionAsistenciaUseCase.registrarFinDescanso(paso3, "15:15")
        assertEquals(2, paso4.size)
        assertEquals("11:00", paso4[0].horaInicio)
        assertEquals("11:30", paso4[0].horaFin)
        assertEquals("15:00", paso4[1].horaInicio)
        assertEquals("15:15", paso4[1].horaFin)
    }

    @Test
    fun validarMarcacion_calculoMinutosDescansoAcumulado() {
        val pausas = listOf(
            PausaDescanso(id = "1", horaInicio = "10:00", horaFin = "10:30"), // 30 min
            PausaDescanso(id = "2", horaInicio = "14:00", horaFin = "14:45"), // 45 min
            PausaDescanso(id = "3", horaInicio = "17:00", horaFin = "-")      // en curso, 0 min
        )

        val minutosTotal = validarMarcacionAsistenciaUseCase.calcularMinutosDescanso(pausas)
        assertEquals(75, minutosTotal)
    }

    @Test
    fun registroAsistencia_retrocompatibilidadYListaPausas() {
        // Caso 1: Registro legacy sin lista descansos pero con inicioDescanso y finDescanso
        val legacy = RegistroAsistencia(
            id = "reg_1",
            empleadoId = "emp_1",
            horaEntrada = "08:00",
            inicioDescanso = "12:00",
            finDescanso = "12:40",
            horaSalida = "16:00"
        )
        val pausasLegacy = legacy.listaPausas
        assertEquals(1, pausasLegacy.size)
        assertEquals("12:00", pausasLegacy.first().horaInicio)
        assertEquals("12:40", pausasLegacy.first().horaFin)

        // Caso 2: Registro moderno con descansos explícitos
        val moderno = RegistroAsistencia(
            id = "reg_2",
            empleadoId = "emp_1",
            descansos = listOf(
                PausaDescanso(id = "p1", horaInicio = "10:00", horaFin = "10:15"),
                PausaDescanso(id = "p2", horaInicio = "13:00", horaFin = "13:30")
            )
        )
        assertEquals(2, moderno.listaPausas.size)
        assertEquals("10:00", moderno.listaPausas[0].horaInicio)
        assertEquals("13:30", moderno.listaPausas[1].horaFin)
    }

    // =========================================================================
    // 27. PRUEBAS DE INMUTABILIDAD DE MARCAS Y REGISTRO DE SUPERVISOR (RN-28)
    // =========================================================================

    @Test
    fun validarMarcaSupervisor_rechazaSiMotivoEnBlanco() {
        val registro = RegistroAsistencia(
            id = "reg_1",
            empleadoId = "emp_1",
            horaEntrada = "08:00",
            horaSalida = "-"
        )

        val resultadoVacio = validarMarcaSupervisorUseCase.validar(
            registroActual = registro,
            tipoMarca = "SALIDA",
            horaMarca = "16:00",
            motivo = "   "
        )
        val invalido = assertIs<ValidacionMarcaSupervisorResult.Invalido>(resultadoVacio)
        assertTrue(invalido.mensaje.contains("motivo", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-28)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarMarcaSupervisor_rechazaFormatoHoraInvalido() {
        val registro = RegistroAsistencia(id = "reg_1", empleadoId = "emp_1")

        val res1 = validarMarcaSupervisorUseCase.validar(registro, "ENTRADA", "25:00", "Olvido de tarjeta")
        val inv1 = assertIs<ValidacionMarcaSupervisorResult.Invalido>(res1)
        assertTrue(inv1.mensaje.contains("formato válido", ignoreCase = true))
        assertFalse(inv1.mensaje.contains("(RN-28)"))

        val res2 = validarMarcaSupervisorUseCase.validar(registro, "ENTRADA", "ocho y media", "Falla en lector")
        val inv2 = assertIs<ValidacionMarcaSupervisorResult.Invalido>(res2)
        assertTrue(inv2.mensaje.contains("formato válido", ignoreCase = true))
        assertFalse(inv2.mensaje.contains("(RN-28)"))
    }

    @Test
    fun validarMarcaSupervisor_bloqueaSobrescribirMarcaRegistradaPorEmpleado() {
        // El empleado registró entrada
        val registro = RegistroAsistencia(
            id = "reg_1",
            empleadoId = "emp_1",
            horaEntrada = "08:05",
            entradaPorSupervisor = false,
            registradoPorSupervisor = false
        )

        // El supervisor intenta sobrescribir la entrada del empleado
        val resultado = validarMarcaSupervisorUseCase.validar(
            registroActual = registro,
            tipoMarca = "ENTRADA",
            horaMarca = "08:00",
            motivo = "Ajuste de hora acordado"
        )

        val invalido = assertIs<ValidacionMarcaSupervisorResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("colaborador", ignoreCase = true))
        assertTrue(invalido.mensaje.contains("inmutable", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-28)"))
    }

    @Test
    fun validarMarcaSupervisor_permiteRegistrarOmisionFaltante() {
        // El empleado registró entrada, pero olvidó marcar salida
        val registro = RegistroAsistencia(
            id = "reg_1",
            empleadoId = "emp_1",
            horaEntrada = "08:00",
            horaSalida = "-",
            entradaPorSupervisor = false
        )

        // El supervisor completa la marca faltante de salida
        val resultado = validarMarcaSupervisorUseCase.validar(
            registroActual = registro,
            tipoMarca = "SALIDA",
            horaMarca = "16:00",
            motivo = "Colaborador olvidó registrar salida al culminar turno"
        )

        assertIs<ValidacionMarcaSupervisorResult.Valido>(resultado)
    }

    @Test
    fun registroAsistencia_trazabilidadIndividualEvitaSobreescrituraCruzada() {
        // 1. Empleado registró entrada
        val registroInicial = RegistroAsistencia(
            id = "reg_1",
            empleadoId = "emp_1",
            horaEntrada = "08:00",
            horaSalida = "-",
            entradaPorSupervisor = false,
            registradoPorSupervisor = false
        )
        assertTrue(registroInicial.fueRegistradaPorEmpleado("ENTRADA"))
        assertFalse(registroInicial.fueRegistradaPorSupervisor("ENTRADA"))

        // 2. Supervisor completó la salida faltante
        val registroConSalidaSupervisor = registroInicial.copy(
            horaSalida = "16:00",
            salidaPorSupervisor = true,
            registradoPorSupervisor = true,
            motivoSupervisor = "Omisión de salida",
            supervisorNombre = "Supervisor Jefe"
        )

        // Verificación de trazabilidad por marca individual
        assertTrue(registroConSalidaSupervisor.fueRegistradaPorEmpleado("ENTRADA"), "La entrada sigue perteneciendo al empleado")
        assertFalse(registroConSalidaSupervisor.fueRegistradaPorSupervisor("ENTRADA"))
        assertFalse(registroConSalidaSupervisor.fueRegistradaPorEmpleado("SALIDA"))
        assertTrue(registroConSalidaSupervisor.fueRegistradaPorSupervisor("SALIDA"), "La salida fue registrada por el supervisor")

        // 3. Intento posterior de sobrescribir la entrada original del empleado
        val intentoModificarEntrada = validarMarcaSupervisorUseCase.validar(
            registroActual = registroConSalidaSupervisor,
            tipoMarca = "ENTRADA",
            horaMarca = "07:50",
            motivo = "Intento de corrección"
        )
        val inv = assertIs<ValidacionMarcaSupervisorResult.Invalido>(intentoModificarEntrada)
        assertTrue(inv.mensaje.contains("inmutable", ignoreCase = true))
        assertFalse(inv.mensaje.contains("(RN-28)"))
    }

    @Test
    fun validarMarcaSupervisor_protegeTodasLasMarcasDeEmpleado() {
        val registroCompletoEmpleado = RegistroAsistencia(
            id = "reg_full",
            empleadoId = "emp_1",
            horaEntrada = "08:00",
            inicioDescanso = "12:00",
            finDescanso = "13:00",
            horaSalida = "16:00",
            entradaPorSupervisor = false,
            salidaPorSupervisor = false,
            inicioDescansoPorSupervisor = false,
            finDescansoPorSupervisor = false
        )

        for (marca in listOf("ENTRADA", "INICIO_DESCANSO", "FIN_DESCANSO", "SALIDA")) {
            val res = validarMarcaSupervisorUseCase.validar(
                registroActual = registroCompletoEmpleado,
                tipoMarca = marca,
                horaMarca = "10:00",
                motivo = "Modificación de prueba"
            )
            val inv = assertIs<ValidacionMarcaSupervisorResult.Invalido>(res)
            assertTrue(inv.mensaje.contains("inmutable", ignoreCase = true))
            assertFalse(inv.mensaje.contains("(RN-28)"))
        }
    }

    // =========================================================================
    // 28. PRUEBAS DE REGISTRO MANUAL DE SUPERVISOR Y ÁMBITO DE SUCURSAL (RN-29)
    // =========================================================================

    @Test
    fun validarPermisoSupervisor_bloqueaSiNoHaySesionActiva() {
        val resultado = validarMarcaSupervisorUseCase.validarPermisoSupervisor(
            usuarioActivo = null,
            sucursalRegistro = "CENTRO"
        )
        val invalido = assertIs<ValidacionMarcaSupervisorResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("iniciar sesión", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-29)"), "No debe exponer identificadores técnicos de regla")
    }

    @Test
    fun validarPermisoSupervisor_bloqueaSiUsuarioEsEmpleado() {
        val usuarioEmpleado = UsuarioActual(
            id = "user_emp",
            nombre = "Pedro Empleado",
            correo = "pedro@empresa.com",
            sucursal = "CENTRO",
            iniciales = "PE",
            cargo = "Barista",
            rol = RolUsuario.EMPLEADO
        )

        val resultado = validarMarcaSupervisorUseCase.validarPermisoSupervisor(
            usuarioActivo = usuarioEmpleado,
            sucursalRegistro = "CENTRO"
        )
        val invalido = assertIs<ValidacionMarcaSupervisorResult.Invalido>(resultado)
        assertTrue(invalido.mensaje.contains("permisos", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-29)"))
    }

    @Test
    fun validarPermisoSupervisor_adminSucursalSoloPuedeMarcarEnSuSucursal() {
        val adminSucursalSur = UsuarioActual(
            id = "admin_sur",
            nombre = "Marta Admin Sur",
            correo = "marta@empresa.com",
            sucursal = "SUR",
            iniciales = "MS",
            cargo = "Administrador de Sucursal",
            rol = RolUsuario.ADMINISTRADOR_SUCURSAL
        )

        // 1. Intento en otra sucursal ("CENTRO") -> Bloqueado
        val resOtraSucursal = validarMarcaSupervisorUseCase.validarPermisoSupervisor(
            usuarioActivo = adminSucursalSur,
            sucursalRegistro = "CENTRO"
        )
        val invalido = assertIs<ValidacionMarcaSupervisorResult.Invalido>(resOtraSucursal)
        assertTrue(invalido.mensaje.contains("SUR", ignoreCase = true))
        assertFalse(invalido.mensaje.contains("(RN-29)"))

        // 2. Intento en su propia sucursal ("SUR") -> Permitido
        val resPropiaSucursal = validarMarcaSupervisorUseCase.validarPermisoSupervisor(
            usuarioActivo = adminSucursalSur,
            sucursalRegistro = "SUR"
        )
        assertIs<ValidacionMarcaSupervisorResult.Valido>(resPropiaSucursal)
    }

    @Test
    fun validarPermisoSupervisor_adminGeneralPuedeMarcarEnCualquierSucursal() {
        val adminGeneral = UsuarioActual(
            id = "admin_gen",
            nombre = "Carlos Admin General",
            correo = "carlos@empresa.com",
            sucursal = "CENTRO",
            iniciales = "CG",
            cargo = "Administrador General",
            rol = RolUsuario.ADMINISTRADOR_GENERAL
        )

        for (sucursal in listOf("CENTRO", "NORTE", "SUR")) {
            val res = validarMarcaSupervisorUseCase.validarPermisoSupervisor(
                usuarioActivo = adminGeneral,
                sucursalRegistro = sucursal
            )
            assertIs<ValidacionMarcaSupervisorResult.Valido>(res)
        }
    }

    @Test
    fun registroAsistencia_persisteSupervisorIdYNombreConTrazabilidadIndividual() {
        val registro = RegistroAsistencia(
            id = "reg_test",
            empleadoId = "emp_1",
            horaEntrada = "08:00",
            horaSalida = "16:00",
            salidaPorSupervisor = true,
            registradoPorSupervisor = true,
            motivoSupervisor = "Omisión de marcación de salida",
            supervisorId = "usr_sup_42",
            supervisorNombre = "Admin Juan"
        )

        assertEquals("usr_sup_42", registro.supervisorId)
        assertEquals("Admin Juan", registro.supervisorNombre)
        assertEquals("Omisión de marcación de salida", registro.motivoSupervisor)
        assertTrue(registro.salidaPorSupervisor)
        assertFalse(registro.entradaPorSupervisor)
    }

    // =========================================================================
    // 29. PRUEBAS DE CÁLCULO DE TIEMPO EFECTIVO Y ESTADO PENDIENTE DE CIERRE (RN-30)
    // =========================================================================

    @Test
    fun rn30_formulaTiempoEfectivo_descuentaPausasDeDescansoCorrectamente() {
        val pausas = listOf(
            PausaDescanso(id = "1", horaInicio = "10:00", horaFin = "10:15"), // 15 min
            PausaDescanso(id = "2", horaInicio = "13:00", horaFin = "13:45")  // 45 min
        )
        // 08:00 a 17:00 = 9 horas (540 minutos)
        // 540 - (15 + 45) = 480 minutos (8 horas)
        val minutosNetos = calcularJornadaUseCase.calcularMinutosTrabajados(
            horaEntrada = "08:00",
            horaSalida = "17:00",
            descansos = pausas
        )
        assertEquals(480, minutosNetos)
    }

    @Test
    fun rn30_formulaTiempoEfectivo_sinDescansos_retornaTotalBruto() {
        val minutosNetos = calcularJornadaUseCase.calcularMinutosTrabajados(
            horaEntrada = "08:00",
            horaSalida = "16:00",
            descansos = emptyList()
        )
        assertEquals(480, minutosNetos)
    }

    @Test
    fun rn30_turnoConEntradaSinSalida_noPasaAFinalizadoDuranteTurno() {
        // Turno en curso dentro del horario (hoy 2026-11-07, programado 08:00 - 16:00, hora actual 12:00 = 720 min)
        val registro = RegistroAsistencia(
            id = "reg_1",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            horarioProgramado = "08:00 - 16:00",
            horaEntrada = "08:00",
            horaSalida = "-",
            estadoAsistencia = EstadoAsistencia.EN_TURNO
        )

        val evaluacion = calcularJornadaUseCase.evaluarJornada(
            registro = registro,
            fechaActualIso = "2026-11-07",
            horaActualMinutos = 12 * 60
        )

        assertFalse(evaluacion.esPendienteDeCierre)
        assertEquals("PRESENTE", evaluacion.estadoTexto)
        assertEquals(EstadoAsistencia.EN_TURNO, evaluacion.estadoAsistencia)
        assertEquals(0, evaluacion.tiempoEfectivoMinutos)
    }

    @Test
    fun rn30_turnoSinSalida_cuandoExpiraElHorarioProgramado_pasaAPendienteDeCierre() {
        // Horario programado termina a las 16:00 (960 min).
        // Hora actual: 16:45 (1005 min, sobrepasa fin + 30 min de tolerancia).
        val registro = RegistroAsistencia(
            id = "reg_1",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            horarioProgramado = "08:00 - 16:00",
            horaEntrada = "08:00",
            horaSalida = "-",
            estadoAsistencia = EstadoAsistencia.EN_TURNO
        )

        val evaluacion = calcularJornadaUseCase.evaluarJornada(
            registro = registro,
            fechaActualIso = "2026-11-07",
            horaActualMinutos = 16 * 60 + 45
        )

        assertTrue(evaluacion.esPendienteDeCierre, "Debe marcarse como pendiente de cierre si expiró el horario")
        assertEquals("PENDIENTE DE CIERRE", evaluacion.estadoTexto)
        assertEquals(0, evaluacion.tiempoEfectivoMinutos, "Tiempo efectivo debe ser 0 mientras no haya salida formal")
    }

    @Test
    fun rn30_turnoSinSalida_cuandoTerminaElDia_pasaAPendienteDeCierre() {
        // Turno del día anterior ("2026-11-06"), hoy es "2026-11-07"
        val registro = RegistroAsistencia(
            id = "reg_ayer",
            empleadoId = "emp_1",
            fechaIso = "2026-11-06",
            horarioProgramado = "08:00 - 16:00",
            horaEntrada = "08:05",
            horaSalida = "-",
            estadoAsistencia = EstadoAsistencia.EN_TURNO
        )

        val evaluacion = calcularJornadaUseCase.evaluarJornada(
            registro = registro,
            fechaActualIso = "2026-11-07",
            horaActualMinutos = 9 * 60
        )

        assertTrue(evaluacion.esPendienteDeCierre, "Debe marcarse como pendiente de cierre si el día ya culminó")
        assertEquals("PENDIENTE DE CIERRE", evaluacion.estadoTexto)
        assertEquals(0, evaluacion.tiempoEfectivoMinutos, "Tiempo efectivo debe permanecer en 0 hasta su resolución")
    }

    @Test
    fun rn30_turnoCerradoConSalida_calculaTiempoEfectivoNetoYEstadoFinalizado() {
        val pausas = listOf(
            PausaDescanso(id = "1", horaInicio = "12:00", horaFin = "12:30") // 30 min
        )
        // 08:00 a 16:30 = 8h 30m (510 min) - 30 min descanso = 480 min (8 horas efectivas)
        val registro = RegistroAsistencia(
            id = "reg_cerrado",
            empleadoId = "emp_1",
            fechaIso = "2026-11-07",
            horarioProgramado = "08:00 - 16:00",
            horaEntrada = "08:00",
            horaSalida = "16:30",
            descansos = pausas,
            estadoAsistencia = EstadoAsistencia.EN_TURNO
        )

        val evaluacion = calcularJornadaUseCase.evaluarJornada(
            registro = registro,
            fechaActualIso = "2026-11-07",
            horaActualMinutos = 17 * 60
        )

        assertFalse(evaluacion.esPendienteDeCierre)
        assertEquals("FINALIZADO", evaluacion.estadoTexto)
        assertEquals(EstadoAsistencia.FINALIZADO, evaluacion.estadoAsistencia)
        assertEquals(480, evaluacion.tiempoEfectivoMinutos)
    }

    @Test
    fun rn30_mensajesYEstados_noContienenIdentificadoresTecnicosDeRegla() {
        val registro = RegistroAsistencia(
            id = "reg_test",
            empleadoId = "emp_1",
            fechaIso = "2026-11-06",
            horarioProgramado = "08:00 - 16:00",
            horaEntrada = "08:00",
            horaSalida = "-"
        )
        val evaluacion = calcularJornadaUseCase.evaluarJornada(registro, "2026-11-07", 10 * 60)
        assertFalse(evaluacion.estadoTexto.contains("RN-30"), "No debe exponer identificadores técnicos de regla en texto")
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

private class FakeNovedadRepository(
    novedadesIniciales: List<Novedad> = emptyList()
) : NovedadRepositoryContract {
    private val _novedades = MutableStateFlow(novedadesIniciales)
    override val novedades: StateFlow<List<Novedad>> = _novedades

    override fun iniciarSincronizacionFirestore(forzar: Boolean) {}
    override fun detenerSincronizacion() {}

    override suspend fun guardarNovedad(novedad: Novedad): Result<Unit> {
        val actual = _novedades.value.toMutableList()
        val idx = actual.indexOfFirst { it.id == novedad.id }
        if (idx >= 0) actual[idx] = novedad else actual.add(0, novedad)
        _novedades.value = actual
        return Result.success(Unit)
    }

    override fun reportarNovedad(novedad: Novedad) {
        val actual = _novedades.value.toMutableList()
        val idx = actual.indexOfFirst { it.id == novedad.id }
        if (idx >= 0) actual[idx] = novedad else actual.add(0, novedad)
        _novedades.value = actual
    }

    override fun revisarNovedad(idNovedad: String, comentario: String, revisor: String, fecha: String) {
        val actual = _novedades.value.toMutableList()
        val idx = actual.indexOfFirst { it.id == idNovedad }
        if (idx >= 0) {
            actual[idx] = actual[idx].revisar(comentario, revisor, fecha)
            _novedades.value = actual
        }
    }

    override fun cambiarEstadoNovedad(idNovedad: String, nuevoEstado: EstadoNovedad) {
        val actual = _novedades.value.toMutableList()
        val idx = actual.indexOfFirst { it.id == idNovedad }
        if (idx >= 0) {
            if (actual[idx].estado == EstadoNovedad.REVISADA && nuevoEstado == EstadoNovedad.REPORTADA) {
                return
            }
            actual[idx] = actual[idx].copy(estado = nuevoEstado)
            _novedades.value = actual
        }
    }

    override suspend fun eliminarNovedad(idNovedad: String): Result<Unit> {
        val actual = _novedades.value.toMutableList()
        val nov = actual.find { it.id == idNovedad }
        if (nov != null && nov.estado == EstadoNovedad.REVISADA) {
            return Result.failure(IllegalStateException("La novedad ya ha sido revisada y no puede ser eliminada."))
        }
        actual.removeAll { it.id == idNovedad }
        _novedades.value = actual
        return Result.success(Unit)
    }
}

