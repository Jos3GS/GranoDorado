# Historias de usuario — Grano Dorado

Conversión a Markdown del documento **«Refinamiento de Historias de Usuario»** (`HU_GranoDorado_Epicas_1a5_Consolidado.docx`), organizada en un archivo por épica.

## Introducción

El documento refina 16 historias de usuario del sistema de turnos de Grano Dorado (HU-01 a HU-16, con numeración consecutiva), agrupadas en 5 épicas. Cada historia se presenta como una ficha con los campos Épica, Nombre HU, Prioridad, Actor, Historia de usuario (formato «Como… quiero… para…»), Contexto, Valor esperado, Alcance, Fuera de alcance, Reglas de negocio, Criterios de aceptación (escenarios Gherkin con identificadores CA-xx.y), RNF aplicables, Dependencias, Riesgos, Datos de prueba y Evaluación DoR. Las historias HU-08 a HU-13 incluyen además el campo Supuestos, y HU-02, Notas de UX. La Épica 5 tiene una sección de contexto propia («Marco de la Épica 5»).

## Índice de épicas

| Épica | Resumen | Nº de historias | Historias de usuario | Archivo |
|---|---|---|---|---|
| Épica 1 — Gestión de usuarios y acceso | Acceso al sistema y administración de empleados. | 4 | HU-01 Login, HU-02 Recuperación de clave, HU-03 Creación de empleados, HU-04 Actualización de empleados | [Abrir archivo](epica-1-gestion-usuarios-acceso.md) |
| Épica 2 — Planificación y asignación de turnos | Catálogo de turnos, asignación con validación de conflictos y horarios por sucursal. | 3 | HU-05 Creación de turnos, HU-06 Asignación de turnos, HU-07 Visualización de horarios por sucursal | [Abrir archivo](epica-2-planificacion-asignacion-turnos.md) |
| Épica 3 — Portal de empleado | Consulta del horario, solicitud de cambios de turno y reporte de novedades por el empleado. | 3 | HU-08 Ver horario asignado, HU-09 Solicitar cambio de turno, HU-10 Reportar novedades | [Abrir archivo](epica-3-portal-empleado.md) |
| Épica 4 — Gestión de novedades | Revisión de novedades, aprobación de cambios de turno y novedades que registra el Administrador de Sucursal. | 3 | HU-11 Ver novedades, HU-12 Aprobar cambios de turno, HU-13 Reportar novedades de empleado | [Abrir archivo](epica-4-gestion-novedades.md) |
| Épica 5 — Control de asistencia | Marcación de asistencia, registro por el supervisor y notificaciones de incumplimientos. | 3 | HU-14 Marcar asistencia al turno, HU-15 Marcar asistencia al turno (registro por el supervisor), HU-16 Notificaciones de incumplimientos | [Abrir archivo](epica-5-control-asistencia.md) |

Los resúmenes de las épicas 1 a 4 se elaboraron a partir de las historias que contienen, porque el documento no incluye una descripción propia de esas épicas. El de la Épica 5 se basa en su Marco.

## Listado de historias de usuario

### [Épica 1 — Gestión de usuarios y acceso](epica-1-gestion-usuarios-acceso.md)

- [HU-01 — Login](epica-1-gestion-usuarios-acceso.md#11-hu-01--login)
- [HU-02 — Recuperación de clave](epica-1-gestion-usuarios-acceso.md#12-hu-02--recuperación-de-clave)
- [HU-03 — Creación de empleados](epica-1-gestion-usuarios-acceso.md#13-hu-03--creación-de-empleados)
- [HU-04 — Actualización de empleados](epica-1-gestion-usuarios-acceso.md#14-hu-04--actualización-de-empleados)

### [Épica 2 — Planificación y asignación de turnos](epica-2-planificacion-asignacion-turnos.md)

- [HU-05 — Creación de turnos](epica-2-planificacion-asignacion-turnos.md#21-hu-05--creación-de-turnos)
- [HU-06 — Asignación de turnos](epica-2-planificacion-asignacion-turnos.md#22-hu-06--asignación-de-turnos)
- [HU-07 — Visualización de horarios por sucursal](epica-2-planificacion-asignacion-turnos.md#23-hu-07--visualización-de-horarios-por-sucursal)

### [Épica 3 — Portal de empleado](epica-3-portal-empleado.md)

- [HU-08 — Ver horario asignado](epica-3-portal-empleado.md#31-hu-08--ver-horario-asignado)
- [HU-09 — Solicitar cambio de turno](epica-3-portal-empleado.md#32-hu-09--solicitar-cambio-de-turno)
- [HU-10 — Reportar novedades](epica-3-portal-empleado.md#33-hu-10--reportar-novedades)

### [Épica 4 — Gestión de novedades](epica-4-gestion-novedades.md)

- [HU-11 — Ver novedades](epica-4-gestion-novedades.md#41-hu-11--ver-novedades)
- [HU-12 — Aprobar cambios de turno](epica-4-gestion-novedades.md#42-hu-12--aprobar-cambios-de-turno)
- [HU-13 — Reportar novedades de empleado](epica-4-gestion-novedades.md#43-hu-13--reportar-novedades-de-empleado)

### [Épica 5 — Control de asistencia](epica-5-control-asistencia.md)

- [Marco de la Épica 5](epica-5-control-asistencia.md#51-marco-de-la-épica-5) — contexto de la épica
- [HU-14 — Marcar asistencia al turno](epica-5-control-asistencia.md#52-hu-14--marcar-asistencia-al-turno)
- [HU-15 — Marcar asistencia al turno (registro por el supervisor)](epica-5-control-asistencia.md#53-hu-15--marcar-asistencia-al-turno-registro-por-el-supervisor)
- [HU-16 — Notificaciones de incumplimientos](epica-5-control-asistencia.md#54-hu-16--notificaciones-de-incumplimientos)

## Tabla de contenido del documento original

| ÉPICA | HISTORIAS QUE CONTIENE |
|---|---|
| Épica 1 — Gestión de usuarios y acceso | HU-01 Login · HU-02 Recuperación de clave · HU-03 Creación de empleados · HU-04 Actualización de empleados |
| Épica 2 — Planificación y asignación de turnos | HU-05 Creación de turnos · HU-06 Asignación de turnos · HU-07 Visualización de horarios por sucursal |
| Épica 3 — Portal de empleado | HU-08 Ver horario asignado · HU-09 Solicitar cambio de turno · HU-10 Reportar novedades |
| Épica 4 — Gestión de novedades | HU-11 Ver novedades · HU-12 Aprobar cambios de turno · HU-13 Reportar novedades de empleado |
| Épica 5 — Control de asistencia | HU-14 Marcar asistencia · HU-15 Marcar asistencia (supervisor) · HU-16 Notificaciones de incumplimientos |

## Estructura de la carpeta

```text
GranoDorado-Historias-Usuario/
├── README.md
├── epica-1-gestion-usuarios-acceso.md
├── epica-2-planificacion-asignacion-turnos.md
├── epica-3-portal-empleado.md
├── epica-4-gestion-novedades.md
├── epica-5-control-asistencia.md
└── observaciones.md
```

## Renumeración de las historias

El documento original (`HU_GranoDorado_Epicas_1a5_Consolidado.docx`) numeraba las historias de HU-01 a HU-17 sin HU-05. En estos archivos la numeración es consecutiva, de HU-01 a HU-16, y los identificadores de los criterios de aceptación siguen a su historia. Los números de sección (1.1, 2.1…) no cambian. HU-01 a HU-04 y sus criterios conservan su identificador. La única mención de la numeración anterior que se conserva dentro de las épicas es el rótulo literal de la ficha de HU-16 en el documento recibido.

| Numeración anterior | Numeración actual | Criterios de aceptación |
|---|---|---|
| HU-06 | HU-05 | CA-06.x → CA-05.x |
| HU-07 | HU-06 | CA-07.x → CA-06.x |
| HU-08 | HU-07 | CA-08.x → CA-07.x |
| HU-09 | HU-08 | CA-09.x → CA-08.x |
| HU-10 | HU-09 | CA-10.x → CA-09.x |
| HU-11 | HU-10 | CA-11.x → CA-10.x |
| HU-12 | HU-11 | CA-12.x → CA-11.x |
| HU-13 | HU-12 | CA-13.x → CA-12.x |
| HU-14 | HU-13 | CA-14.x → CA-13.x |
| HU-15 | HU-14 | CA-15.x → CA-14.x |
| HU-16 | HU-15 | CA-16.x → CA-15.x |
| HU-17 | HU-16 | CA-17.x → CA-16.x |

## Notas de la conversión

- El contenido de cada historia se conserva completo y en el orden original; cada fila de la ficha de Word es un apartado `###`.
- Los criterios de aceptación se presentan con un subtítulo por escenario (`####`) y sus pasos en un bloque `gherkin`, que conserva la sangría y los saltos de línea del original. Las tablas de datos y de ejemplos de los escenarios se escriben como tablas Gherkin dentro del bloque.
- Se conservan los números de sección del documento (1.1, 1.2…) en los títulos de las historias.
- Las inconsistencias encontradas en el documento se registran en [observaciones.md](observaciones.md), sin corregir la fuente.
