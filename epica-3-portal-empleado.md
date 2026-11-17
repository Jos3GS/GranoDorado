# Épica 3 — Portal de empleado

**Resumen:** Agrupa lo que hace el empleado desde su portal: ver su horario asignado (HU-08), solicitar un cambio de turno (HU-09) y reportar novedades (HU-10).

*Resumen elaborado a partir de las historias de la épica; el documento original no incluye una descripción propia de esta épica.*

**Línea de la épica en el documento original:** HU-08 Ver horario asignado · HU-09 Solicitar cambio de turno · HU-10 Reportar novedades

## Historias de usuario de esta épica

| Sección | Identificador | Nombre |
|---|---|---|
| 3.1 | [HU-08](#31-hu-08--ver-horario-asignado) | Ver horario asignado |
| 3.2 | [HU-09](#32-hu-09--solicitar-cambio-de-turno) | Solicitar cambio de turno |
| 3.3 | [HU-10](#33-hu-10--reportar-novedades) | Reportar novedades |

---

## 3.1 HU-08 — Ver horario asignado

**HU-08**

### Épica

Épica 3 — Portal de empleado

### Nombre HU

Ver horario asignado

### Prioridad

Es la historia que reemplaza el envío de horarios por WhatsApp.

### Actor

Empleado: Cajero, Barista o Cocinero. También el Administrador de Sucursal sobre sus propios turnos.

### Historia de usuario

**Como** empleado de Grano Dorado, **quiero** consultar en cualquier momento mi horario semanal asignado, con la fecha, el turno y la sucursal de cada jornada, **para** organizar mi rutina y presentarme puntualmente sin depender de un mensaje de WhatsApp.

### Contexto

Hoy el empleado recibe su horario un domingo por la noche, dentro de un grupo donde el mensaje queda enterrado bajo otras conversaciones y donde el Excel se lee mal en un teléfono. Las consecuencias que el caso describe (llegar tarde o no presentarse) no son problemas de disciplina sino de acceso a la información. Esta historia traslada el horario a un lugar fijo, consultable a cualquier hora y siempre con la última versión.

### Valor esperado

- Elimina la dependencia del grupo de WhatsApp para saber cuándo se trabaja: el horario deja de perderse porque deja de ser un mensaje.
- Reduce las llegadas tarde y las ausencias causadas por leer mal el horario. Medición: número de ausencias e impuntualidades atribuidas a desconocimiento del turno, comparando las 4 semanas previas y las 4 posteriores a la puesta en producción.
- Descarga al Administrador General de responder consultas individuales sobre horarios, tiempo que hoy se suma a las 8 horas semanales de cuadre.
- Medición: al menos el 80 % de los empleados consulta su horario en la aplicación durante la primera semana de cada mes.

### Alcance

- Pantalla "Mi horario" con la semana en curso, mostrando para cada día la fecha, el nombre del turno, la hora de inicio, la hora de fin y la sucursal.
- Marca explícita de los días sin turno asignado, diferenciando día libre de día pendiente de programar.
- Navegación a la semana siguiente y a las semanas anteriores ya trabajadas.
- Total, de horas asignadas en la semana.
- Indicación de la fecha y hora en que se publicó el horario que se está viendo.
- Aviso visible cuando el horario cambia después de haber sido publicado.
- Uso desde el teléfono, que es el dispositivo con el que hoy se consulta el horario.

### Fuera de alcance

- Ver el horario de otros empleados o la malla completa de la sucursal.
- Modificar el horario propio: cualquier cambio pasa por la solicitud de cambio de turno o por el administrador.
- Solicitar días libres, vacaciones o permisos.
- Marcar entrada y salida, o registrar asistencia.
- Exportar el horario a un calendario externo o a un archivo.
- Consultar el detalle de la remuneración asociada a los turnos.

### Reglas de negocio

- El empleado ve únicamente sus propios turnos; en ningún caso los de otro empleado.
- Solo se muestran los horarios ya publicados por el administrador. Un horario en preparación no es visible para el empleado.
- La semana va de lunes a domingo, que es el ciclo con el que hoy se comunican los horarios.
- Cada jornada muestra el nombre del turno, la hora de inicio, la hora de fin y la sucursal en la que se realiza.
- Los días sin turno se muestran como "Sin turno asignado" y no se dejan en blanco, para que la ausencia de información no se confunda con un error.
- El total semanal suma las horas efectivas de cada turno, descontando los descansos definidos en él.
- Si el horario publicado se modifica, el empleado ve la jornada afectada señalada como modificada, junto con la fecha y hora del cambio.
- El horario es de solo lectura: ninguna acción de esta pantalla altera la programación.
- Un empleado inactivo no accede a la aplicación y, por tanto, tampoco a esta pantalla.

### Supuestos

- El horizonte visible es la semana en curso, la siguiente y el historial de las semanas ya trabajadas. El caso no precisa con cuánta anticipación se publica el horario más allá del envío dominical.
- El empleado consulta el horario desde su teléfono. El caso lo sugiere al describir el uso de WhatsApp, pero no lo afirma.
- La publicación del horario genera un aviso al empleado. El canal de ese aviso no está definido en el caso.
- Todas las sucursales comparten la misma zona horaria.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como la empleada "Ana Pérez", Barista de la
    sucursal "Centro"
  Y la semana del 14 al 20 de septiembre de 2026 está publicada
```

#### CA-08.1 Consulta del horario de la semana en curso

```gherkin
  Cuando abro la pantalla "Mi horario"
  Entonces veo los 7 días de la semana, de "Lunes 14" a "Domingo 20"
  Y el "Lunes 14" muestra "Apertura, 06:00 a 14:00, sucursal Centro"
  Y la pantalla indica "Horario publicado el 07/09/2026 a las 18:40"
  Y la pantalla muestra el total "30 h 00 min asignadas esta semana"
```

#### CA-08.2 Días sin turno asignado

```gherkin
  Dado que no tengo ningún turno el "Miércoles 16"
  Cuando abro la pantalla "Mi horario"
  Entonces el "Miércoles 16" muestra el texto "Sin turno asignado"
  Y esa casilla no aparece vacía
```

#### CA-08.3 Solo veo mi propio horario

```gherkin
  Cuando abro la pantalla "Mi horario"
  Entonces todos los turnos mostrados corresponden a "Ana Pérez"
  Y la pantalla no ofrece ninguna opción para consultar el horario de
    otro empleado ni la malla de la sucursal
```

#### CA-08.4 Una semana sin publicar no es visible

```gherkin
  Dado que la semana del 21 al 27 de septiembre aún no ha sido publicada
  Cuando avanzo a esa semana
  Entonces la pantalla muestra "El horario de esta semana todavía no
    está publicado."
  Y no se muestra ningún turno de esa semana
```

#### CA-08.5 Cambio en un horario ya publicado

```gherkin
  Dado que mi turno del "Jueves 17" cambió de "Apertura" a "Cierre"
    después de la publicación
  Cuando abro la pantalla "Mi horario"
  Entonces el "Jueves 17" muestra "Cierre, 14:00 a 22:00, sucursal Centro"
  Y esa jornada aparece señalada como "Modificado el 09/09/2026 a las 11:15"
```

#### CA-08.6 Consulta de una semana anterior y solo lectura

```gherkin
  Cuando retrocedo a la semana del 7 al 13 de septiembre
  Entonces veo los turnos que trabajé esa semana
  Y en ninguna de las semanas la pantalla ofrece acciones para crear,
    modificar o eliminar un turno
```

#### CA-08.7 Consulta desde el teléfono

```gherkin
  Dado que abro la aplicación en una pantalla de 360 píxeles de ancho
  Cuando abro la pantalla "Mi horario"
  Entonces los 7 días son legibles sin desplazamiento horizontal
  Y la pantalla se despliega completa en 3 segundos o menos
```

### RNF aplicables

- RNF-01 (Rendimiento): el horario de la semana del empleado se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): el empleado autenticado consulta únicamente sus propios turnos.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): la pantalla permite identificar fácilmente la fecha y la hora de cada turno.

### Dependencias

- Requiere el inicio de sesión y la identidad del empleado con su rol y su sucursal.
- Requiere que exista la asignación de turnos y el acto de publicar una semana: sin eso no hay nada que mostrar.
- Requiere el catálogo de turnos con sus horas y descansos para poder calcular el total semanal.

### Riesgos

- Alto. Si el horario se publica tarde o no se publica, el empleado abre la pantalla y no encuentra nada, y el equipo vuelve al grupo de WhatsApp. Mitigación: fecha límite de publicación acordada con el Administrador General y aviso automático cuando una semana próxima sigue sin publicar.
- Medio. La adopción no es automática: el personal está habituado a recibir el horario, no a buscarlo. Mitigación: aviso al publicar y acompañamiento durante las primeras semanas.
- Medio. Un empleado sin teléfono con datos queda fuera del canal. Mitigación: mantener una copia impresa en la sucursal durante la transición.
- Bajo. Un cambio de última hora publicado poco antes del turno puede no ser visto a tiempo. Mitigación: señalar el cambio y avisar al empleado afectado.

### Datos de prueba

- Ana Pérez, Barista de la sucursal Centro, activa, con sesión iniciada.
- Semana del 14 al 20 de septiembre de 2026, publicada el 07/09/2026 a las 18:40.
- Cuatro turnos "Apertura" de 06:00 a 14:00 con descanso de 10:00 a 10:30, que suman 30 h 00 min efectivas.
- Miércoles 16 sin turno asignado.
- Jueves 17 modificado de "Apertura" a "Cierre" el 09/09/2026 a las 11:15.
- Semana del 21 al 27 de septiembre sin publicar.
- Semana del 7 al 13 de septiembre ya trabajada, para la consulta histórica.

### Evaluación DoR

**Veredicto: lista con reserva.** La historia tiene actor, valor medible, alcance delimitado, reglas trazables al caso, siete criterios verificables, requisitos no funcionales y datos de prueba.

- Reserva: definir con cuánta anticipación se publica el horario y por qué canal se avisa al empleado. Sin esa definición la historia se puede construir, pero su adopción queda en riesgo.
- No es independiente: no puede entrar a sprint antes de que exista la asignación y la publicación de turnos.

---

## 3.2 HU-09 — Solicitar cambio de turno

**HU-09**

### Épica

Épica 3 — Portal de empleado

### Nombre HU

Solicitar cambio de turno

### Prioridad

Es la historia que ataca la falta de flexibilidad descrita en el caso.

### Actor

Empleado que solicita, empleado que recibe la solicitud y Administrador de Sucursal como aprobador.

### Historia de usuario

**Como** empleado de Grano Dorado, **quiero** solicitar el intercambio de uno de mis turnos con el de un compañero y que la solicitud quede registrada, aceptada por él y aprobada por el Administrador de mi sucursal, **para** resolver una eventualidad o tomar un turno que me conviene más, sin que el cambio dependa de acuerdos informales.

### Contexto

El caso describe este proceso como caótico: cuando un empleado se enferma o necesita cambiar un turno con un compañero, el arreglo ocurre entre ellos y el administrador se entera a último minuto. El problema no es que los empleados se organicen entre sí, sino que el acuerdo vive fuera del sistema: nadie sabe quién cubre realmente el turno hasta que llega la hora. Esta historia mantiene la iniciativa en el empleado, pero hace que el acuerdo sea visible y quede aprobado antes de que el turno ocurra.

### Valor esperado

- Convierte un acuerdo verbal en un registro con solicitante, compañero, turnos involucrados, fecha y aprobación.
- El administrador deja de enterarse a último minuto: conoce cada intercambio antes de que se ejecute.
- Medición: al menos el 90 % de los intercambios de turno del mes quedan registrados en el sistema, comparado con los acuerdos informales reportados por los administradores de sucursal.
- Medición: ningún turno queda sin cubrir por un intercambio que el administrador desconocía.

### Alcance

- Solicitud de intercambio desde uno de mis turnos publicados, eligiendo el turno del compañero con el que quiero intercambiar.
- Lista de turnos elegibles para el intercambio, filtrada por las reglas de cobertura y disponibilidad.
- Campo de motivo, obligatorio y de texto libre.
- Aceptación o rechazo por parte del compañero, con notificación al solicitante.
- Aprobación o rechazo por parte del Administrador de Sucursal, con motivo obligatorio en el rechazo.
- Aplicación automática del intercambio en el horario de ambos empleados una vez aprobado.
- Consulta del estado de mis solicitudes, enviadas y recibidas.
- Cancelación de una solicitud propia mientras no haya sido aprobada.
- Registro de toda la secuencia: quién solicitó, quién aceptó, quién aprobó y cuándo.

### Fuera de alcance

- Ceder un turno sin recibir otro a cambio, es decir, dejarlo libre para que alguien lo tome.
- Intercambios entre empleados de sucursales distintas.
- Reemplazo por enfermedad o ausencia imprevista gestionado por el administrador, que no nace de una solicitud del empleado.
- Intercambio de más de un turno en una sola solicitud.
- Negociación o mensajería entre empleados dentro del sistema.
- Efectos del intercambio sobre remuneración, recargos u horas extra.

### Reglas de negocio

- Solo se puede solicitar el intercambio de un turno propio que esté publicado y cuya fecha y hora de inicio aún no hayan pasado.
- El compañero debe pertenecer a la misma sucursal y desempeñar el mismo perfil, para que la cobertura de la jornada no se altere con el intercambio.
- Ambos empleados deben estar activos en el momento de la solicitud y en el momento de la aprobación.
- Ninguno de los dos puede quedar con dos turnos el mismo día ni con turnos que se solapen.
- El intercambio debe respetar el descanso mínimo entre turnos consecutivos y el máximo de horas semanales que aplique a cada empleado.
- El sistema no ofrece como elegible ningún turno que incumpla alguna de las reglas anteriores.
- La solicitud recorre los estados solicitada, aceptada por el compañero, aprobada y aplicada. Puede terminar en rechazada por el compañero, rechazada por el administrador, cancelada por el solicitante o vencida.
- La solicitud pasa primero por el compañero y solo después llega al Administrador de Sucursal: sin la aceptación del compañero la solicitud no se somete a aprobación.
- La aprobación corresponde al Administrador de Sucursal de la sucursal en la que están los turnos involucrados.
- El intercambio solo se refleja en el horario cuando el Administrador de Sucursal lo aprueba: la aceptación del compañero por sí sola no cambia nada.
- Una solicitud que no ha sido resuelta antes del inicio del turno más próximo queda vencida y no se puede aprobar.
- El rechazo, tanto del compañero como del Administrador de Sucursal, exige un motivo escrito que el solicitante puede leer.
- Un turno con una solicitud de intercambio en curso no admite una segunda solicitud simultánea.
- Cada cambio de estado se registra con el usuario que lo produjo y la fecha y hora.

### Supuestos

- El compañero debe tener el mismo perfil, por ejemplo, un barista con otro barista. El caso no lo establece; se asume para no alterar la cobertura por rol.
- La antelación mínima para solicitar un intercambio es de 24 horas antes del inicio del turno más próximo. El caso no define ningún plazo.
- Un empleado puede solicitar un número ilimitado de intercambios. El caso no menciona límites.
- Los intercambios son siempre entre dos turnos ya asignados, no entre un turno y un día libre.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como la empleada "Ana Pérez", Barista de "Centro"
  Y tengo asignado el turno "Cierre" (14:00 a 22:00) el "2026-09-17"
  Y "Sofía Ramos" es Barista de "Centro" y tiene asignado el turno
    "Apertura" (06:00 a 14:00) el "2026-09-18"
```

#### CA-09.1 Solicitud de intercambio y aceptación del compañero

```gherkin
  Cuando selecciono mi turno del "2026-09-17" y presiono "Solicitar cambio"
  Y elijo el turno de "Sofía Ramos" del "2026-09-18"
  Y escribo el motivo "Tengo una cita médica el jueves en la tarde"
  Y presiono el botón "Enviar solicitud"
  Entonces el sistema muestra "Solicitud enviada a Sofía Ramos."
  Y la solicitud queda en estado "Solicitada"
  Y "Sofía Ramos" recibe la solicitud en su lista de solicitudes recibidas
  Y cuando ella presiona "Aceptar" la solicitud pasa a estado
    "Aceptada por el compañero"
  Y mi horario y el de ella todavía no cambian
```

#### CA-09.2 Aprobación del administrador y aplicación del intercambio

```gherkin
  Dado que la solicitud está en estado "Aceptada por el compañero"
  Cuando el "Administrador de Sucursal" de "Centro" presiona "Aprobar"
  Entonces la solicitud pasa a estado "Aplicada"
  Y mi horario del "2026-09-18" muestra "Apertura, 06:00 a 14:00"
  Y el horario de "Sofía Ramos" del "2026-09-17" muestra
    "Cierre, 14:00 a 22:00"
  Y ninguna de las dos conserva su turno original
  Y ambas recibimos el aviso del cambio
```

#### CA-09.3 Rechazo con motivo

```gherkin
  Dado que la solicitud está en estado "Solicitada"
  Cuando "Sofía Ramos" presiona "Rechazar" sin escribir un motivo
  Entonces el sistema muestra "Indica el motivo del rechazo."
  Y la solicitud permanece en estado "Solicitada"
  Y al escribir el motivo y confirmar, la solicitud pasa a estado
    "Rechazada por el compañero"
  Y yo puedo leer ese motivo en mi lista de solicitudes enviadas
  Y mi horario no cambia
```

#### CA-09.4 Turnos no elegibles para el intercambio

```gherkin
  Dado que "Carlos Ruiz" es Cajero de "Norte"
  Y "Diego Salas" es Cocinero de "Centro"
  Y "Marta León" es Barista de "Centro" pero ya tiene un turno el "2026-09-17"
  Cuando abro la lista de turnos con los que puedo intercambiar
  Entonces la lista no contiene ningún turno de "Carlos Ruiz"
  Y no contiene ningún turno de "Diego Salas"
  Y no contiene el turno de "Marta León" del "2026-09-18"
```

#### CA-09.5 Intercambio que rompe el descanso mínimo

```gherkin
  Dado que aceptar el turno de "Sofía Ramos" me dejaría con 8 horas entre
    el fin de un turno y el inicio del siguiente
  Cuando intento seleccionar ese turno
  Entonces el sistema muestra "Este cambio te dejaría 8 horas de descanso
    entre turnos. El mínimo permitido es 12 horas."
  Y no se crea ninguna solicitud
```

#### CA-09.6 Solicitud fuera de plazo y solicitud vencida

```gherkin
  Dado que mi turno del "2026-09-17" inicia en 10 horas
  Cuando intento solicitar un intercambio sobre ese turno
  Entonces el sistema muestra "Los cambios deben solicitarse con al menos
    24 horas de anticipación."
  Y no se crea ninguna solicitud
  Y una solicitud existente que llegue al inicio del turno sin aprobación
    pasa a estado "Vencida" y deja de poder aprobarse
```

#### CA-09.7 Cancelación por el solicitante y bloqueo de solicitudes duplicadas

```gherkin
  Dado que mi solicitud está en estado "Solicitada"
  Cuando presiono "Cancelar solicitud"
  Entonces la solicitud pasa a estado "Cancelada"
  Y mientras estaba en curso, al intentar crear una segunda solicitud sobre
    el mismo turno el sistema mostraba "Este turno ya tiene una solicitud
    de cambio en curso."
```

### RNF aplicables

- RNF-01 (Rendimiento): la lista de turnos propios y de turnos elegibles para el intercambio se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): el empleado autenticado solicita cambios únicamente sobre sus propios turnos.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): la solicitud muestra con claridad la fecha y la hora del turno que se entrega y del que se recibe.

### Dependencias

- Requiere que el empleado pueda ver su horario, que es donde nace la solicitud.
- Requiere la asignación y publicación de turnos, y el catálogo de turnos con sus horas.
- Requiere las mismas validaciones de conflicto que usa el administrador al asignar: descanso mínimo, máximo semanal y no duplicar turnos el mismo día.
- Requiere un canal de aviso al compañero y al Administrador de Sucursal.

### Riesgos

- Si la aprobación del Administrador de Sucursal tarda, la solicitud vence y el empleado vuelve al arreglo informal, que es exactamente el problema que la historia busca eliminar. Mitigación: aviso inmediato al administrador y visibilidad de las solicitudes pendientes con su plazo.
- La aprobación recae en una sola persona por sucursal. Si el Administrador de Sucursal está de descanso o de vacaciones, las solicitudes de esa sucursal se acumulan. Mitigación: definir quién aprueba en su ausencia.
- Las reglas de elegibilidad dependen de límites de jornada que el caso no define. Si se fijan mal, el sistema puede rechazar intercambios legítimos y el equipo dejará de usarlo. Mitigación: parámetros configurables y revisión tras las primeras 4 semanas.
- Exigir el mismo perfil puede dejar sin opciones a un cocinero que sea el único de su turno en la sucursal. Mitigación: confirmar la regla con el Administrador General antes de construirla.
- Un intercambio aprobado y luego revertido por el administrador genera confusión sobre quién cubre el turno. Mitigación: una vez aplicado, revertir exige una nueva solicitud, no deshacer la anterior.
- La antelación de 24 horas puede ser demasiado rígida para una eventualidad súbita. Mitigación: confirmar el plazo y prever que el administrador pueda asignar directamente en esos casos.

### Datos de prueba

- Ana Pérez, Barista de Centro, con turno "Cierre" de 14:00 a 22:00 el 17/09/2026.
- Sofía Ramos, Barista de Centro, con turno "Apertura" de 06:00 a 14:00 el 18/09/2026.
- Marta León, Barista de Centro, con un turno ya asignado el 17/09/2026, para el caso no elegible.
- Diego Salas, Cocinero de Centro, perfil distinto, para el caso no elegible.
- Carlos Ruiz, Cajero de Norte, otra sucursal, para el caso no elegible.
- Carla Ríos, Administradora de Sucursal de Centro, como aprobadora.
- Turno que inicia en 10 horas, para la validación de antelación.
- Combinación que deja 8 horas entre turnos, para la validación de descanso mínimo.

### Evaluación DoR

**Veredicto: no lista.** La historia tiene actor, valor medible, alcance delimitado, siete criterios verificables y datos de prueba. El flujo quedó confirmado —el empleado solicita, el compañero acepta y el Administrador de Sucursal aprueba— pero dos decisiones siguen abiertas.

- Pendiente: definir si el compañero debe tener el mismo perfil y si el intercambio se limita a la misma sucursal.
- Pendiente: fijar la antelación mínima para solicitar, el momento en que una solicitud vence y quién aprueba cuando el Administrador de Sucursal no está disponible.
- No es independiente: depende de la consulta del horario y de la asignación y publicación de turnos.

---

## 3.3 HU-10 — Reportar novedades

**HU-10**

### Épica

Épica 3 — Portal de empleado

### Nombre HU

Reportar novedades

### Prioridad

Aporta información que hoy no se registra, pero no bloquea la operación diaria.

### Actor

Empleado que reporta. Administrador de Sucursal y Administrador General como revisores.

### Historia de usuario

**Como** empleado de Grano Dorado, **quiero** reportar las novedades ocurridas durante mi turno, como una salida antes de la hora o una ampliación de la jornada, indicando el horario real y el motivo, **para** que mis superiores las conozcan y las tengan en cuenta.

### Contexto

Hoy el horario programado y el horario realmente trabajado son dos cosas distintas, y solo la primera queda escrita. Cuando alguien sale antes o se queda más tiempo, el hecho circula de palabra o por WhatsApp y desaparece. Sin ese registro, el administrador programa la semana siguiente sobre una información incompleta y no tiene forma de reconocer el tiempo adicional que alguien trabajó. Esta historia deja que lo reporte quien lo vivió, en el momento en que ocurrió.

### Valor esperado

- Deja constancia escrita de la diferencia entre el turno programado y el turno trabajado, que hoy no existe en ningún soporte.
- Da al administrador información real sobre lo que ocurre en cada sucursal, sin tener que reconstruirlo de memoria.
- Medición: al menos el 80 % de las novedades reportadas quedan revisadas por un administrador dentro de las 72 horas siguientes.
- Medición: número de novedades registradas en el sistema frente a las que hoy se comunican informalmente, medido durante el primer mes.

### Alcance

- Reporte de una novedad asociada a un turno propio, seleccionando el tipo, indicando el horario real y escribiendo una descripción.
- Tipos disponibles: salida antes de la hora, ampliación del turno, llegada tarde y otra novedad con descripción libre.
- Consulta de mis novedades reportadas con su estado.
- Bandeja del administrador con las novedades de su sucursal, filtrables por fecha, empleado, tipo y estado.
- Revisión por el administrador, que registra la novedad como revisada y puede dejar un comentario.
- Registro de quién reportó, cuándo, quién revisó y cuándo.

### Fuera de alcance

- Cálculo de nómina, horas extra, recargos o cualquier efecto económico de la novedad.
- Marcación automática de entrada y salida: el horario real lo declara el empleado.
- Modificación del turno programado a partir de la novedad: el turno original se conserva.
- Adjuntar archivos, fotografías o soportes.
- Reporte de novedades sobre turnos de otros empleados.
- Solicitudes de permiso o de incapacidad, que son un trámite distinto y previo al turno.
- Aprobación o rechazo de la novedad con efectos vinculantes: el administrador la revisa, no la autoriza.
- Cálculo automático de la diferencia en minutos entre el horario programado y el horario reportado.

### Reglas de negocio

- Una novedad siempre se asocia a un turno propio del empleado que la reporta.
- Solo se pueden reportar novedades sobre turnos cuya hora de inicio ya pasó.
- El reporte debe hacerse dentro de las 48 horas siguientes al fin del turno; después el turno deja de admitir novedades.
- El tipo de novedad y la descripción son obligatorios.
- En una salida antes de la hora, la hora real de salida debe ser posterior al inicio del turno y anterior a su fin.
- En una ampliación del turno, la hora real de salida debe ser posterior al fin del turno y no puede exceder las 22:00, que es el cierre de la operación.
- En una llegada tarde, la hora real de entrada debe ser posterior al inicio del turno y anterior a su fin.
- Un turno admite varias novedades de tipos distintos, pero no dos del mismo tipo.
- La novedad recorre los estados reportada y revisada. Una vez revisada, el empleado no puede modificarla ni eliminarla.
- El empleado puede corregir o eliminar su novedad mientras esté en estado reportada.
- El administrador ve únicamente las novedades de los empleados de su sucursal; el Administrador General ve las de las tres.
- La novedad no altera el turno programado ni el horario publicado: queda registrada junto a él.
- Cada reporte y cada revisión se registran con el usuario y la fecha y hora.

### Supuestos

- Los tipos de novedad son salida antes de la hora, ampliación del turno, llegada tarde y otra. El caso menciona los dos primeros y deja la lista abierta con un etcétera, por lo que el catálogo debería ser configurable.
- El plazo de 48 horas para reportar es una propuesta. El caso no indica ninguno.
- El administrador revisa la novedad, pero no la aprueba ni la rechaza. El caso dice que las novedades deben ser tenidas en cuenta por los superiores, sin precisar qué hacen con ellas.
- La novedad no tiene efecto sobre la remuneración. El caso no menciona nómina en ningún momento.
- El horario real lo declara el empleado, porque el caso no describe ningún mecanismo de marcación de entrada y salida.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como la empleada "Ana Pérez", Barista de "Centro"
  Y trabajé el turno "Cierre" (14:00 a 22:00) el "2026-09-17"
  Y la fecha actual es "2026-09-18"
```

#### CA-10.1 Reporte de una salida antes de la hora

```gherkin
  Cuando selecciono mi turno del "2026-09-17" y presiono "Reportar novedad"
  Y elijo el tipo "Salida antes de la hora"
  Y indico la hora real de salida "20:30"
  Y escribo la descripción "Me retiré antes por una urgencia familiar"
  Y presiono el botón "Enviar"
  Entonces el sistema muestra "Novedad reportada correctamente."
  Y la novedad queda en estado "Reportada"
  Y muestra la diferencia "90 minutos menos que el turno programado"
  Y el turno del "2026-09-17" sigue registrado como "Cierre, 14:00 a 22:00"
```

#### CA-10.2 Reporte de una ampliación del turno

```gherkin
  Dado que trabajé el turno "Apertura" (06:00 a 14:00) el "2026-09-17"
  Cuando reporto el tipo "Ampliación del turno" con hora real de salida
    "15:30" y la descripción "Cubrí hasta que llegó el relevo"
  Entonces la novedad queda en estado "Reportada"
```

#### Esquema del escenario: CA-10.3 Validación del horario reportado

```gherkin
  Dado que mi turno programado fue de "14:00" a "22:00"
  Cuando reporto el tipo "<tipo>" con la hora real "<hora>"
  Entonces el sistema muestra "<mensaje>" y no se crea ninguna novedad
  Ejemplos:

    | Tipo | Hora | Mensaje |
    | Salida antes de la hora | 22:30 | La hora de salida debe ser anterior al final del turno |
    | Salida antes de la hora | 13:00 | La hora de salida debe ser posterior al inicio del turno |
    | Ampliación del turno | 21:00 | La hora de salida debe ser posterior al final turno |
    | Ampliación del turno | 23:00 | La hora de salida no puede superar las 22:00 |
```

#### CA-10.4 Campos obligatorios y plazo para reportar

```gherkin
  Cuando envío una novedad sin seleccionar el tipo
  Entonces el sistema muestra "Selecciona el tipo de novedad."
  Y cuando la envío sin descripción muestra "La descripción es requerida."
  Y al intentar reportar una novedad sobre un turno que terminó hace
    3 días el sistema muestra "El plazo para reportar novedades de este
    turno venció. Comunícate con tu administrador."
  Y en ninguno de los casos se crea una novedad
```

#### CA-10.5 Revisión por el administrador

```gherkin
  Dado que mi novedad está en estado "Reportada"
  Cuando la "Administradora de Sucursal" de "Centro" abre su bandeja
  Entonces ve la novedad con mi nombre, la fecha del turno, el tipo y
    la diferencia en minutos
  Y al presionar "Marcar como revisada" y escribir un comentario,
    la novedad pasa a estado "Revisada"
  Y yo puedo leer ese comentario en mi lista de novedades
  Y desde ese momento no puedo modificar ni eliminar la novedad
```

#### CA-10.6 Alcance de la bandeja del administrador

```gherkin
  Dado que existen novedades de empleados de "Centro", "Norte" y "Sur"
  Cuando la "Administradora de Sucursal" de "Centro" abre su bandeja
  Entonces solo ve novedades de empleados de "Centro"
  Y cuando el "Administrador General" abre la bandeja, ve las de las
    tres sucursales
  Y como empleada solo veo mis propias novedades
```

### RNF aplicables

- RNF-01 (Rendimiento): el turno sobre el que se reporta la novedad se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): el empleado autenticado reporta novedades únicamente de sus propios turnos.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): el formulario indica con claridad la fecha y la hora del turno al que corresponde la novedad.

### Dependencias

- Requiere que el empleado pueda ver sus turnos, porque la novedad nace desde uno de ellos.
- Requiere la asignación de turnos con horas de inicio y fin, que son la referencia contra la que se calcula la diferencia.
- Requiere la identidad del empleado con su sucursal, para dirigir la novedad al administrador correcto.
- Requiere definir el catálogo de tipos de novedad antes de construir el formulario.

### Riesgos

- El caso pide que las novedades sean tenidas en cuenta, pero no dice para qué. Si nadie las usa al programar o al reconocer tiempo adicional, los empleados dejarán de reportarlas. Mitigación: acordar con el Administrador General qué decisión concreta alimenta esta información antes de construir la historia.
- El horario real lo declara el propio empleado, sin verificación. Mitigación: la revisión del administrador y el registro del comentario dejan traza de las discrepancias.
- Un catálogo de tipos incompleto empuja a usar la opción "Otra" para todo y hace inútil la clasificación. Mitigación: catálogo configurable y revisión de las novedades de tipo "Otra" al cabo de un mes.
- Reportar al final de un turno de cierre, a las 22:00, deja poco margen dentro de la franja de operación. Mitigación: el plazo de 48 horas y la disponibilidad del sistema fuera de esa franja para esta función.

### Datos de prueba

- Ana Pérez, Barista de Centro, con turno "Cierre" de 14:00 a 22:00 el 17/09/2026.
- Turno "Apertura" de 06:00 a 14:00 el 17/09/2026, para el caso de ampliación.
- Salida antes de la hora a las 20:30, que arroja 90 minutos menos.
- Ampliación hasta las 15:30, que arroja 90 minutos más.
- Horas inválidas para las validaciones: 22:30, 13:00, 21:00 y 23:00.
- Turno terminado hace 3 días, para la validación del plazo.
- Carla Ríos, Administradora de Sucursal de Centro, y novedades de empleados de Norte y Sur, para el alcance de la bandeja.

### Evaluación DoR

**Veredicto: no lista.** La historia tiene actor, alcance delimitado, seis criterios verificables, requisitos no funcionales y datos de prueba, pero su propósito final sigue sin definirse.

- Pendiente: definir qué se hace con una novedad revisada. Sin una decisión concreta que dependa de ella, el valor de la historia no se puede medir ni sostener en el tiempo.
- Pendiente: cerrar el catálogo de tipos de novedad, hoy abierto con un etcétera.
- Pendiente: fijar el plazo para reportar y confirmar que el administrador revisa sin aprobar.
- No es independiente: depende de la consulta del horario y de la asignación de turnos.
