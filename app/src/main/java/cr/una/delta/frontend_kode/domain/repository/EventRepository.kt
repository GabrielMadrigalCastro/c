package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Event

interface EventRepository {
    suspend fun getNextEvent(): Result<List<Event>>

}
