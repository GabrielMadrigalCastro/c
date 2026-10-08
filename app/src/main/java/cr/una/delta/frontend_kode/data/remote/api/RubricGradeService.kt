package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.StudentRubricGradeDto
import retrofit2.Response
import retrofit2.http.*

interface RubricGradeService {

    @GET("rubric-grades/student/{studentId}")
    suspend fun getByStudent(@Path("studentId") studentId: Long): Response<List<StudentRubricGradeDto>>

    @PUT("rubric-grades")
    suspend fun upsert(@Body body: StudentRubricGradeDto): Response<StudentRubricGradeDto>

    @DELETE("rubric-grades/student/{studentId}/rubric/{rubricId}")
    suspend fun delete(
        @Path("studentId") studentId: Long,
        @Path("rubricId") rubricId: Long
    ): Response<Unit>
}
