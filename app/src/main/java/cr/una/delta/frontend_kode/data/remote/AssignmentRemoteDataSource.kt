// data/remote/AssignmentRemoteDataSource.kt
package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.AssignmentService
import cr.una.delta.frontend_kode.data.remote.dto.AssignmentDto
import javax.inject.Inject

class AssignmentRemoteDataSource @Inject constructor(
    private val service: AssignmentService
) {
    suspend fun getAssignmentsByCourse(courseId: Long): Result<List<AssignmentDto>> =
        safeApiCall { service.getAllAssignments() }
            .map { list -> list.filter { it.courseId == courseId } }
}