package cr.una.delta.frontend_kode.domain.model
import java.time.LocalDateTime

data class Alert(

    val id: Long,
    val title: String,
    val message: String,
    val severity: String,
    val professorId: Long?,       // puede venir null si no hay profesor
    val createdAt: LocalDateTime
)
