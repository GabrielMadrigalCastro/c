package cr.una.delta.frontend_kode.presentation.ui.components.com

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import cr.una.delta.frontend_kode.domain.model.Task

@Composable
fun TaskCard(
    task: Task,
    onTaskChecked: (Task) -> Unit
) {
    var isChecked by remember { mutableStateOf(task.status.label.equals("DONE", ignoreCase = true)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
                isChecked = checked
                if (checked) {
                    onTaskChecked(task) // avisa al ViewModel que se marcó como hecha
                }
            },
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(task.title, style = MaterialTheme.typography.bodyMedium)

        }
    }
}
