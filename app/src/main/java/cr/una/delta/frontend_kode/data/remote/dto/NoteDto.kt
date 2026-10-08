package cr.una.delta.frontend_kode.data.remote.dto

import java.util.Date

data class NoteDto(
    val id: Int,
    val title: String,
    val content: String,
    val subjectId: Int,
    val createdAt: Date,
    val updatedAt: Date

)