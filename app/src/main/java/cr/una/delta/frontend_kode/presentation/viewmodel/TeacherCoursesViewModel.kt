package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import cr.una.delta.frontend_kode.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TeacherCoursesState {
    data object Loading : TeacherCoursesState()
    data object Empty : TeacherCoursesState()
    data class Ready(val courses: List<Course>) : TeacherCoursesState()
    data class Error(val message: String) : TeacherCoursesState()
}

/** Cursos que imparte el profesor logueado. */
@HiltViewModel
class TeacherCoursesViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val courseRepo: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow<TeacherCoursesState>(TeacherCoursesState.Loading)
    val state: StateFlow<TeacherCoursesState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = TeacherCoursesState.Loading
            val uid = authRepo.getCurrentUser().getOrNull()?.id
            if (uid == null) {
                _state.value = TeacherCoursesState.Error("Iniciá sesión primero.")
                return@launch
            }
            val mine = courseRepo.getAllCourses().getOrDefault(emptyList())
                .filter { it.professorId == uid }
            _state.value = if (mine.isEmpty()) TeacherCoursesState.Empty
            else TeacherCoursesState.Ready(mine)
        }
    }

    /** Elimina un curso del profesor y recarga la lista. */
    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            runCatching { courseRepo.deleteCourse(courseId) }
            load()
        }
    }
}
