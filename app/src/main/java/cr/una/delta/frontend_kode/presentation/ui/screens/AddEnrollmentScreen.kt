package cr.una.delta.frontend_kode.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cr.una.delta.frontend_kode.data.local.SessionManager
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.domain.model.Enrollment
import cr.una.delta.frontend_kode.presentation.viewmodel.CourseState
import cr.una.delta.frontend_kode.presentation.viewmodel.CourseViewModel
import cr.una.delta.frontend_kode.presentation.viewmodel.EnrollmentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEnrollmentScreen(
    onBack: () -> Unit,
    onCreateCourse: () -> Unit = {},
    courseViewModel: CourseViewModel = hiltViewModel(),
    enrollmentViewModel: EnrollmentViewModel = hiltViewModel(),
    sessionManager: SessionManager = SessionManager(LocalContext.current)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val courseState by courseViewModel.state.collectAsState()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }

    // 🔥 Estado donde guardamos los cursos matriculados del estudiante
    var enrolledCourses by remember { mutableStateOf<List<Course>>(emptyList()) }

    // 🚀 Cargar cursos disponibles + cursos del estudiante
    LaunchedEffect(Unit) {
        val user = sessionManager.getUser()   // ← esto SÍ es suspend, aquí es válido

        if (user != null) {
            courseViewModel.loadAllCourses()
            val result = courseViewModel.getCoursesOfStudent(user.id)

            enrolledCourses = result.getOrDefault(emptyList())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar curso") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // Crear tu propia materia (a mano)
            Button(
                onClick = onCreateCourse,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Crear mi propia materia")
            }

            when (courseState) {
                is CourseState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is CourseState.Error -> {
                    Text(
                        text = "Error: ${(courseState as CourseState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is CourseState.Success -> {
                    val courses = (courseState as CourseState.Success).courses

                    Text(
                        "Seleccione un curso para matricular:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn {
                        items(courses) { course ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { selectedCourse = course }
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(course.courseName, style = MaterialTheme.typography.titleMedium)
                                    Text("Código: ${course.courseCode ?: "N/A"}")
                                }
                            }
                        }
                    }
                }

                is CourseState.Empty -> {
                    Text("No hay cursos disponibles.")
                }
            }

            Spacer(Modifier.height(20.dp))

            // ============================================================
            // 🔥 BOTÓN MATRICULAR
            // ============================================================
            selectedCourse?.let { course ->

                Button(
                    onClick = {
                        scope.launch {

                            val user = sessionManager.getUser()
                            if (user == null) {
                                Toast.makeText(context, "Error: sesión no encontrada", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            // 🛑 VALIDACIÓN DE DUPLICADO
                            val alreadyEnrolled =
                                enrolledCourses.any { it.courseId == course.courseId }

                            if (alreadyEnrolled) {
                                Toast.makeText(
                                    context,
                                    "⚠️ Ya estás matriculado en este curso",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            // 🔥 Crear matrícula
                            val enrollment = Enrollment(
                                courseId = course.courseId,
                                studentId = user.id
                            )

                            enrollmentViewModel.createEnrollment(enrollment)

                            Toast.makeText(
                                context,
                                "Matriculado en ${course.courseName}",
                                Toast.LENGTH_SHORT
                            ).show()

                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Matricular curso seleccionado")
                }
            }
        }
    }
}
