package cr.una.delta.frontend_kode.presentation.ui.components.home

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun FormatHour(date: Date): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)