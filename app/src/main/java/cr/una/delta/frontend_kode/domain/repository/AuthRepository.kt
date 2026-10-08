package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.User
import cr.una.delta.frontend_kode.domain.model.UserRole

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    /**
     * Register a new user
     *
     * @param name User's full name
     * @param email User's email
     * @param password User's password
     * @param role User's role (STUDENT or TEACHER)
     * @return Result with Unit on success, or error
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<Unit>

    /**
     * Login with email and password
     *
     * @param email User's email
     * @param password User's password
     * @return Result with User data on success, or error
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * Logout current user
     *
     * @return Result with Unit on success, or error
     */
    suspend fun logout(): Result<Unit>

    /**
     * Check if user is authenticated
     *
     * @return Result with boolean indicating authentication status
     */
    suspend fun isAuthenticated(): Result<Boolean>

    /**
     * Get current logged user
     *
     * @return Result with User or null
     */
    suspend fun getCurrentUser(): Result<User?>
    suspend fun getUserById(id: Long): Result<User>
}