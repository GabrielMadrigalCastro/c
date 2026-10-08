package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.*
import cr.una.delta.frontend_kode.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TeacherHomeViewModel @Inject constructor(
    private val eventRepo: EventRepository,
    private val classRepo: ClassSessionRepository,
    private val reminderRepo: ReminderRepository,
    private val alertRepo: AlertRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate

    private val _state = MutableStateFlow<TeacherHomeState>(TeacherHomeState.Initial)
    val state: StateFlow<TeacherHomeState> = _state

    init {
        loadTeacherData()
    }

    fun selectDate(cal: Calendar) {
        _selectedDate.value = cal.clone() as Calendar
    }

    fun selectDay(day: Int) {
        _selectedDate.value = (_selectedDate.value.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, day)
        }
    }

    fun loadTeacherData() {
        viewModelScope.launch {
            _state.value = TeacherHomeState.Loading
            try {
                // 🔹 ID del profesor autenticado
                val professorId = authRepo.getCurrentUser().getOrNull()?.id ?: 0L
                if (professorId == 0L) {
                    _state.value = TeacherHomeState.Error("Iniciá sesión primero.")
                    return@launch
                }

                val event = eventRepo.getNextEvent().getOrElse { emptyList() }.firstOrNull()
                val classes = classRepo.getClassesByProfessor(professorId).getOrElse { emptyList() }
                val reminders = reminderRepo.getReminders().getOrElse { emptyList() }
                val alerts = alertRepo.getByProfessorToday(professorId).getOrElse { emptyList() }

                if (event == null && classes.isEmpty() && reminders.isEmpty() && alerts.isEmpty()) {
                    _state.value = TeacherHomeState.Empty
                } else {
                    _state.value = TeacherHomeState.Success(
                        nextEvent = event,
                        classes = classes,
                        reminders = reminders,
                        alerts = alerts
                    )
                }
            } catch (e: Exception) {
                _state.value = TeacherHomeState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateModality(classId: Long, newModality: String) {
        viewModelScope.launch {
            try {
                classRepo.updateModality(classId, newModality)
                _state.value = TeacherHomeState.Loading
                loadTeacherData()
            } catch (e: Exception) {
                println("❌ Error actualizando modalidad: ${e.message}")
            }
        }
    }
}

sealed class TeacherHomeState {
    object Initial : TeacherHomeState()
    object Loading : TeacherHomeState()
    object Empty : TeacherHomeState()
    data class Success(
        val nextEvent: Event? = null,
        val classes: List<ClassSession> = emptyList(),
        val reminders: List<Reminder> = emptyList(),
        val alerts: List<Alert> = emptyList()
    ) : TeacherHomeState()
    data class Error(val message: String) : TeacherHomeState()
}