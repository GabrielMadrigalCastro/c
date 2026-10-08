package cr.una.delta.frontend_kode.domain.model

import java.time.LocalDate
import java.time.LocalTime

// =============================================================
// 📘 Modelo principal del plan de estudio
// =============================================================
data class StudyPlan(
    val id: Long? = null,
    val studentId: Long? = null,
    val assignmentId: Long? = null,
    val plannedDate: LocalDate,
    val blocks: List<StudyPlanBlock> = emptyList()
)

// =============================================================
// 📅 Plan diario generado
// =============================================================
data class DailyPlan(
    val date: LocalDate,
    val dayName: String,
    val blocks: List<StudyPlanBlock>
)

// =============================================================
// ⏰ Bloque individual del día (clase, tarea, comida, viaje…)
// =============================================================
data class StudyPlanBlock(
    val id: Long? = null,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val type: BlockType,
    val description: String,
    val color: String? = null, // ✅ nuevo campo opcional
    // Detalle para el pop up de un bloque de estudio específico (rúbrica/tarea).
    val detailTitle: String? = null,
    val detailCourse: String? = null,
    val detailDueDate: String? = null // yyyy-MM-dd
)

// =============================================================
// 🧠 Tipos de bloque del día
// =============================================================
enum class BlockType {
    CLASS,       // Clases presenciales o virtuales
    STUDY,       // Estudio o tareas
    MEAL,        // Comidas (almuerzo, cena)
    TRAVEL,      // Viajes (traslado, regreso)
    PERSONAL     // Actividades personales o descanso
}
