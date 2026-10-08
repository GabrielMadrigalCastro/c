package cr.una.delta.frontend_kode.domain.model

// Tipo de evento: clase, tarea, almuerzo, traslado, etc.
enum class EventType {
    CLASS,
    TASK,
    LUNCH,
    TRAVEL,
    OTHER,
    MEAL,
    PERSONAL
}

// Evento genérico para el home (clase, estudio, recordatorio, etc.)
data class NextEvent(
    val title: String,
    val startTime: String, // hh:mm
    val endTime: String,   // hh:mm
    val type: EventType,
    var colorHex: String = ""
)
