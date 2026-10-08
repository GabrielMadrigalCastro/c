package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.EventMapper
import cr.una.delta.frontend_kode.data.remote.EventRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Event
import cr.una.delta.frontend_kode.domain.repository.EventRepository
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val ds: EventRemoteDataSource,
    private val mapper: EventMapper
) : EventRepository {
    override suspend fun getNextEvent(): Result<List<Event>> =
        ds.getNextEvent().map { mapper.toDomainList(it) }
}

