package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.data.remote.api.AuthService
import cr.una.delta.frontend_kode.data.remote.api.CourseRubricService
import cr.una.delta.frontend_kode.data.remote.api.CourseService
import cr.una.delta.frontend_kode.data.remote.api.RubricGradeService
import cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto
import cr.una.delta.frontend_kode.data.remote.dto.StudentRubricGradeDto
import cr.una.delta.frontend_kode.domain.model.Enrollment
import cr.una.delta.frontend_kode.domain.repository.EnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Un estudiante matriculado, con su nota acumulada actual (si tiene notas). */
data class StudentRow(
    val studentId: Long,
    val name: String,
    val grade: Double? = null,      // puntos acumulados de 100 (null = sin notas)
    val gradedWeight: Double = 0.0  // % del curso ya calificado
)

/** Un usuario del sistema, para buscar y matricular en el curso. */
data class UserRow(
    val id: Long,
    val name: String,
    val email: String
)

/**
 * Vista de un curso para el profesor: rúbricas (crear/borrar), estudiantes
 * matriculados y las notas de cada estudiante por rúbrica.
 */
@HiltViewModel
class TeacherCourseViewModel @Inject constructor(
    private val rubricService: CourseRubricService,
    private val enrollmentRepo: EnrollmentRepository,
    private val authService: AuthService,
    private val gradeService: RubricGradeService,
    private val courseService: CourseService
) : ViewModel() {

    // Código del grupo del curso (para que el profe lo comparta).
    private val _joinCode = MutableStateFlow<String?>(null)
    val joinCode: StateFlow<String?> = _joinCode

    private val _rubrics = MutableStateFlow<List<CourseRubricDto>>(emptyList())
    val rubrics: StateFlow<List<CourseRubricDto>> = _rubrics

    private val _students = MutableStateFlow<List<StudentRow>>(emptyList())
    val students: StateFlow<List<StudentRow>> = _students

    // Todos los usuarios del sistema (para el buscador de "Agregar estudiante").
    private val _allUsers = MutableStateFlow<List<UserRow>>(emptyList())
    val allUsers: StateFlow<List<UserRow>> = _allUsers

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ----- Diálogo de notas de un estudiante -----
    private val _selectedStudent = MutableStateFlow<StudentRow?>(null)
    val selectedStudent: StateFlow<StudentRow?> = _selectedStudent

    // rubricId -> texto de la nota
    private val _studentGrades = MutableStateFlow<Map<Long, String>>(emptyMap())
    val studentGrades: StateFlow<Map<Long, String>> = _studentGrades

    private var courseId: Long = 0L

    fun load(courseId: Long) {
        this.courseId = courseId
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Código del grupo (para compartir); si falla, queda null.
                _joinCode.value = runCatching { courseService.getJoinCode(courseId).body()?.joinCode }.getOrNull()

                _rubrics.value = runCatching { rubricService.getCourseRubrics(courseId).body() }
                    .getOrNull().orEmpty()

                val enrolls = enrollmentRepo.getAll().getOrDefault(emptyList())
                    .filter { it.courseId == courseId }
                val users = runCatching { authService.getAllUsers().body() }.getOrNull().orEmpty()
                _allUsers.value = users
                    .mapNotNull { u -> u.id?.let { UserRow(it, u.name, u.email) } }
                    .sortedBy { it.name.lowercase() }
                val nameById = users.mapNotNull { u -> u.id?.let { it to u.name } }.toMap()
                val basic = enrolls
                    .map { StudentRow(it.studentId, nameById[it.studentId] ?: "Estudiante #${it.studentId}") }
                    .sortedBy { it.name.lowercase() }
                _students.value = basic
                _loading.value = false

                // Nota acumulada de cada estudiante (en segundo plano; los nombres ya se ven).
                val rubrics = _rubrics.value
                val withGrades = basic.map { s ->
                    val gmap = runCatching { gradeService.getByStudent(s.studentId).body() }.getOrNull().orEmpty()
                        .mapNotNull { g -> g.grade?.let { g.rubricId to it } }.toMap()
                    computeRow(s, rubrics, gmap)
                }
                _students.value = withGrades
            } catch (e: Exception) {
                _error.value = e.message ?: "No se pudo cargar el curso"
                _loading.value = false
            }
        }
    }

    private fun computeRow(s: StudentRow, rubrics: List<CourseRubricDto>, grades: Map<Long, Double>): StudentRow {
        val graded = rubrics.filter { grades.containsKey(it.rubricId) }
        val accumulated = graded.sumOf { (grades[it.rubricId]!! / 100.0) * it.weightPercentage }
        val gradedWeight = graded.sumOf { it.weightPercentage }
        return s.copy(grade = if (graded.isEmpty()) null else accumulated, gradedWeight = gradedWeight)
    }

    fun addRubric(name: String, weight: Double, dueDate: String?) {
        val n = name.trim()
        if (n.isBlank()) return
        viewModelScope.launch {
            runCatching {
                rubricService.createRubric(
                    CourseRubricDto(rubricId = 0L, courseId = courseId, rubricName = n, weightPercentage = weight, dueDate = dueDate)
                )
            }
            _rubrics.value = runCatching { rubricService.getCourseRubrics(courseId).body() }.getOrNull().orEmpty()
        }
    }

    /** Edita una rúbrica existente (nombre, peso y/o fecha) y recarga. */
    fun updateRubric(rubricId: Long, name: String, weight: Double, dueDate: String?) {
        val n = name.trim()
        if (n.isBlank()) return
        viewModelScope.launch {
            runCatching {
                rubricService.updateRubric(
                    rubricId,
                    CourseRubricDto(rubricId = rubricId, courseId = courseId, rubricName = n, weightPercentage = weight, dueDate = dueDate)
                )
            }.onFailure { _error.value = it.message ?: "No se pudo editar la rúbrica" }
            _rubrics.value = runCatching { rubricService.getCourseRubrics(courseId).body() }.getOrNull().orEmpty()
        }
    }

    /** Matricula a un estudiante (por id) en este curso y recarga la lista. */
    fun enrollStudent(studentId: Long) {
        if (courseId == 0L) return
        // Evita duplicados: si ya está matriculado, no hace nada.
        if (_students.value.any { it.studentId == studentId }) return
        viewModelScope.launch {
            enrollmentRepo.create(Enrollment(courseId = courseId, studentId = studentId))
                .onSuccess { load(courseId) }
                .onFailure { _error.value = it.message ?: "No se pudo matricular al estudiante" }
        }
    }

    fun deleteRubric(rubricId: Long) {
        viewModelScope.launch {
            runCatching { rubricService.deleteRubric(rubricId) }
            _rubrics.value = _rubrics.value.filterNot { it.rubricId == rubricId }
        }
    }

    /** Abre el diálogo de notas de un estudiante y carga sus notas actuales. */
    fun openStudent(student: StudentRow) {
        _selectedStudent.value = student
        _studentGrades.value = emptyMap()
        viewModelScope.launch {
            val list = runCatching { gradeService.getByStudent(student.studentId).body() }.getOrNull().orEmpty()
            _studentGrades.value = list
                .filter { it.grade != null }
                .associate { it.rubricId to formatGrade(it.grade) }
        }
    }

    fun closeStudent() {
        val s = _selectedStudent.value
        if (s != null) {
            val gmap = _studentGrades.value
                .mapNotNull { (rid, txt) -> txt.trim().replace(',', '.').toDoubleOrNull()?.let { rid to it } }
                .toMap()
            _students.value = _students.value.map { if (it.studentId == s.studentId) computeRow(it, _rubrics.value, gmap) else it }
        }
        _selectedStudent.value = null
        _studentGrades.value = emptyMap()
    }

    /** Guarda (o borra) la nota de una rúbrica para el estudiante seleccionado. */
    fun setGrade(rubricId: Long, text: String) {
        val student = _selectedStudent.value ?: return
        _studentGrades.value = _studentGrades.value.toMutableMap().apply { put(rubricId, text) }
        val value = text.trim().replace(',', '.').toDoubleOrNull()
        viewModelScope.launch {
            if (value == null) {
                runCatching { gradeService.delete(student.studentId, rubricId) }
            } else {
                runCatching {
                    gradeService.upsert(
                        StudentRubricGradeDto(id = 0L, studentId = student.studentId, rubricId = rubricId, grade = value.coerceIn(0.0, 100.0))
                    )
                }
            }
        }
    }

    private fun formatGrade(g: Double?): String {
        if (g == null) return ""
        return if (g % 1.0 == 0.0) g.toInt().toString() else g.toString()
    }
}
