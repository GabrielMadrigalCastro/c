package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.SubjectService
import cr.una.delta.frontend_kode.data.remote.dto.SubjectDto
import javax.inject.Inject

class SubjectRemoteDataSource @Inject constructor(
    private val service: SubjectService
) {
    suspend fun getSubjects(): Result<List<SubjectDto>> = safeApiCall { service.getSubjects() }
}