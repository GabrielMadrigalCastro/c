package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.AlertDto
import java.lang.reflect.Type

class AlertDeserializer : JsonDeserializer<AlertDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AlertDto {
        val obj = json.asJsonObject

        val id = try {
            obj.get("id").asLong
        } catch (_: Exception) {
            obj.get("id").asString.toLong()
        }

        val title = obj.get("title")?.asString ?: ""
        val message = obj.get("message")?.asString ?: ""
        val severity = obj.get("severity")?.asString ?: ""
        val professorId = obj.get("professorId")?.takeIf { !it.isJsonNull }?.asLong
        val createdAt = obj.get("createdAt")?.asString ?: ""

        return AlertDto(id, title, message, severity, professorId, createdAt)
    }
}
