package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.EventDto
import retrofit2.Response
import retrofit2.http.GET

interface EventService {
    @GET("events")
    suspend fun getNextEvent(): Response<List<EventDto>>
}
