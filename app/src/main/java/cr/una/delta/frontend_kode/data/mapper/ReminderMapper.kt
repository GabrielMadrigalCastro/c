package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.ReminderDto
import cr.una.delta.frontend_kode.domain.model.Reminder
import javax.inject.Inject

class ReminderMapper @Inject constructor() {
    fun toDomain(dto: ReminderDto) = Reminder(dto.id, dto.title, dto.description)
    fun toDomainList(list: List<ReminderDto>) = list.map(::toDomain)
}
