classDiagram
    direction LR

    class ServicioNotificaciones {
        <<servicio · Client>>
        -canalCorreo: CanalEnvio
        -canalAvisos: CanalEnvio
        +enviarCorreoActivacion(correo: String, enlace: String): void
        +enviarEnlaceRecuperacion(correo: String, enlace: String): void
        +avisarPublicacion(semana: Semana): void
        +avisarSolicitud(solicitud: SolicitudCambio, destinatarios: List~Empleado~): void
        +avisarIncumplimiento(incumplimiento: Incumplimiento): void
        -redactar(): Mensaje
    }
    class Mensaje {
        <<valor>>
        -destinatario: String
        -asunto: String
        -contenido: String
    }
    class CanalEnvio {
        <<interface · Target>>
        +enviar(mensaje: Mensaje): ResultadoEnvio
    }
    class AdaptadorCorreo {
        <<Adapter>>
        -proveedor: ClienteProveedorCorreo
        +enviar(mensaje: Mensaje): ResultadoEnvio
    }
    class AdaptadorAvisos {
        <<Adapter · propuesta>>
        -servicio: ServicioDeAvisos
        +enviar(mensaje: Mensaje): ResultadoEnvio
    }
    class ClienteProveedorCorreo {
        <<externo · Gmail API · Adaptee>>
    }
    class ServicioDeAvisos {
        <<externo · Adaptee>>
    }
    class GestionEmpleados {
        <<servicio · otro bloque>>
    }
    class RecuperacionClave {
        <<servicio · otro bloque>>
    }
    class ProgramacionSemanal {
        <<servicio · otro bloque>>
    }
    class AvisosSolicitud {
        <<servicio · otro bloque>>
    }
    class Incumplimientos {
        <<servicio · otro bloque>>
    }
    class ServicioAuditoria {
        <<servicio>>
        +registrar(accion: TipoAccionAuditoria, usuario: Empleado, valores: String): EventoAuditoria
    }
    class EventoAuditoria {
        -fechaHora: DateTime
        -usuario: Empleado
        -accion: TipoAccionAuditoria
        -valorAnterior: String
        -valorNuevo: String
        -ip: String
    }
    class TipoAccionAuditoria {
        <<enumeration>>
        LOGIN_EXITOSO
        EMPLEADO_CREADO
        TURNO_CREADO
        TURNO_ASIGNADO
        SOLICITUD_RECUPERACION
    }
    class Empleado {
        <<entidad · otro bloque>>
    }

    ServicioNotificaciones "1" --> "2" CanalEnvio : canalCorreo y canalAvisos
    ServicioNotificaciones ..> Mensaje : redacta
    CanalEnvio <|.. AdaptadorCorreo
    CanalEnvio <|.. AdaptadorAvisos
    AdaptadorCorreo --> ClienteProveedorCorreo : envuelve
    AdaptadorAvisos --> ServicioDeAvisos : envuelve
    GestionEmpleados ..> ServicioNotificaciones : correo de activación
    RecuperacionClave ..> ServicioNotificaciones : enlace de recuperación
    ProgramacionSemanal ..> ServicioNotificaciones : publicación
    AvisosSolicitud ..> ServicioNotificaciones : cambios de turno
    Incumplimientos ..> ServicioNotificaciones : incumplimientos
    ServicioAuditoria ..> EventoAuditoria : crea
    EventoAuditoria ..> TipoAccionAuditoria : acción
    EventoAuditoria "0..*" --> "1" Empleado : ejecutado por

    note for ServicioAuditoria "Lo usan todos los módulos que modifican datos (RN-32): empleados, turnos, programación, solicitudes, novedades, marcas y accesos."

    classDef servicio fill:#eff6ff,stroke:#60a5fa,color:#1e3a8a
    classDef interface fill:#f0fdfa,stroke:#2dd4bf,color:#134e4a
    classDef enumeration fill:#fdf2f8,stroke:#f472b6,color:#831843
    classDef externo fill:#f3f4f6,stroke:#9ca3af,color:#374151
    classDef otrobloque fill:#ffffff,stroke:#9ca3af,stroke-dasharray:4 3,color:#6b7280

    cssClass "ServicioAuditoria,ServicioNotificaciones" servicio
    cssClass "CanalEnvio" interface
    cssClass "TipoAccionAuditoria" enumeration
    cssClass "ClienteProveedorCorreo,ServicioDeAvisos" externo
    cssClass "AvisosSolicitud,Empleado,GestionEmpleados,Incumplimientos,ProgramacionSemanal,RecuperacionClave" otrobloque