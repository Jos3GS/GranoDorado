package com.itm.gestordeturnos.domain.usecase

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoSolicitudCambio
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.model.TipoCeldaMalla
import com.itm.gestordeturnos.domain.model.TurnoCelda
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

sealed interface ValidacionSolicitudCambioResult {
    data object Valido : ValidacionSolicitudCambioResult
    data class Invalido(val mensaje: String) : ValidacionSolicitudCambioResult
}

/**
 * Caso de uso para la regla de negocio RN-19:
 * "Solo se solicita el intercambio de un turno propio, publicado y cuya fecha y hora de inicio
 * aún no hayan pasado; un turno con una solicitud en curso no admite una segunda solicitud simultánea.
 * El intercambio solo procede entre empleados activos de la misma sucursal y con el mismo perfil,
 * y el resultado debe seguir cumpliendo todas las reglas de programación."
 */
class ValidarSolicitudCambioTurnoUseCase(
    private val verificarCoberturaUseCase: VerificarCoberturaDiariaUseCase = VerificarCoberturaDiariaUseCase()
) {

    companion object {
        const val DESCANSO_MINIMO_MINUTOS = 12 * 60 // 720 minutos (12 horas)
        const val MAXIMO_MINUTOS_SEMANALES = 42 * 60 // 2520 minutos (42 horas)
    }

    /**
     * Evalúa si un turno propio o de compañero es elegible para mostrarse como seleccionable
     * en el flujo de solicitud de cambio de turno:
     * 1. Pertenece al empleado indicado.
     * 2. Se encuentra publicado ("PUBLICADA").
     * 3. No es descanso ni ausencia (incapacidad, permiso, vacaciones).
     * 4. Su fecha y hora de inicio aún no han pasado respecto al momento actual.
     * 5. No tiene una solicitud de intercambio activa en curso.
     */
    fun esTurnoElegibleParaIntercambio(
        asignacion: AsignacionTurno,
        empleadoId: String,
        solicitudesExistentes: List<SolicitudCambioTurno>,
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        horaActualMinutos: Int = obtenerMinutosActuales()
    ): Boolean {
        // 1. Pertenencia al empleado
        if (asignacion.empleadoId != empleadoId) return false

        // 2. Debe estar publicado
        if (!asignacion.estado.equals("PUBLICADA", ignoreCase = true)) return false

        // 3. No debe ser descanso ni ausencia
        if (asignacion.esDescanso) return false
        if (asignacion.tipoTurno.isBlank() || asignacion.tipoTurno.equals("Descanso", ignoreCase = true)) return false
        if (esTipoAusencia(asignacion.tipoTurno)) return false

        // 4. Fecha y hora de inicio aún no hayan pasado
        val fechaTurno = try { LocalDate.parse(asignacion.fechaIso) } catch (_: Throwable) { return false }
        if (fechaTurno < fechaActual) return false
        if (fechaTurno == fechaActual) {
            val horaInicioMin = parsearHoraInicioMinutos(asignacion.horario)
            if (horaInicioMin != null && horaInicioMin <= horaActualMinutos) {
                return false
            }
        }

        // 5. Sin solicitud en curso
        if (tieneSolicitudEnCurso(asignacion, solicitudesExistentes)) return false

        return true
    }

    /**
     * Valida integralmente la creación de una solicitud de intercambio entre dos colaboradores.
     */
    fun validarSolicitud(
        solicitante: Empleado?,
        companero: Empleado?,
        turnoEntrega: AsignacionTurno?,
        turnoRecibe: AsignacionTurno?,
        solicitudesExistentes: List<SolicitudCambioTurno>,
        todasAsignaciones: List<AsignacionTurno>,
        fechaActual: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        horaActualMinutos: Int = obtenerMinutosActuales()
    ): ValidacionSolicitudCambioResult {
        // 1. Validar existencia de empleados
        if (solicitante == null) {
            return ValidacionSolicitudCambioResult.Invalido("El colaborador solicitante no se encuentra registrado.")
        }
        if (companero == null) {
            return ValidacionSolicitudCambioResult.Invalido("El compañero seleccionado no se encuentra registrado.")
        }

        // 2. No se puede intercambiar consigo mismo
        if (solicitante.id == companero.id) {
            return ValidacionSolicitudCambioResult.Invalido("No puedes solicitar un intercambio de turno contigo mismo.")
        }

        // 3. Ambos empleados deben estar en estado ACTIVO
        if (solicitante.estado != EstadoUsuario.ACTIVO) {
            return ValidacionSolicitudCambioResult.Invalido("Tu cuenta de colaborador se encuentra inactiva.")
        }
        if (companero.estado != EstadoUsuario.ACTIVO) {
            return ValidacionSolicitudCambioResult.Invalido("El intercambio solo procede con compañeros que tengan cuenta activa.")
        }

        // 4. Misma sucursal
        val sucSol = solicitante.sucursal.trim().replace(" ", "")
        val sucComp = companero.sucursal.trim().replace(" ", "")
        if (!sucSol.equals(sucComp, ignoreCase = true)) {
            return ValidacionSolicitudCambioResult.Invalido("El intercambio solo procede entre empleados de la misma sucursal.")
        }

        // 5. Mismo perfil / cargo
        if (solicitante.cargo != companero.cargo) {
            return ValidacionSolicitudCambioResult.Invalido("El intercambio solo procede entre empleados con el mismo perfil o cargo.")
        }

        // 6. Validar turnos seleccionados
        if (turnoEntrega == null) {
            return ValidacionSolicitudCambioResult.Invalido("Debes seleccionar el turno propio que deseas entregar.")
        }
        if (turnoRecibe == null) {
            return ValidacionSolicitudCambioResult.Invalido("Debes seleccionar el turno del compañero que deseas recibir.")
        }

        // 7. Turno propio pertenece al solicitante y está publicado
        if (turnoEntrega.empleadoId != solicitante.id &&
            !turnoEntrega.empleadoNombre.equals(solicitante.nombre, ignoreCase = true)) {
            return ValidacionSolicitudCambioResult.Invalido("El turno a entregar debe pertenecer al colaborador solicitante.")
        }
        if (!turnoEntrega.estado.equals("PUBLICADA", ignoreCase = true)) {
            return ValidacionSolicitudCambioResult.Invalido("Solo se puede solicitar el intercambio de turnos que hayan sido publicados oficialmente.")
        }
        if (turnoEntrega.esDescanso || esTipoAusencia(turnoEntrega.tipoTurno)) {
            return ValidacionSolicitudCambioResult.Invalido("El turno a entregar debe ser un turno de trabajo hábil.")
        }

        // 8. Turno de compañero pertenece al compañero y está publicado
        if (turnoRecibe.empleadoId != companero.id &&
            !turnoRecibe.empleadoNombre.equals(companero.nombre, ignoreCase = true)) {
            return ValidacionSolicitudCambioResult.Invalido("El turno a recibir debe pertenecer al compañero seleccionado.")
        }
        if (!turnoRecibe.estado.equals("PUBLICADA", ignoreCase = true)) {
            return ValidacionSolicitudCambioResult.Invalido("El turno del compañero a recibir debe encontrarse publicado oficialmente.")
        }
        if (turnoRecibe.esDescanso || esTipoAusencia(turnoRecibe.tipoTurno)) {
            return ValidacionSolicitudCambioResult.Invalido("El turno a recibir del compañero debe ser un turno de trabajo hábil.")
        }

        // 9. Fechas y horas de inicio no hayan pasado
        val fechaEntrega = try { LocalDate.parse(turnoEntrega.fechaIso) } catch (_: Throwable) { return ValidacionSolicitudCambioResult.Invalido("Fecha del turno propio no válida.") }
        val fechaRecibe = try { LocalDate.parse(turnoRecibe.fechaIso) } catch (_: Throwable) { return ValidacionSolicitudCambioResult.Invalido("Fecha del turno del compañero no válida.") }

        if (fechaEntrega < fechaActual) {
            return ValidacionSolicitudCambioResult.Invalido("No se puede solicitar el intercambio de un turno cuya fecha ya ha pasado.")
        }
        if (fechaEntrega == fechaActual) {
            val hIni = parsearHoraInicioMinutos(turnoEntrega.horario)
            if (hIni != null && hIni <= horaActualMinutos) {
                return ValidacionSolicitudCambioResult.Invalido("No se puede solicitar el intercambio de un turno cuya hora de inicio ya ha pasado.")
            }
        }

        if (fechaRecibe < fechaActual) {
            return ValidacionSolicitudCambioResult.Invalido("No se puede solicitar el turno de un compañero cuya fecha ya ha pasado.")
        }
        if (fechaRecibe == fechaActual) {
            val hIni = parsearHoraInicioMinutos(turnoRecibe.horario)
            if (hIni != null && hIni <= horaActualMinutos) {
                return ValidacionSolicitudCambioResult.Invalido("No se puede solicitar el turno de un compañero cuya hora de inicio ya ha pasado.")
            }
        }

        // 10. Un turno con una solicitud en curso no admite una segunda solicitud simultánea
        if (tieneSolicitudEnCurso(turnoEntrega, solicitudesExistentes)) {
            return ValidacionSolicitudCambioResult.Invalido("El turno propio seleccionado ya tiene una solicitud de intercambio en curso.")
        }
        if (tieneSolicitudEnCurso(turnoRecibe, solicitudesExistentes)) {
            return ValidacionSolicitudCambioResult.Invalido("El turno del compañero seleccionado ya tiene una solicitud de intercambio en curso.")
        }

        // 11. Reglas de programación tras el intercambio simulado
        val resultadoProgramacion = validarReglasProgramacionIntercambio(
            solicitante = solicitante,
            companero = companero,
            turnoEntrega = turnoEntrega,
            turnoRecibe = turnoRecibe,
            fechaEntrega = fechaEntrega,
            fechaRecibe = fechaRecibe,
            todasAsignaciones = todasAsignaciones
        )
        if (resultadoProgramacion is ValidacionSolicitudCambioResult.Invalido) {
            return resultadoProgramacion
        }

        return ValidacionSolicitudCambioResult.Valido
    }

    /**
     * Valida que la programación resultante tras el intercambio cumpla:
     * - No doble turno ni solapamiento el mismo día.
     * - Ausencias (no asignar sobre días con incapacidad, vacaciones o permisos).
     * - Descanso mínimo de 12 horas entre jornadas consecutivas.
     * - Límite de 42 horas efectivas semanales.
     */
    private fun validarReglasProgramacionIntercambio(
        solicitante: Empleado,
        companero: Empleado,
        turnoEntrega: AsignacionTurno,
        turnoRecibe: AsignacionTurno,
        fechaEntrega: LocalDate,
        fechaRecibe: LocalDate,
        todasAsignaciones: List<AsignacionTurno>
    ): ValidacionSolicitudCambioResult {
        // --- A. SOLICITANTE recibe turnoRecibe en fechaRecibe ---
        if (fechaEntrega != fechaRecibe) {
            val asigExistenteSolEnFechaRecibe = todasAsignaciones.find {
                it.empleadoId == solicitante.id && it.fechaIso == fechaRecibe.toString()
            }
            if (asigExistenteSolEnFechaRecibe != null) {
                if (esTipoAusencia(asigExistenteSolEnFechaRecibe.tipoTurno)) {
                    return ValidacionSolicitudCambioResult.Invalido(
                        "No se puede realizar el intercambio: tienes una ausencia o permiso registrado para esa fecha."
                    )
                }
                if (!asigExistenteSolEnFechaRecibe.esDescanso &&
                    asigExistenteSolEnFechaRecibe.tipoTurno.isNotBlank() &&
                    !asigExistenteSolEnFechaRecibe.tipoTurno.equals("Descanso", ignoreCase = true)) {
                    return ValidacionSolicitudCambioResult.Invalido(
                        "Un empleado no puede tener dos turnos asignados el mismo día ni turnos que se solapen entre sí."
                    )
                }
            }

            // Descanso mínimo 12h entre jornadas consecutivas para solicitante
            val descansoSol = validarDescansoMinimoInterjornada(
                empleadoId = solicitante.id,
                fechaTurno = fechaRecibe,
                horarioNuevo = turnoRecibe.horario,
                fechaTurnoEntregado = fechaEntrega,
                todasAsignaciones = todasAsignaciones
            )
            if (descansoSol is ValidacionSolicitudCambioResult.Invalido) return descansoSol
        }

        // Límite 42h semanales para solicitante
        val limiteHorasSol = validarLimiteHorasSemanalesIntercambio(
            empleadoId = solicitante.id,
            fechaRecibe = fechaRecibe,
            horarioRecibe = turnoRecibe.horario,
            fechaEntrega = fechaEntrega,
            horarioEntrega = turnoEntrega.horario,
            todasAsignaciones = todasAsignaciones,
            esSolicitante = true
        )
        if (limiteHorasSol is ValidacionSolicitudCambioResult.Invalido) return limiteHorasSol

        // --- B. COMPAÑERO recibe turnoEntrega en fechaEntrega ---
        if (fechaEntrega != fechaRecibe) {
            val asigExistenteCompEnFechaEntrega = todasAsignaciones.find {
                it.empleadoId == companero.id && it.fechaIso == fechaEntrega.toString()
            }
            if (asigExistenteCompEnFechaEntrega != null) {
                if (esTipoAusencia(asigExistenteCompEnFechaEntrega.tipoTurno)) {
                    return ValidacionSolicitudCambioResult.Invalido(
                        "No se puede realizar el intercambio: el compañero tiene una ausencia o permiso registrado para esa fecha."
                    )
                }
                if (!asigExistenteCompEnFechaEntrega.esDescanso &&
                    asigExistenteCompEnFechaEntrega.tipoTurno.isNotBlank() &&
                    !asigExistenteCompEnFechaEntrega.tipoTurno.equals("Descanso", ignoreCase = true)) {
                    return ValidacionSolicitudCambioResult.Invalido(
                        "Un empleado no puede tener dos turnos asignados el mismo día ni turnos que se solapen entre sí."
                    )
                }
            }

            // Descanso mínimo 12h entre jornadas consecutivas para compañero
            val descansoComp = validarDescansoMinimoInterjornada(
                empleadoId = companero.id,
                fechaTurno = fechaEntrega,
                horarioNuevo = turnoEntrega.horario,
                fechaTurnoEntregado = fechaRecibe,
                todasAsignaciones = todasAsignaciones
            )
            if (descansoComp is ValidacionSolicitudCambioResult.Invalido) return descansoComp
        }

        // Límite 42h semanales para compañero
        val limiteHorasComp = validarLimiteHorasSemanalesIntercambio(
            empleadoId = companero.id,
            fechaRecibe = fechaEntrega,
            horarioRecibe = turnoEntrega.horario,
            fechaEntrega = fechaRecibe,
            horarioEntrega = turnoRecibe.horario,
            todasAsignaciones = todasAsignaciones,
            esSolicitante = false
        )
        if (limiteHorasComp is ValidacionSolicitudCambioResult.Invalido) return limiteHorasComp

        return ValidacionSolicitudCambioResult.Valido
    }

    /**
     * Valida que entre el nuevo turno asignado y los turnos adyacentes (día anterior y posterior)
     * exista un descanso mínimo obligatorio de 12 horas (720 minutos).
     */
    private fun validarDescansoMinimoInterjornada(
        empleadoId: String,
        fechaTurno: LocalDate,
        horarioNuevo: String,
        fechaTurnoEntregado: LocalDate,
        todasAsignaciones: List<AsignacionTurno>
    ): ValidacionSolicitudCambioResult {
        val (nuevoIni, nuevoFin) = parsearRangoHorario(horarioNuevo) ?: return ValidacionSolicitudCambioResult.Valido

        // 1. Validar respecto al día anterior (fechaTurno - 1)
        val fechaAyer = fechaTurno.minus(DatePeriod(days = 1))
        if (fechaAyer != fechaTurnoEntregado) {
            val asigAyer = todasAsignaciones.find {
                it.empleadoId == empleadoId && it.fechaIso == fechaAyer.toString() &&
                !it.esDescanso && it.tipoTurno.isNotBlank() && !it.tipoTurno.equals("Descanso", ignoreCase = true)
            }
            if (asigAyer != null) {
                val rangoAyer = parsearRangoHorario(asigAyer.horario)
                if (rangoAyer != null) {
                    val finAyer = rangoAyer.second
                    // Descanso: minutos desde finAyer hasta medianoche + nuevoIni
                    val descansoMinutos = (1440 - finAyer) + nuevoIni
                    if (descansoMinutos < DESCANSO_MINIMO_MINUTOS) {
                        return ValidacionSolicitudCambioResult.Invalido(
                            "El intercambio no cumple el descanso mínimo obligatorio de 12 horas consecutivas entre jornadas laborales."
                        )
                    }
                }
            }
        }

        // 2. Validar respecto al día posterior (fechaTurno + 1)
        val fechaManana = fechaTurno.plus(DatePeriod(days = 1))
        if (fechaManana != fechaTurnoEntregado) {
            val asigManana = todasAsignaciones.find {
                it.empleadoId == empleadoId && it.fechaIso == fechaManana.toString() &&
                !it.esDescanso && it.tipoTurno.isNotBlank() && !it.tipoTurno.equals("Descanso", ignoreCase = true)
            }
            if (asigManana != null) {
                val rangoManana = parsearRangoHorario(asigManana.horario)
                if (rangoManana != null) {
                    val iniManana = rangoManana.first
                    val descansoMinutos = (1440 - nuevoFin) + iniManana
                    if (descansoMinutos < DESCANSO_MINIMO_MINUTOS) {
                        return ValidacionSolicitudCambioResult.Invalido(
                            "El intercambio no cumple el descanso mínimo obligatorio de 12 horas consecutivas entre jornadas laborales."
                        )
                    }
                }
            }
        }

        return ValidacionSolicitudCambioResult.Valido
    }

    /**
     * Valida que tras el intercambio, el empleado no supere las 42 horas semanales en la semana
     * del turno recibido.
     */
    private fun validarLimiteHorasSemanalesIntercambio(
        empleadoId: String,
        fechaRecibe: LocalDate,
        horarioRecibe: String,
        fechaEntrega: LocalDate,
        horarioEntrega: String,
        todasAsignaciones: List<AsignacionTurno>,
        esSolicitante: Boolean
    ): ValidacionSolicitudCambioResult {
        val diasDesdeLunes = fechaRecibe.dayOfWeek.ordinal
        val lunesSemanaRecibe = fechaRecibe.minus(DatePeriod(days = diasDesdeLunes))
        val fechasSemana = (0..6).map { lunesSemanaRecibe.plus(DatePeriod(days = it)).toString() }.toSet()

        var minutosTotales = 0

        // Asignaciones actuales del empleado en esa semana
        val asigsEnSemana = todasAsignaciones.filter {
            it.empleadoId == empleadoId && it.fechaIso in fechasSemana
        }

        for (asig in asigsEnSemana) {
            // Si es la fecha del turno que entrega en esa misma semana, no se computa porque lo cede
            if (asig.fechaIso == fechaEntrega.toString()) continue

            if (!asig.esDescanso && asig.tipoTurno.isNotBlank() && !asig.tipoTurno.equals("Descanso", ignoreCase = true)) {
                val dur = calcularMinutosEfectivos(asig.horario)
                minutosTotales += dur
            }
        }

        // Sumar el turno que recibe
        minutosTotales += calcularMinutosEfectivos(horarioRecibe)

        if (minutosTotales > MAXIMO_MINUTOS_SEMANALES) {
            val rolMsg = if (esSolicitante) "el colaborador solicitante" else "el compañero receptor"
            return ValidacionSolicitudCambioResult.Invalido(
                "Ningún empleado puede superar 42 horas efectivas de trabajo en una misma semana ($rolMsg excedería el tope)."
            )
        }

        return ValidacionSolicitudCambioResult.Valido
    }

    /**
     * Comprueba si una asignación ya cuenta con una solicitud de intercambio activa.
     */
    fun tieneSolicitudEnCurso(
        asignacion: AsignacionTurno,
        solicitudes: List<SolicitudCambioTurno>
    ): Boolean {
        val asigId = asignacion.id
        val asigKey = "${asignacion.empleadoId}_${asignacion.fechaIso}"

        return solicitudes.any { s ->
            val estaActiva = s.estado == EstadoSolicitudCambio.PENDIENTE ||
                s.estado == EstadoSolicitudCambio.ACEPTADA_COMPANERO

            if (!estaActiva) return@any false

            val coincideEntrega = (asigId.isNotBlank() && s.turnoEntregaId == asigId) ||
                s.turnoEntregaId == asigKey ||
                (s.empleadoSolicitanteId == asignacion.empleadoId && s.detalleTurnoEntrega.contains(asignacion.fechaIso))

            val coincideRecibe = (asigId.isNotBlank() && s.turnoRecibeId == asigId) ||
                s.turnoRecibeId == asigKey ||
                (s.empleadoCompaneroId == asignacion.empleadoId && s.detalleTurnoRecibe.contains(asignacion.fechaIso))

            coincideEntrega || coincideRecibe
        }
    }

    private fun esTipoAusencia(tipoTurno: String): Boolean {
        val t = tipoTurno.lowercase()
        return t.contains("incapacidad") || t.contains("permiso") || t.contains("vacaciones")
    }

    private fun parsearHoraInicioMinutos(horario: String): Int? {
        val partes = horario.replace("\r", "").replace("\n", "").trim().split("-")
        if (partes.isNotEmpty()) {
            return parsearMinutos(partes[0])
        }
        return null
    }

    private fun parsearRangoHorario(horario: String): Pair<Int, Int>? {
        val limpio = horario.replace("\r", "").replace("\n", "").trim()
        val partes = limpio.split("-")
        if (partes.size == 2) {
            val ini = parsearMinutos(partes[0])
            val fin = parsearMinutos(partes[1])
            if (ini != null && fin != null) {
                return Pair(ini, fin)
            }
        }
        return null
    }

    private fun parsearMinutos(horaStr: String): Int? {
        val partes = horaStr.trim().split(":")
        if (partes.size == 2) {
            val h = partes[0].toIntOrNull() ?: return null
            val m = partes[1].toIntOrNull() ?: return null
            return h * 60 + m
        }
        return null
    }

    private fun calcularMinutosEfectivos(horario: String): Int {
        val rango = parsearRangoHorario(horario) ?: return 450 // 7.5h default
        var dur = rango.second - rango.first
        if (dur < 0) dur += 1440
        return if (dur >= 240) dur - 30 else dur
    }

    private fun obtenerMinutosActuales(): Int {
        return try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            now.hour * 60 + now.minute
        } catch (_: Throwable) {
            0
        }
    }
}
