package cr.una.delta.frontend_kode.data.mapper

import cr.una.delta.frontend_kode.data.remote.dto.StudyPlanDTO
import cr.una.delta.frontend_kode.domain.model.StudyPlan
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StudyPlanMapper {

    fun StudyPlanDTO.toDomain(): StudyPlan {
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        return StudyPlan(
            id = this.id ?: 0L,
            studentId = this.studentId,
            assignmentId = this.assignmentId,
            plannedDate = LocalDate.parse(this.plannedDate, dateFormatter)
        )
    }

    fun List<StudyPlanDTO>.toDomainList(): List<StudyPlan> = this.map { it.toDomain() }
}
