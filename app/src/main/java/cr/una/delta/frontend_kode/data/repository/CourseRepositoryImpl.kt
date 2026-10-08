package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.CourseMapper
import cr.una.delta.frontend_kode.data.remote.CourseRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.domain.repository.CourseRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val ds: CourseRemoteDataSource,
    private val mapper: CourseMapper
) : CourseRepository {
    override suspend fun getStudentCourses(studentId: Long): Result<List<Course>> =
        ds.getStudentCourses(studentId).map { mapper.toDomainList(it) }

    override suspend fun uploadCourseImage(image: MultipartBody.Part, studentId: RequestBody): Result<List<Course>> =
        ds.uploadCourseImage(image, studentId).map { mapper.toDomainList(it) }


        override suspend fun getAllCourses(): Result<List<Course>> =
            ds.getAllCourses().map { mapper.toDomainList(it) }

        override suspend fun getCourseById(id: Long): Result<Course> =
            ds.getCourseById(id).map { mapper.toDomain(it) }

        override suspend fun getCoursesByStudent(studentId: Long): Result<List<Course>> =
            ds.getCoursesByStudent(studentId).map { mapper.toDomainList(it) }

    override suspend fun createCourse(course: Course): Result<Course> =
        ds.createCourse(mapper.toDto(course)).map { mapper.toDomain(it) }

    override suspend fun updateCourse(id: Long, course: Course): Result<Course> =
        ds.updateCourse(id, mapper.toDto(course)).map { mapper.toDomain(it) }

    override suspend fun deleteCourse(id: Long): Result<Unit> =
            ds.deleteCourse(id)


}