package cr.una.delta.frontend_kode.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.StickyNote2

import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector
) {
    data object Dashboard :
        BottomNavItem(route = NavRoutes.Home.ROUTE, icon = Icons.Outlined.Home)

    data object DashboardTeacher :
        BottomNavItem(route = NavRoutes.HomeTeacher.ROUTE, icon = Icons.Outlined.Home)

    data object TeacherCourses :
        BottomNavItem(route = NavRoutes.TeacherCourses.ROUTE, icon = Icons.Outlined.MenuBook)
    data object PlannerStudent :
        BottomNavItem(route = NavRoutes.PlannerStudent.ROUTE, icon = Icons.Outlined.EditCalendar)

    data object MoodLearning :
        BottomNavItem(route = NavRoutes.MoodLearning.ROUTE, icon = Icons.Outlined.Mood)

    data object Apuntes :
        BottomNavItem(route = NavRoutes.Apuntes.ROUTE, icon = Icons.Outlined.StickyNote2)

    data object Profile :
        BottomNavItem(route = NavRoutes.Settings.ROUTE, icon = Icons.Outlined.ManageAccounts)

    companion object {
        /** Devuelve los items del BottomNav según el rol */
        fun items(isTeacher: Boolean): List<BottomNavItem> {
            return if (isTeacher) {
                listOf(DashboardTeacher, TeacherCourses, Profile) // Profesor
            } else {
                listOf(Dashboard, PlannerStudent, Apuntes, Profile) // Estudiante
            }
        }
    }
}
