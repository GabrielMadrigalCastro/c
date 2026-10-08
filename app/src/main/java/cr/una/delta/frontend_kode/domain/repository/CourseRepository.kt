package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Course
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface CourseRepository {
    suspend fun getStudentCourses(studentId: Long): Result<List<Course>>
    suspend fun uploadCourseImage(image: MultipartBody.Part, studentId: RequestBody): Result<List<Course>>

    suspend fun getAllCourses(): Result<List<Course>>
    suspend fun getCourseById(id: Long): Result<Course>
    suspend fun getCoursesByStudent(studentId: Long): Result<List<Course>>
    suspend fun createCourse(course: Course): Result<Course>
    suspend fun updateCourse(id: Long, course: Course): Result<Course>
    suspend fun deleteCourse(id: Long): Result<Unit>
}