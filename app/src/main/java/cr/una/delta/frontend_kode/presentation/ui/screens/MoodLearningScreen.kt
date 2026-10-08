package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cr.una.delta.frontend_kode.presentation.ui.components.moodLeraning.EmojiMoodBar
import cr.una.delta.frontend_kode.presentation.viewmodel.MoodLearningViewModel
import cr.una.delta.frontend_kode.presentation.viewmodel.MoodState

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MoodLearningScreen(
    navController: NavController,
    viewModel: MoodLearningViewModel,
    paddingValues: PaddingValues
) {
    val uiState by viewModel.state.collectAsState()
    val moods by viewModel.moods.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val showDialog = remember { mutableStateOf(false) }

    val tabs = listOf("MoodLearning", "Ranking de materias")
    val selectedTabIndex = remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(12.dp)
    ) {
        // -------- Barra de volver --------
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
        }

        // -------- Tabs superiores --------
        TabRow(
            selectedTabIndex = selectedTabIndex.intValue,
            containerColor = Color.Transparent,
            contentColor = Color.Black,
            indicator = { positions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(positions[selectedTabIndex.intValue]),
                    color = Color.Black,
                    height = 2.dp
                )
            }
        ) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = selectedTabIndex.intValue == i,
                    onClick = { selectedTabIndex.intValue = i },
                    text = {
                        Text(
                            text = title,
                            style = if (selectedTabIndex.intValue == i)
                                MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            else
                                MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // -------- Contenido principal --------
        when (uiState) {
            is MoodState.Loading -> LoadingContent()
            is MoodState.Error -> {
                val msg = (uiState as MoodState.Error).message
                ErrorContent(msg)
            }
            is MoodState.Success -> {
                val data = uiState as MoodState.Success
                MoodLearningContent(
                    viewModel,
                    data,
                    moods,
                    showDialog
                )
            }
            else -> Text("Cargando datos iniciales...")
        }
    }

    // ✅ Dialogo tipo popup (cuadro pequeño) con fondo gris semitransparente
    if (showDialog.value && selectedMood != null) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) { Text("OK") }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Emoji opcional según el score
                    Text(
                        when (selectedMood?.score) {
                            0 -> "😡"
                            1 -> "😟"
                            2 -> "😐"
                            3 -> "🙂"
                            else -> "😁"
                        },
                        fontSize = 40.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = selectedMood!!.message,
                        textAlign = TextAlign.Center
                    )
                }
            },
            containerColor = Color.White,
            shape = MaterialTheme.shapes.medium // bordes redondeados
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun MoodLearningContent(
    viewModel: MoodLearningViewModel,
    moodState: MoodState.Success,
    moods: List<cr.una.delta.frontend_kode.domain.model.MoodLevel>,
    showDialog: MutableState<Boolean>
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            EmojiMoodBar(
                value = moodState.mood?.score ?: 0,
                onChange = { score: Int ->
                    // buscar el MoodLevel que coincide con el score
                    moods.firstOrNull { it.score == score }?.let {
                        viewModel.setMood(it)
                        showDialog.value = true
                    }
                }
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    Text("Selecciona materia:")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        moodState.subjects.forEach { s ->
            FilterChip(
                selected = moodState.selectedSubject?.id == s.id,
                onClick = { viewModel.selectSubject(s) },
                label = { Text(s.name) }
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    Text("Selecciona los temas:")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        moodState.topics.forEach { t ->
            AssistChip(onClick = { /* seleccionar tema */ }, label = { Text(t.name) })
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
