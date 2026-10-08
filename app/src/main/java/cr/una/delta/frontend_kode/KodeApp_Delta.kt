package cr.una.delta.frontend_kode

import android.app.Application
import cr.una.delta.frontend_kode.presentation.ui.screens.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Kode app.
 *
 * Esta clase está anotada con @HiltAndroidApp para habilitar
 * la inyección de dependencias a nivel de toda la aplicación.
 */
@HiltAndroidApp
class KodeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Crea el canal de notificaciones al arrancar (obligatorio en Android 8+)
        NotificationHelper(this)
    }
}
