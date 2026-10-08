package cr.una.delta.frontend_kode.presentation.ui.components.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cr.una.delta.frontend_kode.domain.model.Alert

@Composable
fun AlertCard(alert: Alert) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(alert.title, color = MaterialTheme.colorScheme.error)
            Text(alert.message, color = MaterialTheme.colorScheme.error)
        }
    }
}
