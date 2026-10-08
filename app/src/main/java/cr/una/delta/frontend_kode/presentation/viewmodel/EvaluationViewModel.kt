package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.data.remote.dto.StudentRubricGradeDto
import cr.una.delta.frontend_kode.domain.model.Assignment
import cr.una.delta.frontend_kode.domain.model.CourseRubric
import cr.una.delta.frontend_kode.domain.repository.AuthRepository
import cr.una.delta.frontend_kode.domain.repository.EvaluationRepository
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

sealed class EvaluationState {
    data object Loading : EvaluationState()
    data object Empty : EvaluationState()
    data class Success(
        val rubrics: List<CourseRubric>,
        val assignments: List<Assignment>
    ) : EvaluationState()
    data class Error(val message: String) : EvaluationState()
}

/** Rúbrica leída de una foto, editable antes de importar. Peso y nota como texto. */
data class ScannedRubric(val name: String, val weight: String, val grade: String)

@HiltViewModel
class EvaluationViewModel @Inject constructor(
    private val repository: EvaluationRepository,
    private val courseRubricService: cr.una.delta.frontend_kode.data.remote.api.CourseRubricService,
    private val rubricGradeService: cr.una.delta.frontend_kode.data.remote.api.RubricGradeService,
    private val imageService: cr.una.delta.frontend_kode.data.remote.api.ImageProcessingService,
    private val iaExtras: cr.una.delta.frontend_kode.data.remote.api.IaExtrasService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val gson = com.google.gson.Gson()

    private val _state = MutableStateFlow<EvaluationState>(EvaluationState.Loading)
    val state: StateFlow<EvaluationState> = _state

    // ----- Consejo de la IA en la calculadora de notas -----
    private val _consejo = MutableStateFlow<String?>(null)
    val consejo: StateFlow<String?> = _consejo
    private val _consejoLoading = MutableStateFlow(false)
    val consejoLoading: StateFlow<Boolean> = _consejoLoading

    // Notas por rubricId (texto "0-100"). Se cargan del backend y se guardan allí.
    private val _grades = MutableStateFlow<Map<Long, String>>(emptyMap())
    val grades: StateFlow<Map<Long, String>> = _grades

    // ----- Escaneo de rúbrica por foto -----
    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning
    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError
    // null = no hay revisión pendiente; lista = rúbricas leídas para revisar/editar.
    private val _scanResult = MutableStateFlow<List<ScannedRubric>?>(null)
    val scanResult: StateFlow<List<ScannedRubric>?> = _scanResult

    private var currentCourseId: Long = 0
    private var studentId: Long? = null

    fun loadEvaluations(courseId: Long) {
        currentCourseId = courseId
        viewModelScope.launch {
            _state.value = EvaluationState.Loading

            // Resiliente: si una llamada falla, mostramos lo que sí cargó.
            val rubrics = runCatching {
                repository.getCourseRubrics(courseId).getOrDefault(emptyList())
            }.getOrDefault(emptyList())

            val assignments = runCatching {
                repository.getCourseAssignments(courseId).getOrDefault(emptyList())
            }.getOrDefault(emptyList())

            // Notas guardadas del estudiante (backend).
            loadGrades()

            _state.value = if (rubrics.isEmpty() && assignments.isEmpty()) {
                EvaluationState.Empty
            } else {
                EvaluationState.Success(rubrics, assignments)
            }
        }
    }

    private suspend fun loadGrades() {
        val sid = studentId ?: authRepository.getCurrentUser().getOrNull()
            ?.id?.toString()?.toLongOrNull()
        studentId = sid
        if (sid == null) return

        runCatching {
            val resp = rubricGradeService.getByStudent(sid)
            resp.body().orEmpty()
        }.onSuccess { list ->
            _grades.value = list.associate { it.rubricId to formatGrade(it.grade) }
        }
    }

    /** Se llama al escribir una nota: actualiza en pantalla al instante y guarda en el backend. */
    fun onGradeChanged(rubricId: Long, text: String) {
        // Optimista: refleja el cambio de inmediato.
        _grades.value = _grades.value.toMutableMap().apply { put(rubricId, text) }

        viewModelScope.launch {
            val sid = studentId ?: authRepository.getCurrentUser().getOrNull()
                ?.id?.toString()?.toLongOrNull() ?: return@launch
            studentId = sid

            val value = text.toDoubleOrNull()
            runCatching {
                if (text.isBlank() || value == null) {
                    rubricGradeService.delete(sid, rubricId)
                } else {
                    rubricGradeService.upsert(
                        StudentRubricGradeDto(id = 0L, studentId = sid, rubricId = rubricId, grade = value)
                    )
                }
            }
        }
    }

    /** Agrega una rúbrica al curso (a mano, con fecha opcional) y recarga las evaluaciones. */
    fun addRubric(courseId: Long, name: String, weight: Double, dueDate: String? = null) {
        viewModelScope.launch {
            runCatching {
                courseRubricService.createRubric(
                    cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto(
                        rubricId = 0L,
                        courseId = courseId,
                        rubricName = name,
                        weightPercentage = weight,
                        dueDate = dueDate
                    )
                )
            }
            loadEvaluations(courseId)
        }
    }

    /** Pide a la IA una línea de consejo para la meta de notas. */
    fun pedirConsejo(
        curso: String,
        notaMeta: Double,
        notaActual: Double,
        pesoFaltante: Double,
        promedioNecesario: Double?,
        alcanzable: Boolean
    ) {
        if (_consejoLoading.value) return
        viewModelScope.launch {
            _consejoLoading.value = true
            _consejo.value = null
            val res = runCatching {
                iaExtras.consejoNotas(
                    cr.una.delta.frontend_kode.data.remote.dto.ConsejoNotasRequest(
                        curso = curso.ifBlank { null },
                        notaMeta = notaMeta,
                        notaActual = notaActual,
                        pesoFaltante = pesoFaltante,
                        promedioNecesario = promedioNecesario,
                        alcanzable = alcanzable
                    )
                )
            }.getOrNull()
            val body = res?.body()
            _consejo.value = if (res?.isSuccessful == true && body != null &&
                body.generadoPor != "error" && body.consejo.isNotBlank()
            ) body.consejo else "La IA no está disponible ahora. Probá de nuevo en un momento."
            _consejoLoading.value = false
        }
    }

    fun uploadImage(imageFile: File) {
        viewModelScope.launch {
            _state.value = EvaluationState.Loading

            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            val courseIdBody = currentCourseId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            repository.uploadEvaluationImage(body, courseIdBody).fold(
                onSuccess = { loadEvaluations(currentCourseId) },
                onFailure = { error ->
                    _state.value = EvaluationState.Error(error.message ?: "Error al procesar imagen")
                }
            )
        }
    }

    // =====================================================
    // Escanear rúbrica desde una foto (OCR con Gemini)
    // =====================================================
    private data class ScanDto(val nombre: String? = null, val peso: Double? = null, val nota: Double? = null)

    fun scanRubric(file: File) {
        viewModelScope.launch {
            _scanning.value = true
            _scanError.value = null
            try {
                val req = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, req)
                val prompt = (
                    "Esta imagen es una rúbrica o tabla de evaluación de un curso. " +
                    "Devolvé SOLO un arreglo JSON válido, sin texto extra ni bloques de código, " +
                    "con un objeto por evaluación así: " +
                    "[{\"nombre\":\"Examen 1\",\"peso\":30,\"nota\":85}]. " +
                    "'peso' es el porcentaje (número). 'nota' es la nota obtenida de 0 a 100 si aparece, o null si no."
                ).toRequestBody("text/plain".toMediaTypeOrNull())

                val res = imageService.analyze(part, prompt)
                val raw = res.body()?.text
                if (!res.isSuccessful || raw.isNullOrBlank()) {
                    _scanError.value = res.body()?.error ?: "No se pudo leer la imagen (código ${res.code()})"
                } else {
                    val parsed = parseScan(raw)
                    if (parsed.isEmpty()) _scanError.value = "No se reconocieron rúbricas en la foto."
                    else _scanResult.value = parsed
                }
            } catch (e: Exception) {
                _scanError.value = e.message ?: "Error al procesar la imagen"
            }
            _scanning.value = false
        }
    }

    private fun parseScan(text: String): List<ScannedRubric> {
        // Gemini a veces envuelve en ```json ... ``` o agrega texto; extraemos el arreglo.
        val start = text.indexOf('[')
        val end = text.lastIndexOf(']')
        if (start < 0 || end <= start) return emptyList()
        val json = text.substring(start, end + 1)
        return runCatching {
            gson.fromJson(json, Array<ScanDto>::class.java).orEmpty()
                .filter { !it.nombre.isNullOrBlank() }
                .map {
                    ScannedRubric(
                        name = it.nombre!!.trim(),
                        weight = it.peso?.let { p -> formatGrade(p) } ?: "",
                        grade = it.nota?.let { n -> formatGrade(n) } ?: ""
                    )
                }
        }.getOrDefault(emptyList())
    }

    fun setScannedName(i: Int, v: String) = updateScanned(i) { it.copy(name = v) }
    fun setScannedWeight(i: Int, v: String) = updateScanned(i) { it.copy(weight = v) }
    fun setScannedGrade(i: Int, v: String) = updateScanned(i) { it.copy(grade = v) }
    fun removeScanned(i: Int) {
        _scanResult.value = _scanResult.value?.toMutableList()?.also { if (i in it.indices) it.removeAt(i) }
    }
    private fun updateScanned(i: Int, f: (ScannedRubric) -> ScannedRubric) {
        _scanResult.value = _scanResult.value?.toMutableList()?.also { if (i in it.indices) it[i] = f(it[i]) }
    }

    fun cancelScan() { _scanResult.value = null; _scanError.value = null }

    /** Crea las rúbricas leídas en el curso y guarda las notas del estudiante. */
    fun applyScan(courseId: Long) {
        val list = _scanResult.value ?: return
        viewModelScope.launch {
            _scanResult.value = null
            _state.value = EvaluationState.Loading
            val sid = studentId ?: authRepository.getCurrentUser().getOrNull()?.id?.toString()?.toLongOrNull()
            studentId = sid
            list.forEach { r ->
                val name = r.name.trim()
                val weight = r.weight.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                if (name.isBlank()) return@forEach
                val created = runCatching {
                    courseRubricService.createRubric(
                        cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto(
                            rubricId = 0L, courseId = courseId, rubricName = name, weightPercentage = weight, dueDate = null
                        )
                    ).body()
                }.getOrNull()
                val rubricId = created?.rubricId
                val grade = r.grade.trim().replace(',', '.').toDoubleOrNull()
                if (rubricId != null && rubricId > 0L && grade != null) {
                    runCatching {
                        rubricGradeService.upsert(StudentRubricGradeDto(id = 0L, studentId = sid ?: 0L, rubricId = rubricId, grade = grade.coerceIn(0.0, 100.0)))
                    }
                }
            }
            loadEvaluations(courseId)
        }
    }

    /** 85.0 -> "85" ; 85.5 -> "85.5" ; null -> "". */
    private fun formatGrade(g: Double?): String = when {
        g == null -> ""
        g % 1.0 == 0.0 -> g.toInt().toString()
        else -> g.toString()
    }
}
