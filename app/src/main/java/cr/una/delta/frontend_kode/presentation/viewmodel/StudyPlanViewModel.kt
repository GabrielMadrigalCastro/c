package cr.una.delta.frontend_kode.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cr.una.delta.frontend_kode.domain.model.DailyPlan
import cr.una.delta.frontend_kode.domain.model.BlockType
import cr.una.delta.frontend_kode.domain.repository.StudyPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StudyPlanViewModel @Inject constructor(
    private val repo: StudyPlanRepository
) : ViewModel() {

    private val _dailyPlan = MutableLiveData<DailyPlan?>()
    val dailyPlan: LiveData<DailyPlan?> = _dailyPlan

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ============================================================
    // 📅 Cargar o generar el plan del día actual
    // ============================================================
    fun loadTodayPlan(studentId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val today = LocalDate.now()

                // Intentar obtener plan existente
                val existingPlanResult = repo.getDailyPlan(studentId, today)
                val existingPlan = existingPlanResult.getOrNull()

                // Si no existe o está vacío, generar nuevo
                val plan = if (existingPlan == null || existingPlan.blocks.isEmpty()) {
                    val generated = repo.generateDailyPlan(
                        studentId = studentId,
                        assignmentId = 0L,
                        date = today
                    )
                    generated.getOrElse {
                        throw Exception("Error al generar plan diario: ${it.message}")
                    }
                } else {
                    existingPlan
                }

                _dailyPlan.value = plan
                println("✅ Plan del día (${plan.date}) generado con ${plan.blocks.size} bloques")

            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                println("❌ Error al generar plan diario: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    // ============================================================
    // ♻️ Replanificar automáticamente (cuando cambia una tarea)
    // ============================================================
    fun autoReplan(studentId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val today = LocalDate.now()

                val result = repo.generateDailyPlan(
                    studentId = studentId,
                    assignmentId = 0L,
                    date = today
                )

                val plan = result.getOrNull()
                if (plan != null) {
                    _dailyPlan.value = plan
                    println("♻️ Día replanificado automáticamente con ${plan.blocks.size} bloques.")
                } else {
                    println("⚠️ No se pudo replanificar el día.")
                }

            } catch (e: Exception) {
                _error.value = e.message
                println("❌ Error al replanificar el día: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    // ============================================================
    // 🔄 Replanificar manualmente
    // ============================================================
    fun replanify(studentId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val result = repo.generateDailyPlan(
                    studentId = studentId,
                    assignmentId = 0L,
                    date = LocalDate.now()
                )
                _dailyPlan.value = result.getOrNull()
                println("🔁 Día replanificado manualmente.")
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    // ============================================================
    // ❌ Limpiar estado de error
    // ============================================================
    fun clearError() {
        _error.value = null
    }

    // ============================================================
    // 🗑️ Eliminar tarea completada del plan IA actual
    // ============================================================
    fun removeTaskFromPlan(taskId: Long) {
        val currentPlan = _dailyPlan.value ?: return
        val updatedBlocks = currentPlan.blocks.filterNot { block ->
            block.type == BlockType.STUDY && block.id == taskId
        }
        _dailyPlan.value = currentPlan.copy(blocks = updatedBlocks)
        println("🗑️ Tarea $taskId eliminada del plan IA actual")
    }
}
