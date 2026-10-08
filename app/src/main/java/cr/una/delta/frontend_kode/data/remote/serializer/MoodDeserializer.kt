package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.MoodDto
import java.lang.reflect.Type

class MoodDeserializer : JsonDeserializer<MoodDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): MoodDto {
        val obj = json.asJsonObject

        // score puede venir como Int o String; intentamos primero Int
        val score = try {
            obj.get("score").asInt
        } catch (_: Exception) {
            obj.get("score").asString.toIntOrNull() ?: 0
        }

        val message = obj.get("message")?.asString ?: ""

        return MoodDto(score, message)
    }
}
