package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import cr.una.delta.frontend_kode.presentation.viewmodel.NoteEditorViewModel

private val NOTE_COLORS = listOf("#FFF3B0", "#CDEAC0", "#CFE3FF", "#FFD6E0", "#E7D3FF", "#FFE0C2")
private val TEXT_COLORS = listOf("#1C1B1F", "#6B46C1", "#C2185B", "#00889E", "#B26A00", "#2E7D32")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    navController: NavHostController,
    courseId: Long,
    noteId: Long,
    paddingValues: PaddingValues,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val contentStr by viewModel.content.collectAsState()
    val pinned by viewModel.pinned.collectAsState()
    val color by viewModel.color.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val checklist by viewModel.checklist.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val summarizing by viewModel.summarizing.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val summaryError by viewModel.summaryError.collectAsState()

    LaunchedEffect(noteId) { viewModel.load(noteId) }

    var showTagDialog by remember { mutableStateOf(false) }

    // Estado del editor enriquecido; se siembra una vez con el HTML guardado.
    val richState = rememberRichTextState()
    var seeded by remember { mutableStateOf(false) }
    LaunchedEffect(loading, contentStr) {
        if (!loading && !seeded) {
            richState.setHtml(contentStr)
            seeded = true
        }
    }

    val transparent = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
    )

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = {
            TopAppBar(
                title = { Text(if (noteId > 0L) "Editar apunte" else "Nuevo apunte") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    // Resumir con IA: vuelca el HTML actual y pide resumen + checklist.
                    if (summarizing) {
                        CircularProgressIndicator(Modifier.padding(end = 6.dp).size(22.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = {
                            viewModel.onContent(richState.toHtml())
                            viewModel.resumir()
                        }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Resumir con IA", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            Icons.Outlined.PushPin,
                            contentDescription = if (pinned) "Desfijar" else "Fijar",
                            tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = if (pinned) Modifier else Modifier.rotate(45f)
                        )
                    }
                    if (saving) CircularProgressIndicator(Modifier.padding(end = 12.dp).size(22.dp), strokeWidth = 2.dp)
                    else TextButton(enabled = title.isNotBlank(), onClick = {
                        viewModel.onContent(richState.toHtml())
                        viewModel.save(courseId, noteId) { navController.navigateUp() }
                    }) { Text("Guardar") }
                }
            )
        },
        bottomBar = {
            if (!loading) {
                EditorToolbar(
                    state = richState,
                    selectedColor = color,
                    onColor = { viewModel.setColor(it) },
                    onChecklist = { viewModel.addChecklistItem() }
                )
            }
        }
    ) { inner ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            // Título
            TextField(
                value = title, onValueChange = viewModel::onTitle,
                placeholder = { Text("Título", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                colors = transparent, singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            // Etiquetas
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                tags.forEach { tag ->
                    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Row(Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(tag, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Icon(Icons.Default.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f), modifier = Modifier.padding(start = 3.dp).size(14.dp).clickable { viewModel.removeTag(tag) })
                        }
                    }
                }
                Surface(shape = RoundedCornerShape(999.dp), color = Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.clickable { showTagDialog = true }) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Text(" Etiqueta", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Contenido (WYSIWYG)
            RichTextEditor(
                state = richState,
                colors = RichTextEditorDefaults.richTextEditorColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)
            )

            // Checklist
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Pendientes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            checklist.forEachIndexed { i, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                            .background(if (item.done) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { viewModel.toggleChecklist(i) },
                        contentAlignment = Alignment.Center
                    ) { if (item.done) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(15.dp)) }
                    TextField(
                        value = item.text, onValueChange = { viewModel.setChecklistText(i, it) },
                        placeholder = { Text("Pendiente…") }, singleLine = true, colors = transparent,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textDecoration = if (item.done) TextDecoration.LineThrough else null),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.removeChecklistItem(i) }) { Icon(Icons.Default.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
                }
            }
            TextButton(onClick = { viewModel.addChecklistItem() }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Agregar pendiente")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTagDialog) {
        var newTag by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Nueva etiqueta") },
            text = {
                OutlinedTextField(
                    value = newTag, onValueChange = { newTag = it }, singleLine = true,
                    label = { Text("Etiqueta") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            },
            confirmButton = { TextButton(enabled = newTag.isNotBlank(), onClick = { viewModel.addTag(newTag); showTagDialog = false }) { Text("Agregar") } },
            dismissButton = { TextButton(onClick = { showTagDialog = false }) { Text("Cancelar") } }
        )
    }

    // Resultado del resumen con IA
    summary?.let { s ->
        AlertDialog(
            onDismissRequest = { viewModel.cerrarResumen() },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Resumen del apunte") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (s.resumen.isNotBlank()) {
                        Text(s.resumen, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (s.checklist.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Para estudiar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        s.checklist.forEach { punto ->
                            Row(Modifier.padding(vertical = 2.dp)) {
                                Text("•  ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Text(punto, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (s.checklist.isNotEmpty()) {
                    TextButton(onClick = { viewModel.agregarChecklistSugerido() }) { Text("Agregar a pendientes") }
                } else {
                    TextButton(onClick = { viewModel.cerrarResumen() }) { Text("Listo") }
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.cerrarResumen() }) { Text("Cerrar") } }
        )
    }

    // Error del resumen
    summaryError?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.limpiarErrorResumen() },
            title = { Text("No se pudo resumir") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { viewModel.limpiarErrorResumen() }) { Text("Cerrar") } }
        )
    }
}

/** Barra inferior tipo "mini Word": formato, color de texto y color de la nota. */
@Composable
private fun EditorToolbar(
    state: RichTextState,
    selectedColor: String?,
    onColor: (String?) -> Unit,
    onChecklist: () -> Unit
) {
    val cur = state.currentSpanStyle
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Fila: herramientas de formato
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton(Icons.Default.FormatBold, "Negrita", active = cur.fontWeight == FontWeight.Bold) {
                    state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                }
                ToolButton(Icons.Default.FormatItalic, "Cursiva", active = cur.fontStyle == FontStyle.Italic) {
                    state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                }
                ToolButton(Icons.Default.FormatUnderlined, "Subrayado", active = cur.textDecoration == TextDecoration.Underline) {
                    state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                }
                ToolButton(Icons.Default.Title, "Título", active = cur.fontSize == 22.sp) {
                    state.toggleSpanStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                }
                ToolButton(Icons.Default.FormatListBulleted, "Viñetas", active = state.isUnorderedList) {
                    state.toggleUnorderedList()
                }
                ToolButton(Icons.Default.FormatListNumbered, "Numerada", active = state.isOrderedList) {
                    state.toggleOrderedList()
                }
                ToolButton(Icons.Default.Checklist, "Pendiente", active = false) { onChecklist() }
            }

            Spacer(Modifier.height(6.dp))

            // Fila: color de texto.
            // Recordamos el color elegido en estado local para marcarlo con claridad,
            // aunque no haya texto seleccionado; y lo sincronizamos con el del cursor.
            var activeColor by remember { mutableStateOf<Color?>(null) }
            LaunchedEffect(cur.color) { if (cur.color != Color.Unspecified) activeColor = cur.color }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FormatColorText, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                TEXT_COLORS.forEach { hex ->
                    val c = Color(android.graphics.Color.parseColor(hex))
                    val selected = activeColor == c
                    Box(
                        Modifier.padding(end = 8.dp)
                            .size(if (selected) 30.dp else 24.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable {
                                activeColor = if (activeColor == c) null else c
                                state.toggleSpanStyle(SpanStyle(color = c))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) Icon(
                            Icons.Default.Check, contentDescription = "Color activo",
                            tint = Color.White, modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Fila: color de la nota
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Color de la nota", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                NOTE_COLORS.forEach { hex ->
                    val c = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        Modifier.padding(end = 7.dp).size(22.dp).clip(CircleShape).background(c)
                            .border(width = if (selectedColor == hex) 2.dp else 0.dp, color = MaterialTheme.colorScheme.onSurface, shape = CircleShape)
                            .clickable { onColor(if (selectedColor == hex) null else hex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolButton(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(end = 2.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, contentDescription = label,
            tint = if (active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}
