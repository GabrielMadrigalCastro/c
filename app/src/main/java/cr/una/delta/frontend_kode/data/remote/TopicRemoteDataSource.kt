package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.TopicService
import cr.una.delta.frontend_kode.data.remote.dto.TopicDto
import javax.inject.Inject

class TopicRemoteDataSource @Inject constructor(
    private val service: TopicService
) {
    suspend fun getTopics(subjectId: Long): Result<List<TopicDto>> =
        safeApiCall { service.getTopics() }
            .map { list -> list.filter { it.subjectId == subjectId } }
}
