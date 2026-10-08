package cr.una.delta.frontend_kode.data.local

import android.content.Context

/**
 * Guarda en el dispositivo la fecha PERSONAL que el estudiante le pone a una
 * rúbrica que no trae fecha del profesor. Solo sirve para el plan del propio
 * estudiante: no se comparte con otros ni se sube al backend.
 *
 * Clave = rubricId, valor = "yyyy-MM-dd".
 */
object StudentRubricDates {
    private const val PREFS = "kode_rubric_dates"

    fun get(context: Context, rubricId: Long): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(rubricId.toString(), null)
            ?.takeIf { it.isNotBlank() }

    fun set(context: Context, rubricId: Long, date: String?) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (date.isNullOrBlank()) editor.remove(rubricId.toString())
        else editor.putString(rubricId.toString(), date)
        editor.apply()
    }
}
