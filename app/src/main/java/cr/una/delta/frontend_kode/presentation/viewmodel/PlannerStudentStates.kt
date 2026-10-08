package cr.una.delta.frontend_kode.presentation.viewmodel

import cr.una.delta.frontend_kode.domain.model.ClassSession
import cr.una.delta.frontend_kode.domain.model.Task

// Estado de clases
sealed class ClassesState {
    object Initial : ClassesState()
    object Loading : ClassesState()
    data class Success(val classes: List<ClassSession>) : ClassesState()
    data class Error(val message: String) : ClassesState()
}

// Estado de tareas
sealed class TasksState {
    object Initial : TasksState()
    object Loading : TasksState()
    data class Success(val tasks: List<Task>) : TasksState()
    data class Error(val message: String) : TasksState()
}
