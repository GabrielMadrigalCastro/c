package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Task

interface TaskRepository {
    suspend fun getAll(): Result<List<Task>>
    suspend fun getById(id: Long): Result<Task>
    suspend fun create(task: Task): Result<Task>
    suspend fun update(task: Task): Result<Task>
    suspend fun delete(id: Long): Result<Unit>
    suspend fun markDone(id: Long): Result<Task>
    suspend fun deletePermanent(id: Long): Result<Unit>
}
