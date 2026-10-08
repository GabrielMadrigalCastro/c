package cr.una.delta.frontend_kode.data.remote.dto

data class SubjectDto(

    val id: Long,
    val name: String,
    val unread: Int = 0

)