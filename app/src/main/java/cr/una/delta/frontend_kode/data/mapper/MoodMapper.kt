package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.MoodDto
import cr.una.delta.frontend_kode.domain.model.MoodLevel
import javax.inject.Inject

class MoodMapper @Inject constructor() {

    fun toDomain(dto: MoodDto) = MoodLevel(
        score = dto.score,
        message = dto.message
    )

    fun toDomainList(list: List<MoodDto>) = list.map(::toDomain)
}

