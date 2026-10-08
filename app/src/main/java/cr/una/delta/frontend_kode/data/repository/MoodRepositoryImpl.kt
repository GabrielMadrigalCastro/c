package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.MoodMapper
import cr.una.delta.frontend_kode.data.remote.MoodRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.MoodLevel
import cr.una.delta.frontend_kode.domain.repository.MoodRepository
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(
    private val ds: MoodRemoteDataSource,
    private val mapper: MoodMapper
) : MoodRepository {

    override suspend fun getMoods(): Result<List<MoodLevel>> =
        ds.getMoods().map(mapper::toDomainList)

}


