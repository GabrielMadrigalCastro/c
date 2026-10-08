package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.EventDto
import cr.una.delta.frontend_kode.domain.model.Event
import javax.inject.Inject

class EventMapper @Inject constructor() {
    fun toDomain(dto: EventDto) = Event(dto.id, dto.title, dto.dateTime, dto.location)

    fun toDomainList(list: List<EventDto>) = list.map(::toDomain)

}
