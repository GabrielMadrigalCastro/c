package cr.una.delta.frontend_kode.data.remote.dto

/** Nota de un estudiante en una rúbrica (para la calculadora del curso). */
data class StudentRubricGradeDto(
    val id: Long? = null,
    val studentId: Long,
    val rubricId: Long,
    val grade: Double? = null
)
