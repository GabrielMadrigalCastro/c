package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.CourseService
import cr.una.delta.frontend_kode.data.remote.dto.CourseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
class CourseRemoteDataSource @Inject constructor(
    private val service: CourseService
) {
    suspend fun getStudentCourses(studentId: Long): Result<List<CourseDto>> =
        safeApiCall { service.getStudentCourses() }

    suspend fun uploadCourseImage(image: MultipartBody.Part, studentId: RequestBody): Result<List<CourseDto>> =
        safeApiCall { service.uploadCourseImage(image, studentId) }


    suspend fun getAllCourses(): Result<List<CourseDto>> =
        safeApiCall { service.getAllCourses() }

    suspend fun getCourseById(id: Long): Result<CourseDto> =
        safeApiCall { service.getCourseById(id) }

    suspend fun getCoursesByStudent(studentId: Long): Result<List<CourseDto>> =
        safeApiCall { service.getCoursesByStudent(studentId) }

    suspend fun createCourse(dto: CourseDto): Result<CourseDto> =
        safeApiCall { service.createCourse(dto) }

    suspend fun updateCourse(id: Long, dto: CourseDto): Result<CourseDto> =
        safeApiCall { service.updateCourse(id, dto) }

    suspend fun deleteCourse(id: Long): Result<Unit> =
        safeApiCall { service.deleteCourse(id) }



    }




