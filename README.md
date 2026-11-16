# Análisis de Principios SOLID — Grano Dorado

*Instrumento de apoyo para justificar los principios SOLID dentro del alcance de un producto mínimo viable*

**Uso sugerido:** complete la plantilla durante el análisis del código o diseño y consérvela como evidencia para revisión, retroalimentación o refactorización.

## 1. Información general

| Campo | Detalle |
|---|---|
| **Lenguaje / Framework** | Aplicación: Kotlin Multiplatform + Compose Multiplatform (Android y web). Autenticación y base de datos: Firebase Authentication y Cloud Firestore, a los que la aplicación accede con el SDK de GitLive desde el módulo `shared`. La API REST (Kotlin + Ktor) del C4 sigue como diseño planeado y no existe en el código. |
| **Tipo de análisis** | Revisión del código de la rama `main` (módulo `shared/commonMain` y pruebas), del diagrama de clases del Entregable 1 y del modelo C4 (nivel 3 de la aplicación y nivel 4 de la API). |
| **Contexto** | Gestor de Turnos Laborales — Grano Dorado (ITM): programación, publicación y control de turnos de 45 empleados en 3 sucursales, de 06:00 a 22:00. |

## 2. Objetivo del análisis

Evaluar el cumplimiento de los cinco principios SOLID en el Gestor de Turnos, tomando el código real del MVP (ViewModels, casos de uso, contratos y repositorios, servicios de Firebase y pruebas), el diagrama de clases del Entregable 1 y el modelo C4. Se busca identificar qué responsabilidades conviene ajustar, relacionar cada hallazgo con las reglas de negocio que el código cita (RN-07, RN-10, RN-11, RN-15, RN-16, RN-17, RN-19 y RN-32) y dejar los diagramas coherentes con el código.

**Tipos de evidencia:**

- Diseño (C4 y diagrama de clases): lo que muestran los diagramas y las decisiones de arquitectura. Lo que solo existe en diagramas se rotula como planeado y no se evalúa como cumplido.

- Código (MVP): lo que revisamos en el repositorio, medido con búsquedas sobre la rama `main`: 14 ViewModels, 25 casos de uso, 6 contratos de repositorio con sus 6 implementaciones, `FirestoreService`, `AuthService` y 151 pruebas con 4 repositorios falsos.

## 3. Descripción breve del componente

**Función principal:** Programar, publicar y controlar los turnos de las tres sucursales: empleados, catálogo de turnos, malla semanal con validación de conflictos, consulta de horarios, cambios de turno, novedades, asistencia con incumplimientos y auditoría.

**Responsabilidades actuales:** La aplicación contiene pantallas (Compose), 14 ViewModels, los casos de uso con las reglas de negocio (`domain/usecase`), los contratos de repositorio (`domain/repository`) y su implementación contra Firestore (`data`). No existe una API intermedia: la aplicación lee y escribe directamente en Cloud Firestore y autentica con Firebase Authentication. Las reglas de negocio viven en los casos de uso del módulo compartido.

**Clases / interfaces involucradas:** Presentación y estado: LoginViewModel, MainViewModel, PanelViewModel, EmpleadosViewModel, TurnosViewModel, MallaSemanalViewModel, MiHorarioViewModel, CambiosTurnoViewModel, NovedadesViewModel, ReportarNovedadViewModel, MarcarAsistenciaViewModel, AsistenciaViewModel, AuditoriaViewModel, CambiarContrasenaViewModel y SessionManager. Dominio: 25 casos de uso (por ejemplo ValidarAsignacionTurnoUseCase, ValidarSolicitudCambioTurnoUseCase, ValidarAprobacionCambioTurnoUseCase, IntercambiarTurnosUseCase, ValidarEstadoEmpleadoActivoUseCase, RegistrarEventoAuditoriaUseCase), los contratos EmpleadoRepositoryContract, TurnoRepositoryContract, NovedadRepositoryContract, CambiosTurnoRepositoryContract, AsistenciaRepositoryContract y AuditoriaRepositoryContract, y el modelo (Usuario, Autenticable, Empleado, Administrador, AsignacionTurno, ProgramacionSemanal, SolicitudCambioTurno). Datos: seis repositorios `object`, FirestoreService, AuthService y FirebaseAuthService. Nivel 4 de la API del C4 (diseño planeado, sin código): MotorValidacion y ReglaValidacion, EstadoSolicitud, CanalEnvio y los adaptadores de notificaciones.

**Dependencias principales:** Pantalla → ViewModel → (caso de uso, contrato de repositorio) ← repositorio → FirestoreService → Cloud Firestore. El inicio de sesión va por AuthService → FirebaseAuthService → Firebase Authentication. Los ViewModels también consultan el SessionManager global.

**Relación con otros módulos:** Los casos de uso de asignación y de cambio de turno comparten las reglas de programación (RN-15 a RN-17 y RN-19). Los ViewModels de empleados, turnos, cambios, novedades y asistencia registran eventos en Auditoría (RN-32) mediante RegistrarEventoAuditoriaUseCase.

## 4. Resumen ejecutivo

|                                     |                           |                                                                                                                                                    |
|-------------------------------------|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| **Principio**                       | **Nivel de cumplimiento** | **Observación general**                                                                                                                            |
| S - Single Responsibility Principle | Medio                     | Los casos de uso se cortan por regla de negocio y EmpleadosViewModel ya delega RN-07 y RN-10; pero FirestoreService concentra todas las colecciones, hay ViewModels de 500 a 839 líneas y el modelo conserva métodos de proceso sin uso. |
| O - Open/Closed Principle           | Medio                     | Los resultados `sealed` cierran bien los casos de uso; los límites de 42 h y 12 h se repiten en dos clases, los estados de turno y asignación son texto y el rol se deriva con `when` duplicado. |
| L - Liskov Substitution Principle   | Medio                     | No hay sustitución de usuarios en ejecución, pero Autenticable promete lo que Usuario no cumple y los contratos de repositorio no documentan postcondiciones; el doble de prueba se comporta distinto del real. |
| I - Interface Segregation Principle | Medio                     | Los contratos mezclan lectura, escritura y sincronización: TurnoRepositoryContract tiene 17 miembros y ningún cliente usa más de 5; su doble de prueba solo tiene lógica en 3. |
| D - Dependency Inversion Principle  | Medio                     | Hay 6 contratos, inyección por constructor y dobles en pruebas; pero los valores por defecto apuntan a singletons concretos (41), los contratos nombran a Firestore y SessionManager es global. |

Escala: Cumple = Alto; Cumple parcialmente = Medio; No cumple = Bajo. En la versión corta se usa Sí / Parcial / No.

## Conclusión global del componente

El código ya reparte las reglas de negocio en casos de uso, usa contratos de repositorio con inyección por constructor y tiene pruebas con dobles, lo que favorece SRP y DIP. Los puntos débiles están en el tamaño de los contratos de datos, en la raíz de composición y los nombres atados a Firestore, en las constantes y estados repetidos, y en que el C4 y el diagrama de clases no describen el código actual.

**Nivel general de calidad del diseño:** Medio

**Prioridad de intervención:** Alta (actualizar el C4 y el diagrama de clases, y resolver la raíz de composición y los contratos de datos antes de ampliar el MVP)

## 5. Análisis de Principios SOLID

### 5.1 S – Single Responsibility Principle (SRP)

*Definición. Una clase o módulo debe tener una sola razón para cambiar.*

**Elemento evaluado:** FirestoreService, FirebaseAuthService, los ViewModels más grandes (MallaSemanalViewModel 839 líneas, CambiosTurnoViewModel 608, ReportarNovedadViewModel 580, MarcarAsistenciaViewModel 514), ValidarSolicitudCambioTurnoUseCase (500 líneas), EmpleadosViewModel y las clases Administrador y AsignacionTurno del modelo.

**Evaluación:** Cumple parcialmente.

**Evidencia observada:**

- Código: EmpleadosViewModel delega RN-07 (nadie cambia su propio rol ni estado y siempre queda un Administrador General activo) en ValidarModificacionRolEstadoUseCase, y RN-10 (el Administrador de Sucursal solo gestiona su sucursal) en ValidarPermisosAdminSucursalEmpleadoUseCase. El ViewModel solo orquesta y actualiza el estado de pantalla.

- Código: los casos de uso están cortados por regla de negocio: ValidarEstadoEmpleadoActivoUseCase (RN-11), RegistrarEventoAuditoriaUseCase (RN-32), ControlIntentosLoginUseCase, entre otros.

- Código: FirestoreService es un único `object` con 31 funciones que leen y escriben empleados, turnos, asignaciones, novedades, solicitudes, asistencia y auditoría.

- Código: FirebaseAuthService (381 líneas) mezcla inicio de sesión con correo, inicio con Google, recuperación y cambio de clave, registro de cuenta, control de intentos y la traducción del cargo a RolUsuario.

- Código: MallaSemanalViewModel (839 líneas y 10 referencias a SessionManager) reúne carga de datos, selección de celdas, validación y guardado de la malla.

- Código y diagrama de clases (Entregable 1): Administrador define 9 métodos de proceso (registrarEmpleado, crearTurno, aprobarSolicitud, publicarProgramacion…) y AsignacionTurno.validarDisponibilidad() tiene una regla de una línea. Una búsqueda en el repositorio no encontró llamadas a esos métodos fuera del modelo ni instancias de Administrador; la regla real (RN-17) vive en ValidarAsignacionTurnoUseCase.

**Preguntas guía:**

- ¿La clase tiene una única responsabilidad claramente definida?

- ¿Mezcla lógica de negocio, acceso a datos, validación, presentación o infraestructura?

- ¿Existen múltiples razones de cambio en el mismo artefacto?

- ¿El nombre de la clase refleja una sola intención?

**Hallazgos:**

- Los casos de uso cumplen SRP: cada uno responde a una regla de negocio. Las reglas de HU-03 y HU-04 ya no están duplicadas en EmpleadosViewModel.

- FirestoreService tiene una razón de cambio por cada colección: un cambio de esquema en cualquiera obliga a editar el mismo archivo.

- Hay dos fuentes de verdad para una misma regla: el método del modelo (validarDisponibilidad, Administrador.*) y el caso de uso. Son métodos sin llamadas que además contradicen el rol como dato que usa el código.

- Los ViewModels de más de 500 líneas mezclan varias responsabilidades y son candidatos a extraer casos de uso, como ya se hizo en EmpleadosViewModel.

**Impacto técnico potencial:**

- Un cambio en el esquema de una colección toca FirestoreService y puede afectar por error a las demás.

- Quien lea el diagrama de clases creerá que el administrador ejecuta los procesos, cuando los ejecutan los casos de uso.

- Los ViewModels grandes son más difíciles de probar y de modificar.

**Recomendaciones:**

- Dividir FirestoreService en fuentes de datos por colección (por ejemplo EmpleadosRemoteDataSource y TurnosRemoteDataSource) o moverlas a sus repositorios.

- Retirar del modelo los métodos de proceso sin uso (y quitar esas operaciones del diagrama de clases), de modo que los procesos queden solo en los casos de uso.

- Extraer de FirebaseAuthService el mapeo de cargo a rol.

- Indicadores de verificación: FirestoreService sin funciones de más de una colección; ningún método de proceso del modelo sin llamadas.

### 5.2 O – Open Closed Principle (OCP)

*Definición. Las entidades de software deben estar abiertas para extensión, pero cerradas para modificación.*

**Elemento evaluado:** ValidarAsignacionTurnoUseCase, ValidarSolicitudCambioTurnoUseCase, SolicitudCambioTurno y EstadoSolicitudCambio, AsignacionTurno.estado, TurnoTemplate.estado y el mapeo de rol en AuthService.

**Evaluación:** Cumple parcialmente.

**Evidencia observada:**

- Código: los resultados de validación son `sealed interface` (ValidacionAsignacionResult, ValidacionSolicitudCambioResult…): agregar un caso obliga al compilador a revisar cada `when`.

- Código: el máximo semanal (MAXIMO_MINUTOS_SEMANALES = 42 * 60) está definido en ValidarAsignacionTurnoUseCase y de nuevo en ValidarSolicitudCambioTurnoUseCase; el descanso mínimo de 12 h está en la segunda. HU-07 advierte que esos límites no están validados jurídicamente.

- Código: AsignacionTurno.estado y TurnoTemplate.estado son texto ("BORRADOR", "PUBLICADA", "ACTIVO") aunque existen los enums EstadoProgramacion y EstadoTurno; el literal "PUBLICADA" aparece 23 veces.

- Código: el rol se calcula con un `when` sobre el texto del cargo dos veces en AuthService (líneas 345–347 y 367–369), y UsuarioActual.cargo es texto. Además hay tres enumeraciones para lo mismo: Cargo (6 valores, incluye los administradores), RolUsuario (3) y TipoAdministrador (2).

- Código: las transiciones de SolicitudCambioTurno están repartidas: la entidad (aprobar, rechazar, cancelar) asigna el estado sin comprobar el actual, y la guarda vive en `when (solicitud.estado)` dentro de ValidarAprobacionCambioTurnoUseCase y ControlarVencimientoYCancelacionCambioUseCase.

- Diseño (C4, planeado): Chain of Responsibility para las 7 reglas (MotorValidacion, ReglaValidacion) y State para la solicitud (EstadoSolicitud, EstadoFinal). Ninguno existe todavía en el código, por lo que no se cuenta como cumplimiento.

**Preguntas guía:**

- ¿Agregar funcionalidad obliga a modificar código estable?

- ¿Se usan abstracciones, polimorfismo o estrategias de extensión?

- ¿Existen estructuras if/switch crecientes basadas en tipos o casos?

- ¿El diseño favorece agregar comportamientos sin romper lo ya construido?

**Hallazgos:**

- Ajustar un límite legal obliga a editar dos clases y puede dejarlas con valores distintos.

- Un estado nuevo en EstadoSolicitudCambio obliga a revisar al menos tres lugares sin ayuda del compilador, porque las guardas no están agrupadas.

- Comparar estados y cargos como texto permite errores que solo fallan en ejecución.

- Si las reglas de asignación siguen creciendo, una interfaz ReglaAsignacion con una clase por regla (RN-15, RN-16, RN-17 y límite semanal) permitiría agregar reglas sin tocar las existentes. Es la idea de Chain of Responsibility del C4 y solo cuenta como cumplimiento cuando esté implementada.

**Impacto técnico potencial:**

- Un cambio de 42 h a otro valor podría aplicarse en la asignación y no en el intercambio.

- Una transición prohibida, como aprobar una solicitud vencida, depende de que cada llamador consulte la guarda.

- Un cargo o estado mal escrito pasaría la compilación y fallaría en ejecución.

**Recomendaciones:**

- Crear un único objeto de política (PoliticaProgramacion) con MAXIMO_MINUTOS_SEMANALES y DESCANSO_MINIMO_MINUTOS y usarlo en ambos casos de uso.

- Cambiar `estado: String` por EstadoProgramacion y EstadoTurno en AsignacionTurno y TurnoTemplate, con migración del valor guardado en Firestore.

- Reemplazar los dos `when` de AuthService por una sola función RolUsuario.desde(cargo) y usar Cargo en UsuarioActual.

- Llevar la guarda de transición a SolicitudCambioTurno (por ejemplo puedeTransitarA(nuevo)) antes de decidir si hace falta el patrón State.

- Indicadores de verificación: cada constante legal definida una sola vez; cero literales "PUBLICADA" y "BORRADOR" fuera de la capa de mapeo; un solo `when` de cargo a rol.

### 5.3 L – Liskov Substitution Principle (LSP)

*Definición. Los objetos de una subclase deben poder sustituir a los de su clase base sin alterar el comportamiento esperado del sistema.*

**Elemento evaluado:** Autenticable, Usuario, Empleado y Administrador (código y diagrama de clases); los 6 contratos de repositorio frente a su implementación real y su doble de prueba; AuthService y FirebaseAuthService. Las jerarquías del C4 (ReglaValidacion, EstadoSolicitud, CanalEnvio) son diseño planeado y no se pueden evaluar sin código. La diferencia entre usuarios por herencia y usuarios por rol se trata como coherencia del modelo, no como violación de Liskov.

**Evaluación:** Cumple parcialmente.

**Evidencia observada:**

- Código: Autenticable declara iniciarSesion(correo, clave): Boolean, que debería ser verdadero solo con credenciales válidas. Usuario lo implementa comparando el correo y exigiendo estado ACTIVO, e ignora la clave. Empleado y Administrador lo heredan sin sobrescribirlo.

- Código: cambiarClave, actualizarPerfil y cerrarSesion de Usuario tienen el cuerpo vacío (solo un comentario); los dos subtipos los heredan sin efecto.

- Código: no hay clientes de Autenticable ni variables o listas del tipo Usuario. El inicio de sesión real pasa por AuthService, que devuelve AuthResult. Administrador no se instancia: los administradores son Empleado con cargo ADMINISTRADOR_* y el rol se deriva en AuthService.

- Código y pruebas: EmpleadoRepositoryContract.guardarEmpleado actualiza `empleados` en memoria y persiste después en el repositorio real; en FakeEmpleadoRepository tiene el cuerpo vacío y no actualiza la lista.

- Código: el repositorio real de empleados ignora la lista remota cuando llega vacía, y arranca la sincronización en su bloque `init`; el doble no hace ninguna de las dos cosas.

- Código: los contratos tratan los errores de dos maneras: guardarEmpleado, reportarNovedad, crearSolicitud y aceptarPorCompanero devuelven Unit, mientras guardarNovedad, guardarSolicitud y guardarAsignacion devuelven Result<Unit>.

- Código: AuthService tiene una sola implementación y expresa los fallos con AuthResult.Error (intentos, bloqueo, minutos restantes): su contrato es explícito.

- Diagrama de clases (Entregable 1): Usuario es abstracta y de ella heredan Empleado y Administrador; el C4 y el código usan el rol como dato.

**Preguntas guía:**

- ¿Las subclases respetan el contrato de la superclase?

- ¿Alguna subclase lanza excepciones inesperadas o deshabilita comportamientos heredados?

- ¿Se redefinen precondiciones o postcondiciones de manera incompatible?

- ¿El cliente necesita conocer el subtipo concreto para funcionar correctamente?

**Hallazgos:**

- Usuario.iniciarSesion incumple la postcondición que declara Autenticable, y varios métodos heredados no hacen nada. No se manifiesta en ejecución porque nadie sustituye usuarios, pero un cliente futuro que confiara en iniciarSesion recibiría true sin validar la clave.

- Los contratos de repositorio no escriben sus postcondiciones, así que cada implementación las interpreta a su manera: el doble de prueba y el real divergen, y las pruebas pueden pasar con un comportamiento distinto del real.

- La coexistencia de Usuario/Administrador, Cargo, RolUsuario y TipoAdministrador es un problema de coherencia del modelo, no de sustitución: hay que dejar un solo modelo en el código, el diagrama de clases y el C4.

- Decisión pendiente: HU-03 exige que todo empleado pertenezca a exactamente una sucursal, pero HU-07 y HU-08 indican que el Administrador General actúa sobre las tres.

**Impacto técnico potencial:**

- Las pruebas con el doble pueden pasar mientras el comportamiento real es distinto, porque ambos no comparten una especificación.

- Un guardado fallido en Firestore no llega a la pantalla en las operaciones que devuelven Unit.

- Si el modelo de usuarios sigue distinto en el diagrama, el C4 y el código, los documentos no coincidirán con la implementación.

**Recomendaciones:**

- Adoptar el rol como dato, que es lo que usa el código: dejar Usuario solo con atributos y eliminar Autenticable y Administrador del código y del diagrama de clases (o hacer que Autenticable coincida con AuthService).

- Escribir en el KDoc de cada contrato sus postcondiciones (por ejemplo: tras guardarEmpleado, `empleados` contiene al empleado) y unificar los guardados para que devuelvan Result<Unit>.

- Crear una suite de pruebas de contrato reutilizable (por ejemplo RepositoryContractTest) que se ejecute contra el repositorio real, con una fuente de datos simulada, y contra el doble, verificando cada postcondición.

- Cuando se programen las jerarquías del C4, verificar el LSP con pruebas de contrato: los estados frente a las acciones, cada regla con un caso válido y uno inválido, y cada adaptador con un envío exitoso y uno fallido.

- Indicadores de verificación: la misma suite pasa en el real y en el doble; ninguna operación de guardado devuelve Unit; ningún método con cuerpo vacío en el modelo.

### 5.4 I – Interface Segregation Principle (ISP)

*Definición. Los clientes no deben depender de interfaces que no utilizan.*

**Elemento evaluado:** Los contratos EmpleadoRepositoryContract, TurnoRepositoryContract, NovedadRepositoryContract, CambiosTurnoRepositoryContract, AsistenciaRepositoryContract y AuditoriaRepositoryContract, e interfaz AuthService. Se contó, por cada ViewModel y caso de uso que recibe un contrato, qué miembros llama realmente a través de la dependencia inyectada.

**Evaluación:** Cumple parcialmente (No cumple en TurnoRepositoryContract).

**Evidencia observada:**

- Código: TurnoRepositoryContract tiene 17 miembros y 15 clientes (12 ViewModels y 3 casos de uso); cada cliente usa entre 1 y 5.

- Código: EmpleadoRepositoryContract tiene 5 miembros y 17 clientes que usan entre 0 y 3; la mayoría solo lee `empleados` y arranca la sincronización. CambiarContrasenaViewModel y ValidarEstadoEmpleadoActivoUseCase lo reciben, pero no se detectan llamadas a sus miembros.

- Código: NovedadRepositoryContract (8 miembros, 5 clientes que usan de 1 a 5), CambiosTurnoRepositoryContract (10 miembros, 3 clientes que usan de 1 a 8), AsistenciaRepositoryContract (12 miembros, 4 clientes que usan de 1 a 6) y AuditoriaRepositoryContract (5 miembros, 2 clientes que usan 2 cada uno).

- Código: en TurnoRepositoryContract, al menos 6 de los 17 miembros (horarioSemana, filasMalla, refrescarDesdeFirestore, forzarSincronizacionCatalogo, agregarTurnoCatalogo y getTurnoHoy) no tienen ninguna referencia fuera del repositorio y del contrato.

- Código: cada contrato mezcla lectura (`StateFlow`), escritura (`guardar…`) y ciclo de vida de la sincronización (iniciarSincronizacionFirestore y detenerSincronizacion). El único cliente de detenerSincronizacion en los seis contratos es MainViewModel.

- Código: AsistenciaRepositoryContract.registrarMarcaSupervisor recibe 12 parámetros y solo lo usa AsistenciaViewModel.

- Código: AuthService tiene 7 miembros y 4 ViewModels que usan de 1 a 3 (inicio de sesión, cambio de clave, registro de cuenta y cierre de sesión).

- Código: los tres casos de uso que usan TurnoRepositoryContract (AplicarPermisoEnTurnoUseCase, AplicarIncapacidadEnTurnoUseCase e IntercambiarTurnosUseCase) solo necesitan leer `asignaciones` y guardarlas.

- Pruebas: FakeTurnoRepository debe implementar los 17 miembros y solo 3 tienen lógica (asignaciones, guardarAsignacion y guardarAsignaciones); los otros 14 son vacíos o devuelven valores fijos.

- Diseño (C4, planeado): CanalEnvio tiene una sola operación y su único cliente la usa completa; no está en el código.

**Preguntas guía:**

- ¿Las interfaces obligan a implementar métodos irrelevantes?

- ¿Existen interfaces demasiado amplias o genéricas?

- ¿Los clientes consumen solo una parte mínima de la interfaz?

- ¿Se podrían dividir los contratos en interfaces más cohesionadas?

**Hallazgos:**

- Los clientes dependen de operaciones que no usan: ninguno consume más del 30 % de TurnoRepositoryContract, y los dobles de prueba pagan el costo implementando 14 miembros sin lógica.

- El ciclo de vida de la sincronización es una responsabilidad distinta de leer y escribir datos, y se arrastra en todos los contratos.

- Los 12 parámetros de registrarMarcaSupervisor piden un objeto de datos.

- Los contratos de Auditoría y de Cambios de turno son razonablemente cohesionados para sus clientes principales.

**Impacto técnico potencial:**

- Cada cambio en una operación usada por un solo cliente obliga a revisar todas las implementaciones y los dobles.

- Los casos de uso quedan acoplados a operaciones de catálogo que no necesitan.

- Un módulo puede leer o escribir datos de otro sin que el contrato lo limite.

**Recomendaciones:**

- Dividir TurnoRepositoryContract en CatalogoTurnosRepositorio (turnosCatalogo, guardarTurnoCatalogo, alternarEstadoTurno), AsignacionesRepositorio (asignaciones, guardarAsignacion, guardarAsignaciones, guardarAsignacionesEnBackground) y una interfaz Sincronizable (iniciarSincronizacion, detenerSincronizacion); retirar los miembros sin referencias.

- Aplicar el mismo criterio al resto de contratos: separar lectura, escritura y sincronización y retirar los miembros sin clientes.

- Reemplazar los 12 parámetros de registrarMarcaSupervisor por un objeto MarcaSupervisor.

- Separar AuthService por funcionalidad (por ejemplo InicioSesion y GestionClave) solo si el equipo lo necesita, para no caer en sobreingeniería.

- Indicadores de verificación: el doble de cada interfaz tiene lógica en la mayoría de sus miembros; ningún cliente recibe una interfaz de la que use menos de la mitad.

### 5.5 D – Dependency Inversion Principle (DIP)

*Definición. Los módulos de alto nivel no deben depender de módulos de bajo nivel; ambos deben depender de abstracciones.*

**Elemento evaluado:** ViewModels y casos de uso frente a los contratos; repositorios frente a FirestoreService; AuthService; SessionManager y la raíz de composición (App.kt).

**Evaluación:** Cumple parcialmente.

**Evidencia observada:**

- Código: existen 6 contratos en `domain/repository` y los ViewModels y casos de uso los reciben por constructor (EmpleadosViewModel recibe EmpleadoRepositoryContract y TurnoRepositoryContract). AuthService es una interfaz. Los relojes se inyectan como función (relojMillis en ControlInactividadSesionUseCase y ControlIntentosLoginUseCase).

- Pruebas: hay dobles para 4 de los 6 contratos (FakeEmpleadoRepository, FakeTurnoRepository, FakeNovedadRepository y FakeAuditoriaRepository); no existen para CambiosTurno ni para Asistencia.

- Código: los 14 ViewModels declaran 41 valores por defecto que apuntan a clases o singletons concretos (por ejemplo EmpleadoRepository y FirebaseAuthService()) y por eso importan `data.repository` y `data.service`. App.kt crea los ViewModels con `viewModel { LoginViewModel() }`, así que la raíz de composición son esos valores por defecto.

- Código: los seis repositorios son `object` y llaman a FirestoreService, otro `object`, sin una interfaz de fuente de datos; además arrancan la sincronización en su bloque `init`.

- Código: los contratos filtran el detalle: iniciarSincronizacionFirestore y refrescarDesdeFirestore nombran a Firestore dentro de la abstracción.

- Código: SessionManager es un `object` global con 58 referencias repartidas en 11 ViewModels; el estado de la sesión no se inyecta.

- Diseño (C4): el nivel 2 muestra una API REST que no existe en el código y el nivel 3 marca la Capa de datos, la Sesión y la Asistencia como planeadas cuando ya están programadas; ningún diagrama muestra los contratos ni las dependencias reales. El diagrama de clases tampoco los refleja.

**Preguntas guía:**

- ¿La lógica de negocio depende directamente de implementaciones concretas?

- ¿Se inyectan dependencias o se crean con new dentro del componente?

- ¿Existen interfaces o abstracciones desacopladoras?

- ¿El componente puede probarse fácilmente con mocks, stubs o fakes?

**Hallazgos:**

- La dirección de las dependencias hacia los contratos es correcta, pero la raíz de composición y los valores por defecto concretos la anulan: el ViewModel (alto nivel) conoce la implementación (bajo nivel).

- Los repositorios no se pueden probar sin Firebase porque no hay una abstracción para FirestoreService.

- Los nombres de los contratos atados a Firestore hacen que otra implementación tenga que simular a Firestore.

- SessionManager es un detalle compartido del que dependen los ViewModels.

- El C4 y el diagrama de clases no reflejan estos contratos ni la arquitectura real de la aplicación.

**Impacto técnico potencial:**

- Cambiar de fuente de datos o probar los repositorios exigiría modificar los 6 `object` y las 14 clases de ViewModel.

- Los ViewModels que usan SessionManager no pueden probarse con sesiones distintas sin tocar estado global.

- Los diagramas no servirán como evidencia de las decisiones de diseño si no coinciden con el código.

**Recomendaciones:**

- Crear una raíz de composición (AppContainer, o Koin como ya planeaba el C4) que construya repositorios y casos de uso y los entregue a los ViewModels con una fábrica; quitar los valores por defecto concretos.

- Definir una interfaz de fuente de datos por colección (XRemoteDataSource) con implementación Firestore y que cada repositorio la reciba por constructor.

- Renombrar los miembros de los contratos sin mencionar Firestore (iniciarSincronizacion y refrescar).

- Reemplazar SessionManager por una interfaz SesionUsuario con un `StateFlow<UsuarioActual?>` inyectable.

- Completar los dobles de CambiosTurno y Asistencia.

- Reflejar el DIP en los diagramas: en el C4 nivel 3 mostrar los contratos del dominio, los repositorios que los implementan, la raíz de composición y los casos de uso; en el diagrama de clases mostrar las interfaces de repositorio con sus implementaciones y sus clientes.

- Indicadores de verificación: ningún `import` de `data.repository` o `data.service` en `viewmodel/`; cero valores por defecto concretos; un repositorio probado con una fuente de datos falsa.

## 6. Hallazgos transversales

**Problemas recurrentes identificados:**

- El C4, el diagrama de clases del Entregable 1 y el código describen cosas distintas: el C4 muestra una API REST en Ktor que no existe, marca como planeadas la Capa de datos, la Sesión y la Asistencia ya programadas, y omite los casos de uso, la Auditoría y AuthService; el diagrama de clases conserva Administrador, Autenticable y AsignacionTurno.validarDisponibilidad, que el código no usa, y tiene catálogos distintos de Cargo, rol y EstadoSolicitudCambio (5 valores en el diagrama, 6 en el código y 8 estados en el C4). Hay que dejarlos coherentes con el código.

- El acceso a datos sigue siendo el punto más débil: contratos anchos que mezclan lectura, escritura y sincronización, nombres atados a Firestore y valores por defecto concretos.

- Reglas y constantes dispersas: límites legales duplicados, estados como texto y roles derivados con `when`.

- Ningún patrón GoF del nivel 4 del C4 (Chain of Responsibility, State, Observer, Adapter, Factory Method) está implementado en el código; el análisis no los cuenta como cumplimiento y los rotula como diseño planeado.

- Quedan pendientes el corte HU-07/HU-07b (el código ya aplica RN-15 a RN-17 en la asignación), la historia del registro de disponibilidad y la sucursal del Administrador General.

## 7. Priorización de mejoras

|                                                                                                        |               |               |              |               |                                                                                                                               |
|--------------------------------------------------------------------------------------------------------|---------------|---------------|--------------|---------------|-------------------------------------------------------------------------------------------------------------------------------|
| **Hallazgo**                                                                                           | **Principio** | **Severidad** | **Esfuerzo** | **Prioridad** | **Acción sugerida**                                                                                                           |
| Valores por defecto concretos en los ViewModels y raíz de composición implícita.                       | DIP           | Alta          | Medio        | Alta          | AppContainer (o Koin) y fábrica de ViewModels; quitar los valores por defecto concretos.                                      |
| TurnoRepositoryContract de 17 miembros con sincronización mezclada.                                    | ISP           | Alta          | Medio        | Alta          | Dividir en catálogo, asignaciones y Sincronizable; retirar los miembros sin clientes.                                         |
| C4 y diagrama de clases no coinciden con el código.                                                    | DIP           | Alta          | Bajo         | Alta          | Actualizar el C4 (API planeada, Capa de datos, Sesión, Asistencia, casos de uso) y el diagrama de clases (rol como dato).    |
| Contratos sin postcondiciones; doble distinto del real; errores como Unit.                             | LSP           | Media         | Medio        | Media         | Documentar postcondiciones, usar Result<Unit> y crear una suite de pruebas de contrato.                                       |
| FirestoreService único para todas las colecciones.                                                     | SRP           | Media         | Medio        | Media         | Fuentes de datos por colección detrás de interfaces.                                                                          |
| Límites de 42 h y 12 h duplicados; estados como texto; rol con `when` duplicado.                       | OCP           | Media         | Bajo         | Media         | PoliticaProgramacion, enums y RolUsuario.desde(cargo).                                                                        |
| Usuario, Administrador y Autenticable sin uso y con contrato engañoso.                                 | LSP           | Media         | Bajo         | Media         | Adoptar el rol como dato y limpiar el modelo y el diagrama.                                                                   |
| SessionManager global.                                                                                 | DIP           | Media         | Bajo         | Media         | Interfaz SesionUsuario inyectada.                                                                                             |

## 9. Conclusión

**Síntesis del estado del diseño:** El código organiza las reglas de negocio en casos de uso, usa contratos de repositorio con inyección por constructor y cuenta con pruebas con dobles, lo que favorece SRP y DIP. Los pendientes están en el tamaño de los contratos de datos (ISP), en la raíz de composición y los nombres atados a Firestore (DIP), en las constantes y estados repetidos (OCP) y en que los diagramas no describen el código.

**Principios más comprometidos:** ISP (TurnoRepositoryContract) y DIP (valores por defecto concretos y SessionManager global).

**Principios mejor implementados:** SRP en los casos de uso y en EmpleadosViewModel, y el cierre de resultados con `sealed interface` (OCP).

**Nivel de deuda técnica asociada al diseño:** Media. Los resultados esperados de aplicar las mejoras (menos clases afectadas por un cambio de límite o de esquema, dobles de prueba más pequeños y repositorios probables sin Firebase) deben verificarse con los indicadores de cada principio; no son mejoras ya comprobadas.

**Recomendación final:** Actualizar el C4 y el diagrama de clases para que coincidan con el código; crear la raíz de composición e inyectar los contratos sin valores por defecto concretos; dividir los contratos de datos por responsabilidad; centralizar los límites y estados en un solo lugar; y resolver las decisiones pendientes (HU-07/HU-07b, historia de disponibilidad y sucursal del Administrador General).

## Versión corta para revisión rápida

|               |                 |                                                                                        |                                                                                                  |                                                    |
|---------------|-----------------|----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|----------------------------------------------------|
| **Principio** | **¿Se cumple?** | **Evidencia**                                                                          | **Problema detectado**                                                                           | **Recomendación**                                  |
| SRP           | Parcial         | Casos de uso por regla; EmpleadosViewModel delega RN-07 y RN-10.                       | FirestoreService con 31 funciones y todas las colecciones; métodos de proceso del modelo sin uso. | Fuentes de datos por colección; limpiar el modelo. |
| OCP           | Parcial         | Resultados `sealed` en los casos de uso.                                               | Límites de 42 h y 12 h repetidos; estados como texto; rol con `when` duplicado.                  | PoliticaProgramacion, enums y RolUsuario.desde.    |
| LSP           | Parcial         | AuthService con contrato explícito.                                                    | Usuario.iniciarSesion ignora la clave; contratos sin postcondiciones; doble distinto del real.   | Rol como dato y pruebas de contrato.               |
| ISP           | Parcial         | TurnoRepositoryContract: 17 miembros y clientes que usan de 1 a 5; doble con 3 de 17.  | Lectura, escritura y sincronización mezcladas.                                                   | Dividir los contratos y retirar miembros sin uso.  |
| DIP           | Parcial         | 6 contratos, inyección por constructor y 4 dobles de prueba.                           | 41 valores por defecto concretos; nombres con Firestore; SessionManager global.                  | AppContainer, fuentes de datos y SesionUsuario.    |
