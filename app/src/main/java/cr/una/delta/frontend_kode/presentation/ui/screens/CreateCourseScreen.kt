package cr.una.delta.frontend_kode.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.presentation.viewmodel.CreateCourseState
import cr.una.delta.frontend_kode.presentation.viewmodel.CreateCourseViewModel
import cr.una.delta.frontend_kode.presentation.viewmodel.HorarioForm
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: CreateCourseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#A5C8FF") }
    val horarios = remember { mutableStateListOf(HorarioForm()) }

    // Vigencia de las clases: por defecto de hoy a ~16 semanas (un semestre).
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusWeeks(16)) }

    val palette = listOf("#A5C8FF", "#D1B3FF", "#FFD1DC", "#B5EAD7", "#FFDAC1", "#C7CEEA")
    val days = listOf(
        DayOfWeek.MONDAY to "Lun", DayOfWeek.TUESDAY to "Mar", DayOfWeek.WEDNESDAY to "Mié",
        DayOfWeek.THURSDAY to "Jue", DayOfWeek.FRIDAY to "Vie", DayOfWeek.SATURDAY to "Sáb",
        DayOfWeek.SUNDAY to "Dom"
    )

    LaunchedEffect(state) {
        when (val s = state) {
            is CreateCourseState.Success -> {
                Toast.makeText(context, "Curso creado ✅", Toast.LENGTH_SHORT).show()
                viewModel.reset()
                navController.navigateUp()
            }
            is CreateCourseState.Error ->
                Toast.makeText(context, s.message, Toast.LENGTH_LONG).show()
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                "Crear curso",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del curso *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Text("Color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            palette.forEach { hex ->
                val c = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(
                            width = if (selectedColor == hex) 3.dp else 1.dp,
                            color = if (selectedColor == hex) MaterialTheme.colorScheme.primary
                            else Color.Gray.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .clickable { selectedColor = hex }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Horario", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "Elige el día y la hora. Puedes agregar 1 o más.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        horarios.forEachIndexed { index, h ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Horario ${index + 1}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { if (horarios.size > 1) horarios.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        days.forEach { (d, label) ->
                            DayChip(label = label, selected = h.day == d) {
                                horarios[index] = h.copy(day = d)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("Inicio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TimeRow(
                        hour = h.startHour, minute = h.startMinute, am = h.startAM,
                        onHour = { horarios[index] = h.copy(startHour = it) },
                        onMinute = { horarios[index] = h.copy(startMinute = it) },
                        onAm = { horarios[index] = h.copy(startAM = it) }
                    )

                    Spacer(Modifier.height(10.dp))
                    Text("Fin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TimeRow(
                        hour = h.endHour, minute = h.endMinute, am = h.endAM,
                        onHour = { horarios[index] = h.copy(endHour = it) },
                        onMinute = { horarios[index] = h.copy(endMinute = it) },
                        onAm = { horarios[index] = h.copy(endAM = it) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        OutlinedButton(onClick = { horarios.add(HorarioForm()) }, modifier = Modifier.fillMaxWidth()) {
            Text("＋ Agregar horario")
        }

        Spacer(Modifier.height(20.dp))
        Text("Vigencia de las clases", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "Las clases se repiten cada semana entre estas fechas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateField(
                label = "Inicio",
                date = startDate,
                onDateChange = { startDate = it },
                modifier = Modifier.weight(1f)
            )
            DateField(
                label = "Fin",
                date = endDate,
                onDateChange = { endDate = it },
                modifier = Modifier.weight(1f)
            )
        }
        if (endDate.isBefore(startDate)) {
            Spacer(Modifier.height(4.dp))
            Text(
                "La fecha de fin debe ser igual o posterior a la de inicio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Las rúbricas de evaluación se agregan después, dentro del curso.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.createCourse(name, selectedColor, horarios.toList(), startDate, endDate) },
            enabled = name.isNotBlank() && !endDate.isBefore(startDate) && state !is CreateCourseState.Saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (state is CreateCourseState.Saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Guardar curso")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Fila de hora numérica (hora : min + AM/PM), mismo formato que "Nueva tarea". */
@Composable
private fun TimeRow(
    hour: String,
    minute: String,
    am: Boolean,
    onHour: (String) -> Unit,
    onMinute: (String) -> Unit,
    onAm: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TimePickerField(value = hour, onValueChange = onHour)
        Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
        TimePickerField(value = minute, onValueChange = onMinute)
        Spacer(Modifier.width(12.dp))
        Column {
            AMPMButton("AM", isSelected = am, onClick = { onAm(true) })
            Spacer(Modifier.height(4.dp))
            AMPMButton("PM", isSelected = !am, onClick = { onAm(false) })
        }
    }
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Campo de fecha (solo lectura) que abre un selector de calendario al tocarlo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }
    val fmt = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }

    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Surface(
            onClick = { show = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                date.format(fmt),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
            )
        }
    }

    if (show) {
        val initMillis = remember(date) {
            date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
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
