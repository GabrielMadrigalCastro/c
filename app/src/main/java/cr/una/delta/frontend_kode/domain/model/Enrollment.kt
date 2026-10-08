package cr.una.delta.frontend_kode.domain.model

data class Enrollment(
    val id: Long = 0,               // 👈 valor por defecto
    val courseId: Long,
    val studentId: Long,
    val enrolledAt: String = ""     // 👈 valor por defecto
)