package cr.una.delta.frontend_kode.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// ===================== REQUEST DTOs =====================
data class GenerateStudyPlanRequest(
    val studentId: Long,
    val assignmentId: Long,
    val horasDisponiblesPorDia: Int = 2,
    val diasAntesDueDate: Int = 7
) : Serializable

data class GenerateDailyPlanRequest(
    val studentId: Long,
    val assignmentId: Long,
    val fecha: String? = null
) : Serializable

data class GenerateWeeklyPlanRequest(
    val studentId: Long,
    val assignmentId: Long,
    val fechaInicio: String? = null,
    val dias: Int = 7
) : Serializable

data class ChatRequest(
    val mensaje: String
) : Serializable

// ===================== RESPONSE DTOs =====================
data class StudyPlanResponse(
    val status: String,
    val message: String,
    val totalPlanes: Int?,
    val planes: List<StudyPlanDTO>
) : Serializable

data class DailyPlanResponse(
    val status: String,
    val message: String,
    val fecha: String,
    val dia: String,
    val studentId: Long,
    val assignmentId: Long
) : Serializable


data class WeeklyPlanResponse(
    val status: String,
    val message: String,
    val totalBloques: Int,
    val studentId: Long,
    val assignmentId: Long,
    val planes: List<StudyPlanDTO>
) : Serializable

data class ChatResponse(
    val respuesta: String
) : Serializable

// ===================== StudyPlan DTO =====================
data class StudyPlanDTO(
    val id: Long?,
    val studentId: Long,
    val assignmentId: Long,
    val plannedDate: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val generatedByAi: Boolean
) : Serializable
// ===================== DayPlan DTO =====================
data class DayPlanResponse(
    val date: String,
    val currentTime: String,
    val bloques: List<DayPlanBlockDTO>
) : Serializable

data class DayPlanBlockDTO(
    val status: String,
    val startTime: String,
    val endTime: String,
    val isActive: Boolean
) : Serializable
