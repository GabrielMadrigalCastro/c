package cr.una.delta.frontend_kode.domain.model

import java.util.Date

data class Task(
    val id: Long = 0L,
    val userId: Long = 0L,
    val title: String = "",
    val notes: String = "",
    val createdDate: Date = Date(),
    val dueDate: Date = Date(),
    val priority: Priority = Priority(0L, "Desconocida"),
    val status: Status = Status(0L, "Pendiente")
)
