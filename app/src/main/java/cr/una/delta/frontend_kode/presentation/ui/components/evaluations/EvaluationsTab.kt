package cr.una.delta.frontend_kode.presentation.ui.components.evaluations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cr.una.delta.frontend_kode.data.local.StudentRubricDates
import cr.una.delta.frontend_kode.domain.model.Assignment
import cr.una.delta.frontend_kode.domain.model.CourseRubric
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Convierte el peso (Double/String/BigDecimal) a Double de forma segura. */
private fun CourseRubric.weightAsDouble(): Double =
    weightPercentage.toString().toDoubleOrNull() ?: 0.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationsTab(
    rubrics: List<CourseRubric>,
    assignments: List<Assignment>,
    courseName: String,
    grades: Map<Long, String> = emptyMap(),
    onGradeChange: (Long, String) -> Unit = { _, _ -> },
    onCameraClick: () -> Unit,
    onBackClick: () -> Unit,
    onAddRubric: (String, Double, String?) -> Unit = { _, _, _ -> },
    consejo: String? = null,
    consejoLoading: Boolean = false,
    onPedirConsejo: (Double, Double, Double, Double?, Boolean) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    // Las notas (0..100) por rubricId vienen del backend a través del ViewModel.

    // Diálogo para agregar una rúbrica a mano
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newWeight by remember { mutableStateOf("") }
    var newDate by remember { mutableStateOf<LocalDate?>(null) }
    var target by remember { mutableStateOf("70") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Agregar rúbrica") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre (ej. Examen 1)") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) newWeight = it },
                        label = { Text("Peso %") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    RubricDateField(date = newDate, onDateChange = { newDate = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        onAddRubric(newName.trim(), newWeight.toDoubleOrNull() ?: 0.0, newDate?.toString())
                        newName = ""
                        newWeight = ""
                        newDate = null
                        showAddDialog = false
                    }
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Aporte de cada rubro = nota/100 * peso ; total = suma de aportes
    val totalObtenido = rubrics.sumOf { r ->
        val nota = grades[r.rubricId]?.toDoubleOrNull()
        if (nota != null) nota / 100.0 * r.weightAsDouble() else 0.0
    }
    val pesoConNota = rubrics.sumOf { r ->
        if (grades[r.rubricId]?.toDoubleOrNull() != null) r.weightAsDouble() else 0.0
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                }
                Column {
                    Text(text = "Rubros", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = courseName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Escanear una rúbrica desde una foto (OCR): la lee y la deja
            // para revisar/editar antes de importar.
            FilledTonalIconButton(onClick = onCameraClick) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Escanear rúbrica con una foto"
                )
            }
        }

        // Tarjeta con la nota total calculada + simulador
        if (rubrics.isNotEmpty()) {
            NotaTotalCard(totalObtenido = totalObtenido, pesoConNota = pesoConNota)
            Spacer(Modifier.height(8.dp))
            SimuladorCard(
                target = target,
                onTargetChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) target = it },
                totalObtenido = totalObtenido,
                pesoTotal = rubrics.sumOf { it.weightAsDouble() },
                pesoConNota = pesoConNota,
                consejo = consejo,
                consejoLoading = consejoLoading,
                onPedirConsejo = onPedirConsejo
            )
        }

        // Lista de rubros (calculadora) + evaluaciones
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (rubrics.isNotEmpty()) {
                item {
                    Text(
                        text = "Rubros del curso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(rubrics) { rubric ->
                    GradeRubricCard(
                        rubric = rubric,
                        gradeText = grades[rubric.rubricId] ?: "",
                        onGradeChange = { input ->
                            // Solo permite vacío o números 0..100
                            val valid = input.isEmpty() ||
                                    (input.toDoubleOrNull()?.let { it in 0.0..100.0 } == true)
                            if (valid) onGradeChange(rubric.rubricId, input)
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Este curso aún no tiene rubros de evaluación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Las rúbricas las define el profesor; el estudiante solo ingresa sus notas.

            if (assignments.isNotEmpty()) {
                item {
                    Text(
                        text = "Evaluaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(assignments) { assignment ->
                    AssignmentCard(assignment = assignment)
                }
            }
        }
    }
}

@Composable
private fun NotaTotalCard(totalObtenido: Double, pesoConNota: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Nota acumulada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${formatNum(pesoConNota)}% del curso ya calificado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatNum(totalObtenido),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " /100",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SimuladorCard(
    target: String,
    onTargetChange: (String) -> Unit,
    totalObtenido: Double,
    pesoTotal: Double,
    pesoConNota: Double,
    consejo: String? = null,
    consejoLoading: Boolean = false,
    onPedirConsejo: (Double, Double, Double, Double?, Boolean) -> Unit = { _, _, _, _, _ -> }
) {
    val meta = target.toDoubleOrNull()
    val restante = (pesoTotal - pesoConNota).coerceAtLeast(0.0)

    val alcanzable: Boolean
    val mensaje: String
    // Promedio que necesita en lo que falta (null si no aplica), para el consejo IA.
    var promedioNecesario: Double? = null
    when {
        meta == null -> { alcanzable = false; mensaje = "Escribe la nota meta." }
        totalObtenido >= meta -> { alcanzable = true; mensaje = "¡Ya la alcanzaste! Llevas ${formatNum(totalObtenido)}%." }
        restante <= 0.0 -> { alcanzable = false; mensaje = "Ya no quedan rubros y no llegaste a ${formatNum(meta)}%." }
        else -> {
            val necesita = (meta - totalObtenido) / (restante / 100.0)
            promedioNecesario = necesita
            alcanzable = necesita <= 100.0
            mensaje = if (alcanzable)
                "Necesitas promedio ${formatNum(necesita)}% en lo que falta (${formatNum(restante)}% del curso)."
            else
                "No es alcanzable: necesitarías ${formatNum(necesita)}% (más de 100)."
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "¿Qué necesito para mi meta?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = onTargetChange,
                    label = { Text("Meta") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(96.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (alcanzable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            // Consejo de la IA: solo tiene sentido cuando ya hay una meta escrita.
            if (meta != null) {
                Spacer(Modifier.height(12.dp))
                if (consejoLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Pensando un consejo…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    TextButton(
                        onClick = { onPedirConsejo(meta, totalObtenido, restante, promedioNecesario, alcanzable) },
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (consejo == null) "¿En qué me enfoco? (IA)" else "Otra vez")
                    }
                }
                consejo?.let {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeRubricCard(
    rubric: CourseRubric,
    gradeText: String,
    onGradeChange: (String) -> Unit
) {
    val weight = rubric.weightAsDouble()
    val nota = gradeText.toDoubleOrNull()
    val aporte = if (nota != null) nota / 100.0 * weight else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rubric.rubricName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${formatNum(weight)}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            val profDate = rubric.dueDate?.takeIf { it.isNotBlank() }
            if (profDate != null) {
                // Fecha puesta por el profesor (solo lectura).
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formatRubricDate(profDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Sin fecha del profe: el estudiante puede ponerle una fecha
                // personal, que alimenta su plan del día.
                val context = LocalContext.current
                var personal by remember(rubric.rubricId) {
                    mutableStateOf(
                        StudentRubricDates.get(context, rubric.rubricId)
                            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    )
                }
                Spacer(Modifier.height(8.dp))
                RubricDateField(
                    date = personal,
                    onDateChange = {
                        personal = it
                        StudentRubricDates.set(context, rubric.rubricId, it?.toString())
                    },
                    label = "Tu fecha (para el plan)"
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = onGradeChange,
                    label = { Text("Nota obtenida") },
                    placeholder = { Text("0 - 100") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Aporte",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (aporte != null) "${formatNum(aporte)}%" else "—",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/** Formatea un número: sin decimales si es entero, con 2 decimales si no. */
private fun formatNum(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format("%.2f", value)

/** "yyyy-MM-dd" -> "dd/MM/yyyy" (si no parsea, devuelve el original). */
private fun formatRubricDate(iso: String): String =
    runCatching {
        LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))
    }.getOrDefault(iso)

/** Campo de fecha opcional para la rúbrica; abre un calendario al tocarlo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RubricDateField(
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    label: String = "Fecha (opcional)"
) {
    var show by remember { mutableStateOf(false) }
    val fmt = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }

    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = { show = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    date?.format(fmt) ?: "Sin fecha",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (date == null) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                )
            }
            if (date != null) {
                TextButton(onClick = { onDateChange(null) }) { Text("Quitar") }
            }
        }
    }

    if (show) {
        val initMillis = remember(date) {
            (date ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initMillis)
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        onDateChange(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    show = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun AssignmentCard(assignment: Assignment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                assignment.dueDate?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            assignment.description?.let { desc ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                assignment.type?.let { type ->
                    AssistChip(
                        onClick = { },
                        label = { Text(type) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                AssistChip(
                    onClick = { },
                    label = { Text("Pendiente") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            }
        }
    }
}
