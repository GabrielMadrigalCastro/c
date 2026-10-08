package cr.una.delta.frontend_kode.domain.model

import java.util.Date

class Note(
    val id: String,
    val title: String,
    val content: String,
    val subjectId: String,
    val createdAt: Date,
    val updatedAt: Date,
    val subject: String = "",
    val timeAgo: String = ""
)