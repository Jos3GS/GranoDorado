package com.itm.gestordeturnos.data.mock

import com.itm.gestordeturnos.domain.model.*

object MockDataProviders {

    fun inicialEmpleados(): List<Empleado> = listOf(
        Empleado(
            id = "1",
            nombre = "Diego",
            apellido = "Marín",
            documento = 1000112233L,
            correo = "admin.general@granodorado.com",
            telefono = "3001112233",
            cargo = Cargo.ADMINISTRADOR_GENERAL,
            sucursal = "CENTRO",
            fechaIngreso = "15/01/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "DM"
        ),
        Empleado(
            id = "2",
            nombre = "Carla",
            apellido = "Ríos",
            documento = 1011223344L,
            correo = "carla.rios@granodorado.com",
            telefono = "3002223344",
            cargo = Cargo.ADMINISTRADOR_SUCURSAL,
            sucursal = "CENTRO",
            fechaIngreso = "01/02/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "CR"
        ),
        Empleado(
            id = "3",
            nombre = "Ana",
            apellido = "Pérez",
            documento = 1022334455L,
            correo = "ana.perez@granodorado.com",
            telefono = "3003334455",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO",
            fechaIngreso = "15/02/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "AP"
        ),
        Empleado(
            id = "4",
            nombre = "Sofía",
            apellido = "Ramos",
            documento = 1033445566L,
            correo = "sofia.ramos@granodorado.com",
            telefono = "3004445566",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO",
            fechaIngreso = "01/03/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "SR"
        ),
        Empleado(
            id = "5",
            nombre = "Marta",
            apellido = "León",
            documento = 1044556677L,
            correo = "marta.leon@granodorado.com",
            telefono = "3005556677",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO",
            fechaIngreso = "10/03/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "ML"
        ),
        Empleado(
            id = "6",
            nombre = "Diego",
            apellido = "Salas",
            documento = 1055667788L,
            correo = "diego.salas@granodorado.com",
            telefono = "3006667788",
            cargo = Cargo.COCINERO,
            sucursal = "CENTRO",
            fechaIngreso = "20/03/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "DS"
        ),
        Empleado(
            id = "7",
            nombre = "Julián",
            apellido = "Vera",
            documento = 1066778899L,
            correo = "julian.vera@granodorado.com",
            telefono = "3007778899",
            cargo = Cargo.CAJERO,
            sucursal = "CENTRO",
            fechaIngreso = "01/04/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "JV"
        ),
        Empleado(
            id = "8",
            nombre = "Luis",
            apellido = "Gómez",
            documento = 1077889900L,
            correo = "luis.gomez@granodorado.com",
            telefono = "3008889900",
            cargo = Cargo.COCINERO,
            sucursal = "NORTE",
            fechaIngreso = "15/04/2024",
            estadoLaboral = EstadoEmpleado.INACTIVO,
            estado = EstadoUsuario.INACTIVO,
            iniciales = "LG"
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
                idEmpleado = "1",
                nombre = "Ana Pérez",
                cargo = "BARISTA",
                turnos = listOf(apertura, apertura, descanso, cierre, apertura, medio, descanso),
                totalHoras = "37.5 h"
            ),
            FilaMalla(
                idEmpleado = "2",
                nombre = "Carlos Ruiz",
                cargo = "CAJERO",
                turnos = listOf(cierre, descanso, cierre, apertura, cierre, cierre, descanso),
                totalHoras = "37.5 h"
            ),
            FilaMalla(
                idEmpleado = "3",
                nombre = "María Gómez",
                cargo = "COCINERO",
                turnos = listOf(descanso, apertura, apertura, medio, descanso, apertura, apertura),
                totalHoras = "37.5 h"
            ),
            FilaMalla(
                idEmpleado = "4",
                nombre = "Jorge Luis",
                cargo = "BARISTA",
                turnos = listOf(medio, cierre, cierre, descanso, medio, descanso, cierre),
                totalHoras = "37.5 h"
            )
        )
    }

    fun inicialNovedades(): List<Novedad> = listOf(
        Novedad("1", "Salida antes de la hora", "Salida anticipada por cierre", null, null, EstadoNovedad.REPORTADA, "Ana Pérez", "CENTRO", "Cierre, 14:00 a 22:00 · 2026-09-08", "90 minutos menos que el turno programado", "ROJO", "ROJO"),
        Novedad("2", "Ampliación del turno", "Extensión por afluencia", null, null, EstadoNovedad.REPORTADA, "Marta León", "CENTRO", "Apertura, 06:00 a 14:00 · 2026-09-09", "90 minutos más que el turno programado", "VERDE", "VERDE"),
        Novedad("3", "Ampliación del turno", "Ajuste de inventario", null, null, EstadoNovedad.REPORTADA, "Carlos Ruiz", "NORTE", "Cierre, 14:00 a 22:00 · 2026-09-08", "sin diferencia", "GRIS", "GRIS"),
        Novedad("4", "Llegada tarde", "Retraso en transporte", null, null, EstadoNovedad.REVISADA, "Julián Vera", "CENTRO", "Intermedio, 10:00 a 18:00 · 2026-09-07", "25 minutos menos que el turno programado", "VERDE_CLARO", "VERDE_CLARO")
    )

    fun inicialSolicitudesCambio(): List<SolicitudCambioTurno> = listOf(
        SolicitudCambioTurno(
            id = "1",
            numeroSolicitud = "#S1",
            sucursalNombre = "CENTRO",
            estado = EstadoSolicitudCambio.ACEPTADA_COMPANERO,
            nombreSolicitante = "José Manuel",
            tipoTurnoEntrega = "Cierre",
            detalleTurnoEntrega = "2026-09-17 · 14:00 a 22:00",
            nombreCompanero = "Sofía Ramos",
            tipoTurnoRecibe = "Apertura",
            detalleTurnoRecibe = "2026-09-18 · 06:00 a 14:00",
            motivo = "Tengo una cita médica el jueves en la tarde.",
            validacionesOk = true
        ),
        SolicitudCambioTurno(
            id = "2",
            numeroSolicitud = "#S2",
            sucursalNombre = "CENTRO",
            estado = EstadoSolicitudCambio.PENDIENTE,
            nombreSolicitante = "Marta León",
            tipoTurnoEntrega = "Apertura",
            detalleTurnoEntrega = "2026-09-19 · 06:00 a 14:00",
            nombreCompanero = "José Manuel",
            tipoTurnoRecibe = "Cierre",
            detalleTurnoRecibe = "2026-09-20 · 14:00 a 22:00",
            motivo = "Asunto familiar.",
            validacionesOk = true
        )
    )

    fun inicialTurnosPropios(): List<TurnoSeleccionable> = listOf(
        TurnoSeleccionable("1", "Lunes 14 · Apertura", "06:00-14:00"),
        TurnoSeleccionable("2", "Martes 15 · Apertura", "06:00-14:00"),
        TurnoSeleccionable("3", "Jueves 17 · Cierre", "14:00-22:00"),
        TurnoSeleccionable("4", "Viernes 18 · Apertura", "06:00-14:00"),
        TurnoSeleccionable("5", "Sábado 19 · Apertura", "06:00-14:00")
    )

    fun inicialTurnosCompaneros(): List<TurnoCompaneroSeleccionable> = listOf(
        TurnoCompaneroSeleccionable("c1", "Sofía Ramos", "Miércoles 16 · Intermedio", "10:00-18:00"),
        TurnoCompaneroSeleccionable("c2", "Marta León", "Miércoles 16 · Cierre", "14:00-22:00"),
        TurnoCompaneroSeleccionable("c3", "Marta León", "Domingo 20 · Cierre", "14:00-22:00")
    )

    fun inicialIncumplimientos(): List<NotificacionIncumplimiento> = listOf(
        NotificacionIncumplimiento("1", "Sofía Ramos", "llegada tarde", "(06:15)"),
        NotificacionIncumplimiento("2", "Marta León", "llegada tarde", "(10:12)"),
        NotificacionIncumplimiento("3", "Diego Salas", "llegada tarde", "(14:20)"),
        NotificacionIncumplimiento("4", "Carlos Ruiz", "sin marcar entrada", ""),
        NotificacionIncumplimiento("5", "María Torres", "llegada tarde", "(08:05)"),
        NotificacionIncumplimiento("6", "Laura Cano", "llegada tarde", "(06:10)")
    )

    fun inicialRegistrosAsistencia(): List<RegistroAsistencia> = listOf(
        RegistroAsistencia("1", "Ana Pérez", "CENTRO", "05:58", "-", "PRESENTE"),
        RegistroAsistencia("2", "Sofía Ramos", "CENTRO", "-", "-", "AUSENTE"),
        RegistroAsistencia("3", "Marta León", "CENTRO", "10:12", "-", "TARDE"),
        RegistroAsistencia("4", "Diego Salas", "CENTRO", "-", "-", "AUSENTE"),
        RegistroAsistencia("5", "Julián Vera", "CENTRO", "06:03", "14:00", "PRESENTE"),
        RegistroAsistencia("6", "Carlos Ruiz", "NORTE", "-", "-", "AUSENTE"),
        RegistroAsistencia("7", "María Torres", "NORTE", "-", "-", "AUSENTE")
    )
}
