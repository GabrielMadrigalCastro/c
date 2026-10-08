package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.AlertDto
import cr.una.delta.frontend_kode.domain.model.Alert
import java.time.LocalDateTime
import javax.inject.Inject

class
AlertMapper @Inject constructor() {

    fun toDomain(dto: AlertDto) = Alert(
        id = dto.id,
        title = dto.title,
        message = dto.message,
        severity = dto.severity,
        professorId = dto.professorId,
        createdAt = try {
            LocalDateTime.parse(dto.createdAt)
        } catch (_: Exception) {
            LocalDateTime.MIN
        }
    )

    fun toDomainList(list: List<AlertDto>) = list.map(::toDomain)
}

