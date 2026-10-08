package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.repository.CourseRepository
import cr.una.delta.frontend_kode.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Cuenta cursos y tareas reales del backend para las estadísticas del perfil.
 * Sin datos inventados: si no hay, muestra 0.
 */
@HiltViewModel
class ProfileStatsViewModel @Inject constructor(
    private val courseRepo: CourseRepository,
    private val taskRepo: TaskRepository
) : ViewModel() {

    private val _coursesCount = MutableStateFlow(0)
    val coursesCount: StateFlow<Int> = _coursesCount

    private val _tasksCount = MutableStateFlow(0)
    val tasksCount: StateFlow<Int> = _tasksCount

    fun load(studentId: Long) {
        viewModelScope.launch {
            _coursesCount.value =
                courseRepo.getCoursesByStudent(studentId).getOrDefault(emptyList()).size
            _tasksCount.value =
                taskRepo.getAll().getOrDefault(emptyList()).size
        }
    }
}
