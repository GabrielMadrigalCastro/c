package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.data.remote.api.ApunteService
import cr.una.delta.frontend_kode.data.remote.dto.ApunteDto
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import cr.una.delta.frontend_kode.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ApuntesHomeState {
    data object Loading : ApuntesHomeState()
    data class Ready(
        val apuntes: List<ApunteDto>,
        val courses: List<Course>
    ) : ApuntesHomeState()
    data class Error(val message: String) : ApuntesHomeState()
}

@HiltViewModel
class ApuntesHomeViewModel @Inject constructor(
    private val service: ApunteService,
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ApuntesHomeState>(ApuntesHomeState.Loading)
    val state: StateFlow<ApuntesHomeState> = _state

    private var studentId: Long? = null
    private var courses: List<Course> = emptyList()

    fun load() {
        viewModelScope.launch {
            _state.value = ApuntesHomeState.Loading
            val sid = resolveStudentId()
            if (sid == null) {
                _state.value = ApuntesHomeState.Error("Debes iniciar sesión")
                return@launch
            }
            courses = courseRepository.getCoursesByStudent(sid).getOrDefault(emptyList())
            val apuntes = runCatching { service.byStudent(sid).body().orEmpty() }
                .getOrDefault(emptyList())
            _state.value = ApuntesHomeState.Ready(apuntes, courses)
        }
    }

    fun courseName(courseId: Long): String =
        courses.find { it.courseId == courseId }?.courseName ?: "Curso"

    fun save(id: Long?, courseId: Long, title: String, content: String) {
        if (title.isBlank() || courseId <= 0L) return
        viewModelScope.launch {
            val sid = resolveStudentId() ?: return@launch
            val dto = ApunteDto(
                id = id ?: 0L,
                studentId = sid,
                courseId = courseId,
                title = title.trim(),
                content = content.trim()
            )
            runCatching { if (id == null) service.create(dto) else service.update(id, dto) }
            load()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            runCatching { service.delete(id) }
            load()
        }
    }

    private suspend fun resolveStudentId(): Long? {
        studentId?.let { return it }
        val sid = authRepository.getCurrentUser().getOrNull()?.id?.toString()?.toLongOrNull()
        studentId = sid
        return sid
    }
}
