package cr.una.delta.frontend_kode.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.*
import cr.una.delta.frontend_kode.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

// =============================================================
// ESTADO DEL HOME
// =============================================================
sealed class HomeState {
    object Initial : HomeState()
    object Loading : HomeState()
    object Empty : HomeState()
    data class Success(
        val nextEvent: List<NextEvent> = emptyList(),
        val classes: List<ClassSession> = emptyList(),
        val reminders: List<Reminder> = emptyList(),
        val alert: List<Alert> = emptyList(),
        val tasks: List<Task> = emptyList()
    ) : HomeState()
    data class Error(val message: String) : HomeState()
}

// =============================================================
// VIEWMODEL PRINCIPAL (Home)
// =============================================================
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val classRepo: ClassSessionRepository,
    private val courseRepo: CourseRepository,            // ✅ agregado
    private val reminderRepo: ReminderRepository,
    private val alertRepo: AlertRepository,
    private val studyPlanRepo: StudyPlanRepository,
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()

    private val _state = MutableStateFlow<HomeState>(HomeState.Initial)
    val state = _state.asStateFlow()

    // Nombre (de pila) del usuario para el saludo del Home.
    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private var cachedStudentId: Long? = null

    // =============================================================
    // OBTENER ID DEL ESTUDIANTE
    // =============================================================
    private suspend fun getStudentId(): Long {
        cachedStudentId?.let { return it }
        val user = authRepository.getCurrentUser().getOrNull()
        val id = user?.id ?: 0L
        cachedStudentId = id
        return id
    }

    // =============================================================
    // CARGA PRINCIPAL DE DATOS Y PLAN DIARIO
    // =============================================================
    fun loadHomeData() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val studentId = getStudentId()
                if (studentId == 0L) {
                    _state.value = HomeState.Error("Debes iniciar sesión primero.")
                    return@launch
                }
                // Nombre de pila para el saludo.
                _userName.value = authRepository.getCurrentUser().getOrNull()
                    ?.name?.trim()?.substringBefore(" ").orEmpty()

                // 🔹 Obtener información base. Cada llamada va aislada en runCatching:
                //    si una falla (p. ej. un deserializador con un campo null),
                //    el Home igual carga con el resto en vez de romperse por completo.
                val classes = runCatching { classRepo.getTodayClasses().getOrDefault(emptyList()) }
                    .getOrDefault(emptyList())
                val courses = runCatching { courseRepo.getAllCourses().getOrDefault(emptyList()) }
                    .getOrDefault(emptyList())
                val reminders = runCatching { reminderRepo.getReminders().getOrDefault(emptyList()) }
                    .getOrDefault(emptyList())
                val alerts = runCatching { alertRepo.getAlertOfDay().getOrDefault(emptyList()) }
                    .getOrDefault(emptyList())
                val tasks = runCatching { taskRepository.getAll().getOrDefault(emptyList()) }
                    .getOrDefault(emptyList())

                // 🔹 Generar el plan IA del día
                val dailyPlanResult = studyPlanRepo.generateDailyPlan(
                    studentId = studentId,
                    assignmentId = 0L,
                    date = LocalDate.now()
                )

                val dailyPlan = dailyPlanResult.getOrDefault(
                    DailyPlan(LocalDate.now(), "", emptyList())
                )

                // 🔹 Crear eventos a partir de clases y plan
                val formatter = DateTimeFormatter.ofPattern("HH:mm")

                val classEvents = classes.map { cls ->
                    val course = courses.find { it.courseId == cls.courseId }
                    val colorHex = course?.courseColor ?: "#6FA8DC"
                    val courseName = course?.courseName ?: "Curso ${cls.courseId}"

                    NextEvent(
                        title = courseName,
                        startTime = cls.startTime,
                        endTime = cls.endTime,
                        type = EventType.CLASS,
                        colorHex = colorHex
                    )
                }

                val planEvents = dailyPlan.blocks.map { sp ->
                    NextEvent(
                        title = sp.description,
                        startTime = sp.startTime.format(formatter),
                        endTime = sp.endTime.format(formatter),
                        type = sp.type.toEventType(),
                        colorHex = sp.color ?: ""
                    )
                }

                val events = (classEvents + planEvents).sortedBy { it.startTime }

                if (events.isEmpty() && reminders.isEmpty() && alerts.isEmpty() && tasks.isEmpty()) {
                    _state.value = HomeState.Empty
                } else {
                    _state.value = HomeState.Success(
                        nextEvent = events,
                        classes = classes,
                        reminders = reminders,
                        alert = alerts,
                        tasks = tasks
                    )
                }

                Log.d("HomeViewModel", "✅ Plan del día generado (${events.size} eventos).")

            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error: ${e.message}", e)
                _state.value = HomeState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // =============================================================
    // MARCAR TAREA COMO COMPLETADA ✅
    // =============================================================
    fun markTaskAsDone(task: Task) {
        if (task.status.id == 3L ||
            task.status.label.uppercase() in listOf("DONE", "COMPLETED", "FINISHED")
        ) return

        viewModelScope.launch {
            try {
                val result = taskRepository.markDone(task.id)
                result.onSuccess {
                    Log.d("HomeVM", "✅ Tarea completada: ${task.title}")
                    loadHomeData()
                }.onFailure { e ->
                    _state.value = HomeState.Error("Error al marcar tarea como completada: ${e.message}")
                }
            } catch (e: Exception) {
                _state.value = HomeState.Error("Excepción al marcar tarea como completada: ${e.message}")
            }
        }
    }

    // =============================================================
    // ♻️ REPLANIFICAR AUTOMÁTICAMENTE TRAS AGREGAR / EDITAR TAREA
    // =============================================================
    fun onTaskAddedOrUpdated(task: Task) {
        viewModelScope.launch {
            try {
                _state.value = HomeState.Loading
                val studentId = getStudentId()
                if (studentId == 0L) {
                    _state.value = HomeState.Error("Sesión no válida, inicia sesión nuevamente.")
                    return@launch
                }

                val assignmentId = try {
                    val field = task::class.java.declaredFields.find { it.name == "assignmentId" }
                    field?.apply { isAccessible = true }?.get(task) as? Long ?: 0L
                } catch (_: Exception) { 0L }

                val newPlan = studyPlanRepo.generateDailyPlan(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    date = LocalDate.now()
                )

                if (newPlan.isSuccess) {
                    Log.d("HomeVM", "♻️ Día replanificado automáticamente tras nueva tarea.")
                    loadHomeData()
                } else {
                    _state.value = HomeState.Error("Error al regenerar plan diario tras agregar tarea.")
                }
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Error al actualizar plan diario.")
            }
        }
    }
}
