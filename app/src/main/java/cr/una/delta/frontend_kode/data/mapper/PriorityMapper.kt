package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.PriorityDto
import cr.una.delta.frontend_kode.domain.model.Priority
import javax.inject.Inject

class PriorityMapper @Inject constructor() {
    fun toDomain(dto: PriorityDto) = Priority(dto.id, dto.label)
    fun toDto(priority: Priority) = PriorityDto(priority.id, priority.label)
}
