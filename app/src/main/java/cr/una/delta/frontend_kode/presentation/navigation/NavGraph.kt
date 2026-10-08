package cr.una.delta.frontend_kode.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cr.una.delta.frontend_kode.presentation.ui.screens.*
import cr.una.delta.frontend_kode.presentation.viewmodel.*
import cr.una.delta.frontend_kode.presentation.ui.screens.auth.LoginScreen
import cr.una.delta.frontend_kode.presentation.ui.screens.auth.RegisterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    moodLearningViewModel: MoodLearningViewModel,
    teacherHomeViewModel: TeacherHomeViewModel,
    plannerstudentViewModel: PlannerStudentViewModel,
    createTaskViewModel: CreateTaskViewModel,
    themeViewModel: ThemeViewModel,
    paddingValues: PaddingValues,
    startDestination: String = NavRoutes.Login.ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // -------- AUTH --------
        composable(NavRoutes.Login.ROUTE) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(NavRoutes.Register.ROUTE) {
            RegisterScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        // -------- APP FLOW --------
        composable(NavRoutes.Home.ROUTE) {
            HomeScreen(
                navController = navController,
                homeViewModel = homeViewModel,
                paddingValues = paddingValues
            )
        }


        composable(NavRoutes.HomeTeacher.ROUTE) {
            TeacherHomeScreen(
                navController = navController,
                viewModel = teacherHomeViewModel,
                paddingValues = paddingValues
            )
        }

        composable(NavRoutes.PlannerStudent.ROUTE) {
            PlannerStudentScreen(
                navController = navController,
                viewModel = plannerstudentViewModel,
                paddingValues = paddingValues
            )
        }

        composable(NavRoutes.MoodLearning.ROUTE) {
            MoodLearningScreen(
                navController = navController,
                viewModel = moodLearningViewModel,
                paddingValues = paddingValues
            )
        }

        composable(NavRoutes.Settings.ROUTE) {
            SettingsScreen(
                navController = navController,
                paddingValues = paddingValues,
                authViewModel = authViewModel,
                themeViewModel = themeViewModel,
                onLogout = {
                    navController.navigate(NavRoutes.Login.ROUTE) {
                        popUpTo(0) { inclusive = true } // Limpiar todo el back stack
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.Profile.ROUTE) {
            ProfileScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        composable(NavRoutes.Preferences.ROUTE) {
            PreferencesScreen(
                navController = navController,
                themeViewModel = themeViewModel
            )
        }

        composable(NavRoutes.Subscription.ROUTE) {
            SubscriptionScreen(navController = navController)
        }

        composable(NavRoutes.CreateTask.ROUTE) {
            CreateTaskScreen(
                navController = navController,
                viewModel = createTaskViewModel,
                paddingValues = paddingValues
            )
        }
        composable(
            route = NavRoutes.ChangeModality.ROUTE,
            arguments = listOf(
                navArgument("classId") { type = NavType.LongType },
                navArgument("currentModality") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
            val currentModality = backStackEntry.arguments?.getString("currentModality") ?: "PRESENCIAL"

            ChangeModalityScreen(
                navController = navController,
                classId = classId,
                currentModality = currentModality,
                viewModel = teacherHomeViewModel,
                paddingValues = paddingValues
            )
        }
        composable("add_enrollment") {
            AddEnrollmentScreen(
                onBack = { navController.popBackStack() },
                onCreateCourse = { navController.navigate(NavRoutes.CreateCourse.ROUTE) }
            )
        }

        composable(NavRoutes.CreateCourse.ROUTE) {
            CreateCourseScreen(
                navController = navController,
                paddingValues = paddingValues
            )
        }

        composable(
            route = NavRoutes.EditCourse.ROUTE,
            arguments = listOf(
                navArgument("courseId") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType },
                navArgument("color") { type = NavType.StringType }
            )
        ) { entry ->
            EditCourseScreen(
                navController = navController,
                courseId = entry.arguments?.getLong("courseId") ?: 0L,
                initialName = entry.arguments?.getString("name") ?: "",
                initialColor = entry.arguments?.getString("color") ?: "#A5C8FF",
                paddingValues = paddingValues
            )
        }

        composable(
            route = NavRoutes.Evaluations.ROUTE,
            arguments = listOf(
                navArgument("courseId") { type = NavType.LongType },
                navArgument("courseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L
            val courseName = backStackEntry.arguments?.getString("courseName") ?: ""
            EvaluationScreen(
                navController = navController,
                courseId = courseId,
                courseName = courseName,
                paddingValues = paddingValues
            )
        }

        // -------- PLAN DEL DÍA INTELIGENTE --------
        composable(NavRoutes.PlanDia.ROUTE) {
            PlanDiaScreen(
                navController = navController,
                paddingValues = paddingValues
            )
        }

        // -------- PROFESOR: MIS CURSOS --------
        composable(NavRoutes.TeacherCourses.ROUTE) {
            TeacherCoursesScreen(
                navController = navController,
                paddingValues = paddingValues
            )
        }

        // -------- PROFESOR: CURSO (rúbricas + estudiantes + notas) --------
        composable(
            route = NavRoutes.TeacherCourse.ROUTE,
            arguments = listOf(
                navArgument("courseId") { type = NavType.LongType },
                navArgument("courseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            TeacherCourseScreen(
                navController = navController,
                courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L,
                courseName = backStackEntry.arguments?.getString("courseName") ?: "",
                paddingValues = paddingValues
            )
        }

        // -------- APUNTES (página propia, todos los cursos) --------
        composable(NavRoutes.Apuntes.ROUTE) {
            ApuntesHomeScreen(
                navController = navController,
                paddingValues = paddingValues
            )
        }

        // -------- METAS Y PROGRESO --------
        composable(NavRoutes.GradeGoals.ROUTE) {
            GradeGoalsScreen(
                navController = navController,
                paddingValues = paddingValues
            )
        }

        // -------- ESCANEAR A APUNTE (OCR con cámara) --------
        composable(
            route = "scan_note/{courseId}/{courseName}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.LongType },
                navArgument("courseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ScanNoteScreen(
                navController = navController,
                courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L,
                courseName = backStackEntry.arguments?.getString("courseName") ?: "",
                paddingValues = paddingValues
            )
        }

        // -------- EDITOR DE APUNTE (pantalla completa) --------
        composable(
            route = NavRoutes.NoteEditor.ROUTE,
            arguments = listOf(
                navArgument("courseId") { type = NavType.LongType },
                navArgument("noteId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            NoteEditorScreen(
                navController = navController,
                courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L,
                noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L,
                paddingValues = paddingValues
            )
        }

        // -------- APUNTES (por curso) --------
        composable(
            route = "apuntes/{courseId}/{courseName}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.LongType },
                navArgument("courseName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ApuntesScreen(
                navController = navController,
                courseId = backStackEntry.arguments?.getLong("courseId") ?: 0L,
                courseName = backStackEntry.arguments?.getString("courseName") ?: "",
                paddingValues = paddingValues
            )
        }

    }

}