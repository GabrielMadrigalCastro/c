package cr.una.delta.frontend_kode.data.remote.dto

/** Respuesta del OCR: el texto leído por Gemini (o un error). */
data class OcrResponse(
    val text: String? = null,
    val error: String? = null
)
