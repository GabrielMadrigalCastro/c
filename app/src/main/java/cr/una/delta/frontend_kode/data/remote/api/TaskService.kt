package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.TaskDto
import retrofit2.Response
import retrofit2.http.*

interface TaskService {

    @GET("tasks")     suspend fun getAllTasks(): Response<List<TaskDto>>

    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Long): Response<TaskDto>

    @POST("tasks")
    suspend fun createTask(@Body task: TaskDto): Response<TaskDto>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Long, @Body task: TaskDto): Response<TaskDto>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Long): Response<Unit>

    @PUT("tasks/{id}/done")
    suspend fun markTaskAsDone(@Path("id") id: Long): Response<TaskDto>

    @DELETE("tasks/{id}/permanent")
    suspend fun deleteTaskPermanently(@Path("id") id: Long): Response<Unit>
}
