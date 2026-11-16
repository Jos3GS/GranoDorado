# Máquina de estados

## 1. Entidad modelada: SolicitudCambio

**SolicitudCambio** representa la petición de un empleado (**solicitante**) para intercambiar uno de sus turnos asignados (**turnoEntregado**) por el turno de un **compañero** (**turnoRecibido**). El intercambio solo se aplica si el compañero lo acepta y el **Administrador de Sucursal** lo aprueba después de validarlo. La entidad soporta las historias HU-09 (Solicitar cambio de turno) y HU-12 (Aprobar cambios de turno) del MVP 2.

Se modela con una máquina de estados porque las mismas operaciones (aceptar, rechazar, aprobar, cancelar) tienen efectos distintos según el estado, el actor y las condiciones de guarda; intervienen cuatro actores (solicitante, compañero, administrador y el sistema, mediante un evento de tiempo), y la solicitud bloquea los turnos mientras está activa.

- **Atributos:** solicitante, companero, turnoEntregado, turnoRecibido, motivo, motivoRechazo y estado.
- **Operaciones:** aceptar, rechazar, aprobar, cancelar, vencer, aplicar y turnoMasProximo.
- **Servicios utilizados:** cambiarA, registrar, avisarSolicitud, validarIntercambio (retorna ResultadoValidacion) e intercambiarAsignaciones.

## 2. Diagrama

![Diagrama de máquina de estados UML de la entidad SolicitudCambio](MAQUINA%20DE%20ESTADO.jpg)

**Enlace editable:** <https://miro.com/app/board/uXjVEPrfNFc=/?share_link_id=741616056525>

## 3. Estados de la entidad

La siguiente tabla describe cada estado, su clasificación dentro del ciclo de vida y las acciones internas UML (entry, do y exit) definidas en el diagrama.

| Estado | Tipo | Descripción | Acciones internas |
|---|---|---|---|
| ● Pseudoestado inicial | Inicial | Punto de partida del ciclo de vida. Se activa cuando el empleado crea la solicitud de cambio de turno. | — |
| Solicitada | Intermedio | La solicitud fue registrada por el empleado y está a la espera de la respuesta del compañero. | **entry** / cambiarA(Solicitada, solicitante, motivo); registrar(accion, solicitante, valores); avisarSolicitud(sol, companero)<br>**do** / mantener en misSolicitudes(empleado) y bloquear turnoEntregado y turnoRecibido (CA-09.7)<br>**exit** / cerrar espera de respuesta del compañero |
| AceptadaPorElCompanero | Intermedio | El compañero aceptó el intercambio. La solicitud queda pendiente de la decisión del administrador de la sucursal. | **entry** / avisarSolicitud(sol, administrador)<br>**do** / mantener en pendientesDeAprobacion(sucursal), ordenada por turnoMasProximo(), y bloquear turnoEntregado y turnoRecibido (CA-09.7, CA-12.1)<br>**exit** / retirar de pendientesDeAprobacion(sucursal) |
| Aprobada | Intermedio (transitorio) | El administrador aprobó la solicitud y el intercambio superó la validación. El sistema ejecuta el cambio de asignaciones. | **entry** / registrar(accion, usuario, valores)<br>**do** / intercambiarAsignaciones(turnoEntregado, turnoRecibido) (CA-12.3) |
| Aplicada | Terminal | El intercambio de turnos quedó aplicado en la programación. Es el desenlace exitoso del proceso. | **entry** / avisarSolicitud(sol, [solicitante, companero]) |
| RechazadaPorElCompanero | Terminal | El compañero rechazó el intercambio indicando un motivo. | **entry** / avisarSolicitud(sol, solicitante) |
| RechazadaPorElAdministrador | Terminal | El administrador rechazó la solicitud indicando un motivo. | **entry** / avisarSolicitud(sol, [solicitante, companero]) |
| Cancelada | Terminal | El solicitante retiró la solicitud antes de que fuera resuelta. | **entry** / registrar(accion, solicitante, valores) |
| Vencida | Terminal | Llegó la fecha y hora del turno más próximo sin que la solicitud fuera resuelta; el sistema la vence automáticamente. | **entry** / registrar(accion, sistema, valores) |
| ◉ Estado final | Final | Fin del ciclo de vida. Todos los estados terminales llegan aquí mediante una transición de finalización. | — |

## 4. Matriz de análisis de transiciones

La matriz identifica, para cada transición, el estado de origen, el evento que la dispara, la condición de guarda, la acción que se ejecuta y el estado de destino. Se incluyen también las transiciones internas, en las que la acción se rechaza y la solicitud permanece en el mismo estado. La notación sigue el formato UML **evento [guarda] / acción**.

| # | Estado origen | Evento | Condición de guarda | Acción | Estado destino | Ref. |
|---|---|---|---|---|---|---|
| T01 | ● Inicial | Creación de la solicitud por el empleado | — | Acciones entry de Solicitada: registrar(accion, solicitante, valores); avisarSolicitud(sol, companero) | Solicitada | HU-09 |
| T02 | Solicitada | aceptar(usuario) | usuario = companero | cambiarA(AceptadaPorElCompanero, usuario) | AceptadaPorElCompanero | HU-09 |
| T03 | Solicitada | rechazar(usuario, motivo) | usuario = companero ∧ motivo ≠ vacío | motivoRechazo = motivo; cambiarA(RechazadaPorElCompanero, usuario, motivo) | RechazadaPorElCompanero | CA-09.3 |
| T04 | Solicitada | rechazar(usuario, motivo) | usuario = companero ∧ motivo = vacío | Rechazar la acción y mostrar «Indica el motivo del rechazo.» (transición interna) | Solicitada (sin cambio) | CA-09.3 |
| T05 | Solicitada | cancelar(usuario) | usuario = solicitante | cambiarA(Cancelada, usuario) | Cancelada | HU-09 |
| T06 | Solicitada | at(turnoMasProximo()) (evento de tiempo) | ahora ≥ turnoMasProximo() | vencer() → cambiarA(Vencida) | Vencida | CA-09.7 |
| T07 | AceptadaPorElCompanero | aprobar(usuario) | adminHabilitado ∧ validarIntercambio(sol).valido | cambiarA(Aprobada, usuario) | Aprobada | CA-12.3 |
| T08 | AceptadaPorElCompanero | aprobar(usuario) | adminHabilitado ∧ ¬validarIntercambio(sol).valido | Mostrar los mensajes de ResultadoValidacion (transición interna) | AceptadaPorElCompanero (sin cambio) | CA-12.5 |
| T09 | AceptadaPorElCompanero | rechazar(usuario, motivo) | adminHabilitado ∧ motivo ≠ vacío | motivoRechazo = motivo; cambiarA(RechazadaPorElAdministrador, usuario, motivo) | RechazadaPorElAdministrador | CA-12.4 |
| T10 | AceptadaPorElCompanero | rechazar(usuario, motivo) | adminHabilitado ∧ motivo = vacío | Rechazar la acción y mostrar «Indica el motivo del rechazo.» (transición interna) | AceptadaPorElCompanero (sin cambio) | CA-12.4 |
| T11 | AceptadaPorElCompanero | cancelar(usuario) | usuario = solicitante | cambiarA(Cancelada, usuario) | Cancelada | HU-09 |
| T12 | AceptadaPorElCompanero | at(turnoMasProximo()) (evento de tiempo) | ahora ≥ turnoMasProximo() | vencer() → cambiarA(Vencida) | Vencida | CA-09.7, CA-12.1 |
| T13 | Aprobada | aplicar() | — | cambiarA(Aplicada) | Aplicada | CA-12.3 |
| T14 | Aplicada, RechazadaPorElCompanero, RechazadaPorElAdministrador, Cancelada, Vencida | Finalización (sin evento) | — | Fin del ciclo de vida | ◉ Estado final | — |

## 5. Relación con las funcionalidades del MVP

La máquina de estados implementa dos historias **Should Have** del **MVP 2**: HU-09, de la Épica 3 (Portal de empleado), y HU-12, de la Épica 4 (Gestión de novedades). La tabla relaciona cada paso de esas historias con los elementos del diagrama.

| Historia (story map) | Funcionalidad del MVP | Elementos de la máquina de estados | Criterio |
|---|---|---|---|
| **HU-09** · Solicitar cambio de turno<br>Épica 3 · MVP 2 | Registrar la solicitud de intercambio | T01; estado Solicitada (entry: registrar y avisar al compañero) | — |
| | Bloquear los turnos involucrados | Actividad do de Solicitada y AceptadaPorElCompanero | CA-09.7 |
| | Consultar «Mis solicitudes» | Actividad do de Solicitada | — |
| | Aceptar como compañero | T02 | — |
| | Rechazar como compañero con motivo obligatorio | T03, T04; estado RechazadaPorElCompanero | CA-09.3 |
| | Cancelar la solicitud propia | T05, T11; estado Cancelada | — |
| | Vencer la solicitud no resuelta | T06, T12; estado Vencida | — |
| **HU-12** · Aprobar cambios de turno<br>Épica 4 · MVP 2 | Ver las solicitudes acordadas, ordenadas por turno más próximo | Estado AceptadaPorElCompanero (entry, do, exit) | CA-12.1 |
| | Aprobar conociendo los efectos del cambio | T07, T08 (validarIntercambio y ResultadoValidacion) | CA-12.5 |
| | Rechazar con motivo obligatorio | T09, T10; estado RechazadaPorElAdministrador | CA-12.4 |
| | Aplicar el intercambio de asignaciones | Estado Aprobada (do), T13; estado Aplicada | CA-12.3 |
