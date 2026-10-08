package cr.una.delta.frontend_kode.data.remote.dto

data class EnrollmentDto(
    val id: Long,
    val courseId: Long,
    val studentId: Long,
    val enrolledAt: String
)
