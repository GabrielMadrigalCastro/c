package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.NoteDto
import java.lang.reflect.Type
import java.util.*

/**
 * Custom JSON deserializer for [NoteDto].
 */
class NoteDeserializer : JsonDeserializer<NoteDto> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): NoteDto {
        val obj = json.asJsonObject

        val id = try { obj["id"].asInt } catch (_: Exception) { 0 }
        val title = obj["title"]?.asString ?: "Untitled"
        val content = obj["content"]?.asString ?: ""
        val subjectId = try { obj["subjectId"].asInt } catch (_: Exception) { -1 }

        val createdAt = obj["createdAt"]?.let { context.deserialize<Date>(it, Date::class.java) } ?: Date()
        val updatedAt = obj["updatedAt"]?.let { context.deserialize<Date>(it, Date::class.java) } ?: Date()

        return NoteDto(id, title, content, subjectId, createdAt, updatedAt)
    }
}
