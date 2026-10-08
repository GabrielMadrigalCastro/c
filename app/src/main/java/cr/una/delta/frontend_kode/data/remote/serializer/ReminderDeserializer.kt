package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.ReminderDto
import java.lang.reflect.Type

class ReminderDeserializer : JsonDeserializer<ReminderDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ReminderDto {
        val obj = json.asJsonObject

        val id = try {
            obj.get("id").asLong
        } catch (_: Exception) {
            obj.get("id").asString.toLong()
        }

        val title = obj.get("title")?.asString ?: ""
        val description = obj.get("description")?.asString ?: ""

        return ReminderDto(id, title, description)
    }
}
