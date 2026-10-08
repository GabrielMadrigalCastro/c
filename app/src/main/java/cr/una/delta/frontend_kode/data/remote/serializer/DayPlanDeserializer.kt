package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.*
import java.lang.reflect.Type

class DayPlanDeserializer : JsonDeserializer<DayPlanResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): DayPlanResponse {
        val obj = json.asJsonObject

        val date = obj.get("date")?.asString ?: ""
        val currentTime = obj.get("currentTime")?.asString ?: ""

        val bloques = mutableListOf<DayPlanBlockDTO>()

        obj.get("bloques")?.asJsonArray?.forEach { blockElement ->
            try {
                val blockObj = blockElement.asJsonObject
                val block = DayPlanBlockDTO(
                    status = blockObj.get("status")?.asString ?: "",
                    startTime = blockObj.get("startTime")?.asString ?: "",
                    endTime = blockObj.get("endTime")?.asString ?: "",
                    isActive = blockObj.get("isActive")?.asBoolean ?: false
                )
                bloques.add(block)
            } catch (e: Exception) {
                // Log y continuar con el siguiente bloque
                android.util.Log.e("DayPlanDeserializer", "Error parsing block", e)
            }
        }

        return DayPlanResponse(
            date = date,
            currentTime = currentTime,
            bloques = bloques
        )
    }
}