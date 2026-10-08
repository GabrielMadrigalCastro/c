package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.domain.model.*
import cr.una.delta.frontend_kode.presentation.ui.components.com.ClassCard
import cr.una.delta.frontend_kode.presentation.ui.components.home.*
import cr.una.delta.frontend_kode.presentation.viewmodel.TeacherHomeState
import cr.una.delta.frontend_kode.presentation.viewmodel.TeacherHomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun TeacherHomeScreen(
    navController: NavHostController,
    viewModel: TeacherHomeViewModel,
    paddingValues: PaddingValues
) {
    val state by viewModel.state.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val sdfTime = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val sdfDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadTeacherData()
    }

    val isRefreshing = state is TeacherHomeState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.loadTeacherData() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Calendario ---
            item {
                CalendarCard(
                    selectedDate = selectedDate,
                    onDateClick = { viewModel.selectDate(it) }
                )
            }

            // ===============================
            // ALERTAS DEL PROFESOR 🔔
            // ===============================
            val currentState = state
            val alerts = (currentState as? TeacherHomeState.Success)?.alerts ?: emptyList()

            item {
                Text(
                    text = "Alertas del día",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (alerts.isNotEmpty()) {
                items(alerts) { alert ->
                    AlertCard(alert)
                }
            } else {
                item {
                    Text(
                        text = "No hay alertas para hoy.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ===============================
            // CLASES DEL DÍA 📚
            // ===============================
            // Solo las clases de la fecha seleccionada en el calendario.
            val allClasses = (currentState as? TeacherHomeState.Success)?.classes ?: emptyList()
            val selectedKey = sdfDate.format(selectedDate.time)
            val classes = allClasses.filter { it.classDate.take(10) == selectedKey }

            item {
                Text(
                    text = "Clases del día",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (classes.isNotEmpty()) {
                items(classes) { classInfo ->
                    ClassCardItem(classInfo, navController)
                }
            } else {
                item {
                    Text(
                        text = "No hay clases programadas para este día.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ===============================
            // EVENTOS Y RECORDATORIOS
            // ===============================
            val nextEvent = (currentState as? TeacherHomeState.Success)?.nextEvent
            val reminders = (currentState as? TeacherHomeState.Success)?.reminders ?: emptyList()

            nextEvent?.let {
                item { Text("Próximo evento", style = MaterialTheme.typography.titleMedium) }
                item { NextEventCard(it, sdfTime) }
            }

            if (reminders.isNotEmpty()) {
                item { RemindersRow(reminders) }
            }

            if (nextEvent == null && classes.isEmpty() && reminders.isEmpty() && alerts.isEmpty()) {
                item {
                    Text(
                        "No hay información disponible para hoy.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ----------------------
// COMPONENTE DE ALERTAS
// ----------------------
@Composable
fun AlertCard(alert: Alert) {
    val (icon, color) = when (alert.severity.lowercase()) {
        "warning" -> Pair("⚠", MaterialTheme.colorScheme.tertiaryContainer)
        "error" -> Pair("❌", MaterialTheme.colorScheme.errorContainer)
        else -> Pair("🔔", MaterialTheme.colorScheme.secondaryContainer)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("$icon ${alert.title}", style = MaterialTheme.typography.titleMedium)
            Text(alert.message, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Creado: ${alert.createdAt.toLocalDate()}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ----------------------
// COMPONENTE DE CLASES
// ----------------------
@Composable
fun ClassCardItem(
    classInfo: ClassSession,
    navController: NavHostController
) {
    // Reutiliza el mismo diseño de tarjeta del calendario del estudiante.
    // Al tocarla, el profe puede cambiar la modalidad de esa clase.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    "change_modality/${classInfo.id}/${classInfo.modality}"
                )
            }
    ) {
        ClassCard(classInfo)
    }
}