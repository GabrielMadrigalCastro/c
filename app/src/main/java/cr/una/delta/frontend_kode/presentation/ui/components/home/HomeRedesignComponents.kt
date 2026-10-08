package cr.una.delta.frontend_kode.presentation.ui.components.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cr.una.delta.frontend_kode.domain.model.BlockType

/** Encabezado de sección en mayúsculas (estilo mockup). */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/** Anillo de progreso del día (Canvas). `percent` de 0 a 100. */
@Composable
fun ProgressRing(percent: Int, modifier: Modifier = Modifier, ringSize: Int = 74) {
    val primary = MaterialTheme.colorScheme.primary
    val track = primary.copy(alpha = 0.18f)
    val ink2 = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier.size(ringSize.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(track, -90f, 360f, false, topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(
                primary, -90f, 360f * (percent.coerceIn(0, 100) / 100f), false,
                topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$percent%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primary)
            Text("del día", style = MaterialTheme.typography.labelSmall, color = ink2)
        }
    }
}

/** Color + ícono de un bloque según su tipo (usa color del curso si viene). */
@Composable
fun blockColor(type: BlockType, courseColor: Color?): Color = when (type) {
    BlockType.CLASS -> courseColor ?: MaterialTheme.colorScheme.primary
    BlockType.STUDY -> MaterialTheme.colorScheme.tertiary
    BlockType.MEAL -> MaterialTheme.colorScheme.secondary
    BlockType.TRAVEL -> MaterialTheme.colorScheme.onSurfaceVariant
    BlockType.PERSONAL -> MaterialTheme.colorScheme.secondary
}

fun blockIcon(type: BlockType): ImageVector = when (type) {
    BlockType.CLASS -> Icons.Filled.School
    BlockType.STUDY -> Icons.Filled.MenuBook
    BlockType.MEAL -> Icons.Filled.Restaurant
    BlockType.TRAVEL -> Icons.Filled.DirectionsBus
    BlockType.PERSONAL -> Icons.Filled.LocalCafe
}

/** Un bloque de la línea de tiempo: hora | riel | tarjeta lisa con barra de color. */
@Composable
fun TimelineBlock(
    start: String,
    end: String,
    title: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    showRemove: Boolean,
    onRemove: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Columna de horas
        Column(
            modifier = Modifier.width(46.dp).padding(top = 14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(start, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(end, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(6.dp))
        // Riel
        Box(
            Modifier.width(2.dp).fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Spacer(Modifier.width(6.dp))
        // Tarjeta
        Surface(
            modifier = Modifier.weight(1f).padding(vertical = 6.dp)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(4.dp).fillMaxHeight().background(color))
                Spacer(Modifier.width(11.dp))
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (subtitle.isNotBlank()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (showRemove) {
                    Box(
                        Modifier.padding(end = 6.dp).size(26.dp).clip(RoundedCornerShape(50)).clickable { onRemove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

/** Fila de tarea con checkbox redondeado, título tachable y chip de fecha. */
@Composable
fun TaskRow(
    title: String,
    dueLabel: String,
    urgent: Boolean,
    done: Boolean,
    onToggle: () -> Unit,
    onOpenDetail: (() -> Unit)? = null
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            val primary = MaterialTheme.colorScheme.primary
            Box(
                Modifier.padding(top = 1.dp).size(22.dp).clip(RoundedCornerShape(7.dp))
                    .background(if (done) primary else Color.Transparent)
                    .then(
                        if (done) Modifier
                        else Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.dp))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                if (dueLabel.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    val chipBg = if (urgent && !done) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    val chipInk = if (urgent && !done) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    Surface(shape = RoundedCornerShape(999.dp), color = chipBg) {
                        Text(dueLabel, style = MaterialTheme.typography.labelSmall, color = chipInk, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }
            if (onOpenDetail != null) {
                IconButton(onClick = onOpenDetail) {
                    Icon(Icons.Filled.Info, contentDescription = "Ver detalle", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
