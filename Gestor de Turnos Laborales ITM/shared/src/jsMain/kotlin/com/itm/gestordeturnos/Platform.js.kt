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
