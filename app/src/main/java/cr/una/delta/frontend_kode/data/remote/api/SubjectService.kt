package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.SubjectDto
import retrofit2.Response
import retrofit2.http.GET

interface SubjectService {
    @GET("subjects")
    suspend fun getSubjects(): Response<List<SubjectDto>>
}
