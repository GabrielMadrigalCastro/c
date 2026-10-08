package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.ClassSessionDto
import java.lang.reflect.Type

class ClassSessionDeserializer : JsonDeserializer<ClassSessionDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ClassSessionDto {
        val obj = json.asJsonObject

        val id = try {
            obj.get("id").asLong
        } catch (_: Exception) {
            obj.get("id").asString.toLong()
        }

        val courseId = try {
            obj.get("courseId").asLong
        } catch (_: Exception) {
            obj.get("courseId").asString.toLong()
        }

        val professorId = try {
            obj.get("professorId").asLong
        } catch (_: Exception) {
            obj.get("professorId").asString.toLong()
        }

        val classDate = obj.get("classDate")?.asString ?: ""
        val startTime = obj.get("startTime")?.asString ?: ""
        val endTime = obj.get("endTime")?.asString ?: ""
        val location = obj.get("location")?.asString ?: ""
        val modality = obj.get("modality")?.asString ?: ""

        return ClassSessionDto(
            id = id,
            courseId = courseId,
            professorId = professorId,
            classDate = classDate,
            startTime = startTime,
            endTime = endTime,
            location = location,
            modality = modality
        )
    }
}
