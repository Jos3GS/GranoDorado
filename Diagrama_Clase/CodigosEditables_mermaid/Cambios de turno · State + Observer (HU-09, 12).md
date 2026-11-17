classDiagram
    direction TB

    class SolicitudesCambio {
        <<servicio>>
        +turnosElegibles(miAsignacion: Asignacion): List~Asignacion~
        +solicitarIntercambio(miAsignacion: Asignacion, asignacionCompanero: Asignacion, motivo: String): SolicitudCambio
        +aceptar(solicitud: SolicitudCambio, companero: Empleado): void
        +rechazarComoCompanero(solicitud: SolicitudCambio, companero: Empleado, motivo: String): void
        +cancelar(solicitud: SolicitudCambio, solicitante: Empleado): void
        +misSolicitudes(empleado: Empleado): List~SolicitudCambio~
        +vencerPendientes(ahora: DateTime): void
    }
    class AprobacionCambios {
        <<servicio>>
        +pendientesDeAprobacion(sucursal: Sucursal): List~SolicitudCambio~
        +detalle(id: Integer): SolicitudCambio
        +aprobar(solicitud: SolicitudCambio, administrador: Empleado): void
        +rechazar(solicitud: SolicitudCambio, administrador: Empleado, motivo: String): void
        +historico(sucursal: Sucursal): List~SolicitudCambio~
    }
    class SolicitudCambio {
        <<Context>>
        -motivo: String
        -motivoRechazo: String
        -creadaEn: DateTime
        +aceptar(usuario: Empleado): void
        +rechazar(usuario: Empleado, motivo: String): void
        +aprobar(usuario: Empleado): void
        +aplicar(): void
        +cancelar(usuario: Empleado): void
        +vencer(): void
        +turnoMasProximo(): DateTime
        -cambiarA(nuevo: EstadoSolicitud, usuario: Empleado, motivo: String): void
    }
    class CambioDeEstado {
        -estado: String
        -fechaHora: DateTime
        -motivo: String
    }
    class EstadoSolicitud {
        <<interface · State>>
        +nombre: String
        +aceptar(sol: SolicitudCambio, usuario: Empleado): EstadoSolicitud
        +rechazar(sol: SolicitudCambio, usuario: Empleado, motivo: String): EstadoSolicitud
        +aprobar(sol: SolicitudCambio, usuario: Empleado): EstadoSolicitud
        +aplicar(sol: SolicitudCambio): EstadoSolicitud
        +cancelar(sol: SolicitudCambio, usuario: Empleado): EstadoSolicitud
        +vencer(sol: SolicitudCambio): EstadoSolicitud
    }
    class Solicitada {
        <<estado inicial>>
    }
    class AceptadaPorElCompanero
    class Aprobada
    class EstadoFinal {
        <<abstract>>
    }
    class Aplicada
    class RechazadaPorElCompanero
    class RechazadaPorElAdministrador
    class Cancelada
    class Vencida
    class PublicadorCambiosSolicitud {
        <<Publisher>>
        -observadores: List~ObservadorSolicitud~
        +suscribir(o: ObservadorSolicitud): void
        +desuscribir(o: ObservadorSolicitud): void
        +notificar(sol: SolicitudCambio, cambio: CambioDeEstado): void
    }
    class ObservadorSolicitud {
        <<interface · Subscriber>>
        +alCambiarEstado(sol: SolicitudCambio, cambio: CambioDeEstado): void
    }
    class AvisosSolicitud {
        +alCambiarEstado(sol: SolicitudCambio, cambio: CambioDeEstado): void
    }
    class RegistroAuditoriaSolicitud {
        +alCambiarEstado(sol: SolicitudCambio, cambio: CambioDeEstado): void
    }
    class Empleado {
        <<entidad · otro bloque>>
    }
    class Asignacion {
        <<entidad · otro bloque>>
    }
    class MotorValidacion {
        <<servicio · otro bloque>>
    }
    class ProgramacionSemanal {
        <<servicio · otro bloque>>
    }
    class ServicioNotificaciones {
        <<servicio · otro bloque>>
    }
    class ServicioAuditoria {
        <<servicio · otro bloque>>
    }
    class PlanificadorTareas {
        <<servicio · otro bloque>>
    }

    SolicitudCambio "0..*" --> "1" Empleado : solicitante
    SolicitudCambio "0..*" --> "1" Empleado : compañero
    SolicitudCambio "0..*" --> "1" Asignacion : turno entregado
    SolicitudCambio "0..*" --> "1" Asignacion : turno recibido
    SolicitudCambio "1" *-- "1..*" CambioDeEstado : historial
    SolicitudCambio "1" --> "1" EstadoSolicitud : estado actual
    EstadoSolicitud <|.. Solicitada
    EstadoSolicitud <|.. AceptadaPorElCompanero
    EstadoSolicitud <|.. Aprobada
    EstadoSolicitud <|.. EstadoFinal
    EstadoFinal <|-- Aplicada
    EstadoFinal <|-- RechazadaPorElCompanero
    EstadoFinal <|-- RechazadaPorElAdministrador
    EstadoFinal <|-- Cancelada
    EstadoFinal <|-- Vencida
    SolicitudCambio ..> PublicadorCambiosSolicitud : notifica cada transición
    PublicadorCambiosSolicitud "1" o-- "0..*" ObservadorSolicitud : avisa a
    ObservadorSolicitud <|.. AvisosSolicitud
    ObservadorSolicitud <|.. RegistroAuditoriaSolicitud
    AvisosSolicitud ..> ServicioNotificaciones : envía aviso
    RegistroAuditoriaSolicitud ..> ServicioAuditoria : registra decisión
    SolicitudesCambio ..> SolicitudCambio : crea y resuelve
    AprobacionCambios ..> SolicitudCambio : aprueba o rechaza
    SolicitudesCambio ..> MotorValidacion : turnos elegibles
    AprobacionCambios ..> MotorValidacion : revalida (CA-12.5)
    AprobacionCambios ..> ProgramacionSemanal : intercambia asignaciones
    PlanificadorTareas ..> SolicitudesCambio : vence pendientes

    classDef servicio fill:#eff6ff,stroke:#60a5fa,color:#1e3a8a
    classDef interface fill:#f0fdfa,stroke:#2dd4bf,color:#134e4a
    classDef otrobloque fill:#ffffff,stroke:#9ca3af,stroke-dasharray:4 3,color:#6b7280

    cssClass "AprobacionCambios,SolicitudesCambio" servicio
    cssClass "EstadoSolicitud,ObservadorSolicitud" interface
    cssClass "Asignacion,Empleado,MotorValidacion,PlanificadorTareas,ProgramacionSemanal,ServicioAuditoria,ServicioNotificaciones" otrobloque