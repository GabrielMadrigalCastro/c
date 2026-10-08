package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.domain.model.BlockType
import cr.una.delta.frontend_kode.domain.model.StudyPlanBlock
import cr.una.delta.frontend_kode.presentation.viewmodel.PlanDiaState
import cr.una.delta.frontend_kode.presentation.viewmodel.PlanDiaViewModel

@Composable
fun PlanDiaScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: PlanDiaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val studyHours by viewModel.studyHours.collectAsState()
    var detailBlock by remember { mutableStateOf<StudyPlanBlock?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Column(Modifier.weight(1f)) {
                Text("🧠 Plan del día", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                val dia = (state as? PlanDiaState.Ready)?.plan?.dayName
                if (dia != null) {
                    Text(dia, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = { viewModel.regenerate() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Regenerar", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Control de horas de estudio
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Horas de estudio hoy",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                FilledIconButton(
                    onClick = { viewModel.setStudyHours(studyHours - 1) },
                    enabled = studyHours > 0
                ) { Icon(Icons.Default.Remove, contentDescription = "Menos") }
                Text(
                    "$studyHours h",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                FilledIconButton(
                    onClick = { viewModel.setStudyHours(studyHours + 1) },
                    enabled = studyHours < 10
                ) { Icon(Icons.Default.Add, contentDescription = "Más") }
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(Modifier.fillMaxSize()) {
            when (val s = state) {
                is PlanDiaState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is PlanDiaState.Error ->
                    Text(
                        "Error: ${s.message}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )

                is PlanDiaState.Ready -> {
                    if (s.plan.blocks.isEmpty()) {
                        Text(
                            "No hay nada que planificar hoy.",
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(s.plan.blocks) { block ->
                                val canOpen = block.type == BlockType.STUDY && block.detailTitle != null
                                BlockRow(
                                    block = block,
                                    onClick = if (canOpen) ({ detailBlock = block }) else null,
                                    onRemove = { viewModel.removeStudyBlock(block) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    detailBlock?.let { b ->
        StudyDetailDialog(block = b, onClose = { detailBlock = null })
    }
}

@Composable
private fun StudyDetailDialog(block: StudyPlanBlock, onClose: () -> Unit) {
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
                Text("${block.startTime} – ${block.endTime}", style = MaterialTheme.typography.bodyLarge)
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Cerrar") } }
    )
}

@Composable
private fun BlockRow(block: StudyPlanBlock, onClick: (() -> Unit)? = null, onRemove: () -> Unit) {
    val (emoji, fallback) = when (block.type) {
        BlockType.CLASS -> "🏫" to MaterialTheme.colorScheme.primaryContainer
        BlockType.STUDY -> "📚" to MaterialTheme.colorScheme.tertiaryContainer
        BlockType.MEAL -> "🍽️" to MaterialTheme.colorScheme.surfaceVariant
        BlockType.TRAVEL -> "🚌" to MaterialTheme.colorScheme.surfaceVariant
        BlockType.PERSONAL -> "☕" to MaterialTheme.colorScheme.surfaceVariant
    }
    val container = parseColor(block.color) ?: fallback

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Franja de hora
            Column(
                modifier = Modifier.width(96.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    block.startTime.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    block.endTime.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text("$emoji  ", style = MaterialTheme.typography.titleMedium)
            Text(
                block.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (block.type == BlockType.STUDY) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun parseColor(hex: String?): Color? =
    hex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
