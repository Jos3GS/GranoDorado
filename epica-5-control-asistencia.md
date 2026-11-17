# Épica 5 — Control de asistencia

**Resumen:** Debemos poder registrar la asistencia al turno.

*Tomado del campo «Descripción» del Marco de la Épica 5 (sección 5.1 del documento original).*

**Línea de la épica en el documento original:** HU-14 Marcar asistencia · HU-15 Marcar asistencia (supervisor) · HU-16 Notificaciones de incumplimientos

## Historias de usuario de esta épica

| Sección | Identificador | Nombre |
|---|---|---|
| 5.1 | — | [Marco de la Épica 5](#51-marco-de-la-épica-5) (contexto de la épica, no es una historia) |
| 5.2 | [HU-14](#52-hu-14--marcar-asistencia-al-turno) | Marcar asistencia al turno |
| 5.3 | [HU-15](#53-hu-15--marcar-asistencia-al-turno-registro-por-el-supervisor) | Marcar asistencia al turno (registro por el supervisor) |
| 5.4 | [HU-16](#54-hu-16--notificaciones-de-incumplimientos) | Notificaciones de incumplimientos |

---

## 5.1 Marco de la Épica 5

**ÉPICA 5 — Control de Asistencia**

### Descripción

Debemos poder registrar la asistencia al turno.

### Situación actual

Grano Dorado tiene 3 sucursales y 45 empleados, con atención de lunes a domingo de 06:00 a 22:00. Hoy no existe ningún registro de asistencia: lo único escrito es el horario programado en Excel. Cuando alguien llega tarde o no se presenta, el hecho se conoce porque un compañero lo comenta o porque el mostrador queda sin atender, y el administrador se entera cuando ya no puede reaccionar.

### Qué resuelve la épica

- Introduce el primer registro de lo que realmente ocurre en el turno, frente a lo que estaba programado.
- Da al supervisor una forma de completar un registro cuando el empleado olvidó hacerlo, sin borrar lo que el empleado sí marcó.
- Convierte la ausencia en una señal que llega a tiempo, en lugar de un hecho que se descubre tarde.

### Terminología

- Supervisor equivale al Administrador de Sucursal, con alcance limitado a su propia sucursal. El Administrador General tiene el mismo alcance sobre las tres.
- Marca es cada registro individual de tiempo: inicio de turno, salida a descanso, regreso de descanso y fin de turno.

---

## 5.2 HU-14 — Marcar asistencia al turno

**HU-14**

### Épica

Épica 5 — Control de Asistencia

### Nombre HU

Marcar asistencia al turno

### Prioridad

Sin esta historia no hay asistencia que registrar, completar ni vigilar.

### Actor

Empleado: Cajero, Barista o Cocinero.

Administrador de Sucursal sobre sus propios turnos.

### Historia de usuario

**Como** empleado de Grano Dorado, **quiero** registrar el inicio y el fin de mi turno, y también mis salidas y regresos de descanso, **para** llevar el control del tiempo que efectivamente dedico a mis labores.

### Contexto

Hoy la compañía no tiene forma de llevar el control del tiempo que un empleado pasa efectivamente realizando sus labores, no puede conocer su hora real de llegada y salida, así como los tiempos de descanso que toma, al momento se basan en la confianza en que un empleado informe la verdad sobre su hora de llegada o salida.

### Valor esperado

- Crea el registro de lo realmente trabajado, que hoy no existe en ningún soporte de la cadena.
- Da al empleado constancia verificable de su hora de entrada, sus descansos y su hora de salida.
- Medición: al menos el 90 % de los turnos programados tienen marca de inicio y marca de fin registradas por el propio empleado, medido al cabo del primer mes.
- Medición: la diferencia entre horas programadas y horas efectivamente trabajadas se puede calcular por empleado y por sucursal, algo hoy imposible.

### Alcance

- Pantalla con el turno del día y un único botón que ofrece la marca que corresponde según el momento: iniciar turno, salir a descanso, regresar de descanso o finalizar turno.
- Registro de la fecha y la hora de cada marca, tomadas del servidor.
- Visualización del tiempo transcurrido desde el inicio del turno y del tiempo de descanso consumido.
- Cálculo del tiempo efectivo trabajado al finalizar el turno.
- Aviso al empleado cuando marca el inicio después de la hora programada, indicando los minutos de diferencia.
- Uso desde el teléfono, que es el dispositivo disponible en el puesto de trabajo.

### Fuera de alcance

- Marcar la asistencia de otro empleado, que corresponde al supervisor en la historia siguiente.
- Corregir o eliminar una marca ya registrada.
- Validación de la ubicación desde la que se marca.
- Reconocimiento facial, huella o cualquier otro medio biométrico.
- Cálculo de nómina, horas extra o recargos a partir de las marcas.
- Marcación sin conexión a internet.
- Justificación de una llegada tarde o de una ausencia.

### Reglas de negocio

- Solo se puede marcar sobre un turno propio, ya publicado y correspondiente al día en curso.
- Las marcas siguen un orden obligatorio: inicio de turno, luego cualquier par de salida y regreso de descanso, y por último fin de turno. Ninguna marca se acepta fuera de esa secuencia.
- Cada marca se registra una sola vez en su posición de la secuencia: no se puede marcar dos veces el inicio ni dos veces el fin.
- No se puede regresar de un descanso que no se inició, ni finalizar el turno estando en descanso: primero hay que registrar el regreso.
- Si el inicio se marca después de la hora programada, el sistema registra la diferencia en minutos y la muestra al empleado, sin calificarla.
- La hora de cada marca la toma el servidor, nunca el reloj del dispositivo del empleado.
- Una marca registrada no puede ser modificada ni eliminada por el empleado.
- El tiempo efectivo trabajado es la diferencia entre el fin y el inicio del turno, menos el tiempo consumido en descansos.
- Un turno sin marca de fin al terminar el día queda como pendiente de cierre y así se le muestra al supervisor.
- No se puede marcar en un día sin turno asignado, ni en un día registrado como descanso, vacaciones o incapacidad.
- Cada marca guarda quién la registró (Empleado o Supervisor, en esta historia, Será el empleado)

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como la empleada "Ana Pérez", Barista de "Centro" Y tengo asignado el turno "Apertura" (06:00 a 14:00) el día de hoy Y ese turno define un descanso de 30 minutos
```

#### CA-14.1 Secuencia completa de marcación

```gherkin
  Cuando a las "05:58" presiono "Iniciar turno"
  Entonces el sistema registra la marca de inicio a las "05:58"
  Y muestra el tiempo transcurrido desde el inicio del turno
  Y cuando a las "10:00" presiono "Salir a descanso" registra esa marca
  Y cuando a las "10:28" presiono "Regresar de descanso" registra esa marca
  Y muestra "28 minutos de descanso consumidos"
  Y cuando a las "14:03" presiono "Finalizar turno" registra esa marca
  Y muestra "Tiempo efectivo trabajado: 7 h 37 min"
```

#### CA-14.2 Inicio marcado después de la hora programada

```gherkin
  Cuando a las "06:12" presiono "Iniciar turno"
  Entonces el sistema registra la marca de inicio a las "06:12"
  Y muestra "Iniciaste 12 minutos después de la hora programada"
  Y la marca queda registrada de todas formas
```

#### CA-14.3 Marcas fuera de secuencia

```gherkin
Esquema del escenario:
  Dado que mi estado actual es "<estado>"
  Cuando intento la marca "<intento>"
  Entonces el sistema muestra "<mensaje>" y no registra ninguna marca

 Ejemplos:
   | Estado | Intento | Mensaje |
   | Sin iniciar | Salir a descanso | Primero debes iniciar el turno |
   | Sin Iniciar | Finalizar turno | Primero debes iniciar el turno |
   | En Descanso | Finalizar turno | Primero debes registrar tu regreso de descanso |
   | Turno Iniciado | Regresar de descanso | No tienes un descanso en curso |
   | Turno Cerrado | Iniciar turno | Este turno ya fue finalizado |
```

#### CA-14.4 Día sin turno asignado o en descanso

```gherkin
  Dado que hoy no tengo ningún turno asignado
  Cuando abro la pantalla de asistencia
  Entonces el sistema muestra "Hoy no tienes un turno asignado."
  Y no se ofrece ninguna opción de marcación
  Y el mismo comportamiento se aplica si el día está registrado como
    descanso, vacaciones o incapacidad
```

#### CA-14.5 La hora la determina el servidor

```gherkin
  Dado que el reloj de mi teléfono está adelantado 45 minutos
  Cuando presiono "Iniciar turno"
  Entonces la marca queda registrada con la hora del servidor
  Y no con la hora que muestra mi teléfono
```

#### CA-14.6 Las marcas no se pueden alterar y quedan consultables

```gherkin
  Dado que registré el inicio de mi turno
  Cuando reviso la pantalla de asistencia
  Entonces no se ofrece ninguna opción para modificar ni eliminar esa marca
```

### RNF aplicables

- RNF-01 (Rendimiento): el turno del día y el botón de marca se despliegan en 2 segundos o menos.
- RNF-02 (Seguridad): la marca queda a nombre del empleado autenticado; nadie puede marcar por otro.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): la pantalla muestra con claridad la fecha y la hora del turno sobre el que se marca.

### Dependencias

- HU-01
- Requiere el turno asignado y publicado del día, con su hora de inicio, su hora de fin y sus descansos.
- Requiere el registro de días de descanso, vacaciones e incapacidades para saber cuándo no corresponde marcar.

### Riesgos

- Alto. Si el sistema no está disponible a las 06:00, el empleado no puede marcar el inicio y el registro del día se pierde o queda a cargo del supervisor.
- Alto. Sin verificación de la ubicación, alguien puede marcar sin estar en la sucursal, y el registro deja de reflejar la realidad
- Medio. Olvidar la marca de fin es lo más frecuente en cualquier sistema de asistencia. El turno queda sin cierre y el dato pierde valor. Mitigación: recordatorio al empleado cerca de la hora de fin.
- Medio. Un empleado sin teléfono con datos no puede marcar. Mitigación: definir si la sucursal dispone de un dispositivo para estos casos.
- Bajo. Registrar la llegada tarde sin tolerancia puede generar fricción si la cadena no tiene una política al respecto. Mitigación: el sistema informa la diferencia y no la califica; la política se define aparte.

### Datos de prueba

- Ana Pérez, Barista de la sucursal Centro, activa, con sesión iniciada.
- Turno "Apertura" de 06:00 a 14:00 con un descanso de 30 minutos, asignado al día en curso.
- Secuencia completa: inicio 05:58, salida a descanso 10:00, regreso 10:28 y fin 14:03, que arrojan 7 h 37 min efectivas.
- Inicio a las 06:12, para el aviso de 12 minutos de diferencia.
- Estados para las marcas fuera de secuencia: sin iniciar, en descanso, turno iniciado y turno cerrado.
- Día sin turno asignado y día registrado como vacaciones.
- Dispositivo con el reloj adelantado 45 minutos.

### Evaluación DoR

**Veredicto: lista con reserva.** La historia tiene actor, valor medible, alcance delimitado, reglas completas, siete criterios verificables, requisitos no funcionales y datos de prueba.

- Reserva: decidir si se exige verificar la presencia física al marcar. Es la única decisión que puede cambiar el alcance de forma significativa.
- Reserva: confirmar si existe alguna tolerancia de llegada tarde.
- No es independiente: requiere que los turnos ya se asignen y se publiquen.

---

## 5.3 HU-15 — Marcar asistencia al turno (registro por el supervisor)

**HU-15**

### Épica

Épica 5 — Control de Asistencia

### Nombre HU

Marcar asistencia al turno

### Prioridad

Sin ella, cada olvido deja un turno sin registro y el control de asistencia queda incompleto.

### Actor

Administrador de Sucursal sobre los empleados de su sucursal.

### Historia de usuario

**Como** Administrador de Sucursal de Grano Dorado, **quiero** registrar el inicio o el fin de turno de un empleado de mi sucursal cuando él olvidó marcarlo, dejando constancia del motivo y de que la marca la hice yo, **para** mantener el control completo de quién estuvo laborando y durante cuánto tiempo.

### Contexto

Olvidar una marca es lo más común en cualquier control de asistencia, sobre todo la de salida, cuando la persona ya terminó y se va. Si no hay forma de completarla, el registro queda con vacíos y deja de servir para lo que se creó. El punto delicado de esta historia es el equilibrio: el supervisor debe poder completar lo que falta, pero no reescribir lo que el empleado ya registró, porque entonces el registro dejaría de ser confiable para ambas partes.

### Valor esperado

- Evita que un olvido convierta un turno trabajado en un turno sin registro.
- Mantiene la confianza en el dato al distinguir siempre qué marcó el empleado y qué completó el supervisor.
- Medición: menos del 10 % del total de marcas del mes son registradas por un supervisor. Un porcentaje mayor indica que la marcación propia no está funcionando.
- Medición: ningún turno trabajado queda sin marca de inicio y de fin al cierre de la semana.

### Alcance

- Listado de los turnos del día de mi sucursal, con el estado de marcación de cada empleado.
- Identificación de los turnos con marcas faltantes: sin inicio, sin fin o sin regreso de descanso.
- Registro de la marca faltante indicando la hora que corresponde y un motivo obligatorio.
- Señalización visible de cada marca registrada por el supervisor, con su nombre y el momento en que la registró.
- Corrección de una marca que yo mismo registré, mientras el turno no esté cerrado, con nueva justificación.
- Historial de las marcas de un turno, con quién registró cada una y cuándo.

### Fuera de alcance

- Modificar o eliminar una marca registrada por el propio empleado.
- Registrar marcas de empleados de otra sucursal.
- Justificar una ausencia completa o registrar una inasistencia como si fuera un turno trabajado.
- Reasignar el turno a otra persona, que pertenece a la planificación.

### Reglas de negocio

- Solo se pueden completar marcas de empleados de la propia sucursal.
- Solo se realizan marcas de un turno del día en curso, no turnos pasados o futuros.
- Una marca registrada por el empleado no puede ser modificada ni eliminada por el supervisor.
- Solo se puede registrar una marca respetando la secuencia: no se registra un fin de turno si no existe el inicio.
- El motivo del registro es obligatorio y queda visible en el historial del turno.
- Toda marca registrada por el supervisor queda identificada como tal, con su nombre, el momento en que la registró y la hora que se está registrando, que son dos datos distintos.
- Una marca registrada por el supervisor puede ser corregida por él mientras el turno no esté cerrado, y cada corrección queda en el historial con su justificación.
- El empleado puede consultar las marcas que el supervisor registró sobre sus turnos.
- Completar una marca no altera el turno programado ni la asignación.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como "Carla Ríos", Administradora de Sucursal
    de "Centro" Y la empleada "Ana Pérez" tuvo asignado el turno "Apertura"
    (06:00 a 14:00) el "2026-09-08" Y la fecha y hora actuales son "2026-09-08 15:00"
```

#### CA-15.1 Registro de una marca de fin que el empleado olvidó

```gherkin
  Dado que "Ana Pérez" marcó el inicio a las "06:02" y no marcó el fin
  Cuando abro el listado de turnos y ese turno aparece como
    "Pendiente de cierre"
  Y registro la hora de fin "14:05" con el motivo "La empleada olvidó
    marcar la salida"
  Y presiono el botón "Guardar"
  Entonces el sistema muestra "Marca registrada correctamente."
  Y el turno queda con inicio "06:02" y fin "14:05"
  Y la marca de fin aparece señalada como "Registrada por Carla Ríos
    el 08/09/2026 a las 15:00"
  Y la marca de inicio sigue señalada como registrada por la empleada
```

#### CA-15.2 No se puede alterar una marca hecha por el empleado

```gherkin
  Dado que "Ana Pérez" marcó el inicio a las "06:02"
  Cuando abro el detalle de ese turno
  Entonces la marca de inicio se muestra en modo de solo lectura
  Y no se ofrece ninguna opción para modificarla ni eliminarla
```

#### CA-15.3 El motivo es obligatorio

```gherkin
  Cuando registro una marca sin escribir el motivo
  Y presiono el botón "Guardar"
  Entonces el sistema muestra "Indica el motivo del registro."
  Y no se registra ninguna marca
```

#### Esquema del escenario: CA-15.4 Coherencia de la hora registrada

```gherkin
  Dado que el turno programado fue de "06:00" a "14:00"
  Cuando registro la marca "<marca>" con la hora "<hora>"
  Entonces el sistema muestra "<mensaje>" y no registra ninguna marca
  Ejemplos:

    | Marca | Hora | Mensaje |
    | Inicio | 05:10 | La hora es anterior al inicio de turno de la sucursal |
    | Fin | 05:50 | El fin debe ser posterior al inicio de turno |
    | Fin | 22:30 | La hora no puede superar las 22:00 |
```

#### CA-15.5 Secuencia y turnos futuros

```gherkin
  Dado que "Diego Salas" no tiene ninguna marca en su turno de hoy Cuando intento registrar únicamente su marca de fin Entonces el sistema muestra "Primero debes registrar el inicio del turno." Y al intentar registrar marcas de un turno programado para mañana el sistema muestra "Este turno todavía no ha iniciado."  Y en ninguno de los dos casos se registra una marca
```

#### CA-15.6 Plazo de 48 horas

```gherkin
  Dado que el turno de "Ana Pérez" del "2026-09-06" terminó hace 3 días
  Cuando intento registrar su marca de fin
  Entonces el sistema muestra "El plazo de 48 horas para completar este
    turno venció. Comunícate con el Administrador General."
  Y no se registra ninguna marca
```

#### CA-15.7 Alcance por sucursal

```gherkin
  Dado que existen turnos de empleados de "Centro", "Norte" y "Sur"
  Cuando abro el listado de turnos del día
  Entonces solo veo turnos de empleados de "Centro"
  Y cuando el "Administrador General" abre el listado, ve los de las
    tres sucursales
```

### RNF aplicables

- RNF-01 (Rendimiento): el turno del empleado y las marcas ya registradas se despliegan en 2 segundos o menos.
- RNF-02 (Seguridad): solo el Administrador de Sucursal autenticado completa marcas, y únicamente de empleados de su sucursal.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): la pantalla distingue con claridad la marca hecha por el empleado de la registrada por el supervisor, con su fecha y su hora.

### Dependencias

- Requiere los turnos asignados y publicados del día, con sus horas de inicio y fin.
- Requiere la identidad del supervisor con su sucursal, para delimitar sobre quién puede registrar.
- Alimenta la atención de las notificaciones de incumplimiento de la historia siguiente.

### Riesgos

- Alto. Si el supervisor termina registrando la mayoría de las marcas, el dato deja de reflejar la realidad y pasa a reflejar su memoria.
- Medio. La hora que registra el supervisor no se verifica contra nada. Mitigación: motivo obligatorio, señalización visible del origen de la marca y visibilidad para el empleado.
- Bajo. La corrección de marcas propias del supervisor puede usarse para ajustar registros. Mitigación: cada corrección exige justificación y queda en el historial.

### Datos de prueba

- Carla Ríos, Administradora de Sucursal de Centro, con sesión iniciada.
- Ana Pérez, con turno "Apertura" del 08/09/2026, inicio marcado por ella a las 06:02 y sin marca de fin.
- Hora de fin a registrar: 14:05, con motivo escrito.
- Horas incoherentes para las validaciones: 05:10, 05:50 y 22:30.
- Diego Salas, con turno de hoy sin ninguna marca, para la validación de secuencia.
- Turno programado para mañana, para la validación de turno futuro.
- Turno del 06/09/2026, terminado hace 3 días, para la validación del plazo.
- Turnos de empleados de Norte y Sur, para la validación de alcance.

### Evaluación DoR

**Veredicto: lista con reserva.** La historia tiene actor, valor medible, alcance delimitado, reglas completas, siete criterios verificables, requisitos no funcionales y datos de prueba.

- Reserva: confirmar que el supervisor completa marcas faltantes pero nunca modifica las del empleado. Es la regla que sostiene la confiabilidad del registro.
- Reserva: fijar el plazo para completar y quién puede intervenir una vez vencido.
- No es independiente: no tiene sentido construirla antes que la marcación del empleado.

---

## 5.4 HU-16 — Notificaciones de incumplimientos

**HU-16 (rotulada HU-15 en el documento recibido, con la numeración anterior)**

### Épica

Épica 5 — Control de Asistencia

### Nombre HU

Notificaciones de incumplimientos

### Prioridad

Aporta la reacción a tiempo, pero requiere que la marcación ya esté funcionando para no generar avisos falsos.

### Actor

Administrador de Sucursal sobre su sucursal. Administrador General sobre las tres.

### Historia de usuario

**Como** Administrador de Sucursal de Grano Dorado, **quiero** recibir una notificación cuando un empleado no registra el inicio de su turno pasado un tiempo de la hora programada, **para** enterarme durante la jornada y no al final, y poder decidir qué hacer con la asignación de ese día.

### Contexto

Este es el punto donde la asistencia deja de ser un archivo y se vuelve una herramienta. Hoy el administrador descubre que alguien no llegó cuando la sucursal ya está sin cubrir. Con la marcación funcionando, la ausencia de un dato a una hora determinada es en sí misma una señal, y el sistema puede emitirla sin que nadie tenga que estar mirando. El valor no está en registrar la falta, sino en que llegue mientras todavía se puede hacer algo.

### Valor esperado

- Adelanta el momento en que el administrador se entera de una ausencia, de horas a minutos.
- Le da margen para reorganizar la cobertura del día antes de que la sucursal quede desatendida.
- Medición: la notificación llega dentro de los 20 minutos siguientes a la hora de inicio programada en el 95 % de los casos.
- Medición: reducción del número de franjas horarias que quedaron sin cubrir por una ausencia no advertida, comparando el primer mes con los tres siguientes.

### Alcance

- Notificación al Administrador de Sucursal cuando, pasados 15 minutos de la hora de inicio programada, no existe marca de inicio para un turno de su sucursal.
- Contenido de la notificación: empleado, sucursal, turno, hora programada y minutos transcurridos.
- Notificación al final del turno cuando no existe marca de fin, para que el supervisor la complete.
- Bandeja de incumplimientos del día con los estados pendiente y atendido.
- Cierre automático del aviso de inicio cuando el empleado marca después de haber sido notificado.
- Acción de dejar constancia sobre un incumplimiento, con un comentario.
- Consulta del historial de incumplimientos de la sucursal por rango de fechas y por empleado.

### Fuera de alcance

- Reasignar el turno a otro empleado o cubrir la ausencia, que pertenece a la planificación de turnos.
- Notificar al empleado ausente.
- Sanciones, descuentos o cualquier consecuencia disciplinaria automática.
- Justificación de la ausencia por parte del empleado.
- Notificaciones sobre llegadas tarde que sí registraron marca dentro del umbral.
- Indicadores de ausentismo, tendencias o comparación entre sucursales.
- Escalamiento automático a un nivel superior si el aviso no se atiende.

### Reglas de negocio

- Se genera un incumplimiento de inicio cuando transcurren 15 minutos desde la hora de inicio programada sin que exista marca de inicio.
- Cada turno genera como máximo un incumplimiento de inicio.
- Solo se evalúan turnos publicados. Un turno no publicado no genera incumplimientos.
- No se evalúan días registrados como descanso, vacaciones o incapacidad, porque en ellos no debería existir un turno asignado.
- La notificación llega al Administrador de Sucursal de la sucursal del turno. El Administrador General puede consultar las de las tres.
- Si el empleado marca el inicio después de haberse generado el aviso, el incumplimiento pasa a atendido de forma automática y registra la hora real de la marca.
- Un incumplimiento también pasa a atendido cuando el supervisor completa la marca faltante o deja constancia con un comentario.
- La notificación identifica al empleado, la sucursal, el turno, la hora programada y los minutos transcurridos.
- El incumplimiento no modifica el turno programado ni genera por sí solo ninguna consecuencia para el empleado.
- Cada incumplimiento y cada atención quedan registrados con la fecha, la hora y el usuario que intervino.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión como "Carla Ríos", Administradora de Sucursal de "Centro" Y "Ana Pérez" tiene asignado y publicado el turno "Apertura"  (06:00 a 14:00) del día de hoy
```

#### CA-16.1 Notificación por marca de inicio ausente

```gherkin
  Dado que son las "06:15" y "Ana Pérez" no ha marcado el inicio
  Cuando el sistema evalúa los turnos del día
  Entonces recibo una notificación que dice "Ana Pérez no ha registrado
    el inicio de su turno Apertura de las 06:00 en la sucursal Centro.
    Han pasado 15 minutos."
  Y el incumplimiento aparece en mi bandeja con estado "Pendiente"
  Y la notificación llega en 2 minutos o menos desde que se cumple
    el umbral
```

#### CA-16.2 El aviso no se repite

```gherkin
  Dado que ya se generó el incumplimiento de inicio de ese turno
  Cuando el sistema vuelve a evaluar los turnos a las "06:30" y "07:00"
  Entonces no se genera ningún incumplimiento adicional para ese turno
  Y recibo exactamente 1 notificación por ese turno
```

#### CA-16.3 Cierre automático cuando el empleado marca

```gherkin
  Dado que el incumplimiento está en estado "Pendiente"
  Cuando "Ana Pérez" marca el inicio a las "06:40"
  Entonces el incumplimiento pasa a estado "Atendido"
  Y registra "El empleado marcó el inicio a las 06:40"
  Y no requiere ninguna acción de mi parte
```

#### CA-16.4 Atención manual del incumplimiento

```gherkin
  Dado que el incumplimiento está en estado "Pendiente"
  Y "Ana Pérez" no se presentó en toda la jornada
  Cuando escribo la constancia "Reportó incapacidad por teléfono,
    cubrió Sofía Ramos" y presiono "Dejar constancia"
  Entonces el incumplimiento pasa a estado "Atendido"
  Y la constancia queda registrada con mi nombre y la fecha y hora
  Y el turno programado de "Ana Pérez" no se modifica
```

#### CA-16.5 Turnos que no generan incumplimiento

```gherkin
  Dado que la semana del turno de "Diego Salas" no ha sido publicada
  Y "Marta León" tiene el día de hoy registrado como vacaciones
  Cuando el sistema evalúa los turnos del día
  Entonces no se genera ningún incumplimiento para "Diego Salas"
  Ni para "Marta León"
```

#### CA-16.6 Alcance por sucursal

```gherkin
  Dado que hay incumplimientos en "Centro", "Norte" y "Sur"
  Cuando abro mi bandeja de incumplimientos
  Entonces solo veo los de la sucursal "Centro"
  Y cuando el "Administrador General" abre la suya, ve los de las
    tres sucursales
  Y ningún empleado con rol operativo tiene acceso a esta bandeja
```

### RNF aplicables

- RNF-01 (Rendimiento): el listado de incumplimientos del día se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): el aviso llega únicamente al Administrador de Sucursal autenticado que corresponde a la sucursal del turno.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): el aviso identifica con claridad el empleado, la fecha y la hora del turno incumplido.

### Dependencias

- Requiere la marcación del empleado: sin ella todos los turnos parecerían incumplidos.
- Requiere los turnos asignados y publicados, con su hora de inicio y de fin.
- Requiere el registro de descansos, vacaciones e incapacidades para no generar avisos sobre días en los que nadie debía trabajar.
- Se apoya en el registro por el supervisor como una de las formas de atender un incumplimiento.
- Requiere definir el canal por el que se entrega la notificación.

### Riesgos

- Alto. Si la marcación aún no está adoptada, casi todos los turnos generarán avisos y el supervisor dejará de mirarlos. Mitigación: activar esta historia solo cuando la marcación propia supere un nivel de uso acordado.
- Alto. El sistema no puede distinguir entre alguien que no llegó y alguien que llegó y olvidó marcar. El aviso puede señalar a un empleado que sí está trabajando. Mitigación: redactar la notificación en términos de la marca ausente, no de la persona ausente, y ofrecer la constancia como forma de cerrarlo.
- Medio. Un umbral demasiado corto genera ruido y uno demasiado largo llega tarde. Mitigación: umbral configurable y revisión tras las primeras semanas.
- Medio. El supervisor está atendiendo la sucursal y puede no ver la notificación a tiempo. Mitigación: confirmar el canal y mantener la bandeja visible en la pantalla principal.
- Bajo. La historia entrega el aviso pero no la solución: reasignar el turno ocurre en otra pantalla. Mitigación: enlazar el incumplimiento con la programación del día.

### Datos de prueba

- Carla Ríos, Administradora de Sucursal de Centro, con sesión iniciada.
- Ana Pérez, con turno "Apertura" de 06:00 a 14:00 publicado para el día en curso.
- Evaluación a las 06:15 sin marca de inicio, para el aviso de 15 minutos.
- Marca de inicio a las 06:40, para el cierre automático del aviso.
- Turno con inicio marcado y sin marca de fin a las 14:00, para el aviso de cierre.
- Diego Salas, con turno en una semana sin publicar.
- Marta León, con el día registrado como vacaciones.
- Incumplimientos existentes en Norte y Sur, para la validación de alcance.

### Evaluación DoR

**Veredicto: no lista.** La historia tiene actor, valor medible, alcance delimitado, siete criterios verificables, requisitos no funcionales y datos de prueba, pero dos definiciones siguen abiertas.

- Pendiente: definir el canal por el que llega la notificación. Sin él, la historia no se puede construir ni medir su tiempo de entrega.
- Pendiente: acordar el umbral de minutos y a partir de qué nivel de adopción de la marcación se activan los avisos.
- No es independiente: es la última de la épica y depende de las dos anteriores.
