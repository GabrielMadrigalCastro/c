// data/remote/api/AssignmentService.kt
package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.AssignmentDto
import retrofit2.Response
import retrofit2.http.GET

interface AssignmentService {
    @GET("assignments")
    suspend fun getAllAssignments(): Response<List<AssignmentDto>>
}