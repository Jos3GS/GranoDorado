# Épica 1 — Gestión de usuarios y acceso

**Resumen:** Agrupa las historias para entrar al sistema y administrar a los empleados: inicio de sesión (HU-01), recuperación de clave (HU-02), creación de empleados (HU-03) y actualización de empleados (HU-04).

*Resumen elaborado a partir de las historias de la épica; el documento original no incluye una descripción propia de esta épica.*

## Historias de usuario de esta épica

| Sección | Identificador | Nombre |
|---|---|---|
| 1.1 | [HU-01](#11-hu-01--login) | Login |
| 1.2 | [HU-02](#12-hu-02--recuperación-de-clave) | Recuperación de clave |
| 1.3 | [HU-03](#13-hu-03--creación-de-empleados) | Creación de empleados |
| 1.4 | [HU-04](#14-hu-04--actualización-de-empleados) | Actualización de empleados |

---

## 1.1 HU-01 — Login

**HU-01- Login**

### Épica

Épica 1 — Gestión de usuarios y acceso

### Nombre HU

Login

### Prioridad

bloquea toda la épica y las épicas siguientes.

### Actor

Administrador General, Administrador de Sucursal, Cajero, Barista, Cocinero.

### Historia de usuario

**Como** usuario registrado de Grano Dorado, **quiero** autenticarme con mi correo electrónico y mi contraseña, **para** acceder únicamente a las funciones y a la información de la sucursal que corresponden a mi rol.

### Contexto

Hoy no existe control de acceso: los horarios se publican en un grupo de WhatsApp donde todos ven todo y los mensajes se pierden. Sin identidad individual verificada no se puede saber quién consultó su turno ni restringir la programación al personal autorizado. El login es la puerta de entrada del sistema y precondición de HU-02, HU-03 y HU-04.

### Valor esperado

- Habilita el reemplazo del canal WhatsApp por un acceso individual, atacando la causa "el mensaje se perdió / leyeron mal el Excel".
- El 100 % de los 45 empleados inicia sesión al menos una vez en las 2 primeras semanas de producción.

### Alcance

- Pantalla de inicio de sesión con los campos Correo electrónico y Contraseña.
- Validación de credenciales y del estado de la cuenta (Activo / Inactivo).
- Redirección y menú según el rol, conforme a la matriz de permisos.
- Bloqueo temporal por intentos fallidos y cierre de sesión manual y por inactividad.
- Registro en historial de intentos exitosos y fallidos.
- Enlace "¿Olvidaste tu clave?" que deriva a HU-02.
- Inicio de sesión con proveedores externos (Solo Google)

### Fuera de alcance

- Autenticación multifactor y biométrica.
- Inicio de sesión con proveedores externos (Microsoft, redes sociales).
- Autorregistro de usuarios.
- Recuperación de contraseña.
- Gestión del catálogo de roles y permisos (historia aparte de la misma épica).
- Aplicación móvil nativa (solo web responsiva).

### Reglas de negocio

- El identificador de acceso es el correo electrónico, único y no sensible a mayúsculas.
- Solo las cuentas en estado Activo pueden iniciar sesión, aunque la contraseña sea correcta.
- Ante credenciales inválidas se muestra siempre el mismo mensaje: "Correo o contraseña incorrectos.", sin indicar cuál falló.
- Tras 5 intentos fallidos consecutivos la cuenta se bloquea 15 minutos; el contador se reinicia con un login exitoso o un cambio de clave
- La sesión se cierra automáticamente tras 30 minutos sin interacción.
- Los permisos se aplican según la matriz de permisos del Marco común.
- La contraseña se muestra enmascarada, con un control explícito para revelarla.
- Cada intento, exitoso o fallido, se registra con correo, fecha-hora, resultado e IP.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que existe la cuenta "ana.perez@granodorado.com" con rol "Barista",
        sucursal "Centro" y estado "Activo"
  Y su contraseña es "Cafe2026x"
```

#### CA-01.1 Inicio de sesión exitoso de personal operativo

```gherkin
  Cuando ingreso el correo "ana.perez@granodorado.com" y la contraseña "Cafe2026x"
  Y presiono el botón "Iniciar sesión"
  Entonces el sistema muestra la pantalla "Mi horario" en 3 segundos o menos
  Y el menú contiene únicamente "Mi horario" y "Mi perfil"
  Y el menú NO contiene la opción "Empleados"
  Y se agrega al historial un registro "LOGIN_EXITOSO" con el correo y la fecha-hora
```

#### CA-01.2 Credenciales inválidas no revelan qué campo falló

```gherkin
  Cuando ingreso el correo "ana.perez@granodorado.com" y la contraseña "Cafe2026"
  Entonces el sistema muestra el mensaje "Correo o contraseña incorrectos."
  Y el contador de intentos fallidos de esa cuenta pasa de 0 a 1
  Y al repetir el intento con el correo "noexiste@granodorado.com" el mensaje
    es idéntico carácter por carácter
```

#### CA-01.3 Bloqueo temporal tras 5 intentos fallidos

```gherkin
  Dado que la cuenta acumula 4 intentos fallidos consecutivos
  Cuando ingreso una contraseña incorrecta por quinta vez
  Entonces el sistema muestra "Cuenta bloqueada temporalmente. Intenta de nuevo
    en 15 minutos."
  Y durante 15 minutos los intentos con la contraseña CORRECTA son rechazados
  Y transcurridos 15 minutos el acceso con la contraseña correcta es exitoso
```

#### CA-01.4 Cuenta inactiva

```gherkin
  Dado que la cuenta "luis.gomez@granodorado.com" tiene estado "Inactivo"
  Cuando inicio sesión con su contraseña correcta
  Entonces el sistema muestra "Tu cuenta está inactiva. Comunícate con el
    Administrador General."
  Y no se crea ninguna sesión
```

#### CA-01.5 Cierre automático de sesión por inactividad

```gherkin
  Dado que inicié sesión correctamente
  Cuando transcurren 30 minutos sin ninguna acción y presiono una opción del menú
  Entonces el sistema me redirige al inicio de sesión
  Y muestra el mensaje "Tu sesión expiró por inactividad."
```

#### CA-01.6 Acceso directo a una URL sin permiso

```gherkin
  Dado que inicié sesión con el rol "Barista"
  Cuando solicito directamente la dirección de la pantalla "Crear empleado"
  Entonces el sistema muestra "No tienes permiso para esta acción"
  Y no se muestra ningún dato de empleados
```

### RNF aplicables

- RNF-02 (Seguridad): esta historia es la que implementa la exigencia de iniciar sesión antes de acceder a cualquier función, y habilita el menú según el rol del usuario.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-06 (Seguridad): la contraseña se almacena cifrada, no se muestra en pantalla y no viaja en claro.

### Dependencias

- Catálogo de roles y matriz de permisos definidos y aprobados.
- Al menos una cuenta de Administrador General creada por carga inicial.
- HU-02, HU-03 y HU-04 dependen de esta historia.

### Riesgos

- Rechazo al cambio: el personal está habituado a WhatsApp (H-7). Mitigación: capacitación y operación en paralelo.
- Un bloqueo en hora pico deja a un cajero sin ver su turno. Mitigación: HU-02 operativa en el mismo release y desbloqueo manual.

### Datos de prueba

- admin.general@granodorado.com / Admin2026x — Administrador General — Activo.
- carla.rios@granodorado.com / Suc2026Centro — Administrador de Sucursal (Centro) — Activo.
- ana.perez@granodorado.com / Cafe2026x — Barista (Centro) — Activo.
- luis.gomez@granodorado.com / Cocina2026x — Cocinero (Norte) — Inactivo.
- noexiste@granodorado.com — cuenta inexistente, para el mensaje neutro.

### Evaluación DoR

**Veredicto: NO LISTA.** Cumplen: formato de historia, valor medible, alcance delimitado, criterios verificables, estimabilidad, RNF y datos de prueba.

- Bloqueante 1: confirmar correo como identificador y su cobertura entre los 45 empleados.
- Bloqueante 2: aprobar la matriz de permisos

---

## 1.2 HU-02 — Recuperación de clave

**HU-02- Recuperación de clave**

### Épica

Épica 1 — Gestión de usuarios y acceso

### Nombre HU

Recuperación de clave

### Prioridad

sin ella el olvido de clave deja al empleado sin acceso a su horario.

### Actor

Cualquier usuario con cuenta en estado Activo.

### Historia de usuario

**Como** usuario registrado de Grano Dorado, **quiero** restablecer mi contraseña por mi cuenta mediante un enlace enviado a mi correo registrado, **para** recuperar el acceso a mi horario sin depender de la disponibilidad del Administrador General.

### Contexto

El personal trabaja en turnos dentro de una franja de 16 horas diarias los 7 días, mientras que el Administrador General es una sola persona. Si el restablecimiento fuera manual, un barista del turno de las 06:00 del domingo podría quedar sin ver su horario durante horas, reproduciendo el problema de comunicación que el sistema busca eliminar.

### Valor esperado

- Elimina la intervención del Administrador General en los olvidos de clave.
- ≥ 90 % de las solicitudes se completan sin intervención del Administrador General.
- Tiempo entre la solicitud y la recepción del correo ≤ 2 minutos.

### Alcance

- Pantalla "Recuperar clave" con el campo Correo electrónico.
- Envío de correo con enlace de un solo uso y vigencia de 30 minutos.
- Pantalla de nueva contraseña con confirmación y validación de la política.
- Invalidación del enlace tras su uso o vencimiento.
- Cierre de las sesiones activas y desbloqueo del contador de intentos fallidos al cambiar la clave.
- Límite de solicitudes por cuenta y por hora, y registro en historial.

### Fuera de alcance

- Recuperación por SMS, WhatsApp o llamada telefónica.
- Preguntas de seguridad.
- Recuperación del correo electrónico o del identificador olvidado.
- Cambio de contraseña desde el perfil estando autenticado (historia separada).
- Restablecimiento forzado masivo por parte del administrador.
- Caducidad periódica obligatoria de la contraseña.

### Reglas de negocio

- La solicitud responde siempre con el mismo mensaje, exista o no la cuenta; el correo solo se envía si existe y está Activa.
- El enlace es de un solo uso y caduca a los 30 minutos de su emisión.
- Emitir un enlace nuevo invalida cualquier enlace anterior vigente de esa cuenta.
- La nueva contraseña requiere mínimo 8 caracteres, 1 mayúscula, 1 minúscula y 1 dígito.
- La nueva contraseña no puede ser igual a la vigente.
- Un cambio exitoso cierra todas las sesiones activas y reinicia el contador de intentos fallidos
- Máximo 3 solicitudes por cuenta en una ventana de 60 minutos.
- Una cuenta Inactiva no recibe correo de recuperación.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que existe la cuenta "ana.perez@granodorado.com" con estado "Activo"
  Y su contraseña vigente es "Cafe2026x"
```

#### CA-02.1 Solicitud con correo registrado

```gherkin
  Cuando ingreso "ana.perez@granodorado.com" y presiono "Enviar enlace"
  Entonces el sistema muestra "Si el correo está registrado, enviamos un enlace
    para restablecer tu clave. Revisa tu bandeja de entrada."
  Y se envía 1 correo con un enlace único en 2 minutos o menos
  Y se agrega a la historia un registro "SOLICITUD_RECUPERACION"
```

#### CA-02.2 Correo no registrado o cuenta inactiva no revelan información

```gherkin
  Cuando ingreso "noexiste@granodorado.com" y presiono "Enviar enlace"
  Entonces el sistema muestra el mismo mensaje de CA-02.1, idéntico carácter
    por carácter
  Y no se envía ningún correo
  Y el resultado es el mismo para la cuenta inactiva "luis.gomez@granodorado.com"
```

#### CA-02.3 Restablecimiento exitoso dentro de la vigencia

```gherkin
  Dado que recibí un enlace emitido hace 5 minutos
  Cuando abro el enlace y escribo "NuevaClave1" en ambos campos y presiono "Guardar"
  Entonces el sistema muestra "Tu clave fue actualizada."
  Y al iniciar sesión con "NuevaClave1" el acceso es exitoso
  Y al iniciar sesión con "Cafe2026x" el acceso es rechazado
```

#### CA-02.4 Enlace vencido y enlace ya utilizado

```gherkin
  Dado que el enlace fue emitido hace 31 minutos
  Cuando abro el enlace
  Entonces el sistema muestra "Este enlace expiró. Solicita uno nuevo."
  Y no se muestran los campos de nueva contraseña
  Y si el enlace ya fue usado el mensaje es "Este enlace ya fue utilizado.
    Solicita uno nuevo."
```

#### Esquema del escenario: CA-02.5 Validación de la política de contraseña

```gherkin
  Dado que abrí un enlace de recuperación vigente
  Cuando escribo "<clave>" en ambos campos y presiono "Guardar"
  Entonces el sistema muestra "<mensaje>" y la contraseña NO se modifica
  Ejemplos:
    | CLAVE | MENSAJE |
    | Abc123 | La clave debe tener al menos 8 caracteres |
    | abcdefg1 | La clave debe incluir al menos una letra mayúscula |
    | Abcdefgh | La clave debe incluir al menos un dígito |
    | Cafe2026x | La nueva clave debe ser distinta de la clave actual | |
```

#### CA-02.6 El cambio cierra sesiones y desbloquea la cuenta

```gherkin
  Dado que la cuenta está bloqueada por 5 intentos fallidos
  Y tengo una sesión abierta en otro dispositivo
  Cuando completo el restablecimiento con "NuevaClave1"
  Entonces el otro dispositivo es redirigido al inicio de sesión
  Y el acceso con "NuevaClave1" es exitoso sin esperar los 15 minutos de bloqueo
```

### RNF aplicables

- RNF-02 (Seguridad): el enlace de recuperación no da acceso a ninguna función; al terminar el cambio, el usuario debe iniciar sesión.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-06 (Seguridad): la contraseña nueva se almacena cifrada y la anterior deja de ser válida.

### Dependencias

- HU-01 (cuentas existentes y enlace "¿Olvidaste tu clave?").
- Servicio de envío de correo contratado y dominio verificado.
- Correos válidos capturados como campo obligatorio en HU-03.

### Riesgos

- Correos sin confirmar: el caso solo evidencia WhatsApp. Si baristas, cajeros y cocineros no tienen correo propio, la historia no es implementable como está escrita.
- Los correos clasificados como spam impiden la recuperación.

Mitigación: configurar SPF/DKIM e instrucción visible en pantalla.

- Uso del enlace en un dispositivo compartido de la sucursal.

Mitigación: enlace de un solo uso y vigencia de 30 minutos.

### Datos de prueba

- Existe: ana.perez@granodorado.com, Activo, enlace de hace 5 min → cambio exitoso.
- No existe: noexiste@granodorado.com → mensaje neutro, sin correo.
- Inactivo: luis.gomez@granodorado.com → mensaje neutro, sin correo.
- Vencido: enlace de hace 31 min → enlace expirado.
- Reutilizado: enlace ya usado → enlace utilizado.
- Bloqueada: cuenta con 5 fallidos + enlace vigente → cambio exitoso y desbloqueo.
- Claves inválidas: Abc123, abcdefg1, ABCDEFG1, Abcdefgh, Cafe2026x.

### Notas de UX

*(Sin contenido en el documento original.)*

### Evaluación DoR

**Veredicto: NO LISTA.** Cumplen: formato, valor medible, alcance, 12 criterios verificables, RNF y datos de prueba.

- Bloqueante 1: confirmar correos con un censo de los correos de los 45 empleados; sin él la historia debe reescribirse con otro canal.
- Bloqueante 2: definir y contratar el proveedor de envío de correo (afecta también la estimación).

---

## 1.3 HU-03 — Creación de empleados

**HU-03- Creación de empleados**

### Épica

Épica 1 — Gestión de usuarios y acceso

### Nombre HU

Creación de empleados

### Prioridad

Sin empleados registrados no hay a quién programar.

### Actor

Administrador General. Administrador de Sucursal con alcance limitado a su sucursal.

### Historia de usuario

**Como** Administrador General de Grano Dorado, **quiero** registrar un empleado indicando sus datos de identificación, su rol operativo y su sucursal, **para** que quede habilitado como usuario con acceso propio y pueda ser considerado en la programación de turnos.

### Contexto

Hoy los horarios viven en hojas de Excel del Administrador General. Al no existir un registro único de quién trabaja, en qué sucursal y con qué rol, se asignan turnos a personas de descanso o vacaciones y no hay forma de restringir quién ve qué. Esta historia crea la fuente única de verdad sobre la que se apoyan la autenticación y, después, la programación de turnos.

### Valor esperado

- Sustituye la lista de personal en Excel por un registro consultable y validado, eliminando la digitación repetida al cuadrar horarios.
- Los 45 empleados quedan registrados con rol y sucursal antes de la puesta en producción.
- 0 registros duplicados de un mismo documento de identidad, verificable por consulta a la base de datos.

### Alcance

- Formulario: nombre, apellido, tipo y número de documento, correo, teléfono, rol, sucursal y fecha de ingreso.
- Rol desde catálogo cerrado de 5 valores y sucursal desde catálogo cerrado de 3 valores.
- Creación de la cuenta con estado Activo y envío del correo de activación.
- Restricción por rol según y registro del alta en historial.

### Fuera de alcance

- Carga masiva desde archivo Excel o CSV.
- Actualización, baja o eliminación de empleados.
- Contratos, salarios, nómina y documentos de vinculación.
- Registro de vacaciones y días de descanso (épica de turnos).
- Edición de los catálogos de roles y de sucursales.
- Fotografía o archivos adjuntos del empleado, y autorregistro.

### Reglas de negocio

- Campos obligatorios: nombre, apellido, tipo y número de documento, correo, rol, sucursal y fecha de ingreso. Teléfono es opcional.
- El número de documento es único en el sistema, sin importar el estado del empleado.
- El correo es único y debe cumplir el formato local@dominio.tld
- El rol solo admite uno de los 5 valores del catálogo cerrado; no se acepta texto libre.
- La sucursal solo admite uno de los 3 valores del catálogo; un empleado pertenece a exactamente una sucursal.
- El empleado nace con estado Activo.
- El sistema no asigna contraseña visible: envía un correo de activación con enlace de un solo uso.
- El Administrador de Sucursal solo crea en su propia sucursal y no puede asignar el rol Administrador General.
- La fecha de ingreso no puede superar la fecha actual + 30 días ni ser anterior a la fecha actual.
- El alta se registra con el usuario que la ejecuta, la fecha-hora y los valores creados.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión con el rol "Administrador General"
  Y las sucursales registradas son "Centro", "Norte" y "Sur"
```

#### CA-03.1 Alta exitosa de un empleado

```gherkin
  Cuando completo el formulario con:
    | Nombre/Apellido | María Torres |
    | Número de documento | 1032456789 |
    | Correo electrónico | maria.torres@granodorado.com |
    | Rol / Sucursal | Cajero / Norte |
    | Fecha de ingreso | 2026-09-15 |

  Y presiono el botón "Guardar"
  Entonces el sistema muestra "Empleado creado correctamente."
  Y el empleado aparece en el listado con estado "Activo", rol "Cajero"
    y sucursal "Norte"
  Y se envía 1 correo de activación a "maria.torres@granodorado.com"
  Y se agrega a la bitácora un registro "EMPLEADO_CREADO" con el usuario ejecutor
```

#### CA-03.2 El empleado creado puede acceder tras definir su clave

```gherkin
  Cuando ella abre el enlace de activación y define la clave "Caja2026x"
  Y luego inicia sesión con ese correo y esa clave
  Entonces el acceso es exitoso
  Y el menú corresponde al rol "Cajero" según la matriz.
```

#### CA-03.3 Documento duplicado, incluso de un empleado inactivo

```gherkin
  Dado que ya existe un empleado con el documento "1032456789"
  Cuando intento crear otro empleado con ese mismo número
  Entonces el sistema muestra "Ya existe un empleado con este número
    de documento."
  Y no se crea ningún registro nuevo
  Y el resultado es el mismo si ese empleado tiene estado "Inactivo"
```

#### Esquema del escenario: CA-03.4 Campos obligatorios

```gherkin
  Cuando dejo vacío el campo "<campo>" y completo correctamente los demás
  Entonces el sistema muestra "<campo> es requerido" junto a ese campo
  Y no se crea ningún registro
  Ejemplos:

    | Campo |
    | Nombre |
    | Numero de documento |
    | Correo electrónico |
    | Rol |
    | Sucursal |
    | Fecha de ingreso |
```

#### CA-03.5 Rol y sucursal son catálogos cerrados

```gherkin
  Cuando abro el campo "Rol"
  Entonces es una lista de selección que no admite texto libre
  Y contiene exactamente 5 opciones: "Administrador General",
    "Administrador de Sucursal", "Cajero", "Barista", "Cocinero"
  Y el campo "Sucursal" contiene exactamente 3 opciones:
    "Centro", "Norte" y "Sur"
```

#### CA-03.6 Restricciones por rol

```gherkin
  Dado que inicié sesión con el rol "Administrador de Sucursal" de "Centro"
  Cuando abro la pantalla "Crear empleado"
  Entonces el campo "Sucursal" muestra "Centro" y no es editable
  Y la lista "Rol" NO contiene la opción "Administrador General"
  Y si inicio sesión con el rol "Barista" la opción "Empleados" no está visible
    y el acceso directo a la URL muestra "No tienes permiso para esta acción"
```

### RNF aplicables

- RNF-02 (Seguridad): solo el Administrador General autenticado registra empleados y les define el rol y la sucursal con los que accederán.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.
- RNF-06 (Seguridad): la contraseña inicial del empleado se almacena cifrada.

### Dependencias

- HU-01 (sesión autenticada y matriz de permisos).
- Catálogo de sucursales (3 registros) y de roles (5 registros) cargados como datos semilla.
- Servicio de correo, el mismo de HU-02, para el correo de activación.

### Riesgos

- Dar de alta 45 empleados uno por uno es lento y propenso a error, y la carga masiva está fuera de alcance. Mitigación: evaluar una historia adicional de importación.
- Datos incompletos en el Excel actual (correos o documentos faltantes) impiden completar campos obligatorios. Mitigación: auditar el Excel antes del sprint.
- Homónimos entre los 45 empleados generan confusión. Mitigación: mostrar el documento en el listado.

### Datos de prueba

- Existe: 1032456789 / maria.torres@granodorado.com / Cajero / Norte / 2026-09-15 → creado.
- Documento duplicado: 1032456789 con otro correo → rechazado.
- Documento de empleado inactivo: 1099887766 → rechazado.
- Correo duplicado: ana.perez@granodorado.com → rechazado.
- Correo inválido: "maria.torres", "maria.torres@", "@granodorado.com" → rechazado.
- Fecha fuera de rango: 2026-10-20 y 2015-01-01 → rechazado.
- Administrador de Sucursal (Centro) creando en Centro → creado; intentando otra sucursal → campo no editable.

### Evaluación DoR

**Veredicto: NO LISTA.** Cumplen: formato, valor medible, alcance, 13 criterios verificables, estimabilidad, RNF y datos de prueba.

- Pendientes: Documento como identificador único y rango de fecha de ingreso
- Verificar la calidad de los datos del Excel actual para los 45 empleados.

---

## 1.4 HU-04 — Actualización de empleados

**HU-04- Actualización de empleados**

### Épica

Épica 1 — Gestión de usuarios y acceso

### Nombre HU

Actualización de empleados

### Prioridad

Los traslados y cambios de rol son causa directa de asignaciones erróneas.

### Actor

Administrador General. Administrador de Sucursal con alcance limitado.

### Historia de usuario

**Como** Administrador General de Grano Dorado, **quiero** modificar los datos de un empleado registrado —su rol operativo, su sucursal asignada y su estado de acceso—, **para** que los permisos del sistema y la información usada para programar turnos reflejen su situación laboral vigente.

### Contexto

Grano Dorado es una cadena en crecimiento con 3 sucursales y personal que rota entre puestos y locales. Cuando el rol o la sucursal cambia y el cambio solo vive en el Excel o en la memoria del Administrador General, se producen los conflictos descritos: turnos asignados a quien ya no está disponible o no está en esa sucursal. Además, si alguien deja la empresa y su acceso no se revoca, sigue viendo la programación interna.

### Valor esperado

- Mantiene actualizada la fuente única de verdad que alimenta la programación, reduciendo los conflictos de horario.
- El cambio de rol o sucursal se refleja en los permisos en el siguiente inicio de sesión, sin intervención técnica.
- 0 accesos exitosos de empleados inactivados, verificable en la bitácora.

### Alcance

- Listado de empleados con búsqueda por nombre, documento, rol, sucursal y estado.
- Edición de nombre, apellido, correo, teléfono, rol y sucursal.
- Cambio de estado entre Activo e Inactivo, con revocación efectiva del acceso.
- Reevaluación de permisos tras un cambio de rol y validación de unicidad de correo.
- Historial de cambios por empleado (campo, valor anterior, valor nuevo, quién y cuándo).
- Restricciones por rol.

### Fuera de alcance

- Eliminación física del empleado.
- Reasignación automática de turnos al cambiar de sucursal: el sistema solo advierte
- Registro de vacaciones, incapacidades o días de descanso (épica de turnos).
- Cambio de contraseña de otro usuario por parte del administrador.
- Edición simultánea de más de un empleado en una sola operación.
- Flujo de aprobación o autorización de los cambios.
- Histórico laboral formal de cargos con fechas de vigencia.

### Reglas de negocio

- El tipo y el número de documento no son editables: identifican al empleado. Un error se corrige inactivando el registro y creando uno nuevo.
- Al cambiar el rol, los permisos se recalculan, rigen desde el siguiente inicio de sesión y las sesiones activas se cierran.
- Un empleado pertenece a exactamente una sucursal; cambiarla reemplaza la anterior.
- Si el empleado tiene turnos futuros en la sucursal de origen, el sistema advierte antes de confirmar el traslado e indica la cantidad afectada; no los modifica.
- No se permite dejar el sistema sin al menos un usuario Activo con rol Administrador General.
- Un usuario no puede modificar su propio rol ni estado.
- Al inactivar, las sesiones activas se cierran de inmediato y no puede volver a iniciar sesión; su historial de turnos se conserva-
- Un empleado inactivo puede reactivarse conservando correo, documento e historial.
- El Administrador de Sucursal edita solo empleados de su sucursal, sin cambiarles la sucursal ni asignar el rol Administrador General.
- Toda modificación se registra con campo, valor anterior, valor nuevo, usuario y fecha-hora; si no hay cambios, no se registra nada.

### Criterios de aceptación

#### Antecedentes

```gherkin
  Dado que inicié sesión con el rol "Administrador General"
  Y existe el empleado "María Torres" con documento "1032456789", rol "Cajero",
    sucursal "Norte" y estado "Activo"
```

#### CA-04.1 Cambio de rol y traza en el historial

```gherkin
  Cuando cambio el campo "Rol" de "Cajero" a "Barista" y presiono "Guardar"
  Entonces el sistema muestra "Empleado actualizado correctamente."
  Y la ficha muestra el rol "Barista"
  Y el historial incluye una entrada con campo "Rol", valor anterior "Cajero",
    valor nuevo "Barista", el usuario ejecutor y la fecha-hora
```

#### CA-04.2 El cambio de rol reevalúa los permisos

```gherkin
  Dado que "María Torres" tenía rol "Administrador de Sucursal" y una sesión abierta
  Cuando cambio su rol a "Barista" y guardo
  Entonces su sesión abierta es cerrada
  Y al volver a iniciar sesión el menú NO contiene la opción "Empleados"
```

#### CA-04.3 Cambio de sucursal con turnos futuros programados

```gherkin
  Dado que "María Torres" tiene 3 turnos programados con fecha futura en "Norte"
  Cuando cambio su sucursal de "Norte" a "Sur" y presiono "Guardar"
  Entonces el sistema muestra "Este empleado tiene 3 turnos programados en Norte.
    Al trasladarlo deberás reprogramarlos. ¿Deseas continuar?" con las opciones
    "Continuar" y "Cancelar"
  Y si presiono "Cancelar" la sucursal permanece en "Norte"
  Y si presiono "Continuar" la sucursal cambia a "Sur" y los 3 turnos permanecen
    registrados sin modificación
```

#### CA-04.4 Inactivación y reactivación

```gherkin
  Dado que "María Torres" tiene estado "Activo" y una sesión abierta
  Cuando cambio su estado a "Inactivo" y guardo
  Entonces su sesión abierta es cerrada
  Y al intentar iniciar sesión con sus credenciales correctas el sistema muestra
    "Tu cuenta está inactiva. Comunícate con el Administrador General."
  Y su registro aparece al filtrar el listado por estado "Inactivo"
  Y al reactivarla el acceso vuelve a ser exitoso con el mismo correo y documento
```

#### CA-04.5 Reglas de integridad sobre el propio usuario y el último administrador

```gherkin
  Dado que "admin.general@granodorado.com" es el único Administrador General activo
  Cuando intento cambiar su estado a "Inactivo" y guardo
  Entonces el sistema muestra "Debe existir al menos un Administrador General activo."
  Y su estado permanece "Activo"
  Y al abrir mi propia ficha los campos "Rol" y "Estado" son de solo lectura
```

#### CA-04.6 Alcance del Administrador de Sucursal

```gherkin
  Dado que inicié sesión con el rol "Administrador de Sucursal" de "Centro"
  Cuando abro el listado de empleados
  Entonces solo se muestran empleados de la sucursal "Centro"
  Y al abrir una ficha el campo "Sucursal" es de solo lectura
  Y la lista "Rol" NO contiene la opción "Administrador General"
```

### RNF aplicables

- RNF-02 (Seguridad): solo el Administrador General autenticado edita o inactiva empleados; al cambiar el rol cambian las funciones a las que ese usuario accede.
- RNF-03 (Disponibilidad): el sistema está disponible al menos el 99 % del tiempo cada mes, salvo las ventanas de mantenimiento.
- RNF-04 (Compatibilidad): la pantalla se utiliza desde computador y desde dispositivo móvil mediante un navegador web.

### Dependencias

- HU-01 (sesión y matriz de permisos) y HU-03 (existencia de empleados).
- CA-04.3 requiere el módulo de turnos para contar los turnos futuros. Si aún no existe, entregar HU-04 sin la advertencia y añadirla como historia técnica posterior, documentándolo en el sprint.

### Riesgos

- Un cambio de sucursal o de rol sin reprogramar turnos reintroduce el conflicto de H-8 que el sistema pretende eliminar. Mitigación: advertencia obligatoria y priorizar la historia de reprogramación.

puede dejar bloqueado al único administrador si comete un error. Mitigación: procedimiento documentado de recuperación por soporte técnico.

- El historial de cambios crece indefinidamente. Mitigación: retención de 12 meses.

### Datos de prueba

- Cambio de rol: María Torres (1032456789), Cajero → Barista, Activo/Norte → actualizado con entrada en el historial.
- Reevaluación de permisos: Carla Ríos (1011223344), Administrador de Sucursal → Barista, con sesión abierta → sesión cerrada y menú reducido.
- Traslado sin turnos: María Torres, Norte → Sur, sin turnos futuros → actualizado sin advertencia.
- Traslado con turnos: Ana Pérez (1022334455), Centro → Norte, 3 turnos futuros → advertencia con el número 3.
- Inactivación y reactivación: Luis Gómez (1099887766) con sesión abierta → sesión cerrada y login rechazado; al reactivar, login exitoso.
- Correo duplicado: María Torres → ana.perez@granodorado.com → rechazado.
- Último administrador: admin.general@granodorado.com → Inactivo → rechazado.
- Alcance de sucursal: Carla Ríos (Admin Centro) abre el listado → solo empleados de Centro.

### Evaluación DoR

**Veredicto: NO LISTA.** Cumplen: formato, valor medible, alcance, 14 criterios verificables, RNF y datos de prueba.

- Bloqueante 2: decidir si CA-04.3 entra en esta historia o se difiere hasta que exista el módulo de turnos (no es independiente ni estimable sin esa decisión).
