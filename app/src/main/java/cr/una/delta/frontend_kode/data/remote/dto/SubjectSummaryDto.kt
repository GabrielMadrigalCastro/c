package cr.una.delta.frontend_kode.data.remote.dto

data class SubjectSummaryDto(
    val id: Long,
    val name: String,
    val professor: String,
    val credits: Int?,
    val color: String?,
    val schedule: List<ClassSessionDto> = emptyList()
)
