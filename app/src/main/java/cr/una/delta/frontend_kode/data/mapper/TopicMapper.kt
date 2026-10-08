package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.TopicDto
import cr.una.delta.frontend_kode.domain.model.Topic
import javax.inject.Inject

class TopicMapper @Inject constructor() {
    fun toDomain(dto: TopicDto) = Topic(dto.id, dto.subjectId, dto.name)
    fun toDomainList(list: List<TopicDto>) = list.map(::toDomain)
}
