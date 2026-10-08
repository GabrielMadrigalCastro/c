package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.AssignmentDto
import cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface EvaluationService {
    @GET("courses/{courseId}/rubrics")
    suspend fun getCourseRubrics(@Path("courseId") courseId: Long): Response<List<CourseRubricDto>>

    @GET("courses/{courseId}/assignments")
    suspend fun getCourseAssignments(@Path("courseId") courseId: Long): Response<List<AssignmentDto>>

    @Multipart
    @POST("evaluations/process-image")
    suspend fun uploadEvaluationImage(
        @Part image: MultipartBody.Part,
        @Part("courseId") courseId: RequestBody
    ): Response<List<AssignmentDto>>
}