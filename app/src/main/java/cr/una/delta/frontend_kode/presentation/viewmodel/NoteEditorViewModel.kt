package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import cr.una.delta.frontend_kode.data.remote.api.ApunteService
import cr.una.delta.frontend_kode.data.remote.api.IaExtrasService
import cr.una.delta.frontend_kode.data.remote.dto.ApunteDto
import cr.una.delta.frontend_kode.data.remote.dto.ResumenApunteRequest
import cr.una.delta.frontend_kode.data.remote.dto.ResumenApunteResponse
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Item de checklist dentro del editor. */
data class ChecklistItem(val text: String = "", val done: Boolean = false)

/** Editor de una nota (apunte) a pantalla completa. */
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val service: ApunteService,
    private val iaExtras: IaExtrasService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val gson = Gson()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content
    private val _pinned = MutableStateFlow(false)
    val pinned: StateFlow<Boolean> = _pinned
    private val _color = MutableStateFlow<String?>(null)
    val color: StateFlow<String?> = _color
    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags
    private val _checklist = MutableStateFlow<List<ChecklistItem>>(emptyList())
    val checklist: StateFlow<List<ChecklistItem>> = _checklist

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    // ----- Resumen con IA -----
    private val _summarizing = MutableStateFlow(false)
    val summarizing: StateFlow<Boolean> = _summarizing
    // null = no hay resumen abierto; distinto de null = mostrar diálogo con el resultado.
    private val _summary = MutableStateFlow<ResumenApunteResponse?>(null)
    val summary: StateFlow<ResumenApunteResponse?> = _summary
    private val _summaryError = MutableStateFlow<String?>(null)
    val summaryError: StateFlow<String?> = _summaryError

    private var loaded = false

    fun onTitle(v: String) { _title.value = v }
    fun onContent(v: String) { _content.value = v }
    fun togglePin() { _pinned.value = !_pinned.value }
    fun setColor(hex: String?) { _color.value = hex }

    fun addTag(tag: String) {
        val t = tag.trim()
        if (t.isNotEmpty() && !_tags.value.contains(t)) _tags.value = _tags.value + t
    }
    fun removeTag(tag: String) { _tags.value = _tags.value - tag }

    fun addChecklistItem() { _checklist.value = _checklist.value + ChecklistItem("", false) }
    fun setChecklistText(i: Int, text: String) {
        _checklist.value = _checklist.value.toMutableList().also { if (i in it.indices) it[i] = it[i].copy(text = text) }
    }
    fun toggleChecklist(i: Int) {
        _checklist.value = _checklist.value.toMutableList().also { if (i in it.indices) it[i] = it[i].copy(done = !it[i].done) }
    }
    fun removeChecklistItem(i: Int) {
        _checklist.value = _checklist.value.toMutableList().also { if (i in it.indices) it.removeAt(i) }
    }

    /** Carga el apunte si es edición (noteId > 0). Solo una vez. */
    fun load(noteId: Long) {
        if (loaded || noteId <= 0L) { loaded = true; return }
        loaded = true
        viewModelScope.launch {
            _loading.value = true
            runCatching { service.getById(noteId).body() }.getOrNull()?.let { n ->
                _title.value = n.title
                _content.value = n.content
                _pinned.value = n.pinned
                _color.value = n.color
                _tags.value = n.tagList
                _checklist.value = parseChecklist(n.checklist)
            }
            _loading.value = false
        }
    }

    /** Guarda (crea o actualiza) y llama onDone al terminar. */
    fun save(courseId: Long, noteId: Long, onDone: () -> Unit) {
        if (_title.value.isBlank() || _saving.value) return
        viewModelScope.launch {
            _saving.value = true
            val sid = authRepository.getCurrentUser().getOrNull()?.id?.toString()?.toLongOrNull()
            if (sid == null) { _saving.value = false; return@launch }
            val cleanChecklist = _checklist.value.filter { it.text.isNotBlank() }
            val dto = ApunteDto(
                id = if (noteId > 0L) noteId else 0L,
                studentId = sid,
                courseId = if (courseId > 0L) courseId else null,   // 0 = apunte personal
                title = _title.value.trim(),
                content = _content.value.trim(),
                pinned = _pinned.value,
                color = _color.value,
                tags = _tags.value.joinToString(",").ifBlank { null },
                checklist = if (cleanChecklist.isEmpty()) null else gson.toJson(cleanChecklist.map { mapOf("t" to it.text, "d" to it.done) })
            )
            runCatching { if (noteId > 0L) service.update(noteId, dto) else service.create(dto) }
            _saving.value = false
            onDone()
        }
    }

    /** Pide a la IA un resumen + checklist del contenido actual. */
    fun resumir() {
        if (_summarizing.value) return
        val texto = _content.value.replace(Regex("(?s)<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
        if (texto.length < 20) { _summaryError.value = "Escribí un poco más antes de resumir."; return }
        viewModelScope.launch {
            _summarizing.value = true
            _summaryError.value = null
            val res = runCatching {
                iaExtras.resumenApunte(ResumenApunteRequest(titulo = _title.value.trim(), contenido = _content.value))
            }.getOrNull()
            val body = res?.body()
            if (res?.isSuccessful == true && body != null && body.generadoPor != "error" &&
                (body.resumen.isNotBlank() || body.checklist.isNotEmpty())
            ) {
                _summary.value = body
            } else {
                _summaryError.value = "La IA no está disponible ahora. Probá de nuevo en un momento."
            }
            _summarizing.value = false
        }
    }

    /** Agrega los puntos del checklist sugerido al checklist del apunte. */
    fun agregarChecklistSugerido() {
        val puntos = _summary.value?.checklist ?: return
        val existentes = _checklist.value.map { it.text.trim().lowercase() }.toSet()
        val nuevos = puntos
            .map { it.trim() }
            .filter { it.isNotBlank() && it.lowercase() !in existentes }
            .map { ChecklistItem(text = it, done = false) }
        if (nuevos.isNotEmpty()) _checklist.value = _checklist.value + nuevos
        _summary.value = null
    }

    fun cerrarResumen() { _summary.value = null }
    fun limpiarErrorResumen() { _summaryError.value = null }

    private data class ChkDto(val t: String = "", val d: Boolean = false)

    private fun parseChecklist(json: String?): List<ChecklistItem> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = gson.fromJson(json, Array<ChkDto>::class.java) ?: return emptyList()
            arr.map { ChecklistItem(text = it.t, done = it.d) }
        }.getOrDefault(emptyList())
    }
}
