package cr.una.delta.frontend_kode.data.remote.serializer

import android.util.Log
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import cr.una.delta.frontend_kode.data.remote.dto.RoleDto
import cr.una.delta.frontend_kode.data.remote.dto.UserDto

/**
 * TypeAdapter for UserDto
 * Handles serialization and deserialization
 */
class UserTypeAdapter : TypeAdapter<UserDto>() {

    private val TAG = "UserTypeAdapter"

    override fun write(out: JsonWriter, value: UserDto?) {
        if (value == null) {
            out.nullValue()
            return
        }

        Log.d(TAG, "Serializing UserDto: name=${value.name}, email=${value.email}, role_id=${value.roleId}")

        out.beginObject()

        value.id?.let {
            out.name("id").value(it)
        }

        out.name("fullName").value(value.name)
        out.name("email").value(value.email)

        value.password?.let {
            out.name("password").value(it)
        }

        value.roleId?.let { roleId ->
            out.name("role_id").value(roleId)
        }

        out.endObject()
    }

    override fun read(input: JsonReader): UserDto {
        var id: String? = null
        var name = ""
        var email = ""
        var password: String? = null
        var roleId: Int? = null
        var roleString: String? = null
        var roleObject: RoleDto? = null

        input.beginObject()

        while (input.hasNext()) {
            when (input.nextName()) {
                "id" -> id = input.nextString()
                "name", "fullName", "full_name" -> name = input.nextString()
                "email" -> email = input.nextString()
                "password" -> password = input.nextString()
                "role_id" -> roleId = input.nextInt()
                "role" -> {
                    try {
                        when (input.peek()) {
                            JsonToken.STRING -> roleString = input.nextString()
                            JsonToken.BEGIN_OBJECT -> {
                                input.beginObject()
                                var roleObjId: String? = null
                                var roleName: String? = null
                                var roleType: String? = null
                                var roleCode: String? = null

                                while (input.hasNext()) {
                                    when (input.nextName()) {
                                        "id" -> roleObjId = input.nextString()
                                        "name" -> roleName = input.nextString()
                                        "type" -> roleType = input.nextString()
                                        "code" -> roleCode = input.nextString()
                                        else -> input.skipValue()
                                    }
                                }
                                input.endObject()
                                roleObject = RoleDto(roleObjId, roleName, roleType, roleCode)
                            }
                            else -> input.skipValue()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing role field", e)
                        input.skipValue()
                    }
                }
                "roleObj" -> {
                    try {
                        input.beginObject()
                        var roleObjId: String? = null
                        var roleName: String? = null
                        var roleType: String? = null
                        var roleCode: String? = null

                        while (input.hasNext()) {
                            when (input.nextName()) {
                                "id" -> roleObjId = input.nextString()
                                "name" -> roleName = input.nextString()
                                "type" -> roleType = input.nextString()
                                "code" -> roleCode = input.nextString()
                                else -> input.skipValue()
                            }
                        }
                        input.endObject()
                        roleObject = RoleDto(roleObjId, roleName, roleType, roleCode)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing roleObj", e)
                        input.skipValue()
                    }
                }
                else -> input.skipValue()
            }
        }

        input.endObject()

        return UserDto(
            id = id?.toLongOrNull(), // ✅ conversión segura de String? → Long?
            name = name,
            email = email,
            password = password,
            roleId = roleId,
            role = roleString,
            roleObject = roleObject
        )


    }
}

