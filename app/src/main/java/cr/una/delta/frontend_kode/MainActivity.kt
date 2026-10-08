package cr.una.delta.frontend_kode

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import cr.una.delta.frontend_kode.presentation.navigation.NavGraph
import cr.una.delta.frontend_kode.presentation.navigation.NavRoutes
import cr.una.delta.frontend_kode.presentation.ui.components.com.BottomNavigationBar
import cr.una.delta.frontend_kode.presentation.ui.components.com.KodeTopBar
import cr.una.delta.frontend_kode.presentation.ui.theme.FrontendkodeTheme
import cr.una.delta.frontend_kode.presentation.viewmodel.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val moodLearningViewModel: MoodLearningViewModel by viewModels()
    private val teacherHomeViewModel: TeacherHomeViewModel by viewModels()
    private val plannerStudentViewModel: PlannerStudentViewModel by viewModels()
    private val createTaskViewModel: CreateTaskViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido
        } else {
            // Permiso denegado
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso de notificaciones concedido
        } else {
            // Permiso denegado
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestCameraPermission()
        checkAndRequestNotificationPermission()

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            val isDarkMode = when (themeMode) {
                cr.una.delta.frontend_kode.presentation.viewmodel.ThemeMode.SYSTEM ->
                    androidx.compose.foundation.isSystemInDarkTheme()
                cr.una.delta.frontend_kode.presentation.viewmodel.ThemeMode.LIGHT -> false
                cr.una.delta.frontend_kode.presentation.viewmodel.ThemeMode.DARK -> true
            }

            FrontendkodeTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val authUiState by authViewModel.uiState.collectAsState()
                val sessionChecked by authViewModel.sessionChecked.collectAsState()

                if (!sessionChecked) {
                    // Mientras se revisa la sesión guardada, mostramos un loader
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Si hay sesión guardada, entra directo al home (sin pedir login otra vez)
                    val startDestination = if (authUiState.loggedUser != null) {
                        if (authUiState.isTeacher) NavRoutes.HomeTeacher.ROUTE else NavRoutes.Home.ROUTE
                    } else {
                        NavRoutes.Login.ROUTE
                    }

                    // Ruta actual
                    val backStackEntry = navController.currentBackStackEntryAsState().value
                    val currentRoute = backStackEntry?.destination?.route?.substringBefore("?")

                    // Pantallas de autenticación: sin barras.
                    val isAuth = currentRoute == NavRoutes.Login.ROUTE ||
                            currentRoute == NavRoutes.Register.ROUTE
                    // Estas tienen su propio top bar (editor y crear tarea): ocultamos el global.
                    val hasOwnTopBar = currentRoute == NavRoutes.CreateTask.ROUTE ||
                            currentRoute?.startsWith("note_editor") == true

                    val hideTopBar = isAuth || hasOwnTopBar
                    // La barra inferior (Home · Planner · Apuntes · Perfil) siempre va, salvo en auth.
                    val hideBottomBar = isAuth

                    Scaffold(
                        topBar = { if (!hideTopBar) KodeTopBar() },
                        bottomBar = {
                            if (!hideBottomBar) {
                                BottomNavigationBar(
                                    navController = navController,
                                    isTeacher = authUiState.isTeacher
                                )
                            }
                        }
                    ) { innerPadding ->
                        val contentPadding = when {
                            isAuth -> PaddingValues(0.dp)
                            // Estas pantallas manejan su top bar; solo reservamos abajo la barra de navegación.
                            hasOwnTopBar -> PaddingValues(bottom = innerPadding.calculateBottomPadding())
                            else -> innerPadding
                        }
                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel,
                            homeViewModel = homeViewModel,
                            moodLearningViewModel = moodLearningViewModel,
                            teacherHomeViewModel = teacherHomeViewModel,
                            plannerstudentViewModel = plannerStudentViewModel,
                            createTaskViewModel = createTaskViewModel,
                            themeViewModel = themeViewModel,
                            paddingValues = contentPadding,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Ya tiene permiso
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Ya tiene permiso
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}