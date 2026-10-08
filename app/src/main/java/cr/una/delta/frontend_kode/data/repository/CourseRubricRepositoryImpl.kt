package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.CourseRubricMapper
import cr.una.delta.frontend_kode.data.remote.CourseRubricRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.CourseRubric
import cr.una.delta.frontend_kode.domain.repository.CourseRubricRepository
import javax.inject.Inject

class CourseRubricRepositoryImpl @Inject constructor(
    private val ds: CourseRubricRemoteDataSource,
    private val mapper: CourseRubricMapper
) : CourseRubricRepository {
    override suspend fun getCourseRubrics(courseId: Long): Result<List<CourseRubric>> =
        ds.getCourseRubrics(courseId).map { mapper.toDomainList(it) }
}
