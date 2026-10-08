package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.domain.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CourseState {
    object Loading : CourseState()
    object Empty : CourseState()
    data class Success(val courses: List<Course>) : CourseState()
    data class Error(val message: String) : CourseState()
}

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CourseState>(CourseState.Loading)
    val state: StateFlow<CourseState> = _state

    // 🔥 Cursos que YA tiene el estudiante (para evitar duplicados)
    private val _studentCourses = MutableStateFlow<List<Course>>(emptyList())
    val studentCourses: StateFlow<List<Course>> = _studentCourses

    // 🔥 Cursos disponibles en el sistema
    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses

    // ============================================================
    // 📌 Cargar TODOS los cursos del sistema
    // ============================================================
    fun loadAllCourses() {
        viewModelScope.launch {
            _state.value = CourseState.Loading

            repository.getAllCourses().fold(
                onSuccess = { courses ->
                    _allCourses.value = courses

                    _state.value = if (courses.isEmpty()) CourseState.Empty
                    else CourseState.Success(courses)
                },
                onFailure = { error ->
                    _state.value =
                        CourseState.Error(error.message ?: "Error al cargar cursos")
                }
            )
        }
    }

    // ============================================================
    // 📌 Cargar cursos del estudiante
    // ============================================================
    private var lastStudentId: Long? = null

    fun loadCourses(studentId: Long, force: Boolean = false) {
        lastStudentId = studentId
        viewModelScope.launch {
            // Refresca en segundo plano: solo muestra "cargando" si aún no hay cursos,
            // así un curso recién creado aparece al volver sin parpadeo ni spinner.
            if (_state.value !is CourseState.Success) _state.value = CourseState.Loading
            repository.getCoursesByStudent(studentId).fold(
                onSuccess = { courses ->
                    _studentCourses.value = courses
                    _state.value = if (courses.isEmpty()) CourseState.Empty
                    else CourseState.Success(courses)
                },
                onFailure = { error ->
                    _state.value =
                        CourseState.Error(error.message ?: "Error al cargar cursos")
                }
            )
        }
    }

    // ============================================================
    // 📌 Editar / eliminar curso (y recargar la lista)
    // ============================================================
    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteCourse(courseId) }
            lastStudentId?.let { loadCourses(it, force = true) }
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            runCatching { repository.updateCourse(course.courseId, course) }
            lastStudentId?.let { loadCourses(it, force = true) }
        }
    }

    // ============================================================
    // 📌 Obtener cursos del estudiante (para AddEnrollmentScreen)
    // ============================================================
    suspend fun getCoursesOfStudent(studentId: Long): Result<List<Course>> {
        return repository.getCoursesByStudent(studentId)
    }
}
