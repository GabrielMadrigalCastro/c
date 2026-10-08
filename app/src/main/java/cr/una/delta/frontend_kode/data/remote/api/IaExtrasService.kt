package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.ConsejoNotasRequest
import cr.una.delta.frontend_kode.data.remote.dto.ConsejoNotasResponse
import cr.una.delta.frontend_kode.data.remote.dto.ResumenApunteRequest
import cr.una.delta.frontend_kode.data.remote.dto.ResumenApunteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Features puntuales de IA (no chat): resumir un apunte y consejo de notas. */
interface IaExtrasService {

    @POST("ia/resumen-apunte")
    suspend fun resumenApunte(@Body body: ResumenApunteRequest): Response<ResumenApunteResponse>

    @POST("ia/consejo-notas")
    suspend fun consejoNotas(@Body body: ConsejoNotasRequest): Response<ConsejoNotasResponse>
}
