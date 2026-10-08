package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.SubjectService
import cr.una.delta.frontend_kode.data.remote.api.SubjectSummarService
import cr.una.delta.frontend_kode.data.remote.dto.SubjectSummaryDto
import javax.inject.Inject

class SubjectSummaryRemoteDataSource @Inject constructor(
    private val service: SubjectSummarService
)
{
    suspend fun getSubjectSummaries(): Result<List<SubjectSummaryDto>> = safeApiCall { service.getAllSubjects() }


}