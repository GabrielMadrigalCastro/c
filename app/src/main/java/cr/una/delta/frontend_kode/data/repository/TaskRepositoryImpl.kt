package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.TaskMapper
import cr.una.delta.frontend_kode.data.remote.TaskRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Task
import cr.una.delta.frontend_kode.domain.repository.TaskRepository
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val ds: TaskRemoteDataSource,
    private val mapper: TaskMapper
) : TaskRepository {

    override suspend fun getAll(): Result<List<Task>> =
        ds.getAll().map { mapper.toDomainList(it) }

    override suspend fun getById(id: Long): Result<Task> =
        ds.getById(id).map { mapper.toDomain(it) }

    override suspend fun create(task: Task): Result<Task> =
        ds.create(mapper.toDto(task))
            .map { mapper.toDomain(it) }

    override suspend fun update(task: Task): Result<Task> =
        ds.update(task.id, mapper.toDto(task))
            .map { mapper.toDomain(it) }

    override suspend fun delete(id: Long): Result<Unit> = ds.delete(id)
    override suspend fun markDone(id: Long): Result<Task> =
        ds.markDone(id).map { mapper.toDomain(it) }

    override suspend fun deletePermanent(id: Long): Result<Unit> =
        ds.deletePermanent(id)
}
