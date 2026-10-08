package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.ClassSessionDto
import cr.una.delta.frontend_kode.domain.model.ClassSession
import javax.inject.Inject

class ClassSessionMapper @Inject constructor() {

    fun toDomain(dto: ClassSessionDto) = ClassSession(
        id = dto.id,
        courseId = dto.courseId,
        professorId = dto.professorId,
        classDate = dto.classDate,
        startTime = dto.startTime,
        endTime = dto.endTime,
        location = dto.location,
        modality = dto.modality
    )

    fun toDomainList(list: List<ClassSessionDto>) = list.map(::toDomain)

    fun toDto(domain: ClassSession) = ClassSessionDto(
        id = domain.id,
        courseId = domain.courseId,
        professorId = domain.professorId,
        classDate = domain.classDate,
        startTime = domain.startTime,
        endTime = domain.endTime,
        location = domain.location,
        modality = domain.modality
    )
}
