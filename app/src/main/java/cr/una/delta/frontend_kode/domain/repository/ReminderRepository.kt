package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Reminder

interface ReminderRepository {
    suspend fun getReminders(): Result<List<Reminder>>
}
