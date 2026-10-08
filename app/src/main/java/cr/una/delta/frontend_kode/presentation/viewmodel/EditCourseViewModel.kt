package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.data.remote.api.ClassSessionService
import cr.una.delta.frontend_kode.data.remote.api.CourseService
import cr.una.delta.frontend_kode.data.remote.dto.ClassSessionDto
import cr.una.delta.frontend_kode.data.remote.dto.CourseDto
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

sealed class EditCourseState {
    object Loading : EditCourseState()
    object Ready : EditCourseState()
    object Saving : EditCourseState()
    object Success : EditCourseState()
    data class Error(val message: String) : EditCourseState()
}

@HiltViewModel
class EditCourseViewModel @Inject constructor(
    private val courseService: CourseService,
    private val classSessionService: ClassSessionService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EditCourseState>(EditCourseState.Loading)
    val state: StateFlow<EditCourseState> = _state

    // Horario derivado de las clases actuales del curso (para prellenar el formulario)
    private val _horarios = MutableStateFlow<List<HorarioForm>>(emptyList())
    val horarios: StateFlow<List<HorarioForm>> = _horarios

    // Vigencia (inicio/fin) derivada de las clases actuales, para prellenar el formulario.
    private val _startDate = MutableStateFlow(LocalDate.now())
    val startDate: StateFlow<LocalDate> = _startDate
    private val _endDate = MutableStateFlow(LocalDate.now().plusWeeks(16))
    val endDate: StateFlow<LocalDate> = _endDate

    fun load(courseId: Long) {
        viewModelScope.launch {
            _state.value = EditCourseState.Loading
            val all = runCatching {
                classSessionService.getAllClassSessions().body()
            }.getOrNull() ?: emptyList()
            val classes = all.filter { it.courseId == courseId }
            val derived = classes.mapNotNull { toHorario(it) }.distinct()
            _horarios.value = derived.ifEmpty { listOf(HorarioForm()) }

            // Prellenar inicio/fin con el rango real de clases del curso.
            val dates = classes.mapNotNull { runCatching { LocalDate.parse(it.classDate) }.getOrNull() }
            if (dates.isNotEmpty()) {
                _startDate.value = dates.min()
                _endDate.value = dates.max()
            }
            _state.value = EditCourseState.Ready
        }
    }

    fun save(
        courseId: Long,
        name: String,
        color: String,
        horarios: List<HorarioForm>,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        viewModelScope.launch {
            _state.value = EditCourseState.Saving
            try {
                val user = authRepository.getCurrentUser().getOrNull()
                if (user == null) {
                    _state.value = EditCourseState.Error("Sesión no válida.")
                    return@launch
                }
                if (endDate.isBefore(startDate)) {
                    _state.value = EditCourseState.Error("La fecha de fin no puede ser antes de la de inicio.")
                    return@launch
                }

                // 1) Actualizar datos del curso
                courseService.updateCourse(
                    courseId,
                    CourseDto(courseId, user.id, name.trim(), null, color, null, null)
                )

                // 2) Borrar TODAS las clases actuales del curso (en paralelo)
                val allExisting = runCatching {
                    classSessionService.getAllClassSessions().body()
                }.getOrNull() ?: emptyList()
                coroutineScope {
                    allExisting.filter { it.courseId == courseId }
                        .map { async { runCatching { classSessionService.deleteClassSession(it.id) } } }
                        .awaitAll()
                }

                // 3) Generar las clases nuevas (semanalmente entre inicio y fin) en paralelo
                val payloads = mutableListOf<ClassSessionDto>()
                val seen = mutableSetOf<java.time.DayOfWeek>()
                horarios.forEach { h ->
                    val st = to24(h.startHour, h.startMinute, h.startAM) ?: return@forEach
                    val et = to24(h.endHour, h.endMinute, h.endAM) ?: return@forEach
                    if (!seen.add(h.day)) return@forEach // un día no puede repetirse en el horario
                    var date = startDate.with(TemporalAdjusters.nextOrSame(h.day))
                    var guard = 0
                    while (!date.isAfter(endDate) && guard < 200) {
                        payloads.add(
                            ClassSessionDto(0L, courseId, user.id, date.toString(), st, et, "", "Presencial")
                        )
                        date = date.plusWeeks(1)
                        guard++
                    }
                }
                coroutineScope {
                    payloads.map { dto ->
                        async { runCatching { classSessionService.createClassSession(dto) } }
                    }.awaitAll()
                }

                _state.value = EditCourseState.Success
            } catch (e: Exception) {
                _state.value = EditCourseState.Error(e.message ?: "Error al guardar.")
            }
        }
    }

    private fun toHorario(c: ClassSessionDto): HorarioForm? {
        val date = runCatching { LocalDate.parse(c.classDate) }.getOrNull() ?: return null
        val s = parse12(c.startTime) ?: return null
        val e = parse12(c.endTime) ?: return null
        return HorarioForm(date.dayOfWeek, s.first, s.second, s.third, e.first, e.second, e.third)
    }

    /** "HH:mm" (24h) -> (hora12, minuto, esAM). */
    private fun parse12(s: String): Triple<String, String, Boolean>? {
        val parts = s.split(":")
        val h24 = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
        val m = parts.getOrNull(1)?.take(2)?.toIntOrNull() ?: return null
        val am = h24 < 12
        var h12 = h24 % 12
        if (h12 == 0) h12 = 12
        return Triple(h12.toString(), "%02d".format(m), am)
    }

    private fun to24(hour: String, minute: String, am: Boolean): String? {
        var h = hour.trim().toIntOrNull() ?: return null
        val m = minute.trim().toIntOrNull() ?: return null
        if (h !in 1..12 || m !in 0..59) return null
        if (!am && h != 12) h += 12
        if (am && h == 12) h = 0
        return "%02d:%02d".format(h, m)
    }
}
