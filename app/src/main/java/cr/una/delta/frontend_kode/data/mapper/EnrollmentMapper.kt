package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.EnrollmentDto
import cr.una.delta.frontend_kode.domain.model.Enrollment
import javax.inject.Inject

class EnrollmentMapper @Inject constructor() {
    fun toDomain(dto: EnrollmentDto) = Enrollment(
        id = dto.id,
        courseId = dto.courseId,
        studentId = dto.studentId,
        enrolledAt = dto.enrolledAt
    )

    fun toDomainList(list: List<EnrollmentDto>) = list.map(::toDomain)

    fun toDto(domain: Enrollment) = EnrollmentDto(
        id = domain.id,
        courseId = domain.courseId,
        studentId = domain.studentId,
        enrolledAt = domain.enrolledAt
    )
}
