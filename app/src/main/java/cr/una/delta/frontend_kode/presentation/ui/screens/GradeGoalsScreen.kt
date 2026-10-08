package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import cr.una.delta.frontend_kode.presentation.viewmodel.CourseProgress
import cr.una.delta.frontend_kode.presentation.viewmodel.GradeGoalsState
import cr.una.delta.frontend_kode.presentation.viewmodel.GradeGoalsViewModel
import kotlin.math.roundToInt

@Composable
fun GradeGoalsScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    viewModel: GradeGoalsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize().padding(paddingValues)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 6.dp, end = 20.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
            Column {
                Text("Metas y progreso", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Poné tu nota meta por curso y seguí tu avance", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        when (val s = state) {
            is GradeGoalsState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is GradeGoalsState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { Text(s.message, color = MaterialTheme.colorScheme.error) }
            is GradeGoalsState.Empty -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Matriculate en un curso con rúbricas para ver tu progreso.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            is GradeGoalsState.Ready -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(s.items) { item ->
                    CourseGoalCard(item) { viewModel.setGoal(item.course.courseId, it) }
                }
            }
        }
    }
}

@Composable
private fun CourseGoalCard(item: CourseProgress, onGoal: (Int) -> Unit) {
    val courseColor = parseHex(item.course.courseColor) ?: MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Encabezado: curso + nota acumulada
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(courseColor))
                Spacer(Modifier.width(8.dp))
                Text(item.course.courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Text("${item.accumulated.roundToInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = courseColor)
                Text(" /100", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))

            // Barra-gráfico
            GradeBar(item)

            Spacer(Modifier.height(6.dp))
            Legend()

            Spacer(Modifier.height(12.dp))

            // Meta (slider)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Meta: ${item.goal}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(72.dp))
                Slider(
                    value = item.goal.toFloat(),
                    onValueChange = { onGoal(it.roundToInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Estado / mensaje
            Text(
                statusMessage(item),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    item.reachedGoal -> courseColor
                    !item.achievable -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            item.currentAverage?.let {
                Text(
                    "Promedio actual: ${it.roundToInt()} · ${item.gradedWeight.roundToInt()}% del curso calificado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GradeBar(item: CourseProgress) {
    val obtained = MaterialTheme.colorScheme.primary
    val lost = MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
    val track = MaterialTheme.colorScheme.surfaceVariant
    val goalColor = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.fillMaxWidth().height(16.dp)) {
        val w = size.width
        val h = size.height
        val r = h / 2f
        fun x(v: Double) = (v / 100.0 * w).toFloat().coerceIn(0f, w)

        // Fondo (pendiente = gris)
        drawRoundRect(color = track, size = Size(w, h), cornerRadius = CornerRadius(r, r))
        // Calificado pero no obtenido (rojo tenue): de accumulated a gradedWeight
        val gradedX = x(item.gradedWeight)
        val accX = x(item.accumulated)
        if (gradedX > accX) {
            drawRect(color = lost, topLeft = Offset(accX, 0f), size = Size(gradedX - accX, h))
        }
        // Obtenido (color primario): 0..accumulated
        if (accX > 0f) {
            drawRoundRect(color = obtained, size = Size(accX.coerceAtLeast(r), h), cornerRadius = CornerRadius(r, r))
        }
        // Marca de meta
        val gx = x(item.goal.toDouble())
        drawLine(color = goalColor, start = Offset(gx, -2f), end = Offset(gx, h + 2f), strokeWidth = 3.dp.toPx())
    }
}

@Composable
private fun Legend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LegendDot(MaterialTheme.colorScheme.primary, "Obtenido")
        LegendDot(MaterialTheme.colorScheme.error.copy(alpha = 0.55f), "Perdido")
        LegendDot(MaterialTheme.colorScheme.surfaceVariant, "Pendiente")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun statusMessage(item: CourseProgress): String {
    if (item.reachedGoal) return "¡Ya asegurás tu meta!"
    val needed = item.neededAverage
    if (needed == null) {
        // No queda nada por calificar y no llegó a la meta
        return "Sin evaluaciones pendientes · nota final ${item.accumulated.roundToInt()}"
    }
    if (needed > 100.0) {
        return "Tu meta ya no es alcanzable (necesitarías más de 100 en lo que falta)."
    }
    return "Necesitás promedio ${needed.roundToInt()} en el ${item.remainingWeight.roundToInt()}% que falta."
}

private fun parseHex(hex: String?): Color? =
    hex?.takeIf { it.isNotBlank() }?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
