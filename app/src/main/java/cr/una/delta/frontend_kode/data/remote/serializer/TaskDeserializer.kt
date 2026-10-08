package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.TaskDto
import cr.una.delta.frontend_kode.data.remote.dto.PriorityDto
import cr.una.delta.frontend_kode.data.remote.dto.StatusDto
import java.lang.reflect.Type
import java.time.Instant
import java.util.Date
class TaskDeserializer : JsonDeserializer<TaskDto> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TaskDto {
        val obj = json.asJsonObject

        val id = obj["id"]?.let {
            if (it.isJsonPrimitive && it.asJsonPrimitive.isString) it.asString.toLongOrNull() ?: 0L
            else it.asLong
        } ?: 0L

        val userId = obj["userId"]?.asLong ?: 0L
        val title = obj["title"]?.asString ?: "Untitled"
        val notes = obj["notes"]?.asString ?: ""

        // Fechas: parsear "yyyy-MM-dd" o ISO
        val createDate = obj["createDate"]?.asString?.let {
            try { Date.from(Instant.parse(it)) } catch (_: Exception) { Date() }
        }
        val dueDate = obj["dueDate"]?.asString?.let {
            try { Date.from(Instant.parse(it)) } catch (_: Exception) { Date() }
        }

        // Priority y Status
        val priorityId = obj["priorityId"]?.asLong
        val priorityLabel = obj["priorityLabel"]?.asString
        val statusId = obj["statusId"]?.asLong
        val statusLabel = obj["statusLabel"]?.asString

        return TaskDto(
            id = id,
            userId = userId,
            title = title,
            notes = notes,
            createDate = createDate,
            dueDate = dueDate,
            priorityId = priorityId,
            priorityLabel = priorityLabel,
            statusId = statusId,
            statusLabel = statusLabel
        )
    }
}
