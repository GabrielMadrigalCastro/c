package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.CourseDto
import cr.una.delta.frontend_kode.data.remote.dto.JoinCodeDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface CourseService {
    @GET("courses")
    suspend fun getStudentCourses(): Response<List<CourseDto>>

    @Multipart
    @POST("courses/process-image")
    suspend fun uploadCourseImage(
        @Part image: MultipartBody.Part,
        @Part("studentId") studentId: RequestBody
    ): Response<List<CourseDto>>


    @GET("courses")
    suspend fun getAllCourses(): Response<List<CourseDto>>

    @GET("courses/{id}")
    suspend fun getCourseById(@Path("id") id: Long): Response<CourseDto>

    /** Código del grupo del curso (para que el profe lo comparta). */
    @GET("courses/{id}/join-code")
    suspend fun getJoinCode(@Path("id") id: Long): Response<JoinCodeDto>

    @GET("courses/student/{studentId}")
    suspend fun getCoursesByStudent(@Path("studentId") studentId: Long): Response<List<CourseDto>>

    @POST("courses")
    suspend fun createCourse(@Body input: CourseDto): Response<CourseDto>

    @PUT("courses/{id}")
    suspend fun updateCourse(@Path("id") id: Long, @Body input: CourseDto): Response<CourseDto>

    @DELETE("courses/{id}")
    suspend fun deleteCourse(@Path("id") id: Long): Response<Unit>
}