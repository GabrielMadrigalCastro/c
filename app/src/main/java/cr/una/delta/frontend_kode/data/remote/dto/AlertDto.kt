package cr.una.delta.frontend_kode.data.remote.dto

data class AlertDto(
    val id: Long,
    val title: String,
    val message: String,
    val severity: String,
    val professorId: Long?,   // 🔹 agregado para alinear con backend
    val createdAt: String     // 🔹 fecha ISO-8601 desde el backend
)