package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.MoodService
import cr.una.delta.frontend_kode.data.remote.dto.MoodDto
import javax.inject.Inject

class MoodRemoteDataSource @Inject constructor(
    private val service: MoodService
) {
    suspend fun getMoods(): Result<List<MoodDto>> =
        safeApiCall { service.getMoods() }
}

