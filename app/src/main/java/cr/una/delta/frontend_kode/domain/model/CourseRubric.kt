package cr.una.delta.frontend_kode.domain.model

data class CourseRubric(
    val rubricId: Long,
    val courseId: Long,
    val rubricName: String,
    val weightPercentage: Double,
    val dueDate: String? = null   // yyyy-MM-dd
)