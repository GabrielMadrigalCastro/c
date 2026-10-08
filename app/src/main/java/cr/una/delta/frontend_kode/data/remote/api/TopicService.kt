package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.TopicDto
import retrofit2.Response
import retrofit2.http.GET

interface TopicService {
    @GET("topics")
    suspend fun getTopics(): Response<List<TopicDto>>
}
