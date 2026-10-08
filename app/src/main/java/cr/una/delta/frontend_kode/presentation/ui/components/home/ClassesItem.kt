package cr.una.delta.frontend_kode.presentation.ui.components.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cr.una.delta.frontend_kode.domain.model.ClassSession

@Composable
fun ClassItem(c: ClassSession) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 🔹 Título del curso (usando courseId por ahora)
            Text(
                text = "Curso ID: ${c.courseId}",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )

            // 🔹 Horario y modalidad
            Text(
                text = "${c.startTime} - ${c.endTime}   (${c.modality})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            // 🔹 Ubicación
            Text(
                text = "Ubicación: ${c.location}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            // 🔹 Fecha (opcional)
            Text(
                text = "Fecha: ${c.classDate}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
