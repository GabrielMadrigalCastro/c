package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.ApunteDto
import retrofit2.Response
import retrofit2.http.*

interface ApunteService {

    @GET("notes/{id}")
    suspend fun getById(@Path("id") id: Long): Response<ApunteDto>

    @GET("notes/student/{studentId}")
    suspend fun byStudent(@Path("studentId") studentId: Long): Response<List<ApunteDto>>

    @GET("notes/student/{studentId}/course/{courseId}")
    suspend fun byCourse(
        @Path("studentId") studentId: Long,
        @Path("courseId") courseId: Long
    ): Response<List<ApunteDto>>

    @POST("notes")
    suspend fun create(@Body body: ApunteDto): Response<ApunteDto>

    @PUT("notes/{id}")
    suspend fun update(@Path("id") id: Long, @Body body: ApunteDto): Response<ApunteDto>

    @DELETE("notes/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}
