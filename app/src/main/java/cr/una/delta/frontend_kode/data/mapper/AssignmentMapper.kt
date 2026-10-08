package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.AssignmentDto
import cr.una.delta.frontend_kode.domain.model.Assignment
import javax.inject.Inject

class AssignmentMapper @Inject constructor() {
    fun toDomain(dto: AssignmentDto) = Assignment(
        assignmentId = dto.assignmentId,
        courseId = dto.courseId,
        rubricId = dto.rubricId,
        title = dto.title,
        description = dto.description,
        type = dto.type,
        dueDate = dto.dueDate,
        createdBy = dto.createdBy,
        createdAt = dto.createdAt
    )

    fun toDomainList(list: List<AssignmentDto>) = list.map(::toDomain)
}