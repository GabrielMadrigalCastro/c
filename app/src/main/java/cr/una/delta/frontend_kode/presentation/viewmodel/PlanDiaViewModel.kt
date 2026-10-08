package cr.una.delta.frontend_kode.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import cr.una.delta.frontend_kode.data.local.StudentRubricDates
import cr.una.delta.frontend_kode.data.remote.api.IAApiService
import cr.una.delta.frontend_kode.data.remote.dto.ClaseCtxDto
import cr.una.delta.frontend_kode.data.remote.dto.DiaIARequestDto
import cr.una.delta.frontend_kode.data.remote.dto.PendienteCtxDto
import cr.una.delta.frontend_kode.domain.model.BlockType
import cr.una.delta.frontend_kode.domain.model.ClassSession
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.domain.model.DailyPlan
import cr.una.delta.frontend_kode.domain.model.StudyPlanBlock
import cr.una.delta.frontend_kode.domain.model.Task
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import cr.una.delta.frontend_kode.domain.repository.ClassSessionRepository
import cr.una.delta.frontend_kode.domain.repository.CourseRepository
import cr.una.delta.frontend_kode.domain.repository.CourseRubricRepository
import cr.una.delta.frontend_kode.domain.repository.TaskRepository
import java.time.ZoneId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class PlanDiaState {
    data object Loading : PlanDiaState()
    data class Ready(val plan: DailyPlan) : PlanDiaState()
    data class Error(val message: String) : PlanDiaState()
}

/** Horarios de las comidas (editables por el usuario). */
data class MealTimes(
    val breakfastStart: LocalTime = LocalTime.of(7, 0),
    val breakfastEnd: LocalTime = LocalTime.of(7, 30),
    val lunchStart: LocalTime = LocalTime.of(12, 0),
    val lunchEnd: LocalTime = LocalTime.of(13, 0),
    val dinnerStart: LocalTime = LocalTime.of(19, 0),
    val dinnerEnd: LocalTime = LocalTime.of(20, 0)
)

/** Algo por hacer/estudiar hoy: una tarea o una evaluación (rúbrica con fecha). */
private data class PendingItem(
    val label: String,
    val date: LocalDate?,
    val title: String,
    val course: String? = null
)

/** Ventana en la que el usuario puede estudiar: los bloques de estudio caen acá. */
data class StudyWindow(
    val start: LocalTime = LocalTime.of(15, 0),
    val end: LocalTime = LocalTime.of(21, 0)
)

/**
 * Arma el "Plan del día inteligente": clases (fijas) + viaje + comidas +
 * bloques de estudio priorizados por fecha de entrega, respetando las horas
 * de estudio elegidas. El usuario puede quitar bloques de estudio.
 */
@HiltViewModel
class PlanDiaViewModel @Inject constructor(
    private val classRepo: ClassSessionRepository,
    private val taskRepo: TaskRepository,
    private val courseRepo: CourseRepository,
    private val rubricRepo: CourseRubricRepository,
    private val authRepo: AuthRepository,
    private val iaService: IAApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<PlanDiaState>(PlanDiaState.Loading)
    val state: StateFlow<PlanDiaState> = _state

    private val _studyHours = MutableStateFlow(3)
    val studyHours: StateFlow<Int> = _studyHours

    private val _meals = MutableStateFlow(loadMeals())
    val meals: StateFlow<MealTimes> = _meals

    private val _studyWindow = MutableStateFlow(loadStudyWindow())
    val studyWindow: StateFlow<StudyWindow> = _studyWindow

    private var studentId: Long? = null
    private val removedTasks = mutableSetOf<String>()

    private companion object {
        val DAY_START: LocalTime = LocalTime.of(6, 0)
        val DAY_END: LocalTime = LocalTime.of(22, 0)
        const val TRAVEL_MIN = 60L   // viaje mínimo 1 hora (solo para clases presenciales)
        const val STUDY_BLOCK_MIN = 90L
        const val STUDY_GAP_MIN = 15L
        const val STUDY_PURPLE = "#D1B3FF"
        const val PREFS = "kode_meals"
    }

    fun setStudyHours(h: Int) {
        _studyHours.value = h.coerceIn(0, 10)
        regenerate()
    }

    /** Guarda los horarios de comidas y regenera el plan. */
    fun setMealTimes(m: MealTimes) {
        _meals.value = m
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putString("des_s", m.breakfastStart.toString()); putString("des_e", m.breakfastEnd.toString())
            putString("alm_s", m.lunchStart.toString());     putString("alm_e", m.lunchEnd.toString())
            putString("cen_s", m.dinnerStart.toString());    putString("cen_e", m.dinnerEnd.toString())
            apply()
        }
        regenerate()
    }

    private fun loadMeals(): MealTimes {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        fun t(key: String, def: LocalTime): LocalTime =
            p.getString(key, null)?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: def
        val d = MealTimes()
        return MealTimes(
            breakfastStart = t("des_s", d.breakfastStart), breakfastEnd = t("des_e", d.breakfastEnd),
            lunchStart = t("alm_s", d.lunchStart),         lunchEnd = t("alm_e", d.lunchEnd),
            dinnerStart = t("cen_s", d.dinnerStart),       dinnerEnd = t("cen_e", d.dinnerEnd)
        )
    }

    /** Guarda la ventana "puedo estudiar de X a Y" y regenera el plan. */
    fun setStudyWindow(w: StudyWindow) {
        _studyWindow.value = w
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            putString("study_s", w.start.toString()); putString("study_e", w.end.toString()); apply()
        }
        regenerate()
    }

    private fun loadStudyWindow(): StudyWindow {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        fun t(key: String, def: LocalTime): LocalTime =
            p.getString(key, null)?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: def
        val d = StudyWindow()
        return StudyWindow(t("study_s", d.start), t("study_e", d.end))
    }

    fun load() = regenerate()

    fun removeStudyBlock(block: StudyPlanBlock) {
        removedTasks.add(block.description)
        regenerate()
    }

    fun regenerate() {
        viewModelScope.launch {
            _state.value = PlanDiaState.Loading
            try {
                val sid = resolveStudentId()
                val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val classes = classRepo.getAllClasses().getOrDefault(emptyList())
                    .filter { it.classDate == todayKey }
                val courses = if (sid != null)
                    courseRepo.getCoursesByStudent(sid).getOrDefault(emptyList()) else emptyList()

                // Tareas pendientes.
                val taskItems = taskRepo.getAll().getOrDefault(emptyList())
                    .filter { it.status.id != 3L }
                    .map {
                        PendingItem(
                            label = "Estudiar/Hacer: ${it.title}",
                            date = it.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                            title = it.title,
                            course = null
                        )
                    }

                // Exámenes/entregas (rúbricas con fecha) de los cursos, próximas.
                val today = LocalDate.now()
                val rubricItems = courses.flatMap { c ->
                    rubricRepo.getCourseRubrics(c.courseId).getOrDefault(emptyList()).mapNotNull { r ->
                        // Fecha del profe; si no trae, la fecha personal del estudiante.
                        val iso = r.dueDate?.takeIf { it.isNotBlank() }
                            ?: StudentRubricDates.get(context, r.rubricId)
                        val d = iso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                        if (d != null && !d.isBefore(today))
                            PendingItem(
                                label = "Estudiar: ${r.rubricName.ifBlank { "evaluación" }} · ${c.courseName}",
                                date = d,
                                title = r.rubricName.ifBlank { "Evaluación" },
                                course = c.courseName
                            )
                        else null
                    }
                }

                val pending = (taskItems + rubricItems)
                    .sortedWith(compareBy(nullsLast<LocalDate>()) { it.date })

                // La IA arma el plan; si falla o no hay cuota, se usa el plan local.
                val plan = intentarPlanIA(classes, courses, pending)
                    ?: buildPlan(classes, courses, pending, _studyHours.value)
                _state.value = PlanDiaState.Ready(plan)
            } catch (e: Exception) {
                _state.value = PlanDiaState.Error(e.message ?: "No se pudo armar el plan")
            }
        }
    }

    private fun hhmm(t: LocalTime): String = "%02d:%02d".format(t.hour, t.minute)

    /**
     * Le pide el plan del día a la IA (backend /studyplans/dia-ia) mandándole el
     * contexto. Devuelve null si la IA falla, no responde o viene vacío, para que
     * el llamador use el planificador local como respaldo.
     */
    private suspend fun intentarPlanIA(
        classes: List<ClassSession>,
        courses: List<Course>,
        pending: List<PendingItem>
    ): DailyPlan? {
        return try {
            fun parse(s: String): LocalTime? = runCatching { LocalTime.parse(s.trim()) }.getOrNull()
            val m = _meals.value
            val w = _studyWindow.value

            val request = DiaIARequestDto(
                fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                clases = classes.mapNotNull { c ->
                    val ini = parse(c.startTime) ?: return@mapNotNull null
                    val fin = parse(c.endTime) ?: return@mapNotNull null
                    if (fin <= ini || c.modality.equals("Cancelada", true)) return@mapNotNull null
                    ClaseCtxDto(
                        curso = courses.find { it.courseId == c.courseId }?.courseName ?: "Clase",
                        inicio = hhmm(ini),
                        fin = hhmm(fin),
                        modalidad = c.modality,
                        lugar = c.location.ifBlank { null }
                    )
                },
                pendientes = pending.filter { it.label !in removedTasks }.map {
                    PendienteCtxDto(titulo = it.title, curso = it.course, fecha = it.date?.toString())
                },
                horasEstudio = _studyHours.value,
                estudioInicio = hhmm(w.start),
                estudioFin = hhmm(w.end),
                desayuno = "${hhmm(m.breakfastStart)}-${hhmm(m.breakfastEnd)}",
                almuerzo = "${hhmm(m.lunchStart)}-${hhmm(m.lunchEnd)}",
                cena = "${hhmm(m.dinnerStart)}-${hhmm(m.dinnerEnd)}",
                viajeMinutos = TRAVEL_MIN.toInt()
            )

            val resp = iaService.generarPlanDiaIA(request)
            val body = resp.body()
            if (!resp.isSuccessful || body == null || body.bloques.isEmpty()) return null

            val blocks = body.bloques.mapNotNull { b ->
                val st = parse(b.inicio) ?: return@mapNotNull null
                val et = parse(b.fin) ?: return@mapNotNull null
                if (et <= st) return@mapNotNull null
                val tipo = when (b.tipo.uppercase()) {
                    "CLASS" -> BlockType.CLASS
                    "TRAVEL" -> BlockType.TRAVEL
                    "MEAL" -> BlockType.MEAL
                    "STUDY" -> BlockType.STUDY
                    else -> BlockType.PERSONAL
                }
                StudyPlanBlock(
                    id = null,
                    startTime = st,
                    endTime = et,
                    type = tipo,
                    description = b.titulo,
                    color = if (tipo == BlockType.STUDY) STUDY_PURPLE else null,
                    detailTitle = if (tipo == BlockType.STUDY) b.titulo else null,
                    detailCourse = b.detalleCurso,
                    detailDueDate = b.detalleFecha
                )
            }.sortedBy { it.startTime }
            if (blocks.isEmpty()) return null

            val dayName = SimpleDateFormat("EEEE dd/MM", Locale.getDefault()).format(Date())
                .replaceFirstChar { it.uppercase() }
            DailyPlan(date = LocalDate.now(), dayName = dayName, blocks = blocks)
        } catch (e: Exception) {
            null // cualquier fallo -> respaldo local
        }
    }

    private fun buildPlan(
        classes: List<ClassSession>,
        courses: List<Course>,
        pendingItems: List<PendingItem>,
        studyHours: Int
    ): DailyPlan {
        val blocks = mutableListOf<StudyPlanBlock>()
        val occupied = mutableListOf<Pair<LocalTime, LocalTime>>()

        fun parse(s: String): LocalTime? = runCatching { LocalTime.parse(s.trim()) }.getOrNull()

        // 1) Clases (todas menos canceladas) + viaje CONSOLIDADO.
        //    El viaje se ancla solo a clases PRESENCIALES: una ida antes de la
        //    primera presencial y una vuelta después de la última. Lo que quede
        //    en medio (virtual o presencial) no genera más viajes: te quedás en la U.
        val valid = classes.mapNotNull { c ->
            val st = parse(c.startTime) ?: return@mapNotNull null
            val et = parse(c.endTime) ?: return@mapNotNull null
            if (et <= st || c.modality.equals("Cancelada", true)) return@mapNotNull null
            c to (st to et)
        }
        valid.forEach { (c, times) ->
            val (st, et) = times
            val name = courses.find { it.courseId == c.courseId }?.courseName ?: "Clase"
            val color = courses.find { it.courseId == c.courseId }?.courseColor ?: "#A5C8FF"
            blocks.add(StudyPlanBlock(null, st, et, BlockType.CLASS, name, color))
            occupied.add(st to et)
        }
        val presencial = valid.filter { !it.first.modality.equals("Virtual", true) }.map { it.second }
        if (presencial.isNotEmpty()) {
            val firstStart = presencial.minOf { it.first }
            val lastEnd = presencial.maxOf { it.second }
            val before = firstStart.minusMinutes(TRAVEL_MIN)
            if (!before.isBefore(DAY_START)) {
                blocks.add(StudyPlanBlock(null, before, firstStart, BlockType.TRAVEL, "Camino a la U", null))
                occupied.add(before to firstStart)
            }
            val after = lastEnd.plusMinutes(TRAVEL_MIN)
            if (!after.isAfter(DAY_END)) {
                blocks.add(StudyPlanBlock(null, lastEnd, after, BlockType.TRAVEL, "Viaje de regreso", null))
                occupied.add(lastEnd to after)
            }
        }

        // 2) Comidas: si una comida choca con una clase/viaje, se corre más
        //    temprano (termina justo cuando empieza el bloque que la tapa).
        val m = _meals.value
        fun overlaps(aS: LocalTime, aE: LocalTime): Boolean =
            occupied.any { (s, e) -> aS < e && s < aE }
        listOf(
            Triple("Desayuno", m.breakfastStart, m.breakfastEnd),
            Triple("Almuerzo", m.lunchStart, m.lunchEnd),
            Triple("Cena", m.dinnerStart, m.dinnerEnd)
        ).forEach { (nombre, ini0, fin0) ->
            if (fin0 <= ini0) return@forEach
            var ini = ini0
            var fin = fin0
            if (overlaps(ini, fin)) {
                val durMin = Duration.between(ini0, fin0).toMinutes()
                val firstOverlapStart = occupied.filter { (s, e) -> ini0 < e && s < fin0 }.minOf { it.first }
                val newIni = firstOverlapStart.minusMinutes(durMin)
                if (!newIni.isBefore(DAY_START) && !overlaps(newIni, firstOverlapStart)) {
                    ini = newIni
                    fin = firstOverlapStart
                }
            }
            blocks.add(StudyPlanBlock(null, ini, fin, BlockType.MEAL, nombre, null))
            occupied.add(ini to fin)
        }

        // 3) Bloques de estudio en huecos libres, por urgencia (fecha de entrega)
        // Los bloques de estudio caen solo dentro de la ventana "puedo estudiar".
        val win = _studyWindow.value
        val studyStart = if (win.end > win.start) win.start else DAY_START
        val studyEnd = if (win.end > win.start) win.end else DAY_END
        val free = computeFree(studyStart, studyEnd, occupied)
        // Tareas + evaluaciones (rúbricas con fecha), ya ordenadas por urgencia.
        val items = pendingItems.filter { it.label !in removedTasks }

        // Se reservan SIEMPRE las horas de estudio elegidas (aunque no haya
        // tareas ni exámenes). Si hay pendientes, se usan como etiqueta por
        // urgencia; cuando se acaban, el bloque queda como "Estudiar".
        var budget = studyHours * 60
        var idx = 0
        for ((start, end) in free) {
            var cursor = start
            while (budget >= 30) {
                val avail = Duration.between(cursor, end).toMinutes()
                if (avail < 30) break
                val dur = minOf(STUDY_BLOCK_MIN, avail, budget.toLong())
                if (dur < 30) break
                val blockEnd = cursor.plusMinutes(dur)
                val item = items.getOrNull(idx)
                val label = item?.label ?: "Estudiar"
                blocks.add(
                    StudyPlanBlock(
                        null, cursor, blockEnd, BlockType.STUDY,
                        label, STUDY_PURPLE,
                        detailTitle = item?.title,
                        detailCourse = item?.course,
                        detailDueDate = item?.date?.toString()
                    )
                )
                budget -= dur.toInt()
                if (idx < items.size) idx++
                cursor = blockEnd.plusMinutes(STUDY_GAP_MIN)
            }
            if (budget < 30) break
        }

        val ordered = blocks.sortedBy { it.startTime }
        val dayName = SimpleDateFormat("EEEE dd/MM", Locale.getDefault()).format(Date())
            .replaceFirstChar { it.uppercase() }
        return DailyPlan(date = LocalDate.now(), dayName = dayName, blocks = ordered)
    }

    /** Huecos libres entre start y end, restando los intervalos ocupados (unidos). */
    private fun computeFree(
        start: LocalTime,
        end: LocalTime,
        occupied: List<Pair<LocalTime, LocalTime>>
    ): List<Pair<LocalTime, LocalTime>> {
        val sorted = occupied.filter { it.second > it.first }.sortedBy { it.first }
        val merged = mutableListOf<Pair<LocalTime, LocalTime>>()
        for (iv in sorted) {
            val last = merged.lastOrNull()
            if (last == null || iv.first > last.second) merged.add(iv)
            else merged[merged.size - 1] = last.first to maxOf(last.second, iv.second)
        }
        val free = mutableListOf<Pair<LocalTime, LocalTime>>()
        var cursor = start
        for ((s, e) in merged) {
            if (s > cursor) free.add(cursor to minOf(s, end))
            if (e > cursor) cursor = e
            if (cursor >= end) break
        }
        if (cursor < end) free.add(cursor to end)
        return free.filter { it.second > it.first }
    }

    private suspend fun resolveStudentId(): Long? {
        studentId?.let { return it }
        val sid = authRepo.getCurrentUser().getOrNull()?.id?.toString()?.toLongOrNull()
        studentId = sid
        return sid
    }
}
