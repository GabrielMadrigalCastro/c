package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.NoteDto
import cr.una.delta.frontend_kode.domain.model.Note
import java.util.*
import javax.inject.Inject

class NoteMapper @Inject constructor() {

    fun mapToDomain(dto: NoteDto, subjectName: String = ""): Note = Note(
        id = dto.id.toString(),
        title = dto.title,
        content = dto.content,
        subjectId = dto.subjectId.toString(),
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
        subject = subjectName,
        timeAgo = getTimeAgo(dto.updatedAt)
    )

    fun mapToDto(domain: Note): NoteDto = NoteDto(
        id = domain.id.toInt(),
        title = domain.title,
        content = domain.content,
        subjectId = domain.subjectId.toInt(),
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )
    fun toDomainList(dtos: List<NoteDto>, subjectName: String = ""): List<Note> {
        return dtos.map { mapToDomain(it, subjectName) }
    }
    // Función interna simple para calcular "time ago"
    private fun getTimeAgo(date: Date): String {
        val diff = System.currentTimeMillis() - date.time
        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours h ago"
            else -> "$days d ago"
        }
    }
}
