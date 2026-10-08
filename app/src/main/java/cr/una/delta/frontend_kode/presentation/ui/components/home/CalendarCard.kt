package cr.una.delta.frontend_kode.presentation.ui.components.home

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import java.util.Calendar


@Composable
fun CalendarCard(
    selectedDate: Calendar,
    markedDates: Set<String> = emptySet(),
    onDateClick: (Calendar) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        CalendarMonth(selectedDate = selectedDate, markedDates = markedDates, onDateClick = onDateClick)
    }
}
