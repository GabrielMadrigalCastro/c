package cr.una.delta.frontend_kode.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import cr.una.delta.frontend_kode.data.local.SessionManager
import cr.una.delta.frontend_kode.domain.model.ClassSession
import cr.una.delta.frontend_kode.domain.model.Course
import cr.una.delta.frontend_kode.presentation.ui.components.com.HomeTabs
import cr.una.delta.frontend_kode.presentation.ui.components.courses.CoursesTab
import cr.una.delta.frontend_kode.presentation.ui.components.home.*
import cr.una.delta.frontend_kode.presentation.viewmodel.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun PlannerStudentScreen(
    navController: NavHostController,
    viewModel: PlannerStudentViewModel,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val plannerState by viewModel.state.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val courseViewModel: CourseViewModel = hiltViewModel()
    val enrollmentViewModel: EnrollmentViewModel = hiltViewModel()
    val courseState by courseViewModel.state.collectAsState()

    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    // rememberSaveable: al entrar a un curso/editar y volver, se mantiene el tab (Cursos).
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MES) }

    // Diálogos de gestión de clases
    var actionSession by remember { mutableStateOf<ClassSession?>(null) }
    var editSession by remember { mutableStateOf<ClassSession?>(null) }

    val isRefreshing = plannerState is PlannerState.Loading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            viewModel.loadPlannerData(force = true)
            scope.launch {
                val user = sessionManager.getUser()
                user?.let { courseViewModel.loadCourses(it.id, force = true) }
            }
        }
    )

    // Carga una sola vez (los ViewModel guardan el estado al reentrar a la pantalla).
    LaunchedEffect(Unit) {
        viewModel.loadPlannerData()
        val user = sessionManager.getUser()
        user?.let { courseViewModel.loadCourses(it.id) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .pullRefresh(pullRefreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 🔹 Submenú: Planner | Cursos
            HomeTabs(selectedTabIndex, listOf("Planner", "Cursos")) {
                selectedTabIndex = it
            }

            when (selectedTabIndex) {
                // ----------------------------------------------------------
                // 🔹 TAB 1: PLANNER (Calendario Mes / Semana / Día)
                // ----------------------------------------------------------
                0 -> {
                    when (plannerState) {
                        is PlannerState.Error -> PlaceholderScreen(
                            "Error: ${(plannerState as PlannerState.Error).message}"
                        )
                        // El calendario SIEMPRE se muestra (incluso mientras carga o si no hay clases).
                        // El spinner de carga aparece arriba con el PullRefreshIndicator.
                        else -> {
                            val classes = (plannerState as? PlannerState.Success)?.classes ?: emptyList()
                            val tasks = (plannerState as? PlannerState.Success)?.tasks ?: emptyList()
                            val courses: List<Course> =
                                (courseState as? CourseState.Success)?.courses ?: emptyList()

                            val keyFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

                            val activeTasks = tasks.filter { it.status.id != 3L }
                            val markedDates = remember(classes, activeTasks) {
                                (classes.map { it.classDate } +
                                        activeTasks.map { keyFmt.format(it.dueDate) }).toSet()
                            }

                            // En la vista DÍA la agenda es SIEMPRE hoy; en Mes/Semana es el día tocado.
                            val agendaCal =
                                if (viewMode == CalendarViewMode.DIA) Calendar.getInstance() else selectedDate
                            val agendaKey = keyFmt.format(agendaCal.time)
                            val dayClasses = classes.filter { it.classDate == agendaKey }
                            val dayTasks = activeTasks.filter { keyFmt.format(it.dueDate) == agendaKey }
                            val courseNameFor: (Long) -> String = { id ->
                                courses.find { it.courseId == id }?.courseName ?: "Curso"
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                CalendarViewToggle(
                                    selected = viewMode,
                                    onSelect = { viewMode = it }
                                )

                                Spacer(Modifier.height(12.dp))

                                when (viewMode) {
                                    CalendarViewMode.MES -> CalendarCard(
                                        selectedDate = selectedDate,
                                        markedDates = markedDates,
                                        onDateClick = { viewModel.selectDate(it) }
                                    )
                                    CalendarViewMode.SEMANA -> WeekStrip(
                                        selectedDate = selectedDate,
                                        markedDates = markedDates,
                                        onDateClick = { viewModel.selectDate(it) }
                                    )
                                    CalendarViewMode.DIA -> TodayHeader(today = Calendar.getInstance())
                                }

                                Spacer(Modifier.height(16.dp))

                                DayAgenda(
                                    classes = dayClasses,
                                    tasks = dayTasks,
                                    courseNameFor = courseNameFor,
                                    onClassClick = { actionSession = it }
                                )
                            }
                        }
                    }
                }

                // ----------------------------------------------------------
                // 🔹 TAB 2: CURSOS
                // ----------------------------------------------------------
                1 -> {
                    when (courseState) {
                        is CourseState.Loading -> LoadingContent()
                        is CourseState.Empty -> PlaceholderScreen("No tienes cursos matriculados aún")
                        is CourseState.Success -> {
                            val courses = (courseState as CourseState.Success).courses
                            CoursesTab(
                                courses = courses,
                                onCourseClick = { course ->
                                    navController.navigate(
                                        "evaluations/${course.courseId}/${android.net.Uri.encode(course.courseName)}"
                                    )
                                },
                                onEditCourse = { c ->
                                    navController.navigate(
                                        "edit_course/${c.courseId}/${android.net.Uri.encode(c.courseName)}/${android.net.Uri.encode(c.courseColor ?: "#A5C8FF")}"
                                    )
                                },
                                onApuntes = { c ->
                                    navController.navigate(
                                        "apuntes/${c.courseId}/${android.net.Uri.encode(c.courseName)}"
                                    )
                                },
                                onAddEnrollment = { navController.navigate("add_enrollment") },
                                viewModel = courseViewModel,
                                enrollmentViewModel = enrollmentViewModel
                            )
                        }
                        is CourseState.Error -> PlaceholderScreen(
                            "Error: ${(courseState as CourseState.Error).message}"
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    // ------- Diálogos de gestión de clase -------
    actionSession?.let { s ->
        val name = (courseState as? CourseState.Success)?.courses
            ?.find { it.courseId == s.courseId }?.courseName ?: "Clase"
        ClassActionsDialog(
            session = s,
            courseName = name,
            onEdit = { editSession = s; actionSession = null },
            onSetModality = { m -> viewModel.setModality(s.id, m); actionSession = null },
            onDelete = { viewModel.deleteClass(s.id); actionSession = null },
            onDismiss = { actionSession = null }
        )
    }

    editSession?.let { s ->
        EditClassDialog(
            session = s,
            onSave = { st, et, loc, mod ->
                viewModel.updateClass(s, st, et, loc, mod)
                editSession = null
            },
            onDismiss = { editSession = null }
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
