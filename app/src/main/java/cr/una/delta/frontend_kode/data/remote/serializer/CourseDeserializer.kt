package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.CourseDto
import java.lang.reflect.Type

class CourseDeserializer : JsonDeserializer<CourseDto> {

    // Helpers null-safe: nunca llaman asString/asInt sobre un JsonNull.
    private fun JsonObject.str(key: String): String? =
        get(key)?.takeIf { !it.isJsonNull }?.asString

    private fun JsonObject.long(key: String): Long? =
        get(key)?.takeIf { !it.isJsonNull }?.let {
            runCatching { it.asLong }.getOrNull() ?: it.asString.toLongOrNull()
        }

    private fun JsonObject.int(key: String): Int? =
        get(key)?.takeIf { !it.isJsonNull }?.let {
            runCatching { it.asInt }.getOrNull() ?: it.asString.toIntOrNull()
        }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CourseDto {
        val obj = json.asJsonObject

        return CourseDto(
            courseId = obj.long("id") ?: obj.long("courseId") ?: 0L,
            professorId = obj.long("professorId") ?: 0L,
            courseName = obj.str("courseName") ?: "",
            courseCode = obj.str("courseCode"),
            courseColor = obj.str("courseColor"),
            independentStudyHours = obj.int("independentStudyHours"),
            createdAt = obj.str("createdAt")
        )
    }
}
