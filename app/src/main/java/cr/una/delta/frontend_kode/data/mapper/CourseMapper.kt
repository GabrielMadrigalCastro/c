package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.CourseDto
import cr.una.delta.frontend_kode.domain.model.Course
import javax.inject.Inject

class CourseMapper @Inject constructor() {
    fun toDomain(dto: CourseDto) = Course(
        courseId = dto.courseId,
        professorId = dto.professorId,
        courseName = dto.courseName,
        courseCode = dto.courseCode,
        courseColor = dto.courseColor,
        independentStudyHours = dto.independentStudyHours,
        createdAt = dto.createdAt
    )

    fun toDomainList(list: List<CourseDto>) = list.map(::toDomain)

    fun toDto(domain: Course) = CourseDto(
        courseId = domain.courseId,
        professorId = domain.professorId,
        courseName = domain.courseName,
        courseCode = domain.courseCode,
        courseColor = domain.courseColor,
        independentStudyHours = domain.independentStudyHours,
        createdAt = domain.createdAt
    )
}