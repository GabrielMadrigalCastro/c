package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.TopicDto
import java.lang.reflect.Type

class TopicDeserializer : JsonDeserializer<TopicDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TopicDto {
        val obj = json.asJsonObject

        val id = obj.get("id").asLong
        val subjectId = obj.get("subjectId").asLong
        val name = obj.get("name")?.asString ?: ""

        return TopicDto(id, subjectId, name)
    }
}
