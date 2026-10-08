package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.SubjectDto
import java.lang.reflect.Type

class SubjectDeserializer : JsonDeserializer<SubjectDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SubjectDto {
        val obj = json.asJsonObject

        val id = obj.get("id").asLong
        val name = obj.get("name")?.asString ?: ""
        val unread = obj.get("unread")?.asInt ?: 0

        return SubjectDto(id, name, unread)
    }
}
