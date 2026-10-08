package cr.una.delta.frontend_kode.data.remote.dto

data class CourseDto(
    val courseId: Long,
    val professorId: Long,
    val courseName: String,
    val courseCode: String?,
    val courseColor: String?,
    val independentStudyHours: Int?,
    val createdAt: String?
)