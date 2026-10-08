package cr.una.delta.frontend_kode.presentation.ui.components.com

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cr.una.delta.frontend_kode.domain.model.ClassSession
import cr.una.delta.frontend_kode.domain.model.Task
import cr.una.delta.frontend_kode.presentation.ui.components.home.CalendarMonth
import java.util.Calendar

@Composable
fun HomePlannerTab(
    selectedDate: Calendar,
    classes: List<ClassSession>,
    tasks: List<Task>,
    onDateSelected: (Calendar) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddClassClick: () -> Unit,
    onTaskChecked: (Task) -> Unit,
    showEmptyMessage: Boolean = false   // <-- nuevo parámetro
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CalendarMonth(
                selectedDate = selectedDate,
                onDateClick = onDateSelected
            )
        }

        // Si no hay clases ni tareas y showEmptyMessage es true
        if (showEmptyMessage) {
            item {
                PlaceholderText("No hay clases ni tareas")
            }
        } else {
            // Sección de clases
            item { SectionTitle("Clases del día") }
            if (classes.isEmpty()) {
                item { PlaceholderText("No hay clases") }
            } else {
                items(classes) { ClassCard(it) }
            }

            // Sección de tareas
            item { SectionTitle("Pendientes del día") }
            if (tasks.isEmpty()) {
                item { PlaceholderText("No hay tareas") }
            } else {
                items(tasks) { task -> TaskCard(task = task, onTaskChecked = { onTaskChecked(task) }) }
            }
        }

        item { AddCardsRow(onAddTaskClick, onAddClassClick) }
    }
}


@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun AddCardsRow(onAddTaskClick: () -> Unit, onAddClassClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AddCard(
            text = "Agrega tareas",
            color = Color(0xFFE8F5E8),
            iconTint = Color(0xFF4CAF50),
            icon = Icons.Outlined.EditNote,
            onClick = onAddTaskClick
        )
        AddCard(
            text = "Agrega cursos",
            color = Color(0xFFF3E5F5),
            iconTint = Color(0xFF9C27B0),
            icon = Icons.Outlined.School,
            onClick = onAddClassClick
        )
    }
}

@Composable
private fun RowScope.AddCard(
    text: String,
    color: Color,
    iconTint: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                tint = iconTint
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlaceholderText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}
