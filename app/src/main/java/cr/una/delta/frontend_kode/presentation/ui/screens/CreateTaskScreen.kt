package cr.una.delta.frontend_kode.presentation.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.presentation.viewmodel.CreateTaskViewModel
import cr.una.delta.frontend_kode.presentation.viewmodel.CreateTaskState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    navController: NavHostController,
    viewModel: CreateTaskViewModel = hiltViewModel(),
    paddingValues: PaddingValues
) {
    val context = LocalContext.current

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val taskTitle by viewModel.taskTitle.collectAsStateWithLifecycle()
    val selectedCourse by viewModel.selectedCourse.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val hourText by viewModel.hourText.collectAsStateWithLifecycle()
    val minuteText by viewModel.minuteText.collectAsStateWithLifecycle()
    val isAM by viewModel.isAM.collectAsStateWithLifecycle()
    val selectedPriority by viewModel.selectedPriorityLabel.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusLabel.collectAsStateWithLifecycle()
    val availableCourses by viewModel.availableCourses.collectAsStateWithLifecycle()

    // Al cargar la pantalla
    LaunchedEffect(Unit) { viewModel.loadUserCourses() }

    // Manejo de estados
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateTaskState.Success -> {
                Toast.makeText(context, "Tarea creada", Toast.LENGTH_SHORT).show()
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("task_created", true)
                navController.popBackStack()
            }
            is CreateTaskState.Error -> {
                Toast.makeText(context, (uiState as CreateTaskState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    // DatePickerDialog
    val calendar = Calendar.getInstance().apply { time = selectedDate }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                viewModel.updateSelectedDate(newCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- Barra superior ---
            TopAppBar(
                title = {
                    Text(
                        "Nueva tarea",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            // --- Contenido scrollable ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // --- Título ---
                Text("Nombre de la tarea", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { viewModel.updateTaskTitle(it) },
                    placeholder = { Text("Ej. Práctica de redes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Curso ---
                Text("Curso", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (availableCourses.isEmpty()) {
                    Text("No hay cursos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRowCourses(
                        courses = availableCourses,
                        selected = selectedCourse,
                        onSelect = { viewModel.updateSelectedCourse(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Tipo ---
                Text("Tipo", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                FlowRowSimpleStrings(
                    items = viewModel.availableTypes,
                    selected = selectedType,
                    onSelect = { viewModel.updateSelectedType(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Fecha y hora ---
                Text("Fecha y hora", fontSize = 16.sp, fontWeight = FontWeight.Medium)

                val interactionSource = remember { MutableInteractionSource() }

                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) {
                            datePickerDialog.show()
                        }
                    }
                }

                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = interactionSource,
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Hora ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimePickerField(value = hourText, onValueChange = { viewModel.updateHourText(it) })
                    Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    TimePickerField(value = minuteText, onValueChange = { viewModel.updateMinuteText(it) })
                    Column {
                        AMPMButton("AM", isSelected = isAM, onClick = { viewModel.updateAMPM(true) })
                        AMPMButton("PM", isSelected = !isAM, onClick = { viewModel.updateAMPM(false) })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Estado ---
                Text("Estado", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                FlowRowSimpleStrings(
                    items = viewModel.availableStatuses,
                    selected = selectedStatus,
                    onSelect = { viewModel.updateSelectedStatus(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Prioridad ---
                Text("Prioridad", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.availablePriorities.forEachIndexed { index, priority ->
                        val color = when (index) {
                            0 -> Color(0xFF22C55E)
                            1 -> Color(0xFFF59E0B)
                            2 -> Color(0xFFEF4444)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        PriorityChip(
                            text = priority,
                            isSelected = selectedPriority == priority,
                            color = color,
                            onClick = { viewModel.updateSelectedPriority(priority) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Guardar (mismo estilo que Crear curso, para que sea consistente).
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) viewModel.createTask()
                        else Toast.makeText(context, "Ingresa un título", Toast.LENGTH_SHORT).show()
                    },
                    enabled = taskTitle.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Guardar tarea")
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // --- Overlay de carga ---
        if (uiState is CreateTaskState.LoadingCourses || uiState is CreateTaskState.Creating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** COMPONENTES REUTILIZABLES **/

@Composable
fun FlowRowCourses(courses: List<Course>, selected: Course?, onSelect: (Course) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        courses.forEach { course ->
            SelectableChip(
                text = course.courseName ?: "Curso ${course.courseId}",
                isSelected = selected?.courseId == course.courseId,
                onClick = { onSelect(course) }
            )
        }
    }
}

@Composable
fun FlowRowSimpleStrings(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach {
            SelectableChip(
                text = it,
                isSelected = selected == it,
                onClick = { onSelect(it) }
            )
        }
    }
}

@Composable
fun SelectableChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
    }
}

@Composable
fun TimePickerField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.width(80.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
fun AMPMButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
    }
}

@Composable
fun PriorityChip(text: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) color else MaterialTheme.colorScheme.outlineVariant, shape)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text, color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}