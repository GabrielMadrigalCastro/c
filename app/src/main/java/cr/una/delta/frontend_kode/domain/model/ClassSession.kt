package cr.una.delta.frontend_kode.domain.model
import java.util.Date

data class ClassSession(
    val id: Long,
    val courseId: Long,
    val professorId: Long,
    val classDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val modality: String
)
