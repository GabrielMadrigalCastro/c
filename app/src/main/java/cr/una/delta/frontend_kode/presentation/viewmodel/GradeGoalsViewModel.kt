package cr.una.delta.frontend_kode.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.data.remote.api.RubricGradeService
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import cr.una.delta.frontend_kode.domain.repository.CourseRepository
import cr.una.delta.frontend_kode.domain.repository.CourseRubricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Progreso de nota de un curso frente a la meta del estudiante. */
data class CourseProgress(
    val course: Course,
    val accumulated: Double,   // puntos ya asegurados (de 100)
    val gradedWeight: Double,  // % del curso ya calificado
    val totalWeight: Double,   // % total de las rúbricas
    val goal: Int              // meta del estudiante (0..100)
) {
    val remainingWeight: Double get() = (totalWeight - gradedWeight).coerceAtLeast(0.0)
    /** Promedio necesario en lo que falta para llegar a la meta (null si no queda nada). */
    val neededAverage: Double? get() =
        if (remainingWeight > 0.0) ((goal - accumulated) / (remainingWeight / 100.0)) else null
    val achievable: Boolean get() = (neededAverage ?: 0.0) <= 100.0
    val reachedGoal: Boolean get() = accumulated >= goal
    /** Promedio actual sobre lo ya calificado. */
    val currentAverage: Double? get() =
        if (gradedWeight > 0.0) accumulated / gradedWeight * 100.0 else null
}

sealed class GradeGoalsState {
    data object Loading : GradeGoalsState()
    data object Empty : GradeGoalsState()
    data class Ready(val items: List<CourseProgress>) : GradeGoalsState()
    data class Error(val message: String) : GradeGoalsState()
}

@HiltViewModel
class GradeGoalsViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val courseRepo: CourseRepository,
    private val rubricRepo: CourseRubricRepository,
    private val gradeService: RubricGradeService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<GradeGoalsState>(GradeGoalsState.Loading)
    val state: StateFlow<GradeGoalsState> = _state

    private companion object {
        const val PREFS = "kode_goals"
        const val DEFAULT_GOAL = 70
    }

    private fun goalOf(courseId: Long): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("goal_$courseId", DEFAULT_GOAL)

    /** Guarda la meta del curso y actualiza el estado sin recargar del servidor. */
    fun setGoal(courseId: Long, goal: Int) {
        val g = goal.coerceIn(0, 100)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("goal_$courseId", g).apply()
        val cur = _state.value
        if (cur is GradeGoalsState.Ready) {
            _state.value = GradeGoalsState.Ready(
                cur.items.map { if (it.course.courseId == courseId) it.copy(goal = g) else it }
            )
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = GradeGoalsState.Loading
            try {
                val sid = authRepo.getCurrentUser().getOrNull()?.id
                if (sid == null) {
                    _state.value = GradeGoalsState.Error("Iniciá sesión primero.")
                    return@launch
                }
                val courses = courseRepo.getCoursesByStudent(sid).getOrDefault(emptyList())
                if (courses.isEmpty()) {
                    _state.value = GradeGoalsState.Empty
                    return@launch
                }
                // Todas las notas del estudiante en una sola llamada.
                val grades = runCatching { gradeService.getByStudent(sid).body() }
                    .getOrNull().orEmpty()
                    .mapNotNull { g -> g.grade?.let { g.rubricId to it } }
                    .toMap()

                val items = courses.map { c ->
                    val rubrics = rubricRepo.getCourseRubrics(c.courseId).getOrDefault(emptyList())
                    val totalWeight = rubrics.sumOf { it.weightPercentage }
                    val graded = rubrics.filter { grades[it.rubricId] != null }
                    val accumulated = graded.sumOf { (grades[it.rubricId]!! / 100.0) * it.weightPercentage }
                    val gradedWeight = graded.sumOf { it.weightPercentage }
                    CourseProgress(
                        course = c,
                        accumulated = accumulated,
                        gradedWeight = gradedWeight,
                        totalWeight = totalWeight,
                        goal = goalOf(c.courseId)
                    )
                }
                _state.value = GradeGoalsState.Ready(items)
            } catch (e: Exception) {
                _state.value = GradeGoalsState.Error(e.message ?: "No se pudo cargar el progreso")
            }
        }
    }
}
