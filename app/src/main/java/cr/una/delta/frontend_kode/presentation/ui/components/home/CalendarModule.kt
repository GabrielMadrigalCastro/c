package cr.una.delta.frontend_kode.presentation.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cr.una.delta.frontend_kode.domain.model.ClassSession
import cr.una.delta.frontend_kode.domain.model.Task
import java.text.SimpleDateFormat
import java.util.*

/** Modos de visualización del calendario. */
enum class CalendarViewMode(val label: String) {
    MES("Mes"), SEMANA("Semana"), DIA("Día")
}

// Valores de modalidad usados en toda la app.
object Modality {
    const val PRESENCIAL = "Presencial"
    const val VIRTUAL = "Virtual"
    const val CANCELADA = "Cancelada"
}

// ---------------------------------------------------------------------------
// Selector Mes / Semana / Día
// ---------------------------------------------------------------------------
@Composable
fun CalendarViewToggle(
    selected: CalendarViewMode,
    onSelect: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(Modifier.padding(4.dp)) {
            CalendarViewMode.values().forEach { mode ->
                val isSel = mode == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(mode) },
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent
                ) {
                    Text(
                        text = mode.label,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Vista SEMANA: fila de 7 días con marcador
// ---------------------------------------------------------------------------
@Composable
fun WeekStrip(
    selectedDate: Calendar,
    markedDates: Set<String>,
    onDateClick: (Calendar) -> Unit
) {
    val keyFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dowFmt = remember { SimpleDateFormat("EEE", Locale.getDefault()) } // día abreviado
    val todayKey = remember { keyFmt.format(Calendar.getInstance().time) }

    val startOfWeek = remember(selectedDate.timeInMillis) {
        (selectedDate.clone() as Calendar).apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
    }
    val days = remember(startOfWeek.timeInMillis) {
        (0..6).map { i -> (startOfWeek.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i) } }
    }
    val selectedKey = keyFmt.format(selectedDate.time)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                val key = keyFmt.format(day.time)
                val isSelected = key == selectedKey
                val isToday = key == todayKey
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDateClick(day) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        dowFmt.format(day.time).take(1).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .then(
                                when {
                                    // Día seleccionado: círculo relleno.
                                    isSelected -> Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                                    // Hoy (si no está seleccionado): aro morado.
                                    isToday -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else -> Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${day.get(Calendar.DAY_OF_MONTH)}",
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Box(
                        Modifier
                            .size(5.dp)
                            .background(
                                if (key in markedDates)
                                    (if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary)
                                else Color.Transparent,
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Vista DÍA: encabezado fijo con la fecha actual (hoy)
// ---------------------------------------------------------------------------
@Composable
fun TodayHeader(today: Calendar) {
    val titleFmt = remember { SimpleDateFormat("EEEE dd 'de' MMMM", Locale.getDefault()) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                titleFmt.format(today.time).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Agenda del día: clases (editables) + tareas
// ---------------------------------------------------------------------------
@Composable
fun DayAgenda(
    classes: List<ClassSession>,
    tasks: List<Task>,
    courseNameFor: (Long) -> String,
    onClassClick: (ClassSession) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        SectionHeader("Clases", Icons.Filled.MenuBook)
        Spacer(Modifier.height(8.dp))
        if (classes.isEmpty()) {
            EmptyLine("No hay clases este día.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                classes.sortedBy { it.startTime }.forEach { c ->
                    ClassCard(c, courseNameFor(c.courseId)) { onClassClick(c) }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        SectionHeader("Tareas", Icons.Filled.TaskAlt)
        Spacer(Modifier.height(8.dp))
        if (tasks.isEmpty()) {
            EmptyLine("No hay tareas para este día.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tasks.forEach { TaskRow(it) }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(text: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyLine(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ClassCard(session: ClassSession, courseName: String, onClick: () -> Unit) {
    val cancelled = session.modality.equals(Modality.CANCELADA, ignoreCase = true)
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${session.startTime} - ${session.endTime}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = if (cancelled) TextDecoration.LineThrough else null
                    )
                }
                Text(
                    courseName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (cancelled) TextDecoration.LineThrough else null
                )
                if (session.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            session.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            ModalityBadge(session.modality)
        }
    }
}

@Composable
fun ModalityBadge(modality: String) {
    val bg: Color; val fg: Color; val label: String; val icon: ImageVector
    when {
        modality.equals(Modality.VIRTUAL, true) -> {
            bg = MaterialTheme.colorScheme.tertiaryContainer; fg = MaterialTheme.colorScheme.onTertiaryContainer; label = "Virtual"; icon = Icons.Filled.Videocam
        }
        modality.equals(Modality.CANCELADA, true) -> {
            bg = MaterialTheme.colorScheme.errorContainer; fg = MaterialTheme.colorScheme.onErrorContainer; label = "Cancelada"; icon = Icons.Filled.Block
        }
        else -> {
            bg = MaterialTheme.colorScheme.secondaryContainer; fg = MaterialTheme.colorScheme.onSecondaryContainer; label = "Presencial"; icon = Icons.Filled.School
        }
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
        }
    }
}

@Composable
private fun TaskRow(task: Task) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (task.notes.isNotBlank()) {
                    Text(
                        task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            PriorityChip(task.priority.label)
        }
    }
}

@Composable
private fun PriorityChip(label: String) {
    val (bg, fg) = when (label.uppercase()) {
        "ALTA", "HIGH" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "BAJA", "LOW" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Diálogo de acciones sobre una clase
// ---------------------------------------------------------------------------
@Composable
fun ClassActionsDialog(
    session: ClassSession,
    courseName: String,
    onEdit: () -> Unit,
    onSetModality: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val isCancelled = session.modality.equals(Modality.CANCELADA, true)
    val isVirtual = session.modality.equals(Modality.VIRTUAL, true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(courseName) },
        text = {
            Column {
                Text(
                    "${session.classDate}  ·  ${session.startTime}-${session.endTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                ActionRow("Editar horario", Icons.Filled.Edit, onClick = onEdit)

                if (isVirtual) ActionRow("Cambiar a presencial", Icons.Filled.School) { onSetModality(Modality.PRESENCIAL) }
                else ActionRow("Cambiar a virtual", Icons.Filled.Videocam) { onSetModality(Modality.VIRTUAL) }

                if (isCancelled) ActionRow("Reactivar clase", Icons.Filled.Replay) { onSetModality(Modality.PRESENCIAL) }
                else ActionRow("Marcar como cancelada", Icons.Filled.Block) { onSetModality(Modality.CANCELADA) }

                ActionRow("Eliminar clase", Icons.Filled.DeleteOutline, color = MaterialTheme.colorScheme.error, onClick = onDelete)
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun ActionRow(text: String, icon: ImageVector, color: Color = Color.Unspecified, onClick: () -> Unit) {
    val tint = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = tint, style = MaterialTheme.typography.bodyLarge)
    }
}

// ---------------------------------------------------------------------------
// Diálogo para editar una clase suelta (hora, lugar, modalidad)
// ---------------------------------------------------------------------------
@Composable
fun EditClassDialog(
    session: ClassSession,
    onSave: (startTime: String, endTime: String, location: String, modality: String) -> Unit,
    onDismiss: () -> Unit
) {
    var start by remember { mutableStateOf(session.startTime) }
    var end by remember { mutableStateOf(session.endTime) }
    var location by remember { mutableStateOf(session.location) }
    var modality by remember { mutableStateOf(session.modality.ifBlank { Modality.PRESENCIAL }) }

    fun validTime(t: String): Boolean =
        Regex("^([01]?\\d|2[0-3]):[0-5]\\d$").matches(t.trim())

    val canSave = validTime(start) && validTime(end)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar clase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = start,
                    onValueChange = { start = it },
                    label = { Text("Hora inicio (HH:mm)") },
                    singleLine = true,
                    isError = start.isNotBlank() && !validTime(start),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = end,
                    onValueChange = { end = it },
                    label = { Text("Hora fin (HH:mm)") },
                    singleLine = true,
                    isError = end.isNotBlank() && !validTime(end),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lugar / aula") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Modalidad", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Modality.PRESENCIAL, Modality.VIRTUAL, Modality.CANCELADA).forEach { m ->
                        FilterChip(
                            selected = modality.equals(m, true),
                            onClick = { modality = m },
                            label = { Text(m, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onSave(start.trim(), end.trim(), location.trim(), modality) }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
