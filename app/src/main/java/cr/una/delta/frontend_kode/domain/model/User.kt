package cr.una.delta.frontend_kode.domain.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: UserRole
)
