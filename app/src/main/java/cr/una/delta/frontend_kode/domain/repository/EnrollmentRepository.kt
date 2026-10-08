package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Enrollment

interface EnrollmentRepository {
    suspend fun getAll(): Result<List<Enrollment>>
    suspend fun create(enrollment: Enrollment): Result<Enrollment>
    suspend fun delete(id: Long): Result<Unit>

    /** El estudiante se une a un curso con el código del grupo. */
    suspend fun joinByCode(studentId: Long, code: String): Result<Enrollment>

        suspend fun createEnrollment(enrollment: Enrollment): Result<Enrollment>


}
