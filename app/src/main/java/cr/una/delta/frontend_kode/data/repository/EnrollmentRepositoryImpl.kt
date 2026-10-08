package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.EnrollmentMapper
import cr.una.delta.frontend_kode.data.remote.EnrollmentRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Enrollment
import cr.una.delta.frontend_kode.domain.repository.EnrollmentRepository
import javax.inject.Inject

class EnrollmentRepositoryImpl @Inject constructor(
    private val ds: EnrollmentRemoteDataSource,
    private val mapper: EnrollmentMapper
) : EnrollmentRepository {
    override suspend fun getAll(): Result<List<Enrollment>> =
        ds.getAll().map { mapper.toDomainList(it) }

    override suspend fun create(enrollment: Enrollment): Result<Enrollment> =
        ds.create(mapper.toDto(enrollment)).map { mapper.toDomain(it) }

    override suspend fun delete(id: Long): Result<Unit> =
        ds.delete(id)

    override suspend fun joinByCode(studentId: Long, code: String): Result<Enrollment> =
        ds.joinByCode(studentId, code).map { mapper.toDomain(it) }
    override suspend fun createEnrollment(enrollment: Enrollment): Result<Enrollment> =
        ds.createEnrollment(mapper.toDto(enrollment)).map { mapper.toDomain(it) }
}
