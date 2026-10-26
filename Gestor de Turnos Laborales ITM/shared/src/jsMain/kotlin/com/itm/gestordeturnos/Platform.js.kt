package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.model.EstadoAsistencia
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import com.itm.gestordeturnos.domain.model.RangoDescanso
import com.itm.gestordeturnos.domain.model.Cargo
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoEmpleado
import com.itm.gestordeturnos.domain.model.EstadoNovedad
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.EstadoSolicitudCambio
import com.itm.gestordeturnos.domain.model.SolicitudCambioTurno
import com.itm.gestordeturnos.domain.model.NotificacionIncumplimiento
import com.itm.gestordeturnos.domain.model.Novedad
import kotlinx.datetime.LocalDate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.suspendCancellableCoroutine
import web.navigator.navigator
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class JsPlatform: Platform {
    private val userAgent = navigator.userAgent
    private val browserList = listOf("Chrome", "Firefox", "Safari", "Edge")

    override val name: String = userAgent.findAnyOf(browserList, ignoreCase = true)
            ?.let { (startIndex) -> userAgent.substring(startIndex).substringBefore(" ") }
            ?: "Unknown"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun initPlatformFirebase() {
    try {
        val config = js("window.firebaseConfig")
        if (config != null && config != js("undefined")) {
            val apiKey = config.apiKey as? String
            val authDomain = config.authDomain as? String
            val projectId = config.projectId as? String
            val storageBucket = config.storageBucket as? String
            val messagingSenderId = config.messagingSenderId as? String
            val appId = config.appId as? String

            if (!apiKey.isNullOrBlank() && !projectId.isNullOrBlank() && !appId.isNullOrBlank()) {
                val options = FirebaseOptions(
                    apiKey = apiKey,
                    authDomain = authDomain,
                    projectId = projectId,
                    storageBucket = storageBucket,
                    gcmSenderId = messagingSenderId,
                    applicationId = appId
                )
                Firebase.initialize(options = options)
            }
        }
    } catch (_: Throwable) {
        // Ignorado si ya está inicializado o si hubo un error en JS
    }
}

actual suspend fun iniciarSesionGooglePlataforma(): GoogleUserData? = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = { result ->
        if (result != null && result.user != null) {
            val user = result.user
            val email = user.email as? String
            val displayName = user.displayName as? String
            if (email != null) {
                continuation.resume(GoogleUserData(email = email, displayName = displayName))
            } else {
                continuation.resume(null)
            }
        } else {
            continuation.resume(null)
        }
    }

    val onError: (dynamic) -> Unit = { error ->
        val code = error?.code as? String ?: ""
        if (code == "auth/popup-closed-by-user" || code == "auth/cancelled-popup-request") {
            continuation.resume(null)
        } else {
            val message = error?.message as? String ?: "Error en autenticación con Google"
            continuation.resumeWithException(Exception(message))
        }
    }

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.auth === 'function') {
                var provider = new firebase.auth.GoogleAuthProvider();
                provider.addScope('email');
                provider.addScope('profile');
                firebase.auth().signInWithPopup(provider)
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onSuccess(null);
            }
        } catch (e) {
            onError(e);
        }
    """)
}

private fun parsearIncumplimientoJs(d: dynamic): NotificacionIncumplimiento? {
    if (d == null || d == js("undefined")) return null
    return try {
        val id = (d.id as? String) ?: ""
        val empId = (d.empleadoId as? String) ?: ""
        val empNombre = (d.empleadoNombre as? String) ?: (d.empleado as? String) ?: ""
        val tipo = (d.tipo as? String) ?: ""
        val detalleHora = (d.detalleHora as? String) ?: ""
        val fechaIso = (d.fechaIso as? String) ?: ""
        val sucursal = (d.sucursal as? String) ?: "CENTRO"
        val atendida = (d.atendida as? Boolean) ?: false
        val motivoAtencion = (d.motivoAtencion as? String) ?: ""
        val horaAtencion = (d.horaAtencion as? String) ?: ""

        NotificacionIncumplimiento(
            id = id,
            empleadoId = empId,
            empleado = empNombre,
            tipo = tipo,
            detalleHora = detalleHora,
            fechaIso = fechaIso,
            sucursal = sucursal,
            atendida = atendida,
            motivoAtencion = motivoAtencion,
            horaAtencion = horaAtencion
        )
    } catch (_: Throwable) {
        null
    }
}

actual suspend fun guardarIncumplimientoFirestore(notificacion: NotificacionIncumplimiento): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val id = if (notificacion.id.isNotBlank()) notificacion.id else "${notificacion.empleadoId}_${notificacion.fechaIso}_${notificacion.tipo}"
    val empId = notificacion.empleadoId
    val empNombre = notificacion.empleado
    val tipo = notificacion.tipo
    val detalleHora = notificacion.detalleHora
    val fechaIso = notificacion.fechaIso
    val sucursal = notificacion.sucursal
    val atendida = notificacion.atendida
    val motivoAtencion = notificacion.motivoAtencion
    val horaAtencion = notificacion.horaAtencion

    val onSuccess: (dynamic) -> Unit = {
        println("Incumplimiento guardado con éxito en Firestore: $id")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error al guardar incumplimiento en Firestore"
        println("Error guardando incumplimiento en Firestore ($id): $msg")
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                var data = {
                    id: String(id || ''),
                    empleadoId: String(empId || ''),
                    empleadoNombre: String(empNombre || ''),
                    empleado: String(empNombre || ''),
                    tipo: String(tipo || ''),
                    detalleHora: String(detalleHora || ''),
                    fechaIso: String(fechaIso || ''),
                    sucursal: String(sucursal || 'CENTRO'),
                    atendida: Boolean(atendida),
                    motivoAtencion: String(motivoAtencion || ''),
                    horaAtencion: String(horaAtencion || ''),
                    timestamp: Date.now()
                };
                console.log("Enviando incumplimiento a Firestore /incumplimientos/" + id, data);
                db.collection("incumplimientos").doc(id).set(data)
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onError(new Error("Firebase Firestore no está disponible en window"));
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual suspend fun obtenerIncumplimientosFirestore(): List<NotificacionIncumplimiento> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<NotificacionIncumplimiento>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val inc = parsearIncumplimientoJs(data)
                    if (inc != null) {
                        lista.add(inc)
                    }
                }
            }
        }
        println("Incumplimientos obtenidos de Firestore: " + lista.size)
        continuation.resume(lista)
    }
    val onError: (dynamic) -> Unit = { error ->
        println("Error obteniendo incumplimientos de Firestore: " + error)
        continuation.resume(emptyList())
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("incumplimientos").get()
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onSuccess({ docs: [] });
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual fun observarIncumplimientosFirestore(onUpdate: (List<NotificacionIncumplimiento>) -> Unit) {
    val callback: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<NotificacionIncumplimiento>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val inc = parsearIncumplimientoJs(data)
                    if (inc != null) {
                        lista.add(inc)
                    }
                }
            }
        }
        println("Cambio en tiempo real de Firestore /incumplimientos, total: " + lista.size)
        onUpdate(lista)
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("incumplimientos").onSnapshot(callback, function(err) {
                    console.error("Error en onSnapshot /incumplimientos de Firestore:", err);
                });
            }
        } catch(e) {
            console.error("Error suscribiendo a Firestore /incumplimientos:", e);
        }
    """)
}

private fun parsearSolicitudCambioJs(d: dynamic): SolicitudCambioTurno? {
    if (d == null || d == js("undefined")) return null
    return try {
        val id = (d.id as? String) ?: ""
        val numeroSolicitud = (d.numeroSolicitud as? String) ?: ""
        val fechaSolicitudStr = (d.fechaSolicitud as? String) ?: (d.fechaSolicitudIso as? String) ?: ""
        val fechaSolicitud = try {
            if (fechaSolicitudStr.isNotBlank()) LocalDate.parse(fechaSolicitudStr.take(10)) else null
        } catch (_: Throwable) {
            null
        }
        val estadoStr = (d.estado as? String) ?: "PENDIENTE"
        val estadoEnum = try { EstadoSolicitudCambio.valueOf(estadoStr) } catch (_: Throwable) { EstadoSolicitudCambio.PENDIENTE }
        val motivoRechazo = (d.motivoRechazo as? String) ?: ""
        val motivo = (d.motivo as? String) ?: ""
        val empleadoSolicitanteId = (d.empleadoSolicitanteId as? String) ?: ""
        val nombreSolicitante = (d.nombreSolicitante as? String) ?: ""
        val empleadoCompaneroId = (d.empleadoCompaneroId as? String) ?: ""
        val nombreCompanero = (d.nombreCompanero as? String) ?: ""
        val turnoEntregaId = (d.turnoEntregaId as? String) ?: ""
        val tipoTurnoEntrega = (d.tipoTurnoEntrega as? String) ?: ""
        val detalleTurnoEntrega = (d.detalleTurnoEntrega as? String) ?: ""
        val turnoRecibeId = (d.turnoRecibeId as? String) ?: ""
        val tipoTurnoRecibe = (d.tipoTurnoRecibe as? String) ?: ""
        val detalleTurnoRecibe = (d.detalleTurnoRecibe as? String) ?: ""
        val sucursalNombre = (d.sucursalNombre as? String) ?: (d.sucursal as? String) ?: "CENTRO"
        val validacionesOk = when (val v = d.validacionesOk) {
            is Boolean -> v
            is String -> v.toBoolean()
            else -> true
        }
        val esRemitente = when (val r = d.esRemitente) {
            is Boolean -> r
            is String -> r.toBoolean()
            else -> false
        }

        SolicitudCambioTurno(
            id = id,
            numeroSolicitud = numeroSolicitud,
            fechaSolicitud = fechaSolicitud,
            estado = estadoEnum,
            motivoRechazo = motivoRechazo,
            motivo = motivo,
            empleadoSolicitanteId = empleadoSolicitanteId,
            nombreSolicitante = nombreSolicitante,
            empleadoCompaneroId = empleadoCompaneroId,
            nombreCompanero = nombreCompanero,
            turnoEntregaId = turnoEntregaId,
            tipoTurnoEntrega = tipoTurnoEntrega,
            detalleTurnoEntrega = detalleTurnoEntrega,
            turnoRecibeId = turnoRecibeId,
            tipoTurnoRecibe = tipoTurnoRecibe,
            detalleTurnoRecibe = detalleTurnoRecibe,
            sucursalNombre = sucursalNombre,
            validacionesOk = validacionesOk,
            esRemitente = esRemitente
        )
    } catch (_: Throwable) {
        null
    }
}

actual suspend fun guardarSolicitudCambioFirestore(solicitud: SolicitudCambioTurno): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val id = if (solicitud.id.isNotBlank()) solicitud.id else "sol_${kotlin.random.Random.nextInt(100000, 999999)}"
    val numeroSolicitud = if (solicitud.numeroSolicitud.isNotBlank()) solicitud.numeroSolicitud else "SOL-${kotlin.random.Random.nextInt(1000, 9999)}"
    val fechaSolicitudStr = solicitud.fechaSolicitud?.toString() ?: ""
    val estado = solicitud.estado.name
    val motivoRechazo = solicitud.motivoRechazo
    val motivo = solicitud.motivo
    val empleadoSolicitanteId = solicitud.empleadoSolicitanteId
    val nombreSolicitante = solicitud.nombreSolicitante
    val empleadoCompaneroId = solicitud.empleadoCompaneroId
    val nombreCompanero = solicitud.nombreCompanero
    val turnoEntregaId = solicitud.turnoEntregaId
    val tipoTurnoEntrega = solicitud.tipoTurnoEntrega
    val detalleTurnoEntrega = solicitud.detalleTurnoEntrega
    val turnoRecibeId = solicitud.turnoRecibeId
    val tipoTurnoRecibe = solicitud.tipoTurnoRecibe
    val detalleTurnoRecibe = solicitud.detalleTurnoRecibe
    val sucursalNombre = solicitud.sucursalNombre
    val validacionesOk = solicitud.validacionesOk
    val esRemitente = solicitud.esRemitente

    val onSuccess: (dynamic) -> Unit = {
        println("Solicitud de cambio guardada con éxito en Firestore: $id")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error al guardar solicitud de cambio en Firestore"
        println("Error guardando solicitud de cambio en Firestore ($id): $msg")
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                var data = {
                    id: String(id || ''),
                    numeroSolicitud: String(numeroSolicitud || ''),
                    fechaSolicitud: String(fechaSolicitudStr || ''),
                    fechaSolicitudIso: String(fechaSolicitudStr || ''),
                    estado: String(estado || 'PENDIENTE'),
                    motivoRechazo: String(motivoRechazo || ''),
                    motivo: String(motivo || ''),
                    empleadoSolicitanteId: String(empleadoSolicitanteId || ''),
                    nombreSolicitante: String(nombreSolicitante || ''),
                    empleadoCompaneroId: String(empleadoCompaneroId || ''),
                    nombreCompanero: String(nombreCompanero || ''),
                    turnoEntregaId: String(turnoEntregaId || ''),
                    tipoTurnoEntrega: String(tipoTurnoEntrega || ''),
                    detalleTurnoEntrega: String(detalleTurnoEntrega || ''),
                    turnoRecibeId: String(turnoRecibeId || ''),
                    tipoTurnoRecibe: String(tipoTurnoRecibe || ''),
                    detalleTurnoRecibe: String(detalleTurnoRecibe || ''),
                    sucursalNombre: String(sucursalNombre || 'CENTRO'),
                    sucursal: String(sucursalNombre || 'CENTRO'),
                    validacionesOk: Boolean(validacionesOk),
                    esRemitente: Boolean(esRemitente),
                    timestamp: Date.now()
                };
                console.log("Enviando solicitud de cambio a Firestore /solicitudes_cambio/" + id, data);
                db.collection("solicitudes_cambio").doc(id).set(data)
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onError(new Error("Firebase Firestore no está disponible en window"));
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual suspend fun obtenerSolicitudesCambioFirestore(): List<SolicitudCambioTurno> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<SolicitudCambioTurno>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val s = parsearSolicitudCambioJs(data)
                    if (s != null) {
                        lista.add(s)
                    }
                }
            }
        }
        println("Solicitudes de cambio obtenidas de Firestore: " + lista.size)
        continuation.resume(lista)
    }
    val onError: (dynamic) -> Unit = { error ->
        println("Error obteniendo solicitudes de cambio de Firestore: " + error)
        continuation.resume(emptyList())
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("solicitudes_cambio").get()
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onSuccess({ docs: [] });
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual fun observarSolicitudesCambioFirestore(onUpdate: (List<SolicitudCambioTurno>) -> Unit) {
    val callback: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<SolicitudCambioTurno>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val s = parsearSolicitudCambioJs(data)
                    if (s != null) {
                        lista.add(s)
                    }
                }
            }
        }
        println("Cambio en tiempo real de Firestore /solicitudes_cambio, total: " + lista.size)
        onUpdate(lista)
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("solicitudes_cambio").onSnapshot(callback, function(err) {
                    console.error("Error en onSnapshot /solicitudes_cambio de Firestore:", err);
                });
            }
        } catch(e) {
            console.error("Error suscribiendo a Firestore /solicitudes_cambio:", e);
        }
    """)
}

actual suspend fun enviarRecuperacionPlataforma(correo: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = {
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = error?.message as? String ?: "Error al enviar correo de restablecimiento"
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.auth === 'function') {
                firebase.auth().sendPasswordResetEmail(correo)
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onSuccess(null);
            }
        } catch (e) {
            onError(e);
        }
    """)
}

actual suspend fun registrarCuentaEmpleadoPlataforma(correo: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = {
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = error?.message as? String ?: "Error al registrar empleado en autenticación"
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.initializeApp === 'function' && window.firebaseConfig) {
                var appName = 'EmpleadoRegApp_' + Date.now();
                var secondaryApp = firebase.initializeApp(window.firebaseConfig, appName);
                var tempPassword = 'Tmp#' + Math.random().toString(36).substring(2, 12) + 'Z9!';

                secondaryApp.auth().createUserWithEmailAndPassword(correo, tempPassword)
                    .then(function() {
                        return secondaryApp.auth().sendPasswordResetEmail(correo);
                    })
                    .then(function() {
                        return secondaryApp.delete();
                    })
                    .then(function() {
                        onSuccess(null);
                    })
                    .catch(function(err) {
                        try { secondaryApp.delete(); } catch(e) {}
                        if (err && (err.code === 'auth/email-already-in-use' || (err.message && err.message.indexOf('email-already-in-use') !== -1))) {
                            // Si el correo ya existía en Firebase Auth, enviamos el correo de restablecimiento directamente
                            if (firebase.auth) {
                                firebase.auth().sendPasswordResetEmail(correo)
                                    .then(function() { onSuccess(null); })
                                    .catch(onError);
                            } else {
                                onSuccess(null);
                            }
                        } else {
                            onError(err);
                        }
                    });
            } else {
                onSuccess(null);
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual fun cerrarSesionPlataforma() {
    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.auth === 'function') {
                firebase.auth().signOut();
            }
        } catch(e) {}
    """)
}

private fun parsearEmpleadoJs(d: dynamic): Empleado? {
    if (d == null || d == js("undefined")) return null
    return try {
        val id = (d.id as? String) ?: ""
        val nombre = (d.nombre as? String) ?: ""
        val apellido = (d.apellido as? String) ?: ""
        val docNum = when (val docVal = d.documento) {
            is Number -> docVal.toLong()
            is String -> docVal.toLongOrNull() ?: 0L
            else -> 0L
        }
        val tipoDoc = (d.tipoDocumento as? String) ?: "CC"
        val correo = (d.correo as? String) ?: ""
        val tel = (d.telefono as? String) ?: ""
        val cargoStr = (d.cargo as? String) ?: "BARISTA"
        val cargoEnum = Cargo.entries.find { it.name.equals(cargoStr, ignoreCase = true) || it.displayName.equals(cargoStr, ignoreCase = true) } ?: Cargo.BARISTA
        val sucursal = (d.sucursal as? String) ?: (d.sucursalId as? String) ?: "CENTRO"
        val sucursalId = (d.sucursalId as? String) ?: sucursal
        val sucursalAnterior = (d.sucursalAnterior as? String) ?: ""
        val fechaCambioSucursal = (d.fechaCambioSucursal as? String) ?: ""
        val fechaIngreso = (d.fechaIngreso as? String) ?: "15/01/2024"
        val estadoStr = (d.estado as? String) ?: "ACTIVO"
        val estadoEnum = EstadoUsuario.entries.find { it.name.equals(estadoStr, ignoreCase = true) || it.displayName.equals(estadoStr, ignoreCase = true) } ?: EstadoUsuario.ACTIVO
        val estadoLabStr = (d.estadoLaboral as? String) ?: "DISPONIBLE"
        val estadoLabEnum = EstadoEmpleado.entries.find { it.name.equals(estadoLabStr, ignoreCase = true) || it.displayName.equals(estadoLabStr, ignoreCase = true) } ?: EstadoEmpleado.DISPONIBLE
        val iniciales = (d.iniciales as? String) ?: ""

        Empleado(
            id = id,
            nombre = nombre,
            apellido = apellido,
            documento = docNum,
            tipoDocumento = tipoDoc,
            correo = correo,
            telefono = tel,
            cargo = cargoEnum,
            sucursal = sucursal,
            sucursalId = sucursalId,
            sucursalAnterior = sucursalAnterior,
            fechaCambioSucursal = fechaCambioSucursal,
            fechaIngreso = fechaIngreso,
            estado = estadoEnum,
            estadoLaboral = estadoLabEnum,
            iniciales = iniciales
        )
    } catch (_: Throwable) {
        null
    }
}

actual suspend fun guardarEmpleadoFirestore(empleado: Empleado): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val id = empleado.id
    val nombre = empleado.nombre
    val apellido = empleado.apellido
    val docDouble = empleado.documento.toDouble()
    val tipoDocumento = empleado.tipoDocumento
    val correo = empleado.correo
    val telefono = empleado.telefono
    val cargo = empleado.cargo.name
    val sucursal = empleado.sucursal
    val sucursalId = empleado.sucursalId.ifBlank { sucursal }
    val sucursalAnterior = empleado.sucursalAnterior
    val fechaCambioSucursal = empleado.fechaCambioSucursal
    val fechaIngreso = empleado.fechaIngreso
    val estado = empleado.estado.name
    val estadoLaboral = empleado.estadoLaboral.name
    val iniciales = empleado.iniciales

    val onSuccess: (dynamic) -> Unit = {
        println("Documento guardado con éxito en Firestore: $id")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: "Error al guardar empleado en Firestore"
        println("Error guardando en Firestore ($id): $msg")
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                var data = {
                    id: id,
                    nombre: nombre,
                    apellido: apellido,
                    documento: docDouble,
                    tipoDocumento: tipoDocumento,
                    correo: correo,
                    telefono: telefono,
                    cargo: cargo,
                    sucursal: sucursal,
                    sucursalId: sucursalId,
                    sucursalAnterior: sucursalAnterior,
                    fechaCambioSucursal: fechaCambioSucursal,
                    fechaIngreso: fechaIngreso,
                    estado: estado,
                    estadoLaboral: estadoLaboral,
                    iniciales: iniciales
                };
                console.log("Enviando a Firestore /empleados/" + id, data);
                db.collection("empleados").doc(id).set(data)
                    .then(onSuccess)
                    .catch(onError);
            } else {
                console.warn("firebase.firestore no está disponible en window");
                onError(new Error("Firebase Firestore no está disponible en window"));
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual suspend fun obtenerEmpleadosFirestore(): List<Empleado> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<Empleado>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val emp = parsearEmpleadoJs(data)
                    if (emp != null) {
                        lista.add(emp)
                    }
                }
            }
        }
        println("Empleados obtenidos de Firestore: " + lista.size)
        continuation.resume(lista)
    }
    val onError: (dynamic) -> Unit = { error ->
        println("Error obteniendo empleados de Firestore: " + error)
        continuation.resume(emptyList())
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("empleados").get()
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onSuccess({ docs: [] });
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual fun observarEmpleadosFirestore(onUpdate: (List<Empleado>) -> Unit) {
    val callback: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<Empleado>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val emp = parsearEmpleadoJs(data)
                    if (emp != null) {
                        lista.add(emp)
                    }
                }
            }
        }
        println("Cambio en tiempo real de Firestore /empleados, total: " + lista.size)
        onUpdate(lista)
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("empleados").onSnapshot(callback, function(err) {
                    console.error("Error en onSnapshot de Firestore:", err);
                });
            }
        } catch(e) {
            console.error("Error suscribiendo a Firestore:", e);
        }
    """)
}

private fun parsearTurnoJs(d: dynamic): TurnoTemplate? {
    if (d == null || d == js("undefined")) return null
    return try {
        val id = (d.id as? String) ?: ""
        val nombre = (d.nombre as? String) ?: ""
        val horario = (d.horario as? String) ?: ""
        val descansos = (d.descansos as? String) ?: "-"
        val horasEfectivas = (d.horasEfectivas as? String) ?: ""
        val estado = (d.estado as? String) ?: "ACTIVO"
        val asignaciones = when (val a = d.asignaciones) {
            is Number -> a.toInt()
            is String -> a.toIntOrNull() ?: 0
            else -> 0
        }
        val horaInicio = (d.horaInicio as? String) ?: ""
        val horaFin = (d.horaFin as? String) ?: ""

        val descansosList = mutableListOf<RangoDescanso>()
        val arr = d.listaDescansos
        if (arr != null && js("Array.isArray(arr)")) {
            val jsArr = arr as Array<dynamic>
            for (item in jsArr) {
                if (item != null) {
                    val dId = (item.id as? String) ?: ""
                    val dIni = (item.horaInicio as? String) ?: ""
                    val dFin = (item.horaFin as? String) ?: ""
                    val finalId = if (dId.isNotBlank()) dId else "d_${kotlin.random.Random.nextInt(1000, 9999)}"
                    descansosList.add(RangoDescanso(id = finalId, horaInicio = dIni, horaFin = dFin))
                }
            }
        }

        TurnoTemplate(
            id = id,
            nombre = nombre,
            horario = horario,
            descansos = descansos,
            horasEfectivas = horasEfectivas,
            estado = estado,
            asignaciones = asignaciones,
            horaInicio = horaInicio,
            horaFin = horaFin,
            listaDescansos = descansosList
        )
    } catch (e: Throwable) {
        println("Error parseando turno de Firestore: " + e.message)
        null
    }
}

actual suspend fun guardarTurnoFirestore(turno: TurnoTemplate): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val id = if (turno.id.isNotBlank()) turno.id else "t_${kotlin.random.Random.nextInt(1000, 9999)}"
    val nombre = turno.nombre
    val horario = turno.horario
    val descansos = turno.descansos
    val horasEfectivas = turno.horasEfectivas
    val estado = turno.estado
    val asignacionesNum = turno.asignaciones.toDouble()
    val horaInicio = turno.horaInicio
    val horaFin = turno.horaFin

    val onSuccess: (dynamic) -> Unit = {
        println("Turno guardado con éxito en Firestore: $id")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error al guardar turno en Firestore"
        println("Error guardando turno en Firestore ($id): $msg")
        continuation.resume(Result.failure(Exception(msg)))
    }

    val descansosJs: dynamic = js("[]")
    for (d in turno.listaDescansos) {
        val dObj: dynamic = js("({})")
        dObj.id = d.id
        dObj.horaInicio = d.horaInicio
        dObj.horaFin = d.horaFin
        descansosJs.push(dObj)
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                var data = {
                    id: String(id || ''),
                    nombre: String(nombre || ''),
                    horario: String(horario || ''),
                    descansos: String(descansos || '-'),
                    horasEfectivas: String(horasEfectivas || ''),
                    estado: String(estado || 'ACTIVO'),
                    asignaciones: Number(asignacionesNum || 0),
                    horaInicio: String(horaInicio || ''),
                    horaFin: String(horaFin || ''),
                    listaDescansos: descansosJs
                };
                console.log("Enviando turno a Firestore /turnos/" + id, data);
                db.collection("turnos").doc(id).set(data)
                    .then(onSuccess)
                    .catch(function(err) {
                        console.error("Firestore fallo al guardar en /turnos/" + id + ":", err);
                        onError(err);
                    });
            } else {
                var errNoDb = new Error("Firebase Firestore no está disponible en window");
                console.error(errNoDb);
                onError(errNoDb);
            }
        } catch(e) {
            console.error("Excepción en guardarTurnoFirestore:", e);
            onError(e);
        }
    """)
}

actual suspend fun obtenerTurnosFirestore(): List<TurnoTemplate> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<TurnoTemplate>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val docId = doc.id as? String
                    if ((data.id as? String).isNullOrBlank() && !docId.isNullOrBlank()) {
                        data.id = docId
                    }
                    val t = parsearTurnoJs(data)
                    if (t != null) {
                        lista.add(t)
                    }
                }
            }
        }
        println("Turnos obtenidos de Firestore: " + lista.size)
        continuation.resume(lista)
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error obteniendo turnos de Firestore"
        println("Error obteniendo turnos de Firestore: " + msg)
        continuation.resume(emptyList())
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("turnos").get()
                    .then(onSuccess)
                    .catch(function(err) {
                        console.error("Firestore fallo al obtener /turnos:", err);
                        onError(err);
                    });
            } else {
                onSuccess({ docs: [] });
            }
        } catch(e) {
            console.error("Excepción en obtenerTurnosFirestore:", e);
            onError(e);
        }
    """)
}

actual fun observarTurnosFirestore(onUpdate: (List<TurnoTemplate>) -> Unit) {
    val callback: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<TurnoTemplate>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val docId = doc.id as? String
                    if ((data.id as? String).isNullOrBlank() && !docId.isNullOrBlank()) {
                        data.id = docId
                    }
                    val t = parsearTurnoJs(data)
                    if (t != null) {
                        lista.add(t)
                    }
                }
            }
        }
        println("Cambio en tiempo real de Firestore /turnos, total: " + lista.size)
        onUpdate(lista)
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("turnos").onSnapshot(callback, function(err) {
                    console.error("Error en onSnapshot /turnos de Firestore:", err);
                });
            }
        } catch(e) {
            console.error("Error suscribiendo a Firestore /turnos:", e);
        }
    """)
}

private fun parsearAsignacionJs(d: dynamic): AsignacionTurno? {
    if (d == null || d == js("undefined")) return null
    return try {
        val id = (d.id as? String) ?: ""
        val fechaIso = (d.fechaIso as? String) ?: ""
        val empleadoId = (d.empleadoId as? String) ?: ""
        val empleadoNombre = (d.empleadoNombre as? String) ?: ""
        val turnoId = (d.turnoId as? String) ?: ""
        val tipoTurno = (d.tipoTurno as? String) ?: ""
        val horario = (d.horario as? String) ?: ""
        val sucursalId = (d.sucursalId as? String) ?: ""
        val programacionId = (d.programacionId as? String) ?: ""
        val esDescanso = (d.esDescanso as? Boolean) ?: false
        val estado = (d.estado as? String) ?: "BORRADOR"
        val modificadoTrasPublicar = (d.modificadoTrasPublicar as? Boolean) ?: false

        AsignacionTurno(
            id = id,
            fechaIso = fechaIso,
            empleadoId = empleadoId,
            empleadoNombre = empleadoNombre,
            turnoId = turnoId,
            tipoTurno = tipoTurno,
            horario = horario,
            sucursalId = sucursalId,
            programacionId = programacionId,
            esDescanso = esDescanso,
            estado = estado,
            modificadoTrasPublicar = modificadoTrasPublicar
        )
    } catch (e: Throwable) {
        println("Error parseando asignacion de Firestore: " + e.message)
        null
    }
}

actual suspend fun guardarAsignacionFirestore(asignacion: AsignacionTurno): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val id = if (asignacion.id.isNotBlank()) asignacion.id else "${asignacion.empleadoId}_${asignacion.fechaIso}"
    val fechaIso = asignacion.fechaIso
    val empleadoId = asignacion.empleadoId
    val empleadoNombre = asignacion.empleadoNombre
    val turnoId = asignacion.turnoId
    val tipoTurno = asignacion.tipoTurno
    val horario = asignacion.horario
    val sucursalId = asignacion.sucursalId
    val programacionId = asignacion.programacionId
    val esDescanso = asignacion.esDescanso
    val estado = asignacion.estado
    val modificadoTrasPublicar = asignacion.modificadoTrasPublicar

    val onSuccess: (dynamic) -> Unit = {
        println("Asignacion guardada con éxito en Firestore: $id")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error al guardar asignacion en Firestore"
        println("Error guardando asignacion en Firestore ($id): $msg")
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                var data = {
                    id: String(id || ''),
                    fechaIso: String(fechaIso || ''),
                    empleadoId: String(empleadoId || ''),
                    empleadoNombre: String(empleadoNombre || ''),
                    turnoId: String(turnoId || ''),
                    tipoTurno: String(tipoTurno || ''),
                    horario: String(horario || ''),
                    sucursalId: String(sucursalId || ''),
                    programacionId: String(programacionId || ''),
                    esDescanso: Boolean(esDescanso),
                    estado: String(estado || 'BORRADOR'),
                    modificadoTrasPublicar: Boolean(modificadoTrasPublicar)
                };
                console.log("Enviando asignacion a Firestore /asignaciones/" + id, data);
                db.collection("asignaciones").doc(id).set(data)
                    .then(onSuccess)
                    .catch(function(err) {
                        console.error("Firestore fallo al guardar en /asignaciones/" + id + ":", err);
                        onError(err);
                    });
            } else {
                var errNoDb = new Error("Firebase Firestore no está disponible en window");
                console.error(errNoDb);
                onError(errNoDb);
            }
        } catch(e) {
            console.error("Excepción en guardarAsignacionFirestore:", e);
            onError(e);
        }
    """)
}

actual suspend fun obtenerAsignacionesFirestore(): List<AsignacionTurno> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<AsignacionTurno>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val docId = doc.id as? String
                    if ((data.id as? String).isNullOrBlank() && !docId.isNullOrBlank()) {
                        data.id = docId
                    }
                    val a = parsearAsignacionJs(data)
                    if (a != null) {
                        lista.add(a)
                    }
                }
            }
        }
        println("Asignaciones obtenidas de Firestore: " + lista.size)
        continuation.resume(lista)
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error obteniendo asignaciones de Firestore"
        println("Error obteniendo asignaciones de Firestore: " + msg)
        continuation.resume(emptyList())
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("asignaciones").get()
                    .then(onSuccess)
                    .catch(function(err) {
                        console.error("Firestore fallo al obtener /asignaciones:", err);
                        onError(err);
                    });
            } else {
                onSuccess({ docs: [] });
            }
        } catch(e) {
            console.error("Excepción en obtenerAsignacionesFirestore:", e);
            onError(e);
        }
    """)
}

actual fun observarAsignacionesFirestore(onUpdate: (List<AsignacionTurno>) -> Unit) {
    val callback: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<AsignacionTurno>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val docId = doc.id as? String
                    if ((data.id as? String).isNullOrBlank() && !docId.isNullOrBlank()) {
                        data.id = docId
                    }
                    val a = parsearAsignacionJs(data)
                    if (a != null) {
                        lista.add(a)
                    }
                }
            }
        }
        println("Cambio en tiempo real de Firestore /asignaciones, total: " + lista.size)
        onUpdate(lista)
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("asignaciones").onSnapshot(callback, function(err) {
                    console.error("Error en onSnapshot /asignaciones de Firestore:", err);
                });
            }
        } catch(e) {
            console.error("Error suscribiendo a Firestore /asignaciones:", e);
        }
    """)
}

actual suspend fun guardarAsignacionesBatchFirestore(asignaciones: List<AsignacionTurno>): Result<Unit> = suspendCancellableCoroutine { continuation ->
    if (asignaciones.isEmpty()) {
        continuation.resume(Result.success(Unit))
        return@suspendCancellableCoroutine
    }

    val jsItems: dynamic = js("[]")
    for (asig in asignaciones) {
        val id = if (asig.id.isNotBlank()) asig.id else "${asig.empleadoId}_${asig.fechaIso}"
        val obj: dynamic = js("({})")
        obj.id = id
        obj.fechaIso = asig.fechaIso
        obj.empleadoId = asig.empleadoId
        obj.empleadoNombre = asig.empleadoNombre
        obj.turnoId = asig.turnoId
        obj.tipoTurno = asig.tipoTurno
        obj.horario = asig.horario
        obj.sucursalId = asig.sucursalId
        obj.programacionId = asig.programacionId
        obj.esDescanso = asig.esDescanso
        obj.estado = asig.estado
        obj.modificadoTrasPublicar = asig.modificadoTrasPublicar
        jsItems.push(obj)
    }

    val onSuccess: (dynamic) -> Unit = {
        println("Lote de asignaciones (${asignaciones.size}) guardado con éxito en Firestore")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error al guardar lote de asignaciones en Firestore"
        println("Error guardando lote en Firestore: $msg")
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                var batch = db.batch();
                for (var i = 0; i < jsItems.length; i++) {
                    var item = jsItems[i];
                    var docRef = db.collection("asignaciones").doc(item.id);
                    batch.set(docRef, item);
                }
                console.log("Ejecutando commit de batch en /asignaciones para " + jsItems.length + " registros");
                batch.commit()
                    .then(onSuccess)
                    .catch(function(err) {
                        console.error("Firestore batch commit fallo:", err);
                        onError(err);
                    });
            } else {
                var errNoDb = new Error("Firebase Firestore no está disponible en window");
                console.error(errNoDb);
                onError(errNoDb);
            }
        } catch(e) {
            console.error("Excepción en guardarAsignacionesBatchFirestore:", e);
            onError(e);
        }
    """)
}

private fun parsearRegistroAsistenciaJs(d: dynamic): RegistroAsistencia? {
    if (d == null || d == js("undefined")) return null
    return try {
        val id = (d.id as? String) ?: ""
        val empId = (d.empleadoId as? String) ?: ""
        val empNombre = (d.empleado as? String) ?: (d.empleadoNombre as? String) ?: ""
        val sucursal = (d.sucursal as? String) ?: "CENTRO"
        val fechaIso = (d.fechaIso as? String) ?: ""
        val tipoTurno = (d.tipoTurno as? String) ?: ""
        val horarioProgramado = (d.horarioProgramado as? String) ?: ""
        val horaEntrada = (d.horaEntrada as? String) ?: "-"
        val horaSalida = (d.horaSalida as? String) ?: "-"
        val inicioDescanso = (d.inicioDescanso as? String) ?: "-"
        val finDescanso = (d.finDescanso as? String) ?: "-"
        val estado = (d.estado as? String) ?: "PENDIENTE"
        val estadoAsisStr = (d.estadoAsistencia as? String) ?: "SIN_INICIAR"
        val estadoAsisEnum = try { EstadoAsistencia.valueOf(estadoAsisStr) } catch (_: Throwable) { EstadoAsistencia.SIN_INICIAR }
        val tiempoEfectivo = when (val t = d.tiempoEfectivoMinutos) {
            is Number -> t.toInt()
            is String -> t.toIntOrNull() ?: 0
            else -> 0
        }
        val minsTarde = when (val m = d.minutosTarde) {
            is Number -> m.toInt()
            is String -> m.toIntOrNull() ?: 0
            else -> 0
        }
        val esDescanso = (d.esDescanso as? Boolean) ?: false
        val registradoPorSupervisor = (d.registradoPorSupervisor as? Boolean) ?: false
        val motivoSupervisor = (d.motivoSupervisor as? String) ?: ""

        RegistroAsistencia(
            id = id,
            empleadoId = empId,
            empleado = empNombre,
            sucursal = sucursal,
            fechaIso = fechaIso,
            tipoTurno = tipoTurno,
            horarioProgramado = horarioProgramado,
            horaEntrada = horaEntrada,
            horaSalida = horaSalida,
            inicioDescanso = inicioDescanso,
            finDescanso = finDescanso,
            estado = estado,
            estadoAsistencia = estadoAsisEnum,
            tiempoEfectivoMinutos = tiempoEfectivo,
            minutosTarde = minsTarde,
            esDescanso = esDescanso,
            registradoPorSupervisor = registradoPorSupervisor,
            motivoSupervisor = motivoSupervisor
        )
    } catch (_: Throwable) {
        null
    }
}

actual suspend fun guardarRegistroAsistenciaFirestore(registro: RegistroAsistencia): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val id = if (registro.id.isNotBlank()) registro.id else "${registro.empleadoId}_${registro.fechaIso}"
    val empId = registro.empleadoId
    val empNombre = registro.empleado
    val sucursal = registro.sucursal
    val fechaIso = registro.fechaIso
    val tipoTurno = registro.tipoTurno
    val horarioProgramado = registro.horarioProgramado
    val horaEntrada = registro.horaEntrada
    val horaSalida = registro.horaSalida
    val inicioDescanso = registro.inicioDescanso
    val finDescanso = registro.finDescanso
    val estado = registro.estado
    val estadoAsistencia = registro.estadoAsistencia.name
    val tiempoEfectivoMinutos = registro.tiempoEfectivoMinutos
    val minutosTarde = registro.minutosTarde
    val esDescanso = registro.esDescanso
    val registradoPorSupervisor = registro.registradoPorSupervisor
    val motivoSupervisor = registro.motivoSupervisor

    val onSuccess: (dynamic) -> Unit = {
        println("Registro de asistencia guardado con éxito en Firestore: $id")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error al guardar registro de asistencia en Firestore"
        println("Error guardando asistencia en Firestore ($id): $msg")
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                var data = {
                    id: id,
                    empleadoId: empId,
                    empleado: empNombre,
                    empleadoNombre: empNombre,
                    sucursal: sucursal,
                    fechaIso: fechaIso,
                    tipoTurno: tipoTurno,
                    horarioProgramado: horarioProgramado,
                    horaEntrada: horaEntrada,
                    horaSalida: horaSalida,
                    inicioDescanso: inicioDescanso,
                    finDescanso: finDescanso,
                    estado: estado,
                    estadoAsistencia: estadoAsistencia,
                    tiempoEfectivoMinutos: tiempoEfectivoMinutos,
                    minutosTarde: minutosTarde,
                    esDescanso: esDescanso,
                    registradoPorSupervisor: Boolean(registradoPorSupervisor),
                    motivoSupervisor: String(motivoSupervisor || ''),
                    timestampActualizacion: Date.now()
                };
                console.log("Enviando a Firestore /asistencia/" + id, data);
                db.collection("asistencia").doc(id).set(data)
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onError(new Error("Firebase Firestore no está disponible en window"));
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual suspend fun obtenerRegistrosAsistenciaFirestore(): List<RegistroAsistencia> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<RegistroAsistencia>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val r = parsearRegistroAsistenciaJs(data)
                    if (r != null) {
                        lista.add(r)
                    }
                }
            }
        }
        println("Registros de asistencia obtenidos de Firestore: " + lista.size)
        continuation.resume(lista)
    }
    val onError: (dynamic) -> Unit = { error ->
        println("Error obteniendo asistencia de Firestore: " + error)
        continuation.resume(emptyList())
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("asistencia").get()
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onSuccess({ docs: [] });
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual fun observarRegistrosAsistenciaFirestore(onUpdate: (List<RegistroAsistencia>) -> Unit) {
    val callback: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<RegistroAsistencia>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val r = parsearRegistroAsistenciaJs(data)
                    if (r != null) {
                        lista.add(r)
                    }
                }
            }
        }
        println("Cambio en tiempo real de Firestore /asistencia, total: " + lista.size)
        onUpdate(lista)
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("asistencia").onSnapshot(callback, function(err) {
                    console.error("Error en onSnapshot /asistencia de Firestore:", err);
                });
            }
        } catch(e) {
            console.error("Error suscribiendo a Firestore /asistencia:", e);
        }
    """)
}

private fun parsearNovedadJs(d: dynamic): Novedad? {
    if (d == null || d == js("undefined")) return null
    return try {
        val id = (d.id as? String) ?: ""
        val tipo = (d.tipo as? String) ?: ""
        val descripcion = (d.descripcion as? String) ?: ""
        val estadoStr = (d.estado as? String) ?: "REPORTADA"
        val estadoEnum = try { EstadoNovedad.valueOf(estadoStr) } catch (_: Throwable) { EstadoNovedad.REPORTADA }
        val empleado = (d.empleado as? String) ?: ""
        val sucursal = (d.sucursal as? String) ?: "CENTRO"
        val detalleTurno = (d.detalleTurno as? String) ?: ""
        val diferencia = (d.diferencia as? String) ?: ""
        val colorBorde = (d.colorBorde as? String) ?: "GRIS"
        val colorVorde = (d.colorVorde as? String) ?: colorBorde
        val empleadoId = (d.empleadoId as? String) ?: ""
        val asignacionTurnoId = (d.asignacionTurnoId as? String) ?: ""
        val fechaReferencia = (d.fechaReferencia as? String) ?: ""
        val fechaCreacionIso = (d.fechaCreacionIso as? String) ?: ""
        val horaReal = (d.horaReal as? String) ?: ""
        val comentarioRevision = (d.comentarioRevision as? String) ?: ""
        val revisadoPor = (d.revisadoPor as? String) ?: ""
        val fechaRevision = (d.fechaRevision as? String) ?: ""
        val reportadoPorSupervisor = (d.reportadoPorSupervisor as? Boolean) ?: false

        Novedad(
            id = id,
            tipo = tipo,
            descripcion = descripcion,
            estado = estadoEnum,
            empleado = empleado,
            sucursal = sucursal,
            detalleTurno = detalleTurno,
            diferencia = diferencia,
            colorBorde = colorBorde,
            colorVorde = colorVorde,
            empleadoId = empleadoId,
            asignacionTurnoId = asignacionTurnoId,
            fechaReferencia = fechaReferencia,
            fechaCreacionIso = fechaCreacionIso,
            horaReal = horaReal,
            comentarioRevision = comentarioRevision,
            revisadoPor = revisadoPor,
            fechaRevision = fechaRevision,
            reportadoPorSupervisor = reportadoPorSupervisor
        )
    } catch (_: Throwable) {
        null
    }
}

actual suspend fun guardarNovedadFirestore(novedad: Novedad): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val id = if (novedad.id.isNotBlank()) novedad.id else "nov_${kotlin.random.Random.nextInt(100000, 999999)}"
    val tipo = novedad.tipo
    val descripcion = novedad.descripcion
    val estado = novedad.estado.name
    val empleado = novedad.empleado
    val sucursal = novedad.sucursal
    val detalleTurno = novedad.detalleTurno
    val diferencia = novedad.diferencia
    val colorBorde = novedad.colorBorde
    val colorVorde = novedad.colorVorde
    val empleadoId = novedad.empleadoId
    val asignacionTurnoId = novedad.asignacionTurnoId
    val fechaReferencia = novedad.fechaReferencia
    val fechaCreacionIso = novedad.fechaCreacionIso
    val horaReal = novedad.horaReal
    val comentarioRevision = novedad.comentarioRevision
    val revisadoPor = novedad.revisadoPor
    val fechaRevision = novedad.fechaRevision
    val reportadoPorSupervisor = novedad.reportadoPorSupervisor

    val onSuccess: (dynamic) -> Unit = {
        println("Novedad guardada con éxito en Firestore: $id")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = (error?.message as? String) ?: (error?.toString() as? String) ?: "Error al guardar novedad en Firestore"
        println("Error guardando novedad en Firestore ($id): $msg")
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                var data = {
                    id: String(id || ''),
                    tipo: String(tipo || ''),
                    descripcion: String(descripcion || ''),
                    estado: String(estado || 'REPORTADA'),
                    empleado: String(empleado || ''),
                    sucursal: String(sucursal || 'CENTRO'),
                    detalleTurno: String(detalleTurno || ''),
                    diferencia: String(diferencia || ''),
                    colorBorde: String(colorBorde || 'GRIS'),
                    colorVorde: String(colorVorde || 'GRIS'),
                    empleadoId: String(empleadoId || ''),
                    asignacionTurnoId: String(asignacionTurnoId || ''),
                    fechaReferencia: String(fechaReferencia || ''),
                    fechaCreacionIso: String(fechaCreacionIso || ''),
                    horaReal: String(horaReal || ''),
                    comentarioRevision: String(comentarioRevision || ''),
                    revisadoPor: String(revisadoPor || ''),
                    fechaRevision: String(fechaRevision || ''),
                    reportadoPorSupervisor: Boolean(reportadoPorSupervisor),
                    timestamp: Date.now()
                };
                console.log("Enviando novedad a Firestore /novedades/" + id, data);
                db.collection("novedades").doc(id).set(data)
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onError(new Error("Firebase Firestore no está disponible en window"));
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual suspend fun obtenerNovedadesFirestore(): List<Novedad> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<Novedad>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val n = parsearNovedadJs(data)
                    if (n != null) {
                        lista.add(n)
                    }
                }
            }
        }
        println("Novedades obtenidas de Firestore: " + lista.size)
        continuation.resume(lista)
    }
    val onError: (dynamic) -> Unit = { error ->
        println("Error obteniendo novedades de Firestore: " + error)
        continuation.resume(emptyList())
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("novedades").get()
                    .then(onSuccess)
                    .catch(onError);
            } else {
                onSuccess({ docs: [] });
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual fun observarNovedadesFirestore(onUpdate: (List<Novedad>) -> Unit) {
    val callback: (dynamic) -> Unit = { snapshot ->
        val lista = mutableListOf<Novedad>()
        if (snapshot != null && snapshot.docs != null) {
            val docs = snapshot.docs as Array<dynamic>
            for (doc in docs) {
                val data = doc.data()
                if (data != null) {
                    val n = parsearNovedadJs(data)
                    if (n != null) {
                        lista.add(n)
                    }
                }
            }
        }
        println("Cambio en tiempo real de Firestore /novedades, total: " + lista.size)
        onUpdate(lista)
    }

    js("""
        try {
            var db = null;
            if (typeof firebase !== 'undefined') {
                if ((!firebase.apps || firebase.apps.length === 0) && window.firebaseConfig) {
                    try { firebase.initializeApp(window.firebaseConfig); } catch(e) {}
                }
                if (typeof firebase.firestore === 'function') {
                    db = firebase.firestore();
                }
            }

            if (db) {
                db.collection("novedades").onSnapshot(callback, function(err) {
                    console.error("Error en onSnapshot /novedades de Firestore:", err);
                });
            }
        } catch(e) {
            console.error("Error suscribiendo a Firestore /novedades:", e);
        }
    """)
}
