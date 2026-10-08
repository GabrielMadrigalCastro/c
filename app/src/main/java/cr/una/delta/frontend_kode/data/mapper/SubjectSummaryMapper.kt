package cr.una.delta.frontend_kode.data.mapper

import androidx.compose.ui.graphics.Color
import cr.una.delta.frontend_kode.data.remote.dto.SubjectSummaryDto
import cr.una.delta.frontend_kode.domain.model.SubjectSummary
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SubjectSummaryMapper @Inject constructor() {

    fun mapToDomain(dto: SubjectSummaryDto): SubjectSummary = SubjectSummary(
        subjectId = dto.id.toString(),
        name = dto.name,
        notesCount = 0, // El DTO no tiene este campo
        lastUpdated = Date(),
        colorDot = parseColor(dto.color)
    )

    fun toDomainList(dtos: List<SubjectSummaryDto>): List<SubjectSummary> =
        dtos.map { mapToDomain(it) }

    private fun parseColor(hex: String?): Color {
        return try {
            if (hex.isNullOrBlank()) Color(0xFF6200EA) // color por defecto
            else Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF6200EA)
        }
    }
}
