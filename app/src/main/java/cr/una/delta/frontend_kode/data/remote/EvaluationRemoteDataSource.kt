// data/remote/EvaluationRemoteDataSource.kt
package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.dto.AssignmentDto
import cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class EvaluationRemoteDataSource @Inject constructor(
    private val rubricDataSource: CourseRubricRemoteDataSource,
    private val assignmentDataSource: AssignmentRemoteDataSource
) {
    suspend fun getCourseRubrics(courseId: Long): Result<List<CourseRubricDto>> =
        rubricDataSource.getCourseRubrics(courseId)

    suspend fun getCourseAssignments(courseId: Long): Result<List<AssignmentDto>> =
        assignmentDataSource.getAssignmentsByCourse(courseId)

    suspend fun uploadEvaluationImage(
        image: MultipartBody.Part,
        courseId: RequestBody
    ): Result<List<AssignmentDto>> {
        // TODO: Implementar cuando tengas el endpoint real del backend
        return Result.failure(Exception("Endpoint de imagen no implementado aún"))
    }
}