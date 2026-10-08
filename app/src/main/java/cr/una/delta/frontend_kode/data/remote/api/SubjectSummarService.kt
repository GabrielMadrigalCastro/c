package cr.una.delta.frontend_kode.data.remote.api
import cr.una.delta.frontend_kode.data.remote.dto.SubjectSummaryDto
import retrofit2.Response
import retrofit2.http.*

interface SubjectSummarService {

    // Obtener todos los cursos
    @GET("courses")
    suspend fun getAllSubjects(): Response<List<SubjectSummaryDto>>

    // Obtener un curso por ID
    @GET("courses/{id}")
    suspend fun getSubjectById(@Path("id") id: Int): Response<SubjectSummaryDto>
}