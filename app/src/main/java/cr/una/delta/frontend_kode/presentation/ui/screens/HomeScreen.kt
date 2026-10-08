package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cr.una.delta.frontend_kode.domain.model.BlockType
import cr.una.delta.frontend_kode.domain.model.StudyPlanBlock
import cr.una.delta.frontend_kode.domain.model.Task
import cr.una.delta.frontend_kode.presentation.ui.components.home.ProgressRing
import cr.una.delta.frontend_kode.presentation.ui.components.home.TaskRow
import cr.una.delta.frontend_kode.presentation.ui.components.home.TimelineBlock
import cr.una.delta.frontend_kode.presentation.ui.components.home.blockColor
import cr.una.delta.frontend_kode.presentation.ui.components.home.blockIcon
import cr.una.delta.frontend_kode.presentation.viewmodel.HomeState
import cr.una.delta.frontend_kode.presentation.viewmodel.HomeViewModel
import cr.una.delta.frontend_kode.presentation.viewmodel.MealTimes
import cr.una.delta.frontend_kode.presentation.viewmodel.StudyWindow
import cr.una.delta.frontend_kode.presentation.viewmodel.PlanDiaState
import cr.una.delta.frontend_kode.presentation.viewmodel.PlanDiaViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    planViewModel: PlanDiaViewModel = hiltViewModel(),
    paddingValues: PaddingValues
) {
    val homeState by homeViewModel.state.collectAsState()
    val planState by planViewModel.state.collectAsState()
    val studyHours by planViewModel.studyHours.collectAsState()
    val meals by planViewModel.meals.collectAsState()
    val studyWindow by planViewModel.studyWindow.collectAsState()
    val userName by homeViewModel.userName.collectAsState()
    var detailTask by remember { mutableStateOf<Task?>(null) }

    var showMealsDialog by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0 = Plan, 1 = Tareas

    LaunchedEffect(Unit) {
        homeViewModel.loadHomeData()
        planViewModel.load()
    }

    val isRefreshing = homeState is HomeState.Loading || planState is PlanDiaState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            homeViewModel.loadHomeData()
            planViewModel.regenerate()
        }
    )

    val tasks = (homeState as? HomeState.Success)?.tasks ?: emptyList()
    val blocks = (planState as? PlanDiaState.Ready)?.plan?.blocks ?: emptyList()
    val classCount = blocks.count { it.type == BlockType.CLASS }
    val pending = tasks.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // ===== CABECERA =====
            item {
                HomeHeader(
                    name = userName,
                    classCount = classCount,
                    pending = pending,
                    studyHours = studyHours,
                    onDec = { planViewModel.setStudyHours(studyHours - 1) },
                    onInc = { planViewModel.setStudyHours(studyHours + 1) }
                )
            }

            // Editar comidas
            item {
                TextButton(
                    onClick = { showMealsDialog = true },
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                ) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Editar horarios (estudio y comidas)")
                }
            }

            // ===== PESTAÑAS =====
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeTab("Plan del día", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                    HomeTab("Tareas · $pending", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                }
            }

            if (selectedTab == 0) {
                // ===== PLAN (línea de tiempo) =====
                when (val ps = planState) {
                    is PlanDiaState.Loading -> item { CenterLoader() }
                    is PlanDiaState.Error -> item {
                        Text("Error: ${ps.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(20.dp))
                    }
                    is PlanDiaState.Ready -> {
                        if (ps.plan.blocks.isEmpty()) {
                            item {
                                Text(
                                    "Sin clases ni tareas para hoy. ¡Disfrutá!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        } else {
                            items(ps.plan.blocks) { block ->
                                PlanTimelineRow(block) { planViewModel.removeStudyBlock(block) }
                            }
                        }
                    }
                }
            } else {
                // ===== TAREAS =====
                if (tasks.isEmpty()) {
                    item {
                        Text(
                            "No tienes tareas pendientes",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    items(tasks) { task ->
                        Box(Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                            TaskRow(
                                title = task.title,
                                dueLabel = dueLabel(task),
                                urgent = isToday(task.dueDate),
                                done = isDone(task),
                                onToggle = {
                                    homeViewModel.markTaskAsDone(task)
                                    planViewModel.regenerate()
                                },
                                onOpenDetail = { detailTask = task }
                            )
                        }
                    }
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Las tareas que marcás se sacan del plan y se replanifica el día.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ===== CHIPS DE ACCIÓN =====
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionChip("Crear curso", Icons.Filled.School) { navController.navigate("create_course") }
                    ActionChip("Apuntes de hoy", Icons.Filled.StickyNote2) { navController.navigate("apuntes_home") }
                    ActionChip("Mi progreso", Icons.Filled.TrendingUp) { navController.navigate("grade_goals") }
                    ActionChip("Ánimo", Icons.Filled.Mood) { navController.navigate("moodlearning") }
                    ActionChip("Replanificar", Icons.Filled.Autorenew) { planViewModel.regenerate() }
                }
            }
        }

        // FAB "Agregar tarea" — 16dp arriba del borde (que ya excluye la barra inferior).
        ExtendedFloatingActionButton(
            onClick = { navController.navigate("create_task") },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Agregar tarea") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        )

        // ===== POP-UP: DETALLE DE TAREA =====
        detailTask?.let { t ->
            AlertDialog(
                onDismissRequest = { detailTask = null },
                confirmButton = {
                    TextButton(onClick = { detailTask = null }) { Text("Cerrar") }
                },
                title = { Text(t.title, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Estado: ${t.status.label}")
                        Text("Prioridad: ${t.priority.label}")
                        val due = dueLabel(t)
                        if (due.isNotBlank()) Text("Entrega: $due")
                        if (t.notes.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(t.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    if (showMealsDialog) {
        MealsDialog(
            initial = meals,
            initialWindow = studyWindow,
            onSave = { planViewModel.setMealTimes(it) },
            onSaveWindow = { planViewModel.setStudyWindow(it) },
            onDismiss = { showMealsDialog = false }
        )
    }
}

@Composable
private fun HomeHeader(
    name: String,
    classCount: Int,
    pending: Int,
    studyHours: Int,
    onDec: () -> Unit,
    onInc: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(dateLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
                    Spacer(Modifier.height(3.dp))
                    Text(
                        greeting() + if (name.isNotBlank()) ", $name" else "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        resumen(classCount, pending),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }
                Spacer(Modifier.width(14.dp))
                ProgressRing(percentOfDay())
            }

            Spacer(Modifier.height(16.dp))

            // Horas de estudio (stepper compacto dentro de la cabecera)
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Horas de estudio hoy", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Se reparten en el plan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StepperButton(Icons.Filled.Remove, enabled = studyHours > 0, onClick = onDec)
                    Text("$studyHours h", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.widthIn(min = 40.dp).padding(horizontal = 4.dp))
                    StepperButton(Icons.Filled.Add, enabled = studyHours < 10, onClick = onInc)
                }
            }
        }
    }
}

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(32.dp).clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 0.12f else 0.05f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.4f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun HomeTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun PlanTimelineRow(block: StudyPlanBlock, onRemove: () -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    val color = blockColor(block.type, parseHex(block.color))
    var showDetail by remember { mutableStateOf(false) }
    val canOpen = block.type == BlockType.STUDY && block.detailTitle != null
    Box(Modifier.padding(horizontal = 20.dp)) {
        TimelineBlock(
            start = block.startTime.format(fmt),
            end = block.endTime.format(fmt),
            title = block.description,
            subtitle = "",
            color = color,
            icon = blockIcon(block.type),
            showRemove = block.type == BlockType.STUDY,
            onRemove = onRemove,
            onClick = if (canOpen) ({ showDetail = true }) else null
        )
    }
    if (showDetail) {
        StudyDetailDialog(block = block) { showDetail = false }
    }
}

/** Pop up con el detalle de un bloque de estudio (título, curso, fecha límite). */
@Composable
private fun StudyDetailDialog(block: StudyPlanBlock, onClose: () -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(block.detailTitle ?: "Estudio") },
        text = {
            Column {
                block.detailCourse?.let {
                    Text("Curso", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(it, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                }
                Text("Fecha límite", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(block.detailDueDate ?: "Sin fecha", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text("En tu plan", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${block.startTime.format(fmt)} – ${block.endTime.format(fmt)}", style = MaterialTheme.typography.bodyLarge)
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Cerrar") } }
    )
}

@Composable
private fun ActionChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CenterLoader() {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

// ---------- Helpers ----------

private fun greeting(): String {
    val h = LocalTime.now().hour
    return when {
        h < 12 -> "Buenos días"
        h < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

private fun dateLabel(): String {
    val d = LocalDate.now()
    val dow = d.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }
    val month = d.month.getDisplayName(TextStyle.FULL, Locale("es"))
    return "$dow ${d.dayOfMonth} de $month"
}

private fun resumen(classCount: Int, pending: Int): String {
    if (pending == 0 && classCount == 0) return "Todo al día. Buen trabajo."
    val c = if (classCount == 1) "1 clase" else "$classCount clases"
    val t = if (pending == 1) "1 tarea pendiente" else "$pending tareas pendientes"
    return "$c · $t"
}

/** Porcentaje del día transcurrido entre 6:00 y 22:00. */
private fun percentOfDay(): Int {
    val now = LocalTime.now()
    val start = 6 * 60
    val end = 22 * 60
    val cur = now.hour * 60 + now.minute
    return ((cur - start).coerceIn(0, end - start) * 100 / (end - start))
}

private fun isDone(task: Task): Boolean =
    task.status.id == 3L || task.status.label.uppercase() in listOf("DONE", "COMPLETED", "FINISHED")

private fun isToday(due: java.util.Date): Boolean {
    val c = Calendar.getInstance().apply { time = due }
    val n = Calendar.getInstance()
    return c.get(Calendar.YEAR) == n.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == n.get(Calendar.DAY_OF_YEAR)
}

private fun dueLabel(task: Task): String {
    val fmt = java.text.SimpleDateFormat("d MMM", Locale("es"))
    return if (isToday(task.dueDate)) "Hoy" else fmt.format(task.dueDate)
}

private fun parseHex(hex: String?): Color? =
    hex?.takeIf { it.isNotBlank() }?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }

// ---------- Diálogo de comidas ----------

@Composable
private fun MealsDialog(
    initial: MealTimes,
    initialWindow: StudyWindow,
    onSave: (MealTimes) -> Unit,
    onSaveWindow: (StudyWindow) -> Unit,
    onDismiss: () -> Unit
) {
    var desS by remember { mutableStateOf(initial.breakfastStart.toString()) }
    var desE by remember { mutableStateOf(initial.breakfastEnd.toString()) }
    var almS by remember { mutableStateOf(initial.lunchStart.toString()) }
    var almE by remember { mutableStateOf(initial.lunchEnd.toString()) }
    var cenS by remember { mutableStateOf(initial.dinnerStart.toString()) }
    var cenE by remember { mutableStateOf(initial.dinnerEnd.toString()) }
    var estS by remember { mutableStateOf(initialWindow.start.toString()) }
    var estE by remember { mutableStateOf(initialWindow.end.toString()) }

    fun p(s: String): LocalTime? = runCatching { LocalTime.parse(s.trim()) }.getOrNull()
    val valid = listOf(desS, desE, almS, almE, cenS, cenE, estS, estE).all { p(it) != null } &&
            p(desE)!! > p(desS)!! && p(almE)!! > p(almS)!! && p(cenE)!! > p(cenS)!! && p(estE)!! > p(estS)!!

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Horarios") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                MealRow("¿Cuándo podés estudiar?", estS, { estS = it }, estE, { estE = it })
                Spacer(Modifier.height(4.dp))
                Text("La IA pone los bloques de estudio en ese rango.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                MealRow("Desayuno", desS, { desS = it }, desE, { desE = it })
                Spacer(Modifier.height(8.dp))
                MealRow("Almuerzo", almS, { almS = it }, almE, { almE = it })
                Spacer(Modifier.height(8.dp))
                MealRow("Cena", cenS, { cenS = it }, cenE, { cenE = it })
                Spacer(Modifier.height(6.dp))
                Text("Formato 24 h (HH:mm)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                onSaveWindow(StudyWindow(start = p(estS)!!, end = p(estE)!!))
                onSave(
                    MealTimes(
                        breakfastStart = p(desS)!!, breakfastEnd = p(desE)!!,
                        lunchStart = p(almS)!!, lunchEnd = p(almE)!!,
                        dinnerStart = p(cenS)!!, dinnerEnd = p(cenE)!!
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun MealRow(
    label: String,
    start: String, onStart: (String) -> Unit,
    end: String, onEnd: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = start, onValueChange = onStart, label = { Text("Inicio") }, singleLine = true, modifier = Modifier.weight(1f))
            Text("  a  ")
            OutlinedTextField(value = end, onValueChange = onEnd, label = { Text("Fin") }, singleLine = true, modifier = Modifier.weight(1f))
        }
    }
}
