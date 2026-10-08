package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.AlertService
import cr.una.delta.frontend_kode.data.remote.dto.AlertDto
import javax.inject.Inject

class AlertRemoteDataSource @Inject constructor(
    private val service: AlertService
) {
    suspend fun getAlertOfDay(): Result<List<AlertDto>> = safeApiCall { service.getAlertOfDay() }

    suspend fun getAll(): Result<List<AlertDto>> =
        safeApiCall { service.getAllAlerts() }

    suspend fun getById(id: Long): Result<AlertDto> =
        safeApiCall { service.getAlertById(id) }

    suspend fun getOfDay(): Result<AlertDto?> =
        safeApiCall { service.getOfDay() }

    suspend fun getByProfessorToday(professorId: Long): Result<List<AlertDto>> =
        safeApiCall { service.getByProfessorToday(professorId) }

    suspend fun create(professorId: Long, title: String, message: String, severity: String): Result<AlertDto> =
        safeApiCall {
            val body = mapOf(
                "professorId" to professorId,
                "title" to title,
                "message" to message,
                "severity" to severity
            )
            service.createAlert(body)
        }

    suspend fun delete(id: Long): Result<Unit> =
        safeApiCall { service.deleteAlert(id) }

}




