package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.MoodLevel

interface MoodRepository {

    suspend fun getMoods(): Result<List<MoodLevel>>

}
