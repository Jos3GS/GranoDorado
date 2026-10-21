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
            sucursalId = "CENTRO",
            fechaIngreso = "15/01/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "AG"
        ),
        Empleado(
            id = "emp_1",
            nombre = "Ana",
            apellido = "PÃ©rez",
            documento = 1017200001L,
            correo = "ana.perez@granodorado.com",
            telefono = "3104567890",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO",
            sucursalId = "CENTRO",
            fechaIngreso = "01/02/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "AP"
        ),
        Empleado(
            id = "emp_2",
            nombre = "Carlos",
            apellido = "Ruiz",
            documento = 1017200002L,
            correo = "carlos.ruiz@granodorado.com",
            telefono = "3104567891",
            cargo = Cargo.COCINERO,
            sucursal = "CENTRO",
            sucursalId = "CENTRO",
            fechaIngreso = "15/02/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "CR"
        ),
        Empleado(
            id = "emp_3",
            nombre = "Marta",
            apellido = "LeÃ³n",
            documento = 1017200003L,
            correo = "marta.leon@granodorado.com",
            telefono = "3104567892",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO",
            sucursalId = "CENTRO",
            fechaIngreso = "10/03/2024",
            estadoLaboral = EstadoEmpleado.VACACIONES,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "ML"
        ),
        Empleado(
            id = "emp_4",
            nombre = "JuliÃ¡n",
            apellido = "Vera",
            documento = 1017200004L,
            correo = "julian.vera@granodorado.com",
            telefono = "3104567893",
            cargo = Cargo.CAJERO,
            sucursal = "CENTRO",
            sucursalId = "CENTRO",
            fechaIngreso = "01/04/2024",
            estadoLaboral = EstadoEmpleado.INCAPACITADO,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "JV"
        ),
        Empleado(
            id = "emp_5",
            nombre = "Diego",
            apellido = "Salas",
            documento = 1017200005L,
            correo = "diego.salas@granodorado.com",
            telefono = "3104567894",
            cargo = Cargo.COCINERO,
            sucursal = "CENTRO",
            sucursalId = "CENTRO",
            fechaIngreso = "20/04/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "DS"
        ),
        Empleado(
            id = "emp_6",
            nombre = "SofÃ­a",
            apellido = "Ramos",
            documento = 1017200006L,
            correo = "sofia.ramos@granodorado.com",
            telefono = "3104567895",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO",
            sucursalId = "CENTRO",
            fechaIngreso = "05/05/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "SR"
        ),
        // Empleados Sucursal NORTE
        Empleado(
            id = "emp_7",
            nombre = "Laura",
            apellido = "Restrepo",
            documento = 1017200007L,
            correo = "laura.restrepo@granodorado.com",
            telefono = "3104567896",
            cargo = Cargo.BARISTA,
            sucursal = "NORTE",
            sucursalId = "NORTE",
            fechaIngreso = "12/05/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "LR"
        ),
        Empleado(
            id = "emp_8",
            nombre = "AndrÃ©s",
            apellido = "GÃ³mez",
            documento = 1017200008L,
            correo = "andres.gomez@granodorado.com",
            telefono = "3104567897",
            cargo = Cargo.CAJERO,
            sucursal = "NORTE",
            sucursalId = "NORTE",
            fechaIngreso = "01/06/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "AG"
        ),
        // Empleados Sucursal SUR
        Empleado(
            id = "emp_9",
            nombre = "Valentina",
            apellido = "Morales",
            documento = 1017200009L,
            correo = "valentina.morales@granodorado.com",
            telefono = "3104567898",
            cargo = Cargo.COCINERO,
            sucursal = "SUR",
            sucursalId = "SUR",
            fechaIngreso = "15/06/2024",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.ACTIVO,
            iniciales = "VM"
        ),
        // Empleado INACTIVO (no debe ser cargado en la malla de turnos)
        Empleado(
            id = "emp_10",
            nombre = "Pedro",
            apellido = "Castillo",
            documento = 1017200010L,
            correo = "pedro.castillo@granodorado.com",
            telefono = "3104567899",
            cargo = Cargo.BARISTA,
            sucursal = "CENTRO",
            sucursalId = "CENTRO",
            fechaIngreso = "10/01/2023",
            estadoLaboral = EstadoEmpleado.DISPONIBLE,
            estado = EstadoUsuario.INACTIVO,
            iniciales = "PC"
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
        TurnoAsignado("3", "MIÃ‰", "16", null, null, null, null, esDescanso = true),
        TurnoAsignado("4", "JUE", "17", "Cierre", "14:00 - 22:00", "CENTRO", "7 h 30 min", etiquetaModificado = "MODIFICADO 09/09/2026 11:15"),
        TurnoAsignado("5", "VIE", "18", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
        TurnoAsignado("6", "SÃB", "19", "Apertura", "06:00 - 14:00", "CENTRO", "7 h 30 min"),
        TurnoAsignado("7", "DOM", "20", null, null, null, null, sinAsignar = true)
    )

    fun inicialFilasMalla(): List<FilaMalla> {
        return listOf(
            FilaMalla(
                idEmpleado = "emp_1",
                nombre = "Ana PÃ©rez",
                cargo = "BARISTA",
                turnos = listOf(
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true),
                    TurnoCelda(tipo = "Cierre", horario = "14:00- 22:00", tipoCelda = TipoCeldaMalla.TURNO, modificadoTrasPublicar = false),
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true)
                ),
                totalHoras = "37 h 30 min"
            ),
            FilaMalla(
                idEmpleado = "emp_2",
                nombre = "Carlos Ruiz",
                cargo = "COCINERO",
                turnos = listOf(
                    TurnoCelda(tipo = "Intermedio", horario = "10:00- 18:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Intermedio", horario = "10:00- 18:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Intermedio", horario = "10:00- 18:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true),
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true),
                    TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR),
                    TurnoCelda(tipo = "Cierre", horario = "14:00- 22:00", tipoCelda = TipoCeldaMalla.TURNO)
                ),
                totalHoras = "30 h 00 min"
            ),
            FilaMalla(
                idEmpleado = "emp_3",
                nombre = "Marta LeÃ³n",
                cargo = "BARISTA",
                turnos = listOf(
                    TurnoCelda(tipo = "Vacaciones", horario = "-", tipoCelda = TipoCeldaMalla.VACACIONES),
                    TurnoCelda(tipo = "Vacaciones", horario = "-", tipoCelda = TipoCeldaMalla.VACACIONES),
                    TurnoCelda(tipo = "Vacaciones", horario = "-", tipoCelda = TipoCeldaMalla.VACACIONES),
                    TurnoCelda(tipo = "Vacaciones", horario = "-", tipoCelda = TipoCeldaMalla.VACACIONES),
                    TurnoCelda(tipo = "Vacaciones", horario = "-", tipoCelda = TipoCeldaMalla.VACACIONES),
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true),
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true)
                ),
                totalHoras = "0 h"
            ),
            FilaMalla(
                idEmpleado = "emp_4",
                nombre = "JuliÃ¡n Vera",
                cargo = "CAJERO",
                turnos = listOf(
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Cierre", horario = "14:00- 22:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Cierre", horario = "14:00- 22:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Incapacidad", horario = "-", tipoCelda = TipoCeldaMalla.INCAPACIDAD),
                    TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR),
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true)
                ),
                totalHoras = "30 h 00 min"
            ),
            FilaMalla(
                idEmpleado = "emp_5",
                nombre = "Diego Salas",
                cargo = "COCINERO",
                turnos = listOf(
                    TurnoCelda(tipo = "Cierre", horario = "14:00- 22:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Cierre", horario = "14:00- 22:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true),
                    TurnoCelda(tipo = "Intermedio", horario = "10:00- 18:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR),
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO)
                ),
                totalHoras = "37 h 30 min"
            ),
            FilaMalla(
                idEmpleado = "emp_6",
                nombre = "SofÃ­a Ramos",
                cargo = "BARISTA",
                turnos = listOf(
                    TurnoCelda(tipo = "Descanso", horario = "-", tipoCelda = TipoCeldaMalla.DESCANSO, esDescanso = true),
                    TurnoCelda(tipo = "Intermedio", horario = "10:00- 18:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Apertura", horario = "06:00- 14:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "Cierre", horario = "14:00- 22:00", tipoCelda = TipoCeldaMalla.TURNO),
                    TurnoCelda(tipo = "", horario = "", tipoCelda = TipoCeldaMalla.SIN_ASIGNAR),
                    TurnoCelda(tipo = "Cierre", horario = "14:00- 22:00", tipoCelda = TipoCeldaMalla.TURNO)
                ),
                totalHoras = "37 h 30 min"
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
            empleado = "Ana PÃ©rez",
            sucursal = "CENTRO",
            detalleTurno = "Cierre, 14:00 a 22:00 Â· 2026-09-08",
            diferencia = "90 minutos menos que el turno programado",
            colorBorde = "ROJO",
            colorVorde = "ROJO",
            empleadoId = "3",
            asignacionTurnoId = "t1"
        ),
        Novedad(
            id = "2",
            tipo = "AmpliaciÃ³n del turno",
            descripcion = "ExtensiÃ³n de turno por afluencia",
            horaInicioReal = null,
            horaFinReal = null,
            estado = EstadoNovedad.REPORTADA,
            empleado = "Marta LeÃ³n",
            sucursal = "CENTRO",
            detalleTurno = "Apertura, 06:00 a 14:00 Â· 2026-09-09",
            diferencia = "90 minutos mÃ¡s que el turno programado",
            colorBorde = "VERDE",
            colorVorde = "VERDE",
            empleadoId = "5",
            asignacionTurnoId = "t2"
        ),
        Novedad(
            id = "3",
            tipo = "AmpliaciÃ³n del turno",
            descripcion = "AmpliaciÃ³n por cierre y cuadre",
            horaInicioReal = null,
            horaFinReal = null,
            estado = EstadoNovedad.REPORTADA,
            empleado = "Carlos Ruiz",
            sucursal = "NORTE",
            detalleTurno = "Cierre, 14:00 a 22:00 Â· 2026-09-08",
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
            empleado = "JuliÃ¡n Vera",
            sucursal = "CENTRO",
            detalleTurno = "Intermedio, 10:00 a 18:00 Â· 2026-09-07",
            diferencia = "25 minutos menos que el turno programado",
            colorBorde = "ROJO_OSCURO",
            colorVorde = "ROJO_OSCURO",
            empleadoId = "7",
            asignacionTurnoId = "t4"
        )
    )

    fun inicialSolicitudesCambio(): List<SolicitudCambioTurno> = emptyList()

    fun inicialTurnosPropios(): List<TurnoSeleccionable> = listOf(
        TurnoSeleccionable("1", "Lunes 14 Â· Apertura", "06:00-14:00"),
        TurnoSeleccionable("2", "Martes 15 Â· Apertura", "06:00-14:00"),
        TurnoSeleccionable("3", "Jueves 17 Â· Cierre", "14:00-22:00"),
        TurnoSeleccionable("4", "Viernes 18 Â· Apertura", "06:00-14:00"),
        TurnoSeleccionable("5", "SÃ¡bado 19 Â· Apertura", "06:00-14:00")
    )

    fun inicialTurnosCompaneros(): List<TurnoCompaneroSeleccionable> = emptyList()

    fun inicialIncumplimientos(): List<NotificacionIncumplimiento> = emptyList()

    fun inicialRegistrosAsistencia(): List<RegistroAsistencia> = listOf(
        RegistroAsistencia("1", "Administrador General", "CENTRO", "05:58", "-", "PRESENTE")
    )
}
