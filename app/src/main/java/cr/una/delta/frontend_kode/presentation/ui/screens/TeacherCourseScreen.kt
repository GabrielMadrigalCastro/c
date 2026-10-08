package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cr.una.delta.frontend_kode.data.remote.dto.CourseRubricDto
import cr.una.delta.frontend_kode.presentation.viewmodel.StudentRow
import cr.una.delta.frontend_kode.presentation.viewmodel.TeacherCourseViewModel
import cr.una.delta.frontend_kode.presentation.viewmodel.UserRow
import kotlin.math.roundToInt

@Composable
fun TeacherCourseScreen(
    navController: NavController,
    courseId: Long,
    courseName: String,
    paddingValues: PaddingValues,
    viewModel: TeacherCourseViewModel = hiltViewModel()
) {
    val rubrics by viewModel.rubrics.collectAsState()
    val students by viewModel.students.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val studentGrades by viewModel.studentGrades.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val joinCode by viewModel.joinCode.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(courseId) { viewModel.load(courseId) }
    var showAddRubric by remember { mutableStateOf(false) }
    var showAddStudent by remember { mutableStateOf(false) }
    var editingRubric by remember { mutableStateOf<CourseRubricDto?>(null) }

    Column(Modifier.fillMaxSize().padding(paddingValues)) {
        // Encabezado
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
            Column {
                Text("Curso", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(courseName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Código del grupo: el profe lo comparte para que los alumnos se unan.
        joinCode?.let { code ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Código del grupo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = {
                        val texto = "📚 Unite a mi curso \"$courseName\" en KODE.\n\nCódigo: $code\n\nAbrí KODE → Cursos → Unirse por código, y pegá el código. O tocá: kode://join/$code"
                        val envio = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, texto)
                        }
                        context.startActivity(android.content.Intent.createChooser(envio, "Compartir invitación"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Compartir")
                    }
                }
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ---- Rúbricas ----
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Rúbricas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showAddRubric = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Agregar")
                        }
                    }
                }
                if (rubrics.isEmpty()) {
                    item { Text("Todavía no hay rúbricas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(rubrics) { r ->
                        RubricRow(
                            r = r,
                            onEdit = { editingRubric = r },
                            onDelete = { viewModel.deleteRubric(r.rubricId) }
                        )
                    }
                    item {
                        val total = rubrics.sumOf { it.weightPercentage }
                        Text("Suma de pesos: ${total.toInt()}%", style = MaterialTheme.typography.labelSmall, color = if (total == 100.0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
                    }
                }

                // ---- Estudiantes ----
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Estudiantes (${students.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showAddStudent = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Agregar")
                        }
                    }
                }
                // Resumen del curso: promedio y cuántos aprueban.
                if (students.isNotEmpty()) {
                    item {
                        val graded = students.mapNotNull { it.grade }
                        if (graded.isNotEmpty()) {
                            val avg = graded.average()
                            val passing = graded.count { it >= 70.0 }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text("Promedio del curso", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                        Text("${avg.roundToInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Text("$passing de ${graded.size} aprobando (≥70)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }
                if (students.isEmpty()) {
                    item { Text("Nadie matriculado todavía.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(students) { st ->
                        Surface(
                            onClick = { viewModel.openStudent(st) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(st.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                // Chip de nota actual
                                st.grade?.let { g ->
                                    val ok = g >= 70.0
                                    val bg = if (ok) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                                    val fg = if (ok) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    Surface(shape = RoundedCornerShape(999.dp), color = bg) {
                                        Text("${g.roundToInt()}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = fg, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                }
                                Text("Notas", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddRubric) {
        RubricDialog(
            title = "Nueva rúbrica",
            confirmLabel = "Agregar",
            onDismiss = { showAddRubric = false }
        ) { name, weight, date ->
            viewModel.addRubric(name, weight, date)
            showAddRubric = false
        }
    }

    editingRubric?.let { r ->
        RubricDialog(
            title = "Editar rúbrica",
            confirmLabel = "Guardar",
            initialName = r.rubricName,
            initialWeight = r.weightPercentage.toInt().toString(),
            initialDate = r.dueDate ?: "",
            onDismiss = { editingRubric = null }
        ) { name, weight, date ->
            viewModel.updateRubric(r.rubricId, name, weight, date)
            editingRubric = null
        }
    }

    if (showAddStudent) {
        val enrolledIds = students.map { it.studentId }.toSet()
        AddStudentDialog(
            candidates = allUsers.filter { it.id !in enrolledIds },
            onDismiss = { showAddStudent = false }
        ) { studentId ->
            viewModel.enrollStudent(studentId)
            showAddStudent = false
        }
    }

    selectedStudent?.let { st ->
        StudentGradesDialog(
            student = st,
            rubrics = rubrics,
            grades = studentGrades,
            onGrade = { rid, txt -> viewModel.setGrade(rid, txt) },
            onClose = { viewModel.closeStudent() }
        )
    }
}

@Composable
private fun RubricRow(r: CourseRubricDto, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        onClick = onEdit,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(r.rubricName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${r.weightPercentage.toInt()}%" + (r.dueDate?.let { " · $it" } ?: " · sin fecha"),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun RubricDialog(
    title: String,
    confirmLabel: String,
    initialName: String = "",
    initialWeight: String = "",
    initialDate: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var weight by remember { mutableStateOf(initialWeight) }
    var date by remember { mutableStateOf(initialDate) }
    val weightValue = weight.trim().replace(',', '.').toDoubleOrNull()
    // Fecha válida si está vacía (opcional) o cumple yyyy-MM-dd.
    val dateOk = date.isBlank() || runCatching { java.time.LocalDate.parse(date.trim()) }.isSuccess
    val valid = name.isNotBlank() && weightValue != null && weightValue in 0.0..100.0 && dateOk

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre (ej. Examen 1)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Peso %") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (opcional, yyyy-MM-dd)") },
                    isError = !dateOk,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!dateOk) {
                    Text("Formato de fecha inválido (usá yyyy-MM-dd).", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = { onConfirm(name.trim(), weightValue ?: 0.0, date.trim().ifBlank { null }) }) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AddStudentDialog(
    candidates: List<UserRow>,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()
    val filtered = if (q.isBlank()) candidates
    else candidates.filter { it.name.lowercase().contains(q) || it.email.lowercase().contains(q) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar estudiante") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar por nombre o correo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                when {
                    candidates.isEmpty() -> Text("No hay más usuarios para matricular.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    filtered.isEmpty() -> Text("Sin resultados para \"$query\".", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { u ->
                            Surface(
                                onClick = { onPick(u.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(u.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                    Text(u.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun StudentGradesDialog(
    student: StudentRow,
    rubrics: List<CourseRubricDto>,
    grades: Map<Long, String>,
    onGrade: (Long, String) -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(student.name) },
        text = {
            if (rubrics.isEmpty()) {
                Text("Este curso no tiene rúbricas. Agregá rúbricas primero.")
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Nota por rúbrica (0–100)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    rubrics.forEach { r ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(r.rubricName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${r.weightPercentage.toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(10.dp))
                            OutlinedTextField(
                                value = grades[r.rubricId] ?: "",
                                onValueChange = { onGrade(r.rubricId, it) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(88.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Listo") } }
    )
}
