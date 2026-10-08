package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.TopicMapper
import cr.una.delta.frontend_kode.data.remote.TopicRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Topic
import cr.una.delta.frontend_kode.domain.repository.TopicRepository
import javax.inject.Inject

class TopicRepositoryImpl @Inject constructor(
    private val ds: TopicRemoteDataSource,
    private val mapper: TopicMapper
) : TopicRepository {
    override suspend fun getTopics(subjectId: Long): Result<List<Topic>> =
        ds.getTopics(subjectId).map { mapper.toDomainList(it) }
}
