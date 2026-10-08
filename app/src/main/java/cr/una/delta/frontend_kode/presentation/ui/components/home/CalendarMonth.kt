package cr.una.delta.frontend_kode.presentation.ui.components.home


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable

fun CalendarMonth(
    selectedDate: Calendar,
    markedDates: Set<String> = emptySet(),
    onDateClick: (Calendar) -> Unit
) {
    val monthTitleFmt = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }
    val dayKeyFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    // Fecha actual (hoy) para marcarla siempre distinta del día seleccionado.
    val todayKey = remember { dayKeyFmt.format(Calendar.getInstance().time) }

    // Mes mostrado: inicia en el mes de selectedDate
    var displayMonth by remember(selectedDate.timeInMillis) {
        mutableStateOf((selectedDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) })
    }

    val year = displayMonth.get(Calendar.YEAR)
    val month = displayMonth.get(Calendar.MONTH)
    val firstDayIndex = (displayMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        .get(Calendar.DAY_OF_WEEK) - 1 // 0..6, domingo=0
    val daysInMonth = displayMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Mes anterior / siguiente
    val prevYear = if (month == Calendar.JANUARY) year - 1 else year
    val prevMonth = if (month == Calendar.JANUARY) Calendar.DECEMBER else month - 1
    val nextYear = if (month == Calendar.DECEMBER) year + 1 else year
    val nextMonth = if (month == Calendar.DECEMBER) Calendar.JANUARY else month + 1

    val prevMax = Calendar.getInstance().apply {
        set(Calendar.YEAR, prevYear)
        set(Calendar.MONTH, prevMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

    val leading = firstDayIndex
    val trailing = (7 - ((leading + daysInMonth) % 7)) % 7

    data class DayCell(val cal: Calendar, val inCurrentMonth: Boolean)

    fun calOf(y: Int, m: Int, d: Int) = Calendar.getInstance().apply {
        set(Calendar.YEAR, y); set(Calendar.MONTH, m); set(Calendar.DAY_OF_MONTH, d)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    val cells = buildList {
        // Días del mes anterior visibles
        for (d in (prevMax - leading + 1)..prevMax) add(DayCell(calOf(prevYear, prevMonth, d), false))
        // Días del mes actual
        for (d in 1..daysInMonth) add(DayCell(calOf(year, month, d), true))
        // Días del mes siguiente visibles
        for (d in 1..trailing) add(DayCell(calOf(nextYear, nextMonth, d), false))
    }

    val weekdayHeaders = listOf("S","M","T","W","T","F","S")

    Column(Modifier.padding(12.dp)) {
        // Header con navegación de mes
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                displayMonth = (displayMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            }) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "Mes anterior", tint = MaterialTheme.colorScheme.primary) }

            Text(
                monthTitleFmt.format(displayMonth.time),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = {
                displayMonth = (displayMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            }) { Icon(Icons.Outlined.ChevronRight, contentDescription = "Mes siguiente", tint = MaterialTheme.colorScheme.primary) }
        }

        Spacer(Modifier.height(8.dp))

        // Encabezado de días
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            weekdayHeaders.forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(234.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items(cells) { day ->
                val isSelected =
                    day.cal.get(Calendar.YEAR)  == selectedDate.get(Calendar.YEAR) &&
                            day.cal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                            day.cal.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)

                val isToday = dayKeyFmt.format(day.cal.time) == todayKey

                val textColor =
                    if (day.inCurrentMonth) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(2.dp)
                        .clickable {
                            onDateClick(day.cal)
                            // Si tocas un día de otro mes, también cambia el mes mostrado
                            if (!day.inCurrentMonth) {
                                displayMonth = (day.cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Día seleccionado: círculo morado relleno.
                    if (isSelected) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    } else if (isToday) {
                        // Hoy (si no está seleccionado): aro morado.
                        Box(
                            Modifier
                                .size(32.dp)
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                    // Número del día centrado
                    Text(
                        "${day.cal.get(Calendar.DAY_OF_MONTH)}",
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> textColor
                        },
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    // Punto que marca los días con clases
                    if (dayKeyFmt.format(day.cal.time) in markedDates) {
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 3.dp)
                                .size(5.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

