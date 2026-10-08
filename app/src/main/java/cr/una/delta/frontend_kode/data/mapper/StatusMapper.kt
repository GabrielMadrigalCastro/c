package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.StatusDto
import cr.una.delta.frontend_kode.domain.model.Status
import javax.inject.Inject

class StatusMapper @Inject constructor() {
    fun toDomain(dto: StatusDto) = Status(dto.id, dto.label)
    fun toDto(status: Status) = StatusDto(status.id, status.label)
}
