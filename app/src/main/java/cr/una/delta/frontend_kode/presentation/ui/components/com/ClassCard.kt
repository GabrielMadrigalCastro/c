package cr.una.delta.frontend_kode.presentation.ui.components.com

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cr.una.delta.frontend_kode.domain.model.ClassSession

@Composable
fun ClassCard(cls: ClassSession) {
    val isVirtual = cls.modality.contains("Virtual", ignoreCase = true)
    // Colores derivados del tema (se ven bien en claro y oscuro).
    val badgeBg = if (isVirtual) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.secondaryContainer
    val badgeFg = if (isVirtual) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo con iniciales del lugar (o de la modalidad si no hay lugar).
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = badgeBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initials = cls.location.take(2).ifBlank { cls.modality.take(2) }.uppercase()
                Text(
                    text = initials,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeFg
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la clase
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Curso ID: ${cls.courseId}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${cls.startTime} - ${cls.endTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Lugar: ${cls.location.ifBlank { "—" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Modalidad
            Text(
                text = cls.modality,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Ícono de editar
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = "Editar clase",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
