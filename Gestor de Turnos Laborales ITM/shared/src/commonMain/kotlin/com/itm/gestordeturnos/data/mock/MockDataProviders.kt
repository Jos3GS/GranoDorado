package com.itm.gestordeturnos.data.mock

import com.itm.gestordeturnos.domain.model.*

object MockDataProviders {

    fun inicialEmpleados(): List<Empleado> = listOf(
        Empleado(
            id = "admin_1",
            nombre = "Administrador",
            apellido = "General",
            documento = 1000112233L,
            correo = "gutierrez.sossa.j@gmail.com",
            telefono = "3001112233",
            cargo = Cargo.ADMINISTRADOR_GENERAL,
            sucursal = "CENTRO",
            fechaIngreso = "15/01/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "AG"
        )
    )

    fun inicialTurnosCatalogo(): List<TurnoTemplate> = listOf(
        TurnoTemplate(
            id = "1",
            nombre = "Apertura",
            horario = "06:00-\n14:00",
            descansos = "10:00-\n10:30",
            horasEfectivas = "7 h 30\nmin",
            estado = "ACTIVO",
            asignaciones = 24,
            horaInicio = "06:00",
            horaFin = "14:00",
            listaDescansos = listOf(RangoDescanso(id = "d_ap_1", horaInicio = "10:00", horaFin = "10:30"))
        ),
        TurnoTemplate(
            id = "2",
            nombre = "Intermedio",
            horario = "10:00-\n18:00",
            descansos = "13:30-\n14:00",
            horasEfectivas = "7 h 30\nmin",
            estado = "ACTIVO",
            asignaciones = 16,
            horaInicio = "10:00",
            horaFin = "18:00",
            listaDescansos = listOf(RangoDescanso(id = "d_int_1", horaInicio = "13:30", horaFin = "14:00"))
        ),
        TurnoTemplate(
            id = "3",
            nombre = "Cierre",
            horario = "14:00-\n22:00",
            descansos = "18:00-\n18:30",
            horasEfectivas = "7 h 30\nmin",
            estado = "ACTIVO",
            asignaciones = 12,
            horaInicio = "14:00",
            horaFin = "22:00",
            listaDescansos = listOf(RangoDescanso(id = "d_cie_1", horaInicio = "18:00", horaFin = "18:30"))
        ),
        TurnoTemplate(
            id = "4",
            nombre = "Refuerzo tarde",
            horario = "16:00-\n22:00",
            descansos = "-",
            horasEfectivas = "6 h",
            estado = "INACTIVO",
            asignaciones = 3,
            horaInicio = "16:00",
            horaFin = "22:00",
            listaDescansos = emptyList()
        )
    )

    fun inicialHorarioSemana(): List<TurnoAsignado> = listOf(
        TurnoAsignado("1", "LUN", "14", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
        TurnoAsignado("2", "MAR", "15", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
        TurnoAsignado("3", "MIÉ", "16", null, null, null, null, esDescanso = true),
        TurnoAsignado("4", "JUE", "17", "Cierre", "14:00 - 22:00", "CENTRO", "7 h 30 min", etiquetaModificado = "MODIFICADO 09/09/2026 11:15"),
        TurnoAsignado("5", "VIE", "18", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
        TurnoAsignado("6", "SÁB", "19", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
        TurnoAsignado("7", "DOM", "20", null, null, null, null, sinAsignar = true)
    )

    fun inicialFilasMalla(): List<FilaMalla> {
        val apertura = TurnoCelda("Apertura", "06 - 14")
        val medio = TurnoCelda("Medio", "10 - 18")
        val cierre = TurnoCelda("Cierre", "14 - 22")
        val descanso = TurnoCelda("Descanso", "-", esDescanso = true)

        return listOf(
            FilaMalla(
                idEmpleado = "admin_1",
                nombre = "Administrador General",
                cargo = "ADMINISTRADOR GENERAL",
                turnos = listOf(apertura, apertura, descanso, cierre, apertura, medio, descanso),
                totalHoras = "37.5 h"
            )
        )
    }

    fun inicialNovedades(): List<Novedad> = listOf(
        Novedad(
            id = "1",
            tipo = "Salida antes de la hora",
            descripcion = "Salida antes de la hora programada",
            horaInicioReal = null,
            horaFinReal = null,
            estado = EstadoNovedad.REPORTADA,
            empleado = "Ana Pérez",
            sucursal = "CENTRO",
            detalleTurno = "Cierre, 14:00 a 22:00 · 2026-09-08",
            diferencia = "90 minutos menos que el turno programado",
            colorBorde = "ROJO",
            colorVorde = "ROJO",
            empleadoId = "3",
            asignacionTurnoId = "t1"
        ),
        Novedad(
            id = "2",
            tipo = "Ampliación del turno",
            descripcion = "Extensión de turno por afluencia",
            horaInicioReal = null,
            horaFinReal = null,
            estado = EstadoNovedad.REPORTADA,
            empleado = "Marta León",
            sucursal = "CENTRO",
            detalleTurno = "Apertura, 06:00 a 14:00 · 2026-09-09",
            diferencia = "90 minutos más que el turno programado",
            colorBorde = "VERDE",
            colorVorde = "VERDE",
            empleadoId = "5",
            asignacionTurnoId = "t2"
        ),
        Novedad(
            id = "3",
            tipo = "Ampliación del turno",
            descripcion = "Ampliación por cierre y cuadre",
            horaInicioReal = null,
            horaFinReal = null,
            estado = EstadoNovedad.REPORTADA,
            empleado = "Carlos Ruiz",
            sucursal = "NORTE",
            detalleTurno = "Cierre, 14:00 a 22:00 · 2026-09-08",
            diferencia = "sin diferencia",
            colorBorde = "GRIS",
            colorVorde = "GRIS",
            empleadoId = "6",
            asignacionTurnoId = "t3"
        ),
        Novedad(
            id = "4",
            tipo = "Llegada tarde",
            descripcion = "Retraso involuntario en el ingreso",
            horaInicioReal = null,
            horaFinReal = null,
            estado = EstadoNovedad.REVISADA,
            empleado = "Julián Vera",
            sucursal = "CENTRO",
            detalleTurno = "Intermedio, 10:00 a 18:00 · 2026-09-07",
            diferencia = "25 minutos menos que el turno programado",
            colorBorde = "ROJO_OSCURO",
            colorVorde = "ROJO_OSCURO",
            empleadoId = "7",
            asignacionTurnoId = "t4"
        )
    )

    fun inicialSolicitudesCambio(): List<SolicitudCambioTurno> = emptyList()

    fun inicialTurnosPropios(): List<TurnoSeleccionable> = listOf(
        TurnoSeleccionable("1", "Lunes 14 · Apertura", "06:00-14:00"),
        TurnoSeleccionable("2", "Martes 15 · Apertura", "06:00-14:00"),
        TurnoSeleccionable("3", "Jueves 17 · Cierre", "14:00-22:00"),
        TurnoSeleccionable("4", "Viernes 18 · Apertura", "06:00-14:00"),
        TurnoSeleccionable("5", "Sábado 19 · Apertura", "06:00-14:00")
    )

    fun inicialTurnosCompaneros(): List<TurnoCompaneroSeleccionable> = emptyList()

    fun inicialIncumplimientos(): List<NotificacionIncumplimiento> = emptyList()

    fun inicialRegistrosAsistencia(): List<RegistroAsistencia> = listOf(
        RegistroAsistencia("1", "Administrador General", "CENTRO", "05:58", "-", "PRESENTE")
    )
}
