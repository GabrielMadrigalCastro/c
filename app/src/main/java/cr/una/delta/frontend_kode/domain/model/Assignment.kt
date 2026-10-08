package cr.una.delta.frontend_kode.domain.model

data class Assignment(
    val assignmentId: Long,
    val courseId: Long,
    val rubricId: Long?,
    val title: String,
    val description: String?,
    val type: String?,
    val dueDate: String?,
    val createdBy: Long,
    val createdAt: String?
)