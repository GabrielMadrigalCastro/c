package cr.una.delta.frontend_kode.data.remote.dto

/** Pide a la IA un resumen + checklist de estudio de un apunte. */
data class ResumenApunteRequest(
    val titulo: String? = null,
    val contenido: String
)

data class ResumenApunteResponse(
    val resumen: String = "",
    val checklist: List<String> = emptyList(),
    val generadoPor: String = "ia"   // "ia" o "error"
)

/** Situación de notas para pedir una línea de consejo en la calculadora. */
data class ConsejoNotasRequest(
    val curso: String? = null,
    val notaMeta: Double,
    val notaActual: Double,
    val pesoFaltante: Double,
    val promedioNecesario: Double? = null,
    val alcanzable: Boolean = true
)

data class ConsejoNotasResponse(
    val consejo: String = "",
    val generadoPor: String = "ia"   // "ia" o "error"
)
