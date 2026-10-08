package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.SubjectSummaryMapper
import cr.una.delta.frontend_kode.data.remote.SubjectSummaryRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.SubjectSummary
import cr.una.delta.frontend_kode.domain.repository.SubjectSummaryRepository
import javax.inject.Inject

class SubjectSummaryRepositoryImpl @Inject constructor(
    private val ds: SubjectSummaryRemoteDataSource,
    private val mapper: SubjectSummaryMapper
) : SubjectSummaryRepository {

    override suspend fun getSubjectSummaries(): Result<List<SubjectSummary>> =
        ds.getSubjectSummaries().map { mapper.toDomainList(it) }


}