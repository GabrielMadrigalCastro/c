package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.ReminderDto
import retrofit2.Response
import retrofit2.http.GET

interface ReminderService {
    @GET("reminders")
    suspend fun getReminders(): Response<List<ReminderDto>>
}
