# ☕ Grano Dorado — Gestor de Turnos Laborales (ITM)

Aplicación multiplataforma (**Web y Android**) desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform** para organizar los turnos, la asistencia y las novedades del personal de las cafeterías **Grano Dorado**.

---

## 🎯 ¿Qué hace este proyecto?

**Grano Dorado — Gestor de Turnos Laborales** reemplaza la planificación manual en hojas de cálculo y grupos de WhatsApp por una plataforma centralizada, clara y segura. Permite a los administradores planificar horarios sin errores ni sobrecargas, y a los colaboradores consultar sus turnos, marcar asistencia y gestionar solicitudes desde el computador o el celular.

### Problemas que resuelve
- Elimina confusiones sobre horarios publicados en chats o archivos desactualizados.
- Evita asignar turnos a personal en vacaciones, incapacidad, descanso o con exceso de horas semanales.
- Garantiza que cada sucursal mantenga cobertura continua durante su franja operativa (**06:00 a 22:00**).
- Centraliza novedades, intercambios de turno, control de asistencia y trazabilidad (auditoría) en un solo lugar.

---

## ⚙️ ¿Cómo funciona?

La aplicación adapta automáticamente su menú y permisos según el rol con el que la persona inicia sesión, manteniendo una interfaz adaptable (*responsive*) tanto para pantallas de escritorio como para dispositivos móviles.

### 1. Acceso y seguridad
- **Inicio de sesión** con correo corporativo y contraseña, o mediante **Google**.
- **Recuperación y cambio de contraseña** autogestionados por el usuario.
- **Protección de cuentas**: bloqueo temporal de 15 minutos tras 5 intentos fallidos, cierre automático tras 30 minutos de inactividad y bloqueo de acceso a cuentas inactivas.

### 2. Roles y funciones principales

| Rol | ¿Qué puede hacer en la aplicación? |
| :--- | :--- |
| **Administrador General** | Acceso total a todas las sucursales: panel de indicadores, gestión de empleados, catálogo global de turnos, malla semanal, revisión de novedades, aprobación de cambios de turno, control de asistencia y auditoría. |
| **Administrador de Sucursal** | Gestión operativa completa acotada únicamente a su sucursal: panel, personal de su sede, malla semanal, novedades, aprobación de intercambios, control de asistencia y auditoría. |
| **Empleado** *(Barista, Cajero, Cocinero, Supervisor)* | Portal de autogestión: consultar su horario asignado (**Mi horario**), solicitar o aceptar cambios de turno con compañeros, reportar novedades (permisos, incapacidades, etc.) y marcar su asistencia diaria. |

### 3. Flujo operativo del día a día
1. **Gestión de personal y turnos:** Los administradores registran a los colaboradores por sucursal y definen el catálogo de turnos reutilizables con sus descansos.
2. **Planificación en Malla Semanal:** Se asignan los turnos validando automáticamente reglas clave:
   - Cobertura completa entre **06:00 y 22:00** sin huecos operativos.
   - Máximo **1 turno por día** por colaborador y tope de **42 horas efectivas semanales**.
   - Bloqueo de asignaciones a personal inactivo, incapacitado, en vacaciones o en descanso.
3. **Portal del empleado:** Cada colaborador ve sus turnos publicados, puede proponer intercambios con compañeros (que luego aprueba un administrador) y reportar novedades que ajustan la operación.
4. **Control de asistencia y auditoría:** Los empleados registran inicio de turno, descansos y salida; el sistema calcula la jornada, alerta posibles incumplimientos y guarda un historial de auditoría de las acciones relevantes.

---

## 📦 Dependencias principales

Todas las versiones se centralizan en [`gradle/libs.versions.toml`](./gradle/libs.versions.toml).

| Categoría | Dependencia | Versión | Uso en el proyecto |
| :--- | :--- | :---: | :--- |
| **Lenguaje y KMP** | Kotlin Multiplatform & Serialization | `2.4.20` | Código compartido entre Android y Web |
| **Interfaz (UI)** | Compose Multiplatform (UI, Foundation, Runtime, Resources) | `1.12.0` | Interfaz declarativa compartida 100% en `commonMain` |
| **Diseño** | Material 3 (`compose-material3`) | `1.12.0-alpha03` | Componentes y tema visual de Grano Dorado |
| **Iconografía** | Material Icons Extended | `1.7.3` | Íconos de navegación y estados |
| **Arquitectura UI** | Navigation Compose (`org.jetbrains.androidx.navigation`) | `2.9.2` | Navegación tipada entre pantallas |
| **Estado y Ciclo de vida** | Lifecycle ViewModel & Runtime Compose | `2.11.0` | Gestión reactiva de estado (`StateFlow`) |
| **Datos y Fechas** | `kotlinx-datetime` / `kotlinx-serialization-json` | `0.8.0` / `1.8.0` | Manejo de fechas/horarios y serialización con Firestore |
| **Backend (Firebase)** | GitLive Firebase KMP (`auth`, `firestore`, `messaging`, `common`) | `2.1.0` | Autenticación y base de datos en tiempo real multiplataforma |
| **Firebase Android / Web** | Firebase BoM (Android) / Firebase JS Compat (Web CDN) | `33.4.0` / `10.12.0` | SDKs nativos subyacentes por plataforma |
| **Web (JS)** | `kotlin-wrappers:kotlin-browser` | `2026.9.1` | Interoperabilidad con APIs del navegador |
| **Android** | Android Gradle Plugin / `androidx.activity-compose` | `9.1.1` / `1.13.0` | Empaquetado y host de Compose en Android |
| **Pruebas** | `kotlin-test` / JUnit | `2.4.20` / `4.13.2` | Suite de pruebas unitarias de reglas de negocio |

---

## 🛠️ Documentación técnica mínima

### Estructura del proyecto
El proyecto sigue una estructura estándar de **Kotlin Multiplatform** con separación en capas dentro del módulo compartido:

```text
Gestor de Turnos Laborales ITM/
├── shared/                     # Lógica e interfaz compartidas (Android + Web)
│   └── src/
│       ├── commonMain/kotlin/com/itm/gestordeturnos/
│       │   ├── domain/         # Modelos, contratos de repositorio y casos de uso (UseCases)
│       │   ├── data/           # Repositorios, servicios Firebase (Auth/Firestore) y datos mock
│       │   ├── viewmodel/      # ViewModels con StateFlow y SessionManager
│       │   ├── screens/        # Pantallas en Compose Multiplatform
│       │   ├── components/     # Componentes visuales reutilizables
│       │   └── Platform.kt     # Declaraciones expect para código específico de plataforma
│       ├── androidMain/        # Implementaciones actual para Android
│       ├── jsMain/             # Implementaciones actual para Web (Kotlin/JS + Firebase Web)
│       └── commonTest/         # 151 pruebas unitarias determinísticas de la capa de dominio
├── androidApp/                 # Aplicación Android (minSdk 24, targetSdk/compileSdk 37)
├── webApp/                     # Aplicación Web en Kotlin/JS (HTML, CSS y config de Firebase)
├── firebase.json               # Configuración de Firebase Hosting
└── firestore.rules             # Reglas de seguridad de Cloud Firestore
```

### Arquitectura en resumen
- **Patrón:** MVVM + Arquitectura Limpia simplificada (`UI / Compose` → `ViewModel` → `UseCases / Domain` → `Repository` → `Firebase`).
- **Sincronización y resiliencia:** Los repositorios escuchan cambios en tiempo real desde **Cloud Firestore** mediante `Flow` y mantienen estado local en memoria (`StateFlow`) con datos iniciales de respaldo (`MockDataProviders`) para ofrecer fluidez y soporte en entornos locales.
- **Nota sobre Web (Kotlin/JS):** El target web compila a **Kotlin/JS** (en lugar de WasmJS) para garantizar compatibilidad directa con el SDK de Firebase Web y los flujos de autenticación. Existe un alias de Gradle (`wasmJsBrowserDevelopmentRun`) que redirige a `jsBrowserDevelopmentRun` por conveniencia del IDE.
- **Colecciones en Cloud Firestore:** `empleados`, `turnos`, `asignaciones`, `asistencia`, `novedades`, `solicitudes_cambio`, `incumplimientos` y `auditoria`.

---

## 🚀 Requisitos y puesta en marcha

### Prerrequisitos
- **JDK 21** (el proyecto utiliza Gradle Daemon Toolchain con Azul JDK 21; Android compila a `JVM 17`).
- **Android Studio** o **IntelliJ IDEA** con soporte para Kotlin Multiplatform (opcional si ejecutas por terminal).
- Un proyecto configurado en **Firebase Console** con **Authentication** (Correo/Contraseña y Google) y **Cloud Firestore** habilitados.

### 1. Configurar credenciales de Firebase
Por seguridad, los archivos con credenciales están ignorados en `.gitignore`. Antes de ejecutar:

- **Para Web:** Copia la plantilla y completa tus credenciales de Firebase Console:
  ```bash
  cp webApp/src/webMain/resources/firebase-config.example.js webApp/src/webMain/resources/firebase-config.js
  ```
- **Para Android:** Descarga el archivo `google-services.json` desde Firebase Console y colócalo en `androidApp/google-services.json`.

### 2. Ejecutar la aplicación

> En Windows PowerShell utiliza `.\gradlew.bat` en lugar de `./gradlew`.

- **Web (desarrollo local en navegador):**
  ```bash
  ./gradlew :webApp:jsBrowserDevelopmentRun
  ```
- **Android (compilar APK de depuración):**
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```

### 3. Ejecutar pruebas unitarias
La capa de dominio cuenta con **151 pruebas unitarias** en `shared/src/commonTest` que validan reglas de negocio, cobertura horaria, descansos, cambios de turno, asistencia y seguridad.

- **Pruebas en JVM / Android Host:**
  ```bash
  ./gradlew :shared:testAndroidHostTest
  ```
- **Pruebas en Web (JS):**
  ```bash
  ./gradlew :shared:jsTest
  ```

### 4. Despliegue en Firebase Hosting (Web)
1. Genera la distribución web:
   ```bash
   ./gradlew :webApp:jsBrowserDevelopmentDistribution
   ```
   *(Para producción, usa `:webApp:jsBrowserProductionDistribution` y apunta `hosting.public` en `firebase.json` a `webApp/build/dist/js/productionExecutable`.)*
2. Despliega con Firebase CLI:
   ```bash
   firebase deploy
   ```