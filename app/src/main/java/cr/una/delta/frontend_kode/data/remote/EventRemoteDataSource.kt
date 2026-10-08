package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.EventService
import cr.una.delta.frontend_kode.data.remote.dto.EventDto
import javax.inject.Inject

class EventRemoteDataSource @Inject constructor(
    private val service: EventService
) {
    suspend fun getNextEvent(): Result<List<EventDto>> = safeApiCall { service.getNextEvent() }
}