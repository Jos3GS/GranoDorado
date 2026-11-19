---
title: "sd HU-14 Marcar asistencia al turno (Épica 5)"
---
sequenceDiagram
    autonumber
    actor E as Empleado
    participant MS as :MarcarAsistenciaScreen<br/>«frontera»
    participant MVM as :MarcarAsistenciaViewModel<br/>«control»
    participant EA as :ValidarEstadoEmpleadoActivoUseCase<br/>«control»
    participant VM as :ValidarMarcacionAsistenciaUseCase<br/>«control»
    participant AR as :AsistenciaRepository<br/>«repositorio»
    participant FS as :FirestoreService<br/>«acceso a Cloud Firestore»

    activate E
    E->>+MS: Pulsa Marcar entrada
    MS->>+MVM: marcarEntrada()
    MVM->>+EA: validar(empleadoActivo, "asistencia")
    EA-->>-MVM: resultado : ValidacionEstadoEmpleadoResult
    alt el empleado está inactivo (RN-11)
        MVM-->>MS: mensajeAviso : String
    else el empleado está activo
        MVM->>+VM: validarHabilitacionTurno(empleadoActivo, asignacionDeHoy, hoyIso)
        VM-->>-MVM: resultado : ValidacionMarcacionResult
        alt hoy no hay turno de trabajo publicado, hay incapacidad, vacaciones o permiso (CA-14.4)
            MVM-->>MS: mensajeAviso : String
        else el turno de hoy habilita la marcación
            MVM->>+VM: validarTransicionEstado(estadoActual, EN_TURNO)
            VM-->>-MVM: resultado : ValidacionMarcacionResult
            alt la jornada ya inició o ya finalizó (CA-14.3)
                MVM-->>MS: mensajeAviso : String
            else la acción es válida en la secuencia (CA-14.1)
                MVM->>+MVM: obtenerHoraActualTexto()
                MVM-->>-MVM: horaActual : String
                MVM->>+MVM: calcularRetraso(horario, horaActual)
                MVM-->>-MVM: minutosTarde : Int
                create participant RA as registro:RegistroAsistencia<br/>«entidad»
                MVM-->>RA: «create» estadoAsistencia = EN_TURNO, estado = PRESENTE o TARDE
                activate RA
                par actualizar la pantalla
                    MVM-->>MS: estadoActual = EN_TURNO y mensajeAviso con la hora marcada
                and guardar en segundo plano
                    MVM-)AR: guardarRegistroAsistenciaEnBackground(registro)
                    activate AR
                    AR->>+AR: guardarRegistroAsistencia() actualiza registrosDiarios en memoria
                    AR-->>-AR: listo : Unit
                    AR->>+FS: guardarAsistencia(registro)
                    FS-->>-AR: resultado : Result<Unit>
                    deactivate AR
                end
                deactivate RA
                opt el empleado llegó tarde, minutosTarde mayor que cero (CA-14.2)
                    MVM->>+AR: resolverIncumplimientoPorLlegada(empleadoId, fechaIso)
                    loop mientras haya incumplimientos pendientes de ese empleado y fecha
                        AR->>+FS: guardarIncumplimiento(notificación atendida)
                        FS-->>-AR: resultado : Result<Unit>
                    end
                    AR-->>-MVM: resultado : Result<Unit>
                end
            end
        end
    end
    deactivate MVM
    MS-->>-E: Muestra el estado de la jornada y el aviso
    deactivate E