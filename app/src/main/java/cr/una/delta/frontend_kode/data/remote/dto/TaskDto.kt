package cr.una.delta.frontend_kode.data.remote.dto

import java.util.Date

data class TaskDto(
    val id: Long? = null,
    val userId: Long,
    val title: String? = null,
    val notes: String? = null,
    val dueDate: Date? = null,
    val createDate: Date? = null,
    val priorityId: Long? = null,
    val priorityLabel: String? = null,
    val statusId: Long? = null,
    val statusLabel: String? = null
)
