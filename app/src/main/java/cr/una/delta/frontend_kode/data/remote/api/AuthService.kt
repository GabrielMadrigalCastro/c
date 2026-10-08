package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit service interface for authentication endpoints
 * Base URL: https://kode-v01-8579bff591bf.herokuapp.com/v1/
 */
interface AuthService {
    /**
     * Register a new user
     * POST /users/signup
     */
    @POST("users/signup")
    suspend fun register(@Body user: UserDto): Response<UserDto>

    /**
     * Login with email and password
     * POST /users/login
     */
    @POST("users/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<UserDto>

    /**
     * Alternative: Get user by email (para verificar si existe)
     * GET /users?email=X
     */
    @GET("users")
    suspend fun getUserByEmail(@Query("email") email: String): Response<List<UserDto>>

    /**
     * Get all users (for testing/debugging)
     * GET /users
     */
    @GET("users")
    suspend fun getAllUsers(): Response<List<UserDto>>

    @GET("users/{id}")
    suspend fun getUserById(@retrofit2.http.Path("id") id: Long): Response<UserDto>

}