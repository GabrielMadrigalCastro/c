// data/remote/api/CourseRubricService.kt
package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CourseRubricService {
    @GET("course-rubrics/course/{courseId}")
    suspend fun getCourseRubrics(@Path("courseId") courseId: Long): Response<List<CourseRubricDto>>

    @POST("course-rubrics")
    suspend fun createRubric(@Body input: CourseRubricDto): Response<CourseRubricDto>

    @PUT("course-rubrics/{id}")
    suspend fun updateRubric(@Path("id") id: Long, @Body input: CourseRubricDto): Response<CourseRubricDto>

    @DELETE("course-rubrics/{id}")
    suspend fun deleteRubric(@Path("id") id: Long): Response<Unit>
}
