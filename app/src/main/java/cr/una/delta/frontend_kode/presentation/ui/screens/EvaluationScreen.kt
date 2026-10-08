package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.presentation.ui.components.cameraUtils.compressImage
import cr.una.delta.frontend_kode.presentation.ui.components.cameraUtils.rememberCameraLauncher
import cr.una.delta.frontend_kode.presentation.ui.components.evaluations.EvaluationsTab
import cr.una.delta.frontend_kode.presentation.viewmodel.EvaluationState
import cr.una.delta.frontend_kode.presentation.viewmodel.EvaluationViewModel
import cr.una.delta.frontend_kode.presentation.viewmodel.ScannedRubric

/**
 * Evaluación de un curso: rúbricas + calculadora de notas. El botón de cámara
 * escanea una rúbrica (foto → Gemini) y deja los ítems para revisar/editar
 * antes de importarlos.
 */
@Composable
fun EvaluationScreen(
    navController: NavHostController,
    courseId: Long,
    courseName: String,
    paddingValues: PaddingValues,
    viewModel: EvaluationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val scanning by viewModel.scanning.collectAsState()
    val scanError by viewModel.scanError.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val consejo by viewModel.consejo.collectAsState()
    val consejoLoading by viewModel.consejoLoading.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(courseId) { viewModel.loadEvaluations(courseId) }

    val takePhoto = rememberCameraLauncher(
        context = context,
        onImageCaptured = { viewModel.scanRubric(compressImage(it)) },
        onError = { /* cámara cancelada; sin acción */ }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (val s = state) {
            is EvaluationState.Loading ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            is EvaluationState.Error ->
                Text(
                    text = "Error: ${s.message}",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )

            is EvaluationState.Empty ->
                EvaluationsTab(
                    rubrics = emptyList(),
                    assignments = emptyList(),
                    courseName = courseName,
                    grades = grades,
                    onGradeChange = { rubricId, value -> viewModel.onGradeChanged(rubricId, value) },
                    onCameraClick = { takePhoto() },
                    onBackClick = { navController.navigateUp() },
                    onAddRubric = { n, w, d -> viewModel.addRubric(courseId, n, w, d) },
                    consejo = consejo,
                    consejoLoading = consejoLoading,
                    onPedirConsejo = { meta, actual, falta, prom, alcanzable ->
                        viewModel.pedirConsejo(courseName, meta, actual, falta, prom, alcanzable)
                    }
                )

            is EvaluationState.Success ->
                EvaluationsTab(
                    rubrics = s.rubrics,
                    assignments = s.assignments,
                    courseName = courseName,
                    grades = grades,
                    onGradeChange = { rubricId, value -> viewModel.onGradeChanged(rubricId, value) },
                    onCameraClick = { takePhoto() },
                    onBackClick = { navController.navigateUp() },
                    onAddRubric = { n, w, d -> viewModel.addRubric(courseId, n, w, d) },
                    consejo = consejo,
                    consejoLoading = consejoLoading,
                    onPedirConsejo = { meta, actual, falta, prom, alcanzable ->
                        viewModel.pedirConsejo(courseName, meta, actual, falta, prom, alcanzable)
                    }
                )
        }

        // Cargando el OCR
        if (scanning) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Leyendo la rúbrica…")
                    }
                }
            }
        }
    }

    // Error del escaneo
    scanError?.let { msg ->
        if (scanResult == null && !scanning) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelScan() },
                title = { Text("No se pudo escanear") },
                text = { Text(msg) },
                confirmButton = { TextButton(onClick = { viewModel.cancelScan() }) { Text("Cerrar") } }
            )
        }
    }

    // Revisión de la rúbrica escaneada
    scanResult?.let { list ->
        RubricScanDialog(
            items = list,
            onName = viewModel::setScannedName,
            onWeight = viewModel::setScannedWeight,
            onGrade = viewModel::setScannedGrade,
            onRemove = viewModel::removeScanned,
            onCancel = { viewModel.cancelScan() },
            onImport = { viewModel.applyScan(courseId) }
        )
    }
}

@Composable
private fun RubricScanDialog(
    items: List<ScannedRubric>,
    onName: (Int, String) -> Unit,
    onWeight: (Int, String) -> Unit,
    onGrade: (Int, String) -> Unit,
    onRemove: (Int) -> Unit,
    onCancel: () -> Unit,
    onImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Rúbrica escaneada") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("Revisá y corregí antes de importar. Nota vacía = sin nota.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                items.forEachIndexed { i, r ->
                    OutlinedTextField(
                        value = r.name, onValueChange = { onName(i, it) },
                        label = { Text("Evaluación") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = r.weight, onValueChange = { onWeight(i, it) },
                            label = { Text("Peso %") }, singleLine = true, modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = r.grade, onValueChange = { onGrade(i, it) },
                            label = { Text("Nota") }, singleLine = true, modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onRemove(i) }) { Text("Quitar") }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
                val total = items.sumOf { it.weight.trim().replace(',', '.').toDoubleOrNull() ?: 0.0 }
                Text("Suma de pesos: ${total.toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = if (total == 100.0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(enabled = items.isNotEmpty(), onClick = onImport) { Text("Importar") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}
