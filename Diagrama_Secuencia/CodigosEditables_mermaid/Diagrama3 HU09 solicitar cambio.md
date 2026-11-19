---
title: "sd HU-09 Solicitar cambio de turno (Épica 3)"
---
sequenceDiagram
    autonumber
    actor E as Empleado solicitante
    participant CS as :CambiosTurnoScreen<br/>«frontera»
    participant CVM as :CambiosTurnoViewModel<br/>«control»
    participant EA as :ValidarEstadoEmpleadoActivoUseCase<br/>«control»
    participant VS as :ValidarSolicitudCambioTurnoUseCase<br/>«control»
    participant ER as :EmpleadoRepository<br/>«repositorio»
    participant TR as :TurnoRepository<br/>«repositorio»
    participant CR as :CambiosTurnoRepository<br/>«repositorio»
    participant FS as :FirestoreService<br/>«acceso a Cloud Firestore»

    activate E
    E->>+CS: Elige su turno, el turno del compañero, escribe el motivo y pulsa Enviar
    CS->>+CVM: enviarSolicitud()
    break falta el motivo o alguno de los dos turnos
        CVM-->>CS: sin cambios (no se crea la solicitud)
    end
    CVM->>+ER: empleados.value
    ER-->>-CVM: empleados : List<Empleado>
    CVM->>+EA: validar(empleado, "cambio_turno")
    EA-->>-CVM: resultado : ValidacionEstadoEmpleadoResult
    alt el empleado está inactivo (RN-11)
        CVM-->>CS: mensajeFeedback : String
    else el empleado está activo
        CVM->>+TR: asignaciones.value
        TR-->>-CVM: asignaciones : List<AsignacionTurno>
        CVM->>+CR: solicitudes.value
        CR-->>-CVM: solicitudes : List<SolicitudCambioTurno>
        CVM->>+VS: validarSolicitud(solicitante, companero, turnoEntrega, turnoRecibe, solicitudesExistentes, todasAsignaciones)
        Note over VS: Verifica empleados activos, misma sucursal y mismo cargo, turnos publicados y de trabajo, fechas no vencidas, sin solicitud en curso, sin doble turno, descanso mínimo de 12 horas y máximo de 42 horas semanales
        VS-->>-CVM: resultado : ValidacionSolicitudCambioResult
        alt el resultado es Invalido (CA-09.4, CA-09.5 y CA-09.7)
            CVM-->>CS: mensajeFeedback : String
        else el resultado es Valido (CA-09.1)
            create participant SOL as solicitud:SolicitudCambioTurno<br/>«entidad»
            CVM-->>SOL: «create» estado = PENDIENTE
            activate SOL
            CVM-)CR: crearSolicitud(solicitud)
            activate CR
            CR->>+CR: guardarSolicitud() actualiza la lista en memoria
            CR-->>-CR: listo : Unit
            CR->>+FS: guardarSolicitudCambio(solicitud)
            FS-->>-CR: resultado : Result<Unit>
            deactivate CR
            deactivate SOL
            CVM-->>CS: toggleModalSolicitud(false)
        end
    end
    deactivate CVM
    CS-->>-E: Muestra la solicitud pendiente o el mensaje de error
    deactivate E
    Note over CVM,CR: Continúa en el diagrama 4 - el compañero la acepta con aceptarSolicitud() (estado ACEPTADA_COMPANERO) y la resuelve el administrador