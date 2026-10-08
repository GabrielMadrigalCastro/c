package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.MoodDto
import retrofit2.Response
import retrofit2.http.GET

interface MoodService {
    // GET para obtener la lista de moods (score + message)
    @GET("moods")
    suspend fun getMoods(): Response<List<MoodDto>>
}
