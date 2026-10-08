package cr.una.delta.frontend_kode.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cr.una.delta.frontend_kode.presentation.ui.screens.NotificationHelper

/**
 * Worker que dispara una notificación local cuando llega la hora programada.
 * Recibe el título y el mensaje por inputData.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Recordatorio"
        val message = inputData.getString(KEY_MESSAGE) ?: "Tienes una tarea pendiente"
        NotificationHelper(applicationContext).showNotification(title, message)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
    }
}
