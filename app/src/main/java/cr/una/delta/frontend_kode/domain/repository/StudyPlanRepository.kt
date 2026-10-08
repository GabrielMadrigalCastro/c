package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.DailyPlan
import cr.una.delta.frontend_kode.domain.model.StudyPlan
import java.time.LocalDate

interface StudyPlanRepository {

    suspend fun generateStudyPlan(
        studentId: Long,
        assignmentId: Long,
        hoursPerDay: Int = 2,
        daysBeforeDueDate: Int = 7
    ): Result<List<StudyPlan>>

    suspend fun generateDailyPlan(
        studentId: Long,
        assignmentId: Long,
        date: LocalDate? = null
    ): Result<DailyPlan>

    suspend fun getDailyPlan(studentId: Long, date: LocalDate): Result<DailyPlan>


    suspend fun generateWeeklyPlan(
        studentId: Long,
        assignmentId: Long,
        startDate: LocalDate? = null,
        days: Int = 7
    ): Result<List<StudyPlan>>

    suspend fun chatWithAI(message: String): Result<String>
}