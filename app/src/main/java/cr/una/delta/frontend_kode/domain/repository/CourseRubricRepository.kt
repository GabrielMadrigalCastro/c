package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.CourseRubric

interface CourseRubricRepository {
    suspend fun getCourseRubrics(courseId: Long): Result<List<CourseRubric>>
}
