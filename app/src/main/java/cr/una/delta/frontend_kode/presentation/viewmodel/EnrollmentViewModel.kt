package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.Enrollment
import cr.una.delta.frontend_kode.domain.repository.EnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EnrollmentState {
    object Initial : EnrollmentState()
    object Loading : EnrollmentState()
    data class Success(val enrollments: List<Enrollment>) : EnrollmentState()
    data class Error(val message: String) : EnrollmentState()
}

@HiltViewModel
class EnrollmentViewModel @Inject constructor(
    private val repository: EnrollmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EnrollmentState>(EnrollmentState.Initial)
    val state: StateFlow<EnrollmentState> = _state

    fun loadEnrollments() {
        viewModelScope.launch {
            _state.value = EnrollmentState.Loading
            repository.getAll().fold(
                onSuccess = { _state.value = EnrollmentState.Success(it) },
                onFailure = { _state.value = EnrollmentState.Error(it.message ?: "Error") }
            )
        }
    }

    fun createEnrollment(enrollment: Enrollment) {
        viewModelScope.launch {
            _state.value = EnrollmentState.Loading
            repository.create(enrollment).fold(
                onSuccess = { loadEnrollments() },
                onFailure = { _state.value = EnrollmentState.Error(it.message ?: "Error creando matrícula") }
            )
        }
    }

    /** Une al estudiante a un curso con el código del grupo; onDone(ok, mensaje). */
    fun joinByCode(studentId: Long, code: String, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repository.joinByCode(studentId, code.trim()).fold(
                onSuccess = { onDone(true, "¡Te uniste al curso!") },
                onFailure = { onDone(false, it.message ?: "No se pudo unir. Revisá el código.") }
            )
        }
    }
}
