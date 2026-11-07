package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.*
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import com.itm.gestordeturnos.domain.usecase.AplicarIncapacidadEnTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.CalcularDescansosTurnoUseCase
import com.itm.gestordeturnos.domain.usecase.ControlInactividadSesionUseCase
import com.itm.gestordeturnos.domain.usecase.ControlIntentosLoginUseCase
import com.itm.gestordeturnos.domain.usecase.EvaluacionIntentoLogin
import com.itm.gestordeturnos.domain.usecase.IntercambiarTurnosUseCase
import com.itm.gestordeturnos.domain.usecase.ValidacionAdminSucursalEmpleadoResult
import com.itm.gestordeturnos.domain.usecase.ValidacionClaveResult
import com.itm.gestordeturnos.domain.usecase.ValidacionEstadoEmpleadoResult
import com.itm.gestordeturnos.domain.usecase.ValidacionPermisosEdicionResult
import com.itm.gestordeturnos.domain.usecase.ValidacionTurnoResult
import com.itm.gestordeturnos.domain.usecase.ValidarContrasenaFirebaseAuthUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarEstadoEmpleadoActivoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarModificacionRolEstadoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarPermisosAdminSucursalEmpleadoUseCase
import com.itm.gestordeturnos.domain.usecase.ValidarRecuperacionContrasenaUseCase
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
