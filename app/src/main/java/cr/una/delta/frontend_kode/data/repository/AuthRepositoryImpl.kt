package cr.una.delta.frontend_kode.data.repository

import android.util.Log
import cr.una.delta.frontend_kode.data.local.SessionManager
import cr.una.delta.frontend_kode.data.mapper.toDomain
import cr.una.delta.frontend_kode.data.mapper.toDto
import cr.una.delta.frontend_kode.data.remote.api.AuthService
import cr.una.delta.frontend_kode.data.remote.dto.UserDto
import cr.una.delta.frontend_kode.data.remote.safeApiCall
import cr.una.delta.frontend_kode.domain.model.User
import cr.una.delta.frontend_kode.domain.model.UserRole
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton



/**
 * Implementation of AuthRepository
 * Handles authentication operations with the backend API.
 * Persists session data with DataStore via SessionManager.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val sessionManager: SessionManager,

) : AuthRepository {

    private var currentUser: User? = null
    private var isLoggedIn: Boolean = false

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    // ========== REGISTER ==========
    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<Unit> {
        return try {
            val roleId = role.toDto()
            Log.d(TAG, "Registering user: $email as $role (role_id=$roleId)")

            val userDto = UserDto(
                id = null,
                name = name,
                email = email,
                password = password,
                roleId = roleId
            )

            val result = safeApiCall { authService.register(userDto) }

            result
                .onSuccess { Log.d(TAG, "Registration successful for $email") }
                .onFailure { error -> Log.e(TAG, "Registration failed: ${error.message}") }
                .map { }
        } catch (e: Exception) {
            Log.e(TAG, "Registration exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== LOGIN ==========
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Attempting login for: $email")

            val credentials = mapOf(
                "email" to email,
                "password" to password
            )

            // Llamamos directo (sin safeApiCall) para poder leer el header con el token.
            val response = authService.login(credentials)

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.toDomain()

                // El backend devuelve el JWT en el header "Authorization: Bearer <token>"
                val token = response.headers()["Authorization"]
                    ?.removePrefix("Bearer ")
                    ?.trim()

                currentUser = user
                isLoggedIn = true

                // ✅ Guardar sesión + token en DataStore
                sessionManager.saveUser(user)
                if (!token.isNullOrBlank()) {
                    sessionManager.saveToken(token)
                    Log.d(TAG, "Token JWT guardado (${token.take(12)}...)")
                } else {
                    Log.w(TAG, "Login OK pero no llegó token en el header Authorization")
                }

                Log.d(TAG, "Login successful: ${user.email}, id=${user.id}, role=${user.role}")
                Result.success(user)
            } else {
                Log.e(TAG, "Login failed: HTTP ${response.code()}")
                currentUser = null
                isLoggedIn = false
                Result.failure(Exception("Error ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Login exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== LOGOUT ==========
    override suspend fun logout(): Result<Unit> {
        return try {
            Log.d(TAG, "Logging out user: ${currentUser?.email}")
            sessionManager.clear() // ✅ Limpia datos persistidos
            currentUser = null
            isLoggedIn = false
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Logout exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== AUTH CHECK ==========
    override suspend fun isAuthenticated(): Result<Boolean> {
        return try {
            val storedUser = sessionManager.getUser()
            val authenticated = (isLoggedIn && currentUser != null) || storedUser != null
            Result.success(authenticated)
        } catch (e: Exception) {
            Log.e(TAG, "isAuthenticated exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========== GET CURRENT USER ==========
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            if (currentUser != null) {
                Log.d(TAG, "Returning currentUser (memory): ${currentUser?.email}, id=${currentUser?.id}")
                return Result.success(currentUser)
            }

            val savedUser = sessionManager.getUser()
            currentUser = savedUser

            Log.d(TAG, "Returning currentUser (DataStore): ${savedUser?.email}, id=${savedUser?.id}")
            Result.success(savedUser)
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentUser exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    override suspend fun getUserById(id: Long): Result<User> = try {
        val response = authService.getUserById(id)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.toDomain()) // ✅ usamos la extensión
        } else {
            Result.failure(Exception("Error al obtener usuario: ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

}
