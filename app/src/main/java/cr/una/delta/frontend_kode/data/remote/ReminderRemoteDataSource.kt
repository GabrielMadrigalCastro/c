package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.ReminderService
import cr.una.delta.frontend_kode.data.remote.dto.ReminderDto
import javax.inject.Inject

class ReminderRemoteDataSource @Inject constructor(
    private val service: ReminderService
) {
    suspend fun getReminders(): Result<List<ReminderDto>> = safeApiCall { service.getReminders() }
}