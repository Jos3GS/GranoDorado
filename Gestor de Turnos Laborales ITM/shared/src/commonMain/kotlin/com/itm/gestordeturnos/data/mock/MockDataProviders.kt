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
        TurnoTemplate("1", "Apertura", "06:00-\n14:00", "10:00-\n10:30", "7 h 30\nmin", "ACTIVO", 24),
        TurnoTemplate("2", "Intermedio", "10:00-\n18:00", "13:30-\n14:00", "7 h 30\nmin", "ACTIVO", 16),
        TurnoTemplate("3", "Cierre", "14:00-\n22:00", "18:00-\n18:30", "7 h 30\nmin", "ACTIVO", 12),
        TurnoTemplate("4", "Refuerzo tarde", "16:00-\n22:00", "-", "6 h", "INACTIVO", 3)
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
        Novedad("1", "Salida antes de la hora", "Salida anticipada por cierre", null, null, EstadoNovedad.REPORTADA, "Administrador General", "CENTRO", "Cierre, 14:00 a 22:00 · 2026-09-08", "90 minutos menos que el turno programado", "ROJO", "ROJO")
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
