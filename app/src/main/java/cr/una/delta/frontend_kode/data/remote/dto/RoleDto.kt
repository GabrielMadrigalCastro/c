package cr.una.delta.frontend_kode.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Role object from backend
 */
data class RoleDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("code")
    val code: String? = null
)