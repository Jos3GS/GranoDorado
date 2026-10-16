// Plantilla de configuración de Firebase para Web
// Copia este archivo como 'firebase-config.js' en la misma carpeta y reemplaza con tus credenciales de Firebase Console.
window.firebaseConfig = {
  apiKey: "TU_API_KEY_AQUI",
  authDomain: "TU_PROYECTO.firebaseapp.com",
  projectId: "TU_PROYECTO_ID",
  storageBucket: "TU_PROYECTO.firebasestorage.app",
  messagingSenderId: "TU_SENDER_ID",
  appId: "TU_APP_ID",
  measurementId: "TU_MEASUREMENT_ID"
};

// Soporte retroactivo para SDK Compat si se incluye
if (typeof firebase !== 'undefined' && typeof firebase.initializeApp === 'function') {
  firebase.initializeApp(window.firebaseConfig);
}
