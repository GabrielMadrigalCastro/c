package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cr.una.delta.frontend_kode.presentation.viewmodel.TeacherCoursesState
import cr.una.delta.frontend_kode.presentation.viewmodel.TeacherCoursesViewModel

@Composable
fun TeacherCoursesScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    viewModel: TeacherCoursesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    // Curso pendiente de confirmar borrado (id, nombre).
    var toDelete by remember { mutableStateOf<Pair<Long, String>?>(null) }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("create_course") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Crear curso") },
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp)) {
                Text("Mis cursos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Cursos que impartís", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            when (val s = state) {
                is TeacherCoursesState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is TeacherCoursesState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text(s.message, color = MaterialTheme.colorScheme.error) }
                is TeacherCoursesState.Empty -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Todavía no tenés cursos.\nTocá \"Crear curso\".", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is TeacherCoursesState.Ready -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(s.courses) { course ->
                        val color = runCatching { Color(android.graphics.Color.parseColor(course.courseColor ?: "#A5C8FF")) }.getOrDefault(MaterialTheme.colorScheme.primary)
                        Surface(
                            onClick = { navController.navigate("teacher_course/${course.courseId}/${android.net.Uri.encode(course.courseName)}") },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(14.dp).clip(CircleShape).background(color))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    course.courseCode?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                // Menú Editar / Eliminar
                                var menu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { menu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                        DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                            menu = false
                                            val c = android.net.Uri.encode(course.courseColor ?: "#A5C8FF")
                                            navController.navigate("edit_course/${course.courseId}/${android.net.Uri.encode(course.courseName)}/$c")
                                        })
                                        DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                                            menu = false
                                            toDelete = course.courseId to course.courseName
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { (id, name) ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar curso") },
            text = { Text("¿Seguro que querés eliminar \"$name\"? Se borra el curso y sus rúbricas.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCourse(id); toDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}
