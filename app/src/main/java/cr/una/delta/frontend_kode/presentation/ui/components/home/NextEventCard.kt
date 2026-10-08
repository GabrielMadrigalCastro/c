package cr.una.delta.frontend_kode.presentation.ui.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cr.una.delta.frontend_kode.domain.model.Event
import java.text.SimpleDateFormat

@Composable
fun NextEventCard(nextEvent: Event?, sdfTime: SimpleDateFormat) {
    if (nextEvent != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = nextEvent.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = sdfTime.format(nextEvent.dateTime),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        Text(
            text = "Sin eventos",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(12.dp)
        )
    }
}
