# Plantilla de análisis y selección de patrones GoF

*Instrumento de apoyo para justificar patrones GoF no repetidos dentro del alcance de un producto mínimo viable*

**Uso sugerido:** llenar primero el alcance del MVP, luego analizar necesidades de diseño reales y finalmente aprobar únicamente los patrones que aporten valor y no generen sobreingeniería.

## 1. Datos generales del proyecto

Nombre del proyecto: Gestor de Turnos Laborales — Grano Dorado (ITM)

Equipo de trabajo: Luis Carlos Marin Mayo - Kevin Stiven Montoya Suarez - Nikole Herrera Ruiz - José Manuel Gutierrez Sosa - Jorge Andrés Carvajal Sepúlveda

Asignatura: Diseño de sistemas de información

Docente: Alexandra Guerrero Bocanegra

Fecha: 12 de octubre de 2026

## 2. Descripción general del proyecto

Problema que resuelve el proyecto: Grano Dorado tiene 3 sucursales, 45 empleados y atiende de 06:00 a 22:00. Los horarios se arman a mano en Excel y se envían por WhatsApp: se programan turnos a personas en descanso o vacaciones, los intercambios se acuerdan de palabra y el administrador se entera a último momento, y no hay registro de asistencia ni de novedades (documento de HU, épicas 1 a 5).

Objetivo general del sistema: Según el Story Map, entregar en tres cortes: Corte 1 (MVP), «se programa la semana y el empleado la ve»; Corte 2, «no se programa a quien no está disponible; el cambio de turno se aprueba antes»; Corte 3, «se sabe qué se trabajó y lo ocurrido en el turno queda escrito».

Descripción del producto propuesto: El Story Map organiza el producto en 9 actividades y 24 pasos para tres tipos de usuario (Administrador General, Administrador de Sucursal y empleado). Recoge las 16 HU del documento (HU-01 a HU-17, sin HU-05) más «HU-07b», que separa de HU-07 la validación de conflictos. Según los diagramas C4, el sistema tiene una aplicación web en Kotlin/Wasm + Compose Multiplatform (marcada «EN EL CÓDIGO»), una API REST en Kotlin + Ktor y una base de datos Firebase Cloud Firestore (ambas «PLANEADO»), y usa Gmail para los correos y un canal de avisos por definir. El ZIP no incluye código fuente.

## 3. Alcance del MVP

### 3.1 Funcionalidades incluidas en el MVP

| ID    | Funcionalidad del MVP                                                                     | Prioridad         | Justificación                                                                                  |
|-------|-------------------------------------------------------------------------------------------|-------------------|------------------------------------------------------------------------------------------------|
| HU-01 | Login                                                                                     | Crítica (Corte 1) | Según la HU, bloquea toda la épica y las épicas siguientes.                                    |
| HU-03 | Creación de empleados                                                                     | Crítica (Corte 1) | Sin empleados registrados no hay a quién programar. Incluye el correo de activación (CA-03.1). |
| HU-06 | Creación de turnos                                                                        | Crítica (Corte 1) | HU-07 no puede asignar nada sin un catálogo de turnos.                                         |
| HU-07 | Asignación de turnos (sin la validación de conflictos, que el Story Map separa en HU-07b) | Crítica (Corte 1) | Es el núcleo del producto. La publicación de la semana dispara la notificación al personal.    |
| HU-09 | Ver horario asignado                                                                      | Crítica (Corte 1) | Reemplaza el envío de horarios por WhatsApp y cierra el ciclo mínimo del Corte 1.              |

### 3.2 Restricciones del MVP

- El proyecto debe implementar (número no confirmado: la documentación no fija cuántos son obligatorios; se evaluaron los 5 candidatos establecidos para el proyecto y se seleccionan 3) patrones GoF.

- Los patrones no se pueden repetir.

- Cada patrón debe responder a una necesidad real del sistema.

- La selección debe evitar sobreingeniería.

- Los patrones deben ser viables dentro del tiempo y alcance del MVP.

### 3.3 Funcionalidades fuera del MVP

- Corte 2, «Sin conflictos y con flexibilidad»: HU-02 Recuperación de clave, HU-04 Actualización de empleados, HU-07b Validar conflictos al asignar, HU-08 Malla por sucursal, HU-10 Solicitar cambio de turno y HU-13 Aprobar cambios de turno.

- Corte 3, «Control de la operación real»: HU-15, HU-16 y HU-17 (asistencia e incumplimientos, deseables) y HU-11, HU-14 y HU-12 (novedades, marcadas como historias futuras).

- Dependencia sin historia: el registro de descansos, vacaciones e incapacidades. En el Story Map es un vacío del Corte 2 («Nadie la escribió; de ella dependen los conflictos del Corte 2»), y HU-07 lo declara bloqueante.

- Excluido por las HU: aplicación móvil nativa, autenticación multifactor, carga masiva de empleados, nómina y recargos, turnos por sucursal o que cruzan la medianoche, generación automática de la malla, exportación a Excel o PDF y verificación de ubicación.

- Alcance por confirmar: (a) si CA-07.2 a CA-07.5 pertenecen a HU-07 (documento de HU, Corte 1) o a HU-07b (Story Map, Corte 2); (b) el canal de avisos de HU-07 y HU-17; (c) el código de la tarjeta de disponibilidad, rotulada «HU-11» en el Story Map, que coincide con HU-11 Reportar novedades.

Estas exclusiones descartan patrones pensados para esas funciones, como Strategy para recargos o Abstract Factory para turnos por sucursal, y ubican Chain of Responsibility y State en el Corte 2, no en el MVP.

## 4. Matriz global de análisis de patrones del proyecto

Esta matriz se diligencia antes de aprobar patrones, para identificar necesidades reales de diseño a nivel de proyecto.

| Necesidad de diseño detectada                                                                           | Módulo o funcionalidad afectada                                                         | Tipo de problema | Patrón candidato                        | ¿Aporta valor al MVP?                                                                          | ¿Se solapa con otro patrón?                                                |
|---------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|------------------|-----------------------------------------|------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| Avisar a varios interesados cuando la solicitud de cambio cambia de estado                              | Cambios de turno (HU-10, HU-13 · Corte 2)                                               | Comportamiento   | Observer                                | No en el Corte 1. En el Corte 2, beneficio dudoso: dos reacciones fijas. Preliminar: pendiente | Puede duplicar lo que ya hace una llamada directa a Notificaciones         |
| La solicitud de cambio tiene 8 estados y la misma acción da resultados distintos según el estado        | Cambios de turno (HU-10, HU-13 · Corte 2)                                               | Comportamiento   | State                                   | No en el Corte 1; sí en el Corte 2. Preliminar: seleccionar para el Corte 2                    | No. Con Observer, State decide la transición y Observer solo la difundiría |
| Siete reglas de conflicto, cada una con su mensaje, en orden y compartidas entre asignar e intercambiar | Motor de validación (HU-07b, HU-10, HU-13 · Corte 2)                                    | Comportamiento   | Chain of Responsibility                 | No en el Corte 1 según el Story Map; sí en el Corte 2. Preliminar: seleccionar para el Corte 2 | No. Observer no permite que una regla detenga a las demás                  |
| Enviar correos y avisos por servicios externos con interfaz propia; el canal de avisos no está definido | Notificaciones (HU-03, HU-07 · Corte 1; HU-02, HU-10, HU-13 · Corte 2; HU-17 · Corte 3) | Estructural      | Adapter                                 | Sí: HU-03 y HU-07 son del Corte 1. Preliminar: seleccionar                                     | No. Adapter resuelve cómo se entrega, no a quién se avisa                  |
| La novedad nace con tipos y estado inicial distintos según la registre el empleado o el supervisor      | Novedades (HU-11, HU-14 · Corte 3, futuras)                                             | Creacional       | Factory Method                          | No: historias futuras y diferencia solo de datos. Preliminar: descartar                        | No                                                                         |
| Calcular horas con recargos nocturnos o dominicales                                                     | Turnos (HU-06)                                                                          | Comportamiento   | Strategy                                | No: recargos fuera de alcance (HU-06)                                                          | Sí, con Chain of Responsibility                                            |
| Plantillas de turnos distintas por sucursal                                                             | Turnos (HU-06)                                                                          | Creacional       | Abstract Factory                        | No: el catálogo de turnos es único (HU-06)                                                     | No                                                                         |
| Estados de Semana (Borrador, Publicada), Novedad e Incumplimiento                                       | Turnos, Novedades, Asistencia                                                           | Comportamiento   | State                                   | No: dos estados y una transición; basta una enumeración                                        | Sí, repetiría State                                                        |
| Inicio de sesión con un proveedor externo de identidad                                                  | Autenticación (HU-01 · Corte 1)                                                         | Estructural      | Adapter                                 | No: Google es el único proveedor en alcance                                                    | Sí, repetiría Adapter                                                      |
| Árbol de interfaz, Modifier, NavHost y LazyColumn de Compose                                            | Aplicación web                                                                          | Varios           | Composite, Decorator, Builder, Iterator | No se cuentan: los aporta el framework                                                         | No                                                                         |

## 5.1 Ficha de análisis del patrón: Observer

### 5.1.1 Identificación general

Nombre del patrón: Observer

Categoría: \[ \] Creacional \[ \] Estructural \[X\] De comportamiento

Módulo, componente o funcionalidad donde se aplicará: Módulo Cambios de turno de la API (HU-10, HU-13; Story Map: actividad 07 «Cambiar un turno», Corte 2). Diagrama: nivel4_cambios_turno.png (marco «Patrón Observer», propuesta)

### 5.1.2 Problema de diseño que se quiere resolver

Cuando una solicitud cambia de estado hay que avisar a personas distintas: al compañero cuando se solicita (CA-10.1), al solicitante cuando el compañero acepta o rechaza (alcance de HU-10), a las dos empleadas cuando se aplica (CA-10.2, CA-13.3), y registrar la decisión con nombre, fecha y hora (CA-13.3). La duda es si esto exige Observer o basta una llamada directa, porque los receptores son pocos, fijos y conocidos de antemano.

### 5.1.3 Evidencias del problema en el proyecto

\[ \] Alto acoplamiento \[ \] Multiplicidad de algoritmos \[ \] Cambio frecuente de comportamiento  
\[ \] Complejidad en la creación de objetos \[X\] Necesidad de desacoplar emisor y receptor  
\[ \] Necesidad de ampliar funcionalidad sin modificar la clase base \[ \] Interfaz incompatible  
\[ \] Otro: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_  
  
Explicación contextualizada en el proyecto: A favor: el aviso se repite en varias transiciones de HU-10 y HU-13. En contra: solo hay dos reacciones (aviso y registro), el registro ya lo guarda la solicitud como CambioDeEstado (ficha de State) y ninguna HU pide agregar o quitar interesados en ejecución.

### 5.1.4 Relación con el MVP

- ¿Por qué este problema sí debe resolverse en el MVP y no en una versión futura? No pertenece al MVP: el Story Map ubica HU-10 y HU-13 en el Corte 2, como historias deseables.

- ¿Qué riesgo habría si no se aplica este patrón en el MVP? Ninguno en el Corte 1. En el Corte 2, sin el patrón, cada transición llama a Notificaciones; el riesgo es olvidar un aviso, y lo cubren las pruebas de los criterios CA-10.x y CA-13.x.

- ¿La implementación del patrón es razonable para el tiempo y complejidad del MVP? \[ \] Sí \[X\] No  
  Justificación: No para el MVP, porque la funcionalidad es del Corte 2. En ese corte sería implementable (una interfaz y dos observadores), pero aporta poco frente a una llamada directa.

### 5.1.5 Análisis de adecuación del patrón

- ¿Qué parte del sistema cambia o podría cambiar? Quién recibe cada aviso. No hay evidencia de reacciones nuevas; el cambio previsto es el canal de avisos (HU-17), y lo absorbe Adapter.

- ¿Qué se desea desacoplar? SolicitudCambio de Notificaciones. Se logra también sin Observer si quien avisa es el servicio SolicitudesCambio y no la entidad.

- ¿Qué ventaja concreta ofrece este patrón en este proyecto? Agregar reacciones sin tocar la entidad; hoy no se aprovecha porque las reacciones son dos y fijas.

- ¿Qué principio de diseño ayuda a cumplir? \[X\] Abierto/Cerrado \[ \] Responsabilidad Única  
  \[X\] Inversión de Dependencias \[ \] Segregación de Interfaces \[ \] Otro: \_\_\_\_\_\_\_\_\_\_\_\_\_\_  
  Justificación técnica: Favorecería agregar reacciones sin modificar el publicador, que solo conocería la interfaz ObservadorSolicitud. Un efecto parecido se logra si SolicitudesCambio depende de la interfaz de Notificaciones.

### 5.1.6 Alternativas consideradas

| Patrón alternativo                                  | ¿Por qué podría confundirse?              | ¿Por qué no es el mejor en este caso?                                                                               |
|-----------------------------------------------------|-------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Llamada directa desde el servicio SolicitudesCambio | Resuelve el aviso sin clases nuevas.      | No es peor: con dos receptores fijos es la opción más simple y la recomendada mientras no aparezcan más reacciones. |
| Chain of Responsibility                             | También pasa un mensaje a varios objetos. | En la cadena un eslabón puede detener a los demás; aquí todos los avisos deben ocurrir siempre.                     |

### 5.1.7 No repetición ni solapamiento

- ¿Este patrón ya fue usado en otra parte del proyecto? \[X\] Sí \[ \] No

- ¿Su propósito funcional se solapa con otro patrón seleccionado? \[ \] Sí \[X\] No

- Si la respuesta anterior fue sí, explique por qué ambos siguen siendo necesarios y no redundantes:

Según el diagrama nivel4_codigo_mvvm_observer.png, la aplicación web usa Observer: NovedadesViewModel expone un StateFlow y NovedadesScreen se suscribe con collectAsState(). Lo aporta la librería kotlinx.coroutines y el ZIP no incluye ese código, así que no es verificable. No se solapa con State: State decide la transición y Observer solo la difundiría.

### 5.1.8 Aplicación concreta al diseño

| Rol del patrón                      | Clase/componente del proyecto                                                                    |
|-------------------------------------|--------------------------------------------------------------------------------------------------|
| Publisher (propuesto en el nivel 4) | PublicadorCambiosSolicitud: suscribir(), desuscribir() y notificar()                             |
| Subscriber (propuesto)              | ObservadorSolicitud.alCambiarEstado(solicitud, cambio)                                           |
| ConcreteSubscriber (propuesto)      | AvisosSolicitud (usa Notificaciones) y RegistroAuditoriaSolicitud (usa Auditoría)                |
| Alternativa recomendada             | SolicitudesCambio llama a ServicioNotificaciones después de cada transición                      |
| Uso documentado en el frontend      | StateFlow en NovedadesViewModel y collectAsState() en NovedadesScreen (no verificable en el ZIP) |

Descripción breve de cómo funcionará en el sistema: Si se aplica, SolicitudCambio.cambiarA() guarda el CambioDeEstado y llama a notificar(), y cada observador reacciona. Es una propuesta del nivel 4 sin código. Mientras se valida, se recomienda la llamada directa.

Fragmento de pseudocódigo, diagrama o referencia UML:

```text
// Propuesta del nivel 4
cambiarA(nuevo, usuario, motivo): historial.agregar(...); publicador.notificar(this, cambio)
// Alternativa recomendada
SolicitudesCambio.aprobar(id): sol.aprobar(admin); notificaciones.avisarSolicitud(sol, destinatarios)
```

UML: diagramas/nivel4_cambios_turno.png (marco «Patrón Observer») y nivel4_codigo_mvvm_observer.png (frontend).

### 5.1.9 Conclusión de decisión

- Decisión final sobre este patrón: \[ \] Se aprueba \[ \] Se descarta \[X\] Pendiente de validación

- Justificación final: Pendiente de validación. El problema existe, pero con dos reacciones fijas no se demuestra que Observer aporte más que una llamada directa. Se confirma o se descarta al planear el Corte 2, según aparezcan más reacciones.

## 5.2 Ficha de análisis del patrón: State

### 5.2.1 Identificación general

Nombre del patrón: State

Categoría: \[ \] Creacional \[ \] Estructural \[X\] De comportamiento

Módulo, componente o funcionalidad donde se aplicará: Módulo Cambios de turno de la API, clase SolicitudCambio (HU-10, HU-13; Story Map: actividad 07, pasos «Proponer intercambio», «Aceptar (compañero)» y «Aprobar (Adm. Sucursal)», Corte 2). Diagrama: nivel4_cambios_turno.png (marco «Patrón State», propuesta)

### 5.2.2 Problema de diseño que se quiere resolver

Según las reglas de negocio de HU-10, la solicitud recorre los estados solicitada, aceptada por el compañero, aprobada y aplicada, y puede terminar en rechazada por el compañero, rechazada por el administrador, cancelada o vencida. La misma acción da resultados distintos: rechazar en Solicitada lleva a «Rechazada por el compañero» (CA-10.3) y, en Aceptada, a «Rechazada por el administrador» (CA-13.4). Una solicitud vencida o ya resuelta no admite acciones (CA-13.6), y solo se cancela mientras no esté aprobada (CA-10.7).

### 5.2.3 Evidencias del problema en el proyecto

\[ \] Alto acoplamiento \[ \] Multiplicidad de algoritmos \[X\] Cambio frecuente de comportamiento  
\[ \] Complejidad en la creación de objetos \[ \] Necesidad de desacoplar emisor y receptor  
\[ \] Necesidad de ampliar funcionalidad sin modificar la clase base \[ \] Interfaz incompatible  
\[X\] Otro: las mismas acciones cambian de efecto según el estado  
  
Explicación contextualizada en el proyecto: Son 6 acciones (aceptar, rechazar, aprobar, aplicar, cancelar y vencer) por 8 estados. Con condicionales, esas reglas quedarían repartidas entre SolicitudesCambio (HU-10) y AprobacionCambios (HU-13), y sería fácil permitir una transición prohibida.

### 5.2.4 Relación con el MVP

- ¿Por qué este problema sí debe resolverse en el MVP y no en una versión futura? No pertenece al Corte 1. El Story Map ubica HU-10 y HU-13 en el Corte 2, y el patrón se diseña para esa entrega.

- ¿Qué riesgo habría si no se aplica este patrón en el MVP? Ninguno en el Corte 1. En el Corte 2, sin el patrón, se podría aprobar una solicitud vencida o sin la aceptación del compañero y cambiar horarios sin control, que es el problema del caso.

- ¿La implementación del patrón es razonable para el tiempo y complejidad del MVP? \[X\] Sí \[ \] No  
  Justificación: Sí, para el Corte 2: una interfaz sellada y un objeto pequeño por estado. Las transiciones ya están escritas en HU-10 y HU-13.

### 5.2.5 Análisis de adecuación del patrón

- ¿Qué parte del sistema cambia o podría cambiar? Los estados y las acciones permitidas. HU-10 deja abierto quién aprueba en ausencia del administrador, lo que podría agregar una transición.

- ¿Qué se desea desacoplar? Los servicios SolicitudesCambio y AprobacionCambios de las reglas propias de cada estado.

- ¿Qué ventaja concreta ofrece este patrón en este proyecto? Cada estado declara qué acciones acepta y a qué estado lleva; los cinco estados terminales comparten en EstadoFinal la regla de no permitir ninguna acción.

- ¿Qué principio de diseño ayuda a cumplir? \[X\] Abierto/Cerrado \[X\] Responsabilidad Única  
  \[ \] Inversión de Dependencias \[ \] Segregación de Interfaces \[ \] Otro: \_\_\_\_\_\_\_\_\_\_\_\_\_\_  
  Justificación técnica: Un estado nuevo es una clase nueva que no modifica las demás, y cada clase de estado tiene una sola razón para cambiar.

### 5.2.6 Alternativas consideradas

| Patrón alternativo          | ¿Por qué podría confundirse?                                 | ¿Por qué no es el mejor en este caso?                                                                                                 |
|-----------------------------|--------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Enumeración y condicionales | Es más simple y funciona con pocos estados.                  | Con 8 estados y 6 acciones, los condicionales se repiten en dos servicios. Sí se usa enumeración en Semana, Novedad e Incumplimiento. |
| Strategy                    | También delega el comportamiento en objetos intercambiables. | En Strategy el cliente elige el algoritmo; aquí el comportamiento cambia solo, según el ciclo de vida de la solicitud.                |

### 5.2.7 No repetición ni solapamiento

- ¿Este patrón ya fue usado en otra parte del proyecto? \[ \] Sí \[X\] No

- ¿Su propósito funcional se solapa con otro patrón seleccionado? \[ \] Sí \[X\] No

- Si la respuesta anterior fue sí, explique por qué ambos siguen siendo necesarios y no redundantes:

No aplica. State no se repite en otros módulos: Semana, Novedad e Incumplimiento tienen dos estados y usan enumeraciones. Chain valida el intercambio antes de aprobarlo (CA-13.5) y State decide si la transición está permitida.

### 5.2.8 Aplicación concreta al diseño

| Rol del patrón             | Clase/componente del proyecto                                                                                   |
|----------------------------|-----------------------------------------------------------------------------------------------------------------|
| Context                    | SolicitudCambio: guarda el estado y delega aceptar, rechazar, aprobar, aplicar, cancelar y vencer               |
| State                      | EstadoSolicitud (sealed interface): por defecto rechaza la acción                                               |
| ConcreteState              | Solicitada, AceptadaPorElCompanero y Aprobada                                                                   |
| ConcreteState (terminales) | EstadoFinal y sus subclases Aplicada, RechazadaPorElCompanero, RechazadaPorElAdministrador, Cancelada y Vencida |
| Clientes                   | SolicitudesCambio (HU-10) y AprobacionCambios (HU-13)                                                           |

Descripción breve de cómo funcionará en el sistema: SolicitudCambio delega cada acción en su estado actual, que devuelve el siguiente estado o rechaza la acción. cambiarA() guarda el nuevo estado y agrega un CambioDeEstado con usuario y fecha-hora (CA-13.3). Es una propuesta del nivel 4; la API no tiene código.

Fragmento de pseudocódigo, diagrama o referencia UML:

```text
SolicitudCambio.rechazar(usuario, motivo):
    estado = estado.rechazar(this, usuario, motivo)
    // Solicitada → RechazadaPorElCompanero · AceptadaPorElCompanero → RechazadaPorElAdministrador
```

UML: diagramas/nivel4_cambios_turno.png (marco «Patrón State»).

### 5.2.9 Conclusión de decisión

- Decisión final sobre este patrón: \[X\] Se aprueba \[ \] Se descarta

- Justificación final: Se aprueba para el Corte 2, no para el MVP. Concentra en un solo lugar las reglas de los 8 estados y evita transiciones que las HU prohíben; su costo (9 clases pequeñas) se justifica por el número de estados.

## 5.3 Ficha de análisis del patrón: Chain of Responsibility

### 5.3.1 Identificación general

Nombre del patrón: Chain of Responsibility

Categoría: \[ \] Creacional \[ \] Estructural \[X\] De comportamiento

Módulo, componente o funcionalidad donde se aplicará: Componente «Motor de validación de conflictos» de la API (HU-07b, HU-10, HU-13; Story Map: actividad 05, paso «Validar conflictos», Corte 2). Diagrama: nivel4_motor_validacion.png (propuesta)

### 5.3.2 Problema de diseño que se quiere resolver

Antes de asignar un turno o aplicar un intercambio hay que comprobar siete condiciones: fecha no pasada, empleado activo y de la sucursal, día disponible, un turno por día, máximo de 42 h semanales, descanso mínimo de 12 h y, en el intercambio, misma sucursal y perfil. Cada una tiene su mensaje (CA-07.2 a CA-07.5, CA-10.4, CA-10.5, CA-13.5) y la primera que falla detiene la operación. Hay una contradicción: el documento de HU incluye estas validaciones en HU-07, mientras que el Story Map las separa en «HU-07b · Validar conflictos al asignar», del Corte 2.

### 5.3.3 Evidencias del problema en el proyecto

\[X\] Alto acoplamiento \[ \] Multiplicidad de algoritmos \[X\] Cambio frecuente de comportamiento  
\[ \] Complejidad en la creación de objetos \[ \] Necesidad de desacoplar emisor y receptor  
\[ \] Necesidad de ampliar funcionalidad sin modificar la clase base \[ \] Interfaz incompatible  
\[X\] Otro: reglas que se combinan distinto al asignar y al intercambiar  
  
Explicación contextualizada en el proyecto: Si las siete reglas viven en un solo método, la clase cambia cada vez que se ajusta un límite; HU-07 advierte que 42 h y 12 h no están validados jurídicamente. Asignar usa seis reglas e intercambiar, cinco.

### 5.3.4 Relación con el MVP

- ¿Por qué este problema sí debe resolverse en el MVP y no en una versión futura? Según el Story Map, el MVP asigna sin validar y la validación llega en el Corte 2, junto con el registro de disponibilidad. Si el equipo mantiene CA-07.2 a CA-07.5 en HU-07, como dice el documento de HU, el patrón entra al MVP.

- ¿Qué riesgo habría si no se aplica este patrón en el MVP? Sin la cadena, reglas duplicadas o contradictorias entre asignar e intercambiar. Que el Corte 1 asigne sin validar es una decisión del Story Map, no del patrón, y deja abierto el problema principal del caso hasta el Corte 2.

- ¿La implementación del patrón es razonable para el tiempo y complejidad del MVP? \[X\] Sí \[ \] No  
  Justificación: Sí: una clase base y una clase pequeña por regla, con reglas y mensajes ya escritos en las HU. ReglaDiaDisponible depende del registro de disponibilidad, que no tiene historia.

### 5.3.5 Análisis de adecuación del patrón

- ¿Qué parte del sistema cambia o podría cambiar? Los límites de horas (42 h y 12 h), el conjunto de reglas y el orden en que se aplican.

- ¿Qué se desea desacoplar? Los servicios que asignan o intercambian (ProgramacionSemanal, SolicitudesCambio, AprobacionCambios) de cada regla concreta.

- ¿Qué ventaja concreta ofrece este patrón en este proyecto? La misma cadena sirve a tres historias. Cada regla devuelve el mensaje exacto de su criterio y se puede agregar, quitar o reordenar sin tocar las demás.

- ¿Qué principio de diseño ayuda a cumplir? \[X\] Abierto/Cerrado \[X\] Responsabilidad Única  
  \[ \] Inversión de Dependencias \[ \] Segregación de Interfaces \[ \] Otro: \_\_\_\_\_\_\_\_\_\_\_\_\_\_  
  Justificación técnica: Una regla nueva es un eslabón nuevo, sin modificar los existentes, y cada eslabón revisa una sola condición.

### 5.3.6 Alternativas consideradas

| Patrón alternativo                 | ¿Por qué podría confundirse?                   | ¿Por qué no es el mejor en este caso?                                                                                  |
|------------------------------------|------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| Strategy                           | Cada regla parece un algoritmo intercambiable. | Strategy elige un solo algoritmo; aquí deben ejecutarse todas las reglas, en orden, hasta que una falle.               |
| Lista de validaciones en un método | Es lo más rápido de escribir.                  | Sirve para una regla o dos; con siete reglas y dos combinaciones distintas, el método crece con cada cambio de límite. |

### 5.3.7 No repetición ni solapamiento

- ¿Este patrón ya fue usado en otra parte del proyecto? \[ \] Sí \[X\] No

- ¿Su propósito funcional se solapa con otro patrón seleccionado? \[ \] Sí \[X\] No

- Si la respuesta anterior fue sí, explique por qué ambos siguen siendo necesarios y no redundantes:

No aplica. La cadena se usa solo en el Motor. Disponibilidad no es un patrón aparte: es la fuente de datos del eslabón ReglaDiaDisponible.

### 5.3.8 Aplicación concreta al diseño

| Rol del patrón       | Clase/componente del proyecto                                                                                                                       |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| Handler              | ReglaValidacion: atributo siguiente, enlazar(), validar(ctx) y revisar(ctx) abstracto                                                               |
| ConcreteHandler      | ReglaFechaNoPasada, ReglaEmpleadoActivo, ReglaDiaDisponible, ReglaUnTurnoPorDia, ReglaMaximoSemanal, ReglaDescansoMinimo y ReglaMismaSucursalPerfil |
| Client               | MotorValidacion: arma cadenaAsignacion y cadenaIntercambio                                                                                          |
| Petición y respuesta | ContextoValidacion → ResultadoValidacion                                                                                                            |
| Usuarios del motor   | ProgramacionSemanal (HU-07b), SolicitudesCambio (HU-10) y AprobacionCambios (HU-13)                                                                 |

Descripción breve de cómo funcionará en el sistema: MotorValidacion carga el contexto (empleado, turno, fecha y asignaciones de la semana) y lo entrega al primer eslabón. Si una regla falla, devuelve el mensaje de la HU y corta; si pasa, entrega el contexto a la siguiente. Es una propuesta del nivel 4.

Fragmento de pseudocódigo, diagrama o referencia UML:

```text
validar(ctx): error = revisar(ctx)
             si hay error → devolver ResultadoValidacion(falso, error)
             si no → siguiente.validar(ctx)  (o válido si no hay siguiente)
```

UML: diagramas/nivel4_motor_validacion.png (Handler con autoasociación «siguiente 0..1» y 7 eslabones).

### 5.3.9 Conclusión de decisión

- Decisión final sobre este patrón: \[X\] Se aprueba \[ \] Se descarta

- Justificación final: Se aprueba para el Corte 2 (HU-07b, HU-10, HU-13). Organiza las reglas que dan valor a la asignación y permite reutilizarlas en el intercambio. Queda por confirmar si la validación pertenece a HU-07 (MVP) o a HU-07b.

## 5.4 Ficha de análisis del patrón: Adapter

### 5.4.1 Identificación general

Nombre del patrón: Adapter

Categoría: \[ \] Creacional \[X\] Estructural \[ \] De comportamiento

Módulo, componente o funcionalidad donde se aplicará: Componente «Notificaciones» de la API (HU-03 y HU-07 en el Corte 1; HU-02, HU-10 y HU-13 en el Corte 2; HU-17 en el Corte 3). Diagrama: nivel4_notificaciones.png (propuesta)

### 5.4.2 Problema de diseño que se quiere resolver

Desde el Corte 1 el sistema envía mensajes por dos servicios externos: el correo de activación (CA-03.1: «se envía 1 correo de activación») y el aviso de la semana publicada (HU-07: la publicación «dispara la notificación»). El correo sale por Gmail API (nivel 3) y el canal de avisos está «por definir» en los niveles 1 y 2; HU-17 lo confirma: «Requiere definir el canal por el que se entrega la notificación». Cada servicio tiene una interfaz distinta del mensaje del sistema.

### 5.4.3 Evidencias del problema en el proyecto

\[X\] Alto acoplamiento \[ \] Multiplicidad de algoritmos \[ \] Cambio frecuente de comportamiento  
\[ \] Complejidad en la creación de objetos \[ \] Necesidad de desacoplar emisor y receptor  
\[ \] Necesidad de ampliar funcionalidad sin modificar la clase base \[X\] Interfaz incompatible  
\[X\] Otro: canal de avisos sin definir (niveles 1 y 2, HU-17)  
  
Explicación contextualizada en el proyecto: Si los componentes llaman directamente a Gmail API, decidir el canal de avisos obliga a modificarlos todos y las pruebas enviarían correos reales. Además, el nivel 2 rotula el correo como «SMTP / TLS» y el nivel 3 como «Gmail API · HTTPS»; el adaptador aísla esa decisión pendiente.

### 5.4.4 Relación con el MVP

- ¿Por qué este problema sí debe resolverse en el MVP y no en una versión futura? Es el único de los cinco con historias del Corte 1: HU-03 (correo de activación) y HU-07 (aviso de publicación), críticas en el Story Map.

- ¿Qué riesgo habría si no se aplica este patrón en el MVP? Atar HU-03 y HU-07 a un proveedor y reescribirlas cuando se elija el canal de avisos o se resuelva la diferencia entre SMTP y Gmail API.

- ¿La implementación del patrón es razonable para el tiempo y complejidad del MVP? \[X\] Sí \[ \] No  
  Justificación: Sí: una interfaz (CanalEnvio) y un adaptador por servicio. El MVP arranca con AdaptadorCorreo, y AdaptadorAvisos se agrega cuando se defina el canal.

### 5.4.5 Análisis de adecuación del patrón

- ¿Qué parte del sistema cambia o podría cambiar? El canal de avisos, sin definir, y el protocolo de correo (SMTP en el nivel 2, Gmail API en el nivel 3).

- ¿Qué se desea desacoplar? Los componentes que avisan de los servicios externos concretos (Gmail API y el canal de avisos).

- ¿Qué ventaja concreta ofrece este patrón en este proyecto? Los componentes llaman a operaciones de dominio de ServicioNotificaciones; elegir el canal solo agrega un adaptador, y en pruebas se usa uno falso.

- ¿Qué principio de diseño ayuda a cumplir? \[X\] Abierto/Cerrado \[ \] Responsabilidad Única  
  \[X\] Inversión de Dependencias \[ \] Segregación de Interfaces \[ \] Otro: \_\_\_\_\_\_\_\_\_\_\_\_\_\_  
  Justificación técnica: ServicioNotificaciones depende de la interfaz CanalEnvio y no del servicio externo; un canal nuevo es una clase nueva.

### 5.4.6 Alternativas consideradas

| Patrón alternativo                | ¿Por qué podría confundirse?                     | ¿Por qué no es el mejor en este caso?                                                                                    |
|-----------------------------------|--------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| Integración directa con Gmail API | Es más rápida de escribir para un solo servicio. | Ya en el Corte 1 hay dos tipos de mensaje y un canal sin definir; la integración directa ataría los componentes a Gmail. |
| Facade                            | También simplifica el acceso a servicios.        | Facade simplifica un subsistema propio; aquí hay que traducir hacia interfaces externas distintas e intercambiables.     |

### 5.4.7 No repetición ni solapamiento

- ¿Este patrón ya fue usado en otra parte del proyecto? \[ \] Sí \[X\] No

- ¿Su propósito funcional se solapa con otro patrón seleccionado? \[ \] Sí \[X\] No

- Si la respuesta anterior fue sí, explique por qué ambos siguen siendo necesarios y no redundantes:

No aplica. Se evaluó un adaptador para el inicio de sesión con Google (HU-01) y se descartó porque es el único proveedor en alcance. Adapter resuelve cómo se entrega un mensaje; no decide a quién avisar.

### 5.4.8 Aplicación concreta al diseño

| Rol del patrón | Clase/componente del proyecto                                                       |
|----------------|-------------------------------------------------------------------------------------|
| Client         | ServicioNotificaciones: redacta el Mensaje y lo envía por canalCorreo o canalAvisos |
| Target         | CanalEnvio.enviar(mensaje): ResultadoEnvio                                          |
| Adapter        | AdaptadorCorreo (MVP) y AdaptadorAvisos (cuando se defina el canal)                 |
| Adaptee        | ClienteProveedorCorreo (Gmail API) y ServicioDeAvisos (canal por definir)           |

Descripción breve de cómo funcionará en el sistema: Un componente llama a una operación de dominio (por ejemplo, enviarCorreoActivacion). ServicioNotificaciones arma el Mensaje y llama a canal.enviar(); el adaptador lo traduce a la operación del servicio externo. Es una propuesta del nivel 4.

Fragmento de pseudocódigo, diagrama o referencia UML:

```text
ServicioNotificaciones.enviarCorreoActivacion(correo, enlace):
    canalCorreo.enviar(Mensaje(correo, asunto, texto))      // CA-03.1
AdaptadorCorreo.enviar(mensaje) → operación de envío de Gmail API
```

UML: diagramas/nivel4_notificaciones.png (marco «Patrón Adapter»).

### 5.4.9 Conclusión de decisión

- Decisión final sobre este patrón: \[X\] Se aprueba \[ \] Se descarta

- Justificación final: Se aprueba para el MVP (Corte 1). Aísla HU-03 y HU-07 del proveedor de correo y del canal de avisos todavía sin definir, con una interfaz y un adaptador por servicio.

## 5.5 Ficha de análisis del patrón: Factory Method

### 5.5.1 Identificación general

Nombre del patrón: Factory Method

Categoría: \[X\] Creacional \[ \] Estructural \[ \] De comportamiento

Módulo, componente o funcionalidad donde se aplicará: Módulo Novedades de la API (HU-11, HU-14; Story Map: actividad 09 «Reportar y revisar novedades», Corte 3, historias futuras). Diagrama: nivel4_novedades.png (propuesta)

### 5.5.2 Problema de diseño que se quiere resolver

La novedad del empleado (HU-11) admite salida antes de la hora, ampliación, llegada tarde y otra, y nace Reportada. La del supervisor (HU-14) admite además Ausencia sin hora real, nace Revisada y queda señalada «Registrada por…» (CA-14.1, CA-14.2). La pregunta es si esto exige un patrón creacional: las dos variantes difieren en datos (tipos, estado inicial y autor), no en la forma de construir el objeto.

### 5.5.3 Evidencias del problema en el proyecto

\[ \] Alto acoplamiento \[ \] Multiplicidad de algoritmos \[ \] Cambio frecuente de comportamiento  
\[ \] Complejidad en la creación de objetos \[ \] Necesidad de desacoplar emisor y receptor  
\[ \] Necesidad de ampliar funcionalidad sin modificar la clase base \[ \] Interfaz incompatible  
\[X\] Otro: dos variantes que difieren solo en datos de inicio  
  
Explicación contextualizada en el proyecto: Evidencia: CA-11.1 a CA-11.5 y CA-14.1 a CA-14.6. La creación no tiene pasos complejos ni familias de objetos; basta fijar el estado inicial y validar el tipo según el origen.

### 5.5.4 Relación con el MVP

- ¿Por qué este problema sí debe resolverse en el MVP y no en una versión futura? No: el Story Map marca HU-11, HU-12 y HU-14 como historias futuras del Corte 3, y HU-11 indica que «no bloquea la operación diaria».

- ¿Qué riesgo habría si no se aplica este patrón en el MVP? Ninguno en el MVP. En el Corte 3, crear una novedad del supervisor en estado Reportada; se evita con una función de creación por origen y la prueba de CA-14.1.

- ¿La implementación del patrón es razonable para el tiempo y complejidad del MVP? \[ \] Sí \[X\] No  
  Justificación: No: dos creadores y dos productos más sus clases base para una diferencia de datos es sobreingeniería, y la funcionalidad está fuera del MVP.

### 5.5.5 Análisis de adecuación del patrón

- ¿Qué parte del sistema cambia o podría cambiar? El catálogo de tipos (HU-11 lo deja abierto) y quién puede registrar.

- ¿Qué se desea desacoplar? Los servicios ReporteNovedades y RegistroNovedadSupervisor de la decisión de estado inicial y tipos permitidos.

- ¿Qué ventaja concreta ofrece este patrón en este proyecto? Cada variante nacería con su estado y sus tipos. Esa misma ventaja la da una función de creación por origen, sin jerarquía de creadores.

- ¿Qué principio de diseño ayuda a cumplir? \[X\] Abierto/Cerrado \[ \] Responsabilidad Única  
  \[ \] Inversión de Dependencias \[ \] Segregación de Interfaces \[ \] Otro: \_\_\_\_\_\_\_\_\_\_\_\_\_\_  
  Justificación técnica: Favorecería agregar orígenes de novedad sin modificar los existentes, pero ninguna HU menciona orígenes distintos del empleado y el supervisor.

### 5.5.6 Alternativas consideradas

| Patrón alternativo             | ¿Por qué podría confundirse?                       | ¿Por qué no es el mejor en este caso?                                     |
|--------------------------------|----------------------------------------------------|---------------------------------------------------------------------------|
| Función de creación por origen | Es más simple y también centraliza estado y tipos. | No es peor: lo resuelve en una sola clase. Es la alternativa recomendada. |
| Abstract Factory               | Crea familias de objetos relacionados.             | Solo se crea un producto (Novedad), no una familia.                       |

### 5.5.7 No repetición ni solapamiento

- ¿Este patrón ya fue usado en otra parte del proyecto? \[ \] Sí \[X\] No

- ¿Su propósito funcional se solapa con otro patrón seleccionado? \[ \] Sí \[X\] No

- Si la respuesta anterior fue sí, explique por qué ambos siguen siendo necesarios y no redundantes:

No aplica. Es el único creacional evaluado; al descartarlo no queda ninguno, lo que es coherente con la evidencia.

### 5.5.8 Aplicación concreta al diseño

| Rol del patrón                         | Clase/componente del proyecto                                                                       |
|----------------------------------------|-----------------------------------------------------------------------------------------------------|
| Creator (propuesto, no recomendado)    | CreadorNovedad: registrar() y el método de creación crearNovedad()                                  |
| ConcreteCreator (propuesto)            | CreadorNovedadEmpleado (HU-11) y CreadorNovedadSupervisor (HU-14)                                   |
| Product y ConcreteProduct (propuestos) | Novedad, NovedadDelEmpleado y NovedadDelSupervisor                                                  |
| Alternativa recomendada                | Novedad creada con estado y tipos fijados según su origen; validaciones comunes en un solo servicio |

Descripción breve de cómo funcionará en el sistema: En la propuesta del nivel 4, el servicio llama a creador.registrar(), que valida plazo y horario y delega en crearNovedad(). Se recomienda la alternativa sencilla mientras no aparezcan más orígenes.

Fragmento de pseudocódigo, diagrama o referencia UML:

```text
// Alternativa recomendada (sin patrón)
crearNovedad(origen, datos): estado = si origen = SUPERVISOR → REVISADA, si no → REPORTADA
                             exigir tiposPermitidos(origen).contiene(datos.tipo)
UML de la propuesta descartada: diagramas/nivel4_novedades.png (marco «Patrón Factory Method»).
```

### 5.5.9 Conclusión de decisión

- Decisión final sobre este patrón: \[ \] Se aprueba \[X\] Se descarta

- Justificación final: Se descarta: las historias son futuras (Corte 3) y la diferencia entre variantes es de datos. Se reevalúa en el Corte 3 si aparecen más orígenes o reglas de creación.

## 6. Consolidado final de los patrones seleccionados

| Nº  | Patrón GoF              | Categoría      | Módulo/Funcionalidad                                 | Problema que resuelve                                     | Justificación en el MVP                                                  | ¿Se repite? |
|-----|-------------------------|----------------|------------------------------------------------------|-----------------------------------------------------------|--------------------------------------------------------------------------|-------------|
| 1   | Observer                | Comportamiento | Cambios de turno (HU-10, HU-13 · Corte 2)            | Avisos en cada cambio de estado                           | Pendiente de validación: dos reacciones fijas; basta una llamada directa | No          |
| 2   | State                   | Comportamiento | Cambios de turno (HU-10, HU-13 · Corte 2)            | Acciones permitidas según los 8 estados                   | Seleccionado para el Corte 2, fuera del MVP                              | No          |
| 3   | Chain of Responsibility | Comportamiento | Motor de validación (HU-07b, HU-10, HU-13 · Corte 2) | Siete reglas de conflicto en orden                        | Seleccionado para el Corte 2; confirmar si va en HU-07 (MVP)             | No          |
| 4   | Adapter                 | Estructural    | Notificaciones (HU-03, HU-07 · Corte 1)              | Servicios externos con interfaz propia; canal sin definir | Seleccionado para el MVP                                                 | No          |
| 5   | Factory Method          | Creacional     | Novedades (HU-11, HU-14 · Corte 3)                   | Variantes de novedad según quién registra                 | Descartado: historias futuras y diferencia de datos                      | No          |

## 7. Validación global de consistencia

| Criterio                                                                   | Sí    | No    | Observación                                                                                                                                                          |
|----------------------------------------------------------------------------|-------|-------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Cada patrón responde a un problema real del sistema                        | \[X\] | \[ \] | Sí para los 3 seleccionados (criterios CA-03.1, CA-07.2 a CA-07.5, CA-13.6). El de Observer es real pero débil, y el de Factory Method es de datos.                  |
| Los 3 patrones seleccionados son distintos entre sí                        | \[X\] | \[ \] | Sí. Adapter (estructural), Chain of Responsibility y State (comportamiento) resuelven problemas diferentes.                                                          |
| Ningún patrón fue incluido solo para cumplir con el número                 | \[X\] | \[ \] | Sí. Aunque se establecieron 5 candidatos, Factory Method se descartó y Observer quedó pendiente.                                                                     |
| Todos los patrones caben en el alcance del MVP                             | \[ \] | \[X\] | No. Solo Adapter está en el Corte 1 (MVP). Chain y State se aprueban para el Corte 2, donde el Story Map ubica sus historias.                                        |
| La selección evita sobreingeniería                                         | \[X\] | \[ \] | Sí. State es el más costoso (9 clases), justificado por los 8 estados. Se evitaron Factory Method y Observer.                                                        |
| Existe coherencia entre problema, solución y diseño                        | \[ \] | \[X\] | Parcial. Es coherente en los 3 seleccionados, pero los diagramas de nivel 4 muestran Observer y Factory Method, que este análisis no aprueba: hay que actualizarlos. |
| Existe trazabilidad hacia una historia de usuario o necesidad identificada | \[X\] | \[ \] | Sí. Cada ficha cita HU, criterios y actividad del Story Map. Excepción: el registro de disponibilidad no tiene historia escrita.                                     |
| Cada patrón se ubica razonablemente en la arquitectura C4                  | \[X\] | \[ \] | Sí. Los cinco se ubican en componentes del nivel 3 de la API y en un diagrama de nivel 4.                                                                            |
| Se diferencia implementación existente, diseño documentado y propuesta     | \[X\] | \[ \] | Sí. La API y la base de datos están «PLANEADO» (nivel 2); los patrones son propuestas del nivel 4; el ZIP no trae código.                                            |
| Las decisiones no contradicen las HU ni el alcance documentado             | \[X\] | \[ \] | Sí, con reserva: el documento de HU y el Story Map se contradicen sobre HU-07; la decisión de Chain queda condicionada a esa aclaración.                             |

## 8. Conclusión general del análisis

El Story Map fue la fuente que más cambió el análisis: al definir el Corte 1 como MVP (HU-01, HU-03, HU-06, HU-07 y HU-09), dejó a la mayoría de los problemas de diseño en entregas posteriores. Adapter es el único patrón que conviene aplicar en el MVP, porque el correo de activación y el aviso de publicación dependen de servicios externos con interfaces propias y de un canal todavía sin definir. Chain of Responsibility y State resuelven problemas que las HU documentan con detalle (siete reglas de conflicto y ocho estados de la solicitud), pero se aplican en el Corte 2. Observer queda pendiente, porque dos reacciones fijas se resuelven con una llamada directa, y Factory Method se descarta, porque sus historias son futuras y la diferencia entre variantes es de datos. Los diagramas C4 sirvieron para ubicar cada patrón en un componente (nivel 3) y en sus clases (nivel 4); los patrones GoF no son un nivel del modelo C4, sino decisiones de diseño que se dibujan en el nivel 4. Todos son propuestas: la API está planeada y el ZIP no incluye código fuente, así que ningún patrón puede presentarse como implementado.

## 9. Regla de decisión recomendada

1.  Un patrón solo debe incluirse si resuelve un problema real del sistema.  
    Aplicación: Adapter, Chain y State tienen un problema demostrado con criterios de aceptación (CA-03.1, CA-07.2 a CA-07.5, CA-13.6). Observer quedó pendiente porque una llamada directa resuelve su problema.

2.  Debe ser implementable dentro del alcance del MVP.  
    Aplicación: Adapter es el único del Corte 1. Chain y State se aprueban para el Corte 2, donde el Story Map ubica sus historias, y Factory Method se descarta en parte por pertenecer al Corte 3.

3.  No debe repetir la función de otro patrón ya seleccionado.  
    Aplicación: Adapter entrega, Chain valida y State controla las transiciones. Observer no se aprobó para no duplicar el aviso que ya resuelve Notificaciones; State no se repite en Semana ni Adapter en el inicio con Google.

4.  Debe mejorar el diseño sin añadir complejidad innecesaria.  
    Aplicación: Factory Method se descartó porque cuatro clases para una diferencia de datos no se justifican; donde basta una enumeración o una llamada directa, se usa.

## Anexo. Trazabilidad: Story Map → historia de usuario → componente C4 → patrón GoF

Relación entre las actividades del Story Map (Miro), sus historias y corte de entrega, los componentes de los niveles 3 y 4 del modelo C4 y la decisión sobre cada patrón.

| Actividad del Story Map           | Historias (prioridad · corte)                        | Componente C4 (nivel 3)                                    | Diagrama de nivel 4                           | Patrón GoF                                              | Decisión                                                  |
|-----------------------------------|------------------------------------------------------|------------------------------------------------------------|-----------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------|
| 01 · Acceder                      | HU-01 (crítica · C1), HU-02 (deseable · C2)          | Autenticación; Notificaciones                              | nivel4_autenticacion                          | Adapter (correo de recuperación)                        | Adapter: Corte 2 (HU-02); Adapter para Google, descartado |
| 02 · Administrar el personal      | HU-03 (crítica · C1), HU-04 (deseable · C2)          | Empleados; Notificaciones                                  | nivel4_empleados                              | Adapter (correo de activación)                          | Seleccionado (MVP)                                        |
| 03 · Registrar la disponibilidad  | Vacío rotulado «HU-11», sin historia (C2)            | Disponibilidad                                             | nivel4_disponibilidad                         | Ninguno; alimenta a Chain                               | Pendiente: falta la historia                              |
| 04 · Definir los turnos           | HU-06 (crítica · C1)                                 | Turnos y programación                                      | nivel4_turnos                                 | Ninguno                                                 | Abstract Factory, descartado                              |
| 05 · Programar la semana          | HU-07 (crítica · C1), HU-07b y HU-08 (críticas · C2) | Turnos y programación; Motor de validación; Notificaciones | nivel4_turnos; nivel4_motor_validacion        | Chain of Responsibility; Adapter (aviso de publicación) | Chain: Corte 2; Adapter: MVP                              |
| 06 · Consultar mi horario         | HU-09 (crítica · C1)                                 | Turnos y programación                                      | nivel4_turnos                                 | Ninguno                                                 | No requiere patrón                                        |
| 07 · Cambiar un turno             | HU-10 y HU-13 (deseables · C2)                       | Cambios de turno; Motor de validación; Notificaciones      | nivel4_cambios_turno                          | State; Observer; Chain (reutilizado)                    | State: Corte 2; Observer: pendiente                       |
| 08 · Marcar la asistencia         | HU-15, HU-16 y HU-17 (deseables · C3)                | Asistencia e incumplimientos; Planificador; Notificaciones | nivel4_asistencia; nivel4_planificador        | Adapter (canal de avisos de HU-17)                      | Adapter: Corte 3; State para Incumplimiento, descartado   |
| 09 · Reportar y revisar novedades | HU-11, HU-14 y HU-12 (futuras · C3)                  | Novedades; pantalla Novedades de la app                    | nivel4_novedades; nivel4_codigo_mvvm_observer | Factory Method; Observer (StateFlow)                    | Factory Method, descartado; StateFlow, no verificable     |
