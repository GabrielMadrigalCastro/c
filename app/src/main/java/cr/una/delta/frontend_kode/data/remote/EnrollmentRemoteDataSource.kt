package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.EnrollmentService
import cr.una.delta.frontend_kode.data.remote.dto.EnrollmentDto
import cr.una.delta.frontend_kode.data.remote.dto.JoinCourseDto
import javax.inject.Inject

class EnrollmentRemoteDataSource @Inject constructor(
    private val service: EnrollmentService
) {
    suspend fun getAll(): Result<List<EnrollmentDto>> =
        safeApiCall { service.getAllEnrollments() }

    suspend fun joinByCode(studentId: Long, code: String): Result<EnrollmentDto> =
        safeApiCall { service.joinByCode(JoinCourseDto(studentId, code)) }

    suspend fun create(input: EnrollmentDto): Result<EnrollmentDto> =
        safeApiCall { service.createEnrollment(input) }

    suspend fun delete(id: Long): Result<Unit> =
        safeApiCall { service.deleteEnrollment(id) }
    suspend fun createEnrollment(dto: EnrollmentDto): Result<EnrollmentDto> =
        safeApiCall { service.createEnrollment(dto) }

}
