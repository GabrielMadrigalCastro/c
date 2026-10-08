package cr.una.delta.frontend_kode.data.mapper

import android.util.Log
import cr.una.delta.frontend_kode.data.remote.dto.UserDto
import cr.una.delta.frontend_kode.domain.model.User
import cr.una.delta.frontend_kode.domain.model.UserRole

/**
 * Convierte UserDto a User (domain model)
 * Prioriza role_id numérico, luego maneja role como string o objeto
 */
fun UserDto.toDomain(): User {
    Log.d("UserMapper", "Converting UserDto to User")
    Log.d("UserMapper", "role_id: $roleId, role string: $role")

    // El backend manda el rol como objeto { id, name } bajo "role"; ese id (2/3)
    // es la fuente más confiable. Si no viene, caemos al code/nombre del rol.
    val effectiveRoleId = roleId ?: roleObject?.id?.toIntOrNull()

    val userRole = if (effectiveRoleId != null) {
        when (effectiveRoleId) {
            1 -> UserRole.TEACHER   // Admin -> se trata como profesor
            2 -> UserRole.TEACHER
            3 -> UserRole.STUDENT
            else -> {
                Log.w("UserMapper", "role_id desconocido: $effectiveRoleId, por defecto STUDENT")
                UserRole.STUDENT
            }
        }
    } else {
        // Fallback por texto. getRoleCode() puede devolver la 1ra letra ("T"/"P"/"S"/"E")
        // o el nombre completo ("TEACHER"/"PROFESOR"/...); cubrimos ambos.
        val roleCode = getRoleCode().uppercase()
        Log.d("UserMapper", "Sin role id, usando code/nombre: $roleCode")

        when {
            roleCode.startsWith("P") -> UserRole.TEACHER   // P, PROFESOR
            roleCode.startsWith("T") -> UserRole.TEACHER   // T, TEACHER
            roleCode.startsWith("A") -> UserRole.TEACHER   // ADMIN
            else -> UserRole.STUDENT                        // E, S, ESTUDIANTE, STUDENT
        }
    }

    return User(
        id = id ?: 0L, // si viene null, lo dejamos en 0L
        name = name,
        email = email,
        role = userRole
    )
}

/**
 * Convierte UserRole (domain) a role_id numérico para enviar al backend
 * 1 = Admin, 2 = Profesor, 3 = Estudiante
 */
fun UserRole.toDto(): Int = when (this) {
    UserRole.STUDENT -> 3
    UserRole.TEACHER -> 2
}
