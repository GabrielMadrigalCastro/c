package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.ClassSessionDto
import retrofit2.Response
import retrofit2.http.*

interface ClassSessionService {

    @GET("classes")
    suspend fun getAllClassSessions(): Response<List<ClassSessionDto>>

    @GET("classes/{id}")
    suspend fun getClassSessionById(@Path("id") id: Long): Response<ClassSessionDto>

    @GET("classes/course/{courseId}")
    suspend fun getClassSessionsByCourse(@Path("courseId") courseId: Long): Response<List<ClassSessionDto>>

    @GET("classes/date/{classDate}")
    suspend fun getClassSessionsByDate(@Path("classDate") date: String): Response<List<ClassSessionDto>>

    @GET("classes/professor/{professorId}")
    suspend fun getClassesByProfessor(@Path("professorId") professorId: Long): Response<List<ClassSessionDto>>

    @POST("classes")
    suspend fun createClassSession(@Body input: ClassSessionDto): Response<ClassSessionDto>

    @PUT("classes/{id}")
    suspend fun updateClassSession(@Path("id") id: Long, @Body input: ClassSessionDto): Response<ClassSessionDto>

    @DELETE("classes/{id}")
    suspend fun deleteClassSession(@Path("id") id: Long): Response<Unit>
    @PATCH("classes/{id}/modality")
    suspend fun updateClassModality(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<ClassSessionDto>

}
