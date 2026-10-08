// data/remote/CourseRubricRemoteDataSource.kt
package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.CourseRubricService
import cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto
import javax.inject.Inject

class CourseRubricRemoteDataSource @Inject constructor(
    private val service: CourseRubricService
) {
    suspend fun getCourseRubrics(courseId: Long): Result<List<CourseRubricDto>> =
        safeApiCall { service.getCourseRubrics(courseId) }
}
