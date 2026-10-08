package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import cr.una.delta.frontend_kode.domain.model.*
import cr.una.delta.frontend_kode.domain.repository.*
import cr.una.delta.frontend_kode.notifications.ReminderScheduler
import cr.una.delta.frontend_kode.presentation.ui.screens.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: EnrollmentRepository, // ✅ cambiado aquí
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow<CreateTaskState>(CreateTaskState.Initial)
    val state: StateFlow<CreateTaskState> = _state

    // ✅ Curso seleccionado
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse

    // Campos del formulario
    val taskTitle = MutableStateFlow("")
    val selectedType = MutableStateFlow("")
    val selectedDate = MutableStateFlow(Date())
    val hourText = MutableStateFlow("8")
    val minuteText = MutableStateFlow("00")
    val isAM = MutableStateFlow(true)
    val selectedPriorityLabel = MutableStateFlow("Media")
    val selectedStatusLabel = MutableStateFlow("Pendiente")

    // Listas disponibles
    val availableCourses = MutableStateFlow<List<Course>>(emptyList())
    val availableTypes = listOf("Tarea", "Proyecto", "Examen", "Quiz", "Otro")
    val availableStatuses = listOf("Pendiente", "En progreso", "Completada")
    val availablePriorities = listOf("Baja", "Media", "Alta")

    init {
        loadUserCourses()
    }

    // --- Manejo de campos ---
    fun updateTaskTitle(value: String) { taskTitle.value = value }

    fun updateSelectedCourse(course: Course) {
        _selectedCourse.value = if (_selectedCourse.value?.courseId == course.courseId) {
            null // si toca el mismo, se deselecciona
        } else {
            course
        }
    }

    fun updateSelectedType(value: String) { selectedType.value = value }
    fun updateSelectedDate(value: Date) { selectedDate.value = value }
    fun updateHourText(value: String) { hourText.value = value }
    fun updateMinuteText(value: String) { minuteText.value = value }
    fun updateAMPM(value: Boolean) { isAM.value = value }
    fun updateSelectedPriority(value: String) { selectedPriorityLabel.value = value }
    fun updateSelectedStatus(value: String) { selectedStatusLabel.value = value }

    // ------------------------------------------------------------
    // 🔹 Cargar cursos matriculados según el usuario logueado
    // ------------------------------------------------------------
    fun loadUserCourses() {
        viewModelScope.launch {
            _state.value = CreateTaskState.LoadingCourses
            try {
                val user = authRepository.getCurrentUser().getOrNull()
                if (user == null) {
                    _state.value = CreateTaskState.Error("Debes iniciar sesión")
                    return@launch
                }

                val userId = user.id.toLong()

                // 1️⃣ Obtener todas las matrículas (Enrollment)
                val enrollmentResult = enrollmentRepository.getAll()
                val enrollments = enrollmentResult.getOrDefault(emptyList())
                    .filter { it.studentId == userId }

                // 2️⃣ Obtener todos los cursos disponibles
                val coursesResult = courseRepository.getAllCourses()
                val allCourses = coursesResult.getOrDefault(emptyList())

                // 3️⃣ Filtrar los cursos en los que el usuario está matriculado
                val enrolledCourses = allCourses.filter { course ->
                    enrollments.any { it.courseId == course.courseId }
                }

                availableCourses.value = enrolledCourses
                _state.value = CreateTaskState.Ready

            } catch (e: Exception) {
                _state.value = CreateTaskState.Error("Error al cargar cursos: ${e.message}")
            }
        }
    }

    // ------------------------------------------------------------
    // 🔹 Crear tarea
    // ------------------------------------------------------------
    fun createTask() {
        viewModelScope.launch {
            _state.value = CreateTaskState.Creating
            try {
                val user = authRepository.getCurrentUser().getOrNull()
                val userId = user?.id?.toString()?.toLongOrNull()

                if (userId == null || userId <= 0L) {
                    _state.value = CreateTaskState.Error("Usuario inválido o no autenticado.")
                    return@launch
                }

                println("🧩 DEBUG CreateTask → userId=$userId, title=${taskTitle.value}, course=${_selectedCourse.value?.courseName}, priority=${selectedPriorityLabel.value}, status=${selectedStatusLabel.value}")

                val task = Task(
                    userId = userId,
                    title = taskTitle.value,
                    notes = _selectedCourse.value?.courseName ?: "General",
                    dueDate = selectedDate.value,
                    createdDate = Date(),
                    priority = Priority(0L, selectedPriorityLabel.value),
                    status = Status(0L, selectedStatusLabel.value)
                )

                val result = taskRepository.create(task)
                result.onSuccess {
                    _state.value = CreateTaskState.Success("Tarea creada correctamente.")

                    // 🔔 Notificación inmediata de confirmación
                    NotificationHelper(appContext).showNotification(
                        title = "✅ Tarea creada",
                        message = "📌 ${taskTitle.value}"
                    )

                    // ⏰ Recordatorios inteligentes (24 h / 3 h / 1 h antes de la entrega)
                    val cal = Calendar.getInstance().apply { time = selectedDate.value }
                    val h12 = hourText.value.toIntOrNull() ?: 8
                    val mm = minuteText.value.toIntOrNull() ?: 0
                    var hour24 = h12 % 12
                    if (!isAM.value) hour24 += 12
                    if (isAM.value && h12 == 12) hour24 = 0
                    cal.set(Calendar.HOUR_OF_DAY, hour24)
                    cal.set(Calendar.MINUTE, mm)
                    cal.set(Calendar.SECOND, 0)
                    ReminderScheduler.scheduleTaskReminders(appContext, taskTitle.value, cal.timeInMillis)
                }.onFailure {
                    _state.value = CreateTaskState.Error("Error creando tarea: ${it.message}")
                }
            } catch (e: Exception) {
                _state.value = CreateTaskState.Error("Error: ${e.message}")
            }
        }
    }
}

// ------------------------------------------------------------
// 🔹 Estados de UI
// ------------------------------------------------------------
sealed class CreateTaskState {
    object Initial : CreateTaskState()
    object LoadingCourses : CreateTaskState()
    object Creating : CreateTaskState()
    object Ready : CreateTaskState()
    data class Success(val message: String) : CreateTaskState()
    data class Error(val message: String) : CreateTaskState()
}
