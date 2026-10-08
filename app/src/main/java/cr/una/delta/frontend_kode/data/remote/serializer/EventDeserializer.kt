package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.EventDto
import java.lang.reflect.Type
import java.util.*

class EventDeserializer : JsonDeserializer<EventDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): EventDto {
        val obj = json.asJsonObject

        val id = try {
            obj.get("id").asLong
        } catch (_: Exception) {
            obj.get("id").asString.toLong()
        }

        val title = obj.get("title")?.asString ?: ""

        val dateTime = obj.get("dateTime")?.let {
            context.deserialize<Date>(it, Date::class.java)
        } ?: Date()

        val location = obj.get("location")?.asString ?: ""

        return EventDto(id, title, dateTime, location)
    }
}
