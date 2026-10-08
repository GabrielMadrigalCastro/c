package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.data.remote.api.ClassSessionService
import cr.una.delta.frontend_kode.data.remote.api.CourseService
import cr.una.delta.frontend_kode.data.remote.api.EnrollmentService
import cr.una.delta.frontend_kode.data.remote.dto.ClassSessionDto
import cr.una.delta.frontend_kode.data.remote.dto.CourseDto
import cr.una.delta.frontend_kode.data.remote.dto.EnrollmentDto
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/** Un bloque de horario: día + hora de inicio y fin (12h con AM/PM). */
data class HorarioForm(
    val day: DayOfWeek = DayOfWeek.MONDAY,
    val startHour: String = "8",
    val startMinute: String = "00",
    val startAM: Boolean = true,
    val endHour: String = "10",
    val endMinute: String = "00",
    val endAM: Boolean = true
)

sealed class CreateCourseState {
    object Idle : CreateCourseState()
    object Saving : CreateCourseState()
    object Success : CreateCourseState()
    data class Error(val message: String) : CreateCourseState()
}

@HiltViewModel
class CreateCourseViewModel @Inject constructor(
    private val courseService: CourseService,
    private val enrollmentService: EnrollmentService,
    private val classSessionService: ClassSessionService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CreateCourseState>(CreateCourseState.Idle)
    val state: StateFlow<CreateCourseState> = _state

    fun reset() { _state.value = CreateCourseState.Idle }

    /**
     * Crea el curso (profesor = el propio usuario), genera las clases del horario
     * (8 semanas hacia adelante por cada día) y matricula al estudiante.
     */
    fun createCourse(
        name: String,
        color: String,
        horarios: List<HorarioForm>,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        viewModelScope.launch {
            _state.value = CreateCourseState.Saving
            try {
                val user = authRepository.getCurrentUser().getOrNull()
                if (user == null) {
                    _state.value = CreateCourseState.Error("Sesión no válida. Inicia sesión de nuevo.")
                    return@launch
                }
                if (name.isBlank()) {
                    _state.value = CreateCourseState.Error("El nombre del curso es obligatorio.")
                    return@launch
                }
                if (endDate.isBefore(startDate)) {
                    _state.value = CreateCourseState.Error("La fecha de fin no puede ser antes de la de inicio.")
                    return@launch
                }

                val courseResp = courseService.createCourse(
                    CourseDto(
                        courseId = 0L,
                        professorId = user.id,
                        courseName = name.trim(),
                        courseCode = null,
                        courseColor = color,
                        independentStudyHours = null,
                        createdAt = null
                    )
                )
                if (!courseResp.isSuccessful || courseResp.body() == null) {
                    _state.value = CreateCourseState.Error("No se pudo crear el curso (${courseResp.code()}).")
                    return@launch
                }
                val courseId = courseResp.body()!!.courseId

                // Generar las clases del horario, semanalmente entre la fecha de
                // inicio y la de fin (por cada día del horario).
                val payloads = mutableListOf<ClassSessionDto>()
                val seen = mutableSetOf<Triple<DayOfWeek, String, String>>()
                horarios.forEach { h ->
                    val st = to24(h.startHour, h.startMinute, h.startAM) ?: return@forEach
                    val et = to24(h.endHour, h.endMinute, h.endAM) ?: return@forEach
                    if (!seen.add(Triple(h.day, st, et))) return@forEach // evita clases duplicadas a la misma hora
                    var date = startDate.with(TemporalAdjusters.nextOrSame(h.day))
                    var guard = 0
                    while (!date.isAfter(endDate) && guard < 200) {
                        payloads.add(
                            ClassSessionDto(
                                id = 0L,
                                courseId = courseId,
                                professorId = user.id,
                                classDate = date.toString(), // yyyy-MM-dd
                                startTime = st,
                                endTime = et,
                                location = "",
                                modality = "Presencial"
                            )
                        )
                        date = date.plusWeeks(1)
                        guard++
                    }
                }

                // Crear todas las clases en paralelo (mucho más rápido que en fila).
                coroutineScope {
                    payloads.map { dto ->
                        async { runCatching { classSessionService.createClassSession(dto) } }
                    }.awaitAll()
                }

                runCatching {
                    enrollmentService.createEnrollment(
                        EnrollmentDto(id = 0L, courseId = courseId, studentId = user.id, enrolledAt = "")
                    )
                }

                _state.value = CreateCourseState.Success
            } catch (e: Exception) {
                _state.value = CreateCourseState.Error(e.message ?: "Error al crear el curso.")
            }
        }
    }

    /** Convierte hora 12h + AM/PM a "HH:mm" 24h. Null si es inválido. */
    private fun to24(hour: String, minute: String, am: Boolean): String? {
        var h = hour.trim().toIntOrNull() ?: return null
        val m = minute.trim().toIntOrNull() ?: return null
        if (h !in 1..12 || m !in 0..59) return null
        if (!am && h != 12) h += 12
        if (am && h == 12) h = 0
        return "%02d:%02d".format(h, m)
    }
}
