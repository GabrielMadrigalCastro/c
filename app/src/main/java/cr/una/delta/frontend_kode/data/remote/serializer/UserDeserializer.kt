package cr.una.delta.frontend_kode.data.remote.serializer

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import cr.una.delta.frontend_kode.data.remote.dto.RoleDto
import cr.una.delta.frontend_kode.data.remote.dto.UserDto
import java.lang.reflect.Type

/**
 * Custom deserializer for UserDto
 * Maneja role tanto como string como objeto
 */
class UserDeserializer : JsonDeserializer<UserDto> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): UserDto {
        val jsonObject = json.asJsonObject

        val id = jsonObject.get("id")?.asString
        val name = jsonObject.get("name")?.asString ?: ""
        val email = jsonObject.get("email")?.asString ?: ""
        val password = jsonObject.get("password")?.asString

        // Obtener role_id si existe
        val roleId = jsonObject.get("role_id")?.asInt

        // Determinar si role es string u objeto
        val roleElement = jsonObject.get("role") ?: jsonObject.get("roleObj")
        var roleString: String? = null
        var roleObject: RoleDto? = null

        if (roleElement != null) {
            when {
                roleElement.isJsonPrimitive -> {
                    // Es un string simple
                    roleString = roleElement.asString
                    Log.d("UserDeserializer", "Role is string: $roleString")
                }
                roleElement.isJsonObject -> {
                    // Es un objeto
                    try {
                        val roleJson = roleElement.asJsonObject
                        roleObject = RoleDto(
                            id = roleJson.get("id")?.asString,
                            name = roleJson.get("name")?.asString,
                            type = roleJson.get("type")?.asString,
                            code = roleJson.get("code")?.asString
                        )
                        Log.d("UserDeserializer", "Role is object: $roleObject")
                    } catch (e: Exception) {
                        Log.e("UserDeserializer", "Error parsing role object", e)
                    }
                }
            }
        }

        Log.d("UserDeserializer", "role_id from backend: $roleId")

        return UserDto(
            id = id?.toLongOrNull(), // ✅ convierte String a Long si es numérico
            name = name,
            email = email,
            password = password,
            roleId = roleId,
            role = roleString,
            roleObject = roleObject
        )

    }
}