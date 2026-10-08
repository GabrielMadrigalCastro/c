// domain/repository/EvaluationRepository.kt
package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Assignment
import cr.una.delta.frontend_kode.domain.model.CourseRubric
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface EvaluationRepository {
    suspend fun getCourseRubrics(courseId: Long): Result<List<CourseRubric>>
    suspend fun getCourseAssignments(courseId: Long): Result<List<Assignment>>
    suspend fun uploadEvaluationImage(image: MultipartBody.Part, courseId: RequestBody): Result<List<Assignment>>
}