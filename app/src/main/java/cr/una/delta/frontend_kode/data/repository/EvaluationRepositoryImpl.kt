// data/repository/EvaluationRepositoryImpl.kt
package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.AssignmentMapper
import cr.una.delta.frontend_kode.data.mapper.CourseRubricMapper
import cr.una.delta.frontend_kode.data.remote.EvaluationRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Assignment
import cr.una.delta.frontend_kode.domain.model.CourseRubric
import cr.una.delta.frontend_kode.domain.repository.EvaluationRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class EvaluationRepositoryImpl @Inject constructor(
    private val ds: EvaluationRemoteDataSource,
    private val rubricMapper: CourseRubricMapper,
    private val assignmentMapper: AssignmentMapper
) : EvaluationRepository {
    override suspend fun getCourseRubrics(courseId: Long): Result<List<CourseRubric>> =
        ds.getCourseRubrics(courseId).map { rubricMapper.toDomainList(it) }

    override suspend fun getCourseAssignments(courseId: Long): Result<List<Assignment>> =
        ds.getCourseAssignments(courseId).map { assignmentMapper.toDomainList(it) }

    override suspend fun uploadEvaluationImage(
        image: MultipartBody.Part,
        courseId: RequestBody
    ): Result<List<Assignment>> =
        ds.uploadEvaluationImage(image, courseId).map { assignmentMapper.toDomainList(it) }
}