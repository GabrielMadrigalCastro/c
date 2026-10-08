package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.ReminderMapper
import cr.una.delta.frontend_kode.data.remote.ReminderRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Reminder
import cr.una.delta.frontend_kode.domain.repository.ReminderRepository
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val ds: ReminderRemoteDataSource,
    private val mapper: ReminderMapper
) : ReminderRepository {
    override suspend fun getReminders(): Result<List<Reminder>> =
        ds.getReminders().map { mapper.toDomainList(it) }
}
