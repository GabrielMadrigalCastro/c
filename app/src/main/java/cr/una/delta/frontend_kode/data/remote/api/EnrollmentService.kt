package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.EnrollmentDto
import cr.una.delta.frontend_kode.data.remote.dto.JoinCourseDto
import retrofit2.Response
import retrofit2.http.*

interface EnrollmentService {
    @GET("enrollments")
    suspend fun getAllEnrollments(): Response<List<EnrollmentDto>>

    /** El estudiante se une a un curso con el código del grupo. */
    @POST("enrollments/join")
    suspend fun joinByCode(@Body input: JoinCourseDto): Response<EnrollmentDto>

    @GET("enrollments/{id}")
    suspend fun getEnrollmentById(@Path("id") id: Long): Response<EnrollmentDto>

    @POST("enrollments")
    suspend fun createEnrollment(@Body input: EnrollmentDto): Response<EnrollmentDto>

    @DELETE("enrollments/{id}")
    suspend fun deleteEnrollment(@Path("id") id: Long): Response<Unit>

}
