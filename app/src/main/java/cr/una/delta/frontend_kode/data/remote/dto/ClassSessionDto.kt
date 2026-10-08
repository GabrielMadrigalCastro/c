package cr.una.delta.frontend_kode.data.remote.dto

data class ClassSessionDto(
    val id: Long,
    val courseId: Long,
    val professorId: Long,
    val classDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val modality: String
)

