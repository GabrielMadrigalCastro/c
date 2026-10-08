package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.data.remote.dto.ApunteDto
import cr.una.delta.frontend_kode.presentation.viewmodel.ApuntesState
import cr.una.delta.frontend_kode.presentation.viewmodel.ApuntesViewModel

private val NOTE_COLORS = listOf(
    Color(0xFFFFF3B0), Color(0xFFCDEAC0), Color(0xFFCFE3FF),
    Color(0xFFFFD6E0), Color(0xFFE7D3FF), Color(0xFFFFE0C2),
)
private val NOTE_INK = Color(0xFF2A2733)

@Composable
fun ApuntesScreen(
    navController: NavHostController,
    courseId: Long,
    courseName: String,
    paddingValues: PaddingValues,
    viewModel: ApuntesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load(courseId) }

    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todas") }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // "Escanear" (OCR con IA) solo aplica a apuntes de un curso, no a los personales.
                if (courseId > 0L) {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate("scan_note/$courseId/${android.net.Uri.encode(courseName)}") },
                        icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                        text = { Text("Escanear") },
                        shape = RoundedCornerShape(18.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("note_editor/$courseId/0") },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    text = { Text("Nuevo apunte") },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                Column {
                    Text("Apuntes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(courseName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            when (val s = state) {
                is ApuntesState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is ApuntesState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error) }
                is ApuntesState.Success -> {
                    val q = query.trim().lowercase()
                    val allTags = s.apuntes.flatMap { it.tagList }.distinct()
                    val filters = listOf("Todas", "Fijadas") + allTags

                    fun passSearch(n: ApunteDto) = q.isBlank() || n.title.lowercase().contains(q) || n.plainText.lowercase().contains(q) || n.tagList.any { it.lowercase().contains(q) }
                    fun passFilter(n: ApunteDto) = when (filter) {
                        "Todas" -> true
                        "Fijadas" -> n.pinned
                        else -> n.tagList.contains(filter)
                    }
                    val visibles = s.apuntes.filter { passSearch(it) && passFilter(it) }
                        .sortedByDescending { it.pinned }

                    // Búsqueda
                    SearchBox(query, { query = it }, { query = "" }, "Buscar en este curso", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                    // Filtros
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        items(filters) { f ->
                            FilterChip(
                                selected = filter == f,
                                onClick = { filter = f },
                                label = { Text(f) }
                            )
                        }
                    }

                    if (visibles.isEmpty()) {
                        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(24.dp))
                            Icon(
                                if (q.isNotBlank() || filter != "Todas") Icons.Default.SearchOff else Icons.Outlined.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (q.isNotBlank() || filter != "Todas") "Sin resultados" else "Todavía no hay apuntes.\nTocá \"Nuevo apunte\".",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp
                        ) {
                            items(visibles.size) { i ->
                                val note = visibles[i]
                                StickyNote(
                                    apunte = note,
                                    color = parse(note.color) ?: NOTE_COLORS[i % NOTE_COLORS.size],
                                    onClick = { navController.navigate("note_editor/$courseId/${note.id ?: 0L}") },
                                    onPin = { viewModel.togglePin(note) },
                                    onDelete = { note.id?.let { viewModel.delete(it) } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBox(query: String, onQuery: (String) -> Unit, onClear: () -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            TextField(
                value = query, onValueChange = onQuery,
                placeholder = { Text(placeholder) }, singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            if (query.isNotEmpty()) Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onClear() })
        }
    }
}

@Composable
private fun StickyNote(apunte: ApunteDto, color: Color, onClick: () -> Unit, onPin: () -> Unit, onDelete: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp),
        color = color,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(apunte.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NOTE_INK, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Outlined.PushPin, contentDescription = if (apunte.pinned) "Desfijar" else "Fijar",
                    tint = if (apunte.pinned) NOTE_INK else NOTE_INK.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(18.dp)
                        .then(if (apunte.pinned) Modifier else Modifier.rotate(45f))
                        .clickable { onPin() }
                )
            }
            if (apunte.plainText.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(apunte.plainText, style = MaterialTheme.typography.bodyMedium, color = NOTE_INK.copy(alpha = 0.85f), maxLines = 8, overflow = TextOverflow.Ellipsis)
            }
            // Checklist
            checklistDoneTotal(apunte.checklist)?.let { (done, total) ->
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckBox, contentDescription = null, tint = NOTE_INK.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("$done/$total completados", style = MaterialTheme.typography.labelSmall, color = NOTE_INK.copy(alpha = 0.75f))
                }
                Spacer(Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { if (total > 0) done.toFloat() / total else 0f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(99.dp)),
                    color = NOTE_INK.copy(alpha = 0.55f),
                    trackColor = NOTE_INK.copy(alpha = 0.16f)
                )
            }
            // Tags
            if (apunte.tagList.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    apunte.tagList.take(3).forEach { tag ->
                        Surface(shape = RoundedCornerShape(999.dp), color = Color.White.copy(alpha = 0.5f)) {
                            Text(tag, style = MaterialTheme.typography.labelSmall, color = NOTE_INK, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            apunte.createdAt?.take(10)?.let { date ->
                Spacer(Modifier.height(9.dp))
                Text(date, style = MaterialTheme.typography.labelSmall, color = NOTE_INK.copy(alpha = 0.6f))
            }
        }
    }
}

private data class ChkItem(val t: String = "", val d: Boolean = false)

private fun checklistDoneTotal(json: String?): Pair<Int, Int>? {
    if (json.isNullOrBlank()) return null
    return runCatching {
        val arr = com.google.gson.Gson().fromJson(json, Array<ChkItem>::class.java) ?: return null
        (arr.count { it.d } to arr.size).takeIf { it.second > 0 }
    }.getOrNull()
}

private fun parse(hex: String?): Color? =
    hex?.takeIf { it.isNotBlank() }?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
