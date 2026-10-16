// Plantilla de configuración de Firebase para Web
// Copia este archivo como 'firebase-config.js' y reemplaza con tus credenciales de Firebase Console.
const firebaseConfig = {
  apiKey: "TU_API_KEY_AQUI",
  authDomain: "TU_PROYECTO.firebaseapp.com",
  projectId: "TU_PROYECTO_ID",
  storageBucket: "TU_PROYECTO.firebasestorage.app",
  messagingSenderId: "TU_SENDER_ID",
  appId: "TU_APP_ID",
  measurementId: "TU_MEASUREMENT_ID"
};

if (typeof firebase !== 'undefined') {
  firebase.initializeApp(firebaseConfig);
}
