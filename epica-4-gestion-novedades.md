# Épica 4 — Gestión de novedades

**Resumen:** Agrupa lo que atiende el Administrador de Sucursal: ver las novedades (HU-11), aprobar los cambios de turno (HU-12) y reportar las novedades que el empleado no registró (HU-13).

*Resumen elaborado a partir de las historias de la épica; el documento original no incluye una descripción propia de esta épica.*

**Línea de la épica en el documento original:** HU-11 Ver novedades · HU-12 Aprobar cambios de turno · HU-13 Reportar novedades de empleado

## Historias de usuario de esta épica

| Sección | Identificador | Nombre |
|---|---|---|
| 4.1 | [HU-11](#41-hu-11--ver-novedades) | Ver novedades |
| 4.2 | [HU-12](#42-hu-12--aprobar-cambios-de-turno) | Aprobar cambios de turno |
| 4.3 | [HU-13](#43-hu-13--reportar-novedades-de-empleado) | Reportar novedades de empleado |

---

## 4.1 HU-11 — Ver novedades

**HU-11**

### Épica

Épica 4 — Gestión de novedades

### Nombre HU

Ver novedades

### Prioridad

Sin ella el reporte de novedades del empleado no tiene destinatario y el circuito queda abierto.

### Actor

Administrador de Sucursal sobre los empleados de su sucursal. Administrador General sobre las tres.

### Historia de usuario

**Como** Administrador de Sucursal de Grano Dorado, **quiero** consultar en un solo lugar las novedades que reportó mi equipo, con el turno afectado y la diferencia frente a lo programado, **para** estar al tanto de lo que ocurre en cada jornada y dejar constancia de que las conocí.

### Contexto

Que el empleado pueda reportar una novedad no sirve de nada si nadie la lee. Hoy el supervisor se entera de que alguien salió antes porque se lo cuentan, y esa información no sobrevive a la semana. Esta historia es el otro extremo del reporte: el lugar donde la novedad llega se lee y queda marcada como conocida. Es también la primera vez que el supervisor puede ver, junta, la diferencia entre lo que programó y lo que ocurrió en su sucursal.

### Valor esperado

- Cierra el circuito del reporte de novedades: lo que el empleado escribe llega a alguien y queda constancia de que fue leído.
- Le da al supervisor una visión consolidada de su sucursal que hoy no existe en ningún soporte.
- Medición: al menos el 80 % de las novedades reportadas quedan marcadas como revisadas dentro de las 72 horas siguientes.
- Medición: el supervisor puede responder, en menos de 1 minuto, cuántas novedades tuvo su sucursal en una semana y de qué tipo.

### Alcance

- Bandeja con las novedades de los empleados de mi sucursal, con las pendientes de revisar primero y las más recientes arriba.
- Contador de novedades pendientes de revisión, visible al abrir la bandeja.
- Filtros por rango de fechas, empleado, tipo de novedad y estado.
- Detalle de una novedad: empleado, turno programado con su horario, horario reportado, descripción, quién la reportó y cuándo.
- Acción de marcar como revisada, con un comentario opcional que el empleado puede leer.
- Consulta del histórico de novedades de la sucursal por rango de fechas.
- Selector de sucursal para el Administrador General.

### Fuera de alcance

- Reportar una novedad, tanto la del empleado como la del supervisor.
- Aprobar o rechazar una novedad con algún efecto vinculante: aquí se revisa, no se autoriza.
- Modificar el turno programado, las marcas de asistencia o el contenido de la novedad.
- Cualquier efecto sobre remuneración, horas extra o medidas disciplinarias.
- Indicadores, tendencias o comparación de novedades entre sucursales.
- Aviso automático al supervisor cuando llega una novedad nueva.
- Exportar o imprimir la bandeja.

### Reglas de negocio

- El Administrador de Sucursal ve únicamente novedades de empleados de su sucursal. El Administrador General ve las de las tres y puede alternar entre ellas.
- Cada novedad muestra el turno programado, el horario reportado y la diferencia en minutos entre ambos, calculada por el sistema.
- Una novedad tiene dos estados: reportada y revisada.
- Al marcar una novedad como revisada se registra el nombre del revisor y la fecha y hora.
- Una novedad revisada no puede volver al estado reportada.
- El supervisor no puede modificar ni eliminar el contenido de una novedad, la haya reportado quien la haya reportado.
- El comentario del revisor es visible para el empleado que reportó la novedad.
- La bandeja muestra por defecto las novedades del día en curso, con acceso al histórico mediante el filtro de fechas.
- Revisar una novedad no altera el turno programado, la asignación ni las marcas de asistencia.
- Las novedades de un empleado que pasó a inactivo siguen siendo consultables en el histórico.
- Ningún empleado con rol operativo tiene acceso a esta bandeja.

### Supuestos

- El supervisor revisa la novedad, pero no la aprueba ni la rechaza. El caso solo pide conocer las novedades reportadas.
- El supervisor entra a la bandeja por iniciativa propia. El caso no pide un aviso automático cuando llega una novedad nueva.
- No existe un plazo obligatorio para revisar una novedad. La medición de 72 horas es una meta propuesta, no una regla del sistema.
- La bandeja abre por defecto en el día en curso. El caso no define ningún horizonte.
- El catálogo de tipos de novedad es el mismo que usa el empleado al reportar.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como "Carla Ríos", Administradora de Sucursal
    de "Centro"
  Y "Ana Pérez" reportó una novedad de tipo "Salida antes de la hora"
    sobre su turno "Cierre" (14:00 a 22:00) del "2026-09-08"
  Y en esa novedad declaró la hora real de salida "20:30"
```

#### CA-11.1 Bandeja de novedades de la sucursal

```gherkin
  Cuando abro la bandeja de novedades
  Entonces veo la novedad de "Ana Pérez" con estado "Reportada"
  Y las novedades pendientes de revisar aparecen antes que las revisadas
  Y la pantalla muestra el contador "1 novedad pendiente de revisión"
```

#### CA-11.2 Detalle con la diferencia frente a lo programado

```gherkin
  Cuando abro el detalle de esa novedad
  Entonces veo el turno programado "Cierre, 14:00 a 22:00"
  Y la hora real reportada "20:30"
  Y la diferencia "90 minutos menos que el turno programado"
  Y la descripción escrita por la empleada
  Y el dato "Reportada por Ana Pérez el 08/09/2026 a las 20:35"
```

#### CA-11.3 Revisión con comentario visible para el empleado

```gherkin
  Cuando escribo el comentario "Confirmado, avisó por teléfono"
  Y presiono el botón "Marcar como revisada"
  Entonces la novedad pasa a estado "Revisada"
  Y queda registrada como "Revisada por Carla Ríos el 09/09/2026"
  Y "Ana Pérez" puede leer ese comentario en su lista de novedades
  Y el contador de pendientes pasa a "0 novedades pendientes de revisión"
```

#### CA-11.4 Una novedad revisada no se reabre ni se altera

```gherkin
  Dado que la novedad está en estado "Revisada"
  Cuando abro su detalle
  Entonces no se ofrece ninguna opción para volverla a "Reportada"
  Y no se ofrece ninguna opción para modificar ni eliminar su contenido
  Y el turno del "2026-09-08" sigue registrado como "Cierre, 14:00 a 22:00"
```

#### CA-11.5 Filtros de la bandeja

```gherkin
  Dado que mi sucursal tiene novedades de varios empleados y tipos
  Cuando filtro por el rango "2026-09-01" a "2026-09-08", el empleado
    "Ana Pérez" y el tipo "Salida antes de la hora"
  Entonces el listado muestra únicamente novedades que cumplen las tres
    condiciones
  Y al limpiar los filtros vuelve a mostrarse el día en curso
```

#### CA-11.6 Alcance por sucursal y por rol

```gherkin
  Dado que existen novedades en "Centro", "Norte" y "Sur"
  Cuando abro la bandeja
  Entonces solo veo novedades de empleados de "Centro"
  Y cuando el "Administrador General" abre la bandeja, puede alternar
    entre las tres sucursales
  Y si inicio sesión con el rol "Barista" la bandeja no está visible
    en el menú
```

### RNF aplicables

- RNF-01 (Rendimiento): la bandeja de novedades con sus turnos se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): solo el administrador autenticado consulta la bandeja, limitado a la sucursal que le corresponde.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): cada novedad muestra con claridad el empleado, la fecha y la hora del turno afectado.

### Dependencias

- Requiere que el empleado pueda reportar novedades: es lo que llena esta bandeja.
- Requiere los turnos asignados y publicados, con su horario, para calcular la diferencia.
- Requiere la identidad del supervisor con su sucursal, para delimitar qué novedades ve.
- Es donde se revisan también las novedades que el propio supervisor registra.

### Riesgos

- Alto. Sin aviso automático, el supervisor puede tardar días en abrir la bandeja y la novedad pierde su utilidad para reaccionar. Mitigación: decidir si se incorpora un aviso o si basta con el contador de pendientes en la pantalla principal.
- Medio. Si nadie revisa, los empleados dejan de reportar y la bandeja queda vacía por desuso, no por ausencia de novedades. Mitigación: medir el porcentaje revisado y hacerlo visible al Administrador General.
- Medio. Sin una decisión concreta que dependa de las novedades, revisarlas se vuelve un trámite. Mitigación: acordar qué alimenta esta información, por ejemplo, la programación de la semana siguiente.
- Bajo. Un supervisor podría querer corregir una novedad mal reportada. Mitigación: no se modifica el contenido; la corrección se hace mediante el comentario del revisor.

### Datos de prueba

- Carla Ríos, Administradora de Sucursal de Centro, con sesión iniciada.
- Novedad de Ana Pérez, tipo "Salida antes de la hora", sobre el turno "Cierre" del 08/09/2026, con hora real 20:30 y 90 minutos de diferencia.
- Novedades adicionales de otros empleados de Centro y de otros tipos, para los filtros.
- Novedades de empleados de Norte y Sur, para la validación de alcance.
- Una novedad ya revisada, para verificar que no se reabre.
- Un empleado inactivo con novedades históricas.
- Sesión con rol Barista, para verificar que la bandeja no aparece.

### Evaluación DoR

**Veredicto: lista con reserva.** La historia tiene actor, valor medible, alcance delimitado, reglas completas, seis criterios verificables, requisitos no funcionales y datos de prueba.

- Reserva: decidir si el supervisor recibe un aviso cuando llega una novedad nueva o si basta con el contador de pendientes.
- Reserva: retirar del reporte de novedades del empleado la bandeja de revisión que allí se había incluido, para que este alcance quede en un solo lugar.
- No es independiente: sin el reporte del empleado, la bandeja no tiene contenido.

---

## 4.2 HU-12 — Aprobar cambios de turno

**HU-12**

### Épica

Épica 4 — Gestión de novedades

### Nombre HU

Aprobar cambios de turno

### Prioridad

Es el paso que convierte un acuerdo entre empleados en un cambio efectivo y conocido.

### Actor

Administrador de Sucursal sobre las solicitudes de su sucursal. Administrador General sobre las tres.

### Historia de usuario

**Como** Administrador de Sucursal de Grano Dorado, **quiero** ver las solicitudes de cambio de turno que mis empleados ya acordaron entre ellos y aprobarlas o rechazarlas con conocimiento de sus efectos, **para** que trabajen en el turno que más les convenga sin que la cobertura de la sucursal se vea afectada.

### Contexto

El caso describe el intercambio de turnos como un proceso caótico en el que el administrador se entera a último minuto. La solicitud que crea el empleado y la aceptación de su compañero ya ocurren en el sistema; lo que falta es el momento en que el supervisor decide. Ese momento es el que hoy no existe: hasta ahora el administrador no aprobaba, se enteraba. Esta historia lo convierte en una decisión con toda la información a la vista y antes de que el turno ocurra.

### Valor esperado

- El supervisor pasa de enterarse a último minuto a decidir con antelación, que es exactamente el problema que el caso señala.
- Ningún turno cambia de responsable sin que el supervisor lo sepa y lo haya autorizado.
- Medición: al menos el 90 % de las solicitudes se resuelven, aprobadas o rechazadas, antes de vencer.
- Medición: cero turnos descubiertos a causa de un intercambio que el supervisor desconocía.

### Alcance

- Bandeja de solicitudes pendientes de aprobación de mi sucursal, ordenadas por proximidad del turno más cercano.
- Indicador de las solicitudes próximas a vencer.
- Detalle de la solicitud: solicitante, compañero, ambos turnos con su fecha y horario, motivo escrito por el solicitante y resultado de las validaciones que el sistema ya verificó.
- Aprobar la solicitud, lo que aplica el intercambio en el horario de ambos empleados y les avisa.
- Rechazar la solicitud con un motivo obligatorio, visible para ambos empleados.
- Revalidación de las reglas en el momento de aprobar, con aviso si alguna dejó de cumplirse desde que se creó la solicitud.
- Consulta del histórico de solicitudes resueltas de la sucursal.
- Selector de sucursal para el Administrador General.

### Fuera de alcance

- Crear una solicitud o aceptarla en nombre de un empleado.
- Proponer un turno alternativo distinto al que se solicitó.
- Modificar los turnos involucrados durante la aprobación.
- Reasignar un turno directamente sin que exista una solicitud, que pertenece a la planificación.
- Aprobar solicitudes de otra sucursal, salvo para el Administrador General.
- Efectos sobre remuneración, recargos u horas extra.
- Escalamiento automático de una solicitud que nadie atiende.

### Reglas de negocio

- A la bandeja solo llegan las solicitudes que el compañero ya aceptó. Una solicitud aún sin respuesta del compañero no se muestra para aprobación.
- El Administrador de Sucursal solo ve y resuelve solicitudes cuyos turnos pertenecen a su sucursal. El Administrador General puede resolver las de las tres.
- Un supervisor no puede aprobar ni rechazar una solicitud en la que él sea el solicitante o el compañero.
- Al aprobar, el sistema vuelve a verificar todas las reglas del intercambio. Si alguna dejó de cumplirse, no aplica el cambio y explica cuál falló.
- Aprobar intercambia los turnos de ambos empleados y les avisa a los dos. Ninguno conserva su turno original.
- Rechazar exige un motivo escrito, deja ambos horarios sin cambios y el motivo es visible para los dos empleados.
- Una solicitud que llegó al inicio del turno sin resolverse queda vencida y ya no se puede aprobar ni rechazar.
- Una solicitud resuelta no se reabre. Revertir un intercambio ya aplicado exige una nueva solicitud.
- La bandeja ordena las solicitudes por la proximidad del turno más cercano de los dos involucrados.
- Cada decisión queda registrada con el usuario que la tomó, la fecha y hora, y el motivo cuando corresponde.
- Ningún empleado con rol operativo tiene acceso a esta bandeja.

### Supuestos

- El Administrador General puede aprobar solicitudes de cualquiera de las tres sucursales. El caso no lo establece.
- No existe un segundo nivel de aprobación ni escalamiento automático si la solicitud no se atiende.
- Quién aprueba cuando el Administrador de Sucursal está de descanso o de vacaciones sigue sin definirse.
- No hay un plazo máximo para decidir distinto del vencimiento de la propia solicitud, que ocurre al iniciar el turno.
- El supervisor no puede aprobar parcialmente: la solicitud se acepta o se rechaza completa.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como "Carla Ríos", Administradora de Sucursal
    de "Centro"
  Y "Ana Pérez" solicitó intercambiar su turno "Cierre" (14:00 a 22:00)
    del "2026-09-17" con el turno "Apertura" (06:00 a 14:00) del
    "2026-09-18" de "Sofía Ramos"
  Y "Sofía Ramos" ya aceptó la solicitud
```

#### CA-12.1 Bandeja de solicitudes pendientes

```gherkin
  Cuando abro la bandeja de cambios de turno
  Entonces veo la solicitud de "Ana Pérez" con estado "Aceptada por
    el compañero"
  Y las solicitudes se ordenan por la proximidad del turno más cercano
  Y una solicitud aún sin respuesta del compañero NO aparece en la bandeja
```

#### CA-12.2 Detalle de la solicitud

```gherkin
  Cuando abro el detalle de la solicitud
  Entonces veo a "Ana Pérez" con su turno "Cierre, 17/09, 14:00 a 22:00"
  Y a "Sofía Ramos" con su turno "Apertura, 18/09, 06:00 a 14:00"
  Y el motivo escrito por la solicitante
  Y el resultado de las validaciones que el sistema ya verificó
```

#### CA-12.3 Aprobación y aplicación del intercambio

```gherkin
  Cuando presiono el botón "Aprobar"
  Entonces la solicitud pasa a estado "Aplicada"
  Y el horario de "Ana Pérez" del "2026-09-18" muestra "Apertura,
    06:00 a 14:00"
  Y el horario de "Sofía Ramos" del "2026-09-17" muestra "Cierre,
    14:00 a 22:00"
  Y ninguna de las dos conserva su turno original
  Y ambas reciben el aviso del cambio
  Y la decisión queda registrada con mi nombre y la fecha y hora
```

#### CA-12.4 Rechazo con motivo obligatorio

```gherkin
  Cuando presiono "Rechazar" sin escribir el motivo
  Entonces el sistema muestra "Indica el motivo del rechazo."
  Y la solicitud permanece pendiente
  Y al escribir "Necesito dos baristas el jueves en la tarde" y confirmar,
    la solicitud pasa a estado "Rechazada por el administrador"
  Y ambos horarios permanecen sin cambios
  Y las dos empleadas pueden leer ese motivo
```

#### CA-12.5 Revalidación en el momento de aprobar

```gherkin
  Dado que después de la aceptación se asignó a "Ana Pérez" otro turno
    el "2026-09-18"
  Cuando presiono el botón "Aprobar"
  Entonces el sistema muestra "No se puede aplicar el cambio: Ana Pérez
    ya tiene un turno asignado el 18/09/2026."
  Y el intercambio no se aplica
  Y la solicitud permanece pendiente para que la resuelva
```

#### CA-12.6 Solicitud vencida y solicitud ya resuelta

```gherkin
  Dado que el turno del "2026-09-17" ya inició sin que yo resolviera
    la solicitud
  Cuando abro su detalle
  Entonces la solicitud aparece en estado "Vencida"
  Y no se ofrecen las opciones "Aprobar" ni "Rechazar"
  Y una solicitud ya resuelta tampoco ofrece opción de reabrirla
```

#### CA-12.7 Alcance por sucursal y conflicto de interés

```gherkin
  Dado que existen solicitudes en "Centro", "Norte" y "Sur"
  Cuando abro la bandeja
  Entonces solo veo solicitudes de turnos de la sucursal "Centro"
  Y si yo misma soy parte de una solicitud, esa solicitud no ofrece
    las opciones "Aprobar" ni "Rechazar" para mí
  Y el "Administrador General" puede resolver las de las tres sucursales
```

### RNF aplicables

- RNF-01 (Rendimiento): la bandeja de solicitudes pendientes se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): solo el Administrador de Sucursal autenticado aprueba o rechaza, y únicamente en su sucursal.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): la solicitud muestra con claridad los dos turnos involucrados, con su fecha y su hora.

### Dependencias

- Requiere la solicitud de cambio de turno del empleado y la aceptación del compañero: es la continuación de ese flujo.
- Requiere las mismas validaciones de conflicto que se aplican al asignar turnos, para poder revalidar al aprobar.
- Requiere los turnos asignados y publicados y la identidad del supervisor con su sucursal.
- Requiere un canal de aviso a los dos empleados.

### Riesgos

- Alto. Si el supervisor no resuelve a tiempo, la solicitud vence y los empleados vuelven al acuerdo informal, que es el problema original. Mitigación: orden por proximidad del turno, indicador de próximas a vencer y visibilidad en la pantalla principal.
- Alto. La aprobación recae en una sola persona por sucursal. Si está de descanso o de vacaciones, las solicitudes se acumulan y vencen. Mitigación: definir quién decide en su ausencia antes de construir la historia.
- Medio. Un intercambio válido al momento de la aceptación puede dejar de serlo días después. Mitigación: la revalidación obligatoria al aprobar, que ya está en las reglas.
- Medio. Rechazos frecuentes sin explicación clara desincentivan el uso del canal formal. Mitigación: motivo obligatorio y visible para ambos empleados.
- Bajo. Un supervisor podría aprobar una solicitud en la que es parte interesada. Mitigación: la regla que se lo impide y la traza de cada decisión.

### Datos de prueba

- Carla Ríos, Administradora de Sucursal de Centro, con sesión iniciada.
- Solicitud de Ana Pérez sobre su turno "Cierre" del 17/09/2026, contra el turno "Apertura" del 18/09/2026 de Sofía Ramos, ya aceptada por ella.
- Solicitud aún sin respuesta del compañero, que no debe aparecer en la bandeja.
- Turno adicional asignado a Ana Pérez el 18/09/2026, para forzar el fallo de revalidación.
- Solicitud cuyo turno más cercano ya inició, para el estado vencida.
- Solicitud en la que la propia Carla Ríos es parte, para el conflicto de interés.
- Solicitudes de las sucursales Norte y Sur, para la validación de alcance.

### Evaluación DoR

**Veredicto: lista con reserva.** El flujo quedó confirmado —el empleado solicita, el compañero acepta y el Administrador de Sucursal aprueba— y la historia tiene actor, valor medible, alcance delimitado, siete criterios verificables, requisitos no funcionales y datos de prueba.

- Reserva: definir quién aprueba cuando el Administrador de Sucursal no está disponible, porque de eso depende que las solicitudes no venzan por ausencia.
- Reserva: retirar de la solicitud del empleado el paso de aprobación que allí se había incluido, para que este alcance quede en un solo lugar.
- No es independiente: continúa el flujo que inicia el empleado y comparte sus estados y validaciones.

---

## 4.3 HU-13 — Reportar novedades de empleado

**HU-13**

### Épica

Épica 4 — Gestión de novedades

### Nombre HU

Reportar novedades de empleado

### Prioridad

Should have. Completa lo que el empleado no reportó, pero depende de que el circuito de novedades ya funcione.

### Actor

Administrador de Sucursal sobre los empleados de su sucursal. Administrador General sobre las tres.

### Historia de usuario

**Como** Administrador de Sucursal de Grano Dorado, **quiero** registrar una novedad sobre el turno de un empleado de mi sucursal cuando él no la reportó, indicando qué ocurrió y el horario real, **para** mantener actualizado el estado de la operación.

### Contexto

Lo que el empleado no reporta simplemente no existe para el sistema, y el vacío más probable es justamente el de quien no se presentó: nadie reporta su propia ausencia. Esta historia le da al supervisor la forma de dejar constancia de lo que él observó. Su punto delicado es el mismo del control de asistencia: el supervisor completa lo que falta, pero no reescribe lo que el empleado ya dijo, porque entonces el registro dejaría de ser confiable para ambas partes.

### Valor esperado

- Evita que las novedades que nadie reporta, en especial las ausencias, queden fuera del registro de la operación.
- Da a la programación de la semana siguiente un panorama completo de lo que ocurrió, no solo de lo que alguien decidió contar.
- Medición: al cierre de cada semana, ningún turno con una diferencia observada frente a lo programado queda sin novedad asociada.
- Medición: menos del 30 % del total de novedades del mes son registradas por el supervisor. Una proporción mayor indica que el reporte del empleado no está funcionando.

### Alcance

- Registro de una novedad sobre el turno de un empleado de mi sucursal, seleccionando el empleado y el turno.
- Tipos disponibles: salida antes de la hora, ampliación del turno, llegada tarde, ausencia y otra novedad con descripción libre.
- Indicación del horario real cuando el tipo lo requiere, con cálculo automático de la diferencia en minutos.
- Descripción obligatoria de lo ocurrido.
- Señalización visible de que la novedad la registró el supervisor, con su nombre y el momento del registro.
- Visibilidad de la novedad para el empleado afectado.
- Corrección de una novedad que yo mismo registré, dentro del plazo permitido.
- Consulta de las novedades que he registrado.

### Fuera de alcance

- Modificar o eliminar una novedad reportada por el empleado.
- Registrar novedades sobre empleados de otra sucursal, salvo para el Administrador General.
- Registrar novedades sobre turnos que aún no han iniciado.
- Cualquier efecto sobre remuneración, horas extra o medidas disciplinarias.
- Modificar el turno programado, la asignación o las marcas de asistencia.
- Reasignar el turno o cubrir la ausencia, que pertenece a la planificación.
- Registrar novedades en bloque para varios empleados a la vez.

### Reglas de negocio

- Solo se registran novedades sobre empleados de la propia sucursal. El Administrador General puede hacerlo en las tres.
- Solo se registran novedades sobre turnos cuya hora de inicio ya pasó.
- El supervisor no puede modificar ni eliminar una novedad reportada por el empleado.
- Si el empleado ya reportó una novedad del mismo tipo sobre ese turno, no se registra otra igual: la observación se agrega como comentario de la existente.
- La descripción es obligatoria en todos los tipos.
- En los tipos que implican un horario real, se aplican las mismas validaciones que rigen el reporte del empleado: la hora debe ser coherente con el turno programado y no superar las 22:00.
- En el tipo ausencia no se indica horario real, porque no hubo jornada trabajada.
- Toda novedad registrada por el supervisor queda identificada como tal, con su nombre y el momento en que la registró.
- Una novedad registrada por el supervisor nace en estado revisada, porque la registra quien revisa.
- El plazo para registrar una novedad es de 48 horas desde el fin del turno.
- El supervisor puede corregir una novedad que él mismo registró dentro de ese plazo, y cada corrección queda en el historial.
- El empleado afectado puede consultar la novedad y el nombre de quien la registró.
- Registrar una novedad no altera el turno programado ni las marcas de asistencia.

### Supuestos

- El catálogo de tipos es el mismo del reporte del empleado, más el tipo ausencia, que solo el supervisor puede registrar porque nadie reporta su propia inasistencia. El caso deja la lista abierta.
- El plazo de 48 horas para registrar es una propuesta. El caso no define ninguno.
- La novedad nace revisada. El caso no describe ningún paso posterior de validación.
- El empleado puede consultar la novedad pero no objetarla dentro del sistema. El caso no menciona ningún mecanismo de desacuerdo.
- Una ausencia detectada automáticamente por el control de asistencia y una novedad de tipo ausencia registrada aquí se refieren al mismo hecho. Debe decidirse si se registran una sola vez o si conviven ambos registros.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como "Carla Ríos", Administradora de Sucursal
    de "Centro"
  Y "Ana Pérez" tuvo asignado el turno "Cierre" (14:00 a 22:00)
    del "2026-09-08"
  Y la fecha y hora actuales son "2026-09-09 09:00"
```

#### CA-13.1 Registro de una novedad que el empleado no reportó

```gherkin
  Cuando selecciono a "Ana Pérez" y su turno del "2026-09-08"
  Y elijo el tipo "Salida antes de la hora" con la hora real "20:30"
  Y escribo la descripción "Se retiró antes, avisó al cierre de caja"
  Y presiono el botón "Guardar"
  Entonces el sistema muestra "Novedad registrada correctamente."
  Y la novedad muestra la diferencia "90 minutos menos que el turno
    programado"
  Y queda señalada como "Registrada por Carla Ríos el 09/09/2026
    a las 09:00"
  Y queda en estado "Revisada"
  Y "Ana Pérez" puede consultarla en su lista de novedades
```

#### CA-13.2 Registro de una ausencia

```gherkin
  Dado que "Diego Salas" no se presentó a su turno del "2026-09-08"
  Cuando registro una novedad de tipo "Ausencia" con la descripción
    "No se presentó ni avisó"
  Entonces el sistema no solicita ninguna hora real
  Y la novedad queda registrada y asociada a ese turno
  Y el turno programado de "Diego Salas" no se modifica
```

#### CA-13.3 No se altera lo que reportó el empleado

```gherkin
  Dado que "Ana Pérez" ya reportó una novedad de tipo "Salida antes
    de la hora" sobre ese mismo turno
  Cuando intento registrar otra novedad del mismo tipo sobre ese turno
  Entonces el sistema muestra "El empleado ya reportó una novedad de
    este tipo para este turno. Puedes agregar un comentario."
  Y no se crea una segunda novedad
  Y al abrir la novedad de la empleada, su contenido se muestra en modo
    de solo lectura
```

#### CA-13.4 Campos obligatorios y turnos futuros

```gherkin
  Cuando registro una novedad sin escribir la descripción
  Entonces el sistema muestra "La descripción es requerida."
  Y al intentar registrar una novedad sobre un turno programado para
    mañana el sistema muestra "Este turno todavía no ha iniciado."
  Y en ninguno de los dos casos se crea una novedad
```

#### Esquema del escenario: CA-13.5 Coherencia del horario real

```gherkin
  Dado que el turno programado fue de "14:00" a "22:00"
  Cuando registro el tipo "<tipo>" con la hora real "<hora>"
  Entonces el sistema muestra "<mensaje>" y no se crea la novedad
  Ejemplos:

    | Tipo | Hora | Mensaje |
    | Salida antes de la hora | 22:30 | La hora debe ser anterior al fin del turno |
    | Salida antes de la hora | 13:00 | La hora debe ser posterior al inicio del turno |
    | Ampliación del turno | 23:00 | La hora no puede superar las 22:00 |
```

#### CA-13.6 Plazo y alcance por sucursal

```gherkin
  Dado que el turno de "Ana Pérez" del "2026-09-06" terminó hace 3 días
  Cuando intento registrar una novedad sobre ese turno
  Entonces el sistema muestra "El plazo de 48 horas para registrar
    novedades de este turno venció."
  Y al abrir el selector de empleados solo aparecen empleados de "Centro"
  Y el "Administrador General" puede seleccionar empleados de las tres
    sucursales
```

### RNF aplicables

- RNF-01 (Rendimiento): el turno del empleado sobre el que se registra la novedad se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): solo el Administrador de Sucursal autenticado registra novedades, y únicamente sobre empleados de su sucursal.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): el formulario indica con claridad el empleado, la fecha y la hora del turno al que corresponde la novedad.

### Dependencias

- Requiere el catálogo de tipos de novedad y las validaciones de horario del reporte del empleado, que deben definirse una sola vez.
- Requiere los turnos asignados y publicados, con su horario, para calcular la diferencia.
- Requiere la bandeja de novedades, que es donde se consultan las que aquí se registran.
- Se relaciona con el control de asistencia: una ausencia puede detectarse allí de forma automática y registrarse aquí de forma manual.

### Riesgos

- Alto. Una ausencia puede quedar registrada dos veces, como incumplimiento detectado por el control de asistencia y como novedad registrada aquí. Mitigación: decidir cuál de los dos es el registro válido antes de construir ambas épicas.
- Medio. Si el supervisor registra la mayoría de las novedades, el dato refleja su percepción y no lo que vivió el empleado. Mitigación: medir la proporción y revisar por qué el reporte propio no se usa.
- Medio. El empleado no tiene forma de manifestar desacuerdo con una novedad registrada sobre él. Mitigación: decidir si se habilita un comentario del empleado, hoy fuera del alcance.
- Bajo. El plazo de 48 horas puede quedar corto si el supervisor descansa dos días seguidos. Mitigación: confirmar el plazo y quién puede registrar una vez vencido.

### Datos de prueba

- Carla Ríos, Administradora de Sucursal de Centro, con sesión iniciada.
- Ana Pérez, con turno "Cierre" de 14:00 a 22:00 del 08/09/2026 y salida real a las 20:30.
- Diego Salas, con turno del 08/09/2026 al que no se presentó, para el tipo ausencia.
- Novedad ya reportada por Ana Pérez del mismo tipo y turno, para la validación de duplicado.
- Horas incoherentes para las validaciones: 22:30, 13:00 y 23:00.
- Turno programado para mañana, para la validación de turno futuro.
- Turno del 06/09/2026, terminado hace 3 días, para la validación del plazo.
- Empleados de Norte y Sur, para la validación de alcance.

### Evaluación DoR

**Veredicto: no lista.** La historia tiene actor, valor medible, alcance delimitado, seis criterios verificables, requisitos no funcionales y datos de prueba, pero arrastra dos definiciones que no le pertenecen solo a ella.

- Bloqueante: decidir si una ausencia se registra como incumplimiento del control de asistencia, como novedad, o ambas cosas. Sin esa decisión, el mismo hecho puede quedar duplicado.
- Pendiente: cerrar el catálogo de tipos de novedad, que hoy sigue abierto.
- Pendiente: fijar el plazo para registrar y definir si el empleado puede manifestar desacuerdo.
- No es independiente: comparte catálogo y validaciones con el reporte del empleado y se consulta desde la bandeja de novedades.
