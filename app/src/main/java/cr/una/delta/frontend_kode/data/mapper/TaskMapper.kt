package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.TaskDto
import cr.una.delta.frontend_kode.domain.model.Task
import cr.una.delta.frontend_kode.domain.model.Priority
import cr.una.delta.frontend_kode.domain.model.Status
import jakarta.inject.Inject
import java.time.ZoneId
import java.util.*

class TaskMapper @Inject constructor() {

    // ----------- DTO → Domain -----------
    fun toDomain(dto: TaskDto): Task = Task(
        id = dto.id ?: 0L,
        userId = dto.userId,
        title = dto.title ?: "",
        notes = dto.notes ?: "",
        createdDate = dto.createDate ?: Date(),
        dueDate = dto.dueDate ?: Date(),
        priority = Priority(
            id = dto.priorityId ?: 0L,
            label = dto.priorityLabel ?: "Desconocida"
        ),
        status = Status(
            id = dto.statusId ?: 0L,
            label = dto.statusLabel ?: "Pendiente"
        )
    )

    // ----------- Domain → DTO -----------
    fun toDto(domain: Task): TaskDto = TaskDto(
        userId = domain.userId,
        title = domain.title.ifBlank { null },
        notes = domain.notes.ifBlank { null },
        dueDate = domain.dueDate,
        createDate = domain.createdDate,
        // ✅ Solo enviamos labels, no IDs
        priorityLabel = domain.priority.label.ifBlank { null },
        statusLabel = domain.status.label.ifBlank { null },
        // ✅ Evitamos enviar IDs vacíos
        priorityId = null,
        statusId = null
    )

    fun toDomainList(list: List<TaskDto>): List<Task> = list.map { toDomain(it) }
}
