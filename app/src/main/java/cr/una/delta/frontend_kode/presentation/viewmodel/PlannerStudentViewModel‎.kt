package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.*
import cr.una.delta.frontend_kode.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject


@HiltViewModel
class PlannerStudentViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val classRepo: ClassSessionRepository,
    private val authRepository: AuthRepository,
    private val studyPlanRepository: StudyPlanRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate

    private val _state = MutableStateFlow<PlannerState>(PlannerState.Initial)
    val state: StateFlow<PlannerState> = _state

    private val _aiPlanState = MutableStateFlow<AIPlanState>(AIPlanState.Initial)
    val aiPlanState: StateFlow<AIPlanState> = _aiPlanState

    fun selectDate(cal: Calendar) {
        _selectedDate.value = cal.clone() as Calendar
    }

    private fun getPriorityLevel(task: Task): Int = when (task.priority.label.uppercase()) {
        "HIGH", "ALTA" -> 3
        "MEDIUM", "MEDIA" -> 2
        "LOW", "BAJA" -> 1
        else -> 0
    }

    fun loadPlannerData(force: Boolean = false) {
        viewModelScope.launch {
            // Spinner de pantalla completa SOLO si aún no hay datos. Si ya hay datos,
            // se refresca en segundo plano sin tapar el calendario (así aparece lo
            // recién creado y no "se cae de nuevo" al reentrar a la pantalla).
            if (_state.value !is PlannerState.Success) _state.value = PlannerState.Loading
            try {
                val userResult = authRepository.getCurrentUser()
                if (userResult.getOrNull() == null) {
                    _state.value = PlannerState.Error("Debes iniciar sesión")
                    return@launch
                }

                val classesResult = classRepo.getAllClasses()
                val classes = classesResult.getOrDefault(emptyList())
                val tasks = taskRepository.getAll().getOrDefault(emptyList())

                val activeTasks = tasks.filter { it.status.id != 3L }
                val sortedTasks = activeTasks.sortedByDescending { getPriorityLevel(it) }

                _state.value = if (classes.isEmpty() && sortedTasks.isEmpty()) {
                    PlannerState.Empty
                } else {
                    PlannerState.Success(classes = classes, tasks = sortedTasks)
                }
            } catch (e: Exception) {
                _state.value = PlannerState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ================== FUNCIONES DE IA ==================

    fun generateAIStudyPlan(taskId: Long, daysBeforeDue: Int = 7) {
        viewModelScope.launch {
            _aiPlanState.value = AIPlanState.Loading
            try {
                val user = authRepository.getCurrentUser().getOrNull()
                if (user == null) {
                    _aiPlanState.value = AIPlanState.Error("Debes iniciar sesión")
                    return@launch
                }

                // Convertir user.id String -> Long
                val studentIdLong = user.id.toLong()

                val result = studyPlanRepository.generateStudyPlan(
                    studentId = studentIdLong,
                    assignmentId = taskId,
                    hoursPerDay = 2,
                    daysBeforeDueDate = daysBeforeDue
                )

                result.onSuccess { plans ->
                    _aiPlanState.value = AIPlanState.Success(plans)
                    loadPlannerData(force = true)
                }.onFailure { e ->
                    _aiPlanState.value = AIPlanState.Error(e.message ?: "Error al generar plan")
                }

            } catch (e: Exception) {
                _aiPlanState.value = AIPlanState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun generateDailyAIPlan(taskId: Long, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            _aiPlanState.value = AIPlanState.Loading
            try {
                val user = authRepository.getCurrentUser().getOrNull()
                if (user == null) {
                    _aiPlanState.value = AIPlanState.Error("Debes iniciar sesión")
                    return@launch
                }

                val studentIdLong = user.id.toLong()

                val result = studyPlanRepository.generateDailyPlan(
                    studentId = studentIdLong,
                    assignmentId = taskId,
                    date = date
                )

                result.onSuccess { dailyPlan ->
                    _aiPlanState.value = AIPlanState.DailySuccess(dailyPlan)
                    loadPlannerData(force = true)
                }.onFailure { e ->
                    _aiPlanState.value = AIPlanState.Error(e.message ?: "Error al generar plan diario")
                }

            } catch (e: Exception) {
                _aiPlanState.value = AIPlanState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun generateWeeklyAIPlan(taskId: Long, days: Int = 7) {
        viewModelScope.launch {
            _aiPlanState.value = AIPlanState.Loading
            try {
                val user = authRepository.getCurrentUser().getOrNull()
                if (user == null) {
                    _aiPlanState.value = AIPlanState.Error("Debes iniciar sesión")
                    return@launch
                }

                val studentIdLong = user.id.toLong()

                val result = studyPlanRepository.generateWeeklyPlan(
                    studentId = studentIdLong,
                    assignmentId = taskId,
                    startDate = LocalDate.now(),
                    days = days
                )

                result.onSuccess { plans ->
                    _aiPlanState.value = AIPlanState.Success(plans)
                    loadPlannerData(force = true)
                }.onFailure { e ->
                    _aiPlanState.value = AIPlanState.Error(e.message ?: "Error al generar plan semanal")
                }

            } catch (e: Exception) {
                _aiPlanState.value = AIPlanState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // ================== Resto del ViewModel ==================

    fun getTasksForDay(day: Calendar): List<Task> {
        val currentState = _state.value
        if (currentState is PlannerState.Success) {
            val tasksForDay = currentState.tasks
                .filter { it.status.id != 3L }
                .filter { task ->
                    val cal = Calendar.getInstance().apply { time = task.dueDate }
                    cal.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
                }
                .sortedByDescending { getPriorityLevel(it) }

            return if (tasksForDay.size >= 3) tasksForDay
            else {
                val filler = currentState.tasks.filter { it !in tasksForDay && it.status.id != 3L }
                tasksForDay + filler.take(3 - tasksForDay.size)
            }
        }
        return emptyList()
    }

    fun markTaskAsDone(task: Task) {
        if (task.status.id == 3L || task.status.label.uppercase() in listOf("DONE", "COMPLETED", "FINISHED")) return

        viewModelScope.launch {
            try {
                val result = taskRepository.markDone(task.id)
                result.onSuccess { loadPlannerData() }
                    .onFailure { e ->
                        _state.value = PlannerState.Error("Error al marcar tarea como DONE: ${e.message}")
                    }
            } catch (e: Exception) {
                _state.value = PlannerState.Error("Excepción al marcar tarea como DONE: ${e.message}")
            }
        }
    }
    private val _dailyPlan = MutableLiveData<DailyPlan>()
    val dailyPlan: LiveData<DailyPlan> get() = _dailyPlan

    fun removeTaskFromPlan(taskId: Long) {
        val currentPlan = dailyPlan.value ?: return
        val updatedBlocks = currentPlan.blocks.filterNot { block ->
            block.type == BlockType.STUDY && block.id == taskId
        }
        _dailyPlan.value = currentPlan.copy(blocks = updatedBlocks)
    }

    // ================== GESTIÓN DE CLASES (Calendario) ==================

    /** Elimina una clase suelta del horario y recarga el planner. */
    fun deleteClass(id: Long) {
        viewModelScope.launch {
            classRepo.deleteClassSession(id)
                .onSuccess { loadPlannerData(force = true) }
                .onFailure { _state.value = PlannerState.Error("No se pudo eliminar la clase: ${it.message}") }
        }
    }

    /** Cambia la modalidad de una clase (Presencial / Virtual / Cancelada). */
    fun setModality(id: Long, modality: String) {
        viewModelScope.launch {
            classRepo.updateModality(id, modality)
                .onSuccess { loadPlannerData(force = true) }
                .onFailure { _state.value = PlannerState.Error("No se pudo actualizar la clase: ${it.message}") }
        }
    }

    /** Edita hora, lugar y modalidad de una clase suelta. */
    fun updateClass(
        session: ClassSession,
        startTime: String,
        endTime: String,
        location: String,
        modality: String
    ) {
        viewModelScope.launch {
            val updated = session.copy(
                startTime = startTime,
                endTime = endTime,
                location = location,
                modality = modality
            )
            classRepo.updateClassSession(session.id, updated)
                .onSuccess { loadPlannerData(force = true) }
                .onFailure { _state.value = PlannerState.Error("No se pudo editar la clase: ${it.message}") }
        }
    }
}


// Estados
sealed class PlannerState {
    object Initial : PlannerState()
    object Loading : PlannerState()
    object Empty : PlannerState()
    data class Success(
        val classes: List<ClassSession> = emptyList(),
        val tasks: List<Task> = emptyList()
    ) : PlannerState()
    data class Error(val message: String) : PlannerState()
}

sealed class AIPlanState {
    object Initial : AIPlanState()
    object Loading : AIPlanState()
    data class Success(val plans: List<StudyPlan>) : AIPlanState()
    data class DailySuccess(val dailyPlan: DailyPlan) : AIPlanState()
    data class Error(val message: String) : AIPlanState()
}