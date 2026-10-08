package cr.una.delta.frontend_kode.domain.model

import androidx.compose.ui.graphics.Color
import java.util.Date

class SubjectSummary(
    val subjectId: String,
    val name: String,
    val notesCount: Int,
    val lastUpdated: Date,
    val colorDot: Color

)
{
    val lastUpdatedText: String
        get() {
            val diff = System.currentTimeMillis() - lastUpdated.time
            val hours = diff / (1000 * 60 * 60)
            return when {
                hours < 1 -> "Hace menos de una hora"
                hours < 24 -> "Hace $hours horas"
                else -> "Hace ${hours / 24} días"
            }
        }
}