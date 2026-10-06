# **Refinamiento de Historias de Usuario**

# **Contenido**

| ÉPICA | HISTORIAS QUE CONTIENE |
| :---- | :---- |
| **Épica 1 — Gestión de usuarios y acceso** | HU-01 Login · HU-02 Recuperación de clave · HU-03 Creación de empleados · HU-04 Actualización de empleados |
| **Épica 2 — Planificación y asignación de turnos** | HU-06 Creación de turnos · HU-07 Asignación de turnos · HU-08 Visualización de horarios por sucursal |
| **Épica 3 — Portal de empleado** | HU-09 Ver horario asignado · HU-10 Solicitar cambio de turno · HU-11 Reportar novedades |
| **Épica 4 — Gestión de novedades** | HU-12 Ver novedades · HU-13 Aprobar cambios de turno · HU-14 Reportar novedades de empleado |
| **Épica 5 — Control de asistencia** | HU-15 Marcar asistencia · HU-16 Marcar asistencia (supervisor) · HU-17 Notificaciones de incumplimientos |

# **Épica 1 — Gestión de usuarios y acceso**

# **1.1 HU-01 — Login**

| Campo | Detalle |
| :---- | :---- |
| **HU-01** | Login |
| **Épica** | Épica 1 — Gestión de usuarios y acceso |
| **Nombre HU** | Login |
| **Prioridad** | Bloquea toda la épica y las épicas siguientes. |
| **Actor** | Administrador General, Administrador de Sucursal, Cajero, Barista, Cocinero. |
| **Historia de usuario** | **Como** usuario registrado de Grano Dorado, **quiero** autenticarme con mi correo electrónico y mi contraseña, **para** acceder únicamente a las funciones y a la información de la sucursal que corresponden a mi rol. |
| **Contexto** | Hoy no existe control de acceso: los horarios se publican en un grupo de WhatsApp donde todos ven todo y los mensajes se pierden. Sin identidad individual verificada no se puede saber quién consultó su turno ni restringir la programación al personal autorizado. El login es la puerta de entrada del sistema y precondición de HU-02, HU-03 y HU-04. |
| **Valor esperado** | \* Habilita el reemplazo del canal WhatsApp por un acceso individual, atacando la causa "el mensaje se perdió / leyeron mal el Excel".\* El 100 % de los 45 empleados inicia sesión al menos una vez en las 2 primeras semanas de producción. |
| **Alcance** | \* Pantalla de inicio de sesión con los campos Correo electrónico y Contraseña.\* Validación de credenciales y del estado de la cuenta (Activo / Inactivo).\* Redirección y menú según el rol, conforme a la matriz de permisos.\* Bloqueo temporal por intentos fallidos y cierre de sesión manual y por inactividad.\* Registro en historial de intentos exitosos y fallidos.\* Enlace "¿Olvidaste tu clave?" que deriva a HU-02.\* Inicio de sesión con proveedores externos (Solo Google). |
| **Fuera de alcance** | \* Autenticación multifactor y biométrica.\* Inicio de sesión con proveedores externos (Microsoft, redes sociales).\* Autorregistro de usuarios.\* Recuperación de contraseña.\* Gestión del catálogo de roles y permisos (historia aparte de la misma épica).\* Aplicación móvil nativa (solo web responsiva). |
| **Reglas de negocio** | \* El identificador de acceso es el correo electrónico, único y no sensible a mayúsculas.\* Solo las cuentas en estado Activo pueden iniciar sesión, aunque la contraseña sea correcta.\* Ante credenciales inválidas se muestra siempre el mismo mensaje: "Correo o contraseña incorrectos.", sin indicar cuál falló.\* Tras 5 intentos fallidos consecutivos la cuenta se bloquea 15 minutos; el contador se reinicia con un login exitoso o un cambio de clave.\* La sesión se cierra automáticamente tras 30 minutos sin interacción.\* Los permisos se aplican según la matriz de permisos del Marco común.\* La contraseña se muestra enmascarada, con un control explícito para revelarla.\* Cada intento, exitoso o fallido, se registra con correo, fecha-hora, resultado e IP. |
| **Criterios de aceptación** | Antecedentes:Dado que existe la cuenta "ana.perez@granodorado.com" con rol "Barista", sucursal "Centro" y estado "Activo" Y su contraseña es "Cafe2026x"**CA-01.1 Inicio de sesión exitoso de personal operativo**Cuando ingreso el correo "ana.perez@granodorado.com" y la contraseña "Cafe2026x" Y presiono el botón "Iniciar sesión"Entonces el sistema muestra la pantalla "Mi horario" en 3 segundos o menos Y el menú contiene únicamente "Mi horario" y "Mi perfil" Y el menú NO contiene la opción "Empleados" Y se agrega al historial un registro "LOGIN\_EXITOSO" con el correo y la fecha-hora.**CA-01.2 Credenciales inválidas no revelan qué campo falló**Cuando ingreso el correo "ana.perez@granodorado.com" y la contraseña "Cafe2026"Entonces el sistema muestra el mensaje "Correo o contraseña incorrectos." Y el contador de intentos fallidos de esa cuenta pasa de 0 a 1 Y al repetir el intento con el correo "noexiste@granodorado.com" el mensaje es idéntico carácter por carácter.**CA-01.3 Bloqueo temporal tras 5 intentos fallidos**Dado que la cuenta acumula 4 intentos fallidos consecutivosCuando ingreso una contraseña incorrecta por quinta vezEntonces el sistema muestra "Cuenta bloqueada temporalmente. Intenta de nuevo en 15 minutos." Y durante 15 minutos los intentos con la contraseña CORRECTA son rechazados Y transcurridos 15 minutos el acceso con la contraseña correcta es exitoso.**CA-01.4 Cuenta inactiva**Dado que la cuenta "luis.gomez@granodorado.com" tiene estado "Inactivo"Cuando inicio sesión con su contraseña correctaEntonces el sistema muestra "Tu cuenta está inactiva. Comunícate con el Administrador General." Y no se crea ninguna sesión.**CA-01.5 Cierre automático de sesión por inactividad**Dado que inicié sesión correctamenteCuando transcurren 30 minutos sin ninguna acción y presiono una opción del menúEntonces el sistema me redirige al inicio de sesión Y muestra el mensaje "Tu sesión expiró por inactividad."**CA-01.6 Acceso directo a una URL sin permiso**Dado que inicié sesión con el rol "Barista"Cuando solicito directamente la dirección de la pantalla "Crear empleado"Entonces el sistema muestra "No tienes permiso para esta acción" Y no se muestra ningún dato de empleados. |
| **RNF aplicables** | \* RNF-02 (Seguridad): exigencia de iniciar sesión antes de acceder a cualquier función.\* RNF-03 (Disponibilidad): sistema disponible al menos el 99 % del tiempo cada mes.\* RNF-04 (Compatibilidad): uso desde computador y dispositivo móvil vía navegador web.\* RNF-06 (Seguridad): contraseña almacenada cifrada. |
| **Dependencias** | \* Catálogo de roles y matriz de permisos aprobados.\* Al menos una cuenta de Administrador General creada por carga inicial.\* HU-02, HU-03 y HU-04 dependen de esta historia. |
| **Riesgos** | \* Rechazo al cambio (WhatsApp). Mitigación: capacitación y operación en paralelo.\* Un bloqueo en hora pico deja a un cajero sin ver su turno. Mitigación: HU-02 operativa y desbloqueo manual. |
| **Datos de prueba** | \* admin.general@granodorado.com / Admin2026x — Administrador General — Activo.\* carla.rios@granodorado.com / Suc2026Centro — Administrador de Sucursal (Centro) — Activo.\* ana.perez@granodorado.com / Cafe2026x — Barista (Centro) — Activo.\* luis.gomez@granodorado.com / Cocina2026x — Cocinero (Norte) — Inactivo.\* noexiste@granodorado.com — cuenta inexistente. |
| **Evaluación DoR** | **Veredicto: NO LISTA.**\* Bloqueante 1: confirmar correo como identificador y cobertura entre los 45 empleados.\* Bloqueante 2: aprobar la matriz de permisos. |

# **1.2 HU-02 — Recuperación de clave**

| Campo | Detalle |
| :---- | :---- |
| **HU-02** | Recuperación de clave |
| **Épica** | Épica 1 — Gestión de usuarios y acceso |
| **Nombre HU** | Recuperación de clave |
| **Prioridad** | Sin ella el olvido de clave deja al empleado sin acceso a su horario. |
| **Actor** | Cualquier usuario con cuenta en estado Activo. |
| **Historia de usuario** | **Como** usuario registrado de Grano Dorado, **quiero** restablecer mi contraseña por mi cuenta mediante un enlace enviado a mi correo registrado, **para** recuperar el acceso a mi horario sin depender de la disponibilidad del Administrador General. |
| **Contexto** | El personal trabaja en turnos dentro de una franja de 16 horas diarias los 7 días, mientras que el Administrador General es una sola persona. Si el restablecimiento fuera manual, un barista del turno de las 06:00 del domingo podría quedar sin ver su horario durante horas. |
| **Valor esperado** | \* Elimina la intervención del Administrador General en los olvidos de clave.\* ≥ 90 % de las solicitudes se completan sin intervención del Administrador General.\* Tiempo entre la solicitud y la recepción del correo ≤ 2 minutos. |
| **Alcance** | \* Pantalla "Recuperar clave" con el campo Correo electrónico.\* Envío de correo con enlace de un solo uso y vigencia de 30 minutos.\* Pantalla de nueva contraseña con confirmación y validación de la política.\* Invalidación del enlace tras su uso o vencimiento.\* Cierre de las sesiones activas y desbloqueo del contador de intentos fallidos.\* Límite de solicitudes por cuenta y por hora, y registro en historial. |
| **Fuera de alcance** | \* Recuperación por SMS, WhatsApp o llamada telefónica.\* Preguntas de seguridad.\* Recuperación del correo electrónico o del identificador olvidado.\* Cambio de contraseña desde el perfil estando autenticado.\* Restablecimiento forzado masivo por parte del administrador.\* Caducidad periódica obligatoria de la contraseña. |
| **Reglas de negocio** | \* La solicitud responde siempre con el mismo mensaje, exista o no la cuenta.\* El enlace es de un solo uso y caduca a los 30 minutos.\* Emitir un enlace nuevo invalida cualquier enlace anterior vigente.\* La nueva contraseña requiere mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 dígito.\* La nueva contraseña no puede ser igual a la vigente.\* Un cambio exitoso cierra todas las sesiones activas y reinicia intentos fallidos.\* Máximo 3 solicitudes por cuenta en una ventana de 60 minutos.\* Una cuenta Inactiva no recibe correo de recuperación. |
| **Criterios de aceptación** | **CA-02.1 Solicitud con correo registrado**Cuando ingreso "ana.perez@granodorado.com" y presiono "Enviar enlace"Entonces el sistema muestra "Si el correo está registrado, enviamos un enlace para restablecer tu clave. Revisa tu bandeja de entrada." Y se envía 1 correo con un enlace único en 2 minutos o menos Y se agrega un registro "SOLICITUD\_RECUPERACION".**CA-02.2 Correo no registrado o cuenta inactiva**Cuando ingreso "noexiste@granodorado.com" y presiono "Enviar enlace"Entonces el sistema muestra el mismo mensaje de CA-02.1 Y no se envía ningún correo.**CA-02.3 Restablecimiento exitoso**Dado que recibí un enlace emitido hace 5 minutosCuando abro el enlace, escribo "NuevaClave1" en ambos campos y presiono "Guardar"Entonces el sistema muestra "Tu clave fue actualizada." Y al iniciar sesión con "NuevaClave1" el acceso es exitoso Y con "Cafe2026x" es rechazado.**CA-02.4 Enlace vencido o ya utilizado**Dado que el enlace fue emitido hace 31 minutos o ya fue usadoEntonces el sistema muestra "Este enlace expiró. Solicita uno nuevo." o "Este enlace ya fue utilizado."**CA-02.5 Validación de política de contraseña**Validación de min. 8 caracteres, 1 mayúscula, 1 minúscula, 1 dígito y ser distinta de la clave actual.**CA-02.6 Cierre de sesiones y desbloqueo**El restablecimiento exitoso cierra sesiones en otros dispositivos y desbloquea la cuenta. |
| **RNF aplicables** | \* RNF-02 (Seguridad), RNF-03 (Disponibilidad), RNF-04 (Compatibilidad), RNF-06 (Seguridad). |
| **Dependencias** | \* HU-01, servicio de correo contratado y dominio verificado. |
| **Riesgos** | \* Correos sin confirmar / clasificados como spam. |
| **Datos de prueba** | \* ana.perez@granodorado.com (Activo), noexiste@granodorado.com, luis.gomez@granodorado.com (Inactivo). |
| **Evaluación DoR** | **Veredicto: NO LISTA.**\* Bloqueante 1: censo de correos.\* Bloqueante 2: contratar proveedor de correo. |

# **1.3 HU-03 — Creación de empleados**

| Campo | Detalle |
| :---- | :---- |
| **HU-03** | Creación de empleados |
| **Épica** | Épica 1 — Gestión de usuarios y acceso |
| **Nombre HU** | Creación de empleados |
| **Prioridad** | Sin empleados registrados no hay a quién programar. |
| **Actor** | Administrador General; Administrador de Sucursal (limitado a su sucursal). |
| **Historia de usuario** | **Como** Administrador General de Grano Dorado, **quiero** registrar un empleado indicando sus datos de identificación, su rol operativo y su sucursal, **para** que quede habilitado como usuario con acceso propio y pueda ser considerado en la programación de turnos. |
| **Contexto** | Reemplaza las listas de personal en Excel por un registro único y validado. |
| **Valor esperado** | \* Fuente única de verdad sobre el personal.\* Los 45 empleados registrados antes de producción.\* 0 registros duplicados de documento de identidad. |
| **Alcance** | \* Formulario: nombre, apellido, tipo y número de documento, correo, teléfono, rol, sucursal y fecha de ingreso.\* Catálogos cerrados: 5 roles, 3 sucursales.\* Envío de correo de activación. |
| **Fuera de alcance** | Carga masiva desde CSV/Excel, eliminación, nómina, contratos, fotos. |
| **Reglas de negocio** | Documento y correo únicos; rol de catálogo de 5 opciones; sucursal de catálogo de 3 opciones; estado inicial Activo; fecha de ingreso actual o hasta \+30 días. |
| **Criterios de aceptación** | CA-03.1 Alta exitosa, CA-03.2 Definición de clave inicial, CA-03.3 Rechazo por documento duplicado, CA-03.4 Validación de campos obligatorios, CA-03.5 Listas desplegables cerradas, CA-03.6 Restricciones por rol de administrador de sucursal. |
| **Evaluación DoR** | **Veredicto: NO LISTA.** (Pendiente auditoría de calidad de datos Excel del personal). |

# **1.4 HU-04 — Actualización de empleados**

| Campo | Detalle |
| :---- | :---- |
| **HU-04** | Actualización de empleados |
| **Épica** | Épica 1 — Gestión de usuarios y acceso |
| **Nombre HU** | Actualización de empleados |
| **Prioridad** | Los traslados y cambios de rol son causa directa de asignaciones erróneas. |
| **Actor** | Administrador General; Administrador de Sucursal (alcance limitado). |
| **Historia de usuario** | **Como** Administrador General, **quiero** modificar los datos de un empleado registrado (rol, sucursal, estado), **para** que los permisos y la programación de turnos reflejen su situación laboral vigente. |
| **Alcance** | Edición de datos, cambio de estado (Activo/Inactivo), historial de modificaciones, advertencia de turnos futuros asignados al cambiar de sucursal. |
| **Reglas de negocio** | Documento no editable; cambio de rol cierra sesiones activas; inactivación cierra sesión y revoca acceso; no se puede dejar el sistema sin al menos un Administrador General activo. |
| **Criterios de aceptación** | CA-04.1 Cambio de rol y traza, CA-04.2 Reevaluación de permisos, CA-04.3 Advertencia por turnos futuros programados al trasladar, CA-04.4 Inactivación y reactivación, CA-04.5 Protección del último administrador, CA-04.6 Alcance del Administrador de Sucursal. |
| **Evaluación DoR** | **Veredicto: NO LISTA.** (Pendiente decidir si CA-04.3 entra ahora o al existir el módulo de turnos). |

# **Épica 2 — Planificación y asignación de turnos**

# **2.1 HU-06 — Creación de turnos**

| Campo | Detalle |
| :---- | :---- |
| **HU-06** | Creación de turnos |
| **Épica** | Épica 2 — Planificación y Asignación de turnos |
| **Nombre HU** | Creación de turnos |
| **Prioridad** | HU-07 no puede asignar nada sin un catálogo de turnos. |
| **Actor** | Administrador General |
| **Historia de usuario** | **Como Administrador General, quiero definir turnos laborales con hora de inicio, fin y descansos, para disponer de un catálogo reutilizable.** |
| **Alcance** | Formulario con nombre, hora inicio, hora fin, descansos. Cálculo automático de duración total y efectiva. Validación de rango 06:00–22:00. |
| **Reglas de negocio** | Horarios dentro de 06:00 a 22:00; descansos dentro del rango del turno sin solaparse; duración efectiva \> 0; nombre único; turnos con asignaciones se inactivan en lugar de eliminarse. |
| **Criterios de aceptación** | CA-06.1 Creación exitosa, CA-06.2 Validación de rango horario, CA-06.3 Descanso fuera del turno, CA-06.4 Descansos solapados, CA-06.5 Nombre duplicado, CA-06.6 Inactivación de turno asignado. |
| **Evaluación DoR** | **Veredicto: LISTA CON RESERVA. (Confirmar si el catálogo es único para las 3 sucursales).** |

# **2.2 HU-07 — Asignación de turnos**

| Campo | Detalle |
| :---- | :---- |
| **HU-07** | Asignación de turnos |
| **Épica** | Épica 2 — Planificación y Asignación de turnos |
| **Nombre HU** | Asignación de turnos |
| **Prioridad** | Núcleo del producto. |
| **Actor** | Administrador General y Administrador de Sucursal. |
| **Historia de usuario** | **Como** Administrador, **quiero** asignar turnos a empleados en fechas concretas y que se rechacen conflictos, **para** garantizar la cobertura de 06:00 a 22:00 sin programar a personal en descanso o vacaciones. |
| **Reglas de negocio** | Rechazo por descanso/vacaciones/incapacidad; máximo 1 turno por día por empleado; límite semanal de 42 horas efectivas; estado Borrador/Publicada; no programar en fechas pasadas. |
| **Criterios de aceptación** | CA-07.1 Asignación exitosa, CA-07.2 Rechazo por descanso/vacaciones, CA-07.3 Rechazo por segundo turno el mismo día, CA-07.4 Bloqueo por exceso de 42h semanales, CA-07.5 Empleado inactivo o de otra sucursal no disponible, CA-07.6 Alcance por rol. |
| **Evaluación DoR** | **Veredicto: NO LISTA.** (Bloqueante: requiere módulo de registro de descansos/vacaciones/incapacidades). |

# **2.3 HU-08 — Visualización de horarios por sucursal**

| Campo | Detalle |
| :---- | :---- |
| **HU-08** | Visualización de horarios por sucursal |
| **Épica** | Épica 2 — Planificación y Asignación de turnos |
| **Nombre HU** | Visualización de horarios por sucursal |
| **Actor** | Administrador General / Administrador de Sucursal |
| **Historia de usuario** | **Como Administrador, quiero ver la malla semanal de turnos de una sucursal con alertas de cobertura, para verificar que la jornada 06:00-22:00 queda cubierta antes de publicar.** |
| **Criterios de aceptación** | CA-08.1 Despliegue de malla semanal, CA-08.2 Días de no disponibilidad, CA-08.3 Totalizador de horas efectivas, CA-08.4 Estado Borrador/Publicada, CA-08.5 Alcance por rol. |
| **Evaluación DoR** | **Veredicto: NO LISTA. (Depende de HU-07 y definición de cobertura mínima).** |

# **Épica 3 — Portal de empleado**

# **3.1 HU-09 — Ver horario asignado**

| Campo | Detalle |
| :---- | :---- |
| **HU-09** | Ver horario asignado |
| **Épica** | Épica 3 — Portal de empleado |
| **Actor** | Cajero, Barista, Cocinero, Administrador de Sucursal. |
| **Historia de usuario** | **Como** empleado, **quiero** consultar mi horario semanal asignado, **para** presentarme puntualmente sin depender de WhatsApp. |
| **Criterios de aceptación** | CA-09.1 Consulta de semana publicada, CA-09.2 Días sin turno visiblemente marcados, CA-09.3 Visibilidad exclusiva del horario propio, CA-09.4 Ocultamiento de semanas en Borrador, CA-09.5 Indicación de cambios posteriores a publicación, CA-09.6 Histórico y solo lectura, CA-09.7 Responsividad móvil (360px). |
| **Evaluación DoR** | **Veredicto: LISTA CON RESERVA.** |

# **3.2 HU-10 — Solicitar cambio de turno**

| Campo | Detalle |
| :---- | :---- |
| **HU-10** | Solicitar cambio de turno |
| **Épica** | Épica 3 — Portal de empleado |
| **Actor** | Empleado solicitante, empleado receptor y Administrador de Sucursal. |
| **Historia de usuario** | **Como** empleado, **quiero** solicitar el intercambio de un turno con un compañero, **para** resolver eventualidades formalmente. |
| **Reglas de negocio** | Flujo: Solicitud \-\> Aceptación por compañero \-\> Aprobación por Administrador. Mismo rol/sucursal; respeto a descanso mínimo (12h) y límite de horas semanales. |
| **Criterios de aceptación** | CA-10.1 Envío y aceptación del compañero, CA-10.2 Aprobación del administrador y aplicación, CA-10.3 Rechazo con motivo, CA-10.4 Filtrado de turnos no elegibles, CA-10.5 Control de descanso mínimo, CA-10.6 Antelación mínima (24h) y vencimiento, CA-10.7 Cancelación y prevención de duplicados. |
| **Evaluación DoR** | **Veredicto: NO LISTA.** (Pendiente reglas exactas de perfil y antelación). |

# **3.3 HU-11 — Reportar novedades**

| Campo | Detalle |
| :---- | :---- |
| **HU-11** | Reportar novedades |
| **Épica** | Épica 3 — Portal de empleado |
| **Actor** | Empleado |
| **Historia de usuario** | **Como** empleado, **quiero** reportar novedades de mi turno (salida temprana, ampliación, llegada tarde), **para** dejar constancia del tiempo real. |
| **Reglas de negocio** | Reporte sobre turnos pasados dentro de 48 horas; tipos: Salida antes, Ampliación, Llegada tarde, Otra; descripción obligatoria. |
| **Criterios de aceptación** | CA-11.1 Salida antes de la hora, CA-11.2 Ampliación del turno, CA-11.3 Validación de horas reportadas, CA-11.4 Campos requeridos y plazo 48h, CA-11.5 Revisión por el administrador, CA-11.6 Alcance de la bandeja. |
| **Evaluación DoR** | **Veredicto: NO LISTA.** |

# **Épica 4 — Gestión de novedades**

# **4.1 HU-12 — Ver novedades**

| Campo | Detalle |
| :---- | :---- |
| **HU-12** | Ver novedades |
| **Épica** | Épica 4 — Gestión de novedades |
| **Actor** | Administrador de Sucursal / Administrador General |
| **Historia de usuario** | **Como** Administrador, **quiero** consultar las novedades reportadas por mi equipo, **para** conocer las diferencias vs lo programado y marcarlas como revisadas. |
| **Criterios de aceptación** | CA-12.1 Bandeja de novedades, CA-12.2 Detalle y cálculo de diferencia en minutos, CA-12.3 Marcar como revisada con comentario, CA-12.4 Inmutabilidad de novedades revisadas, CA-12.5 Filtros de búsqueda, CA-12.6 Alcance por sucursal. |
| **Evaluación DoR** | **Veredicto: LISTA CON RESERVA.** |

# **4.2 HU-13 — Aprobar cambios de turno**

| Campo | Detalle |
| :---- | :---- |
| **HU-13** | Aprobar cambios de turno |
| **Épica** | Épica 4 — Gestión de novedades |
| **Actor** | Administrador de Sucursal / Administrador General |
| **Historia de usuario** | **Como** Administrador, **quiero** revisar y aprobar/rechazar las solicitudes de cambio de turno previamente aceptadas por los compañeros, **para** validar la cobertura. |
| **Criterios de aceptación** | CA-13.1 Bandeja de solicitudes, CA-13.2 Detalle comparativo, CA-13.3 Aprobación y reintercambio automático de horarios, CA-13.4 Rechazo con motivo obligatorio, CA-13.5 Revalidación de reglas al momento de aprobar, CA-13.6 Manejo de solicitudes vencidas, CA-13.7 Alcance por sucursal y conflicto de interés. |
| **Evaluación DoR** | **Veredicto: LISTA CON RESERVA.** |

# **4.3 HU-14 — Reportar novedades de empleado**

| Campo | Detalle |
| :---- | :---- |
| **HU-14** | Reportar novedades de empleado |
| **Épica** | Épica 4 — Gestión de novedades |
| **Actor** | Administrador de Sucursal / Administrador General |
| **Historia de usuario** | **Como** Administrador, **quiero** registrar novedades sobre el turno de un empleado cuando él no lo hizo (p. ej. ausencias), **para** mantener actualizada la operación. |
| **Criterios de aceptación** | CA-14.1 Registro por el supervisor, CA-14.2 Registro de Ausencia (sin hora real), CA-14.3 No duplicación si el empleado ya reportó, CA-14.4 Validación de turnos futuros y descripción, CA-14.5 Coherencia de horarios, CA-14.6 Plazo de 48h y alcance por sucursal. |
| **Evaluación DoR** | **Veredicto: NO LISTA.** (Pendiente unificar lógica de ausencias con Épica 5). |

# **Épica 5 — Control de asistencia**

# **5.1 Marco de la Épica 5**

| Campo | Detalle |
| :---- | :---- |
| **Descripción** | Registro de asistencia al turno de trabajo. |
| **Situación actual** | 3 sucursales, 45 empleados, operación 06:00 a 22:00. Sin registro de asistencia actual. |
| **Solución** | Registro en tiempo real de entradas, salidas y descansos. |

# **5.2 HU-15 — Marcar asistencia al turno**

| Campo | Detalle |
| :---- | :---- |
| **HU-15** | Marcar asistencia al turno |
| **Épica** | Épica 5 — Control de Asistencia |
| **Actor** | Cajero, Barista, Cocinero, Administrador de Sucursal (sobre su turno). |
| **Historia de usuario** | **Como** empleado, **quiero** registrar inicio, descansos y fin de mi turno, **para** llevar control del tiempo efectivamente trabajado. |
| **Reglas de negocio** | Secuencia estricta: Inicio \-\> Salida Descanso \-\> Regreso Descanso \-\> Fin. Hora tomada exclusivamente del servidor. Unipersonal e inmutable tras marcar. |
| **Criterios de aceptación** | CA-15.1 Secuencia completa de marcación, CA-15.2 Registro de inicio tardío con aviso de diferencia, CA-15.3 Bloqueo de marcas fuera de secuencia, CA-15.4 Restricción en días sin turno/libre, CA-15.5 Hora proveniente del servidor, CA-15.6 Marcas inalterables. |
| **Evaluación DoR** | **Veredicto: LISTA CON RESERVA.** |

# **5.3 HU-16 — Marcar asistencia al turno (registro por el supervisor)**

| Campo | Detalle |
| :---- | :---- |
| **HU-16** | Marcar asistencia al turno (supervisor) |
| **Épica** | Épica 5 — Control de Asistencia |
| **Actor** | Administrador de Sucursal |
| **Historia de usuario** | **Como** Administrador, **quiero** registrar la marca faltante de un empleado cuando él olvidó hacerlo, **para** completar el registro de asistencia. |
| **Reglas de negocio** | No se pueden modificar las marcas hechas por el empleado; motivo obligatorio; plazo máximo de 48 horas. |
| **Criterios de aceptación** | CA-16.1 Registro de marca de fin olvidada, CA-16.2 Inmutabilidad de marcas del empleado, CA-16.3 Motivo obligatorio, CA-16.4 Coherencia de horarios, CA-16.5 Respeto de secuencia, CA-16.6 Plazo de 48 horas, CA-16.7 Alcance por sucursal. |
| **Evaluación DoR** | **Veredicto: LISTA CON RESERVA.** |

# **5.4 HU-17 — Notificaciones de incumplimientos**

| Campo | Detalle |
| :---- | :---- |
| **HU-17** | Notificaciones de incumplimientos |
| **Épica** | Épica 5 — Control de Asistencia |
| **Actor** | Administrador de Sucursal / Administrador General |
| **Historia de usuario** | **Como** Administrador, **quiero** recibir una notificación tras 15 minutos del inicio programado sin marca de entrada, **para** gestionar la ausencia oportunamente. |
| **Reglas de negocio** | Disparo a los 15 min de retraso en turnos publicados; cierre automático si el empleado marca después; atención manual con constancia. |
| **Criterios de aceptación** | CA-17.1 Alerta automática tras 15 minutos sin marca, CA-17.2 No repetición de alerta, CA-17.3 Cierre automático por marcación tardía, CA-17.4 Atención manual con constancia, CA-17.5 Exclusión de semanas no publicadas o vacaciones, CA-17.6 Alcance por sucursal. |
| **Evaluación DoR** | **Veredicto: NO LISTA.** (Pendiente definir canal de notificación push/email/SMS). |

