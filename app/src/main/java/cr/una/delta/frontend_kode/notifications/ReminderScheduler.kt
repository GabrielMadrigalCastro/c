package cr.una.delta.frontend_kode.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Programa recordatorios locales con WorkManager (no requiere permisos de alarma
 * exacta y sobrevive reinicios de la app).
 */
object ReminderScheduler {

    /** Agenda una notificación en un momento específico (solo si es futuro). */
    fun scheduleAt(context: Context, title: String, message: String, atMillis: Long) {
        val delay = atMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_MESSAGE, message)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    /**
     * Recordatorios inteligentes de una tarea: 24 h, 3 h y 1 h antes de la entrega
     * (los que aún estén en el futuro), más un aviso al momento de vencer.
     */
    fun scheduleTaskReminders(context: Context, taskTitle: String, dueAtMillis: Long) {
        val hour = 60L * 60L * 1000L
        scheduleAt(context, "⏰ Falta 1 día: $taskTitle",
            "Tu tarea \"$taskTitle\" vence mañana.", dueAtMillis - 24 * hour)
        scheduleAt(context, "⏰ Faltan 3 horas: $taskTitle",
            "Tu tarea \"$taskTitle\" vence en 3 horas.", dueAtMillis - 3 * hour)
        scheduleAt(context, "⏰ Falta 1 hora: $taskTitle",
            "Tu tarea \"$taskTitle\" vence en 1 hora.", dueAtMillis - hour)
        scheduleAt(context, "⏰ ¡Es hora! $taskTitle",
            "Tu tarea \"$taskTitle\" vence ahora.", dueAtMillis)
    }

    /** Compat: mantiene el nombre anterior (agenda los recordatorios inteligentes). */
    fun scheduleTaskReminder(context: Context, taskTitle: String, dueAtMillis: Long) =
        scheduleTaskReminders(context, taskTitle, dueAtMillis)
}
