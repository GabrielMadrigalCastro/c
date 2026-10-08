package cr.una.delta.frontend_kode.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("fullName")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String? = null,

    @SerializedName("role_id")
    val roleId: Int? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("roleObj")
    val roleObject: RoleDto? = null
) {
    // Constructor para registro - usa role_id numérico
    constructor(
        id: Long? = null,
        name: String,
        email: String,
        password: String,
        roleId: Int
    ) : this(
        id = id,
        name = name,
        email = email,
        password = password,
        roleId = roleId,
        role = null,
        roleObject = null
    )

    // Propiedad para mantener compatibilidad con código existente
    val roleString: String?
        get() = role

    fun getRoleCode(): String {
        roleObject?.let { roleObj ->
            return roleObj.code
                ?: roleObj.type
                ?: roleObj.name?.first()?.toString()?.uppercase()
                ?: "E"
        }
        return roleString ?: "E"
    }
}