package cr.una.delta.frontend_kode.data.remote.dto

/**
 * DTOs para el plan del día con IA (POST /v1/studyplans/dia-ia).
 * Los nombres coinciden con los del backend (Gson los serializa igual).
 */

data class ClaseCtxDto(
    val curso: String,
    val inicio: String,      // HH:mm
    val fin: String,         // HH:mm
    val modalidad: String,
    val lugar: String? = null
)

data class PendienteCtxDto(
    val titulo: String,
    val curso: String? = null,
    val fecha: String? = null   // yyyy-MM-dd
)

data class DiaIARequestDto(
    val fecha: String? = null,
    val clases: List<ClaseCtxDto>,
    val pendientes: List<PendienteCtxDto>,
    val horasEstudio: Int,
    val estudioInicio: String,
    val estudioFin: String,
    val desayuno: String? = null,
    val almuerzo: String? = null,
    val cena: String? = null,
    val viajeMinutos: Int
)

data class BloquePlanDto(
    val inicio: String,      // HH:mm
    val fin: String,         // HH:mm
    val tipo: String,        // CLASS | TRAVEL | MEAL | STUDY | PERSONAL
    val titulo: String,
    val detalleCurso: String? = null,
    val detalleFecha: String? = null
)

data class DiaIAResponseDto(
    val generadoPor: String,     // "ia" o "error"
    val bloques: List<BloquePlanDto>
)
