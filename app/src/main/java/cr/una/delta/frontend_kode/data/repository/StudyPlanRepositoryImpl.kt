package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.domain.model.*
import cr.una.delta.frontend_kode.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class StudyPlanRepositoryImpl @Inject constructor(
    private val classRepo: ClassSessionRepository,
    private val taskRepo: TaskRepository,
    private val courseRepo: CourseRepository
) : StudyPlanRepository {

    override suspend fun generateStudyPlan(
        studentId: Long,
        assignmentId: Long,
        hoursPerDay: Int,
        daysBeforeDueDate: Int
    ): Result<List<StudyPlan>> = Result.success(emptyList())

    override suspend fun generateDailyPlan(
        studentId: Long,
        assignmentId: Long,
        date: LocalDate?
    ): Result<DailyPlan> = withContext(Dispatchers.IO) {
        try {
            val today = date ?: LocalDate.now()
            val tasks = taskRepo.getAll().getOrDefault(emptyList())

            val blocks = mutableListOf<StudyPlanBlock>()

            // === CLASES REALES DE HOY (desde el backend) ===
            val courses = courseRepo.getCoursesByStudent(studentId).getOrDefault(emptyList())
            val courseNameById = courses.associate { it.courseId to it.courseName }
            val courseColorById = courses.associate { it.courseId to it.courseColor }

            fun parseTime(s: String): LocalTime? =
                runCatching { LocalTime.parse(s) }.getOrNull()
                    ?: runCatching { LocalTime.parse(s.take(5)) }.getOrNull()

            val todayClasses = classRepo.getTodayClasses().getOrDefault(emptyList())
            for (c in todayClasses) {
                val cStart = parseTime(c.startTime) ?: continue
                val cEnd = parseTime(c.endTime) ?: continue
                val hex = courseColorById[c.courseId]?.takeIf { it.startsWith("#") } ?: "#A5C8FF"
                blocks += StudyPlanBlock(
                    id = c.id,
                    startTime = cStart,
                    endTime = cEnd,
                    type = BlockType.CLASS,
                    description = courseNameById[c.courseId] ?: "Clase",
                    color = hex
                )
            }

            // === TAREAS REALES ===
            val occupied = blocks.map { it.startTime to it.endTime }.toMutableList()

            fun isFree(start: LocalTime, end: LocalTime): Boolean =
                occupied.none { (s, e) -> start < e && end > s }

            var current = LocalTime.of(7, 0)
            val endOfDay = LocalTime.of(22, 0)

            for (task in tasks) {
                val duration = 90L // cada bloque de tarea dura 1.5h
                while (current.plusMinutes(duration) < endOfDay) {
                    val start = current
                    val end = current.plusMinutes(duration)
                    if (isFree(start, end)) {
                        occupied += start to end
                        blocks += StudyPlanBlock(
                            id = task.id,
                            startTime = start,
                            endTime = end,
                            type = BlockType.STUDY,
                            description = task.title.trim(),
                            color = "#D1B3FF" // Lila pastel
                        )
                        current = end.plusMinutes(15)
                        break
                    }
                    current = current.plusMinutes(15)
                }
            }

            val sorted = blocks.sortedBy { it.startTime }

            Result.success(
                DailyPlan(
                    date = today,
                    dayName = today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                    blocks = sorted
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    override suspend fun getDailyPlan(studentId: Long, date: LocalDate): Result<DailyPlan> =
        Result.failure(Exception("El plan diario se genera dinámicamente en frontend."))

    override suspend fun generateWeeklyPlan(
        studentId: Long,
        assignmentId: Long,
        startDate: LocalDate?,
        days: Int
    ): Result<List<StudyPlan>> = Result.success(emptyList())

    override suspend fun chatWithAI(message: String): Result<String> =
        Result.success("IA simulada en frontend")
}
