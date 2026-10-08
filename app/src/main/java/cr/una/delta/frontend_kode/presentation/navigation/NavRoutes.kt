package cr.una.delta.frontend_kode.presentation.navigation

sealed class NavRoutes {
    data object Home : NavRoutes() { const val ROUTE = "home" }
    data object MoodLearning : NavRoutes() { const val ROUTE = "moodlearning" }
    data object PlannerStudent : NavRoutes() { const val ROUTE = "plannerStudent" }
    data object HomeTeacher : NavRoutes() { const val ROUTE = "homeTeacher" }
    data object Settings : NavRoutes() { const val ROUTE = "settings" }
    data object Profile : NavRoutes() { const val ROUTE = "profile" }
    data object Preferences : NavRoutes() { const val ROUTE = "preferences" }
    data object Subscription : NavRoutes() { const val ROUTE = "subscription" }
    data object CreateTask : NavRoutes() { const val ROUTE = "create_task" }
    data object Evaluations : NavRoutes() { const val ROUTE = "evaluations/{courseId}/{courseName}" }
    data object CreateCourse : NavRoutes() { const val ROUTE = "create_course" }
    data object EditCourse : NavRoutes() { const val ROUTE = "edit_course/{courseId}/{name}/{color}" }
    data object Apuntes : NavRoutes() { const val ROUTE = "apuntes_home" }
    data object PlanDia : NavRoutes() { const val ROUTE = "plan_dia" }
    data object GradeGoals : NavRoutes() { const val ROUTE = "grade_goals" }
    data object TeacherCourses : NavRoutes() { const val ROUTE = "teacher_courses" }
    data object TeacherCourse : NavRoutes() { const val ROUTE = "teacher_course/{courseId}/{courseName}" }
    data object NoteEditor : NavRoutes() { const val ROUTE = "note_editor/{courseId}/{noteId}" }
    // Authentication
    data object Login : NavRoutes() { const val ROUTE = "login" }
    data object Register : NavRoutes() { const val ROUTE = "register" }
    data object ChangeModality : NavRoutes() { const val ROUTE = "change_modality/{classId}/{currentModality}" }

}
