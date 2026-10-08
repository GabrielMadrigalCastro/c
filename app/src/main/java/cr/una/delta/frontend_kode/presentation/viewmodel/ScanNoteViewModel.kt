package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.data.remote.api.ApunteService
import cr.una.delta.frontend_kode.data.remote.api.ImageProcessingService
import cr.una.delta.frontend_kode.data.remote.dto.ApunteDto
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Escanea una imagen con Gemini (OCR) y deja el texto editable para
 * guardarlo como apunte. El texto se puede editar antes y (reabriendo el
 * apunte) también después de guardarlo.
 */
@HiltViewModel
class ScanNoteViewModel @Inject constructor(
    private val imageService: ImageProcessingService,
    private val apunteService: ApunteService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text
    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _hasScanned = MutableStateFlow(false)
    val hasScanned: StateFlow<Boolean> = _hasScanned

    fun onTitle(v: String) { _title.value = v }
    fun onText(v: String) { _text.value = v }
    fun setError(msg: String?) { _error.value = msg }

    fun scan(file: File) {
        viewModelScope.launch {
            _scanning.value = true
            _error.value = null
            try {
                val req = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, req)
                val prompt = ("Transcribí exactamente el texto que aparece en esta imagen, " +
                        "respetando los saltos de línea. No agregues comentarios ni explicaciones.")
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val res = imageService.analyze(part, prompt)
                val body = res.body()
                if (res.isSuccessful && !body?.text.isNullOrBlank()) {
                    _text.value = body!!.text!!.trim()
                    _hasScanned.value = true
                } else {
                    _error.value = body?.error ?: "No se pudo leer la imagen (código ${res.code()})"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al procesar la imagen"
            }
            _scanning.value = false
        }
    }

    fun save(courseId: Long, onDone: () -> Unit) {
        if (_text.value.isBlank() || _saving.value) return
        viewModelScope.launch {
            _saving.value = true
            val sid = authRepository.getCurrentUser().getOrNull()?.id
            if (sid == null) {
                _error.value = "Iniciá sesión primero."
                _saving.value = false
                return@launch
            }
            val title = _title.value.trim().ifBlank { "Escaneo" }
            runCatching {
                apunteService.create(
                    ApunteDto(id = 0L, studentId = sid, courseId = courseId, title = title, content = _text.value.trim())
                )
            }.onSuccess { _saving.value = false; onDone() }
                .onFailure { _saving.value = false; _error.value = it.message ?: "No se pudo guardar el apunte" }
        }
    }
}
