package com.itm.gestordeturnos

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
            val idToken = result.credential?.idToken as? String
            if (email != null) {
                continuation.resume(GoogleUserData(email = email, displayName = displayName, idToken = idToken))
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

actual suspend fun actualizarContrasenaPlataforma(claveActual: String, nuevaClave: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = {
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val code = error?.code as? String ?: ""
        val msg = when {
            code == "auth/wrong-password" || code == "auth/invalid-credential" ->
                "La contraseña actual es incorrecta."
            code == "auth/weak-password" ->
                "La nueva contraseña debe tener al menos 6 caracteres (mínimo exigido por Firebase)."
            code == "auth/requires-recent-login" ->
                "Por seguridad, debes cerrar sesión e iniciarla nuevamente antes de cambiar la contraseña."
            else ->
                error?.message as? String ?: "Error al actualizar la contraseña."
        }
        continuation.resume(Result.failure(Exception(msg)))
    }

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.auth === 'function') {
                var auth = firebase.auth();
                var user = auth.currentUser;
                if (!user || !user.email) {
                    onError(new Error("No hay usuario autenticado"));
                } else {
                    var credential = firebase.auth.EmailAuthProvider.credential(user.email, claveActual);
                    user.reauthenticateWithCredential(credential)
                        .then(function() {
                            return user.updatePassword(nuevaClave);
                        })
                        .then(function() {
                            onSuccess(null);
                        })
                        .catch(onError);
                }
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

actual suspend fun guardarEmpleadoPlataforma(empleado: com.itm.gestordeturnos.domain.model.Empleado): Result<Unit>? = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = {
        continuation.resume(Result.success(Unit))
    }
    val onNotAvailable: (dynamic) -> Unit = {
        continuation.resume(null)
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = error?.message as? String ?: "Error al guardar empleado en Firestore"
        continuation.resume(Result.failure(Exception(msg)))
    }

    val id = empleado.id.ifBlank { "emp_${(10000..99999).random()}" }
    val nombre = empleado.nombre
    val apellido = empleado.apellido
    val docNum = empleado.documento.toDouble()
    val tipoDocumento = empleado.tipoDocumento
    val correo = empleado.correo
    val telefono = empleado.telefono
    val claveHash = empleado.claveHash
    val cargoStr = empleado.cargo.name
    val estadoLaboralStr = empleado.estadoLaboral.name
    val estadoStr = empleado.estado.name
    val fechaRegStr: String? = empleado.fechaRegistro?.toString()
    val empSucursal = empleado.sucursalId.ifBlank { empleado.sucursal }
    val sucursal = empleado.sucursal
    val fechaIngreso = empleado.fechaIngreso
    val iniciales = empleado.iniciales
    val sucursalAnterior = empleado.sucursalAnterior
    val fechaCambioSucursal = empleado.fechaCambioSucursal
    val intentosFallidosLogin = empleado.intentosFallidosLogin
    val bloqNum = empleado.bloqueadoHastaEpochMillis.toDouble()

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.firestore === 'function') {
                var docData = {
                    id: id,
                    nombre: nombre,
                    apellido: apellido,
                    documento: Number(docNum),
                    tipoDocumento: tipoDocumento,
                    correo: correo,
                    telefono: telefono,
                    claveHash: claveHash,
                    cargo: cargoStr,
                    estadoLaboral: estadoLaboralStr,
                    estado: estadoStr,
                    fechaRegistro: fechaRegStr,
                    sucursalId: empSucursal,
                    sucursal: sucursal,
                    fechaIngreso: fechaIngreso,
                    iniciales: iniciales,
                    sucursalAnterior: sucursalAnterior,
                    fechaCambioSucursal: fechaCambioSucursal,
                    intentosFallidosLogin: Number(intentosFallidosLogin),
                    bloqueadoHastaEpochMillis: Number(bloqNum)
                };

                firebase.firestore().collection("empleados").doc(id).set(docData)
                    .then(function() {
                        onSuccess(null);
                    })
                    .catch(onError);
            } else {
                onNotAvailable(null);
            }
        } catch(e) {
            onError(e);
        }
    """)
}

private fun construirObjetoJsAsignacion(asignacion: com.itm.gestordeturnos.domain.model.AsignacionTurno): dynamic {
    val id = asignacion.id.ifBlank { "${asignacion.empleadoId}_${asignacion.fechaIso}" }
    val fechaStr: String? = asignacion.fecha?.toString()
    val empleadoId = asignacion.empleadoId
    val turnoId = asignacion.turnoId
    val sucursalId = asignacion.sucursalId
    val programacionId = asignacion.programacionId
    val esDescanso = asignacion.esDescanso
    val fechaIso = asignacion.fechaIso
    val empleadoNombre = asignacion.empleadoNombre
    val tipoTurno = asignacion.tipoTurno
    val horario = asignacion.horario
    val estado = asignacion.estado
    val modificadoTrasPublicar = asignacion.modificadoTrasPublicar

    return js("""({
        id: id,
        fecha: fechaStr,
        empleadoId: empleadoId,
        turnoId: turnoId,
        turno: null,
        sucursalId: sucursalId,
        programacionId: programacionId,
        marcas: [],
        novedades: [],
        incumplimiento: null,
        esDescanso: Boolean(esDescanso),
        fechaIso: fechaIso,
        empleadoNombre: empleadoNombre,
        tipoTurno: tipoTurno,
        horario: horario,
        estado: estado,
        modificadoTrasPublicar: Boolean(modificadoTrasPublicar)
    })""")
}

actual suspend fun guardarAsignacionPlataforma(asignacion: com.itm.gestordeturnos.domain.model.AsignacionTurno): Result<Unit>? = suspendCancellableCoroutine { continuation ->
    val onSuccess: (dynamic) -> Unit = {
        continuation.resume(Result.success(Unit))
    }
    val onNotAvailable: (dynamic) -> Unit = {
        continuation.resume(null)
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = error?.message as? String ?: "Error al guardar asignación en Firestore"
        continuation.resume(Result.failure(Exception(msg)))
    }

    val id = asignacion.id.ifBlank { "${asignacion.empleadoId}_${asignacion.fechaIso}" }
    val docData = construirObjetoJsAsignacion(asignacion.copy(id = id))

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.firestore === 'function') {
                firebase.firestore().collection("asignaciones").doc(id).set(docData)
                    .then(function() {
                        onSuccess(null);
                    })
                    .catch(onError);
            } else {
                onNotAvailable(null);
            }
        } catch(e) {
            onError(e);
        }
    """)
}

actual suspend fun guardarAsignacionesBatchPlataforma(asignaciones: List<com.itm.gestordeturnos.domain.model.AsignacionTurno>): Result<Unit>? = suspendCancellableCoroutine { continuation ->
    if (asignaciones.isEmpty()) {
        continuation.resume(Result.success(Unit))
        return@suspendCancellableCoroutine
    }

    val onSuccess: (dynamic) -> Unit = {
        continuation.resume(Result.success(Unit))
    }
    val onNotAvailable: (dynamic) -> Unit = {
        continuation.resume(null)
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = error?.message as? String ?: "Error al guardar lote de asignaciones en Firestore"
        continuation.resume(Result.failure(Exception(msg)))
    }

    val docsArray = asignaciones.map { asig ->
        val id = asig.id.ifBlank { "${asig.empleadoId}_${asig.fechaIso}" }
        construirObjetoJsAsignacion(asig.copy(id = id))
    }.toTypedArray()

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.firestore === 'function') {
                var db = firebase.firestore();
                var batch = db.batch();
                var col = db.collection("asignaciones");
                for (var i = 0; i < docsArray.length; i++) {
                    var item = docsArray[i];
                    batch.set(col.doc(item.id), item);
                }
                batch.commit()
                    .then(function() {
                        onSuccess(null);
                    })
                    .catch(onError);
            } else {
                onNotAvailable(null);
            }
        } catch(e) {
            onError(e);
        }
    """)
}
