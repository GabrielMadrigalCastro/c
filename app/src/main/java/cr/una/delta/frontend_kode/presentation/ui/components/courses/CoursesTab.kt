package cr.una.delta.frontend_kode.presentation.ui.components.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cr.una.delta.frontend_kode.data.local.SessionManager
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.presentation.viewmodel.CourseViewModel
import cr.una.delta.frontend_kode.presentation.viewmodel.EnrollmentViewModel
import kotlinx.coroutines.launch

@Composable
fun CoursesTab(
    courses: List<Course>,
    onCourseClick: (Course) -> Unit,
    onEditCourse: (Course) -> Unit = {},
    onApuntes: (Course) -> Unit = {},
    onAddEnrollment: () -> Unit = {},
    viewModel: CourseViewModel = hiltViewModel(),
    enrollmentViewModel: EnrollmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showJoin by remember { mutableStateOf(false) }
    var codeText by remember { mutableStateOf("") }
    var joinMsg by remember { mutableStateOf<String?>(null) }
    var joining by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Mis cursos matriculados",
            style = MaterialTheme.typography.titleLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(onClick = { showJoin = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Unirse por código")
            }
            OutlinedButton(onClick = onAddEnrollment, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Matricular")
            }
        }

        if (showJoin) {
            AlertDialog(
                onDismissRequest = { if (!joining) showJoin = false },
                title = { Text("Unirse a un curso") },
                text = {
                    Column {
                        Text(
                            "Pedile el código del grupo al profesor y pegalo acá.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = codeText,
                            onValueChange = { codeText = it.uppercase().trim() },
                            label = { Text("Código del grupo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        joinMsg?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                confirmButton = {
                    TextButton(enabled = codeText.isNotBlank() && !joining, onClick = {
                        joining = true; joinMsg = null
                        scope.launch {
                            val sid = SessionManager(context).getUser()?.id
                            if (sid == null) { joinMsg = "Iniciá sesión primero."; joining = false; return@launch }
                            enrollmentViewModel.joinByCode(sid, codeText) { ok, msg ->
                                joining = false
                                if (ok) {
                                    showJoin = false; codeText = ""; joinMsg = null
                                    viewModel.loadCourses(sid, force = true)
                                } else {
                                    joinMsg = msg
                                }
                            }
                        }
                    }) { Text(if (joining) "Uniéndote…" else "Unirme") }
                },
                dismissButton = { TextButton(onClick = { showJoin = false }, enabled = !joining) { Text("Cancelar") } }
            )
        }

        courses.forEach { course ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onCourseClick(course) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .background(parseCourseColor(course.courseColor), CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(course.courseName, fontWeight = FontWeight.Bold)
                        Text(
                            "Toca para ver notas y rúbricas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Apuntes") },
                                onClick = { expanded = false; onApuntes(course) }
                            )
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = { expanded = false; onEditCourse(course) }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                onClick = { expanded = false; viewModel.deleteCourse(course.courseId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Convierte el color guardado (#RRGGBB) a Color; usa un lila por defecto si falla. */
private fun parseCourseColor(hex: String?): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex ?: "#A5C8FF")) }
        .getOrDefault(Color(0xFFA5C8FF))
