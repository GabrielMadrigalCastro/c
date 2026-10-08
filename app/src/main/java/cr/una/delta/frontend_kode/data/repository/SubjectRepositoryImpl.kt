package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.SubjectMapper
import cr.una.delta.frontend_kode.data.remote.SubjectRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Subject
import cr.una.delta.frontend_kode.domain.repository.SubjectRepository
import javax.inject.Inject

class SubjectRepositoryImpl @Inject constructor(
    private val ds: SubjectRemoteDataSource,
    private val mapper: SubjectMapper
) : SubjectRepository {
    override suspend fun getSubjects(): Result<List<Subject>> =
        ds.getSubjects().map { mapper.toDomainList(it) }
}
