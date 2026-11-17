# Épica 2 — Planificación y asignación de turnos

**Resumen:** Agrupa la definición del catálogo de turnos (HU-05), la asignación de turnos a empleados con validación de conflictos (HU-06) y la visualización de los horarios por sucursal (HU-07).

*Resumen elaborado a partir de las historias de la épica; el documento original no incluye una descripción propia de esta épica.*

## Historias de usuario de esta épica

| Sección | Identificador | Nombre |
|---|---|---|
| 2.1 | [HU-05](#21-hu-05--creación-de-turnos) | Creación de turnos |
| 2.2 | [HU-06](#22-hu-06--asignación-de-turnos) | Asignación de turnos |
| 2.3 | [HU-07](#23-hu-07--visualización-de-horarios-por-sucursal) | Visualización de horarios por sucursal |

---

## 2.1 HU-05 — Creación de turnos

**HU-05**

### Épica

Épica 2 — Planificación y Asignación de turnos

### Nombre HU

Creación de turnos

### Prioridad

HU-06 no puede asignar nada sin un catálogo de turnos.

### Actor

Administrador General

### Historia de usuario

**Como** Administrador General de Grano Dorado, **quiero** definir turnos laborales con su hora de inicio, su hora de fin y sus tiempos de descanso, **para** disponer de un catálogo reutilizable que pueda asignar a los empleados de las 3 sucursales.

### Contexto

Hoy cada horario se escribe a mano en Excel, celda por celda, lo que obliga a redigitar las mismas franjas semana tras semana y abre la puerta a errores de transcripción. La sucursal abre 16 horas diarias los 7 días, un rango que ninguna persona cubre sola: el trabajo real del administrador es combinar unas pocas franjas fijas. Definir esas franjas una sola vez, como plantillas, es lo que convierte la programación en una selección en lugar de una redacción.

### Valor esperado

- Elimina la redigitación semanal de horarios y estandariza las franjas, atacando directamente las 8 h/semana
- El catálogo cubre el 100 % de la franja 06:00–22:00 sin huecos ni solapamientos no intencionales, verificable sobre la suma de los turnos definidos.
- Asignar una semana completa deja de requerir escribir horas a mano.

### Alcance

- Formulario de creación de turno: nombre, hora de inicio, hora de fin y lista de descansos (hora de inicio y hora de fin de cada uno).
- Cálculo automático de la duración total y de la duración efectiva (total menos descansos).
- Validación de rango horario, de coherencia entre inicio y fin, y de los descansos dentro del turno.
- Listado del catálogo de turnos con su duración y su estado (Activo / Inactivo).
- Edición e inactivación de turnos, conservando los ya asignados.
- Registro en historial de la creación y de cada modificación.

### Fuera de alcance

- Asignación de turnos a empleados.
- Turnos que cruzan la medianoche: la operación termina a las 22:00.
- Turnos diferentes por sucursal: el catálogo es único y compartido.
- Reglas de remuneración, recargos nocturnos, dominicales o festivos.
- Plantillas de semana completa o patrones rotativos.
- Definición de la cobertura mínima requerida por franja (pertenece a HU-06 / HU-07).

### Reglas de negocio

- Un turno se compone de nombre, hora de inicio, hora de fin y cero o más descansos.
- La hora de inicio y la hora de fin deben estar dentro de la franja de operación 06:00–22:00.
- La hora de fin debe ser posterior a la hora de inicio; ningún turno cruza la medianoche.
- Cada descanso debe estar contenido en el rango del turno y no puede solaparse con otro descanso.
- La duración efectiva es la duración total menos la suma de los descansos, y debe ser mayor que 0.
- El nombre del turno es único en el catálogo, sin distinguir mayúsculas.
- Un turno que ya tiene asignaciones no se elimina: se inactiva y deja de ofrecerse
- Modificar un turno no altera las asignaciones ya publicadas, que conservan el horario con el que se publicaron.
- El Administrador General crea, edita e inactiva turnos

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión con el rol "Administrador General"
  Y la franja de operación de Grano Dorado es de 06:00 a 22:00
```

#### CA-05.1 Creación exitosa de un turno con descanso

```gherkin
  Cuando registro el turno "Apertura" con inicio "06:00", fin "14:00"
    y un descanso de "10:00" a "10:30"
  Y presiono el botón "Guardar"
  Entonces el sistema muestra "Turno creado correctamente."
  Y el catálogo muestra "Apertura" con duración total "8 h" y duración
    efectiva "7 h 30 min"
  Y el turno queda en estado "Activo"
  Y se agrega al historial un registro "TURNO_CREADO" con el usuario ejecutor
```

#### Esquema del escenario: CA-05.2 Rango horario y coherencia de las horas

```gherkin
  Cuando registro un turno con inicio "<inicio>" y fin "<fin>"
  Entonces el sistema muestra "<mensaje>" y no se crea ningún turno
  Ejemplos:
    | Inicio | Fin | Mensaje |
    | 05:00 | 13:00 | El horario debe estar entre las 06:00 y las 22:00 |
    | 16:00 | 23:00 | El horario debe estar entre las 06:00 y las 22:00 |
    | 14:00 | 14:00 | La hora de fin debe ser posterior a la hora de inicio |
    | 18:00 | 09:00 | La hora de fin debe ser posterior a la hora de inicio |
```

#### CA-05.3 Descanso fuera del rango del turno

```gherkin
  Dado que registro el turno "Cierre" con inicio "14:00" y fin "22:00"
  Cuando agrego un descanso de "13:00" a "13:30"
  Entonces el sistema muestra "El descanso debe estar dentro del horario
    del turno."
  Y no se crea ningún turno
```

#### CA-05.4 Descansos que se solapan entre sí

```gherkin
  Dado que registro el turno "Intermedio" con inicio "10:00" y fin "18:00"
  Cuando agrego un descanso de "14:00" a "14:30" y otro de "14:15" a "14:45"
  Entonces el sistema muestra "Los descansos no pueden solaparse."
  Y no se crea ningún turno
```

#### CA-05.5 Nombre de turno duplicado

```gherkin
  Dado que ya existe el turno "Apertura"
  Cuando registro otro turno llamado "apertura"
  Entonces el sistema muestra "Ya existe un turno con este nombre."
  Y el catálogo sigue conteniendo exactamente 1 turno llamado "Apertura"
```

#### CA-05.6 Un turno con asignaciones se inactiva, no se elimina

```gherkin
  Dado que el turno "Cierre" tiene 12 asignaciones registradas
  Cuando abro su ficha
  Entonces la acción "Eliminar" no está disponible
  Y al presionar "Inactivar" el turno pasa a estado "Inactivo"
  Y deja de aparecer en la lista de turnos asignables de HU-06
  Y las 12 asignaciones existentes conservan su horario sin cambios
```

### RNF aplicables

- RNF-01 (Rendimiento): el catálogo de turnos con sus horas se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): solo el Administrador General autenticado crea, edita o inactiva turnos.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): cada turno muestra con claridad su hora de inicio, su hora de fin y sus descansos.

### Dependencias

- HU-01 (sesión autenticada y matriz de permisos): solo el Administrador General accede a esta función.
- Ninguna dependencia funcional aguas arriba: es la historia más independiente de la épica y puede iniciarse en paralelo con la Épica 1.
- HU-06 y HU-07 dependen de ella.

### Riesgos

- Cada sucursal necesita turnos propios (por ejemplo, distinta hora de apertura), el catálogo debe llevar sucursal y cambia el modelo de datos. Mitigación: confirmar con el Administrador General antes del sprint.
- Si existen jornadas partidas, el concepto de turno único por día se rompe y afecta a HU-06. Mitigación: revisar el Excel actual en busca de jornadas partidas.
- Un catálogo demasiado granular reintroduce el trabajo manual que la historia busca eliminar. Mitigación: acordar con el Administrador General un conjunto inicial de 3 a 5 turnos.

### Datos de prueba

- Inicio: "Apertura" 06:00–14:00 con descanso 10:00–10:30 → creado, efectiva 7 h 30 min.
- Fuera de franja: 05:00–13:00 y 16:00–23:00 → rechazados.
- Horas incoherentes: 14:00–14:00 y 18:00–09:00 → rechazados.
- Descanso fuera del turno: turno 14:00–22:00 con descanso 13:00–13:30 → rechazado.
- Descansos solapados: 14:00–14:30 y 14:15–14:45 → rechazado.
- Nombre duplicado: "apertura" existiendo "Apertura" → rechazado.
- Turno con 12 asignaciones: "Cierre" → sin acción Eliminar, solo Inactivar.

### Evaluación DoR

**Veredicto: LISTA CON RESERVA.** Cumplen: formato, valor medible, alcance delimitado, 6 criterios verificables, reglas trazables al caso, RNF, datos de prueba, independencia y estimabilidad.

- Reserva: confirmar catálogo único para las 3 sucursales, que es lo único que puede cambiar el modelo de datos.
- Es la única historia de la épica que puede entrar a sprint casi de inmediato; conviene programarla primero.

---

## 2.2 HU-06 — Asignación de turnos

**HU-06**

### Épica

Épica 2 — Planificación y Asignación de turnos

### Nombre HU

Asignación de turnos

### Prioridad

Es el núcleo del producto y la historia que ataca el problema principal del caso.

### Actor

Administrador General en las 3 sucursales. Administrador de Sucursal, limitado a la suya.

### Historia de usuario

**Como** Administrador General de Grano Dorado, **quiero** asignar turnos del catálogo a empleados en fechas concretas y que el sistema rechace las asignaciones que entran en conflicto con su disponibilidad, **para** garantizar la cobertura de la jornada de 06:00 a 22:00 sin programar a personas que están en descanso o de vacaciones.

### Contexto

Este es el problema central del caso. Hoy la asignación se hace a mano en Excel, que no sabe nada sobre descansos ni vacaciones, y por eso se programan turnos a personas no disponibles. El costo son 8 horas semanales del Administrador General más las ausencias que provoca el error. La validación automática de conflictos, no el formulario de asignación es lo que entrega el valor de esta historia.

### Valor esperado

- Impide en el momento de programar el error de asignar horarios a personas en descanso o vacaciones, en lugar de descubrirlo cuando el empleado no se presenta.
- 0 asignaciones publicadas sobre empleados en día de descanso, vacaciones o incapacidad.
- El tiempo de programación semanal del Administrador General baja de 8 h a 2 h o menos, medido sobre 4 semanas consecutivas.
- 100 % de las franjas de 06:00 a 22:00 con cobertura verificada antes de publicar.

### Alcance

- Selección de sucursal y de semana (lunes a domingo) sobre la que se programa.
- Asignación de un turno del catálogo a un empleado en una fecha concreta.
- Validación en línea de conflictos: empleado inactivo, de otra sucursal, en descanso, en vacaciones, con incapacidad, con turno solapado o sin el descanso mínimo entre turnos.
- Modificación y eliminación de asignaciones mientras la semana está en estado Borrador.
- Publicación de la semana, que la vuelve visible para los empleados y dispara la notificación.
- Restricción por rol: el Administrador de Sucursal solo programa en su sucursal.
- Registro en historial de cada asignación, cambio y publicación.

### Fuera de alcance

- Generación automática o sugerida de la malla: la asignación es manual.
- Registro de descansos, vacaciones e incapacidades: esta historia los consulta, no los captura.
- Intercambio de turnos entre compañeros y reemplazos por enfermedad: requiere historias propias.
- Cálculo de nómina, horas extra y recargos.
- Definición del catálogo de turnos.
- Visualización consolidada de la malla.
- Programación de más de una semana en una sola operación.

### Reglas de negocio

- Solo se asignan turnos a empleados en estado Activo y de la misma sucursal de la programación.
- El sistema rechaza la asignación si el empleado tiene registrado ese día un descanso, vacaciones o una incapacidad. Es la regla que da sentido a la historia.
- Un empleado no puede tener dos asignaciones el mismo día.
- Al superar 42 horas efectivas en la semana, el sistema bloquea la asignación e indica el total acumulado.
- Una semana está en estado Borrador o Publicada. En Borrador se edita libremente; una vez Publicada, cada cambio genera una versión nueva y notifica al empleado afectado.
- No se crean asignaciones con fecha anterior a la fecha actual.
- El Administrador de Sucursal programa únicamente en su sucursal.
- Toda asignación, cambio y publicación se registra con usuario, fecha-hora y valores anterior y nuevo.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión con el rol "Administrador General"
  Y el catálogo contiene los turnos "Apertura" (06:00-14:00) y
    "Cierre" (14:00-22:00)
  Y la sucursal "Centro" tiene a la empleada "Ana Pérez" (Barista, Activo)
  Y la semana del 14 al 20 de septiembre de 2026 está en estado "Borrador"
```

#### CA-06.1 Asignación exitosa de un turno

```gherkin
  Cuando asigno el turno "Apertura" a "Ana Pérez" el "2026-09-14"
  Y presiono el botón "Guardar"
  Entonces el sistema muestra "Turno asignado correctamente."
  Y la celda del lunes 14 para "Ana Pérez" muestra "Apertura 06:00-14:00"
  Y se agrega a la bitácora un registro "TURNO_ASIGNADO" con el usuario ejecutor
```

#### CA-06.2 Rechazo por día de descanso o vacaciones

```gherkin
  Dado que "Ana Pérez" tiene registrado el "2026-09-16" como día de descanso
  Cuando asigno el turno "Cierre" a "Ana Pérez" el "2026-09-16"
  Entonces el sistema muestra "Ana Pérez está en día de descanso el
    16/09/2026. No es posible asignarle un turno."
  Y no se crea ninguna asignación
  Y el mensaje equivalente se muestra cuando el día está marcado como
    "Vacaciones" o "Incapacidad"
```

#### CA-06.3 Rechazo por segundo turno el mismo día

```gherkin
  Dado que "Ana Pérez" ya tiene asignado "Apertura" el "2026-09-14"
  Cuando le asigno además el turno "Cierre" el "2026-09-14"
  Entonces el sistema muestra "Ana Pérez ya tiene un turno asignado
    el 14/09/2026."
  Y no se crea ninguna asignación
```

#### CA-06.4 Bloqueo al superar el máximo semanal

```gherkin
  Dado que "Ana Pérez" acumula 38 horas efectivas asignadas en la semana
  Cuando le asigno un turno de 7 h 30 min más en esa misma semana
  Entonces el sistema muestra "Ana Pérez alcanzaría 45h 30 min en la semana.
    El máximo permitido es 42 horas."
  Y no se crea ninguna asignación
```

#### CA-06.5 Empleado inactivo o de otra sucursal no es asignable

```gherkin
  Dado que "Luis Gómez" tiene estado "Inactivo"
  Y "Carlos Ruiz" pertenece a la sucursal "Norte"
  Cuando abro la lista de empleados asignables de la sucursal "Centro"
  Entonces la lista NO contiene a "Luis Gómez" ni a "Carlos Ruiz"
```

#### CA-06.6 Alcance del Administrador de Sucursal

```gherkin
  Dado que inicié sesión con el rol "Administrador de Sucursal" de "Centro"
  Cuando abro la pantalla de asignación
  Entonces el selector de sucursal muestra "Centro" y no es editable
  Y la lista de empleados contiene únicamente empleados de "Centro"
```

### RNF aplicables

- RNF-01 (Rendimiento): la malla de la semana de una sucursal se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): solo los administradores autenticados asignan turnos, y el Administrador de Sucursal únicamente sobre la suya.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): cada asignación muestra con claridad el empleado, la fecha y la hora del turno.

### Dependencias

- HU-01 (sesión y permisos), HU-03 y HU-04 (empleados con rol, sucursal y estado vigentes) y HU-05 (catálogo de turnos).
- Bloqueante: se requiere una historia que registre descansos, vacaciones e incapacidades. Sin ella la historia pierde su razón de ser.
- HU-07 depende de las asignaciones que produce esta historia.

### Riesgos

- Si no existe el registro de descansos y vacaciones, HU-06 se entrega sin su validación principal y el problema H-8 permanece. Mitigación: incorporar esa historia al mismo sprint o al anterior.
- La normativa laboral aplicable: bloquear con límites incorrectos puede impedir programaciones legítimas o permitir jornadas irregulares. Mitigación: revisión con asesoría laboral y parámetros configurables.
- Reglas demasiado estrictas en una cadena con 45 empleados pueden dejar franjas sin cubrir.
- Edición simultánea del Administrador General y del Administrador de Sucursal sobre la misma semana. Mitigación: aviso de conflicto.
- Reprogramación tras la publicación genera versiones y notificaciones repetidas. Mitigación: agrupar los cambios y notificar una sola vez por publicación.

### Datos de prueba

- Semana de prueba: 14 al 20 de septiembre de 2026, sucursal "Centro", estado "Borrador".
- Turnos: "Apertura" 06:00–14:00 y "Cierre" 14:00–22:00, ambos de 7 h 30 min efectivas.
- Ana Pérez — Barista, Centro, Activo, con descanso registrado el 16/09/2026.
- Luis Gómez — Cocinero, Norte, Inactivo (no debe aparecer como asignable).
- Carlos Ruiz — Cajero, Norte, Activo (no debe aparecer al programar Centro).
- Caso de descanso mínimo: Cierre el 14/09 seguido de Apertura el 15/09 → 8 h de separación.
- Caso de máximo semanal: empleado con 38 h acumuladas más un turno de 7 h 30 min.
- Caso de cobertura: martes 15 sin asignaciones entre 18:00 y 22:00.

### Evaluación DoR

**Veredicto: NO LISTA.** Cumplen: formato, valor medible, alcance, 8 criterios verificables, RNF y datos de prueba.

- Bloqueante 1:no existe historia que registre descansos, vacaciones e incapacidades, y sin esos datos CA-06.2 no es implementables.
- Bloqueante 2: Los límites de jornada, descanso mínimo y máximo semanal no están definidos ni validados jurídicamente.
- No es independiente: depende de HU-05 y del módulo de empleados de la Épica 1.

---

## 2.3 HU-07 — Visualización de horarios por sucursal

**HU-07**

### Épica

Épica 2 — Planificación y Asignación de turnos

### Nombre HU

Visualización de horarios por sucursal

### Prioridad

Es la verificación que pide la épica; depende de que existan asignaciones.

### Actor

Administrador General en las 3 sucursales. Administrador de Sucursal, limitado a la suya.

### Historia de usuario

**Como** Administrador General o Administrador de Sucursal de Grano Dorado, **quiero** ver en una sola pantalla la malla semanal de turnos programados de una sucursal, con los vacíos de cobertura señalados, **para** verificar que la jornada de 06:00 a 22:00 queda cubierta antes de publicar el horario.

### Contexto

La descripción de la épica pide expresamente "verificar la asignación de cada sucursal". Hoy esa verificación se hace leyendo una hoja de Excel donde los huecos de cobertura no saltan a la vista, que es parte de por qué los errores se detectan cuando alguien no llega. Con 3 sucursales, 45 empleados y 112 horas semanales de operación por local, la malla es el instrumento de control del Administrador General.

### Valor esperado

- Convierte la verificación de cobertura en una lectura visual, en lugar de un recuento manual sobre Excel.
- Los vacíos de cobertura de una semana se identifican en menos de 1 minuto, medido con 5 mallas de prueba.
- 0 semanas publicadas con una franja descubierta sin que el administrador lo hubiera advertido antes.

### Alcance

- Malla semanal por sucursal: filas de empleados y columnas de lunes a domingo.
- Cada celda muestra el turno asignado con su horario, o la marca de Descanso, Vacaciones o Incapacidad.
- Indicador de cobertura por día y franja frente al mínimo configurado, con las franjas descubiertas resaltadas.
- Totalizador de horas efectivas por empleado en la semana.
- Navegación entre semanas y, para el Administrador General, cambio de sucursal.
- Distinción visible entre una semana en Borrador y una Publicada.
- Filtro por rol (Cajero, Barista, Cocinero) dentro de la sucursal.

### Fuera de alcance

- Vista del empleado sobre su propio horario: se necesita una historia aparte y es la que cierra el problema de comunicación.
- Edición de asignaciones desde la malla: esta historia es de solo lectura (la edición es HU-06).
- Vista consolidada de las 3 sucursales en una sola pantalla.
- Exportación a Excel o PDF e impresión.
- Indicadores históricos, costos laborales y comparación entre semanas.
- Notificaciones y recordatorios de turno.

### Reglas de negocio

- La malla se presenta por sucursal y por semana de lunes a domingo.
- Cada celda muestra el nombre del turno y su horario, o el estado de no disponibilidad del empleado ese día.
- El Administrador General puede cambiar de sucursal; el Administrador de Sucursal solo ve la propia.
- La malla indica de forma visible si la semana está en Borrador o Publicada, y la fecha-hora de la última publicación.
- La malla se muestra en modo de solo lectura: ninguna acción de esta pantalla modifica asignaciones.
- Los empleados inactivos no aparecen en la malla de semanas futuras, pero sí en las semanas ya publicadas donde tenían turnos.
- El totalizador por empleado suma la duración efectiva, descontando los descansos definidos en el turno.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión con el rol "Administrador General"
  Y la sucursal "Centro" tiene programada la semana del 14 al 20 de
    septiembre de 2026
  Y el mínimo de cobertura configurado es de 1 empleado por franja
```

#### CA-07.1 Despliegue de la malla semanal

```gherkin
  Cuando selecciono la sucursal "Centro" y la semana del "2026-09-14"
  Entonces el sistema muestra una tabla con una fila por empleado activo
    de "Centro" y 7 columnas, de "Lunes 14" a "Domingo 20"
  Y la celda de "Ana Pérez" en "Lunes 14" muestra "Apertura 06:00-14:00"
  Y la malla se despliega completa en 3 segundos o menos
```

#### CA-07.2 Días de no disponibilidad

```gherkin
  Dado que "Ana Pérez" tiene registrado el "2026-09-16" como día de descanso
  Cuando abro la malla
  Entonces su celda del "Miércoles 16" muestra "Descanso"
  Y esa celda no muestra ningún turno
```

#### CA-07.3 Totalizador de horas efectivas por empleado

```gherkin
  Dado que "Ana Pérez" tiene asignados 4 turnos de 7 h 30 min efectivas
    en la semana
  Cuando abro la malla
  Entonces su fila muestra el total "30 h 00 min"
```

#### CA-07.4 Estado de la semana y solo lectura

```gherkin
  Cuando abro una semana en estado "Borrador"
  Entonces la malla muestra la etiqueta "Borrador — no visible para los
    empleados"
  Y al abrir una semana publicada muestra "Publicada el 07/09/2026 18:40"
  Y en ninguno de los dos casos la malla ofrece acciones para crear,
    modificar o eliminar asignaciones
```

#### CA-07.5 Alcance por rol

```gherkin
  Dado que inicié sesión con el rol "Administrador de Sucursal" de "Centro"
  Cuando abro la malla
  Entonces el selector de sucursal muestra "Centro" y no es editable
  Y no puedo consultar las mallas de "Norte" ni de "Sur"
  Y si inicio sesión con el rol "Barista" la opción de malla por sucursal
    no está visible en el menú
```

### RNF aplicables

- RNF-01 (Rendimiento): la malla de horarios de la sucursal se despliega en 2 segundos o menos.
- RNF-02 (Seguridad): solo usuarios autenticados consultan la malla, y cada uno ve la sucursal que su rol le permite.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-05 (Usabilidad): la malla permite identificar fácilmente la fecha y la hora de cada turno.

### Dependencias

- HU-06: sin asignaciones no hay malla que mostrar. Es la dependencia dura de esta historia.
- HU-05 (turnos con duración efectiva) y HU-03 / HU-04 (empleados con rol, sucursal y estado).
- El registro de descansos, vacaciones e incapacidades (SUP-19) alimenta CA-07.2.
- La cobertura mínima por franja alimenta CA-07.3.

### Riesgos

- Si HU-07 se entrega, pero el empleado sigue sin ver su propio horario, el problema de comunicación permanece intacto y el proyecto no cumple su promesa. Mitigación: priorizar la historia de vista del empleado en la misma release.
- Una malla de 20 empleados por 7 días es difícil de leer en un teléfono Mitigación: diseño con desplazamiento horizontal y vista por día en pantallas menores a 768 px.
- Sin cobertura mínima configurada, el indicador de CA-07.3 no puede calcularse y la historia pierde su función de verificación. Mitigación: cerrar SUP-18 antes del sprint.
- Señalar la cobertura solo por color excluye a usuarios con daltonismo. Mitigación: acompañar el color con texto, ya exigido en CA-07.3.

### Datos de prueba

- Sucursal "Centro", semana del 14 al 20 de septiembre de 2026, con 5 empleados activos.
- Ana Pérez con 4 turnos "Apertura" en la semana y descanso el 16/09 → total 30 h 00 min y celda "Descanso".
- Martes 15 sin asignaciones entre 18:00 y 22:00 → indicador de franja sin cobertura.
- Semana del 21/09 en estado "Borrador" y semana del 14/09 "Publicada el 07/09/2026 18:40".
- Luis Gómez, inactivo con turnos en una semana ya publicada → visible en esa semana, ausente en las futuras.
- Carla Ríos, Administrador de Sucursal de "Centro" → selector de sucursal fijo.
- Ana Pérez con rol "Barista" → la opción de malla por sucursal no aparece en su menú.

### Evaluación DoR

**Veredicto: NO LISTA.** Cumplen: formato, valor medible, alcance, 6 criterios verificables, RNF y datos de prueba.

- Bloqueante 1: Sin cobertura mínima definida, CA-07.3 no puede evaluarse y la historia deja de cumplir el "verificar" de la épica.
- Bloqueante 2: depende por completo de HU-06, que a su vez está bloqueada. No es estimable como historia independiente.
- Decisión pendiente: si la vista del empleado sobre su propio horario entra en esta épica o en una posterior.
