package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.data.remote.dto.ApunteDto
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.presentation.viewmodel.ApuntesHomeState
import cr.una.delta.frontend_kode.presentation.viewmodel.ApuntesHomeViewModel

@Composable
fun ApuntesHomeScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: ApuntesHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (val s = state) {
            is ApuntesHomeState.Loading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

            is ApuntesHomeState.Error ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                }

            is ApuntesHomeState.Ready -> {
                val q = query.trim().lowercase()
                fun matches(n: ApunteDto) = q.isNotBlank() &&
                        (n.title.lowercase().contains(q) || n.plainText.lowercase().contains(q) ||
                                n.tagList.any { it.lowercase().contains(q) })
                val results = if (q.isBlank()) emptyList() else s.apuntes.filter { matches(it) }
                val pinned = s.apuntes.filter { it.pinned }
                val personalCount = s.apuntes.count { it.courseId == null }
                // courseId null = apunte personal (fuera de clases).
                fun courseName(id: Long?) = if (id == null) "Personal" else s.courses.find { it.courseId == id }?.courseName ?: "Curso"

                // Crea un apunte a mano (sin IA). Si hay varios cursos, se elige antes.
                fun nuevoApunte(course: Course) {
                    navController.navigate("note_editor/${course.courseId}/0")
                }
                // Apunte personal nuevo (curso 0 = sin curso).
                fun nuevoPersonal() { navController.navigate("note_editor/0/0") }
                // Lista de apuntes personales.
                fun abrirPersonal() { navController.navigate("apuntes/0/${android.net.Uri.encode("Personal")}") }

                Box(Modifier.fillMaxSize()) {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    item {
                        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp)) {
                            Text("Apuntes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "${s.apuntes.size} ${if (s.apuntes.size == 1) "apunte" else "apuntes"} en ${s.courses.size} ${if (s.courses.size == 1) "curso" else "cursos"}",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    item {
                        SearchField(
                            query = query, onQuery = { query = it }, onClear = { query = "" },
                            placeholder = "Buscar en todos los apuntes",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }

                    if (q.isNotBlank()) {
                        // Resultados de búsqueda
                        item {
                            SectionLabel(
                                "${results.size} ${if (results.size == 1) "resultado" else "resultados"}",
                                Modifier.padding(start = 22.dp, bottom = 6.dp)
                            )
                        }
                        if (results.isEmpty()) {
                            item { EmptySearch() }
                        } else {
                            items(results) { note ->
                                NoteResultRow(
                                    note = note, courseName = courseName(note.courseId),
                                    onClick = { navController.navigate("note_editor/${note.courseId ?: 0L}/${note.id ?: 0L}") }
                                )
                            }
                        }
                    } else {
                        // Fijadas
                        if (pinned.isNotEmpty()) {
                            item { SectionLabel("Fijadas", Modifier.padding(start = 22.dp, top = 4.dp, bottom = 8.dp)) }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(pinned) { note ->
                                        PinnedCard(
                                            note = note, courseName = courseName(note.courseId),
                                            onClick = { navController.navigate("note_editor/${note.courseId ?: 0L}/${note.id ?: 0L}") }
                                        )
                                    }
                                }
                            }
                        }
                        // Personal (fuera de clases)
                        item { SectionLabel("Personal", Modifier.padding(start = 22.dp, top = 14.dp, bottom = 8.dp)) }
                        item {
                            PersonalNotesCard(
                                count = personalCount,
                                onClick = { abrirPersonal() }
                            )
                        }

                        // Cursos
                        item { SectionLabel("Cursos", Modifier.padding(start = 22.dp, top = 14.dp, bottom = 8.dp)) }
                        if (s.courses.isEmpty()) {
                            item {
                                Text(
                                    "Matriculate en un curso para tomar apuntes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        } else {
                            items(s.courses) { course ->
                                val count = s.apuntes.count { it.courseId == course.courseId }
                                val fixed = s.apuntes.count { it.courseId == course.courseId && it.pinned }
                                CourseNotesCard(
                                    course = course, count = count, pinnedCount = fixed,
                                    onClick = { navController.navigate("apuntes/${course.courseId}/${android.net.Uri.encode(course.courseName)}") }
                                )
                            }
                        }
                    }
                }

                // Botón principal: escribir un apunte a mano (no depende de la IA).
                NewNoteFab(
                    courses = s.courses,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                    onPickCourse = { nuevoApunte(it) },
                    onPersonal = { nuevoPersonal() }
                )
                }
            }
        }
    }
}

@Composable
private fun NewNoteFab(
    courses: List<Course>,
    modifier: Modifier = Modifier,
    onPickCourse: (Course) -> Unit,
    onPersonal: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    ExtendedFloatingActionButton(
        // Sin cursos: va directo al apunte personal. Con cursos: se elige dónde.
        onClick = { if (courses.isEmpty()) onPersonal() else showPicker = true },
        icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
        text = { Text("Nuevo apunte") },
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
    )

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("¿Dónde lo guardás?") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            "Personal (fuera de clases)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPicker = false; onPersonal() }
                                .padding(vertical = 14.dp)
                        )
                    }
                    items(courses) { course ->
                        Text(
                            course.courseName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPicker = false; onPickCourse(course) }
                                .padding(vertical = 14.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun PersonalNotesCard(count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(19.dp)) }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text("Mis cosas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                if (count == 0) "Notas y pendientes que no van para la U"
                else "${if (count == 1) "1 apunte" else "$count apuntes"} · fuera de clases",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit, onClear: () -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            TextField(
                value = query, onValueChange = onQuery,
                placeholder = { Text(placeholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            if (query.isNotEmpty()) {
                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onClear() })
            }
        }
    }
}

@Composable
private fun NoteResultRow(note: ApunteDto, courseName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(parse(note.color) ?: MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Outlined.Description, contentDescription = null, tint = Color(0xFF2A2733), modifier = Modifier.size(17.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(note.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (note.plainText.isNotBlank()) {
                Text(note.plainText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(courseName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun PinnedCard(note: ApunteDto, courseName: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(172.dp),
        shape = RoundedCornerShape(16.dp),
        color = parse(note.color) ?: MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Column(Modifier.padding(13.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PushPin, contentDescription = null, tint = Color(0xFF2A2733), modifier = Modifier.size(15.dp))
                Text(note.createdAt?.take(10) ?: "", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2A2733).copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(6.dp))
            Text(note.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2A2733), maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (note.plainText.isNotBlank()) {
                Text(note.plainText, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2A2733).copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
            }
            Text(courseName, style = MaterialTheme.typography.labelSmall, color = Color(0xFF2A2733).copy(alpha = 0.7f), modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun CourseNotesCard(course: Course, count: Int, pinnedCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(16.dp).clip(CircleShape).background(parse(course.courseColor) ?: Color(0xFFA5C8FF)))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                buildString {
                    append(if (count == 1) "1 apunte" else "$count apuntes")
                    if (pinnedCount > 0) append(" · $pinnedCount ${if (pinnedCount == 1) "fijada" else "fijadas"}")
                },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptySearch() {
    Column(
        Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔎", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text("Sin resultados", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Probá con otra palabra.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

private fun parse(hex: String?): Color? =
    hex?.takeIf { it.isNotBlank() }?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
