package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.AssignmentDto
import java.lang.reflect.Type

class AssignmentDeserializer : JsonDeserializer<AssignmentDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AssignmentDto {
        val obj = json.asJsonObject

        return AssignmentDto(
            assignmentId = obj.get("assignmentId")?.asLong ?: obj.get("assignment_id")?.asLong ?: 0L,
            courseId = obj.get("courseId")?.asLong ?: obj.get("course_id")?.asLong ?: 0L,
            rubricId = obj.get("rubricId")?.asLong ?: obj.get("rubric_id")?.asLong,
            title = obj.get("title")?.asString ?: "",
            description = obj.get("description")?.asString,
            type = obj.get("type")?.asString,
            dueDate = obj.get("dueDate")?.asString ?: obj.get("due_date")?.asString,
            createdBy = obj.get("createdBy")?.asLong ?: obj.get("created_by")?.asLong ?: 0L,
            createdAt = obj.get("createdAt")?.asString ?: obj.get("created_at")?.asString
        )
    }
}