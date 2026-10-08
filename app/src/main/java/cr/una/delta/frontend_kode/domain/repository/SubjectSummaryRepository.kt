package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.SubjectSummary

interface  SubjectSummaryRepository {
    suspend fun getSubjectSummaries(): Result<List<SubjectSummary>>
}