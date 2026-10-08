package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.data.remote.api.ApunteService
import cr.una.delta.frontend_kode.data.remote.dto.ApunteDto
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ApuntesState {
    data object Loading : ApuntesState()
    data class Success(val apuntes: List<ApunteDto>) : ApuntesState()
    data class Error(val message: String) : ApuntesState()
}

@HiltViewModel
class ApuntesViewModel @Inject constructor(
    private val service: ApunteService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ApuntesState>(ApuntesState.Loading)
    val state: StateFlow<ApuntesState> = _state

    private var studentId: Long? = null
    private var courseId: Long = 0L

    fun load(courseId: Long) {
        this.courseId = courseId
        viewModelScope.launch {
            _state.value = ApuntesState.Loading
            val sid = resolveStudentId()
            if (sid == null) {
                _state.value = ApuntesState.Error("Debes iniciar sesión")
                return@launch
            }
            // courseId <= 0 => apuntes personales (sin curso): se filtran de todos.
            val list = if (courseId > 0L) {
                runCatching { service.byCourse(sid, courseId).body().orEmpty() }.getOrDefault(emptyList())
            } else {
                runCatching { service.byStudent(sid).body().orEmpty() }.getOrDefault(emptyList())
                    .filter { it.courseId == null }
            }
            _state.value = ApuntesState.Success(list)
        }
    }

    /** Crea (id null) o edita un apunte, luego recarga. */
    fun save(id: Long?, title: String, content: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val sid = resolveStudentId() ?: return@launch
            val dto = ApunteDto(
                id = id ?: 0L,
                studentId = sid,
                courseId = if (courseId > 0L) courseId else null,   // 0 = apunte personal
                title = title.trim(),
                content = content.trim()
            )
            runCatching {
                if (id == null) service.create(dto) else service.update(id, dto)
            }
            load(courseId)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching { service.delete(id) }
            load(courseId)
        }
    }

    /** Fija / desfija un apunte (mantiene el resto de sus campos). */
    fun togglePin(note: ApunteDto) {
        val id = note.id ?: return
        viewModelScope.launch {
            runCatching { service.update(id, note.copy(pinned = !note.pinned)) }
            load(courseId)
        }
    }

    private suspend fun resolveStudentId(): Long? {
        studentId?.let { return it }
        val sid = authRepository.getCurrentUser().getOrNull()?.id?.toString()?.toLongOrNull()
        studentId = sid
        return sid
    }
}
