package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.AlertMapper
import cr.una.delta.frontend_kode.data.remote.AlertRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Alert
import cr.una.delta.frontend_kode.domain.repository.AlertRepository
import javax.inject.Inject

class AlertRepositoryImpl @Inject constructor(
    private val ds: AlertRemoteDataSource,
    private val mapper: AlertMapper
) : AlertRepository {
    override suspend fun getAlertOfDay(): Result<List<Alert>> =
        ds.getAlertOfDay().map { mapper.toDomainList(it) }


    override suspend fun getAll(): Result<List<Alert>> =
        ds.getAll().map { mapper.toDomainList(it) }

    override suspend fun getById(id: Long): Result<Alert> =
        ds.getById(id).map { mapper.toDomain(it) }

    override suspend fun getOfDay(): Result<Alert?> =
        ds.getOfDay().map { it?.let(mapper::toDomain) }

    override suspend fun getByProfessorToday(professorId: Long): Result<List<Alert>> =
        ds.getByProfessorToday(professorId).map { mapper.toDomainList(it) }

    override suspend fun create(
        professorId: Long,
        title: String,
        message: String,
        severity: String
    ): Result<Alert> =
        ds.create(professorId, title, message, severity).map { mapper.toDomain(it) }

    override suspend fun delete(id: Long): Result<Unit> =
        ds.delete(id)


}
