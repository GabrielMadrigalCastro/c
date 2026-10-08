package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.User
import cr.una.delta.frontend_kode.domain.model.UserRole
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedUser: User? = null,
    val registered: Boolean = false,
    val isTeacher: Boolean = false  // Nuevo campo
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Indica si ya se revisó la sesión guardada (para el auto-login al arrancar)
    private val _sessionChecked = MutableStateFlow(false)
    val sessionChecked: StateFlow<Boolean> = _sessionChecked.asStateFlow()

    init {
        restoreSession()
    }

    /** Restaura la sesión guardada en DataStore para no pedir login cada vez. */
    private fun restoreSession() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser().getOrNull()
            if (user != null) {
                // KODE es una app solo para estudiantes: nunca se muestra la
                // interfaz de profesor.
                _uiState.value = _uiState.value.copy(
                    loggedUser = user,
                    isTeacher = false
                )
            }
            _sessionChecked.value = true
        }
    }

    fun login(email: String, password: String) {
        val validation = validateLogin(email, password)
        if (validation != null) {
            _uiState.value = _uiState.value.copy(error = validation, isLoading = false)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.login(email.trim(), password)
            result
                .onSuccess { user ->
                    // App solo para estudiantes: no hay interfaz de profesor.
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        loggedUser = user,
                        error = null,
                        isTeacher = false
                    )
                }
                .onFailure { ex ->
                    // Mejorar mensaje según el tipo de error
                    val errorMessage = when {
                        ex.message?.contains("404") == true || ex.message?.contains("no encontrado") == true ->
                            "Usuario no encontrado. Por favor regístrate para crear una cuenta."
                        ex.message?.contains("401") == true || ex.message?.contains("Credenciales incorrectas") == true ->
                            "Contraseña incorrecta. Por favor verifica tus credenciales."
                        else -> ex.message ?: "Error al iniciar sesión. Por favor intenta nuevamente."
                    }

                    _uiState.value = AuthUiState(
                        isLoading = false,
                        error = errorMessage
                    )
                }
        }
    }

    fun register(name: String, email: String, password: String, confirm: String, role: UserRole) {
        val validation = validateRegister(name, email, password, confirm)
        if (validation != null) {
            _uiState.value = _uiState.value.copy(error = validation, isLoading = false)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null, registered = false)
        viewModelScope.launch {
            val result = authRepository.register(name.trim(), email.trim(), password, role)
            result
                .onSuccess {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        error = null,
                        loggedUser = null,
                        registered = true
                    )
                }
                .onFailure { ex ->
                    // Mejorar mensajes de error del registro
                    val errorMessage = when {
                        ex.message?.contains("409") == true || ex.message?.contains("ya está registrado") == true ->
                            "Este correo ya está registrado. Por favor inicia sesión."
                        ex.message?.contains("400") == true ->
                            "Datos inválidos. Verifica la información ingresada."
                        else -> ex.message ?: "Error al registrarse. Por favor intenta nuevamente."
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage,
                        registered = false
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun acknowledgeRegistrationHandled() {
        _uiState.value = _uiState.value.copy(registered = false)
    }

    fun logout() {
        _uiState.value = AuthUiState() // Resetear completamente el estado
    }

    // Validaciones
    private fun validateLogin(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) {
            return "⚠️ Todos los campos son obligatorios."
        }

        val trimmedEmail = email.trim().lowercase()

        // Validar formato básico de email
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return "⚠️ Formato de email inválido."
        }

        if (password.length < 6) {
            return "⚠️ La contraseña debe tener al menos 6 caracteres."
        }

        return null
    }

    private fun validateRegister(name: String, email: String, password: String, confirm: String): String? {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
            return "⚠️ Todos los campos son obligatorios."
        }

        if (name.trim().length < 3) {
            return "⚠️ El nombre debe tener al menos 3 caracteres."
        }

        val trimmedEmail = email.trim().lowercase()

        // Validar formato básico de email
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return "⚠️ Formato de email inválido."
        }

        if (password.length < 6) {
            return "⚠️ La contraseña debe tener al menos 6 caracteres."
        }

        if (password != confirm) {
            return "⚠️ Las contraseñas no coinciden."
        }

        // Validación adicional: contraseña debe tener al menos una letra y un número
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return "🔒 La contraseña debe contener al menos una letra y un número."
        }

        return null
    }
}