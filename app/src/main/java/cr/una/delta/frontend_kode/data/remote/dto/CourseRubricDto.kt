package cr.una.delta.frontend_kode.data.remote.dto

data class CourseRubricDto(
    val rubricId: Long,
    val courseId: Long,
    val rubricName: String,
    val weightPercentage: Double,
    val dueDate: String? = null   // yyyy-MM-dd
)