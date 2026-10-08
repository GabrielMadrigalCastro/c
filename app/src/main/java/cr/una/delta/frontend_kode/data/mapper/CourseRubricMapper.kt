// data/mapper/CourseRubricMapper.kt
package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto
import cr.una.delta.frontend_kode.domain.model.CourseRubric
import javax.inject.Inject

class CourseRubricMapper @Inject constructor() {
    fun toDomain(dto: CourseRubricDto) = CourseRubric(
        rubricId = dto.rubricId,
        courseId = dto.courseId,
        rubricName = dto.rubricName,
        weightPercentage = dto.weightPercentage,
        dueDate = dto.dueDate
    )

    fun toDomainList(list: List<CourseRubricDto>) = list.map(::toDomain)
}