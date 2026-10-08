// data/remote/serializer/CourseRubricDeserializer.kt
package cr.una.delta.frontend_kode.data.remote.serializer

import com.google.gson.*
import cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto
import java.lang.reflect.Type

class CourseRubricDeserializer : JsonDeserializer<CourseRubricDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CourseRubricDto {
        val obj = json.asJsonObject

        return CourseRubricDto(
            rubricId = obj.get("rubricId")?.asLong ?: obj.get("rubric_id")?.asLong ?: obj.get("id")?.asLong ?: 0L,
            courseId = obj.get("courseId")?.asLong ?: obj.get("course_id")?.asLong ?: 0L,
            rubricName = obj.get("rubricName")?.asString ?: obj.get("rubric_name")?.asString ?: "",
            weightPercentage = obj.get("weightPercentage")?.asDouble
                ?: obj.get("weight_percentage")?.asDouble ?: 0.0,
            dueDate = obj.get("dueDate")?.takeIf { !it.isJsonNull }?.asString
                ?: obj.get("due_date")?.takeIf { !it.isJsonNull }?.asString
        )
    }
}