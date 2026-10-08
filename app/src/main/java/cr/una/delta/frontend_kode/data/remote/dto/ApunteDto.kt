package cr.una.delta.frontend_kode.data.remote.dto

/** Apunte del estudiante en un curso (mapea /v1/notes del backend). */
data class ApunteDto(
    val id: Long? = null,
    val studentId: Long,
    val courseId: Long? = null,   // null = apunte personal (fuera de clases)
    val title: String,
    val content: String,
    val pinned: Boolean = false,
    val color: String? = null,
    val tags: String? = null,       // etiquetas separadas por coma
    val checklist: String? = null,  // JSON: [{"t":"...","d":true}]
    val createdAt: String? = null
) {
    /** Etiquetas como lista (a partir del string separado por coma). */
    val tagList: List<String>
        get() = tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    /**
     * Contenido sin HTML, para vistas previas y búsqueda.
     * El editor guarda HTML (WYSIWYG); las notas viejas en texto plano quedan igual.
     */
    val plainText: String
        get() = content
            .replace(Regex("(?s)<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()
}
