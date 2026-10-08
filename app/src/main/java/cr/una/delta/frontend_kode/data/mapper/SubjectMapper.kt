package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.SubjectDto
import cr.una.delta.frontend_kode.domain.model.Subject
import javax.inject.Inject

class SubjectMapper @Inject constructor() {
    fun toDomain(dto: SubjectDto) = Subject(dto.id, dto.name, dto.unread)
    fun toDomainList(list: List<SubjectDto>) = list.map(::toDomain)
}
