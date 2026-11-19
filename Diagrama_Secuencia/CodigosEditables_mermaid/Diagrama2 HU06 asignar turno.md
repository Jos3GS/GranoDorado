---
title: "sd HU-06 Asignar turno y publicar semana (Épica 2)"
---
sequenceDiagram
    autonumber
    actor A as Administrador General<br/>o de Sucursal
    participant MS as :MallaSemanalScreen<br/>«frontera»
    participant MVM as :MallaSemanalViewModel<br/>«control»
    participant CH as :ConsultarHorarioEmpleadoUseCase<br/>«control»
    participant VA as :ValidarAsignacionTurnoUseCase<br/>«control»
    participant TR as :TurnoRepository<br/>«repositorio»
    participant FS as :FirestoreService<br/>«acceso a Cloud Firestore»

    Note over A,FS: Fase 1 - Asignar un turno en la malla
    activate A
    A->>+MS: Elige un turno del catálogo para un empleado y un día
    MS->>+MVM: asignarTurnoAEmpleado(idEmpleado, diaIndex, turnoTemplate)
    MVM->>+CH: validarPermisoModificarProgramacion(rol, esSemanaPasada)
    CH-->>-MVM: resultado : ValidacionSoloLecturaResult
    alt la semana ya pasó o el rol no tiene permiso
        MVM-->>MS: mensajeError : String
    else el rol puede modificar la programación
        alt la celda tiene vacaciones, incapacidad o permiso (RN-17) (CA-06.2)
            MVM-->>MS: mensajeError : String
        else la celda está disponible
            MVM->>+TR: asignaciones.value
            TR-->>-MVM: asignaciones : List<AsignacionTurno>
            MVM->>+VA: validarAsignacion(empleado, sucursalEmpleado, sucursalProgramacion, fechaAsignacion, turnosSemanaActuales, nuevoTurno, diaIndex, todasAsignaciones)
            Note over VA: Verifica en orden semana modificable, fecha no pasada, empleado activo, misma sucursal, un turno por día (RN-16), máximo 42 horas semanales (RN-16) y disponibilidad (RN-17)
            VA-->>-MVM: resultado : ValidacionAsignacionResult
            alt el resultado es Invalido (CA-06.3, CA-06.4 y CA-06.5)
                MVM-->>MS: mensajeError : String
            else el resultado es Valido (CA-06.1)
                MVM->>+MVM: celdas[diaIndex] = nuevoTurno y hayCambiosPendientes = true
                MVM-->>-MVM: listo : Unit
                opt el empleado llega a 5 turnos de trabajo en la semana
                    MVM->>+MVM: completa los días sin asignar como Descanso
                    MVM-->>-MVM: listo : Unit
                end
                MVM-->>MS: filas : List<FilaMalla>
            end
        end
    end
    deactivate MVM
    MS-->>-A: Muestra el turno asignado o el mensaje de error
    deactivate A

    Note over A,FS: Fase 2 - Publicar la semana (el guardado estaba diferido)
    activate A
    A->>+MS: Pulsa Publicar semana
    MS->>+MVM: publicarSemana()
    MVM->>+CH: validarPermisoModificarProgramacion(rol, esSemanaPasada)
    CH-->>-MVM: resultado : ValidacionSoloLecturaResult
    alt no hay permiso o la semana no es modificable
        MVM-->>MS: mensajeError : String
    else la semana se puede publicar
        MVM->>+MVM: publicado = true y días sin asignar pasan a Descanso
        MVM-->>-MVM: listo : Unit
        MVM->>+TR: guardarAsignacionesEnBackground(asignaciones con estado PUBLICADA)
        TR->>+TR: aplicarAsignacionesEnMemoria(lista)
        TR-->>-TR: listo : Unit
        TR-)FS: guardarAsignacionesBatch(lista)
        activate FS
        TR-->>-MVM: listo : Unit
        alt el guardado en Firestore termina bien
            FS--)TR: resultado : Result.success
        else el guardado en Firestore falla
            FS--)TR: resultado : Result.failure
            TR->>+TR: println(error)
            TR-->>-TR: listo : Unit
        end
        deactivate FS
        MVM-->>MS: estadoMalla = "PUBLICADA - VISIBLE PARA LOS EMPLEADOS"
        Note over MVM: RN-32 registra el evento de publicación con RegistrarEventoAuditoriaUseCase
    end
    deactivate MVM
    MS-->>-A: Muestra la semana publicada
    deactivate A