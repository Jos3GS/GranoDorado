package com.itm.gestordeturnos

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun initPlatformFirebase() {
    // En Android Firebase se inicializa automáticamente mediante Google Services ContentProvider
}
