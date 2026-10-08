package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.TaskService
import cr.una.delta.frontend_kode.data.remote.dto.TaskDto
import retrofit2.Response
import javax.inject.Inject

class TaskRemoteDataSource @Inject constructor(
    private val service: TaskService
) {
    suspend fun getAll(): Result<List<TaskDto>> = safeApiCall { service.getAllTasks() }
    suspend fun getById(id: Long): Result<TaskDto> = safeApiCall { service.getTaskById(id) }
    suspend fun create(dto: TaskDto): Result<TaskDto> = safeApiCall { service.createTask(dto) }
    suspend fun update(id: Long, dto: TaskDto): Result<TaskDto> = safeApiCall { service.updateTask(id, dto) }
    suspend fun delete(id: Long): Result<Unit> = safeApiCall { service.deleteTask(id) }
    suspend fun markDone(id: Long): Result<TaskDto> = safeApiCall { service.markTaskAsDone(id) }
    suspend fun deletePermanent(id: Long): Result<Unit> = safeApiCall { service.deleteTaskPermanently(id) }

}
