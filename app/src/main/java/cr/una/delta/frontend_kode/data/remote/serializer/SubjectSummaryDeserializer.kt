package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.SubjectSummaryDto
import androidx.compose.ui.graphics.Color
import cr.una.delta.frontend_kode.data.remote.dto.ClassSessionDto
import java.lang.reflect.Type

/**
 * Custom JSON deserializer for [SubjectSummaryDto].
 */
class SubjectSummaryDeserializer : JsonDeserializer<SubjectSummaryDto> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SubjectSummaryDto {
        val obj = json.asJsonObject

        val id = try { obj["id"].asLong } catch (_: Exception) { -1L }
        val name = obj["name"]?.asString ?: "Unknown"
        val professor = obj["professor"]?.asString ?: "Por asignar"
        val credits = try { obj["credits"]?.asInt } catch (_: Exception) { null }
        val color = obj["color"]?.asString ?: "#808080"
        val schedule = if (obj.has("schedule") && obj["schedule"].isJsonArray) {
            obj["schedule"].asJsonArray.map { el -> context.deserialize(el, ClassSessionDto::class.java) }
        } else emptyList<ClassSessionDto>()

        return SubjectSummaryDto(
            id = id,
            name = name,
            professor = professor,
            credits = credits,
            color = color,
            schedule = schedule
        )
    }
}
