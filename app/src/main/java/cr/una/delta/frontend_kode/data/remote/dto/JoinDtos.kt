package cr.una.delta.frontend_kode.data.remote.dto

/** Cuerpo para que un estudiante se una a un curso con el código del grupo. */
data class JoinCourseDto(
    val studentId: Long,
    val code: String
)

/** Respuesta del backend con el código del grupo de un curso. */
data class JoinCodeDto(
    val joinCode: String? = null
)
