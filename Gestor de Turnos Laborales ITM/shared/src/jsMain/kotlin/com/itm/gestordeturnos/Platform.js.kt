package com.itm.gestordeturnos

import com.itm.gestordeturnos.domain.model.Cargo
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoEmpleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
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
        val sucursal = (d.sucursal as? String) ?: "CENTRO"
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
    val onSuccess: (dynamic) -> Unit = {
        js("console.log('Documento guardado con éxito en Firestore:', empleado.id);")
        continuation.resume(Result.success(Unit))
    }
    val onError: (dynamic) -> Unit = { error ->
        val msg = error?.message as? String ?: "Error al guardar empleado en Firestore"
        js("console.error('Error guardando en Firestore:', error);")
        continuation.resume(Result.failure(Exception(msg)))
    }

    val id = empleado.id
    val nombre = empleado.nombre
    val apellido = empleado.apellido
    val docDouble = empleado.documento.toDouble()
    val tipoDocumento = empleado.tipoDocumento
    val correo = empleado.correo
    val telefono = empleado.telefono
    val cargo = empleado.cargo.name
    val sucursal = empleado.sucursal
    val fechaIngreso = empleado.fechaIngreso
    val estado = empleado.estado.name
    val estadoLaboral = empleado.estadoLaboral.name
    val iniciales = empleado.iniciales

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.firestore === 'function') {
                var db = firebase.firestore();
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
                onSuccess(null);
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
        js("console.log('Empleados obtenidos de Firestore:', lista.size);")
        continuation.resume(lista)
    }
    val onError: (dynamic) -> Unit = { error ->
        js("console.error('Error obteniendo empleados de Firestore:', error);")
        continuation.resume(emptyList())
    }

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.firestore === 'function') {
                var db = firebase.firestore();
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
        js("console.log('Cambio en tiempo real de Firestore /empleados, total:', lista.size);")
        onUpdate(lista)
    }

    js("""
        try {
            if (typeof firebase !== 'undefined' && typeof firebase.firestore === 'function') {
                var db = firebase.firestore();
                db.collection("empleados").onSnapshot(callback, function(err) {
                    console.error("Error en onSnapshot de Firestore:", err);
                });
            }
        } catch(e) {
            console.error("Error suscribiendo a Firestore:", e);
        }
    """)
}
