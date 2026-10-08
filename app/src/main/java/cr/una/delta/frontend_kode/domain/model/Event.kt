package cr.una.delta.frontend_kode.domain.model
import java.util.Date

data class Event(
    val id: Long,
    val title: String,
    val dateTime: Date,
    val location: String
)
