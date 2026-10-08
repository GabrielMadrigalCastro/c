package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Alert

interface AlertRepository {
    suspend fun getAlertOfDay(): Result<List<Alert>>
    suspend fun getAll(): Result<List<Alert>>
    suspend fun getById(id: Long): Result<Alert>
    suspend fun getOfDay(): Result<Alert?>
    suspend fun getByProfessorToday(professorId: Long): Result<List<Alert>>
    suspend fun create(professorId: Long, title: String, message: String, severity: String): Result<Alert>
    suspend fun delete(id: Long): Result<Unit>
}
