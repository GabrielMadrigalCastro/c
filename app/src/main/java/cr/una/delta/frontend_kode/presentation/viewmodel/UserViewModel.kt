package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.User
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _professorName = MutableStateFlow<Map<Long, String>>(emptyMap())
    val professorName: StateFlow<Map<Long, String>> = _professorName

    // 🔹 Obtiene el nombre del profesor por ID (y lo guarda en cache local)
    fun loadProfessorName(professorId: Long) {
        // Evitar recargar si ya existe
        if (_professorName.value.containsKey(professorId)) return

        viewModelScope.launch {
            repository.getUserById(professorId).fold(
                onSuccess = { user ->
                    _professorName.value = _professorName.value + (professorId to user.name)
                },
                onFailure = {
                    _professorName.value = _professorName.value + (professorId to "Desconocido")
                }
            )
        }
    }
}
