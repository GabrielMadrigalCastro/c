package cr.una.delta.frontend_kode.data.remote.dto

data class AssignmentDto(
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