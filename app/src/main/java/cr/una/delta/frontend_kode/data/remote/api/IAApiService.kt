package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface IAApiService {

    @POST("studyplans/generar")
    suspend fun generateStudyPlan(
        @Body request: GenerateStudyPlanRequest
    ): Response<StudyPlanResponse>

    @POST("studyplans/dia")
    suspend fun generateDailyPlan(
        @Body request: GenerateDailyPlanRequest
    ): Response<DailyPlanResponse>

    @GET("studyplans/day-plan")
    suspend fun getDailyPlan(
        @Query("studentId") studentId: Long,
        @Query("fecha") fecha: String? = null,
        @Query("hora") hora: String? = null
    ): Response<DayPlanResponse>

    @POST("studyplans/semana")
    suspend fun generateWeeklyPlan(
        @Body request: GenerateWeeklyPlanRequest
    ): Response<WeeklyPlanResponse>

    @POST("ia")
    suspend fun chatWithAI(
        @Body request: ChatRequest
    ): Response<ChatResponse>

    /** Plan del día armado por la IA (con las reglas de clases/transporte/estudio). */
    @POST("studyplans/dia-ia")
    suspend fun generarPlanDiaIA(
        @Body request: DiaIARequestDto
    ): Response<DiaIAResponseDto>
}