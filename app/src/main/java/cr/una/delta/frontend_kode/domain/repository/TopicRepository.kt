package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Topic

interface TopicRepository {
    suspend fun getTopics(subjectId: Long): Result<List<Topic>>
}