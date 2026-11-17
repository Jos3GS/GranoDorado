classDiagram
    direction LR

    class Empleado
    class Cuenta
    class Turno
    class Descanso
    class Semana
    class Asignacion
    class RegistroNoDisponibilidad
    class Marca
    class Incumplimiento
    class Novedad
    class ComentarioNovedad
    class SolicitudCambio
    class CambioDeEstado
    class EventoAuditoria

    Empleado "1" --> "1" Cuenta : tiene
    Empleado "1" o-- "0..*" RegistroNoDisponibilidad : no disponible
    Turno "1" *-- "0..*" Descanso : contiene
    Semana "1" *-- "0..*" Asignacion : contiene
    Asignacion "0..*" --> "1" Turno : utiliza
    Asignacion "0..*" --> "1" Empleado : asignada a
    Asignacion "1" *-- "0..*" Marca : registra
    Asignacion "1" *-- "0..1" Incumplimiento : genera
    Asignacion "1" *-- "0..*" Novedad : contiene
    Novedad "1" *-- "0..*" ComentarioNovedad : comentarios
    SolicitudCambio "0..*" --> "2" Asignacion : turno entregado y recibido
    SolicitudCambio "0..*" --> "2" Empleado : solicitante y compañero
    SolicitudCambio "1" *-- "1..*" CambioDeEstado : historial
    EventoAuditoria "0..*" --> "1" Empleado : ejecutado por