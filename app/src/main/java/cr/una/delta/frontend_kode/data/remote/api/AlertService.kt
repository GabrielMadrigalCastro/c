package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.AlertDto
import retrofit2.Response
import retrofit2.http.*

interface AlertService {
    @GET("alerts")
    suspend fun getAlertOfDay(): Response<List<AlertDto>>

    // 🔹 Obtener todas las alertas
    @GET("alerts")
    suspend fun getAllAlerts(): Response<List<AlertDto>>

    // 🔹 Obtener una alerta por ID
    @GET("alerts/{id}")
    suspend fun getAlertById(@Path("id") id: Long): Response<AlertDto>

    // 🔹 Obtener la alerta del día (una sola)
    @GET("alerts/today")
    suspend fun getOfDay(): Response<AlertDto?>

    // 🔹 Obtener las alertas del día de un profesor
    @GET("alerts/professor/{professorId}/today")
    suspend fun getByProfessorToday(@Path("professorId") professorId: Long): Response<List<AlertDto>>

    // 🔹 Crear una nueva alerta
    @POST("alerts")
    suspend fun createAlert(@Body body: Map<String, Any>): Response<AlertDto>

    // 🔹 Eliminar una alerta
    @DELETE("alerts/{id}")
    suspend fun deleteAlert(@Path("id") id: Long): Response<Unit>
}
